/**
* Description de la classe
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.bits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class BitVectorTest {
    public static final BitVector VECTOR_TEST = new BitVector.Builder(96)
            .setByte(11, 0xFF).setByte(0, 0xFF).setByte(5, 0xE4)
            .setByte(9, 0xE4).build();
    private static final BitVector VECTOR_TEST32 = new BitVector.Builder(32)
            .setByte(0, 0b1000_1010).setByte(1, 0b1111_0110)
            .setByte(2, 0b1010_0000).setByte(3, 0b1100_0011).build();
    private static final BitVector ZERO_VECTOR64 = new BitVector.Builder(64)
            .build();
    private static final BitVector ZERO_VECTOR32 = new BitVector.Builder(32)
            .build();
    private static final BitVector ONE_VECTOR64 = new BitVector.Builder(64)
            .setByte(0, 0b1111_1111).setByte(1, 0b1111_1111)
            .setByte(2, 0b1111_1111).setByte(3, 0b1111_1111)
            .setByte(4, 0b1111_1111).setByte(5, 0b1111_1111)
            .setByte(6, 0b1111_1111).setByte(6, 0b1111_1111).build();
    private static final BitVector ONE_VECTOR32 = new BitVector.Builder(32)
            .setByte(0, 0b1111_1111).setByte(1, 0b1111_1111)
            .setByte(2, 0b1111_1111).setByte(3, 0b1111_1111).build();

    @Test
    void test2() {
        BitVector v = new BitVector(32, true);
        for (int i = 0; i < v.size(); i++) {
            assertEquals(true, v.testBit(i));
        }
    }

    @Test
    void randomTest() {
        BitVector.Builder builder = new BitVector.Builder(64);
        builder.setByte(0, 0b1001_0010).setByte(1, 0b1101_1010)
                .setByte(2, 0b0100_1010).setByte(3, 0b0010_1110)
                .setByte(4, 0b1001_0010).setByte(5, 0b1101_1010)
                .setByte(6, 0b0100_1010).setByte(7, 0b0010_1110);
        ;
        BitVector v1 = builder.build();
        assertThrows(IllegalStateException.class, () -> {
            builder.build();
        });
    }

    @Test
    void doubleNotDoesNothing() {
        BitVector modified = VECTOR_TEST32;
        assertEquals(VECTOR_TEST32, modified.not().not());
    }

    @Test
    void notWorks() {
        BitVector.Builder sb = new BitVector.Builder(32);
        sb.setByte(0, 0b0111_0101).setByte(1, 0b0000_1001)
                .setByte(2, 0b0101_1111).setByte(3, 0b0011_1100);
        BitVector modified = sb.build();
        assertEquals(modified, VECTOR_TEST32.not());
    }

    @Test
    void andWorks() {
        assertEquals(ONE_VECTOR64, ONE_VECTOR64.and(ONE_VECTOR64));
        assertEquals(ZERO_VECTOR64, ONE_VECTOR64.and(ZERO_VECTOR64));
        assertEquals(ZERO_VECTOR64, ZERO_VECTOR64.and(ONE_VECTOR64));
        assertEquals(ZERO_VECTOR64, ZERO_VECTOR64.and(ZERO_VECTOR64));
        assertEquals(ZERO_VECTOR32, VECTOR_TEST32.and(VECTOR_TEST32.not()));

        BitVector vector = new BitVector.Builder(32).setByte(0, 0b1000_1010)
                .setByte(1, 0b1111_0110).setByte(2, 0b1010_0000)
                .setByte(3, 0b1100_0011).build();
        BitVector mask = new BitVector.Builder(32).setByte(0, 0b11111111)
                .setByte(1, 0b11111111).setByte(2, 0b1100_1010)
                .setByte(3, 0b11111111).build();
        BitVector expected = new BitVector.Builder(32).setByte(0, 0b1000_1010)
                .setByte(1, 0b1111_0110).setByte(2, 0b1000_0000)
                .setByte(3, 0b1100_0011).build();
        assertEquals(expected, vector.and(mask));

    }

    @Test
    void orWorks() {
        assertEquals(ONE_VECTOR64, ONE_VECTOR64.or(ONE_VECTOR64));
        assertEquals(ONE_VECTOR64, ONE_VECTOR64.or(ZERO_VECTOR64));
        assertEquals(ONE_VECTOR64, ZERO_VECTOR64.or(ONE_VECTOR64));
        assertEquals(ZERO_VECTOR64, ZERO_VECTOR64.or(ZERO_VECTOR64));
        assertEquals(ONE_VECTOR32, VECTOR_TEST32.or(VECTOR_TEST32.not()));

        BitVector vector = new BitVector.Builder(32).setByte(0, 0b1000_1010)
                .setByte(1, 0b1111_0110).setByte(2, 0b1010_0000)
                .setByte(3, 0b1100_0011).build();
        BitVector mask = new BitVector.Builder(32).setByte(2, 0b1100_1010)
                .build();
        BitVector expected = new BitVector.Builder(32).setByte(0, 0b1000_1010)
                .setByte(1, 0b1111_0110).setByte(2, 0b1110_1010)
                .setByte(3, 0b1100_0011).build();
        assertEquals(expected, vector.or(mask));
    }

    @Test
    void AndOrFailsWhenWidthDoesntMatch() {
        assertThrows(IllegalArgumentException.class, () -> {
            ONE_VECTOR32.and(ONE_VECTOR64);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ONE_VECTOR32.or(ONE_VECTOR64);
        });
    }

    @Test
    void doubleShiftworks() {
        BitVector vector = new BitVector.Builder(32).setByte(1, 0b1111_0110)
                .setByte(2, 0b1010_0000).setByte(3, 0b1100_0011).build();
        assertEquals(vector, VECTOR_TEST32.shift(-8).shift(8));

        BitVector vector2 = new BitVector.Builder(32).setByte(0, 0b1000_1010)
                .setByte(1, 0b1111_0110).setByte(2, 0b1010_0000).build();
        assertEquals(vector2, VECTOR_TEST32.shift(8).shift(-8));

    }

//    @Test
//    void test() {
//        System.out.println(VECTOR_TEST);
//    }

    @Test
    void extractZeroWorksOutOfTheVector() {
        BitVector v1 = VECTOR_TEST.extractZeroExtended(88, 64);
        BitVector v2 = new BitVector.Builder(64).setByte(0, 0xFF).build();
        assertEquals(v2, v1);
    }

    @Test
    void extractZeroWorksOutOfTheVector2() {
        BitVector v1 = VECTOR_TEST.extractZeroExtended(-80, 128);
        BitVector v2 = new BitVector.Builder(128).setByte(15, 0xE4)
                .setByte(10, 0xFF).build();
        assertEquals(v2, v1);
    }

    @Test
    void extractZeroWorksOverAllTheVectorAndOut() {
        BitVector v = new BitVector.Builder(160).setByte(14, 0xFF)
                .setByte(3, 0xFF).setByte(8, 0xE4).setByte(12, 0xE4).build();
        assertEquals(v, VECTOR_TEST.extractZeroExtended(-24, 160));
    }

    @Test
    void extractZeroWorksFullyOut() {
        BitVector v1 = new BitVector(128, false);
        BitVector v2 = VECTOR_TEST.extractZeroExtended(-1000, 128);
        BitVector v3 = new BitVector(128, false);
        BitVector v4 = VECTOR_TEST.extractZeroExtended(1000, 128);
        assertEquals(v1, v2);
        assertEquals(v3, v4);
    }

    @Test
    void extractWrappedWorksOutOfTheVector() {
        BitVector v1 = VECTOR_TEST.extractWrapped(88, 64);
        BitVector v2 = new BitVector.Builder(64).setByte(0, 0xFF)
                .setByte(1, 0xFF).setByte(6, 0xE4).build();
        assertEquals(v2, v1);
    }

    @Test
    void extractWrappedWorksOutOfTheVector2() {
        BitVector v1 = VECTOR_TEST.extractWrapped(-16, 32);
        BitVector v2 = new BitVector.Builder(32).setByte(1, 0xFF)
                .setByte(2, 0xFF).build();
        assertEquals(v1, v2);
    }

    @Test
    void extractWrappedWorksOverAllTheVectorAndOut() {
        BitVector v = new BitVector.Builder(160).setByte(2, 0xFF)
                .setByte(0, 0xE4).setByte(14, 0xFF).setByte(3, 0xFF)
                .setByte(8, 0xE4).setByte(12, 0xE4).setByte(15, 0xFF).build();
        assertEquals(v, VECTOR_TEST.extractWrapped(-24, 160));
    }

    @Test
    void extractWrappedWorksFullyOut() {
        BitVector v = new BitVector.Builder(64).setByte(3, 0xE4)
                .setByte(7, 0xE4).build();
        assertEquals(v, VECTOR_TEST.extractWrapped(-80, 64));
    }

    @Test
    void extractWrappedWorksFullyOut2() {
        BitVector v = new BitVector.Builder(64).setByte(3, 0xE4)
                .setByte(7, 0xE4).build();
        assertEquals(v, VECTOR_TEST.extractWrapped(112, 64));
    }

    @Test
    void extractWrappedWorksOverAllTheVectorAndOutMultiple32() {
        BitVector v = new BitVector.Builder(160).setByte(1, 0xE4)
                .setByte(3, 0xFF).setByte(15, 0xFF).setByte(4, 0xFF)
                .setByte(9, 0xE4).setByte(13, 0xE4).setByte(16, 0xFF).build();
        assertEquals(v, VECTOR_TEST.extractWrapped(-32, 160));
    }

    @Test
    void extractZeroWorksOverAllTheVectorAndOutMultiple32() {
        BitVector v = new BitVector.Builder(160).setByte(15, 0xFF)
                .setByte(4, 0xFF).setByte(9, 0xE4).setByte(13, 0xE4).build();
        assertEquals(v, VECTOR_TEST.extractZeroExtended(-32, 160));
    }

}
