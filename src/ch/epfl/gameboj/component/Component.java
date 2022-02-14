/**
* Représente un composant connecté aux bus d'adresses et de données
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {
    /**
     * Valeur renvoyée par le composant quand il n'y a pas de donnée
     */
    public static final int NO_DATA = 0x100;

    /**
     * Retourne l'octet stocké à l'adresse donnée par le composant.
     * 
     * @param address
     *            l'adresse de l'octet à retourner
     * @return l'octet stocké à l'adresse donnée
     */
    int read(int address);

    /**
     * Stocke la valeur donnée à l'adresse donnée dans le composant.
     * 
     * @param address
     *            l'adresse de l'octet dans lequel stocker la valeur
     * @param data
     *            la valeur à stocker
     */
    void write(int address, int data);

    /**
     * Attache le composant au bus donné
     * 
     * @param bus
     *            le bus auquel attacher le composant
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }

}
