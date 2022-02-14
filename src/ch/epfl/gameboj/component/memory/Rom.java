/**
* Simule la mémoire morte de la console.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

public final class Rom {
	private final byte[] memory;

	/**
	 * Construit un nouvel objet de type Rom en créant une copie du tableau passé en
	 * argument.
	 * 
	 * @param data
	 *            un tableau de byte
	 */
	public Rom(byte[] data) {
		memory = Arrays.copyOf(data, data.length);
	}

	/**
	 * Retourne la taille en octet de la mémoire.
	 * 
	 * @return un int, la taille de la mémoire.
	 */
	public int size() {
		return memory.length;
	}

	/**
	 * Retourne l'octet se trouvant à l'indice donné
	 * 
	 * @param index
	 *            l'indice de l'octet du tableau à retourner.
	 * @throws IndexOutOfBoundsException
	 *             si l'indice est invalide
	 * @return l'octet du tableau à l'indice demandé.
	 */
	public int read(int index) {
		return Byte.toUnsignedInt(memory[Objects.checkIndex(index, memory.length)]);
	}
}
