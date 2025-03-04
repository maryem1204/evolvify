package tn.esprit.Utils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

public class SimpleCaptcha {
    private String correctAnswer;
    private BufferedImage captchaImage;

    public SimpleCaptcha() {
        generateCaptcha();
    }

    private void generateCaptcha() {
        // Generate a random 5-character string
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        correctAnswer = sb.toString();

        // Create image
        captchaImage = new BufferedImage(200, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = captchaImage.createGraphics();

        // Set background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 80);

        // Draw distortion lines
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(200);
            int y1 = random.nextInt(80);
            int x2 = random.nextInt(200);
            int y2 = random.nextInt(80);
            g.drawLine(x1, y1, x2, y2);
        }

        // Draw text
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < correctAnswer.length(); i++) {
            // Rotate each character slightly
            double rotation = -0.2 + random.nextDouble() * 0.4;
            AffineTransform originalTransform = g.getTransform();
            g.rotate(rotation, 40 + i * 30, 40);
            g.drawString(correctAnswer.substring(i, i+1), 30 + i * 30, 50);
            g.setTransform(originalTransform);
        }

        g.dispose();
    }

    public BufferedImage getImage() {
        return captchaImage;
    }

    public boolean verify(String userAnswer) {
        return correctAnswer.equalsIgnoreCase(userAnswer);
    }

    public void refresh() {
        generateCaptcha();
    }
}