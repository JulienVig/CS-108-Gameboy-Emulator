/**
* Description de la classe
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

public class LcdImageTest {
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
    
    @Test
    void builderWorks() {
        LcdImage test1 = new LcdImage.Builder(2, 64).setLine(0, RANDOM_LINE1).setLine(1, RANDOM_LINE2).build();
        LcdImage test2 = new LcdImage(2, 64, Arrays.asList(RANDOM_LINE1, RANDOM_LINE2));
        assertEquals(test2, test1);
    }
    
    @Test
    void gettersWorks() {
        LcdImage test1 = new LcdImage.Builder(2, 64).setLine(0, RANDOM_LINE1).setLine(1, RANDOM_LINE2).build();
        assertEquals(2, test1.height());
        assertEquals(64, test1.width());
    }
    
    @Test
    void getAColorWorks() {
        LcdImage test1 = new LcdImage.Builder(2, 64).setLine(0, RANDOM_LINE1).setLine(1, RANDOM_LINE2).build();
            for (int j = 0; j < test1.width(); j++) {
                assertEquals((RANDOM_LINE1.msb().testBit(j) ? 2 : 0) + (RANDOM_LINE1.lsb().testBit(j) ? 1 : 0), test1.get(j, 0));
            }
            for (int j = 0; j < test1.width(); j++) {
                assertEquals((RANDOM_LINE2.msb().testBit(j) ? 2 : 0) + (RANDOM_LINE2.lsb().testBit(j) ? 1 : 0), test1.get(j, 1));
            }
    }
}
