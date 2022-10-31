package com.example.texttoimageservice.controller;

import com.example.texttoimageservice.dto.ChatIdDescriptionDto;
import com.example.texttoimageservice.service.TextToImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/image/")
public class TextToImageController {
    private final TextToImageService textToImageService;

    @PostMapping("/from/text/")
    public ResponseEntity<Void> getTextToImage(
            @RequestBody ChatIdDescriptionDto chatIdDescriptionDto, HttpServletResponse response
    ) throws IOException {
        textToImageService.getQuestionnaire(
                chatIdDescriptionDto.getChatId(),
                chatIdDescriptionDto.getDescription(),
                response
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/")
    public String test(
            @RequestBody ChatIdDescriptionDto chatIdDescriptionDto
            ) throws IOException {
        return  chatIdDescriptionDto.getDescription()+" "+chatIdDescriptionDto.getChatId();
    }

}
