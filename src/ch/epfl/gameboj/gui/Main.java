/**
* Classe principale du projet GameBoy qui se charge de la gestion de l'interface graphique.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdController.LcdContent;
import ch.epfl.gameboj.component.lcd.LcdImage;
import ch.epfl.gameboj.gui.menu.BasicItem;
import ch.epfl.gameboj.gui.menu.Item;
import ch.epfl.gameboj.gui.menu.Menu;
import ch.epfl.gameboj.gui.menu.MenuItem;
import ch.epfl.gameboj.gui.menu.SelectItem;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class Main extends Application {
    private static final List<Float> SPEED = List.of(0.5f, 1.0f, 2.0f, 4.0f);

    private static final int[] DEFAULT_PALETTE = new int[] { 0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9,
            0xFF_00_00_00 };
    private static final int[] AUTHENTIC_PALETTE = new int[] { 0xFF_9B_BC_0F, 0xFF_8B_AC_0F, 0xFF_30_62_30,
            0xFF_0F_38_0F };
    private static final int[] MARIO2_PALETTE = new int[] { 0xFF_EF_F7_B6, 0xFF_DF_A6_77, 0xFF_11_C6_00,
            0xFF_00_00_00 };
    private static final int[] ZELDA_PALETTE = new int[] { 0xFF_FF_FF_B5, 0xFF_7B_C6_7B, 0xFF_6B_8C_42, 0xFF_5A_39_21 };
    private static final int[] CHOCOLATE_PALETTE = new int[] { 0xFF_FF_E4_C2, 0xFF_DC_A4_56, 0xFF_A9_60_4C,
            0xFF_42_29_36 };
    private static final int[] ARQ4_PALETTE = new int[] { 0xFF_FF_FF_FF, 0xFF_67_72_A9, 0xFF_3A_32_77, 0xFF_00_00_00 };
    private static final List<int[]> PALETTES = List.of(DEFAULT_PALETTE, AUTHENTIC_PALETTE, CHOCOLATE_PALETTE,
            ARQ4_PALETTE, MARIO2_PALETTE, ZELDA_PALETTE);

    private static final Map<KeyCode, Key> keyMapCode = new HashMap<>(
            Map.of(KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT, Key.LEFT, KeyCode.UP, Key.UP, KeyCode.DOWN, Key.DOWN));
    private static final Map<String, Key> keyMapText = new HashMap<>(
            Map.of("a", Key.A, "b", Key.B, " ", Key.SELECT, "s", Key.START));

    private static final File MAIN_FOLDER = new File("Gameboj Data");
    private static final File SCREEN_FOLDER = new File(MAIN_FOLDER, "Screen");
    private static final File SAVE_FOLDER = new File(MAIN_FOLDER, "Save");
    private static final File ROM_FOLDER = new File(MAIN_FOLDER, "ROMs");
    private static final File GRAPHIC_FOLDER = new File(MAIN_FOLDER, "Graphic Data");

    private float simulationSpeed = 1f;
    private boolean gamePaused = false;
    private boolean turbo = false;
    private long elapsed;
    private long before;
    private int[] selectedPalette = DEFAULT_PALETTE;

    private StackPane mainPane;
    private ImageView gameView;
    private Menu mainMenu;
    private Menu selectedMenu;
    private GameBoy gameBoy;
    private String gameName;

    /**
     * Méthode principale du programme.
     * 
     * @param args
     *            tableau d'arguments, doit contenir le chemin d'accès du fichier contenant la cartouche.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javafx.application.Application#start(javafx.stage.Stage)
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> saveGame()));
        primaryStage.setHeight(LcdController.LCD_HEIGHT * 3);
        primaryStage.setWidth(LcdController.LCD_WIDTH * 3);
        primaryStage.setTitle("The Juliens' GameBoj");
        startCatridgeSelector(primaryStage);
        primaryStage.show();
    }

    private List<String> findRomNames() {
        int maxCapacity = 8;
        ArrayList<String> romFiles = new ArrayList<>(maxCapacity);
        if (!ROM_FOLDER.exists()) {
            ROM_FOLDER.mkdirs();
        }
        for (File f : ROM_FOLDER.listFiles()) {
            if (getFileExtension(f).equals("gb")) {
                romFiles.add(f.getName().substring(0, f.getName().length() - 3));
            }
            if (romFiles.size() >= maxCapacity) {
                return romFiles;
            }
        }
        return romFiles;
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    private void startCatridgeSelector(Stage primaryStage) {
        Pane selectPane = new StackPane();
        Scene selectScene = new Scene(selectPane);
        Menu selectMenu = new Menu("Select a catridge");

        List<String> romNames = findRomNames();
        Collections.sort(romNames);
        for (String str : romNames) {
            BasicItem item = new BasicItem(str);
            item.setAction(() -> gameName = str);
            selectMenu.addElement(item);
        }

        if (romNames.isEmpty()) {
            Label info1 = new Label("Please add .gb files in");
            Label info2 = new Label("the folder \"Gameboj Data/ROMs/\"");
            Label info3 = new Label("and restart the emulator to play");
            selectMenu.getChildren().addAll(info1, info2, info3);
        }

        selectPane.getChildren().add(selectMenu);

        EventHandler<KeyEvent> selectorHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                selectMenu.handle(event);
                if (gameName != null) {
                    selectPane.getChildren().remove(selectMenu);
                    primaryStage.setScene(createContent());
                    primaryStage.sizeToScene();
                    startGame();
                    loadGame();
                }
            }
        };
        selectPane.setOnKeyPressed(selectorHandler);
        selectPane.requestFocus();
        primaryStage.setScene(selectScene);
    }

    private void startGame() {
        try {
            gameBoy = new GameBoy(Cartridge.ofFile(new File(ROM_FOLDER, gameName + ".gb")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Label turboLabel = new Label("");
        turboLabel.setFont((Font.font("Futura", 28)));
        turboLabel.setTextFill(Color.WHITE);
        turboLabel.setEffect(new DropShadow(5, Color.BLACK));
        turboLabel.setVisible(false);
        StackPane.setMargin(turboLabel, new Insets(0, 0, mainPane.getHeight() - 40, mainPane.getWidth() - 80));
        mainPane.getChildren().add(turboLabel);

        Timeline timeLine = new Timeline();
        timeLine.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(turboLabel.visibleProperty(), true)),
                new KeyFrame(Duration.millis(1200), new KeyValue(turboLabel.visibleProperty(), false)));

        EventHandler<KeyEvent> handlerPressed = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (selectedMenu != null) {
                    selectedMenu.handle(event);
                }
                Key k = keyMapText.getOrDefault(event.getText(), keyMapCode.get(event.getCode()));
                if (k != null) {
                    gameBoy.joypad().keyPressed(k);
                }
                switch (event.getCode()) {
                case P:
                    takeScreenShot();
                    break;
                case ESCAPE:
                    toggleMenu(mainMenu);
                    break;
                case T:
                    if (!turbo && simulationSpeed <= 2) {
                        simulationSpeed *= 2;
                        turbo = true;
                        turboLabel.setText("x " + simulationSpeed);
                        timeLine.play();
                    }
                default:
                    break;
                }
            }
        };

        EventHandler<KeyEvent> handlerReleased = new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                Key k = keyMapText.getOrDefault(event.getText(), keyMapCode.get(event.getCode()));
                if (k != null) {
                    gameBoy.joypad().keyReleased(k);
                }
                if (event.getCode() == KeyCode.T) {
                    if (turbo) {
                        simulationSpeed *= 0.5;
                        turbo = false;
                    }
                }
            }
        };

        gameView.requestFocus();
        gameView.setOnKeyPressed(handlerPressed);
        gameView.setOnKeyReleased(handlerReleased);

        before = System.nanoTime();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long deltaTime = now - before;
                before = now;

                if (!gamePaused) {
                    elapsed += deltaTime * simulationSpeed;
                    long cycle = (long) (elapsed * GameBoy.CYCLES_PER_NANOSECOND);
                    gameBoy.runUntil(cycle);
                }
                gameView.setImage(ImageConverter.convert(gameBoy.lcdController().currentImage(), selectedPalette));
            }
        }.start();
    }

    private void toggleMenu(Menu mainMenu) {
        if (!gamePaused) {
            gameView.setOpacity(0.5);
            gameView.setEffect(new GaussianBlur());
            mainPane.getChildren().add(mainMenu);
            selectedMenu = mainMenu;
        } else {
            gameView.setOpacity(1);
            gameView.setEffect(null);
            mainPane.getChildren().remove(selectedMenu);
            selectedMenu = null;
        }
        gamePaused = !gamePaused;
    }

    private void saveGame() {
        File saveFile = new File(SAVE_FOLDER, gameName + ".sav");
        if (!SAVE_FOLDER.exists()) {
            SAVE_FOLDER.mkdirs();
        }
        if (gameBoy != null) {
            gameBoy.saveCatridgeRam(saveFile);
        }
    }

    private void loadGame() {
        File saveFile = new File(SAVE_FOLDER, gameName + ".sav");
        if (saveFile.exists()) {
            gameBoy.loadCatridgeRam(saveFile);
        }
    }

    private void takeScreenShot() {
        if (!SCREEN_FOLDER.exists()) {
            SCREEN_FOLDER.mkdirs();
        }
        File screenFile = new File(SCREEN_FOLDER,
                gameName + " screenshot " + new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.FRANCE).format(new Date()) + ".png");
        WritableImage img = mainPane.snapshot(new SnapshotParameters(), null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", screenFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Timeline timeLine = new Timeline();
        timeLine.getKeyFrames().addAll(new KeyFrame(Duration.ZERO, new KeyValue(gameView.opacityProperty(), 0)),
                new KeyFrame(Duration.millis(2000), new KeyValue(gameView.opacityProperty(), 1)));
        timeLine.play();
    }

    private void saveLcdContent(LcdContent content) {
        if (!GRAPHIC_FOLDER.exists()) {
            GRAPHIC_FOLDER.mkdirs();
        }

        String fileName = gameName + " " + content + " "
                + new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.FRANCE).format(new Date()) + ".png";
        LcdImage gbImg = null;
        switch (content) {
        case VRAM:
            gbImg = gameBoy.lcdController().printMemory();
            break;
        case WIN:
            gbImg = gameBoy.lcdController().printEntireImage(true);
            break;
        case BG:
            gbImg = gameBoy.lcdController().printEntireImage(false);
            break;
        case OAM:
            gbImg = gameBoy.lcdController().printOam();
            break;
        }
        Image img = ImageConverter.convert(gbImg, selectedPalette);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(GRAPHIC_FOLDER, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Scene createContent() {

        gameView = new ImageView();
        mainPane = new StackPane(gameView);
        Scene scene = new Scene(mainPane);

        gameView.setPreserveRatio(true);
        gameView.setImage(ImageConverter.convert(
                new LcdImage.Builder(LcdController.LCD_HEIGHT, LcdController.LCD_WIDTH).build(), DEFAULT_PALETTE));
        gameView.setVisible(true);
        gameView.fitWidthProperty().bind(scene.widthProperty());
        gameView.fitHeightProperty().bind(scene.heightProperty());

        mainMenu = new Menu("Main menu");
        Menu optionMenu = new Menu("Options");
        Menu graphicMenu = new Menu("Graphic Data");

        MenuItem optionItem = new MenuItem("Options", optionMenu);
        MenuItem graphicItem = new MenuItem("Graphic Data", graphicMenu);

        // Main Menu Items
        Item screenshotItem = new BasicItem("Take a screenshot");
        Item saveItem = new BasicItem("Save");
        Item resumeItem = new BasicItem("Resume");
        Item quitItem = new BasicItem("Save and Quit game");
        mainMenu.addAll(optionItem, screenshotItem, graphicItem, saveItem, resumeItem, quitItem);

        optionItem.setAction(() -> {
            mainPane.getChildren().remove(optionItem.getParent());
            selectedMenu = optionItem.nextMenu();
            mainPane.getChildren().add(selectedMenu);
        });

        screenshotItem.setAction(() -> {
            toggleMenu(mainMenu);
            takeScreenShot();
            toggleMenu(mainMenu);
        });

        graphicItem.setAction(() -> {
            mainPane.getChildren().remove(graphicItem.getParent());
            selectedMenu = graphicItem.nextMenu();
            mainPane.getChildren().add(selectedMenu);
        });

        saveItem.setAction(() -> saveGame());
        resumeItem.setAction(() -> toggleMenu(mainMenu));
        quitItem.setAction(() -> System.exit(0));

        MenuItem returnFromOptionsItem = new MenuItem("Return", mainMenu);
        returnFromOptionsItem.setAction(() -> {
            mainPane.getChildren().remove(returnFromOptionsItem.getParent());
            selectedMenu = returnFromOptionsItem.nextMenu();
            mainPane.getChildren().add(selectedMenu);
        });

        // Options Menu Items
        SelectItem<Float> speedSelect = new SelectItem<Float>("Speed", SPEED, 1);
        SelectItem<int[]> paletteSelect = new SelectItem<>("Palette", PALETTES,
                List.of("Default", "Authentic", "Chocolate", "Night Dream", "Mario Land 2", "Zelda"));
        optionMenu.addAll(returnFromOptionsItem, paletteSelect, speedSelect);

        speedSelect.setAction(() -> simulationSpeed = speedSelect.setting());
        paletteSelect.setAction(() -> selectedPalette = paletteSelect.setting());

        // Graphic Data Menu Items
        BasicItem vramItem = new BasicItem("Print every tiles from the Video RAM");
        BasicItem bgItem = new BasicItem("Print the entire background");
        BasicItem windowItem = new BasicItem("Print the entire window");
        BasicItem oamItem = new BasicItem("Print the sprite tiles from the OAM");
        MenuItem returnFromGraphicItem = new MenuItem("Return", mainMenu);
        graphicMenu.addAll(returnFromGraphicItem, vramItem, bgItem, windowItem, oamItem);

        returnFromGraphicItem.setAction(() -> {
            mainPane.getChildren().remove(returnFromGraphicItem.getParent());
            selectedMenu = returnFromGraphicItem.nextMenu();
            mainPane.getChildren().add(selectedMenu);
        });
        vramItem.setAction(() -> saveLcdContent(LcdContent.VRAM));
        bgItem.setAction(() -> saveLcdContent(LcdContent.BG));
        windowItem.setAction(() -> saveLcdContent(LcdContent.WIN));
        oamItem.setAction(() -> saveLcdContent(LcdContent.OAM));

        return scene;
    }
}
