/**
* Classe principale du projet GameBoy qui se charge de la gestion de l'interface graphique.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class OldMain extends Application {
    private static final Map<KeyCode, Key> keyMapCode = new HashMap<>(
            Map.of(KeyCode.RIGHT, Key.RIGHT, KeyCode.LEFT, Key.LEFT, KeyCode.UP, Key.UP, KeyCode.DOWN, Key.DOWN));
    private static final Map<String, Key> keyMapText = new HashMap<>(
            Map.of("a", Key.A, "b", Key.B, " ", Key.SELECT, "s", Key.START));

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
        List<String> list = getParameters().getRaw();
        if (list.size() != 1) {
            System.exit(1);
        }
        File f = new File(list.get(0));
        GameBoy gb = new GameBoy(Cartridge.ofFile(f));

        ImageView view = new ImageView();
        BorderPane pane = new BorderPane(view);
        Scene scene = new Scene(pane);

        view.setFitHeight(LcdController.LCD_HEIGHT * 2);
        view.setFitWidth(LcdController.LCD_WIDTH * 2);
        view.setImage(ImageConverter.convert(gb.lcdController().currentImage(),
                new int[] { 0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 }));
        primaryStage.setScene(scene);
        primaryStage.show();
        view.requestFocus();

        long start = System.nanoTime();
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - start;
                long cycle = (long) (elapsed * GameBoy.CYCLES_PER_NANOSECOND);
                gb.runUntil(cycle);
                view.setImage(ImageConverter.convert(gb.lcdController().currentImage(),
                        new int[] { 0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 }));
            }

        }.start();

        EventHandler<KeyEvent> handlerPressed = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Key k = keyMapText.getOrDefault(event.getText(), keyMapCode.get(event.getCode()));
                if (k != null) {
                    gb.joypad().keyPressed(k);
                }
            }
        };

        EventHandler<KeyEvent> handlerReleased = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Key k = keyMapText.getOrDefault(event.getText(), keyMapCode.get(event.getCode()));
                if (k != null) {
                    gb.joypad().keyReleased(k);
                }
            }

        };

        view.setOnKeyPressed(handlerPressed);
        view.setOnKeyReleased(handlerReleased);
    }

}
