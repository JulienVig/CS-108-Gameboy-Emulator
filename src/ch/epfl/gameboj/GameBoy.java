/**
* Simule la console GameBoy
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj;

import java.io.File;
import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class GameBoy {
    private final Bus bus;
    private final Cpu cpu;
    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;
    private final Cartridge cartridge;
    private long totalCycles = 0;

    /**
     * Nombre de cycle que le GameBoy effectue chaque seconde
     */
    public static final long CYCLES_PER_SECOND = (long) Math.pow(2, 20);
    /**
     * Nombre de cycle que le GameBoy effectue chaque nanoseconde
     */
    public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND * Math.pow(10, -9);

    /**
     * Construit un nouveau GameBoy, crée un bus, un processeur, ainsi que de la mémoire vive et attache tous les
     * composants au Bus
     * 
     * @param cartridge
     *            la cartouche
     * @throws NullPointerException
     *             si la cartouche est nulle
     */
    public GameBoy(Cartridge cartridge) {
        this.cartridge = cartridge;
        bus = new Bus();
        cpu = new Cpu();
        cpu.attachTo(bus);
        Ram workRAM = new Ram(AddressMap.WORK_RAM_SIZE);
        new RamController(workRAM, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END).attachTo(bus);
        new RamController(workRAM, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END).attachTo(bus);
        new BootRomController(Objects.requireNonNull(cartridge)).attachTo(bus);
        timer = new Timer(cpu);
        timer.attachTo(bus);
        lcdController = new LcdController(cpu);
        lcdController.attachTo(bus);
        joypad = new Joypad(cpu);
        joypad.attachTo(bus);
    }

    /**
     * Retourne le composant Bus du GameBoy.
     * 
     * @return le Bus du GameBoy
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Retourne le composant Cpu du GameBoy.
     * 
     * @return le processeur du GameBoy
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Retourne le composant Timer du GameBoy.
     * 
     * @return le minuteur du GameBoy
     */
    public Timer timer() {
        return timer;
    }

    /**
     * Retourne le contrôleur lcd du GameBoy.
     * 
     * @return le contrôleur lcd du GameBoy
     */
    public LcdController lcdController() {
        return lcdController;
    }

    /**
     * Retourne le clavier du GameBoy.
     * 
     * @return le clavier du GameBoy
     */
    public Joypad joypad() {
        return joypad;
    }

    /**
     * Simule le fonctionnement du GameBoy jusqu'au cycle donné moins 1.
     * 
     * @param cycle
     *            nombre de cycle à simuler
     * @throws IllegalArgumentException
     *             si un nombre (strictement) supérieur de cycles a déjà été simulé
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(totalCycles <= cycle);
        for (long i = totalCycles; i < cycle; i++) {
            timer.cycle(i);
            cpu.cycle(i);
            lcdController.cycle(i);
        }
        totalCycles = cycle;
    }

    /**
     * Retourne le nombre de cycles déjà simulés.
     * 
     * @return le nombre de cycles déjà simulés
     */
    public long cycles() {
        return totalCycles;
    }

    /**
     * Sauvegarde le contenu de la mémoire vive de la cartouche dans le fichier spécifié
     * 
     * @param file
     *            le fichier dans lequel sauvegarder la mémoire
     */
    public void saveCatridgeRam(File file) {
        cartridge.saveRam(file);
    }

    /**
     * Charge le contenu du fichier spécifié dans la mémoire vive de la cartouche.
     * 
     * @param file
     *            le fichier depuis lequel charger la mémoire
     */
    public void loadCatridgeRam(File file) {
        cartridge.loadRam(file);
    }
}
