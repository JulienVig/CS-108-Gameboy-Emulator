/**
*  Bus d'adresses et de données connectant les composants du Game Boy entre eux.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

public final class Bus {
	private final ArrayList<Component> components;
	private static final int DEFAULT_READ_VALUE = 0xFF;

	/**
	 * Crée un nouveau bus et initialise son tableau de composants.
	 */
	public Bus() {
		components = new ArrayList<Component>();
	}

	/**
	 * Attache le composant donné au bus.
	 * 
	 * @param component
	 *            le composant à attacher au bus
	 * @throws NullPointerException
	 *             si le composant vaut null
	 */
	public void attach(Component component) {
		components.add(Objects.requireNonNull(component));
	}

	/**
	 * Retourne la valeur stockée à l'adresse donnée.
	 * 
	 * @param address
	 *            l'adresse de la valeur à retourner
	 * @throws IllegalArgumentException
	 *             si l'adresse n'est pas une valeur 16 bits
	 * @return la valeur stockée à l'adresse passée en argument ou 255 si aucun des
	 *         composants n'a de valeur à l'adresse indiquée
	 */
	public int read(int address) {
		Preconditions.checkBits16(address);
		for (Component c : components) {
			if (c.read(address) != Component.NO_DATA) {
				return c.read(address);
			}
		}
		return DEFAULT_READ_VALUE;
	}

	/**
	 * Ecrit la valeur à l'adresse donnée dans tous les composants connectés au bus.
	 * 
	 * @param address
	 *            l'adresse où écrire la valeur
	 * @param data
	 *            la valeur à écrire à l'adresse indiquée
	 * @throws IllegalArgumentException
	 *             si l'adresse n'est pas une valeur 16 bits ou si la donnée n'est
	 *             pas une valeur 8 bits
	 */
	public void write(int address, int data) {
		Preconditions.checkBits16(address);
		Preconditions.checkBits8(data);
		for (Component c : components) {
			c.write(address, data);
		}
	}

}
