package com.example.texttoimageservice.controller;

import com.example.texttoimageservice.service.TextToImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/image/")
public class TextToImageController {
    private final TextToImageService textToImageService;

    @GetMapping("/from/text/")
    public ResponseEntity<Void> getTextToImage(
            @RequestParam String description, HttpServletResponse response
    ) throws IOException {
        textToImageService.getQuestionnaire(
                description,
                response
        );
        return ResponseEntity.ok().build();
    }
}
