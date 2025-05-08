package com.capstone.group46.pronounceit.controller;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.service.WordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/words")
public class WordController {
    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<?> getWordById(@PathVariable Long wordId) {
        Optional<WordEntity> wordOptional = wordService.getWordById(wordId);

        if (wordOptional.isPresent()) {
            WordEntity word = wordOptional.get();
            String audioURL = word.getAudioURL();

            if (audioURL != null && !audioURL.isEmpty()) {
                try {
                    Path audioPath = Paths.get("src", "main", "resources", "audio", audioURL.substring(7));
                    FileSystemResource fileSystemResource = new FileSystemResource(audioPath);

                    if (fileSystemResource.exists()) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
                        headers.setContentLength(fileSystemResource.contentLength());

                        return new ResponseEntity<>(fileSystemResource, headers, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("Audio file not found", HttpStatus.NOT_FOUND);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error serving audio file");
                }
            } else {
                return new ResponseEntity<>("Audio URL is not set for this word", HttpStatus.NOT_FOUND);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<WordEntity>> getAllWords() {
        List<WordEntity> words = wordService.getAllWords();
        return ResponseEntity.ok(words);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<WordEntity>> getWordsByLessonId(@PathVariable Long lessonId) {
        List<WordEntity> words = wordService.getWordsByLessonId(lessonId);
        return ResponseEntity.ok(words);
    }

    @PostMapping
    public WordEntity createWord(@RequestBody WordEntity word) {
        return wordService.createWord(word);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public WordEntity createWord(
            @RequestPart("word") String wordJson,
            @RequestPart("image") MultipartFile imageFile) throws IOException {
        // Convert the JSON string to a WordEntity object
        ObjectMapper objectMapper = new ObjectMapper();
        WordEntity word = objectMapper.readValue(wordJson, WordEntity.class);

        // Upload the image and set the image URL
        String imageUrl = wordService.uploadImage(imageFile);
        word.setImageURL(imageUrl);

        // Save the WordEntity
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