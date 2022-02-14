package ch.epfl.gameboj.gui.menu;

/**
 * Représente une section de menu pointant vers un autre menu.
 *
 * @author Vignoud Julien (282142)
 * @author Benhaim Julien (284558)
 */

public class MenuItem extends BasicItem {
    private final Menu nextMenu;

    /**
     * Construit une nouvelle section de menu référençant un autre menu.
     * @param name le nom de la section
     * @param nextMenu le menu référencé par la section
     */
    public MenuItem(String name, Menu nextMenu) {
        super(name);
        this.nextMenu = nextMenu;
    }
    
    /**
     * Retourne le menu référencé par la section.
     * @return le menu référencé par la section
     */
    public Menu nextMenu() {
        return nextMenu;
    }

}
