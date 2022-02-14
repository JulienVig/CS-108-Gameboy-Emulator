/**
* Représente le processeur du Game Boy.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.cpu.Opcode.Kind;
import ch.epfl.gameboj.component.memory.Ram;

public final class Cpu implements Component, Clocked {

    private long nextNonIdleCycle;
    private static final long INTERRUPTIONS_CYCLES = 5;
    private static final int PREFIX_OPCODE = 0xCB;
    private static final int OPCODE_TABLE_SIZE = 0x100;
    private int PC;
    private int SP;
    private final RegisterFile<Reg> registerFile;

    private Bus bus;
    private final Ram highRAM;

    private boolean IME;
    private int IE;
    private int IF;

    private int nextPC;

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    /**
     * Enumération contenant 4 valeurs qui correspondent aux 4 sources possible d'un fanion.
     * 
     */
    private enum FlagSrc {
        V0, V1, ALU, CPU;
    }

    /**
     * Enumération permettant de décrire les différentes interruptions du processeur et de leur associer à chacune un
     * bit
     */
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    /**
     * Construit un nouveau Cpu en initialisant ses attributs.
     */
    public Cpu() {
        registerFile = new RegisterFile<>(Reg.values());
        highRAM = new Ram(AddressMap.HIGH_RAM_SIZE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_IE)
            return IE;
        if (address == AddressMap.REG_IF)
            return IF;
        if (address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END) {
            return highRAM.read(address - AddressMap.HIGH_RAM_START);
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_IE) {
            IE = data;
        }
        if (address == AddressMap.REG_IF) {
            IF = data;
        }
        if (address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END) {
            highRAM.write(address - AddressMap.HIGH_RAM_START, data);
        }
    }

    /**
     * Met à 1 le bit correspondant à l'interruption passée en paramètre dans le registre IF.
     * 
     * @param i
     *            l'interruption
     */
    public void requestInterrupt(Interrupt i) {
        IF = Bits.set(IF, i.index(), true);
    }

    /**
     * Exécute la prochaine instruction désignée par le compteur de programme ou ne fait rien si le cycle passé en
     * argument ne correspond pas à l'attribut nextNonIdleCycle.
     * 
     * @param cycle
     *            un entier de type long correspondant au cycle actuel
     */
    @Override
    public void cycle(long cycle) {
        if ((nextNonIdleCycle == Long.MAX_VALUE) && (IE & IF) > 0) {
            nextNonIdleCycle = cycle;
        }
        if (nextNonIdleCycle == cycle) {
            reallyCycle();
        }
    }

    /**
     * Examine si les interruptions sont activées (c-à-d si IME est vrai) et si une interruption est en attente, auquel
     * cas effectue le traitement correspondant ; sinon, exécute normalement la prochaine instruction
     * 
     * @param cycle
     *            un entier de type long correspondant au cycle acctuel
     */
    private void reallyCycle() {
        if (checkInterrupt()) {
            IME = false;
            int i = getIndexInterrupt();
            IF = Bits.set(IF, i, false);
            push16(PC);
            PC = AddressMap.INTERRUPTS[i];
            nextNonIdleCycle += INTERRUPTIONS_CYCLES;
        } else {
            Opcode opcode;
            if (bus.read(PC) == PREFIX_OPCODE) {
                opcode = PREFIXED_OPCODE_TABLE[read8AfterOpcode()];
            } else {
                opcode = DIRECT_OPCODE_TABLE[bus.read(PC)];
            }
            dispatch(opcode);
            nextNonIdleCycle += opcode.cycles;
        }
    }

    /**
     * Exécute les instructions contenues dans l'opcode passé en argument.
     * 
     * @param opcode
     *            l'opcode d'où extraire les instructions
     */
    private void dispatch(Opcode opcode) {
        nextPC = PC + opcode.totalBytes;
        switch (opcode.family) {
        case NOP: {
        }
            break;

        // Loads
        case LD_R8_HLR: {
            registerFile.set(extractReg(opcode, 3), read8AtHl());
        }

            break;
        case LD_A_HLRU: {
            registerFile.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }

            break;
        case LD_A_N8R: {
            registerFile.set(Reg.A, bus.read(AddressMap.REGS_START + read8AfterOpcode()));
        }
            break;
        case LD_A_CR: {
            registerFile.set(Reg.A, bus.read(AddressMap.REGS_START + getReg(Reg.C)));
        }
            break;
        case LD_A_N16R: {
            registerFile.set(Reg.A, read8(read16AfterOpcode()));
        }
            break;
        case LD_A_BCR: {
            registerFile.set(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;
        case LD_A_DER: {
            registerFile.set(Reg.A, read8(reg16(Reg16.DE)));
        }
            break;
        case LD_R8_N8: {
            registerFile.set(extractReg(opcode, 3), read8AfterOpcode());
        }
            break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(opcode), read16AfterOpcode());
        }
            break;
        case POP_R16: {
            setReg16(extractReg16(opcode), pop16());
        }
            break;
        case LD_HLR_R8: {
            write8AtHl(getReg(extractReg(opcode, 0)));
        }
            break;
        case LD_HLRU_A: {
            write8AtHl(getReg(Reg.A));
            setReg16(Reg16.HL, Bits.clip(16, reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
            break;
        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(), getReg(Reg.A));
        }
            break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + getReg(Reg.C), getReg(Reg.A));
        }
            break;
        case LD_N16R_A: {
            write8(read16AfterOpcode(), getReg(Reg.A));
        }
            break;
        case LD_BCR_A: {
            write8(reg16(Reg16.BC), getReg(Reg.A));
        }
            break;
        case LD_DER_A: {
            write8(reg16(Reg16.DE), getReg(Reg.A));
        }
            break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        }
            break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), SP);
        }
            break;
        case LD_R8_R8: {
            registerFile.set(extractReg(opcode, 3), getReg(extractReg(opcode, 0)));
        }
            break;
        case LD_SP_HL: {
            SP = reg16(Reg16.HL);
        }
            break;
        case PUSH_R16: {
            push16(reg16(extractReg16(opcode)));
        }
            break;

        // Add
        case ADD_A_R8: {
            addToRegisterAndSetFlags(Reg.A, getReg(extractReg(opcode, 0)), flagCForAddingInstructions(opcode));
        }
            break;
        case ADD_A_N8: {
            addToRegisterAndSetFlags(Reg.A, read8AfterOpcode(), flagCForAddingInstructions(opcode));
        }
            break;
        case ADD_A_HLR: {
            addToRegisterAndSetFlags(Reg.A, read8AtHl(), flagCForAddingInstructions(opcode));
        }
            break;
        case INC_R8: {
            int vf = Alu.add(getReg(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case INC_HLR: {
            int vf = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case INC_R16SP: {
            setReg16SP(extractReg16(opcode), Alu.unpackValue(Alu.add16H(reg16SP(extractReg16(opcode)), 1)));
        }
            break;
        case ADD_HL_R16SP: {
            int vf = Alu.add16H(reg16(Reg16.HL), reg16SP(extractReg16(opcode)));
            setReg16(Reg16.HL, Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.CPU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU);
        }
            break;
        case LD_HLSP_S8: {
            int vf = Alu.add16L(Bits.clip(16, readAfterOpcodeAsSigned()), SP);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            if (Bits.test(opcode.encoding, 4)) {
                setReg16(Reg16.HL, Alu.unpackValue(vf));
            } else {
                SP = Alu.unpackValue(vf);
            }
        }
            break;

        // Subtract
        case SUB_A_R8: {
            subToRegisterAndSetFlags(Reg.A, getReg(extractReg(opcode, 0)), flagCForAddingInstructions(opcode));
        }
            break;
        case SUB_A_N8: {
            subToRegisterAndSetFlags(Reg.A, read8AfterOpcode(), flagCForAddingInstructions(opcode));
        }
            break;
        case SUB_A_HLR: {
            subToRegisterAndSetFlags(Reg.A, read8AtHl(), flagCForAddingInstructions(opcode));
        }
            break;
        case DEC_R8: {
            int vf = Alu.sub(getReg(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case DEC_HLR: {
            int vf = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
        }
            break;
        case CP_A_R8: {
            setFlags(Alu.sub(getReg(Reg.A), getReg(extractReg(opcode, 0))));
        }
            break;
        case CP_A_N8: {
            setFlags(Alu.sub(getReg(Reg.A), read8AfterOpcode()));
        }
            break;
        case CP_A_HLR: {
            setFlags(Alu.sub(getReg(Reg.A), read8AtHl()));

        }
            break;
        case DEC_R16SP: {
            setReg16SP(extractReg16(opcode), Bits.clip(16, reg16SP(extractReg16(opcode)) - 1));
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
            setRegFlags(Reg.A, Alu.and(getReg(Reg.A), read8AfterOpcode()));
        }
            break;
        case AND_A_R8: {
            setRegFlags(Reg.A, Alu.and(getReg(Reg.A), getReg(extractReg(opcode, 0))));
        }
            break;
        case AND_A_HLR: {
            setRegFlags(Reg.A, Alu.and(getReg(Reg.A), read8AtHl()));
        }
            break;
        case OR_A_R8: {
            setRegFlags(Reg.A, Alu.or(getReg(Reg.A), getReg(extractReg(opcode, 0))));
        }
            break;
        case OR_A_N8: {
            setRegFlags(Reg.A, Alu.or(getReg(Reg.A), read8AfterOpcode()));
        }
            break;
        case OR_A_HLR: {
            setRegFlags(Reg.A, Alu.or(getReg(Reg.A), read8AtHl()));
        }
            break;
        case XOR_A_R8: {
            setRegFlags(Reg.A, Alu.xor(getReg(Reg.A), getReg(extractReg(opcode, 0))));
        }
            break;
        case XOR_A_N8: {
            setRegFlags(Reg.A, Alu.xor(getReg(Reg.A), read8AfterOpcode()));
        }
            break;
        case XOR_A_HLR: {
            setRegFlags(Reg.A, Alu.xor(getReg(Reg.A), read8AtHl()));
        }
            break;
        case CPL: {
            registerFile.set(Reg.A, Bits.complement8(getReg(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            int vf = Alu.rotate(getDirFromOpcode(opcode), getReg(Reg.A));
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            setRegFromAlu(Reg.A, vf);
        }
            break;
        case ROTA: {
            int vf = Alu.rotate(getDirFromOpcode(opcode), getReg(Reg.A), testFlag(Flag.C));
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            setRegFromAlu(Reg.A, vf);
        }
            break;
        case ROTC_R8: {
            setRegFlags(extractReg(opcode, 0), Alu.rotate(getDirFromOpcode(opcode), getReg(extractReg(opcode, 0))));
        }
            break;
        case ROT_R8: {
            setRegFlags(extractReg(opcode, 0),
                    Alu.rotate(getDirFromOpcode(opcode), getReg(extractReg(opcode, 0)), testFlag(Flag.C)));
        }
            break;
        case ROTC_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(getDirFromOpcode(opcode), read8AtHl()));
        }
            break;
        case ROT_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(getDirFromOpcode(opcode), read8AtHl(), testFlag(Flag.C)));
        }
            break;
        case SWAP_R8: {
            setRegFlags(extractReg(opcode, 0), Alu.swap(getReg(extractReg(opcode, 0))));
        }
            break;
        case SWAP_HLR: {
            write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
        }
            break;
        case SLA_R8: {
            setRegFlags(extractReg(opcode, 0), Alu.shiftLeft(getReg(extractReg(opcode, 0))));
        }
            break;
        case SRA_R8: {
            setRegFlags(extractReg(opcode, 0), Alu.shiftRightA(getReg(extractReg(opcode, 0))));
        }
            break;
        case SRL_R8: {
            setRegFlags(extractReg(opcode, 0), Alu.shiftRightL(getReg(extractReg(opcode, 0))));
        }
            break;
        case SLA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftLeft(read8AtHl()));
        }
            break;
        case SRA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightA(read8AtHl()));
        }
            break;
        case SRL_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightL(read8AtHl()));
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            combineAluFlags(Alu.testBit(getReg(extractReg(opcode, 0)), getIndexFromOpcode(opcode)), FlagSrc.ALU,
                    FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        }
            break;
        case BIT_U3_HLR: {
            combineAluFlags(Alu.testBit(read8AtHl(), getIndexFromOpcode(opcode)), FlagSrc.ALU, FlagSrc.V0, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;
        case CHG_U3_R8: {
            registerFile.set(extractReg(opcode, 0),
                    Bits.set(getReg(extractReg(opcode, 0)), getIndexFromOpcode(opcode), getValueFromOpcode(opcode)));
        }
            break;
        case CHG_U3_HLR: {
            write8AtHl(Bits.set(read8AtHl(), getIndexFromOpcode(opcode), getValueFromOpcode(opcode)));
        }
            break;

        // Misc. ALU
        case DAA: {
            int value = Alu.bcdAdjust(getReg(Reg.A), testFlag(Flag.N), testFlag(Flag.H), testFlag(Flag.C));
            setRegFlags(Reg.A, value);
            combineAluFlags(value, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
        }
            break;
        case SCCF: {
            registerFile.setBit(Reg.F, Flag.C, flagCForAddingInstructions(opcode));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.CPU);
        }
            break;

        // Jumps
        case JP_HL: {
            jump(reg16(Reg16.HL));
        }
            break;
        case JP_N16: {
            jump(read16AfterOpcode());
        }
            break;
        case JP_CC_N16: {
            if (checkCondition(opcode)) {
                jump(read16AfterOpcode());
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;
        case JR_E8: {
            nextPC = add16E8(nextPC);
        }
            break;
        case JR_CC_E8: {
            if (checkCondition(opcode)) {
                nextPC = add16E8(nextPC);
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        // Calls and returns
        case CALL_N16: {
            push16(nextPC);
            nextPC = read16AfterOpcode();
        }
            break;
        case CALL_CC_N16: {
            if (checkCondition(opcode)) {
                push16(nextPC);
                nextPC = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;
        case RST_U3: {
            push16(nextPC);
            nextPC = AddressMap.RESETS[getIndexFromOpcode(opcode)];
        }
            break;
        case RET: {
            nextPC = pop16();
        }
            break;
        case RET_CC: {
            if (checkCondition(opcode)) {
                nextPC = pop16();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, 3);
        }
            break;
        case RETI: {
            IME = true;
            nextPC = pop16();
        }
            break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        }
            break;
        case STOP:
            throw new Error("STOP is not implemented");

        default:
            throw new NullPointerException("Opcode's family not considered in switch");
        }
        PC = Bits.clip(16, nextPC);
    }

    /**
     * Construit un tableau indexé d'opcode contenant les 256 opcodes du type indiqué.
     * 
     * @param kind
     *            la famille d'Opcode que l'on souhaite initialiser
     * @return le tableau d'opcode créé
     */
    private static Opcode[] buildOpcodeTable(Kind kind) {
        Opcode[] table = new Opcode[OPCODE_TABLE_SIZE];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) {
                table[o.encoding] = o;
            }
        }
        return table;
    }

    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);

    private int getReg(Reg register) {
        return registerFile.get(register);
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse donnée.
     * 
     * @param address
     *            l'entier où lire la valeur contenu dans la mémoire
     * @return la valeur contenue à l'adresse donnée
     */
    private int read8(int address) {
        return bus.read(address);
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse contenue dans la paire de registres HL.
     * 
     * @return la valeur contenue à l'adresse contenue dans le registre HL
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse suivant celle contenue dans le compteur de programme, c-à-d à
     * l'adresse PC+1.
     * 
     * @return l'octet directement après l'opcode (paramètre de l'instruction représenté par l'opcode)
     */
    private int read8AfterOpcode() {
        return read8(PC + 1);
    }

    /**
     * Lit depuis le bus la valeur 16 bits à l'adresse donnée.
     * 
     * @param address
     *            la valeur 16 bits contenue à l'adresse donnée
     * @return
     */
    private int read16(int address) {
        return Bits.make16(read8(address + 1), read8(address));
    }

    /**
     * Lit depuis le bus la valeur 16 bits à l'adresse suivant celle contenue dans le compteur de programme, c-à-d à
     * l'adresse PC+1
     * 
     * @return la valeur 16 bits directement après l'opcode (paramètre de l'instruction représenté par l'opcode)
     */
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }

    /**
     * Ecrit sur le bus, à l'adresse donnée, la valeur 8 bits donnée
     * 
     * @param address
     *            l'entier où stocker la valeur
     * @param v
     *            la valeur à stocker
     */
    private void write8(int address, int v) {
        bus.write(address, v);
    }

    /**
     * Ecrit sur le bus, à l'adresse donnée, la valeur 16 bits donnée
     * 
     * @param address
     *            l'entier où stocker la valeur
     * @param la
     *            valeur à stocker
     */
    private void write16(int address, int v) {
        write8(address + 1, Bits.extract(v, 8, 8));
        write8(address, Bits.clip(8, v));
    }

    /**
     * Ecrit sur le bus, à l'adresse contenue dans la paire de registres HL, la valeur 8 bits donnée
     * 
     * @param v
     *            la valeur 8 bits à stocker à l'adresse contenue dans HL
     */
    private void write8AtHl(int v) {
        write8(reg16(Reg16.HL), v);
    }

    /**
     * Décrémente l'adresse contenue dans le pointeur de pile (registre SP) de 2 unités, puis écrit à cette nouvelle
     * adresse la valeur 16 bits donnée
     * 
     * @param La
     *            valeur à stocker à l'adresse contenue dans SP
     */
    private void push16(int v) {
        SP = Bits.clip(16, SP - 2);
        write16(SP, v);
    }

    /**
     * Lit depuis le bus (et retourne) la valeur 16 bits à l'adresse contenue dans le pointeur de pile (registre SP),
     * puis l'incrémente de 2 unités
     * 
     * @return la valeur stockée à l'adresse contenue dans SP (avant incrémentation)
     */
    private int pop16() {
        int temp = SP;
        SP = Bits.clip(16, SP + 2);
        return read16(temp);
    }

    /**
     * Retourne la valeur contenue dans la paire de registres donnée.
     * 
     * @param r
     *            la paire de registre dont on veut la valeur
     * @return la valeur contenue dans la paire de registre
     */
    private int reg16(Reg16 r) {
        int index16 = r.index() * 2;
        return Bits.make16(getReg(Reg.values()[index16]), getReg(Reg.values()[index16 + 1]));
        /*
         * Crée une valeur 16bits à partir du premier registre (dont l'index est égal à l'indice de r multiplié par 2)
         * et du second (dont l'index est égal à celui du premier incrémenté de 1)
         */
    }

    /**
     * Retourne la valeur contenue dans la paire de registres donnée et modifie SP si le registre AF est passé en
     * argument.
     * 
     * @param r
     *            la paire de registre dont on veut la valeur
     * @return la valeur contenue dans la paire de registre
     */
    private int reg16SP(Reg16 r) {
        if (r == Reg16.AF) {
            return SP;
        } else {
            return reg16(r);
        }
    }

    /**
     * Modifie la valeur contenue dans la paire de registres donnée, en faisant attention de mettre à 0 les bits de
     * poids faible si la paire en question est AF.
     * 
     * @param r
     *            la paire de registre dont on modifie la valeur
     * @param newV
     *            la valeur à stocker dans la paire de registre
     */
    private void setReg16(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        int index16 = r.index() * 2;
        registerFile.set(Reg.values()[index16], Bits.extract(newV, 8, 8));
        if (r == Reg16.AF) {
            registerFile.set(Reg.F, Bits.clip(8, newV) & (-1 << 4));
        } else {
            registerFile.set(Reg.values()[index16 + 1], Bits.clip(8, newV));
        }
    }

    /**
     * Modifie la valeur contenue dans la paire de registres donnée, sauf dans le cas où la paire passée est AF, auquel
     * cas le registre SP est modifié en lieu et place de la paire AF.
     * 
     * @param r
     *            la paire de registre à modifier
     * @param newV
     *            la valeur à stocker dans la paire de registre
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r == Reg16.AF) {
            SP = newV;
        } else {
            setReg16(r, newV);
        }
    }

    /**
     * Extrait et retourne l'identité d'un registre 8 bits de l'encodage de l'opcode donné, à partir du bit d'index
     * donné.
     * 
     * @param opcode
     *            l'opcode d'où extraire le registre
     * @param startBit
     *            l'indice (commençant à 0) à partir duquel les 3 bits correspondant au registre commencent
     * @return le registre extrait de l'opcode
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        switch (Bits.extract(opcode.encoding, startBit, 3)) {
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b110:
            return null;
        case 0b111:
            return Reg.A;
        default:
            return null;
        }
    }

    /**
     * Extrait et retourne l'identité d'une paire de registre de l'encodage de l'opcode donné.
     * 
     * @param opcode
     *            l'opcode d'où extraire la paire de registre
     * @return la paire de registre extraite
     */
    private Reg16 extractReg16(Opcode opcode) {
        switch (Bits.extract(opcode.encoding, 4, 2)) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default:
            return null;
        }
    }

    /**
     * Retourne -1 ou +1 en fonction du bit d'index 4, qui est utilisé pour encoder l'incrémentation ou la
     * décrémentation de la paire HL dans différentes instructions.
     * 
     * @param opcode
     *            l'opcode où on regarde le bit d'indice 4
     * @return -1 si le bit 4 vaut 1 et 1 si le bit 4 vaut 0;
     */
    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }

    /**
     * Extrait la valeur stockée dans la paire donnée et la place dans le registre donné.
     * 
     * @param r
     *            Registre où placer la valeur
     * @param vf
     *            Paire où extraire la valeur
     */
    private void setRegFromAlu(Reg r, int vf) {
        registerFile.set(r, Alu.unpackValue(vf));
    }

    /**
     * Extrait les fanions stockés dans la paire donnée et les place dans le registre F.
     * 
     * @param valueFlags
     *            paire où extraire les fanions
     */
    private void setFlags(int valueFlags) {
        registerFile.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    /**
     * Extrait la valeur stockée dans la paire donnée et la place dans le registre donné ; et extrait les fanions
     * stockés dans la paire donnée et les place dans le registre F.
     * 
     * @param r
     *            Registre où placer la valeur
     * @param vf
     *            Paire où extraire la valeur et les fanions
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    /**
     * Extrait la valeur stockée dans la paire donnée et l'écrit sur le bus à l'adresse contenue dans la paire de
     * registres HL, puis extrait les fanions stockés dans la paire et les place dans le registre F.
     * 
     * @param vf
     *            Paire où extraire la valeur et les fanions
     */
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }

    /**
     * Combine les fanions stockés dans le registre F avec ceux contenus dans la paire vf, en fonction des quatre
     * derniers paramètres, qui correspondent chacun à un fanion, et stocke le résultat dans le registre F.
     * 
     * @param vf
     *            l'entier le paquet valeur/fanions
     * @param z
     *            le fanion Z
     * @param n
     *            le fanion N
     * @param h
     *            le fanion H
     * @param c
     *            le fanion C
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int v1 = vector(FlagSrc.V1, z, n, h, c);
        int alu = vector(FlagSrc.ALU, z, n, h, c);
        int cpu = vector(FlagSrc.CPU, z, n, h, c);
        registerFile.set(Reg.F, v1 + (alu & Alu.unpackFlags(vf)) + (cpu & getReg(Reg.F)));
    }

    /**
     * Méthode utilitaire qui permet de creer des vecteurs de bits en fonction des arguments, chacun des 4 bits valant 1
     * s'il est du même type que flagSrc.
     * 
     * @param flagSrc
     *            le type avec lequel comparé les différents arguments
     * @param z
     *            le bit d'indice 7
     * @param n
     *            le bit d'indice 6
     * @param h
     *            le bit d'indice 5
     * @param c
     *            le bit d'indice 4
     * @return le vecteur de bits créé
     */
    private int vector(FlagSrc flagSrc, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        return Alu.maskZNHC(flagSrc == z, flagSrc == n, flagSrc == h, flagSrc == c);
    }

    /**
     * Extrait la direction de la rotation à partir de l'Opcode d'une instruction.
     * 
     * @param opcode
     *            l'opcode où extraire la rotation
     * @return la direction de rotation
     */
    private RotDir getDirFromOpcode(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }

    /**
     * Donne l'index du bit à tester ou modifier (bits 3 à 5), pour les instructions BIT, RES et SET
     * 
     * @param opcode
     *            l'opcode d'où extraire l'index
     * @return l'index du bit à tester ou modifier
     */
    private int getIndexFromOpcode(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3);
    }

    /**
     * Détermine la valeur à attribuer au bit à modifier, pour les instructions RES et SET
     * 
     * @param opcode
     *            l'opcode d'où extraire la valeur
     * @return true si le bit vaut 1
     */
    private boolean getValueFromOpcode(Opcode opcode) {
        return Bits.test(opcode.encoding, 6);
    }

    /**
     * Combine la valeur du fanion C et de la valeur de l'Opcode pour obtenir la valeur à retourner dans les
     * instructions du CPU, selon l'instruction utilisée le calcul change
     * 
     * @param opcode
     *            l'opcode
     * @return true si le nouveau fanion vaut 1
     */
    private boolean flagCForAddingInstructions(Opcode opcode) {
        if (Bits.test(opcode.encoding, 7)) {
            return Bits.test(opcode.encoding, 3) && testFlag(Flag.C);
        } else {
            return !(Bits.test(opcode.encoding, 3) && testFlag(Flag.C));
        }
    }

    /**
     * Ajoute une valeur donnée à un registre donné et stocke le résultat dans ce registre, modifie ensuite les fanions
     * du registre F de la manière suivante : Z0HC
     * 
     * @param r
     *            le registre auquel ajouter la valeur et à modifier
     * @param v
     *            la valeur à ajouter
     * @param initialCarry
     *            la retenue initiale
     */
    /**
     * @param r
     * @param v
     * @param initialCarry
     */
    private void addToRegisterAndSetFlags(Reg r, int v, boolean initialCarry) {
        setRegFlags(r, Alu.add(getReg(r), v, initialCarry));
    }

    /**
     * Soustrait une valeur donnée à un registre donné et stocke le résultat dans ce registre, modifie ensuite les
     * fanions du registre F de la manière suivante : Z1HC
     * 
     * @param r
     *            le registre auquel soustraire la valeur et à modifier
     * @param v
     *            la valeur à soustraire
     * @param initialCarry
     *            l'emprunt initial
     */
    private void subToRegisterAndSetFlags(Reg r, int v, boolean initialCarry) {
        setRegFlags(r, Alu.sub(getReg(r), v, initialCarry));
    }

    /**
     * Met la valeur "newValue" dans le registre PC, ce qui a pour effet de faire "sauter" le processeur directement à
     * l'instruction se trouvent à l'adresse correspondante.
     * 
     * @param newValue
     *            la valeur à attribuer au registre PC
     */
    private void jump(int newValue) {
        nextPC = newValue;
    }

    /**
     * Extrait la condition de l'opcode passé en paramètre, la teste, et retourne le résultat.
     * 
     * @param opcode
     *            l'opcode dans lequel se trouve l'instruction
     * @return le résultat, un booléen valant vrai si l'instruction est vraie
     */
    private boolean checkCondition(Opcode opcode) {
        int condition = Bits.extract(opcode.encoding, 3, 2);
        switch (condition) {
        case 0b00:
            return !testFlag(Flag.Z);
        case 0b01:
            return testFlag(Flag.Z);
        case 0b10:
            return !testFlag(Flag.C);
        case 0b11:
            return testFlag(Flag.C);
        default:
            throw new IllegalArgumentException("Condition inconnue : opcode erroné");
        }
    }

    /**
     * Teste le fanion (stocké dans le registre F) passé en paramètre et retourne vrai si il vaut 1.
     * 
     * @param f
     *            le fanion à tester
     * @return vrai si le fanion testé est vrai
     */
    private boolean testFlag(Flag f) {
        return registerFile.testBit(Reg.F, f);
    }

    private int getIndexInterrupt() {
        return 31 - Integer.numberOfLeadingZeros(Integer.lowestOneBit(IE & IF));
    }

    private boolean checkInterrupt() {
        return IME && (IE & IF) > 0;
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse suivant celle contenue dans le compteur de programme, c-à-d à
     * l'adresse PC+1. Cette valeur est interprétée comme un entier signé afin de pouvoir additioner des nombres
     * négatifs
     * 
     * @return la valeur interprété comme un nombre signé
     */
    private int readAfterOpcodeAsSigned() {
        return Bits.signExtend8(read8AfterOpcode());
    }

    /**
     * Lit depuis le bus la valeur 8 bits à l'adresse suivant celle contenue dans le compteur de programme, c-à-d à
     * l'adresse PC+1. L'inteprête comme une valeur signée. Puis ajoute cette valeur à celle passée en paramètre et
     * retourne le résultat.
     * 
     * @param value
     *            la valeur à additionner
     * @return le résultat
     */
    private int add16E8(int value) {
        return Bits.clip(16, value + readAfterOpcodeAsSigned());
    }

    /**
     * Retourne un tableau contenant, dans l'ordre, la valeur des registres PC, SP, A, F, B, C, D, E, H et L.
     * 
     * @return le tableau
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] test = new int[10];
        test[0] = PC;
        test[1] = SP;
        for (int i = 2; i < test.length; i++) {
            test[i] = getReg(Reg.values()[i - 2]);
        }
        return test;
    }
}
