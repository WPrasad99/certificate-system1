package com.certificate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

@Slf4j
@Service
public class PngCertificateGenerator {

    // High-resolution coordinates (2x scale for fast generation)
    // Original template is assumed to be 1024x768, we'll scale to 2048x1536
    private static final double SCALE_FACTOR = 2.0;
    private static final int NAME_CENTER_X = (int) (512 * SCALE_FACTOR); // Center X
    private static final int NAME_CENTER_Y = (int) (410 * SCALE_FACTOR); // Y position for name
    private static final int FONT_SIZE = (int) (48 * SCALE_FACTOR); // Font size for name
    private static final int QR_SIZE = (int) (100 * SCALE_FACTOR); // QR code size
    private static final int QR_MARGIN = (int) (60 * SCALE_FACTOR); // QR margin (Increased to be inside border)

    /**
     * Generate certificate PDF by overlaying participant name and QR code on
     * template
     */
    public byte[] generateCertificatePdf(byte[] templateImage, String participantName, BufferedImage qrCode)
            throws IOException {
        // ... (Existing image generation logic reuse or refactor) ...
        // For simplicity, we keep the image generation logic inside but write to PDF

        // Load template image
        BufferedImage template;
        if (templateImage != null && templateImage.length > 0) {
            template = ImageIO.read(new java.io.ByteArrayInputStream(templateImage));
        } else {
            template = loadDefaultTemplate();
        }

        if (template == null) {
            throw new IOException("Failed to load certificate template");
        }

        // Scale template to high resolution
        int scaledWidth = (int) (template.getWidth() * SCALE_FACTOR);
        int scaledHeight = (int) (template.getHeight() * SCALE_FACTOR);

        BufferedImage scaledTemplate = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2dScale = scaledTemplate.createGraphics();
        g2dScale.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2dScale.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2dScale.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2dScale.drawImage(template, 0, 0, scaledWidth, scaledHeight, null);
        g2dScale.dispose();

        // Create certificate image
        BufferedImage certificate = new BufferedImage(scaledTemplate.getWidth(), scaledTemplate.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = certificate.createGraphics();
        g2d.drawImage(scaledTemplate, 0, 0, null);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Font
        Font nameFont = loadBestFont(FONT_SIZE);
        g2d.setFont(nameFont);
        g2d.setColor(Color.BLACK);

        FontMetrics metrics = g2d.getFontMetrics(nameFont);
        int textWidth = metrics.stringWidth(participantName);
        int x = NAME_CENTER_X - (textWidth / 2);
        int y = NAME_CENTER_Y;

        g2d.drawString(participantName, x, y);

        // QR Code
        if (qrCode != null) {
            int qrX = scaledTemplate.getWidth() - QR_SIZE - QR_MARGIN;
            int qrY = QR_MARGIN;
            g2d.setColor(Color.WHITE);
            g2d.fillRect(qrX - 5, qrY - 5, QR_SIZE + 10, QR_SIZE + 10);
            g2d.drawImage(qrCode, qrX, qrY, QR_SIZE, QR_SIZE, null);
        }

        g2d.dispose();

        // Convert BufferedImage to PDF
        try (PDDocument doc = new PDDocument()) {
            // Create a page with the same aspect ratio as the image
            // PDF units are points (1/72 inch).
            // We'll set the page size to standard A4 landscape or match image ratio
            // Let's match image dimensions converted to points ~
            // but scaled down to fit standard paper if printed?
            // For now, let's make the PDF page size match the aspect ratio of the image
            // typically A4 Landscape is 842 x 595 points.

            float pdfWidth = 842; // A4 Landscape width
            float pdfHeight = (pdfWidth / scaledWidth) * scaledHeight;

            PDPage page = new PDPage(new PDRectangle(pdfWidth, pdfHeight));
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, certificate);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.drawImage(pdImage, 0, 0, pdfWidth, pdfHeight);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);

            log.info("Generated certificate PDF for: {}", participantName);
            return baos.toByteArray();
        }
    }

    /**
     * Load the best available font for the certificate
     */
    private Font loadBestFont(int size) {
        // Try to load elegant script fonts (in order of preference)
        String[] preferredFonts = {
                "Lucida Handwriting",
                "Brush Script MT",
                "Edwardian Script ITC",
                "French Script MT",
                "Monotype Corsiva",
                "Pristina",
                "Segoe Script",
                "Vladimir Script",
                "Mistral",
                "Lucida Calligraphy"
        };

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();

        // Try to find a matching elegant font
        for (String preferredFont : preferredFonts) {
            for (String availableFont : availableFonts) {
                if (availableFont.equalsIgnoreCase(preferredFont)) {
                    log.info("Using font: {}", preferredFont);
                    return new Font(preferredFont, Font.BOLD, size);
                }
            }
        }

        // Fallback to Serif Bold if no script font available
        log.warn("No script font found, using Serif");
        return new Font("Serif", Font.BOLD, size);
    }

    /**
     * Load default template from resources
     */
    private BufferedImage loadDefaultTemplate() {
        try {
            ClassPathResource resource = new ClassPathResource("templates/default_certificate.png");
            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    BufferedImage image = ImageIO.read(is);
                    log.info("Loaded default certificate template");
                    return image;
                }
            }
        } catch (Exception e) {
            log.error("Failed to load default template", e);
        }
        return null;
    }

    /**
     * Get default template as bytes (for frontend preview)
     */
    public byte[] getDefaultTemplate() throws IOException {
        BufferedImage template = loadDefaultTemplate();
        if (template == null) {
            throw new IOException("Default template not found");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(template, "PNG", baos);
        return baos.toByteArray();
    }
}
