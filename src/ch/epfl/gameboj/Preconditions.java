package ch.epfl.gameboj;

/**
 * Contient des méthodes utilitaires permettant de faciliter l'écriture des préconditions.
 * 
 * @author Vignoud Julien (282142)
 * @author Benhaim Julien (284558)
 *
 */
public interface Preconditions {
    /**
     * Lève une exception si le paramètre est faux.
     * 
     * @param b
     *            un booléen
     * @throws IllegalArgumentException
     *             si b est faux
     */
    static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Retourne l'argument si celui-ci contient une valeur de 8 bits, sinon lève une exception.
     * 
     * @param v
     *            un entier
     * @throws IllegalArgumentException
     *             si v n'est pas compris entre 0 et 255
     * @return l'entier passé en paramètre si il est compris dans l'intervalle
     */
    static int checkBits8(int v) {
        checkArgument(v >= 0 && v <= 255);
        return v;
    }

    /**
     * Retourne l'argument si celui-ci contient une valeur de 16 bits, sinon lève une exception.
     * 
     * @param v
     *            un entier
     * @throws IllegalArgumentException
     *             si v n'est pas compris entre 0 et 65535
     * @return l'entier passé en paramètre si il est compris dans l'intervalle
     */
    static int checkBits16(int v) {
        checkArgument(v >= 0 && v <= 65535);
        return v;
    }

}
