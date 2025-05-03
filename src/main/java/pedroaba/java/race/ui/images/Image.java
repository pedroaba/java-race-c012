package pedroaba.java.race.ui.images;

import processing.core.PConstants;
import processing.core.PImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public abstract class Image {
    protected String imagePath;
    private PImage image = null;

    public PImage image() {
        if (this.image == null) {
            try {
                BufferedImage bf = ImageIO.read(new File(this.imagePath));
                this.image = new PImage(bf.getWidth(), bf.getHeight(), PConstants.ARGB);
                bf.getRGB(0, 0, bf.getWidth(), bf.getHeight(), this.image.pixels, 0, bf.getWidth());
                this.image.updatePixels();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }

        return image;
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
