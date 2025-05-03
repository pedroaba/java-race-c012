package pedroaba.java.race.ui.fonts;

import processing.core.PFont;

import java.awt.*;
import java.io.File;

public abstract class AbstractFont {
    protected String fontPath;
    private PFont font = null;

    public Integer fontSize = 12;
    public Boolean isSmooth = true;

    public PFont font() {
        if (font == null) {
            try {
                Font awtFont = Font.createFont(Font.TRUETYPE_FONT, new File(this.fontPath))
                        .deriveFont(Font.PLAIN, this.fontSize);

                this.font = new PFont(awtFont, this.isSmooth);
            } catch (Exception e) {
                e.fillInStackTrace();
            }
        }

        return font;
    }
}
