package org.example;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Random;

public class MainProjectTest {
    final int MAXINPUT = 1023;
    final int MININPUT = 0;
    final int ERRORVALUE = -1000;
    final int MINRESULT = 0;
    final int MAXRESULT = 100;

    @Test
    public void testOutOfBounds() {
        assertEquals(ERRORVALUE, MainProject.normalizeYValue(MAXINPUT + 1));
        assertEquals(ERRORVALUE, MainProject.normalizeYValue(-1));
    }

    @Test
    public void testBoundaryValues() {
        assertEquals(0, MainProject.normalizeYValue(MININPUT));
        assertTrue(MainProject.normalizeYValue(MAXINPUT) <= MAXRESULT);
    }

    @Test
    public void testRandomValidValues() {
        for (int i = 0; i < 100; i++) {
            int rand = new Random().nextInt(MAXINPUT + 1);
            int result = MainProject.normalizeYValue(rand);
            assertTrue(\"Value out of range: \" + result, result >= MINRESULT && result <= MAXRESULT);
        }
    }
}
