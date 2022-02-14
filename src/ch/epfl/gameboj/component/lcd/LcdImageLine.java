/**
* Représente une ligne d'image Game Boy.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {
    private static final int DEFAULT_PALETTE = 0b11_10_01_00;
    private final BitVector msb;
    private final BitVector lsb;
    private final BitVector opacity;

    /**
     * Crée une nouvelle ligne d'image à partir des trois vecteurs de bits (de même longueur) passés en argument.
     * 
     * @param m
     *            le vecteur correspondant aux bits de poids fort
     * @param l
     *            le vecteur correspondant aux bits de poids faible
     * @param o
     *            le vecteur correspondant à l'opacité
     * @throws IllegalArgumentException
     *             si les trois vecteurs de bits ne sont pas de même longeur.
     */
    public LcdImageLine(BitVector m, BitVector l, BitVector o) {
        Preconditions.checkArgument(m.size() == l.size() && l.size() == o.size());
        msb = m;
        lsb = l;
        opacity = o;
    }

    /**
     * Retourne la longueur, en pixels, de la ligne.
     * 
     * @return la longueur de la ligne
     */
    public int size() {
        return msb.size();
    }

    /**
     * Retourne le vecteur de bits correspondant aux bits de poids fort.
     * 
     * @return le vecteur de bits correspondant aux bits de poids fort.
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Retourne le vecteur de bits correspondant aux bits de poids faible.
     * 
     * @return le vecteur de bits correspondant aux bits de poids faible.
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Retourne le vecteur de bits correspondant à l'opacité.
     * 
     * @return le vecteur de bits correspondant à l'opacité.
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Décale la ligne d'un nombre de pixels donné, en complétant avec des zéros. A gauche si la distance est positive,
     * à droite sinon.
     * 
     * @param distance
     *            la distance de décalage
     * @return une nouvelle ligne décalée
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }

    /**
     * Extrait de l'extension infinie par enroulement, à partir d'un pixel donné, une ligne de longueur donnée.
     * 
     * @param startIndex
     *            le pixel de départ
     * @param size
     *            la taille de la ligne (qui doit être un multiple de 32).
     * @return une nouvelle ligne à partir de l'extension par enroulement
     */
    public LcdImageLine extractWrapped(int startIndex, int size) {
        return new LcdImageLine(msb.extractWrapped(startIndex, size), lsb.extractWrapped(startIndex, size),
                opacity.extractWrapped(startIndex, size));
    }

    /**
     * Transforme les couleurs de la ligne en fonction d'une palette, donnée sous la forme d'un octet encodé. Le i-ème
     * groupe de 2 bits associe sa valeur à la couleur i. Par exemple, la palette 0b00_01_10_11 remplace respectivement
     * les couleurs 11, 10, 01 et 00 par 00, 01, 10 et 11.
     * 
     * @param palette
     *            la palette qui associe chaque couleur à une nouvelle couleur.
     * @return une nouvelle ligne avec les couleurs transformées.
     */
    public LcdImageLine mapColors(int palette) {
        if (palette == DEFAULT_PALETTE) {
            return new LcdImageLine(msb, lsb, opacity);
        }
        BitVector newLsb = new BitVector(size(), false);
        BitVector newMsb = new BitVector(size(), false);
        for (int i = 0b00; i <= 0b11; i++) {

            // Crée un masque qui permet d'isoler uniquement les bits susceptibles d'être affectés par le changement de
            // couleur actuel
            BitVector operande1 = Bits.test(i, 0) ? lsb : lsb.not();
            BitVector operande2 = Bits.test(i, 1) ? msb : msb.not();
            BitVector mask = operande1.and(operande2);

            // Modifie le vecteur seulement si un bit de la nouvelle couleur est à 1
            if (Bits.test(palette, 2 * i + 1)) {
                newMsb = newMsb.or(mask);
            }
            if (Bits.test(palette, 2 * i)) {
                newLsb = newLsb.or(mask);
            }
        }
        return new LcdImageLine(newMsb, newLsb, opacity);
    }

    /**
     * Compose la ligne avec une seconde de même longueur, placée au-dessus d'elle, en utilisant l'opacité de la ligne
     * supérieure pour effectuer la composition.
     * 
     * @param upperLine
     *            la ligne supérieure
     * @return une nouvelle ligne à partir de la composition
     */
    public LcdImageLine below(LcdImageLine upperLine) {
        return below(upperLine, upperLine.opacity);
    }

    /**
     * Compose la ligne avec une seconde de même longueur, placée au-dessus d'elle, en utilisant un vecteur d'opacité
     * passé en argument pour effectuer la composition.
     * 
     * @param upperLine
     *            la ligne supérieure
     * @param effectiveOpacity
     *            l'opacité utilisée pour la composition
     * @return une nouvelle ligne à partir de la composition
     */
    public LcdImageLine below(LcdImageLine upperLine, BitVector effectiveOpacity) {
        BitVector newMsb = upperLine.msb.and(effectiveOpacity).or(msb.and(effectiveOpacity.not()));
        BitVector newLsb = upperLine.lsb.and(effectiveOpacity).or(lsb.and(effectiveOpacity.not()));
        BitVector newOpacity = effectiveOpacity.or(opacity);
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }

    /**
     * Effectue la jointure de la ligne avec une autre de même longueur, à partir d'un pixel d'indice donné.
     * 
     * @param that
     *            la ligne à joindre à la première
     * @param index
     *            le pixel d'indice de début
     * @throws IndexOutOfBoundsException
     *             si l'indice est invalide
     * @return une nouvelle ligne à partir de la jointure
     */
    public LcdImageLine join(LcdImageLine that, int index) {
        Objects.checkIndex(index, size());
        // Crée un masque qui permet d'isoler la partie de this que l'on souhaite garder et dont la négation permet
        // de garder celle de that
        BitVector mask = new BitVector(size(), true).shift(index - size());
        BitVector newMsb = msb.and(mask).or(mask.not().and(that.msb));
        BitVector newLsb = lsb.and(mask).or(mask.not().and(that.lsb));
        BitVector newOpacity = opacity.and(mask).or(mask.not().and(that.opacity));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return (that instanceof LcdImageLine) && msb.equals(((LcdImageLine) that).msb())
                && lsb.equals(((LcdImageLine) that).lsb()) && opacity.equals(((LcdImageLine) that).opacity());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    /**
     * Représente un bâtisseur de ligne d'image
     *
     */
    public final static class Builder {
        private final BitVector.Builder lsbBuilder;
        private final BitVector.Builder msbBuilder;

        /**
         * Construit un bâtisseur de ligne dont les bits de chaque vecteur valent 0.
         * 
         * @param size
         *            la taille de la ligne à construire
         */
        public Builder(int size) {
            lsbBuilder = new BitVector.Builder(size);
            msbBuilder = new BitVector.Builder(size);
        }

        /**
         * Définit la valeur des octets de poids fort et de poids faible de la ligne, à un index donné.
         * 
         * @param index
         *            l'indice (en octet) de l'octet à modifier
         * @param valueLsb
         *            la valeur de l'octet à définir du vecteur des bits de poids faible
         * @param valueMsb
         *            la valeur de l'octet à définir du vecteur des bits de poids fort
         * @return le bâtisseur actuel
         */
        public Builder setBytes(int index, int valueMsb, int valueLsb) {
            lsbBuilder.setByte(index, valueLsb);
            msbBuilder.setByte(index, valueMsb);
            return this;
        }

        /**
         * Construit la ligne avec les octets définis jusqu'à présent et rend le bâtisseur inutilisable.
         * 
         * @return une nouvelle ligne correspondant à celle construite
         */
        public LcdImageLine build() {
            BitVector lsb = lsbBuilder.build();
            BitVector msb = msbBuilder.build();
            BitVector opacity = lsb.or(msb);
            return new LcdImageLine(msb, lsb, opacity);
        }
    }

}
