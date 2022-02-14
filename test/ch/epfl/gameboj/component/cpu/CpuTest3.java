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

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class CpuTest3 {
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
    void JPN16worksWell() {
        cpu.setAllRegs(0, 0, 0xFF, 0, 0, 0, 0, 0);
        bus.write(0, Opcode.JP_N16.encoding);
        bus.write(1, 0x45);
        bus.write(2, 0x23);
        bus.write(0x2345, Opcode.LD_A_B.encoding);
        cycleCpu(cpu, Opcode.JP_N16.cycles + Opcode.LD_A_B.cycles);
        assertArrayEquals(new int[] { 0x2345 + Opcode.LD_A_B.totalBytes, 0,
                0xFF, 0, 0xFF, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPN16overflows() {
        bus.write(0, Opcode.JP_N16.encoding);
        bus.write(1, 0xFE);
        bus.write(2, 0xFF);
        bus.write(0xFFFE, Opcode.LD_A_N8.encoding);
        cycleCpu(cpu, Opcode.JP_N16.cycles + Opcode.LD_A_N8.cycles);
        assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPCCN16worksWell() {
        cpu.setAllRegs(0, 0x80, 0, 0, 0, 0, 0, 0);
        bus.write(0, Opcode.JP_Z_N16.encoding);
        bus.write(1, 0x45);
        bus.write(2, 0x23);
        cycleCpu(cpu, Opcode.JP_Z_N16.cycles);
        assertArrayEquals(new int[] { 0x2345, 0, 0, 0x80, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPCCN16worksWell2() {
        cpu.setAllRegs(0, 0xE0, 0, 0, 0, 0, 0, 0);
        bus.write(0, Opcode.JP_NC_N16.encoding);
        bus.write(1, 0x45);
        bus.write(2, 0x23);
        cycleCpu(cpu, Opcode.JP_NC_N16.cycles);
        assertArrayEquals(new int[] { 0x2345, 0, 0, 0xE0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPCCN16doesNothing() {
        bus.write(0, Opcode.JP_C_N16.encoding);
        bus.write(1, 0x45);
        bus.write(2, 0x23);
        cycleCpu(cpu, Opcode.JP_C_N16.cycles);
        assertArrayEquals(new int[] { Opcode.JP_C_N16.totalBytes, 0, 0, 0, 0, 0,
                0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPHLworksWell() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0xFF, 0xFE);
        bus.write(0, Opcode.JP_HL.encoding);
        bus.write(0xFFFE, Opcode.LD_A_B.encoding);
        cycleCpu(cpu, Opcode.JP_HL.cycles + Opcode.LD_A_B.cycles);
        assertArrayEquals(new int[] { 0xFFFE + Opcode.LD_A_B.totalBytes, 0, 0,
                0, 0, 0, 0, 0, 0xFF, 0xFE }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JPHLworksWell2() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 1);
        bus.write(0, Opcode.JP_HL.encoding);
        cycleCpu(cpu, Opcode.JP_HL.cycles);
        assertArrayEquals(new int[] { Opcode.JP_HL.totalBytes, 0, 0, 0, 0, 0, 0,
                0, 0, 1 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JRE8worksWell() {
        cpu.setAllRegs(0, 0, 0xFF, 0, 0, 0, 0, 0);
        cpu.setPC(0xA);
        bus.write(0xA, Opcode.JR_E8.encoding);
        bus.write(0xB, 0xFA);
        bus.write(0xC, 0xFF);
        bus.write(6, Opcode.LD_A_B.encoding);
        cycleCpu(cpu, Opcode.JR_E8.cycles + Opcode.LD_A_B.cycles);
        assertArrayEquals(new int[] { 6 + Opcode.LD_A_B.totalBytes, 0, 0xFF, 0,
                0xFF, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void JRE8underflows() {
        cpu.setPC(56);
        bus.write(56, Opcode.JR_E8.encoding);
        bus.write(57, 0b11000011);
        bus.write(58, 0xFF);
        bus.write(0xFFFD, Opcode.LD_C_N8.encoding);
        bus.write(0xFFFE, 98);
        cycleCpu(cpu, Opcode.JR_E8.cycles + Opcode.LD_C_N8.cycles);
        assertArrayEquals(new int[] { 0xFFFD + Opcode.LD_C_N8.totalBytes, 0, 0,
                0, 0, 98, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());

    }

    @Test
    void CALLN16worksWell() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0xFF, 0, 0);
        cpu.setSP(0xFFFF);
        cpu.setPC(240);
        bus.write(240, Opcode.CALL_N16.encoding);
        bus.write(241, 0x45);
        bus.write(242, 0x23);
        bus.write(243, 0xFF);
        cycleCpu(cpu, Opcode.CALL_N16.totalBytes);
        assertArrayEquals(
                new int[] { 0x2345, 0xFFFD, 0, 0, 0, 0, 0, 0xFF, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(243, bus.read(0xFFFD));
    }

    @Test
    void CALLccN16worksWell() {
        cpu.setAllRegs(0, 0x90, 0, 0, 0, 0, 0, 0);
        cpu.setSP(0xFFFF);
        cpu.setPC(25300);
        bus.write(25300, Opcode.CALL_C_N16.encoding);
        bus.write(25301, 0x45);
        bus.write(25302, 0x23);
        cycleCpu(cpu, Opcode.CALL_C_N16.cycles);
        assertArrayEquals(
                new int[] { 0x2345, 0xFFFD, 0, 0x90, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
        assertEquals(0xD7, bus.read(0xFFFD));
        assertEquals(0x62, bus.read(0xFFFE));

    }

    @Test
    void HALTwithoutinterrupts() {
        cpu.setAllRegs(0, 0, 0, 0, 65, 0, 0, 0);
        bus.write(0, Opcode.LD_A_D.encoding);
        bus.write(1, Opcode.HALT.encoding);
        bus.write(2, Opcode.LD_C_D.encoding);
        bus.write(3, Opcode.JP_N16.encoding);
        cycleCpu(cpu, Opcode.LD_A_A.cycles + Opcode.HALT.cycles
                + Opcode.LD_C_D.cycles + Opcode.JP_N16.cycles);
        assertArrayEquals(
                new int[] { Opcode.LD_A_D.totalBytes + Opcode.HALT.totalBytes,
                        0, 65, 0, 0, 0, 65, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
        cpu.setInterruptRegs(false, 0x1, 0x1);
        cycleCpu(cpu, Opcode.LD_C_D.cycles);
        assertArrayEquals(new int[] {
                Opcode.LD_A_D.totalBytes + Opcode.HALT.totalBytes
                        + Opcode.LD_C_D.totalBytes,
                0, 65, 0, 0, 65, 65, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
        cycleCpu(cpu, Opcode.JP_N16.cycles);
        assertArrayEquals(new int[] { 0, 0, 65, 0, 0, 65, 65, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void HALTwithinterrupts() {
        cpu.setAllRegs(0, 0, 0, 0, 65, 0, 0, 0);
        bus.write(0, Opcode.LD_A_D.encoding);
        bus.write(1, Opcode.HALT.encoding);
        bus.write(2, Opcode.LD_C_D.encoding);
        bus.write(64, Opcode.RETI.encoding);
        cycleCpu(cpu, Opcode.LD_A_A.cycles + Opcode.HALT.cycles
                + Opcode.LD_C_D.cycles + Opcode.RETI.cycles);
        assertArrayEquals(
                new int[] { Opcode.LD_A_D.totalBytes + Opcode.HALT.totalBytes,
                        0, 65, 0, 0, 0, 65, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
        cpu.setInterruptRegs(true, 0x1, 0x1);
        cycleCpu(cpu, 1);
        assertArrayEquals(new int[] { AddressMap.INTERRUPTS[0x1 - 1], 0xFFFE,
                65, 0, 0, 0, 65, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
        cycleCpu(cpu, Opcode.RETI.cycles + Opcode.LD_C_D.cycles+5);
        assertArrayEquals(new int[] { 3, 0, 65, 0, 0, 65, 65, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void EI() {
        cpu.setInterruptRegs(false, 0, 0);
        executeInstruction(Opcode.EI);
        assertArrayEquals(new int[] { 1, 0, 0 }, cpu.get_IME_IE_IF());
    }

    @Test
    void DI() {
        cpu.setInterruptRegs(true, 0, 0);
        executeInstruction(Opcode.DI);
        assertArrayEquals(new int[] { 0, 0, 0 }, cpu.get_IME_IE_IF());
    }

    @Test
    void RET() {
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET);
        assertArrayEquals(new int[] { 42, 880, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC1() {
        cpu.setAllRegs(0, 0b0000_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_C);
        assertArrayEquals(new int[] { 1, 878, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC2() {
        cpu.setAllRegs(0, 0b0001_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_C);
        assertArrayEquals(new int[] { 42, 880, 0, 16, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC3() {
        cpu.setAllRegs(0, 0b0000_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_NC);
        assertArrayEquals(new int[] { 42, 880, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC4() {
        cpu.setAllRegs(0, 0b0000_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_NZ);
        assertArrayEquals(new int[] { 42, 880, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC5() {
        cpu.setAllRegs(0, 0b1000_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_Z);
        assertArrayEquals(new int[] { 42, 880, 0, 128, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RET_CC6() {
        cpu.setAllRegs(0, 0b0000_0000, 0, 0, 0, 0, 0, 0);
        cpu.setPC(0);
        cpu.setSP(878);
        bus.write(878, 42);
        executeInstruction(Opcode.RET_Z);
        assertArrayEquals(new int[] { 1, 878, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

    @Test
    void RST_U3() {
        cpu.setAllRegs(0, 0, 0, 0, 0, 0, 0, 0);
        cpu.setSP(878);
        executeInstruction(Opcode.RST_3);
        assertEquals(Opcode.RST_3.totalBytes, bus.read(876));
        assertArrayEquals(new int[] { 24, 876, 0, 0, 0, 0, 0, 0, 0, 0 },
                cpu._testGetPcSpAFBCDEHL());
    }

}
