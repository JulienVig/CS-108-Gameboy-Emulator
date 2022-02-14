/**
* Représente un vecteur de bits dont la taille est un multiple de 32.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.bits;

import static java.lang.Math.floorMod;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BinaryOperator;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    private static final int CHUNK_SIZE = Integer.SIZE;
    private static final int BYTE_PER_CHUNK = CHUNK_SIZE / Byte.SIZE;
    private final int[] vectorChunks;

    private enum ExtractType {
        ZERO_EXTENDED, WRAPPED
    }

    /**
     * Crée un nouveau vecteur de bits en initialisant tous ses bits à la valeur donnée.
     * 
     * @param size
     *            la taille en bits du vecteur (multiple de 32)
     * @param initialValue
     *            la valeur initiale de tous les bits
     * @throws IllegalArgumentException
     *             si la taille n'est pas un multiple de 32 ou strictement positive
     */
    public BitVector(int size, boolean initialValue) {
        this(initializeArray(size, initialValue));
    }

    /**
     * Crée un nouveau vecteur de bits en initialisant tous ses bits à 0.
     * 
     * @param size
     *            la taille en bits du vecteur (multiple de 32)
     * @throws IllegalArgumentException
     *             si la taille n'est pas un multiple de 32 ou strictement positive
     */
    public BitVector(int size) {
        this(size, false);
    }

    private BitVector(int[] data) {
        Preconditions.checkArgument(data.length > 0);
        vectorChunks = data;
    }

    private static int[] initializeArray(int size, boolean initialValue) {
        Preconditions.checkArgument(size > 0 && size % CHUNK_SIZE == 0);
        int[] tab = new int[size / CHUNK_SIZE];
        if (initialValue) {
            // Si le vecteur doit être initialement rempli de 1, on met -1(0b1111_11......1_1111) dans tous les morceaux
            // du vecteur
            Arrays.fill(tab, -1);
        }
        return tab;
    }

    /**
     * Retourne la taille du vecteur.
     * 
     * @return la taille du vecteur en bits
     */
    public int size() {
        return vectorChunks.length * CHUNK_SIZE;
    }

    /**
     * Retourne la valeur du bit d'indice donné du vecteur.
     * 
     * @param index
     *            l'indice du bit
     * @return true si le bit d'indice donné vaut 1
     */
    public boolean testBit(int index) {
        return Bits.test(vectorChunks[index / CHUNK_SIZE], index % CHUNK_SIZE);
    }

    /**
     * Calcule le complément du vecteur de bits et retourne le résultat sous forme d'un nouveau vecteur.
     * 
     * @return un nouveau vecteur correspondant au complément bit à bit
     */
    public BitVector not() {
        int[] tab = new int[vectorChunks.length];
        for (int i = 0; i < vectorChunks.length; i++) {
            tab[i] = ~vectorChunks[i];
        }
        return new BitVector(tab);
    }

    /**
     * Calcule la conjonction du vecteur de bits avec le vecteur passé en argument et retourne le résultat sous forme
     * d'un nouveau vecteur.
     * 
     * @param that
     *            un vecteur de bits
     * @throws IllegalArgumentException
     *             si les deux vecteurs ne sont pas de la même taille
     * @return un nouveau vecteur correspondant à la conjonction bit à bit
     */
    public BitVector and(BitVector that) {
        return applyOperation(that, (x, y) -> x & y);
    }

    /**
     * Calcule la disjonction du vecteur de bits avec le vecteur passé en argument et retourne le résultat sous forme
     * d'un nouveau vecteur.
     * 
     * @param that
     *            un vecteur de bits
     * @throws IllegalArgumentException
     *             si les deux vecteurs ne sont pas de la même taille
     * @return un nouveau vecteur correspondant à la disjonction bit à bit
     */
    public BitVector or(BitVector that) {
        return applyOperation(that, (x, y) -> x | y);
    }

    private BitVector applyOperation(BitVector that, BinaryOperator<Integer> op) {
        Preconditions.checkArgument(size() == that.size());
        int[] tab = new int[vectorChunks.length];
        for (int i = 0; i < vectorChunks.length; i++) {
            tab[i] = op.apply(that.vectorChunks[i], vectorChunks[i]);
        }
        return new BitVector(tab);
    }

    /**
     * Extrait un vecteur de taille donnée de l'extension par 0 du vecteur.
     * 
     * @param startIndex
     *            l'indice à partir duquel extraire les bits
     * @param size
     *            le nombre de bits à extraire
     * @return un nouveau vecteur résultant de l'extraction par 0
     */
    public BitVector extractZeroExtended(int startIndex, int size) {
        return extract(startIndex, size, ExtractType.ZERO_EXTENDED);
    }

    /**
     * Extrait un vecteur de taille donnée de l'extension par enroulement du vecteur.
     * 
     * @param startIndex
     *            l'indice à partir duquel extraire les bits
     * @param size
     *            le nombre de bits à extraire
     * @return un nouveau vecteur résultant de l'extraction par enroulement.
     */
    public BitVector extractWrapped(int startIndex, int size) {
        return extract(startIndex, size, ExtractType.WRAPPED);
    }

    private BitVector extract(int bitStartIndex, int bitSize, ExtractType type) {
        Preconditions.checkArgument(bitSize > 0 && bitSize % CHUNK_SIZE == 0);
        int[] tab = new int[bitSize / CHUNK_SIZE];
        for (int i = 0; i < tab.length; i++) {
            tab[i] = getChunck(i * CHUNK_SIZE + bitStartIndex, type);
        }
        return new BitVector(tab);
    }

    private int getChunck(int bitIndex, ExtractType type) {
        int bitIndexInChunk = floorMod(bitIndex, size()) % CHUNK_SIZE;
        int chunkIndex = floorMod(bitIndex, size()) / CHUNK_SIZE;
        int endIndexOfChunk = bitIndex + CHUNK_SIZE;
        int bitIndexToChunkSize = CHUNK_SIZE - bitIndexInChunk;
        // Cas multiple de 32, on retourne simplement la bonne case du tableau
        if (floorMod(bitIndex, CHUNK_SIZE) == 0) {
            if (bitIndex < 0 || bitIndex >= size()) {
                return type == ExtractType.ZERO_EXTENDED ? 0 : vectorChunks[chunkIndex];
            }
            return vectorChunks[bitIndex / CHUNK_SIZE];
        }

        // Cas particuliers de l'extention par zéro
        if (type == ExtractType.ZERO_EXTENDED) {
            // Le chunk à calculer est totalement en dehors du vecteur
            if (endIndexOfChunk < 0 || bitIndex >= size()) {
                return 0;
            }
            // Le début du chunk à calculer est dans le vecteur et la fin en dehors
            else if (bitIndex < size() && endIndexOfChunk >= size()) {
                return Bits.extract(vectorChunks[chunkIndex], bitIndexInChunk, bitIndexToChunkSize);

            }
            // Le début du chunk est en dehors du vecteur et la fin dedans
            else if (bitIndex < 0 && endIndexOfChunk >= 0) {
                return Bits.clip(endIndexOfChunk, vectorChunks[0]) << -bitIndex;
            }
        }
        // Extraction par enroulement ainsi que l'extraction par zéro lorsque le chunk est inclu dans le vecteur
        int firstVectorPart = Bits.extract(vectorChunks[chunkIndex], bitIndexInChunk, bitIndexToChunkSize);
        int endVectorPart = Bits.clip(bitIndexInChunk,
                vectorChunks[(chunkIndex + 1) % (size() / CHUNK_SIZE)]) << bitIndexToChunkSize;

        return firstVectorPart + endVectorPart;
    }

    /**
     * Décale le vecteur d'une distance donnée en argument, à gauche si la distance est positive, à droite sinon.
     * 
     * @param distance
     *            la distance de décalage
     * @return un nouveau vecteur décalé
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return (that instanceof BitVector) && Arrays.equals(this.vectorChunks, ((BitVector) that).vectorChunks);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(vectorChunks);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(size());
        for (int i = 0; i < size(); i++) {
            if (i != 0 && i % Byte.SIZE == 0) {
                builder.append("_");
            }
            builder.append(testBit(i) ? "1" : "0");
        }
        return builder.reverse().toString();
    }

    /**
     * Représente un bâtisseur de vecteur de bits.
     *
     */
    public static final class Builder {
        private int[] bitsBuilder;

        /**
         * Construit un bâtisseur de vecteur de bits (valant tous 0) d'une taille donnée en argument.
         * 
         * @param size
         *            la taille du vecteur de bit qui doit être un multiple de 32 différent de 0
         */
        public Builder(int size) {
            bitsBuilder = initializeArray(size, false);
        }

        /**
         * Définit la valeur d'un octet à partir de l'indice passé en argument.
         * 
         * @param index
         *            l'indice (en octet) de l'octet à modifier
         * @param value
         *            la valeur à attribuer à l'octet
         * @throws IllegalArgumentException
         *             si la valeur donnée n'est pas une valeur 8 bits.
         * @throws IndexOutOfBoundsException
         *             si l'index est invalide
         * @throws IllegalStateException
         *             si la méthode build de ce constructeur a déjà été appelée
         */
        public Builder setByte(int index, int value) {
            checkState();
            Preconditions.checkBits8(value);
            Objects.checkIndex(index, bitsBuilder.length * BYTE_PER_CHUNK);
            int chunkIndex = index / BYTE_PER_CHUNK;
            int bitIndex = (index % BYTE_PER_CHUNK) * Byte.SIZE;
            // Cette opération sur les bits permet de changer la valeur de l'octet désiré.
            bitsBuilder[chunkIndex] = bitsBuilder[chunkIndex] & ~(0b1111_1111 << bitIndex) | (value << bitIndex);
            return this;
        }

        /**
         * Construit le vecteur de bits et rend le bâtisseur invalide.
         * 
         * @throws IllegalStateException
         *             si la méthode build de ce constructeur a déjà été appelée
         * @return le vecteur de bits
         */
        public BitVector build() {
            checkState();
            BitVector built = new BitVector(bitsBuilder);
            bitsBuilder = null;
            return built;
        }

        private void checkState() {
            if (bitsBuilder == null) {
                throw new IllegalStateException();
            }
        }
    }

}
