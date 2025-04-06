package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.service.WordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}