/**
* Simule la mémoire vive de la console.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class Ram {
    private final byte[] memory;

    /**
     * Construit un nouvel objet de type Ram en créant un tableau de la taille passée en argument.
     * 
     * @param size
     *            taille de la mémoire en octet
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        memory = new byte[size];
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
     * Retourne l'octet se trouvant à l'indice donné.
     * 
     * @param index
     *            l'indice de l'octet du tableau à retourner
     * @throws IndexOutOfBoundsException
     *             si l'indice est invalide
     * @return l'octet du tableau à l'indice demandé
     */
    public int read(int index) {
        return Byte.toUnsignedInt(memory[Objects.checkIndex(index, memory.length)]);
    }

    /**
     * Modifie le contenu de la mémoire à l'indice donné pour qu'il soit égal à la valeur donnée.
     * 
     * @param index
     *            l'indice de l'octet de la mémoire à modifier
     * @param value
     *            la valeur à donner à l'octet modifié
     * @throws IndexOutOfBoundsException
     *             si l'indice est invalide
     * @throws IllegalArgumentException
     *             si la valeur n'est pas une valeur 8 bits
     */
    public void write(int index, int value) {
        memory[Objects.checkIndex(index, memory.length)] = (byte) Preconditions.checkBits8(value);
    }

    /**
     * Sauvegarde le contenu de la mémoire vive dans le fichier spécifié
     * 
     * @param file
     *            le fichier dans lequel sauvegarder la mémoire
     */
    public void saveRam(File file) {
        try (OutputStream s = new FileOutputStream(file, false)) {
            s.write(memory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge le contenu du fichier spécifié dans la mémoire vive.
     * 
     * @param file
     *            le fichier depuis lequel charger la mémoire
     */
    public void loadRam(File file) {
        try (InputStream s = new FileInputStream(file)) {
            byte[] content = s.readAllBytes();
            for (int i = 0; i < content.length; i++) {
                if (content[i] != -1) {
                    write(i, Byte.toUnsignedInt(content[i]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
