/**
* Représente un contrôleur de banque mémoire dotée d'une mémoire morte de 32 768 octets.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class MBC0 implements Component {
    private static final int ROM_SIZE = 0x8000;
    private final Rom memory;

    /**
     * Construit un contrôleur de type 0 pour la mémoire donnée.
     * 
     * @param rom
     *            mémoire morte liée au contrôleur
     * @throws NullPointerException
     *             si la mémoire est nulle
     * @throws IllegalArgumentException
     *             si la mémoire ne contient pas 32 768 octets
     */
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        Preconditions.checkArgument(rom.size() == ROM_SIZE);
        memory = rom;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < ROM_SIZE) {
            return memory.read(address);
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
    }

}
