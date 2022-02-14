/**
* Représente le contrôleur de la mémoire morte de démarrage.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BootRomController implements Component {
    private final Cartridge cartridge;
    private final Rom bootRom;
    private boolean disabled;

    /**
     * Construit un contrôleur de mémoire de démarrage auquel la cartouche donnée est attachée.
     * 
     * @param cartridge
     *            la cartouche à attacher au contrôleur
     * @throws NullPointerException
     *             si la cartouche est nulle
     */
    public BootRomController(Cartridge cartridge) {
        this.cartridge = Objects.requireNonNull(cartridge);
        disabled = false;
        bootRom = new Rom(BootRom.DATA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (!disabled && address >= AddressMap.BOOT_ROM_START && address < AddressMap.BOOT_ROM_END) {
            return bootRom.read(address - AddressMap.BOOT_ROM_START);
        }
        return cartridge.read(address);
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
        if (address == AddressMap.REG_BOOT_ROM_DISABLE) {
            disabled = true;
        } else {
            cartridge.write(address, data);
        }
    }
}
