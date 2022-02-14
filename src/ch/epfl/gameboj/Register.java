/**
* Interface implémentée par les types énumérés représentant les registres d'un même banc.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj;

public interface Register {

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

}
