/**
* Représente un composant contrôlant l'accès à une mémoire vive.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

public final class RamController implements Component {
    private final Ram ram;
    private final int startAddress;
    private final int endAddress;

    /**
     * Construit un contrôleur pour la mémoire vive donnée.
     * 
     * @param ram
     *            mémoire à laquelle est lié le controlleur
     * @param startAddress
     *            adresse à partir de laquelle la mémoire est accessible
     * @param endAddress
     *            adresse jusqu'à laquelle la mémoire est accessible (non inclue)
     * @throws NullPointerException
     *             si la mémoire donnée est nulle
     * @throws IllegalArgumentException
     *             si une des deux adresses n'est pas une valeur 16 bits, ou si l'intervalle accessible a une taille
     *             négative ou supérieure à celle de la mémoire
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Objects.requireNonNull(ram);
        Preconditions.checkArgument(endAddress - startAddress > 0 && endAddress - startAddress <= ram.size());
        this.ram = ram;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
    }

    /**
     * Construit un contrôleur de la même manière que le constructeur précédent, de manière à ce que la totalité de la
     * mémoire vive soit accessible.
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, startAddress + ram.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < startAddress || address >= endAddress) {
            return NO_DATA;
        }
        return ram.read(address - startAddress);
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
        if (address >= startAddress && address < endAddress) {
            ram.write(address - startAddress, data);
        }
    }

}
