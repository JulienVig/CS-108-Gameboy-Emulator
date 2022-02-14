/**
* Description de la classe
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest2 {

    Bus bus;
    Cpu cpu;

    @BeforeEach
    private void initialize() {
        cpu = new Cpu();
        bus = new Bus();
        cpu.attachTo(bus);
        bus.attach(new RamController(new Ram(0xFFFF), 0));
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c)
            cpu.cycle(c);
    }

    private void executeInstruction(Opcode code) {
        if (code.kind == Opcode.Kind.PREFIXED) {
            bus.write(0, 0xCB);
            bus.write(1, code.encoding);
        } else {
            bus.write(0, code.encoding);
        }
        cycleCpu(cpu, code.cycles);
    }

    @Test
    void ADD_A_R8() {
        cpu.setAllRegs(54, 0, 0, 28, 0, 0, 0, 0);
        executeInstruction(Opcode.ADD_A_C);
        assertArrayEquals(new int[] { 1, 0, 82, 32, 0, 28, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_N8() {
        cpu.setAllRegs(8, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 88);
        executeInstruction(Opcode.ADD_A_N8);
        assertArrayEquals(new int[] { 2, 0, 96, 32, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_A_HLR() {
        cpu.setAllRegs(8, 0, 0, 0, 0, 0, 2, 25);
        bus.write(537, 88);
        executeInstruction(Opcode.ADD_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 96, 32, 0, 0, 0, 0, 2, 25 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R8() {
        cpu.setAllRegs(0, 16, 0, 0, 254, 0, 2, 25);
        executeInstruction(Opcode.INC_D);
        assertArrayEquals(new int[] { 1, 0, 0, 16, 0, 0, 255, 0, 2, 25 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_HLR() {
        cpu.setAllRegs(0, 16, 0, 0, 0, 0, 2, 25);
        bus.write(537, 88);
        executeInstruction(Opcode.INC_HLR);
        assertArrayEquals(new int[] { 1, 0, 0, 16, 0, 0, 0, 0, 2, 25 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(89, bus.read(537));
    }

    @Test
    void INC_R16SP() {
        cpu.setAllRegs16(112, 0, 937, 0);
        executeInstruction(Opcode.INC_DE);
        assertArrayEquals(new int[] { 1, 0, 0, 112, 0, 0, 3, 170, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void INC_R16SP2() {
        cpu.setAllRegs16(192, 0, 937, 937);
        executeInstruction(Opcode.INC_HL);
        assertArrayEquals(new int[] { 1, 0, 0, 192, 0, 0, 3, 169, 3, 170 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void ADD_HL_R16SP() {
        cpu.setAllRegs16(128, 0, 0, 937);
        cpu.setSP(13);
        executeInstruction(Opcode.ADD_HL_SP);
        assertArrayEquals(new int[] { 1, 13, 0, 128, 0, 0, 0, 0, 3, 182 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void LD_HLSP_S8() {
        cpu.setSP(937);
        bus.write(1, 0b1100_1000); // -56
        executeInstruction(Opcode.LD_HL_SP_N8);
        assertArrayEquals(new int[] { Opcode.LD_HL_SP_N8.totalBytes, 937, 0, 48,
                0, 0, 0, 0, 3, 113 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_R8() {
        cpu.setAllRegs(35, 0, 0, 0, 0, 134, 0, 0);
        executeInstruction(Opcode.SUB_A_E);
        assertArrayEquals(new int[] { 1, 0, 157, 112, 0, 0, 0, 134, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_N8() {
        cpu.setAllRegs(35, 0, 0, 0, 0, 134, 0, 0);
        bus.write(1, 12);
        executeInstruction(Opcode.SUB_A_N8);
        assertArrayEquals(
                new int[] { 2, 0, 23, 0b0110_0000, 0, 0, 0, 134, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SUB_A_HLR() {
        cpu.setAllRegs(245, 0, 0, 0, 0, 134, 0, 23);
        bus.write(23, 12);
        executeInstruction(Opcode.SUB_A_HLR);
        assertArrayEquals(
                new int[] { 1, 0, 233, 0b0110_0000, 0, 0, 0, 134, 0, 23 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R8() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 57, 0);
        executeInstruction(Opcode.DEC_H);
        assertArrayEquals(new int[] { 1, 0, 0, 0b0100_0000, 0, 0, 0, 0, 56, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_HLR() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 134, 0, 23);
        bus.write(23, 12);
        executeInstruction(Opcode.DEC_HLR);
        assertArrayEquals(
                new int[] { 1, 0, 0, 0b0100_0000, 0, 0, 0, 134, 0, 23 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(11, bus.read(23));
    }

    @Test
    void CP_A_R8() {
        cpu.setAllRegs(35, 0, 0, 0, 0, 134, 0, 0);
        executeInstruction(Opcode.CP_A_E);
        assertArrayEquals(
                new int[] { 1, 0, 35, 0b0111_0000, 0, 0, 0, 134, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_R82() {
        cpu.setAllRegs(35, 0, 0, 0, 0, 35, 0, 0);
        executeInstruction(Opcode.CP_A_E);
        assertArrayEquals(
                new int[] { 1, 0, 35, 0b1100_0000, 0, 0, 0, 35, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_R83() {
        cpu.setAllRegs(35, 0, 0, 0, 0, 35, 0, 0);
        executeInstruction(Opcode.CP_A_A);
        assertArrayEquals(
                new int[] { 1, 0, 35, 0b1100_0000, 0, 0, 0, 35, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_N8() {
        cpu.setAllRegs(12, 0, 0, 0, 0, 134, 0, 0);
        bus.write(1, 12);
        executeInstruction(Opcode.CP_A_N8);
        assertArrayEquals(
                new int[] { 2, 0, 12, 0b1100_0000, 0, 0, 0, 134, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_N82() {
        cpu.setAllRegs(12, 0, 0, 0, 0, 134, 0, 0);
        bus.write(1, 144);
        executeInstruction(Opcode.CP_A_N8);
        assertArrayEquals(
                new int[] { 2, 0, 12, 0b0101_0000, 0, 0, 0, 134, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_HLR() {
        cpu.setAllRegs(12, 0, 0, 0, 0, 134, 0, 23);
        bus.write(23, 12);
        executeInstruction(Opcode.CP_A_HLR);
        assertArrayEquals(
                new int[] { 1, 0, 12, 0b1100_0000, 0, 0, 0, 134, 0, 23 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CP_A_HLR2() {
        cpu.setAllRegs(12, 0, 0, 0, 0, 134, 0, 23);
        bus.write(23, 13);
        executeInstruction(Opcode.CP_A_HLR);
        assertArrayEquals(
                new int[] { 1, 0, 12, 0b0111_0000, 0, 0, 0, 134, 0, 23 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R16SP() {
        cpu.setAllRegs16(192, 654, 0, 0);
        executeInstruction(Opcode.DEC_BC);
        assertArrayEquals(new int[] { 1, 0, 0, 192, 2, 141, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DEC_R16SP2() {
        cpu.setAllRegs16(160, 654, 0, 0);
        cpu.setSP(355);
        executeInstruction(Opcode.DEC_SP);
        assertArrayEquals(new int[] { 1, 354, 0, 160, 2, 142, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_N8() {
        cpu.setAllRegs(0b1111_1111, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 144);
        executeInstruction(Opcode.AND_A_N8);
        assertArrayEquals(
                new int[] { 2, 0, 144, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_N82() {
        cpu.setAllRegs(244, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 12);
        executeInstruction(Opcode.AND_A_N8);
        assertArrayEquals(
                new int[] { 2, 0, 244 & 12, 0b0010_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_N83() {
        cpu.setAllRegs(0b0101_0101, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 0b1010_1010);
        executeInstruction(Opcode.AND_A_N8);
        assertArrayEquals(new int[] { 2, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_R8() {
        cpu.setAllRegs(221, 0, 12, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.AND_A_B);
        assertArrayEquals(
                new int[] { 1, 0, 221 & 12, 0b0010_0000, 12, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_R82() {
        cpu.setAllRegs(0b0101_0101, 0, 0b1010_1010, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.AND_A_B);
        assertArrayEquals(
                new int[] { 1, 0, 0, 0b1010_0000, 0b1010_1010, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_HLR() {
        cpu.setAllRegs(0b0101_0101, 0, 0, 0, 0, 0, 0, 53);
        bus.write(53, 132);
        executeInstruction(Opcode.AND_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 0b0101_0101 & 132, 0b0010_0000, 0,
                0, 0, 0, 0, 53 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void AND_A_HLR2() {
        cpu.setAllRegs(0b0101_0101, 0, 0, 0, 0, 0, 0, 3);
        bus.write(53, 4);
        executeInstruction(Opcode.AND_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 3 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_N8() {
        cpu.setAllRegs(0b1111_1111, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 144);
        executeInstruction(Opcode.OR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 255, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_N82() {
        cpu.setAllRegs(244, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 12);
        executeInstruction(Opcode.OR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 244 | 12, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_N83() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 0);
        executeInstruction(Opcode.OR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 0, 0b1000_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_R8() {
        cpu.setAllRegs(221, 0, 12, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.OR_A_B);
        assertArrayEquals(
                new int[] { 1, 0, 221 | 12, 0b0000_0000, 12, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_R82() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.OR_A_B);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1000_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_HLR() {
        cpu.setAllRegs(23, 0, 0, 0, 0, 0, 0, 53);
        bus.write(53, 132);
        executeInstruction(Opcode.OR_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 132 | 23, 0, 0, 0, 0, 0, 0, 53 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void OR_A_HLR2() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 3);
        bus.write(3, 0);
        executeInstruction(Opcode.OR_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1000_0000, 0, 0, 0, 0, 0, 3 },
                cpu._testGetPcSpAFBCDEHL());
    }
    // XOR

    @Test
    void XOR_A_N8() {
        cpu.setAllRegs(0b1111_1111, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 0);
        executeInstruction(Opcode.XOR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 255, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_N82() {
        cpu.setAllRegs(244, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 12);
        executeInstruction(Opcode.XOR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 244 ^ 12, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_N83() {
        cpu.setAllRegs(255, 0, 0, 0, 0, 0, 0, 0);
        bus.write(1, 255);
        executeInstruction(Opcode.XOR_A_N8);
        assertArrayEquals(new int[] { 2, 0, 0, 0b1000_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_R8() {
        cpu.setAllRegs(221, 0, 12, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.XOR_A_B);
        assertArrayEquals(
                new int[] { 1, 0, 221 ^ 12, 0b0000_0000, 12, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_R82() {
        cpu.setAllRegs(42, 0, 42, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.XOR_A_B);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1000_0000, 42, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_HLR() {
        cpu.setAllRegs(23, 0, 0, 0, 0, 0, 0, 53);
        bus.write(53, 132);
        executeInstruction(Opcode.XOR_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 132 ^ 23, 0, 0, 0, 0, 0, 0, 53 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_HLR2() {
        cpu.setAllRegs(122, 0, 0, 0, 0, 0, 0, 3);
        bus.write(3, 122);
        executeInstruction(Opcode.XOR_A_HLR);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1000_0000, 0, 0, 0, 0, 0, 3 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void XOR_A_HLR3() {
        cpu.setAllRegs(122, 0, 0, 0, 0, 0, 0, 3);
        bus.write(3, 0);
        executeInstruction(Opcode.XOR_A_HLR);
        assertArrayEquals(
                new int[] { 1, 0, 122, 0b0000_0000, 0, 0, 0, 0, 0, 3 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CPL() {
        cpu.setAllRegs(122, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.CPL);
        assertArrayEquals(
                new int[] { 1, 0, 0b1000_0101, 0b0110_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CPL2() {
        cpu.setAllRegs(255, 144, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.CPL);
        assertArrayEquals(new int[] { 1, 0, 0, 0b1111_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CPL3() {
        cpu.setAllRegs(132, 16, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.CPL);
        assertArrayEquals(new int[] { 1, 0, Bits.complement8(132), 0b0111_0000,
                0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_R8() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0b1101_1011, 0);
        executeInstruction(Opcode.SLA_H);
        assertArrayEquals(
                new int[] { 2, 0, 0, 0b0001_0000, 0, 0, 0, 0, 0b1011_0110, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_R82() {
        cpu.setAllRegs(0, 0, 0b0101_1011, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SLA_B);
        assertArrayEquals(
                new int[] { 2, 0, 0, 0b0000_0000, 0b1011_0110, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_R83() {
        cpu.setAllRegs(0, 0, 0b1000_0000, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SLA_B);
        assertArrayEquals(new int[] { 2, 0, 0, 0b1001_0000, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SLA_HLR() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 122);
        bus.write(122, 0b1101_1011);
        executeInstruction(Opcode.SLA_HLR);
        assertArrayEquals(
                new int[] { 2, 0, 0, 0b0001_0000, 0, 0, 0, 0, 0, 122 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1011_0110, bus.read(122));
    }

    @Test
    void SLA_HLR2() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 122);
        bus.write(122, 0b0101_1011);
        executeInstruction(Opcode.SLA_HLR);
        assertArrayEquals(
                new int[] { 2, 0, 0, 0b0000_0000, 0, 0, 0, 0, 0, 122 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1011_0110, bus.read(122));
    }

    @Test
    void SLA_HLR3() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 122);
        bus.write(122, 0b1000_0000);
        executeInstruction(Opcode.SLA_HLR);
        assertArrayEquals(
                new int[] { 2, 0, 0, 0b1001_0000, 0, 0, 0, 0, 0, 122 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(122));
    }

    @Test
    void SCFworksWell() {
        cpu.setAllRegs(0, 128, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SCF);
        assertArrayEquals(new int[] { 1, 0, 0, 144, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SCFworksWell2() {
        cpu.setAllRegs(1, 176, 2, 3, 4, 5, 6, 7);
        executeInstruction(Opcode.SCF);
        assertArrayEquals(new int[] { 1, 0, 1, 144, 2, 3, 4, 5, 6, 7 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CCFworksWell() {
        cpu.setAllRegs(0, 176, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.CCF);
        assertArrayEquals(new int[] { 1, 0, 0, 128, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void CCFworksWell2() {
        cpu.setAllRegs(1, 160, 2, 3, 4, 5, 6, 7);
        executeInstruction(Opcode.CCF);
        assertArrayEquals(new int[] { 1, 0, 1, 144, 2, 3, 4, 5, 6, 7 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DAAworksWell() {
        cpu.setAllRegs(0x9B, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.DAA);
        assertArrayEquals(new int[] { 1, 0, 1, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void DAAworksWell2() {
        cpu.setAllRegs(0xFF, 0x70, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.DAA);
        assertArrayEquals(new int[] { 1, 0, 0x99, 0x50, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void DAAdoesNothing() {
        cpu.setAllRegs(0x23, 0x40, 1, 2, 3, 4, 5, 6);
        executeInstruction(Opcode.DAA);
        assertArrayEquals(new int[] { 1, 0, 0x23, 0x40, 1, 2, 3, 4, 5, 6 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void BITregWorksWell() {
        cpu.setAllRegs(0, 0x10, 0, 0, 0x10, 0, 0, 0);
        executeInstruction(Opcode.BIT_7_D);
        assertArrayEquals(new int[] { Opcode.BIT_7_D.totalBytes, 0, 0, 0xB0, 0,
                0, 0x10, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void BITregWorkWell2() {
        cpu.setAllRegs(0, 0x10, 0xF, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.BIT_2_B);
        assertArrayEquals(new int[] { Opcode.BIT_2_B.totalBytes, 0, 0, 0x30,
                0xF, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void BitHLworksWell() {
        cpu.setAllRegs(0, 0x10, 0, 0, 0, 0, 0x10, 0xB9);
        bus.write(0xB9, 0xAB);
        executeInstruction(Opcode.BIT_4_HLR);
        assertArrayEquals(new int[] { Opcode.BIT_4_HLR.totalBytes, 0, 0, 0xB0,
                0, 0, 0, 0, 0x10, 0xB9 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void BitHLworksWell2() {
        cpu.setAllRegs(1, 0x10, 2, 3, 4, 5, 0x10, 0xB9);
        bus.write(0x10B9, 0xBB);
        executeInstruction(Opcode.BIT_1_HLR);
        assertArrayEquals(new int[] { Opcode.BIT_1_HLR.totalBytes, 0, 1, 0x30,
                2, 3, 4, 5, 0x10, 0xB9 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SETworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SET_7_C);
        assertArrayEquals(new int[] { Opcode.SET_7_C.totalBytes, 0, 0, 0xFF, 0,
                0x80, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SETworksWell2() {
        cpu.setAllRegs(0x20, 0, 0, 0, 6, 0, 155, 0);
        executeInstruction(Opcode.SET_5_A);
        assertArrayEquals(new int[] { Opcode.SET_5_A.totalBytes, 0, 0x20, 0, 0,
                0, 6, 0, 155, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SetHLworksWell() {
        cpu.setAllRegs(0, 0, 0, 4, 0, 0, 0x30, 0xB3);
        bus.write(0x30B3, 169);
        executeInstruction(Opcode.SET_4_HLR);
        assertArrayEquals(new int[] { Opcode.SET_4_HLR.totalBytes, 0, 0, 0, 0,
                4, 0, 0, 0x30, 0xB3 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(185, bus.read(0x30B3));
    }

    @Test
    void SetHLworksWell2() {
        cpu.setAllRegs(0, 176, 0, 0, 0, 0, 0x13, 0xF2);
        bus.write(0x13F2, 185);
        executeInstruction(Opcode.SET_7_HLR);
        assertArrayEquals(new int[] { Opcode.SET_7_HLR.totalBytes, 0, 0, 176, 0,
                0, 0, 0, 0x13, 0xF2 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(185, bus.read(0x13F2));
    }

    @Test
    void RESworksWell() {
        cpu.setAllRegs(0, 0, 0, 0xFF, 5, 2, 155, 0xFD);
        executeInstruction(Opcode.RES_0_C);
        assertArrayEquals(new int[] { Opcode.RES_0_C.totalBytes, 0, 0, 0, 0,
                0xFE, 5, 2, 155, 0xFD }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RESworksWell2() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0x7F);
        executeInstruction(Opcode.RES_7_L);
        assertArrayEquals(new int[] { Opcode.RES_7_L.totalBytes, 0, 0, 0, 0, 0,
                0, 0, 0, 0x7F }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void ResHLworksWell() {
        cpu.setAllRegs(1, 2, 3, 4, 5, 6, 0xD8, 0x32);
        bus.write(0xD832, 0xFF);
        executeInstruction(Opcode.RES_0_HLR);
        assertArrayEquals(new int[] { Opcode.RES_0_HLR.totalBytes, 0, 1, 2, 3,
                4, 5, 6, 0xD8, 0x32 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xFE, bus.read(0xD832));
    }

    @Test
    void ResHLworksWell2() {
        cpu.setAllRegs(1, 2, 3, 4, 5, 6, 0x18, 0xFF);
        bus.write(0x18FF, 0xF);
        executeInstruction(Opcode.RES_6_HLR);
        assertArrayEquals(new int[] { Opcode.RES_0_HLR.totalBytes, 0, 1, 2, 3,
                4, 5, 6, 0x18, 0xFF }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xF, bus.read(0x18FF));
    }

    @Test
    void RotRightThenLeftDoesNothing() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 93, 0, 0);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.RRC_E.encoding);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.RLC_E.encoding);
        cycleCpu(cpu, 2 * Opcode.RRC_E.cycles);
        assertArrayEquals(new int[] { 2 * Opcode.RRC_E.totalBytes, 0, 0, 0x10,
                0, 0, 0, 93, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotLeftworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0xF, 0);
        executeInstruction(Opcode.RLC_H);
        assertArrayEquals(new int[] { Opcode.RLC_H.totalBytes, 0, 0, 0, 0, 0, 0,
                0, 30, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotLeftworksOnTrivialValue() {
        executeInstruction(Opcode.RLC_B);
        assertArrayEquals(new int[] { Opcode.RLC_B.totalBytes, 0, 0, 128, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotRightworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0xF, 0);
        executeInstruction(Opcode.RRC_H);
        assertArrayEquals(new int[] { Opcode.RRC_H.totalBytes, 0, 0, 16, 0, 0,
                0, 0, 135, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotRightworksOnTrivialValue() {
        executeInstruction(Opcode.RRC_B);
        assertArrayEquals(new int[] { Opcode.RRC_B.totalBytes, 0, 0, 128, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void rotHLRightLeftDoesNothing() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0x45, 0x3A);
        bus.write(0x453A, 156);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.RRC_HLR.encoding);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.RLC_HLR.encoding);
        cycleCpu(cpu, 2 * Opcode.RRC_HLR.cycles);
        assertArrayEquals(new int[] { 2 * Opcode.RRC_B.totalBytes, 0, 0, 0, 0,
                0, 0, 0, 0x45, 0x3A }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(156, bus.read(0x453A));
    }

    @Test
    void RotHLLeftworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0xF, 0xFF);
        bus.write(0xFFF, 0b1000_1111);
        executeInstruction(Opcode.RLC_HLR);
        assertArrayEquals(new int[] { Opcode.RLC_HLR.totalBytes, 0, 0, 0x10, 0,
                0, 0, 0, 0xF, 0xFF }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0001_1111, bus.read(0xFFF));
    }

    @Test
    void RotHLLeftworksOnTrivialValue() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0x45, 0x34);
        executeInstruction(Opcode.RLC_HLR);
        assertArrayEquals(new int[] { Opcode.RLC_HLR.totalBytes, 0, 0, 128, 0,
                0, 0, 0, 0x45, 0x34 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x4534));
    }

    @Test
    void RotHLRightworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0xF, 0);
        bus.write(0xF00, 0b1000_0001);
        executeInstruction(Opcode.RRC_HLR);
        assertArrayEquals(new int[] { Opcode.RRC_HLR.totalBytes, 0, 0, 16, 0, 0,
                0, 0, 0xF, 0 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_0000, bus.read(0xF00));
    }

    @Test
    void RotHLRightworksOnTrivialValue() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0x12, 0x34);
        executeInstruction(Opcode.RRC_HLR);
        assertArrayEquals(new int[] { Opcode.RRC_B.totalBytes, 0, 0, 128, 0, 0,
                0, 0, 0x12, 0x34 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x1234));
    }

    @Test
    void RotWithCRightThenLeftDoesNothing() {
        cpu.setAllRegs(0, 0xFF, 0, 0b1000_0001, 0, 0, 0, 0);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.RR_C.encoding);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.RL_C.encoding);
        cycleCpu(cpu, 2 * Opcode.RR_C.cycles);
        assertArrayEquals(new int[] { 2 * Opcode.RL_C.totalBytes, 0, 0, 0x10, 0,
                0b1000_0001, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void RotWithCRightWorksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 1, 1, 2);
        executeInstruction(Opcode.RR_E);
        assertArrayEquals(new int[] { Opcode.RR_E.totalBytes, 0, 0, 0x10, 0, 0,
                0, 0b1000_0000, 1, 2 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotWithCRightWorksOnTrivialValue() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RR_L);
        assertArrayEquals(new int[] { Opcode.RR_L.totalBytes, 0, 0, 0, 0, 0, 0,
                0, 0, 0x80 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotWithCLeftWorksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0b1111_1110, 0, 0, 0);
        executeInstruction(Opcode.RL_D);
        assertArrayEquals(new int[] { Opcode.RL_D.totalBytes, 0, 0, 0x10, 0, 0,
                0b1111_1101, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotWithCLeftWorksOnTrivialValue() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RL_C);
        assertArrayEquals(
                new int[] { Opcode.RL_C.totalBytes, 0, 0, 0, 0, 1, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotHLRightWithCworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0xFD);
        bus.write(0xFD, 0b1001_1001);
        executeInstruction(Opcode.RR_HLR);
        assertArrayEquals(new int[] { Opcode.RR_HLR.totalBytes, 0, 0, 0x10, 0,
                0, 0, 0, 0, 0xFD }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1100_1100, bus.read(0xFD));
    }

    @Test
    void RotHlLeftWithCworksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0xFD);
        bus.write(0xFD, 0b1001_1001);
        executeInstruction(Opcode.RL_HLR);
        assertArrayEquals(new int[] { Opcode.RL_HLR.totalBytes, 0, 0, 0x10, 0,
                0, 0, 0, 0, 0xFD }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0011_0011, bus.read(0xFD));

    }

    @Test
    void SwapWorksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0b1010_0101, 0);
        executeInstruction(Opcode.SWAP_H);
        assertArrayEquals(new int[] { Opcode.SWAP_H.totalBytes, 0, 0, 0, 0, 0,
                0, 0, 0b0101_1010, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SwapTwiceDoesNothing() {
        cpu.setAllRegs(0xF, 0xFF, 0, 0, 0, 0, 0, 0);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.SWAP_A.encoding);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.SWAP_A.encoding);
        cycleCpu(cpu, 2 * Opcode.SWAP_A.cycles);
        assertArrayEquals(new int[] { 2 * Opcode.SWAP_A.totalBytes, 0, 0xF, 0,
                0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SwapOnTrivialValueDoesNothing() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SWAP_D);
        assertArrayEquals(new int[] { Opcode.SWAP_D.totalBytes, 0, 0, 128, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SwapHLWorksWell() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0x34, 0x20);
        bus.write(0x3420, 0b1010_0101);
        executeInstruction(Opcode.SWAP_HLR);
        assertArrayEquals(new int[] { Opcode.SWAP_HLR.totalBytes, 0, 0, 0, 0, 0,
                0, 0, 0x34, 0x20 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b0101_1010, bus.read(0x3420));
    }

    @Test
    void SwapHLTwiceDoesNothing() {
        cpu.setAllRegs(0xF, 0xFF, 0, 0, 0, 0, 0x23, 0xFF);
        bus.write(0x23FF, 0xFF);
        bus.write(0, 0xCB);
        bus.write(1, Opcode.SWAP_HLR.encoding);
        bus.write(2, 0xCB);
        bus.write(3, Opcode.SWAP_HLR.encoding);
        cycleCpu(cpu, 2 * Opcode.SWAP_HLR.cycles);
        assertArrayEquals(new int[] { 2 * Opcode.SWAP_HLR.totalBytes, 0, 0xF, 0,
                0, 0, 0, 0, 0x23, 0xFF }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xFF, bus.read(0x23FF));
    }

    @Test
    void SwapHLOnTrivialValueDoesNothing() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0xFF, 0xFE);
        executeInstruction(Opcode.SWAP_HLR);
        assertArrayEquals(new int[] { Opcode.SWAP_HLR.totalBytes, 0, 0, 128, 0,
                0, 0, 0, 0xFF, 0xFE }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotARightWorksWell() {
        cpu.setAllRegs(0b1000_0001, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RRCA);
        assertArrayEquals(new int[] { Opcode.RRCA.totalBytes, 0, 0b1100_0000,
                16, 0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotALeftWorksWell() {
        cpu.setAllRegs(0b1000_0001, 0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RLCA);
        assertArrayEquals(new int[] { Opcode.RLCA.totalBytes, 0, 0b0011, 16, 0,
                0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotARightWithCWorksWell() {
        cpu.setAllRegs(0b0000_0001, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RRA);
        assertArrayEquals(new int[] { Opcode.RRA.totalBytes, 0, 0x80, 16, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotARightWithCWorksWell2() {
        cpu.setAllRegs(0b0000_0001, 0xF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RRA);
        assertArrayEquals(
                new int[] { Opcode.RRA.totalBytes, 0, 0, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotALeftWithCWorksWell() {
        cpu.setAllRegs(0b1000_0000, 0xFF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RLA);
        assertArrayEquals(
                new int[] { Opcode.RLA.totalBytes, 0, 1, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RotALeftWithCWorksWell2() {
        cpu.setAllRegs(0b1000_0000, 0xF, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.RLA);
        assertArrayEquals(
                new int[] { Opcode.RLA.totalBytes, 0, 0, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRLworksWell() {
        cpu.setAllRegs(128, 0xF0, 0, 0, 0, 0, 0, 0);
        executeInstruction(Opcode.SRL_A);
        assertArrayEquals(new int[] { Opcode.SRL_A.totalBytes, 0, 64, 0, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRLHLworksWell() {
        cpu.setAllRegs(128, 0xF0, 0, 0, 0, 0, 0, 0x55);
        bus.write(0x55, 1);
        executeInstruction(Opcode.SRL_HLR);
        assertArrayEquals(new int[] { Opcode.SRL_HLR.totalBytes, 0, 128, 144, 0,
                0, 0, 0, 0, 0x55 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0, bus.read(0x55));
    }

    @Test
    void SRAworksWell() {
        cpu.setAllRegs(0, 0, 0, 0b10_0010, 0, 0, 0, 0);
        executeInstruction(Opcode.SRA_C);
        assertArrayEquals(new int[] { Opcode.SRA_C.totalBytes, 0, 0, 0, 0,
                0b01_0001, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void SRAworksWell2() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0b1100_1101);
        executeInstruction(Opcode.SRA_L);
        assertArrayEquals(new int[] { Opcode.SRA_L.totalBytes, 0, 0, 16, 0, 0,
                0, 0, 0, 0b1110_0110 }, cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void SRAHLworksWell2() {
        cpu.setAllRegs(0, 0xFF, 0, 0, 0, 0, 0x56, 0xF4);
        bus.write(0x56F4, 0b1100_1101);
        executeInstruction(Opcode.SRA_HLR);
        assertArrayEquals(new int[] { Opcode.SRA_HLR.totalBytes, 0, 0, 16, 0, 0,
                0, 0, 0x56, 0xF4 }, cpu._testGetPcSpAFBCDEHL());
        assertEquals(0b1110_0110, bus.read(0x56F4));

    }

}
