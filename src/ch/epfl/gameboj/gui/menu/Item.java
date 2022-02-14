/**
* Représente une section de menu pouvant effectuer une action.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui.menu;

import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class Item extends HBox {
    private static final Font ITEM_FONT = Font.font("Futura",24);
    private final Text text;
    private Runnable script;

    /**
     * Construit une nouvelle section de menu.
     * @param name le nom de la section
     */
    public Item(String name) {
        super(10);
        text = new Text(name);
        getChildren().add(text);
        setAlignment(Pos.CENTER);
    }
    
    /**
     * Définit si la section est actuellement sélectionnée.
     * @param b true si la section est sélectionnée
     */
    public void setActive(boolean b) {
        text.setFill(b ? Color.WHITE : Color.BLACK);
        text.setFont(ITEM_FONT);
    }
    
    /**
     * Définit l'action à effectuer lorsque la section est activée.
     * @param r l'action à effectuer
     */
    public void setAction(Runnable r) {
        script = r;
    }
    
    /**
     * Active la section et effectue son action associée.
     */
    public void activate() {
        if (script != null)
            script.run();
    }
    
    /**
     * Traite l'évènement clavier reçu.
     * @param e l'évènement traité
     */
    public abstract void handle(KeyEvent e);
    
}
