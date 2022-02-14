/**
* Représente une image Game Boy.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.gameboj.Preconditions;

public final class LcdImage {
    private final List<LcdImageLine> lines;
    private final int height;
    private final int width;

    /**
     * Construit une nouvelle image à partir d'une liste de ligne d'image, et des dimensions de l'image, ces dimensions
     * doivent correspondre à la liste passer en argument.
     * 
     * @param h
     *            la hauteur de l'image
     * @param w
     *            la largeur de l'image
     * @param l
     *            la liste de lignes d'image
     * @throws IllegalArgumentException
     *             si les dimensions ne sont pas strictement positives
     * @throws IllegalArgumentExcpetion
     *             si les dimensions passées en argument ne correspondent pas à celles de la liste
     */
    public LcdImage(int h, int w, List<LcdImageLine> l) {
        Preconditions.checkArgument(h > 0 && w > 0);
        Preconditions.checkArgument(l.size() == h);
        // Tester que tous les éléments de la liste ont une taille égale à la largeur prendrait trop de temps, donc nous
        // avons arbitrairement choisit de tester au moins le premier élément.
        Preconditions.checkArgument(l.get(0).size() == w);
        height = h;
        width = w;
        lines = Collections.unmodifiableList(new ArrayList<LcdImageLine>(l));
    }

    /**
     * Retourne la hauteur de l'image.
     * 
     * @return la hauteur de l'image
     */
    public int height() {
        return height;
    }

    /**
     * Retourne la largeur de l'image.
     * 
     * @return la largeur de l'image
     */
    public int width() {
        return width;
    }

    /**
     * Retourne, sous la forme d'un entier compris entre 0 et 3, la couleur d'un pixel d'index (x, y) donné.
     * 
     * @param x
     *            la coordonnée x du pixel
     * @param y
     *            la coordonnée y du pixel
     * @return le pixel aux coordonnées donées
     */
    public int get(int x, int y) {
        Preconditions.checkArgument(x >= 0 && x < width);
        Preconditions.checkArgument(y >= 0 && y < height);
        int lsb = lines.get(y).lsb().testBit(x) ? 1 : 0;
        int msb = lines.get(y).msb().testBit(x) ? 1 : 0;
        return (msb << 1) | lsb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
        return (that instanceof LcdImage) && lines.equals(((LcdImage) that).lines);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return lines.hashCode();
    }

    /**
     * Représente un bâtisseur d'image.
     * 
     */
    public final static class Builder {
        private final List<LcdImageLine> lines;
        private int height;
        private int width;

        /**
         * Construit un nouveau bâtisseur d'image avec les dimensions données dont tous les pixels valent 0.
         * 
         * @param h
         *            la hauteur de l'image à construire.
         * @param w
         *            la largeur de l'image à construire.
         * @throws IllegalArgumentException
         *             si les dimensions ne sont pas strictement positives
         */
        public Builder(int h, int w) {
            Preconditions.checkArgument(h > 0 && w > 0);
            height = h;
            width = w;
            lines = new ArrayList<LcdImageLine>(h);
            for (int i = 0; i < h; i++) {
                lines.add(new LcdImageLine.Builder(w).build());
            }
        }

        /**
         * Remplace la ligne d'indice donné par celle passée en argument.
         * 
         * @param index
         *            l'indice de la ligne à remplacer.
         * @param line
         *            la nouvelle ligne.
         * @return le bâtisseur actuel.
         */
        public Builder setLine(int index, LcdImageLine line) {
            lines.set(index, line);
            return this;
        }

        /**
         * Construit et retourne l'image.
         * 
         * @return la nouvelle image.
         */
        public LcdImage build() {
            return new LcdImage(height, width, lines);
        }
    }
}
