package com.rjgc.nzy.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.security.SecureRandom;

@Service
public class CaptchaService {

    public static final String SESSION_KEY = "LOGIN_CAPTCHA";

    private static final String CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 42;
    private final SecureRandom random = new SecureRandom();

    public BufferedImage createImage(HttpSession session) {
        String code = randomCode();
        session.setAttribute(SESSION_KEY, code);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(245, 248, 255));
            graphics.fillRect(0, 0, WIDTH, HEIGHT);

            for (int i = 0; i < 12; i++) {
                graphics.setColor(randomSoftColor());
                int x1 = random.nextInt(WIDTH);
                int y1 = random.nextInt(HEIGHT);
                int x2 = random.nextInt(WIDTH);
                int y2 = random.nextInt(HEIGHT);
                graphics.drawLine(x1, y1, x2, y2);
            }

            graphics.setFont(new Font("Arial", Font.BOLD, 26));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(randomTextColor());
                int y = 29 + random.nextInt(6);
                graphics.drawString(String.valueOf(code.charAt(i)), 16 + i * 24, y);
            }
        } finally {
            graphics.dispose();
        }
        return image;
    }

    public boolean validate(HttpSession session, String input) {
        Object expected = session.getAttribute(SESSION_KEY);
        session.removeAttribute(SESSION_KEY);
        return expected != null
                && input != null
                && !input.isBlank()
                && expected.toString().equalsIgnoreCase(input.trim());
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return builder.toString();
    }

    private Color randomSoftColor() {
        return new Color(120 + random.nextInt(100), 120 + random.nextInt(100), 120 + random.nextInt(100));
    }

    private Color randomTextColor() {
        return new Color(random.nextInt(90), random.nextInt(90), random.nextInt(120));
    }
}
