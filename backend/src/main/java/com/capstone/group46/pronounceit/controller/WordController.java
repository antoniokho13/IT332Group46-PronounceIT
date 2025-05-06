package com.capstone.group46.pronounceit.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.service.WordService;

@RestController
@RequestMapping("/api/words")
public class WordController {
    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<WordEntity> getWordById(@PathVariable Long wordId) {
        return wordService.getWordById(wordId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<WordEntity>> getAllWords() {
        List<WordEntity> words = wordService.getAllWords();
        return ResponseEntity.ok(words);
    }

    @PostMapping
    public WordEntity createWord(@RequestBody WordEntity word) {
        return wordService.createWord(word);
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<WordEntity> updateWord(@PathVariable Long wordId, @RequestBody WordEntity updatedWord) {
        return wordService.updateWord(wordId, updatedWord)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long wordId) {
        wordService.deleteWord(wordId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/audio/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getAudioFile(@PathVariable String filename) throws IOException {
        try {
            Path audioPath = Paths.get("src", "main", "resources", "audio", filename);
            FileSystemResource fileSystemResource = new FileSystemResource(audioPath);

            if (!fileSystemResource.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/mpeg"))
                    .body(fileSystemResource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}