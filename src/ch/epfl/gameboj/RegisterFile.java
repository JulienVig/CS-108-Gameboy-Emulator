/**
* Représente un banc de registres 8 bits.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class RegisterFile<E extends Register> {
    private final byte[] file;

    /**
     * Construit un banc de registres 8 bits dont la taille est égale à la taille du tableau donné.
     * 
     * @param allRegs
     *            le tableau dont la taille est à copier
     */
    public RegisterFile(E[] allRegs) {
        file = new byte[allRegs.length];
    }

    /**
     * Retourne la valeur 8 bits contenue dans le registre .
     * 
     * @param reg
     *            le registre où se trouve la valeur
     * @return la valeur contenue dans le registre
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(file[reg.index()]);
    }

    /**
     * Modifie le contenu du registre donné pour qu'il soit égal à la valeur 8 bits donnée.
     * 
     * @param reg
     *            le registre à modifier
     * @param newValue
     *            la valeur à assigner au registre
     * @throws IllegalArgumentException
     *             si l'entier passé en argument n'est pas une valeur 8 bits
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        file[reg.index()] = (byte) newValue;
    }

    /**
     * Retourne vrai si et seulement si le bit donné du registre donné vaut 1.
     * 
     * @param reg
     *            registre à tester
     * @param b
     *            bit à tester
     * @return vrai si le bit testé du registre donné vaut 1
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }

    /**
     * Modifie la valeur stockée dans le registre donné pour que le bit donné ait la nouvelle valeur donnée.
     * 
     * @param reg
     *            registre à modifier
     * @param bit
     *            bit à modifier
     * @param newValue
     *            la valeur à donner au bit
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }

}
