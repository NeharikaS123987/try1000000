package org.example;

import org.firmata4j.ssd1306.MonochromeCanvas;
import org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.ssd1306.InvalidDimensionException;

public class ShapeRenderer {

    boolean[][] screen;
    SSD1306 oledDisplay;

    public ShapeRenderer(SSD1306 oledDisplay, int width, int height) {
        if (width < 1 || height < 1) {
            throw new InvalidDimensionException("Dimensions must be greater than 0");
        }
        if (width % 2 == 0) width--;
        if (height % 2 == 0) height--;

        this.oledDisplay = oledDisplay;
        screen = new boolean[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                screen[i][j] = false;
            }
        }
    }

    public void drawTextStatus(String line1, String line2) {
        oledDisplay.clear();
        oledDisplay.getCanvas().drawString(0, 0, line1);
        oledDisplay.getCanvas().drawString(0, 16, line2);
        oledDisplay.display();
    }
}
