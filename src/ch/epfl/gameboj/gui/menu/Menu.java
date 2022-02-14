/**
* Représente un menu composé de plusieurs sections.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui.menu;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public final class Menu extends VBox {
    private final List<Item> items;
    private int selectedItemIndex;

    /**
     * Construit un nouveau menu.
     */
    public Menu() {
        items = new ArrayList<>();
        setMaxSize(300, 300);
        setStyle("-fx-border-style: solid inside;" + "-fx-border-width: 2;" + "-fx-border-radius: 5;"
                + "-fx-border-color: black;" + "-fx-background-radius: 5;" + "-fx-background-color: rgba(0,0,0,0.3);");
        setAlignment(Pos.TOP_CENTER);
    }

    /**
     * Construit un nouveau menu avec un titre.
     * 
     * @param title
     *            le titre du menu
     */
    public Menu(String title) {
        this();
        Label name = new Label(title);
        name.setPadding(new Insets(0, 0, 10, 0));
        name.setTextFill(Color.WHITE);
        name.setStyle("-fx-font: 24 Futura;");
        getChildren().add(name);
    }

    /**
     * Traite l'évènement clavier reçu, en modifiant la section sélectionnée ou en transmettant l'évènement à la section
     * sélectionnée.
     * 
     * @param e
     *            l'évènement clavier reçu
     */
    public void handle(KeyEvent e) {
        if (items.isEmpty()) {
            return;
        }
        if (e.getCode() == KeyCode.UP) {
            if (selectedItemIndex > 0) {
                items.get(selectedItemIndex).setActive(false);
                items.get(--selectedItemIndex).setActive(true);
            }
        } else if (e.getCode() == KeyCode.DOWN) {
            if (selectedItemIndex < items.size() - 1) {
                items.get(selectedItemIndex).setActive(false);
                items.get(++selectedItemIndex).setActive(true);
            }
        } else {
            items.get(selectedItemIndex).handle(e);
        }
    }

    /**
     * Ajoute toutes les sections au menu.
     * @param items les sections à ajouter.
     */
    public void addAll(Item... items) {
        for (Item item : items) {
            addElement(item);
        }
    }

    /**
     * Ajoute une section au menu.
     * @param item la section à ajouter
     */
    public void addElement(Item item) {
        getChildren().add(item);
        items.add(item);
        items.get(selectedItemIndex).setActive(true);
    }
}
