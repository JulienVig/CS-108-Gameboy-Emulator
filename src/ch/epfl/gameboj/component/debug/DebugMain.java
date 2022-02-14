/**
* Classe permettant de lancer les tests de Blargg
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.debug;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

public final class DebugMain {
    public static void main(String[] args) throws IOException {
      File romFile = new File("Blargg\\instr_timing.gb");
      long cycles = Long.parseLong("30000000");

      GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
      Component printer = new DebugPrintComponent();
      printer.attachTo(gb.bus());
      while (gb.cycles() < cycles) {
        long nextCycles = Math.min(gb.cycles() + 17556, cycles);
        gb.runUntil(nextCycles);
        gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
      }
    }
  }