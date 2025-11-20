package com.auth;

import static org.junit.Assert.*;

import java.awt.Frame;
import java.util.Arrays;

import org.junit.Test;

public class AppMainTest {

    @Test
    public void testAppMainStartsFrame() throws Exception {
        // Call main; it uses invokeLater, so give EDT a moment
        App.main(new String[0]);

        // Wait briefly for the EDT to create frame
        Thread.sleep(200);

        // Find AuthFrame instances among frames and dispose them
        Frame[] frames = Frame.getFrames();
        boolean found = false;
        for (Frame f : frames) {
            if (f instanceof AuthFrame) {
                found = true;
                f.dispose();
            }
        }

        assertTrue("AuthFrame should have been created by App.main", found);
    }
}
