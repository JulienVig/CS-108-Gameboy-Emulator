/**
* Contient les tests de la classe Cpu
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest {

    Bus bus;
    Cpu cpu;

    @BeforeEach
    private void initialize() {
        cpu = new Cpu();
        bus = new Bus();
        cpu.attachTo(bus);
        bus.attach(new RamController(new Ram(0xFFFF), 0));
    }

    @Test
    void LD_A_HLRUworksWell() {
        bus.write(2543, 42);
        cpu.setAllRegs16(0, 0, 0, 2543);
        executeInstruction(Opcode.LD_A_HLRI);
        assertArrayEquals(new int[] { 1, 0, 42, 0, 0, 0, 0, 0, 9, 240 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_HLRUoverflows() {
        cpu.setAllRegs16(0, 0, 0, 0xFFFF);
        executeInstruction(Opcode.LD_A_HLRI);
        assertArrayEquals(new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_HLRDworksWell() {
        bus.write(3256, 24);
        cpu.setAllRegs16(0, 0, 0, 3256);
        executeInstruction(Opcode.LD_A_HLRD);
        assertArrayEquals(new int[] { 1, 0, 24, 0, 0, 0, 0, 0, 12, 183 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_HLRDunderflows() {
        cpu.setAllRegs16(0, 0, 0, 0);
        executeInstruction(Opcode.LD_A_HLRD);
        assertArrayEquals(new int[] { 1, 0, Opcode.LD_A_HLRD.encoding, 0, 0, 0,
                0, 0, 0xFF, 0xFF }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_N8RworksWell() {
        bus.write(1, 42);
        bus.write(AddressMap.REGS_START + 42, 155);
        executeInstruction(Opcode.LD_A_N8R);
        assertArrayEquals(new int[] { Opcode.LD_A_N8R.totalBytes, 0, 155, 0, 0,
                0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void LD_A_N8RworksOnLimitValues() {
        bus.write(1, 0);
        bus.write(AddressMap.REGS_START, 23);
        bus.write(3, 0xFF);
        bus.write(AddressMap.REGS_START + 0xFF, 230);
        executeInstruction(Opcode.LD_A_N8R);

        assertArrayEquals(new int[] { Opcode.LD_A_N8R.totalBytes, 0, 23, 0, 0,
                0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());

    }

    // Erreur REGS_START+ 0xFF
    @Test
    void LD_A_N8RworksOnLimitValues2() {
        bus.write(1, 0xFE);
        bus.write(AddressMap.REGS_START + 0xFE, 230);
        executeInstruction(Opcode.LD_A_N8R);
        assertArrayEquals(new int[] { Opcode.LD_A_N8R.totalBytes, 0, 230, 0, 0,
                0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_CRworksWell() {
        bus.write(AddressMap.REGS_START + 4, 42);
        cpu.setAllRegs(0, 0, 0, 4, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_A_CR);
        assertArrayEquals(new int[] { Opcode.LD_A_CR.totalBytes, 0, 42, 0, 0, 4,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_CRworksOnLimitValues() {
        bus.write(AddressMap.REGS_START, 42);
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_A_CR);
        assertArrayEquals(new int[] { Opcode.LD_A_CR.totalBytes, 0, 42, 0, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_CRworksOnLimitValues2() {
        bus.write(AddressMap.REGS_START + 0xFE, 42);
        cpu.setAllRegs(0, 0, 0, 0xFE, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_A_CR);
        assertArrayEquals(new int[] { Opcode.LD_A_CR.totalBytes, 0, 42, 0, 0,
                0xFE, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_N16RworksOnLimitValues() {
        bus.write(1, 0xFE);
        bus.write(2, 0xFF);
        bus.write(0xFFFE, 42);
        executeInstruction(Opcode.LD_A_N16R);
        assertArrayEquals(new int[] { Opcode.LD_A_N16R.totalBytes, 0, 42, 0, 0,
                0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_N16RworksOnLimitValues2() {
        executeInstruction(Opcode.LD_A_N16R);
        assertArrayEquals(
                new int[] { Opcode.LD_A_N16R.totalBytes, 0,
                        Opcode.LD_A_N16R.encoding, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_BCRworksWell() {
        bus.write(2356, 123);
        cpu.setAllRegs16(0, 2356, 0, 0);
        executeInstruction(Opcode.LD_A_BCR);
        assertArrayEquals(new int[] { Opcode.LD_A_BCR.totalBytes, 0, 123, 0, 9,
                52, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_A_DERworksWell() {
        bus.write(2356, 42);
        cpu.setAllRegs16(0, 0, 2356, 0);
        executeInstruction(Opcode.LD_A_DER);
        assertArrayEquals(new int[] { Opcode.LD_A_DER.totalBytes, 0, 42, 0, 0,
                0, 9, 52, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void setReg16worksOnAF() {
        cpu.setAllRegs16(4369, 0, 0, 0);
        assertArrayEquals(new int[] { 0, 0, 17, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_N8worksWell() {
        bus.write(1, 42);

        executeInstruction(Opcode.LD_A_N8);
        assertArrayEquals(new int[] { Opcode.LD_A_N8.totalBytes, 0, 42, 0, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void LD_R8_N8worksWell2() {
        bus.write(1, 84);
        executeInstruction(Opcode.LD_B_N8);
        assertArrayEquals(new int[] { Opcode.LD_B_N8.totalBytes, 0, 0, 0, 84, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R16SP_N16worksWellOnSP() {
        bus.write(1, 0xFE);
        bus.write(2, 0xFF);
        executeInstruction(Opcode.LD_SP_N16);
        assertArrayEquals(new int[] { Opcode.LD_SP_N16.totalBytes, 0xFFFE, 0, 0,
                0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R16SP_N16worksWellOnBC() {
        bus.write(1, 1);
        bus.write(2, 2);
        executeInstruction(Opcode.LD_BC_N16);
        assertArrayEquals(new int[] { Opcode.LD_BC_N16.totalBytes, 0, 0, 0, 2,
                1, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R16SP_N16worksWellOnDE() {
        bus.write(1, 0xFF);
        bus.write(2, 0xFE);
        executeInstruction(Opcode.LD_DE_N16);
        assertArrayEquals(new int[] { Opcode.LD_DE_N16.totalBytes, 0, 0, 0, 0,
                0, 0xFE, 0xFF, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R16SP_N16worksWellOnHL() {
        bus.write(1, 235);
        bus.write(2, 142);
        executeInstruction(Opcode.LD_HL_N16);
        assertArrayEquals(new int[] { Opcode.LD_HL_N16.totalBytes, 0, 0, 0, 0,
                0, 0, 0, 142, 235 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_R8() {
        bus.write(1024, 1);
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 87, 0);
        executeInstruction(Opcode.LD_E_H);
        assertArrayEquals(new int[] { 1, 0, 0, 0, 0, 0, 0, 87, 87, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_R8_HLR() {
        bus.write(8190, 1);
        cpu.setAllRegs16(0, 0, 0, 8190);
        executeInstruction(Opcode.LD_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 1, 0, 0, 0, 0, 0, 31, 254 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void PUSH_R16() {
        cpu.setSP(12);
        cpu.setAllRegs16(0, 54, 0, 0);
        executeInstruction(Opcode.PUSH_BC);
        assertEquals(54, Bits.make16(bus.read(10 + 1), bus.read(10)));
    }

    @Test
    void LD_SP_HL() {
        cpu.setAllRegs16(0, 0, 0, 769);
        executeInstruction(Opcode.LD_SP_HL);
        assertArrayEquals(new int[] { 1, 769, 0, 0, 0, 0, 0, 0, 3, 1 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_N16R_SP() {
        cpu.setSP(297);
        bus.write(1, 3);
        bus.write(2, 10);
        executeInstruction(Opcode.LD_N16R_SP);
        assertEquals(297, Bits.make16(bus.read(2563 + 1), bus.read(2563)));
    }

    @Test
    void LD_HLR_N8() {
        cpu.setAllRegs16(0, 0, 0, 1024);
        bus.write(1, 65);
        executeInstruction(Opcode.LD_HLR_N8);
        assertEquals(65, bus.read(1024));
    }

    @Test
    void LD_DER_A() {
        cpu.setAllRegs(243, 0, 0, 0, 19, 61, 0, 0);
        executeInstruction(Opcode.LD_DER_A);
        assertEquals(243, bus.read(4925));
    }

    @Test
    void LD_BCR_A() {
        cpu.setAllRegs(1, 0, 14, 68, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_BCR_A);
        assertEquals(1, bus.read(3652));
    }

    @Test
    void LD_N16R_A() {
        cpu.setAllRegs(254, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 68);
        bus.write(2, 14);
        executeInstruction(Opcode.LD_N16R_A);
        assertEquals(254, bus.read(3652));
    }

    @Test
    void LD_CR_A() {
        cpu.setAllRegs(123, 0, 0, 0xAB, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_CR_A);
        assertEquals(123, bus.read(0xFFAB));
    }

    @Test
    void LD_N8R_A() {
        bus.write(1, 0xAB);
        cpu.setAllRegs(123, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.LD_N8R_A);
        assertEquals(123, bus.read(0xFFAB));
    }

    @Test
    void LD_HLRU_Aincrements() {
        cpu.setAllRegs(123, 0, 0, 0, 0, 0, 0, 12);
        executeInstruction(Opcode.LD_HLRD_A);
        assertEquals(123, bus.read(12));
        assertArrayEquals(new int[] { 1, 0, 123, 0, 0, 0, 0, 0, 0, 11 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLRU_Adecrements() {
        cpu.setAllRegs(123, 0, 0, 0, 0, 0, 0, 12);
        executeInstruction(Opcode.LD_HLRI_A);
        assertEquals(123, bus.read(12));
        assertArrayEquals(new int[] { 1, 0, 123, 0, 0, 0, 0, 0, 0, 13 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLR_R8() {
        cpu.setAllRegs(0, 0, 5, 0, 0, 0, 0, 13);
        executeInstruction(Opcode.LD_HLR_B);
        assertEquals(5, bus.read(13));
    }

    @Test
    void POP_R16() {
        cpu.setSP(43562);
        cpu.setAllRegs16(0, 0, 0, 0);
        bus.write(43562, 12);
        executeInstruction(Opcode.POP_DE);
        assertArrayEquals(new int[] { 1, 43564, 0, 0, 0, 0, 0, 12, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }
    
    @Test
    void multiplesInstructionsWork() {
        cpu.setAllRegs(54, 0, 0, 0, 0, 0, 0, 255);
        bus.write(0, Opcode.LD_BC_N16.encoding);
        bus.write(1, 68);
        bus.write(2, 14);
        bus.write(3, Opcode.LD_BCR_A.encoding);
        bus.write(4, Opcode.LD_HLRI_A.encoding);
        cycleCpu(cpu, 10);
        assertArrayEquals(new int[] {8, 0, 54, 0, 14, 68, 0, 0, 1, 0 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(54, bus.read(255));
        assertEquals(54, bus.read(3652));
        
    }
    
    

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    private void executeInstruction(Opcode code) {
        bus.write(0, code.encoding);
        cycleCpu(cpu, code.cycles);
    }

    @Test
    void nopDoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                c._testGetPcSpAFBCDEHL());
    }

}
