/**
* Tests unitaires pour la classe Alu
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.cpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;

public class AluTest {

    @Test
    void testBitWorksWithNormalIndex() {
        for (int i = 0; i <= 7; i++) {
            assertEquals(0xA0, Alu.testBit(0, i));
        }
    }

    @Test
    void testBitFailsForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> Alu.testBit(0, 8));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0, 37733838));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0, -94939393));
    }

    @Test
    void testBitFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(256, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.testBit(-42, 1));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.testBit(25642, 1));
    }

    @Test
    void testBitWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            for (int j = 0; j < 7; j++) {
                if (!Bits.test(i, j)) {
                    assertEquals(0b1010_0000, Alu.testBit(i, j));
                } else {
                    assertEquals(0b0010_0000, Alu.testBit(i, j));
                }
            }

        }
    }

    @Test
    void swapFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(-1));
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(256));
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(-42));
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(25642));
    }

    @Test
    void swapWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            assertEquals(((((i & 0xf) << 4) | ((i & 0xf0) >> 4)) << 8)
                    + (i == 0 ? 0b1000_0000 : 0), Alu.swap(i));
        }
    }

    @Test
    void rotateFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 256));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -42));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 25642));
    }

    @Test
    void rotateWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            assertEquals(
                    (Bits.rotate(8, i, 1) << 8) + (i == 0 ? 0b1000_0000 : 0)
                            + (Bits.test(i, 7) ? 0b0001_0000 : 0),
                    Alu.rotate(RotDir.LEFT, i));
            assertEquals(
                    (Bits.rotate(8, i, -1) << 8) + (i == 0 ? 0b1000_0000 : 0)
                            + (Bits.test(i, 0) ? 0b0001_0000 : 0),
                    Alu.rotate(RotDir.RIGHT, i));
        }
    }

    @Test
    void rotateThroughCarryFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -1, true));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 256, false));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.LEFT, -42, false));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.rotate(RotDir.RIGHT, 25642, true));
    }

    @Test
    void rotateThroughCarryWorksOnEveryValues() {
        assertEquals(0x90, Alu.rotate(RotDir.LEFT, 0x80, false));
        assertEquals(0x0100, Alu.rotate(RotDir.LEFT, 0x00, true));
    }

    @Test
    void shiftLeftFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(-1));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(256));
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(-42));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftLeft(25642));
    }

    @Test
    void shiftLeftWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            assertEquals(
                    (Bits.clip(8, (i << 1)) << 8)
                            + ((Bits.clip(8, i << 1)) == 0 ? 0b1000_0000 : 0)
                            + (Bits.test(i, 7) ? 0b0001_0000 : 0),
                    Alu.shiftLeft(i));
        }
    }

    @Test
    void shiftRightAFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightA(-1));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightA(256));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightA(-42));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightA(25642));
    }

    @Test
    void shiftRightAWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            int value = (Bits.clip(8,
                    (i >> 1) + (Bits.test(i, 7) ? 0b1000_0000 : 0)) << 8)
                    + (Bits.test(i, 0) ? 0b0001_0000 : 0)
                    + ((Bits.clip(8, (i >> 1)
                            + (Bits.test(i, 7) ? 0b1000_0000 : 0))) == 0
                                    ? 0b1000_0000
                                    : 0);
            assertEquals(value, Alu.shiftRightA(i));
        }
    }

    @Test
    void shiftRightLFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightL(-1));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightL(256));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightL(-42));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.shiftRightL(25642));
    }

    @Test
    void shiftRightLWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            int value = (Bits.clip(8, (i >>> 1)) << 8)
                    + (Bits.test(i, 0) ? 0b0001_0000 : 0)
                    + ((Bits.clip(8, (i >>> 1))) == 0 ? 0b1000_0000 : 0);
            assertEquals(value, Alu.shiftRightL(i));
        }
    }

    @Test
    void xorFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(256, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(-42, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(25642, 1));

        assertThrows(IllegalArgumentException.class, () -> Alu.xor(1, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(1, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(1, -42));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(1, 25642));
    }

    @Test
    void xorWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            for (int j = 0; j <= 255; j++) {
                assertEquals(((i ^ j) << 8) + ((i ^ j) == 0 ? 0b1000_0000 : 0),
                        Alu.xor(i, j));
            }
        }
        assertEquals(0xF400, Alu.xor(0x53, 0xA7));
    }

    @Test
    void orFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(256, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(-42, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(25642, 1));

        assertThrows(IllegalArgumentException.class, () -> Alu.or(1, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(1, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(1, -42));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(1, 25642));
    }

    @Test
    void orWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            for (int j = 0; j <= 255; j++) {
                assertEquals(((i | j) << 8) + ((i | j) == 0 ? 0b1000_0000 : 0),
                        Alu.or(i, j));
            }
        }
        assertEquals(0xF700, Alu.or(0x53, 0xA7));
    }

    @Test
    void andFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-1, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(256, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(-42, 1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(25642, 1));

        assertThrows(IllegalArgumentException.class, () -> Alu.and(1, -1));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(1, 256));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(1, -42));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(1, 25642));
    }

    @Test
    void andWorksOnEveryValues() {
        for (int i = 0; i <= 255; i++) {
            for (int j = 0; j <= 255; j++) {
                assertEquals(((i & j) << 8) + ((i & j) == 0 ? 0b1000_0000 : 0)
                        + 0b0010_0000, Alu.and(i, j));
            }
        }
        assertEquals(0x0320, Alu.and(0x53, 0xA7));
    }

    private boolean t = true;
    private boolean f = false;

    @Test
    void maskZNHCworksOnEveryValue() {
        for (int i = 0; i <= 0xF; i++) {
            assertEquals(i << 4, Alu.maskZNHC(Bits.test(i, 3), Bits.test(i, 2),
                    Bits.test(i, 1), Bits.test(i, 0)));
        }
        assertEquals(0x70, Alu.maskZNHC(f, t, t, t));
    }

    @Test
    void unpackValueWorksOnEveryValue() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xF; j++) {
                assertEquals(i, Alu.unpackValue((i << 8) + (j << 4)));
            }
        }
        assertEquals(0xFF, Alu.unpackValue(0xFF70));
    }

    @Test
    void unpackFlagsWorksOnEveryValue() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xF; j++) {
                assertEquals((j << 4), Alu.unpackFlags((i << 8) + (j << 4)));
            }
        }
        assertEquals(0x70, Alu.unpackFlags(0xFF70));

    }

    @Test
    void addThrowsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(256, 1, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(1, 256, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(-1, 1, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(0xFFFFFFF, 1, t);
        });
    }

    @Test
    void addWithoutFlagsWorksOnEveryValue() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xFF; j++) {
                assertEquals(Bits.clip(8, i + j), Alu.add(i, j, f) >> 8);
            }
        }
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xFF; j++) {
                assertEquals(Bits.clip(8, i + j + 1), Alu.add(i, j, t) >> 8);
            }
        }
    }

    @Test
    void addWithFlagsWorksOnLimitValue() {
        assertEquals(0x00B0, Alu.add(0x80, 0x7F, t));
        assertEquals(0x0080, Alu.add(0, 0, f));
        assertEquals(0x0100, Alu.add(0, 0, t));
        assertEquals(0x1020, Alu.add(0xF, 0, t));
        assertEquals(0x0110, Alu.add(0xF0, 0x10, t));

    }

    @Test
    void addWithoutBooleanThrowsOnInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(256, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(1, 256);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(-1, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(0xFFFFFFF, 1);
        });
    }

    @Test
    void add16LFailsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16L(0x10000, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16L(1, 0x10000);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16L(-1, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16L(0xFFFFFFF, 1);
        });
    }

//    @Test
//    void add16LWithoutFlagsWorksOnEveryValues() {
//        for (int i = 0; i <= 0xFFFF; i++) {
//            for (int j = 0; j <= 0xFFFF; j++) {
//                assertEquals(Bits.clip(16, i + j), Alu.add16L(i, j) >> 8);
//            }
//        }
//    }
//
//    @Test
//    void add16HWithoutFlagsWorksOnEveryValues() {
//        for (int i = 0; i <= 0xFFFF; i++) {
//            for (int j = 0; j <= 0xFFFF; j++) {
//                assertEquals(Bits.clip(16, i + j), Alu.add16H(i, j) >> 8);
//            }
//        }
//    }

    @Test
    void add16LWithFlagsWorksOnLimitValue() {
        assertEquals(0x120030, Alu.add16L(0x11FF, 0x0001));
        assertEquals(0, Alu.add16L(0xFF00, 0x0100));
        assertEquals(0x011020, Alu.add16L(0x010F, 0x0001));
        assertEquals(0, Alu.add16L(0, 0));
        assertEquals(0xFFFE30, Alu.add16L(0xFFFF, 0xFFFF));
    }

    @Test
    void add16HFailsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16H(0x10000, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16H(1, 0x10000);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16H(-1, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add16H(0xFFFFFFF, 1);
        });
    }

    @Test
    void add16HWithFlagsWorksOnLimitValue() {
        assertEquals(0x120000, Alu.add16H(0x11FF, 1));
        assertEquals(0x30, Alu.add16H(0xFF00, 0x0100));
        assertEquals(0x10, Alu.add16H(0xF000, 0x1000));
        assertEquals(0x100020, Alu.add16H(0x0F00, 0x0100));
        assertEquals(0x30, Alu.add16H(0xFFFF, 0x1));
        assertEquals(0x10000, Alu.add16H(0x00FF, 0x0001));
        assertEquals(0, Alu.add16H(0, 0));
    }

    @Test
    void subFailsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(256, 1, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(1, 256, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(-1, 1, t);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(0xFFFFFFF, 1, t);
        });
    }

    @Test
    void subWithBorrowWithoutFlagsWorksOnEveryValues() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xFF; j++) {
                assertEquals(Bits.clip(8, i - j - 1), Alu.sub(i, j, t) >> 8);
            }
        }
    }

    @Test
    void subWithoutWithoutFlagsBorrowWorksOnEveryValues() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xFF; j++) {
                assertEquals(Bits.clip(8, i - j), Alu.sub(i, j, f) >> 8);
            }
        }
    }

    @Test
    void subWithFlagsWorksOnLimitValues() {
        assertEquals(0xC0, Alu.sub(0xFF, 0xFE, t));
        assertEquals(0xFF70, Alu.sub(0, 0, t));
        assertEquals(0xFF70, Alu.sub(0x01, 0x01, t));
        assertEquals(0x160, Alu.sub(0x10, 0x0F, f));
    }

    @Test
    void subWithoutBorrowFailsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.add(256, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(1, 256);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(-1, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.sub(0xFFFFFFF, 1);
        });
    }

    @Test
    void subWithoutBorrowWithoutFlagsWorksOnEveryValues() {
        for (int i = 0; i <= 0xFF; i++) {
            for (int j = 0; j <= 0xFF; j++) {
                assertEquals(Bits.clip(8, i - j), Alu.sub(i, j) >> 8);
            }
        }
    }

    @Test
    void bcdAdjustFailsOnInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.bcdAdjust(511, f, f, f);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.bcdAdjust(256, f, f, f);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.bcdAdjust(-1, f, f, f);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Alu.bcdAdjust(0xFFFFFFF, f, f, f);
        });
    }

    @Test
    void bcdAdjustWorksOnLimitValues() {
        assertEquals(0x7300, Alu.bcdAdjust(0x6D, f, f, f));
        assertEquals(0x080, Alu.bcdAdjust(0, f, f, f));
        assertEquals(0x0940, Alu.bcdAdjust(0x0F, true, true, false));
        assertEquals(0x90, Alu.bcdAdjust(0x9A, f, f, f)); // 99 + 1= 0
        assertEquals(0x9950, Alu.bcdAdjust(0xFF, t, t, t)); // 1-2 = 99
        assertEquals(0x2340, Alu.bcdAdjust(0x23, t, f, f)); // 35 - 12 = 23
    }

}
