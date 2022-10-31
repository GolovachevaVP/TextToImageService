package com.example.texttoimageservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TextToImageService {
    private static final int HEADER = 0;
    private static final int DESCRIPTION = 1;
    private static final int FONT_HEADER_SIZE = 48;
    private static final int FONT_DESCRIPTION_SIZE = 40;
    private static final int REDUCTION_FONT = 1;
    private static final int INDENTATION_RIGHT = 100;
    private static final int INDENTATION_LEFT = 50;
    private static final int INDENTATION_TOP = 30;
    private static final int INDENTATION_BOTTOM = 30;

    public void getQuestionnaire(long chatId, String description, HttpServletResponse response) throws IOException {
        int fontHeaderSize = FONT_HEADER_SIZE;
        int fontDescriptionSize = FONT_DESCRIPTION_SIZE;
        boolean textDoesNotFit = true;
        while (textDoesNotFit) {
            textDoesNotFit = saveQuestionnaire(chatId, description,
                    new Font("Old Standard TT", Font.BOLD, fontHeaderSize),
                    new Font("Old Standard TT", Font.PLAIN, fontDescriptionSize));

            fontDescriptionSize -= REDUCTION_FONT;
            fontHeaderSize -= REDUCTION_FONT;
        }
        saveFileToResponse(chatId, response);
    }
    public boolean saveQuestionnaire(long chatId, String description, Font fontHeader, Font fontDescription) throws IOException {
        BufferedImage image = ImageIO.read(new File("src/main/resources/prerev-background.jpg"));

        List<String> text = changeDescription(description);

        List<String> linesHeader = addHeaderToImage(text, fontHeader, image);
        List<String> linesDescription = addDescriptionToImage(text, fontDescription, image);

        Graphics g = image.createGraphics();
        g.setFont(fontDescription);
        final FontMetrics fontMetricsDescription = g.getFontMetrics();
        int lineHeightDescription = fontMetricsDescription.getHeight();

        g.setFont(fontHeader);
        final FontMetrics fontMetricsHeader = g.getFontMetrics();
        int lineHeightHeader = fontMetricsHeader.getHeight();

        int linesHeight = lineHeightHeader * linesHeader.size() + lineHeightDescription * linesDescription.size();
        if (linesHeight > image.getHeight() - lineHeightDescription - INDENTATION_BOTTOM) {
            log.error("текст больше картинки");
            return true;
        }

        for (int i = 0; i < linesHeader.size(); i++) {
            g.setColor(Color.BLACK);
            g.setFont(fontHeader);
            g.drawString(linesHeader.get(i), INDENTATION_LEFT, (INDENTATION_TOP + lineHeightHeader + i * lineHeightHeader));
        }
        int indent = 35 + linesHeader.size() * lineHeightHeader;
        for (int i = 0; i < linesDescription.size(); i++) {
            g.setColor(Color.BLACK);
            g.setFont(fontDescription);
            g.drawString(linesDescription.get(i), INDENTATION_LEFT, (indent + lineHeightDescription +
                    i * lineHeightDescription));
        }


        g.dispose();
        ImageIO.write(image, "png",
                new File("src/main/resources/questionnaires/questionnaire" +
                        chatId + ".png"));

        return false;
    }


    public List<String> addHeaderToImage(List<String> text, Font fontHeader, BufferedImage image) {
        Graphics gHeader = image.createGraphics();
        gHeader.setFont(fontHeader);
        final FontMetrics fontMetricsHeader = gHeader.getFontMetrics();

        List<String> lines = new ArrayList<>();
        String[] words = text.get(HEADER).split(" ");
        String line = "";

        for (String word : words) {
            if (fontMetricsHeader.stringWidth(line + word) > image.getWidth() - INDENTATION_RIGHT) {
                lines.add(line);
                line = "";
            }
            line += word + " ";
        }
        lines.add(line);
        return lines;
    }

    public List<String> addDescriptionToImage(List<String> text, Font fontDescription, BufferedImage image) {
        Graphics gDescription = image.createGraphics();
        gDescription.setFont(fontDescription);
        final FontMetrics fontMetricsDescription = gDescription.getFontMetrics();

        List<String> lines = new ArrayList<>();
        String[] words = text.get(DESCRIPTION).split(" ");
        String line = "";

        for (String word : words) {
            if (fontMetricsDescription.stringWidth(line + word) > image.getWidth() - INDENTATION_RIGHT) {
                lines.add(line);
                line = "";
            }
            line += word + " ";
        }
        lines.add(line);
        return lines;
    }


    public static List<String> changeDescription(String text) {
        String[] descriptionMultiLine = text.split("\\. ");
        String heading;
        String description = "";
        List<String> result = new ArrayList<>();
        if (descriptionMultiLine.length > 1) {
            heading = descriptionMultiLine[HEADER] + ".";
            result.add(heading);
            for (int i = 1; i < descriptionMultiLine.length; i++) {
                description += descriptionMultiLine[i];
                if (!descriptionMultiLine[i].contains(".") &&!descriptionMultiLine[i].contains("!")) {
                    description += ". ";
                }
            }
            result.add(description);
        } else {
            String[] descriptionOneLine = text.split(" ");
            heading = descriptionOneLine[HEADER];
            result.add(heading);
            if (descriptionOneLine.length > 1) {
                for (int i = 1; i < descriptionOneLine.length; i++) {
                    description += descriptionOneLine[i] + " ";
                }
            }
            result.add(description);
        }
        return result;
    }

    private void saveFileToResponse(long chatId, HttpServletResponse response) throws IOException {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_TYPE, "image/png");
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questionnaire"+
//                chatId + ".png");
        response.setContentType("image/png");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=questionnaire"+
                chatId + ".png");
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
//        File file = new File(getClass().getClassLoader().getResource("questionnaires/questionnaire" +
//                 chatId + ".png").getFile());
//        InputStreamResource resource = new InputStreamResource(Files.newInputStream(file.toPath()));
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentLength(file.length())
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//    }
        Path path = Paths.get("src/main/resources/questionnaires/questionnaire" +chatId + ".png");
        BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path));
        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
            byte[] buffer = new byte[10240];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();


    }
}
