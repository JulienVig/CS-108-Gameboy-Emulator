/**
* Représente un composant piloté par l'horloge du système.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component;

public interface Clocked{

    /**
     * Demande au composant d'évoluer en exécutant toutes les opérations qu'il
     * doit exécuter durant le cycle d'index donné en argument.
     * 
     * @param cycle
     *            numéro du cycle à exécuter
     */
    void cycle(long cycle);
}
