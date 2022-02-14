/**
* Convertisseur d'image Game Boy en image JavaFX.
*
*@author Vignoud Julien (282142)
*@author Benhaim Julien (284558)
*/

package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {

	/**
	 * Convertit l'image gameboy en une image JavaFX.
	 * 
	 * @param img
	 *            l'image gameboy
	 * @throws NullPointerException si l'image gameboy est nulle
	 * @return l'image convertie
	 */
	public static Image convert(LcdImage img, int[] palette) {
	    Objects.requireNonNull(img);
		WritableImage fxImage = new WritableImage(img.width(), img.height());
		PixelWriter writer = fxImage.getPixelWriter();
		for (int y = 0; y < img.height(); ++y) {
			for (int x = 0; x < img.width(); ++x) {
				writer.setArgb(x, y, palette[img.get(x, y)]);
			}
		}
		return fxImage;
	}
}
