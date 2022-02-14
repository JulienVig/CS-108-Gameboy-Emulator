/**
* Interface implémentée par les types énumérés réprésentant un esemble de bits.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.bits;

public interface Bit {
    /**
     * Retourne la position de l'élément dans l'énumération.
     * 
     * @return un entier, la position de l'élément dans l'énumération
     */
    int ordinal();

    /**
     * Retourne l'indice de l'élément dans l'énumération.
     * 
     * @return un entier, la position de l'élément dans l'énumération
     */
    default int index() {
        return this.ordinal();
    }

    /**
     * Retourne le masque binaire dont seul le bit correspondant à l'indice vaut 1.
     * 
     * @return un masque binaire sous forme d'un entier
     */
    default int mask() {
        return Bits.mask(this.index());
    }
}
