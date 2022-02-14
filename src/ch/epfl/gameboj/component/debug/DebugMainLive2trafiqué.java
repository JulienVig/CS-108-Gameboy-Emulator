package ch.epfl.gameboj.component.debug;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class DebugMainLive2trafiqué extends Application {

    /** Configuration */

    private static final String ROM_PATH = "ROMs/flappyboy.gb";

    private static final float EMULATION_SPEED = 1f;
    private static final int CYCLES_PER_ITERATION = (int)(17_556 * EMULATION_SPEED);
    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };

    @Override 
    public void start(Stage stage) throws IOException, InterruptedException {
        // Create GameBoy
        File romFile = new File(ROM_PATH);
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));

        // Create Scene
        ImageView imageView = new ImageView();
        imageView.setImage(getImage(gb));
        imageView.setSmooth(false);
        Group root = new Group();
        Scene scene = new Scene(root);
        imageView.fitWidthProperty().bind(scene.widthProperty());
        imageView.fitHeightProperty().bind(scene.heightProperty());
        scene.setFill(Color.BLACK);
        HBox box = new HBox();
        box.getChildren().add(imageView);
        root.getChildren().add(box);
        stage.setWidth(320);
        stage.setHeight(288);
        stage.setScene(scene);
        stage.sizeToScene();
        EventHandler<KeyEvent> handlerP = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                case A :
                    gb.joypad().keyPressed(Key.A);
                    break;
                case Z :
                    gb.joypad().keyPressed(Key.B);
                    break;
                case W :
                    gb.joypad().keyPressed(Key.START);
                    break;
                case X :
                    gb.joypad().keyPressed(Key.SELECT);
                    break;
                case UP :
                    gb.joypad().keyPressed(Key.UP);
                    break;
                case DOWN :
                    gb.joypad().keyPressed(Key.DOWN );
                    break;
                case RIGHT :
                    gb.joypad().keyPressed(Key.RIGHT);
                    break;
                case LEFT :
                    gb.joypad().keyPressed(Key.LEFT);
                    break;
                default:
                    break;
                }
                
            }
            
        };
        EventHandler<KeyEvent> handlerR = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                case A :
                    gb.joypad().keyReleased(Key.A);
                    break;
                case Z :
                    gb.joypad().keyReleased(Key.B);
                    break;
                case W :
                    gb.joypad().keyReleased(Key.START);
                    break;
                case X :
                    gb.joypad().keyReleased(Key.SELECT);
                    break;
                case UP :
                    gb.joypad().keyReleased(Key.UP);
                    break;
                case DOWN :
                    gb.joypad().keyReleased(Key.DOWN );
                    break;
                case RIGHT :
                    gb.joypad().keyReleased(Key.RIGHT);
                    break;
                case LEFT :
                    gb.joypad().keyReleased(Key.LEFT);
                default:
                    break;
                }
            }
            
        };
        scene.setOnKeyPressed(handlerP);
        scene.setOnKeyReleased(handlerR);
        stage.minWidthProperty().bind(scene.heightProperty());
        stage.minHeightProperty().bind(scene.widthProperty());
        stage.setTitle("gameboj");
        stage.show();

        // Update GameBoy
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                gb.runUntil(gb.cycles() + CYCLES_PER_ITERATION);
                imageView.setImage(null);
                imageView.setImage(getImage(gb));
            }
        }.start();
    }

    private static final Image getImage(GameBoy gb) {
        LcdImage lcdImage = gb.lcdController().currentImage();
        BufferedImage bufferedImage = new BufferedImage(lcdImage.width(),
                lcdImage.height(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < lcdImage.height(); ++y)
            for (int x = 0; x < lcdImage.width(); ++x)
                bufferedImage.setRGB(x, y, COLOR_MAP[lcdImage.get(x, y)]);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}