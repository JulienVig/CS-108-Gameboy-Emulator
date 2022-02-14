/**
* Représente une section de menu permettant de sélectionner un paramètre parmi plusieurs paramètres.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;

public final class SelectItem<E> extends Item {
    private final Polyline leftArrow;
    private final Polyline rightArrow;
    private final Text currentLabel;
    private final List<E> settings;
    private final List<String> settingsName;
    private final IntegerProperty property;
    private int selectedSetting;

    /**
     * Construit une nouvelle section de sélection.
     * 
     * @param name
     *            le nom de la section
     * @param settings
     *            les paramètres pouvant être sélectionnés
     * @param settingsName
     *            les noms des différents paramètres, la liste doit être de la même taille que la liste de paramètres
     * @param defaultSetting
     *            l'indice du réglage par défaut.
     */
    public SelectItem(String name, List<E> settings, List<String> settingsName, int defaultSetting) {
        super(name);
        Preconditions.checkArgument(settings.size() == settingsName.size());
        leftArrow = new Polyline(0, 0, -5, 5, 0, 10);
        rightArrow = new Polyline(0, 0, 5, 5, 0, 10);
        leftArrow.setStroke(Color.WHITE);
        leftArrow.setStrokeWidth(3);
        rightArrow.setStroke(Color.WHITE);
        rightArrow.setStrokeWidth(3);

        property = new SimpleIntegerProperty();
        selectedSetting = Objects.checkIndex(defaultSetting, settings.size());
        this.settings = settings;
        this.settingsName = settingsName;
        currentLabel = new Text(": " + settingsName.get(selectedSetting));
        currentLabel.setFont(Font.font("Arial", FontPosture.ITALIC, 24));
        getChildren().add(0, leftArrow);
        getChildren().add(2, currentLabel);
        getChildren().add(rightArrow);
        setActive(false);
    }

    /**
     * Construit une nouvelle section de sélection dont le réglage par défaut est le premier.
     * 
     * @param name
     *            le nom de la section
     * @param settings
     *            les paramètres pouvant être sélectionnés
     * @param settingsName
     *            les noms des différents paramètres, la liste doit être de la même taille que la liste de paramètres
     */
    public SelectItem(String name, List<E> settings, List<String> settingsName) {
        this(name, settings, settingsName, 0);
    }

    /**
     * Construit une nouvelle section de sélection dont le nom des réglages correspond à la représentation textuelle par
     * défaut des réglages.
     * 
     * @param name
     *            le nom de la section
     * @param settings
     *            les paramètres pouvant être sélectionnés
     * @param defaultSetting
     *            l'indice du réglage par défaut.
     */
    public SelectItem(String name, List<E> settings, int defaultSetting) {
        this(name, settings, toStringArray(settings), defaultSetting);
    }

    private static <E> List<String> toStringArray(List<E> settings) {
        List<String> stringList = new ArrayList<>();
        for (E e : settings) {
            stringList.add(e.toString());
        }
        return stringList;

    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.gui.Item#setActive(boolean)
     */
    @Override
    public void setActive(boolean b) {
        super.setActive(b);
        leftArrow.setVisible(b);
        rightArrow.setVisible(b);
        if (b) {
            updateArrows();
        }
        currentLabel.setFill(b ? Color.WHITE : Color.BLACK);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.gui.Item#handle(javafx.scene.input.KeyEvent)
     */
    @Override
    public void handle(KeyEvent e) {
        if (e.getCode() == KeyCode.RIGHT && selectedSetting < settings.size() - 1) {
            selectedSetting++;
        }
        if (e.getCode() == KeyCode.LEFT && selectedSetting > 0) {
            selectedSetting--;
        }
        activate();
        updateArrows();
        updateText();
        property.set(selectedSetting);
    }

    private void updateText() {
        currentLabel.setText(": " + settingsName.get(selectedSetting));
    }

    private void updateArrows() {
        if (selectedSetting == 0) {
            leftArrow.setVisible(false);
        } else if (selectedSetting == settings.size() - 1) {
            rightArrow.setVisible(false);
        } else {
            leftArrow.setVisible(true);
            rightArrow.setVisible(true);
        }
    }

    /**
     * Retourne le réglage actuellement selectionné.
     * 
     * @return le réglage actuellement selectionné
     */
    public E setting() {
        return settings.get(selectedSetting);
    }
}
