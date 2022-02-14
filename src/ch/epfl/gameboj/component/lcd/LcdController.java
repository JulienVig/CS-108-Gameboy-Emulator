/**
* Représente un contrôleur LCD.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

public final class LcdController implements Component, Clocked {
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int[] MODE_CYCLES = { 51, 114, 20, 43 };
    private static final int TILE_SIZE = 8;
    private static final int BG_PIXEL_SIZE = 256;
    private static final int BG_TILE_SIZE = BG_PIXEL_SIZE / TILE_SIZE;
    private static final int LY_ADDRESS = 0xFF44;

    private static final int SPRITE_SIZE = 8;
    private static final int BIG_SPRITE_SIZE = 16;
    private static final int SPRITE_OAM_SIZE = 4;

    private long nextNonIdleCycle;
    private final Cpu cpu;
    private final Ram videoRAM;
    private final Ram OAM;
    private final RegisterFile<Reg> registerFile;
    private Bus bus;
    private LcdImage currentImage;
    private LcdModes nextMode;
    private LcdImage.Builder nextImageBuilder;
    private int winY;
    private int copySource;
    private int copyDestination;

    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum LcdcBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum StatBits implements Bit {
        MOD0, MOD1, LYC_EQ_LY, INT_MOD0, INT_MOD1, INT_MOD2, INT_LYC
    }

    private enum SpriteInfo implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    private enum LcdModes {
        H_BLANK, V_BLANK, MODE_2, MODE_3
    }

    /**
     * Construit un contrôleur LCD
     * 
     * @param cpu
     *            le processeur auquel le contrôleur est lié
     */
    public LcdController(Cpu cpu) {
        this.cpu = cpu;
        videoRAM = new Ram(AddressMap.VIDEO_RAM_SIZE);
        OAM = new Ram(AddressMap.OAM_RAM_SIZE);
        currentImage = null;
        registerFile = new RegisterFile<>(Reg.values());
        copyDestination = 160;
    }

    /**
     * Retourne l'image actuellement affichée à l'écran
     * 
     * @return l'image actuellement affichée à l'écran
     */
    public LcdImage currentImage() {
        if (currentImage != null) {
            return currentImage;
        } else {
            return new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH).build();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        Component.super.attachTo(bus);
        this.bus = bus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            return videoRAM.read(address - AddressMap.VIDEO_RAM_START);
        }
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            return OAM.read(address - AddressMap.OAM_START);
        }
        if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) {
            return getReg(Reg.values()[address - AddressMap.REGS_LCDC_START]);
        }
        return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END) {
            videoRAM.write(address - AddressMap.VIDEO_RAM_START, data);
            return;
        }
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            OAM.write(address - AddressMap.OAM_START, data);
            return;
        }
        if (!(address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END) || address == LY_ADDRESS) {
            return;
        }
        Reg modifiedReg = Reg.values()[address - AddressMap.REGS_LCDC_START];
        if (modifiedReg == Reg.STAT) {
            registerFile.set(Reg.STAT, Bits.clip(3, getReg(Reg.STAT)) + (Bits.extract(data, 3, 5) << 3));
            return;
        }
        registerFile.set(modifiedReg, data);
        if (modifiedReg == Reg.DMA) {
            copyDestination = 0;
            copySource = getReg(Reg.DMA) << 8;
        }
        if (modifiedReg == Reg.LYC) {
            updateLYInterrupts();
        }
        if (!(testBitReg(Reg.LCDC, LcdcBits.LCD_STATUS) || nextNonIdleCycle == Long.MAX_VALUE)) {
            registerFile.setBit(Reg.STAT, StatBits.MOD0, false);
            registerFile.setBit(Reg.STAT, StatBits.MOD1, false);
            registerFile.set(Reg.LY, 0);
            updateLYInterrupts();
            nextNonIdleCycle = Long.MAX_VALUE;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (copyDestination != AddressMap.OAM_RAM_SIZE) {
            OAM.write(copyDestination, bus.read(copySource));
            copyDestination++;
            copySource++;
        }
        if (testBitReg(Reg.LCDC, LcdcBits.LCD_STATUS)) {
            if (nextNonIdleCycle == Long.MAX_VALUE) {
                nextNonIdleCycle = cycle;
                nextMode = LcdModes.MODE_2;
            }
            if (nextNonIdleCycle == cycle) {
                reallyCycle();
            }
        }
    }

    private void reallyCycle() {
        incNextNonIdleCycle(nextMode);
        updateStatMode(nextMode);
        switch (nextMode) {
        case H_BLANK:
            if (getLY() == LCD_HEIGHT - 1) {
                nextMode = LcdModes.V_BLANK;
            } else {
                nextMode = LcdModes.MODE_2;
                incLY();
            }
            break;
        case V_BLANK:
            if (getLY() == LCD_HEIGHT - 1) {
                currentImage = nextImageBuilder.build();
                cpu.requestInterrupt(Interrupt.VBLANK);
            }
            incLY();
            if (getLY() == LCD_HEIGHT + 10) {
                nextMode = LcdModes.MODE_2;
                registerFile.set(Reg.LY, 0);
                updateLYInterrupts();
            }
            break;
        case MODE_2:
            if (getLY() == 0) {
                nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
                winY = 0;
            }
            nextMode = LcdModes.MODE_3;
            break;
        case MODE_3:
            nextMode = LcdModes.H_BLANK;
            nextImageBuilder.setLine(getLY(), computeLine(getLY()));
            break;
        }
    }

    private void updateStatMode(LcdModes mode) {
        if (getLY() < LCD_HEIGHT) {
            int modeIndex = mode.ordinal();
            registerFile.setBit(Reg.STAT, StatBits.MOD0, Bits.test(modeIndex, 0));
            registerFile.setBit(Reg.STAT, StatBits.MOD1, Bits.test(modeIndex, 1));
            if (mode != LcdModes.MODE_3) {
                changingModeInterrupt(mode);
            }
        }
    }

    private void incLY() {
        registerFile.set(Reg.LY, getReg(Reg.LY) + 1);
        updateLYInterrupts();
    }

    private int getLY() {
        return getReg(Reg.LY);
    }

    private void updateLYInterrupts() {
        registerFile.setBit(Reg.STAT, StatBits.LYC_EQ_LY, getReg(Reg.LYC) == getLY());
        if (testBitReg(Reg.STAT, StatBits.INT_LYC) && testBitReg(Reg.STAT, StatBits.LYC_EQ_LY)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
    }

    private void changingModeInterrupt(LcdModes mode) {
        if (Bits.test(getReg(Reg.STAT), StatBits.INT_MOD0.index() + mode.ordinal())) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
    }

    private void incNextNonIdleCycle(LcdModes nextMode) {
        nextNonIdleCycle += MODE_CYCLES[nextMode.ordinal()];
    }

    private LcdImageLine computeLine(int lineIndex) {
        int adjustedWX = Math.max(0, getReg(Reg.WX) - (SPRITE_SIZE - 1));
        int adjustedLineIndex = (lineIndex + getReg(Reg.SCY)) % BG_PIXEL_SIZE;

        // Tests de l'activation des différents composants d'une image
        boolean windowActivated = getReg(Reg.WY) <= lineIndex && testBitReg(Reg.LCDC, LcdcBits.WIN) && adjustedWX >= 0
                && adjustedWX < LCD_WIDTH;
        boolean bgActivated = testBitReg(Reg.LCDC, LcdcBits.BG);
        boolean spritesActivated = testBitReg(Reg.LCDC, LcdcBits.OBJ);

        // Création des constructeurs
        LcdImageLine.Builder bgBuilder = new LcdImageLine.Builder(BG_PIXEL_SIZE);
        LcdImageLine.Builder windowBuilder = new LcdImageLine.Builder(BG_PIXEL_SIZE);
        LcdImageLine bgSpritesLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        LcdImageLine fgSpritesLine = new LcdImageLine.Builder(LCD_WIDTH).build();

        int bgStartAddress = AddressMap.BG_DISPLAY_DATA[testBitReg(Reg.LCDC, LcdcBits.BG_AREA) ? 1 : 0];
        int winStartAddress = AddressMap.BG_DISPLAY_DATA[testBitReg(Reg.LCDC, LcdcBits.WIN_AREA) ? 1 : 0];
        int[] spritesArray = spritesIntersectingLine(lineIndex);

        for (int i = 0; i < BG_TILE_SIZE; i++) {
            if (bgActivated) {
                addTileToBuilder(bgStartAddress, adjustedLineIndex, i, bgBuilder);
            }
            if (windowActivated && (i < (LCD_WIDTH - adjustedWX) / TILE_SIZE)) {
                addTileToBuilder(winStartAddress, winY, i, windowBuilder);
            }
            if (spritesActivated && (i < spritesArray.length)) {
                LcdImageLine.Builder spritesBuilder = new LcdImageLine.Builder(LCD_WIDTH);

                // Déclaration des variables concernant le sprite actuel
                int spriteIndexInOAM = spritesArray[i] * SPRITE_OAM_SIZE;
                int[] spriteAttributes = { OAM.read(spriteIndexInOAM), OAM.read(spriteIndexInOAM + 1),
                        OAM.read(spriteIndexInOAM + 2), OAM.read(spriteIndexInOAM + 3) };
                int tileIndexInVideoRAM = spriteAttributes[2];
                int adjustedYCoord = spriteAttributes[0] - BIG_SPRITE_SIZE;

                // Gestion du retournement vertical si nécessaire selon la taille du sprite
                int currentDrawingLine = lineIndex - adjustedYCoord;
                if (Bits.test(spriteAttributes[3], SpriteInfo.FLIP_V)) {
                    int spriteSize = testBitReg(Reg.LCDC, LcdcBits.OBJ_SIZE) ? BIG_SPRITE_SIZE : SPRITE_SIZE;
                    currentDrawingLine = (spriteSize - 1) - currentDrawingLine;
                }

                // Calcul du sprite actuel en tenant compte du retournement horizontal si nécessaire
                boolean flipH = Bits.test(spriteAttributes[3], SpriteInfo.FLIP_H);
                int[] bytes = computeBytes(AddressMap.TILE_SOURCE[1], tileIndexInVideoRAM, currentDrawingLine, flipH);
                spritesBuilder.setBytes(0, bytes[0], bytes[1]);

                // Ajout du sprite calculé à la ligne complète des sprites (selon le plan dans lequel il se trouve)
                int palette = registerFile
                        .get(Bits.test(spriteAttributes[3], SpriteInfo.PALETTE) ? Reg.OBP1 : Reg.OBP0);
                LcdImageLine line = spritesBuilder.build().shift(spriteAttributes[1] - SPRITE_SIZE).mapColors(palette);
                if (Bits.test(spriteAttributes[3], SpriteInfo.BEHIND_BG)) {
                    bgSpritesLine = line.below(bgSpritesLine);
                } else {
                    fgSpritesLine = line.below(fgSpritesLine);
                }
            }
        }

        // Calcul de la portion de l'arrière-plan affichée, puis construction finale de la ligne avec ajout des sprites
        // et de la fenêtre si nécessaire
        int palette = getReg(Reg.BGP);
        LcdImageLine finalLine = bgBuilder.build().extractWrapped(getReg(Reg.SCX), LCD_WIDTH).mapColors(palette);
        BitVector fusionOpacity = finalLine.opacity().or(bgSpritesLine.opacity().not());
        if (windowActivated) {
            winY++;
            LcdImageLine windowLine = windowBuilder.build().mapColors(palette);
            finalLine = finalLine.join(windowLine.extractWrapped(0, LCD_WIDTH).shift(adjustedWX), adjustedWX);
        }
        finalLine = bgSpritesLine.below(finalLine, fusionOpacity).below(fgSpritesLine);
        return finalLine;
    }

    private void addTileToBuilder(int startAddress, int currentLine, int currentTile, LcdImageLine.Builder builder) {
        int tileIndex = read(startAddress + (currentLine / TILE_SIZE) * BG_TILE_SIZE + currentTile);
        int tileStartAddress;
        if (tileIndex >= 0x80) {
            tileStartAddress = AddressMap.TILE_SOURCE[1];
        } else {
            // La case 0 de Tile_source a été modifiée dans l'addresse map pour respecter notre mise en oeuvre, nous
            // avons cherché à profiter de la zone partagée par les tuiles d'indice supérieur à 0x80
            tileStartAddress = AddressMap.TILE_SOURCE[testBitReg(Reg.LCDC, LcdcBits.TILE_SOURCE) ? 1 : 0];
        }
        int tileLine = currentLine % TILE_SIZE;
        int[] bytes = computeBytes(tileStartAddress, tileIndex, tileLine);
        builder.setBytes(currentTile, bytes[0], bytes[1]);
    }

    private int[] computeBytes(int startAddress, int tileIndex, int tileLine) {
        return computeBytes(startAddress, tileIndex, tileLine, false);
    }

    private int[] computeBytes(int startAddress, int tileIndex, int tileLine, boolean flipped) {
        int address = startAddress + tileIndex * (TILE_SIZE * 2) + tileLine * 2;
        int msb = read(address + 1);
        int lsb = read(address);
        if (!flipped) {
            msb = Bits.reverse8(msb);
            lsb = Bits.reverse8(lsb);
        }
        return new int[] { msb, lsb };
    }

    private int[] spritesIntersectingLine(int lineIndex) {
        // Le nombre de sprites affichés sur une ligne ne peut excéder 10
        int[] spritesArray = new int[10];
        int spritesCount = 0;
        for (int i = 0; i < 39; i++) {
            int adjustedYcoord = OAM.read(i * SPRITE_OAM_SIZE) - BIG_SPRITE_SIZE;
            int spriteSize = testBitReg(Reg.LCDC, LcdcBits.OBJ_SIZE) ? BIG_SPRITE_SIZE : SPRITE_SIZE;
            if (lineIndex >= adjustedYcoord && lineIndex < (adjustedYcoord + spriteSize)
                    && spritesCount < spritesArray.length) {
                spritesArray[spritesCount++] = Bits.make16(OAM.read(i * SPRITE_OAM_SIZE + 1), i);
            }
        }
        Arrays.sort(spritesArray, 0, spritesCount);
        for (int i = 0; i < spritesArray.length; i++) {
            spritesArray[i] = Bits.clip(8, spritesArray[i]);
        }
        return Arrays.copyOf(spritesArray, spritesCount);
    }

    private int getReg(Reg register) {
        return registerFile.get(register);
    }

    private boolean testBitReg(Reg register, Bit bit) {
        return registerFile.testBit(register, bit);
    }

    /**
     * Représente les différents types de données graphiques du système.
     *
     */
    public enum LcdContent {
        VRAM, BG, WIN, OAM,
    }

    /**
     * Imprime la totalité du contenu de la mémoire vidéo dans une image GameBoy et la retourne.
     * 
     * @return la totalité du contenu de la mémoire vidéo
     */
    public LcdImage printMemory() {
        int tilesInMemory = 384;
        int tilesPerLine = 16;
        int lineSize = tilesPerLine * TILE_SIZE;
        int numberOfLine = tilesInMemory / tilesPerLine;
        LcdImage.Builder imageBuilder = new LcdImage.Builder(numberOfLine * TILE_SIZE, lineSize);
        for (int i = 0; i < numberOfLine * TILE_SIZE; i++) {
            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(lineSize);
            for (int j = 0; j < tilesPerLine; j++) {
                int[] bytes = computeBytes(AddressMap.VIDEO_RAM_START, i / TILE_SIZE * tilesPerLine + j, i);
                lineBuilder.setBytes(j, bytes[0], bytes[1]);
            }
            imageBuilder.setLine(i, lineBuilder.build());
        }
        return imageBuilder.build();
    }

    /**
     * Imprime la totalité de l'image calculée par le LCDController (de 256*256 pixels), si le paramètre printWindow est
     * vrai, on imprime la fenêtre, sinon l'arrière-plan. L'image est retournée sous forme d'une image GameBoy
     * 
     * @param printWindow
     *            vrai si on veut imprimer la fenêtre, sinon l'arrière plan est imprimé.
     * @return la totalité de l'image calculée sous forme d'une image GameBoy
     */
    public LcdImage printEntireImage(boolean printWindow) {
        LcdImage.Builder imageBuilder = new LcdImage.Builder(BG_PIXEL_SIZE, BG_PIXEL_SIZE);
        LcdcBits area = printWindow ? LcdcBits.WIN_AREA : LcdcBits.BG_AREA;
        int displayArea = AddressMap.BG_DISPLAY_DATA[testBitReg(Reg.LCDC, area) ? 1 : 0];

        for (int i = 0; i < BG_PIXEL_SIZE; i++) {
            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(BG_PIXEL_SIZE);
            for (int j = 0; j < BG_PIXEL_SIZE / TILE_SIZE; j++) {
                addTileToBuilder(displayArea, i, j, lineBuilder);
            }
            imageBuilder.setLine(i, lineBuilder.build());
        }
        return imageBuilder.build();
    }

    /**
     * Imprime la totalité du contenu de la mémoire d'attributs d'objets sous la forme d'une image GameBoy.
     * 
     * @return le contenu de l'OAM sous forme d'une image GameBoy
     */
    public LcdImage printOam() {
        int spritesInOam = 40;
        int tilesPerLine = 8;
        int lineSize = tilesPerLine * SPRITE_SIZE;
        int numberOfLine = spritesInOam / tilesPerLine;
        LcdImage.Builder imageBuilder = new LcdImage.Builder(spritesInOam, lineSize);

        for (int i = 0; i < numberOfLine * SPRITE_SIZE; i++) {
            LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(lineSize);
            for (int j = 0; j < tilesPerLine; j++) {
                int[] bytes = computeBytes(AddressMap.TILE_SOURCE[1],
                        OAM.read(((i / SPRITE_SIZE) * tilesPerLine + j) * SPRITE_OAM_SIZE + 2), i, false);
                lineBuilder.setBytes(j, bytes[0], bytes[1]);
            }
            imageBuilder.setLine(i, lineBuilder.build());
        }
        return imageBuilder.build();
    }
}
