/**
* Classe simulant l'UAL du GameBoj
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

public final class Alu {
    private Alu() {
    }

    /**
     * Enumération contenant les différents fanions renvoyés par les opérations binaires.
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z;
    }

    /**
     * Enumération contenant les différentes directions possibles pour une rotation binaire.
     */
    public enum RotDir {
        LEFT, RIGHT;
    }

    private static int packValueZNHC(int v, boolean z, boolean n, boolean h, boolean c) {
        Preconditions.checkBits16(v);
        return (v << 8) | (z ? Flag.Z.mask() : 0) | (n ? Flag.N.mask() : 0) | (h ? Flag.H.mask() : 0)
                | (c ? Flag.C.mask() : 0);
    }

    private static int booleanToBit(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * Retourne une valeur dont les bits correspondant aux différents fanions valent 1 si et seulement si l'argument
     * correspondant est vrai.
     * 
     * @param z
     *            booléen étant vrai si et seulement si le résultat de l'opération vaut zéro
     * @param n
     *            booléen étant vrai si et seulement si l'opération effectuée était une soustraction
     * @param h
     *            booléen étant vrai si et seulement si une retenue (resp. un emprunt) a été produite par l'addition
     *            (resp. la soustraction) des 4 bits de poids faible
     * @param c
     *            booléen étant vrai si et seulement si une retenue (resp. un emprunt) a été produite par l'addition
     *            (resp. la soustraction) de la totalité des 8 bits
     * @return un entier, le masque créé
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(0, z, n, h, c);
    }

    /**
     * Retourne la valeur contenue dans le paquet valeur/fanions donné.
     * 
     * @param valueFlags
     *            le paquet valeur/fanion
     * @return la plage de bits correspondant à la valeur dans le paquet
     */
    public static int unpackValue(int valueFlags) {
        return Bits.extract(valueFlags, 8, 23);
    }

    /**
     * Retourne les fanions contenus dans le paquet valeur/fanions donné.
     * 
     * @param valueFlags
     *            le paquet valeur/fanion
     * @return la plage de bits correspondant aux fanions dans le paquet
     */
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags);
    }

    /**
     * Retourne la somme de deux valeurs 8 bits compte tenu de la retenue initiale, concaténée avec les fanions.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @param c0
     *            la retenue initiale
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return la somme
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int value = Bits.clip(8, booleanToBit(c0) + l + r);
        boolean halfCarry = (Bits.clip(4, l) + Bits.clip(4, r) + booleanToBit(c0)) > 0xF;
        boolean carry = l + r + booleanToBit(c0) > 0xFF;
        return packValueZNHC(value, value == 0, false, halfCarry, carry);
    }

    /**
     * Retourne la somme de deux valeurs 8 bits sans retenue initiale, concaténée avec les fanions.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return la somme
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Retourne la somme des deux valeurs 16 bits données et les fanions correspondant à l'addition des 8 bits de poids
     * faible.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 16 bits
     * @return la somme
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int value = Bits.clip(16, l + r);
        boolean halfCarry = Bits.clip(4, l) + Bits.clip(4, r) > 0xF;
        boolean carry = Bits.clip(8, l) + Bits.clip(8, r) > 0xFF;
        return packValueZNHC(value, false, false, halfCarry, carry);
    }

    /**
     * Retourne la somme des deux valeurs 16 bits données et les fanions correspondant à l'addition des 8 bits de poids
     * forts.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 16 bits
     * @return la somme
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int value = Bits.clip(16, l + r);
        int carryPropagation = booleanToBit(Bits.test(Bits.clip(8, r) + Bits.clip(8, l), 8));
        // Additionne les 4 premiers bits des 8 bits de poids forts, ainsi
        // que l'éventuelle retenue venant des 8 bits de poids faible, et
        // teste si cette addition provoque une retenue
        boolean halfCarry = (Bits.extract(l, 8, 4) + Bits.extract(r, 8, 4) + carryPropagation) > 0xF;
        // Même chose, avec les 4 derniers bits.
        boolean carry = (Bits.extract(l, 8, 8) + Bits.extract(r, 8, 8) + carryPropagation) > 0xFF;
        return packValueZNHC(value, false, false, halfCarry, carry);
    }

    /**
     * Retourne la différence des valeurs de 8 bits données et du bit d'emprunt initial ainsi que les fanions Z1HC.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @param b0
     *            emprunt initial
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return la différence des deux entiers compte tenu de l'emprunt initial
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int value = Bits.clip(8, l - r - booleanToBit(b0));
        boolean halfCarry = Bits.clip(4, l) < (Bits.clip(4, r) + booleanToBit(b0));
        boolean carry = l < r + booleanToBit(b0);
        return packValueZNHC(value, value == 0, true, halfCarry, carry);

    }

    /**
     * Retourne la différence des valeurs de 8 bits données ainsi que les fanions Z1HC.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return la différence des deux entiers sans emprunt initial
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Ajuste la valeur 8 bits donnée en argument afin qu'elle soit au format DCB. Retourne les fanions ZN0C, le fanion
     * C est vrai si il y a eu une correction.
     * 
     * @param v
     *            la valeur à ajuster
     * @param n
     *            fanion N
     * @param h
     *            fanion H
     * @param c
     *            fanion C
     * @throws IllegalArgumentException
     *             si l'entier passé en argument n'est pas une valeur 8 bits
     * @return la valeur ajustée, avec les fanions (modifiés si nécessaire)
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        boolean fixL = h || (!n && Bits.clip(4, v) > 9);
        boolean fixH = c || (!n && v > 0x99);
        int fix = 0x60 * booleanToBit(fixH) + 0x06 * booleanToBit(fixL);
        int value = n ? v - fix : v + fix;
        value = Bits.clip(8, value);
        return packValueZNHC(value, value == 0, n, false, fixH);

    }

    /**
     * Retourne le « et » bit à bit des deux valeurs 8 bits données et les fanions Z010.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return l'entier avec les fanions
     */

    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int value = l & r;
        return packValueZNHC(value, value == 0, false, true, false);
    }

    /**
     * Retourne le « ou inclusif » bit à bit des deux valeurs 8 bits données et les fanions Z000.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la seconde valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return l'entier créé avec les fanions
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int value = l | r;
        return packValueZNHC(value, value == 0, false, false, false);
    }

    /**
     * Retourne le « ou exclusif » bit à bit des deux valeurs 8 bits données et les fanions Z000.
     * 
     * @param l
     *            la première valeur
     * @param r
     *            la première valeur
     * @throws IllegalArgumentException
     *             si les entiers l et r ne sont pas des valeurs 8 bits
     * @return l'entier créé avec les fanions
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int value = l ^ r;
        return packValueZNHC(value, value == 0, false, false, false);
    }

    /**
     * Retourne la valeur 8 bits donnée décalée à gauche d'un bit, et les fanions Z00C où le fanion C contient le bit
     * éjecté par le décalage.
     * 
     * @param v
     *            la valeur à décaler
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur décalée
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        int value = Bits.clip(8, v << 1);
        return packValueZNHC(value, value == 0, false, false, Bits.test(v, 7));
    }

    /**
     * Retourne la valeur 8 bits donnée décalée à droite d'un bit, de manière logique, et les fanions Z00C où C contient
     * le bit éjecté par le décalage.
     * 
     * @param v
     *            la valeur à décaler
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur décalée
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        int value = v >>> 1;
        return packValueZNHC(value, value == 0, false, false, Bits.test(v, 0));
    }

    /**
     * Retourne la valeur 8 bits donnée décalée à droite d'un bit, de manière arithmétique, et les fanions Z00C où C
     * contient le bit éjecté par le décalage.
     * 
     * @param v
     *            la valeur à décaler
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur décalée
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        int value = Bits.set(v >>> 1, 7, Bits.test(v, 7));
        return packValueZNHC(value, value == 0, false, false, Bits.test(v, 0));
    }

    /**
     * Retourne la rotation de la valeur 8 bits donnée, d'une distance de un bit dans la direction donnée, et les
     * fanions Z00C où C contient le bit qui est passé d'une extrémité à l'autre lors de la rotation.
     * 
     * @param d
     *            la direction de la rotation
     * @param v
     *            l'entier à modifier
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur après rotation
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        int value = Bits.rotate(8, v, d == RotDir.LEFT ? 1 : -1);
        boolean carry = Bits.test(v, d == RotDir.LEFT ? 7 : 0);
        return packValueZNHC(value, value == 0, false, false, carry);
    }

    /**
     * Retourne la rotation à travers la retenue, dans la direction donnée, de la combinaison de la valeur 8 bits et du
     * fanion de retenue donnés.
     * 
     * @param d
     *            la direction de la rotation
     * @param v
     *            l'entier à modifier
     * @param c
     *            la retenue initiale
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur après rotation
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        int value = v + (booleanToBit(c) << 8);
        value = Bits.rotate(9, value, d == RotDir.LEFT ? 1 : -1);
        boolean carry = Bits.test(value, 8);
        value = Bits.clip(8, value);
        return packValueZNHC(value, value == 0, false, false, carry);
    }

    /**
     * Retourne la valeur obtenue en échangeant les 4 bits de poids faible et de poids fort de la valeur 8 bits donnée
     * et les fanions Z000.
     * 
     * @param v
     *            l'entier à modifier
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @return la valeur après échange
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        int value = Bits.rotate(8, v, 4);
        return packValueZNHC(value, value == 0, false, false, false);
    }

    /**
     * Retourne la valeur 0 et les fanions Z010 où Z est vrai si et seulement si le bit d'index donné de la valeur 8
     * bits donnée vaut 0.
     * 
     * @param v
     *            l'entier à tester
     * @param bitIndex
     *            l'indice (démarrant à 0) du bit à tester
     * 
     * @throws IllegalArgumentException
     *             si l'entier v n'est pas une valeur 8 bits
     * @throws IndexOutOfBoundsException
     *             si l'indice est invalide
     * @return 0 ainsi que les fanions correspondants au test
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        return packValueZNHC(0, !Bits.test(v, bitIndex), false, true, false);
    }
}
