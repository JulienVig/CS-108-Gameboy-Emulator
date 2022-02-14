/**
* Représente le clavier du GameBoy
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Joypad implements Component {
    private static final int LINE_SIZE = 4;
    private static final int LINE0_INDEX = 0;
    private static final int LINE1_INDEX = 1;
    private final Cpu cpu;
    private int P1;
    private int[] pressedLine;

    /**
     * Représente les touches du clavier du GameBoy
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    private enum P1bits implements Bit {
        STATE_COLUMN0, STATE_COLUMN1, STATE_COLUMN2, STATE_COLUMN3, SELECT_LINE0, SELECT_LINE1
    }

    /**
     * Construit un nouveau clavier et le lie à un processeur passé en argument afin de pouvoir lever des interruptions.
     * 
     * @param cpu
     *            le processeur lié.
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
        pressedLine = new int[2];
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        if (address == AddressMap.REG_P1) {
            updateP1();
            return Bits.complement8(P1);
        } else {
            return NO_DATA;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        if (address == AddressMap.REG_P1) {
            int mask = P1bits.SELECT_LINE0.mask() | P1bits.SELECT_LINE1.mask();
            P1 = (Bits.complement8(data) & mask) | (P1 & Bits.complement8(mask));
        }
    }

    /**
     * Simule la pression d'une touche.
     * 
     * @param k
     *            la touche pressée.
     */
    public void keyPressed(Key k) {
        int line = k.ordinal() / LINE_SIZE;
        int column = k.ordinal() % LINE_SIZE;
        pressedLine[line] = Bits.set(pressedLine[line], column, true);
        if (Bits.test(P1, P1bits.SELECT_LINE0) && line == LINE0_INDEX
                || Bits.test(P1, P1bits.SELECT_LINE1) && line == LINE1_INDEX) {
            cpu.requestInterrupt(Interrupt.JOYPAD);
        }
    }

    /**
     * Simule le relâchement d'une touche.
     * 
     * @param k
     *            la touche relâchée.
     */
    public void keyReleased(Key k) {
        int line = k.ordinal() / LINE_SIZE;
        int column = k.ordinal() % LINE_SIZE;
        pressedLine[line] = Bits.set(pressedLine[line], column, false);
    }

    private void updateP1() {
        P1 = (Bits.extract(P1, LINE_SIZE, LINE_SIZE) << LINE_SIZE)
                | (Bits.test(P1, P1bits.SELECT_LINE0) ? pressedLine[LINE0_INDEX] : 0)
                | (Bits.test(P1, P1bits.SELECT_LINE1) ? pressedLine[LINE1_INDEX] : 0);
    }
}
