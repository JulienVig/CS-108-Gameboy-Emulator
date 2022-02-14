/**
* Représente une section de menu sélectionnable.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui.menu;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class BasicItem extends Item {
    private final Polygon triangle;

    /**
     * Construit une nouvelle section de menu simple.
     * @param name le nom de la section
     */
    public BasicItem(String name) {
        super(name);
        triangle = new Polygon(0.0, 0.0, 0.0, 14.0, 7, 7);
        triangle.setFill(Color.WHITE);
        getChildren().add(0, triangle);
        setActive(false);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.gui.Item#setActive(boolean)
     */
    @Override
    public void setActive(boolean b) {
        super.setActive(b);
        triangle.setVisible(b);
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.gui.Item#handle(javafx.scene.input.KeyEvent)
     */
    @Override
    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            activate();
        }
    }
}