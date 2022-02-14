/**
* Représente une cartouche.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

public final class Cartridge implements Component {
    private final Component mbc;
    private final static int INDEX_CARTRIDGE_TYPE = 0x147;
    private final static int RAM_SIZE = 0x149;
    private final static int[] TYPES_RAM_SIZE = { 0, 2048, 8192, 32768 };

    private Cartridge(Component controler) {
        mbc = controler;
    }

    /**
     * Retourne une cartouche dont la mémoire morte contient les octets du fichier donné.
     * 
     * @param romFile
     *            Fichier dont le contenu est à stocker dans la mémoire morte
     * @throws IOException
     *             en cas d'erreur concernant l'entrée/sortie du fichier romFile, par exemple si le fichier n'existe pas
     * @throws IllegalArgumentException
     *             si le fichier romFile ne contient pas 0, 1, 2 ou 3 à la position 0x147
     * @return une nouvelle cartouche
     * 
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream stream = new FileInputStream(romFile)) {
            Rom memory = new Rom(stream.readAllBytes());
            int type = memory.read(INDEX_CARTRIDGE_TYPE);
            Preconditions.checkArgument(type <= 3);
            return new Cartridge(
                    type == 0 ? new MBC0(memory) : new MBC1(memory, TYPES_RAM_SIZE[memory.read(RAM_SIZE)]));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
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
        mbc.write(address, data);
    }
    

    /**
     * Sauvegarde le contenu de la mémoire vive de la cartouche dans le fichier spécifié
     * 
     * @param file
     *            le fichier dans lequel sauvegarder la mémoire
     */
    public void saveRam(File file) {
        if (mbc instanceof MBC1) {
            ((MBC1) mbc).saveRam(file);
        }
    }
    
    /**
     * Charge le contenu du fichier spécifié dans la mémoire vive de la cartouche.
     * 
     * @param file
     *            le fichier depuis lequel charger la mémoire
     */
    public void loadRam(File file) {
        if (mbc instanceof MBC1) {
            ((MBC1) mbc).loadRam(file);
        }
    }

}
