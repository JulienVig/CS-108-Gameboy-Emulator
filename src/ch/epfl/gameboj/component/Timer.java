/**
* Représente le minuteur du GameBoy
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {
    private final Cpu cpu;
    private int mainTimer;
    private int TIMA;
    private int TMA;
    private int TAC;
    private static final int[] TAC_VALUES = { 9, 3, 5, 7 };

    /**
     * Construit un minuteur associé au processeur donné.
     * 
     * @param cpu
     *            le processeur associé au minuteur
     */
    public Timer(Cpu cpu) {
        this.cpu = Objects.requireNonNull(cpu);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        boolean s0 = state();
        mainTimer = Bits.clip(16, mainTimer + 4);
        incIfChange(s0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV:
            return Bits.extract(mainTimer, 8, 8);
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TAC:
            return TAC;
        default:
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
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        boolean s0 = state();
        switch (address) {
        case AddressMap.REG_DIV:
            mainTimer = 0;
            incIfChange(s0);
            break;
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;
        case AddressMap.REG_TMA:
            TMA = data;
            break;
        case AddressMap.REG_TAC:
            TAC = data;
            incIfChange(s0);
            break;
        }
    }

    /**
     * Retourne l'état du processeur.
     * 
     * @return true si le minuteur est activé et que la valeur d'un bit précis du compteur principal est 1
     */
    private boolean state() {
        return Bits.test(TAC, 2) && Bits.test(mainTimer, extractIndexFromTAC());
    }

    /**
     * Retourne l'indice du bit a tester du compteur principal.
     * 
     * @return l'indice contenu dans les 2 bits de poids faibles de TAC
     */
    private int extractIndexFromTAC() {
        return TAC_VALUES[Bits.clip(2, TAC)];
    }

    /**
     * Incrémente la valeur de TIMA si l'état du processeur passe de vrai à faux, sauf si TIMA vaut 0xFF, dans ce cas
     * TIMA prend la valeur contenu dans TMA.
     * 
     * @param previousState
     *            l'état précédant l'état actuel du processeur
     */
    private void incIfChange(boolean previousState) {
        if (!state() && previousState) {
            if (TIMA == 0xFF) {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            } else {
                TIMA++;
            }
        }
    }

}
