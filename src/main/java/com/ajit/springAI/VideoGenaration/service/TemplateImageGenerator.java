package com.ajit.springAI.VideoGenaration.service;

import com.ajit.springAI.VideoGenaration.model.GeneratedImage;
import com.ajit.springAI.VideoGenaration.model.PanelPrompt;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import javax.imageio.ImageIO;

public class TemplateImageGenerator implements ImageGenerator {

    private final Path baseDirectory;

    public TemplateImageGenerator() {
        this(Path.of("generated", "images"));
    }

    public TemplateImageGenerator(Path baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public GeneratedImage generate(PanelPrompt prompt) {
        String sceneText = prompt != null && prompt.getScene() != null && !prompt.getScene().isBlank()
                ? prompt.getScene()
                : "Comic panel";
        String safeName = sceneText.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (safeName.isBlank()) {
            safeName = "panel";
        }

        try {
            Files.createDirectories(baseDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create template image directory", e);
        }

        Path svgPath = baseDirectory.resolve(safeName + ".svg");
        String svgContent = buildSvgTemplate(sceneText, prompt != null ? prompt.getStyle() : "comic");
        try {
            Files.writeString(svgPath, svgContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write SVG panel", e);
        }

        Path pngPath = baseDirectory.resolve(safeName + ".png");
        BufferedImage image = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setPaint(new GradientPaint(0, 0, new Color(42, 63, 97), 1280, 720, new Color(20, 24, 34)));
        graphics.fill(new RoundRectangle2D.Double(20, 20, 1240, 680, 32, 32));

        graphics.setPaint(new Color(255, 202, 102));
        graphics.setStroke(new BasicStroke(6f));
        graphics.draw(new RoundRectangle2D.Double(80, 80, 1120, 500, 30, 30));

        graphics.setPaint(new Color(255, 255, 255));
        graphics.setFont(new Font("Arial", Font.BOLD, 38));
        graphics.drawString("Comic Panel", 120, 150);

        graphics.setFont(new Font("Arial", Font.PLAIN, 26));
        String wrapped = wrapText(sceneText, 26, 30);
        int y = 240;
        for (String line : wrapped.split("\\n")) {
            graphics.drawString(line, 120, y);
            y += 36;
        }

        graphics.dispose();

        try {
            ImageIO.write(image, "png", pngPath.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write PNG panel", e);
        }

        return new GeneratedImage(sceneText, pngPath.toString());
    }

    private String buildSvgTemplate(String sceneText, String style) {
        String displayStyle = style == null ? "comic" : style;
        String safeText = sceneText.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1280\" height=\"720\">"
                + "<rect width=\"1280\" height=\"720\" rx=\"32\" fill=\"#1a2438\"/>"
                + "<rect x=\"90\" y=\"90\" width=\"1100\" height=\"540\" rx=\"28\" fill=\"#2b3d59\" stroke=\"#f5c46f\" stroke-width=\"6\"/>"
                + "<text x=\"130\" y=\"160\" font-size=\"38\" font-family=\"Arial\" font-weight=\"bold\" fill=\"#f5c46f\">"
                + displayStyle.toUpperCase(Locale.ROOT) + " PANEL</text>"
                + "<text x=\"130\" y=\"250\" font-size=\"26\" font-family=\"Arial\" fill=\"#ffffff\">"
                + safeText + "</text>"
                + "</svg>";
    }

    private String wrapText(String text, int maxCharsPerLine, int maxLines) {
        String[] words = text.split(" ");
        StringBuilder builder = new StringBuilder();
        int lineCount = 0;
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine && currentLine.length() > 0) {
                builder.append(currentLine).append(System.lineSeparator());
                currentLine = new StringBuilder();
                lineCount++;
                if (lineCount >= maxLines) {
                    break;
                }
            }
            if (currentLine.length() > 0) {
                currentLine.append(' ');
            }
            currentLine.append(word);
        }

        if (currentLine.length() > 0) {
            builder.append(currentLine);
        }
        return builder.toString();
    }
}
