/**
* Description de la classe
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

public class LcdImageLineTest {
    private static final BitVector VECTOR_TEST64 = new BitVector.Builder(64)
            .setByte(0, 0b1000_1010).setByte(1, 0b1111_0110)
            .setByte(2, 0b1010_0000).setByte(3, 0b1100_0011)
            .setByte(4, 0b1000_1010).setByte(5, 0b1111_0110)
            .setByte(6, 0b1010_0000).setByte(7, 0b1100_0011).build();
    private static final BitVector ZERO_VECTOR64 = new BitVector(64);
    private static final BitVector ZERO_VECTOR32 = new BitVector(32);
    private static final BitVector ONE_VECTOR64 = new BitVector(64, true);
    private static final BitVector ONE_VECTOR32 = new BitVector(32, true);

    private static final BitVector RANDOM_VECTOR1 = new BitVector.Builder(64)
            .setByte(3, 0xFF).setByte(4, 76).setByte(6, 187).setByte(7, 187)
            .build();
    private static final BitVector RANDOM_VECTOR2 = new BitVector.Builder(64)
            .setByte(3, 76).setByte(5, 123).setByte(7, 95).build();
    private static final BitVector RANDOM_VECTOR3 = new BitVector.Builder(64)
            .setByte(2, 0xFF).setByte(5, 76).setByte(6, 54).setByte(7, 205)
            .build();
    private static final BitVector RANDOM_VECTOR4 = new BitVector.Builder(64)
            .setByte(3, 0xAF).setByte(4, 154).setByte(6, 234).setByte(7, 65)
            .build();
    private static final BitVector RANDOM_VECTOR5 = new BitVector.Builder(64)
            .setByte(0, 0xFF).setByte(1, 13).setByte(4, 193).setByte(5, 239)
            .setByte(7, 95).build();
    private static final BitVector RANDOM_VECTOR6 = new BitVector.Builder(64)
            .setByte(1, 0x4A).setByte(3, 189).setByte(6, 212).setByte(7, 205)
            .build();

    private static final LcdImageLine RANDOM_LINE1 = new LcdImageLine(
            RANDOM_VECTOR1, RANDOM_VECTOR2, RANDOM_VECTOR3);
    private static final LcdImageLine RANDOM_LINE2 = new LcdImageLine(
            RANDOM_VECTOR4, RANDOM_VECTOR5, RANDOM_VECTOR6);

    private static final BitVector V1 = new BitVector(96, true);
    private static final BitVector V2 = new BitVector(96, true);
    private static final BitVector V3 = new BitVector(96, true);
    private static final BitVector V4 = new BitVector(96, true);
    private static final BitVector V5 = new BitVector(96, true);
    private static final BitVector V6 = new BitVector(96, true);
    private static final LcdImageLine L1 = new LcdImageLine(V1, V2, V3);
    private static final LcdImageLine L2 = new LcdImageLine(V4, V5, V6);

    @Test
    void equalityIsStructuralAndCommutative() {
        assertEquals(L1, L2);
        assertEquals(L2, L1);
    }

    @Test
    void hashCodesCompatiblesWithEquals() {
        assertEquals(L1.hashCode(), L2.hashCode());
    }

    @Test
    void sizeWorks() {
        LcdImageLine l1 = new LcdImageLine(new BitVector(128, true),
                new BitVector(128, false), new BitVector(128, false));
        LcdImageLine l2 = new LcdImageLine(new BitVector(96, true),
                new BitVector(96, false), new BitVector(96, false));
        assertEquals(128, l1.size());
        assertEquals(96, l2.size());
    }

    @Test
    void gettersWork() {
        // La référence doit être la même
        assertEquals(true, V1 == L1.msb());
        assertEquals(true, V2 == L1.lsb());
        assertEquals(true, V3 == L1.opacity());
        assertEquals(true, V4 == L2.msb());
        assertEquals(true, V5 == L2.lsb());
        assertEquals(true, V6 == L2.opacity());
    }

    @Test
    void randomJoin() {
        // System.out.println(RANDOM_VECTOR1);
        // System.out.println(RANDOM_VECTOR2);
        // System.out.println(RANDOM_VECTOR3);
        // System.out.println("****");
        // System.out.println(RANDOM_VECTOR4);
        // System.out.println(RANDOM_VECTOR5);
        // System.out.println(RANDOM_VECTOR6);
        BitVector expected1 = new BitVector.Builder(64).setByte(3, 0xAF)
                .setByte(4, 154).setByte(6, 234).setByte(7, 65).build();
        BitVector expected2 = new BitVector.Builder(64).setByte(4, 193)
                .setByte(5, 239).setByte(7, 95).build();
        BitVector expected3 = new BitVector.Builder(64).setByte(2, 0xFF)
                .setByte(3, 189).setByte(6, 212).setByte(7, 205).build();
        assertEquals(new LcdImageLine(expected1, expected2, expected3),
                RANDOM_LINE1.join(RANDOM_LINE2, 24));
    }

    @Test
    void joinReplaceTheWholeLinesWithIndex0() {
        assertEquals(RANDOM_LINE2, RANDOM_LINE1.join(RANDOM_LINE2, 0));
    }

    @Test
    void joinTheSameLineWorks() {
        assertEquals(RANDOM_LINE1, RANDOM_LINE1.join(RANDOM_LINE1, 3));
        assertEquals(RANDOM_LINE1, RANDOM_LINE1.join(RANDOM_LINE1, 25));
        assertEquals(RANDOM_LINE1, RANDOM_LINE1.join(RANDOM_LINE1, 32));
        assertEquals(RANDOM_LINE1, RANDOM_LINE1.join(RANDOM_LINE1, 56));
    }

    @Test
    void joinThrowsExceptionWhenOutOfBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            RANDOM_LINE1.join(RANDOM_LINE1, -1);
        });
        assertThrows(IndexOutOfBoundsException.class, () -> {
            RANDOM_LINE1.join(RANDOM_LINE1, 64);
        });
    }

    @Test
    void builderWorks() {
        LcdImageLine.Builder builder = new LcdImageLine.Builder(64)
                .setBytes(3, 0xFF, 76).setBytes(4, 76, 0).setBytes(5, 0, 123)
                .setBytes(6, 187, 0).setBytes(7, 187, 95);
        LcdImageLine test = builder.build();
        assertEquals(new LcdImageLine(RANDOM_VECTOR1, RANDOM_VECTOR2,
                RANDOM_VECTOR1.or(RANDOM_VECTOR2)), test);
    }

    @Test
    void MapColorsWhiteToBlack() {
        LcdImageLine white = new LcdImageLine(ZERO_VECTOR64, ZERO_VECTOR64,
                VECTOR_TEST64);
        LcdImageLine black = new LcdImageLine(ONE_VECTOR64, ONE_VECTOR64,
                VECTOR_TEST64);
        assertEquals(black, white.mapColors(0b11_11_11_11));
    }

    @Test
    void MapColorsRandomPalette() {
        BitVector msb = new BitVector.Builder(32).setByte(0, 0b0001_1000)
                .setByte(1, 0b0001_1000).setByte(2, 0b1110_0111)
                .setByte(3, 0b1110_0111).build();
        BitVector lsb = new BitVector.Builder(32).setByte(0, 0)
                .setByte(1, 0b11111111).setByte(2, 0b0001_1000)
                .setByte(3, 0b1110_0111).build();
        assertEquals(new LcdImageLine(msb.not(), lsb.not(), ONE_VECTOR32),
                new LcdImageLine(msb, lsb, ONE_VECTOR32)
                        .mapColors(0b00_01_10_11));
    }

    @Test
    void MapColorsDoesNothingWith3210Palette() {
        LcdImageLine randomLine = new LcdImageLine(RANDOM_VECTOR1,
                RANDOM_VECTOR2, RANDOM_VECTOR3);
        assertEquals(randomLine, randomLine.mapColors(0b11_10_01_00));
    }

    @Test
    void MapColorsRandomToUnicolor() {
        LcdImageLine white = new LcdImageLine(ZERO_VECTOR64, ZERO_VECTOR64,
                RANDOM_VECTOR3);
        LcdImageLine lightGray = new LcdImageLine(ZERO_VECTOR64, ONE_VECTOR64,
                RANDOM_VECTOR3);
        LcdImageLine darkGray = new LcdImageLine(ONE_VECTOR64, ZERO_VECTOR64,
                RANDOM_VECTOR6);
        LcdImageLine black = new LcdImageLine(ONE_VECTOR64, ONE_VECTOR64,
                RANDOM_VECTOR6);

        assertEquals(white, RANDOM_LINE1.mapColors(0));
        assertEquals(lightGray, RANDOM_LINE1.mapColors(0b01_01_01_01));
        assertEquals(darkGray, RANDOM_LINE2.mapColors(0b10_10_10_10));
        assertEquals(black, RANDOM_LINE2.mapColors(0b11_11_11_11));
    }

    @Test
    void bellowWorksWithNullOrFullOpacity() {
        assertEquals(RANDOM_LINE1,
                RANDOM_LINE1.below(RANDOM_LINE2, ZERO_VECTOR64));
        assertEquals(
                new LcdImageLine(RANDOM_LINE1.msb(), RANDOM_LINE1.lsb(),
                        ONE_VECTOR64),
                RANDOM_LINE2.below(RANDOM_LINE1, ONE_VECTOR64));
    }

    @Test
    void bellowWith2TransparentLinesWorks() {
        LcdImageLine down = new LcdImageLine(RANDOM_LINE1.msb(),
                RANDOM_LINE1.lsb(), ZERO_VECTOR64);
        LcdImageLine up = new LcdImageLine(RANDOM_LINE2.msb(),
                RANDOM_LINE2.lsb(), ZERO_VECTOR64);
        assertEquals(down, down.below(up));
    }

    @Test
    void bellowWithAndWithoutArgumentWorks() {
        BitVector opacity = new BitVector.Builder(64).setByte(0, 0b1111_1111)
                .setByte(2, 0b1111_1111).setByte(4, 0b1111_1111)
                .setByte(6, 0b1111_1111).build();
        LcdImageLine up = new LcdImageLine(RANDOM_LINE2.msb(),
                RANDOM_LINE2.lsb(), opacity);
        LcdImageLine temp = new LcdImageLine.Builder(64).setBytes(0, 0, 0xFF)
                .setBytes(1, 0, 0).setBytes(2, 0, 0).setBytes(3, 0XFF, 76)
                .setBytes(4, 154, 193).setBytes(5, 0, 123).setBytes(6, 234, 0)
                .setBytes(7, 187, 95).build();
        LcdImageLine expected = new LcdImageLine(temp.msb(), temp.lsb(),
                RANDOM_LINE1.opacity().or(up.opacity()));
        assertEquals(expected, RANDOM_LINE1.below(up));
        assertEquals(expected, RANDOM_LINE1.below(up, opacity));
    }

    @Test
    void equalsAndHashcodeWorks() {
        assertEquals(
                new LcdImage(2, 64, Arrays.asList(RANDOM_LINE1, RANDOM_LINE2)),
                new LcdImage(2, 64, Arrays.asList(RANDOM_LINE1, RANDOM_LINE2)));

        assertEquals(
                new LcdImage(2, 64, Arrays.asList(RANDOM_LINE1, RANDOM_LINE2))
                        .hashCode(),
                new LcdImage(2, 64, Arrays.asList(RANDOM_LINE1, RANDOM_LINE2))
                        .hashCode());
    }
}