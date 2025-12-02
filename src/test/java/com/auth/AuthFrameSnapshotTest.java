package com.auth;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

/**
 * Snapshot-style test for the AuthFrame UI.
 *
 * Usage:
 * - To create the baseline image: mvn -DcreateSnapshot=true -Dtest=AuthFrameSnapshotTest test
 * - To run comparison: mvn -Dtest=AuthFrameSnapshotTest test
 *
 * The test will write actual and diff images to `target/snapshots/` on mismatch to help debugging.
 */
public class AuthFrameSnapshotTest extends AssertJSwingJUnitTestCase {

    @Override
    protected void onSetUp() {
        // no special setup required; tests will create frames themselves
    }


    @Test
    public void snapshotMatchesBaseline_orCreatesIt() throws Exception {
        // stub service to produce deterministic UI text
        AuthService stub = new AuthService() {
            @Override
            public String login(String email, String password) {
                return "Login exitoso 🎉";
            }

            @Override
            public String recoverPassword(String email) {
                return "Se ha enviado un email de recuperación (simulado).";
            }
        };

        // create frame on EDT
        AuthFrame frame = GuiActionRunner.execute(() -> {
            AuthFrame f = new AuthFrame(stub);
            f.setSize(520, 420);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });

        // show and let EDT paint
        GuiActionRunner.execute(() -> frame.setVisible(true));
        pause(300);

        BufferedImage actual = captureComponent(frame);

        Path baseline = Path.of("src", "test", "resources", "snapshots", "AuthFrame_baseline.png");
        if (System.getProperty("createSnapshot") != null || !Files.exists(baseline)) {
            // create baseline
            Files.createDirectories(baseline.getParent());
            ImageIO.write(actual, "png", baseline.toFile());
            throw new AssertionError("Baseline created at: " + baseline + ". Re-run test without -DcreateSnapshot=true to compare.");
        }

        BufferedImage expected = ImageIO.read(baseline.toFile());

        ComparisonResult result = compareImages(expected, actual);
        double maxAllowedPercent = 1.0; // allow up to 1% pixels to differ
        if (result.percentDifferent > maxAllowedPercent) {
            Path outDir = Path.of("target", "snapshots");
            Files.createDirectories(outDir);
            ImageIO.write(actual, "png", outDir.resolve("AuthFrame_actual.png").toFile());
            ImageIO.write(result.diff, "png", outDir.resolve("AuthFrame_diff.png").toFile());
            throw new AssertionError(String.format("Snapshot mismatch: %.2f%% pixels differ (allowed %.2f%%). Actual and diff written to %s",
                    result.percentDifferent, maxAllowedPercent, outDir.toAbsolutePath()));
        }

        // success
        assertTrue(true);
    }

    // small helper to let EDT settle
    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    private BufferedImage captureComponent(Component c) {
        int w = Math.max(1, c.getWidth());
        int h = Math.max(1, c.getHeight());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        // paint background
        g.setColor(c.getBackground() == null ? Color.WHITE : c.getBackground());
        g.fillRect(0, 0, w, h);
        c.paintAll(g);
        g.dispose();
        return img;
    }

    private static class ComparisonResult {
        final double percentDifferent;
        final BufferedImage diff;

        ComparisonResult(double percentDifferent, BufferedImage diff) {
            this.percentDifferent = percentDifferent;
            this.diff = diff;
        }
    }

    private ComparisonResult compareImages(BufferedImage expected, BufferedImage actual) {
        int w = Math.max(expected.getWidth(), actual.getWidth());
        int h = Math.max(expected.getHeight(), actual.getHeight());
        BufferedImage diff = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        long total = 0;
        long diffCount = 0;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int exp = (x < expected.getWidth() && y < expected.getHeight()) ? expected.getRGB(x, y) : 0xFFFFFFFF;
                int act = (x < actual.getWidth() && y < actual.getHeight()) ? actual.getRGB(x, y) : 0xFFFFFFFF;
                total++;
                if (exp != act) {
                    diffCount++;
                    // mark differences red
                    diff.setRGB(x, y, 0xFFFF0000);
                } else {
                    // copy expected pixel as translucent green-ish to indicate match
                    int a = (exp >> 24) & 0xFF;
                    int r = (exp >> 16) & 0xFF;
                    int g = (exp >> 8) & 0xFF;
                    int b = exp & 0xFF;
                    diff.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }

        double percent = 100.0 * diffCount / (double) total;
        return new ComparisonResult(percent, diff);
    }
}
