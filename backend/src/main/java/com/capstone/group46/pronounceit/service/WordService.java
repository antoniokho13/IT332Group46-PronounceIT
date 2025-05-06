package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.repository.WordRepository;
import com.capstone.group46.pronounceit.service.TextToSpeechService;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class WordService {
    private final WordRepository wordRepository;
    private final TextToSpeechService textToSpeechService;

    public WordService(WordRepository wordRepository, TextToSpeechService textToSpeechService) {
        this.wordRepository = wordRepository;
        this.textToSpeechService = textToSpeechService;
    }

    public List<WordEntity> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<WordEntity> getWordById(Long wordId) {
        return wordRepository.findById(wordId);
    }

    public WordEntity createWord(WordEntity word) {
        try {
            byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
            String audioURL = storeAudio(audioContent, word.getWord()); // Implement this method to store audio and get URL
            word.setAudioURL(audioURL);
            word.setCreatedDate(LocalDateTime.now()); // Set the creation date
        } catch (IOException e) {
            // Handle exception appropriately (e.g., log it and set a default audio URL or null)
            e.printStackTrace();
            word.setAudioURL(null); // Or a default audio URL
        }
        return wordRepository.save(word);
    }

    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord) {
        return wordRepository.findById(wordId).map(word -> {
            word.setWord(updatedWord.getWord());
            word.setImageURL(updatedWord.getImageURL());
            try {
                byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                String audioURL = storeAudio(audioContent, word.getWord()); // Implement this method to store audio and get URL
                word.setAudioURL(audioURL);
            } catch (IOException e) {
                // Handle exception appropriately
                e.printStackTrace();
                word.setAudioURL(null); // Or a default audio URL
            }
            return wordRepository.save(word);
        });
    }

    public void deleteWord(Long wordId) {
        wordRepository.deleteById(wordId);
    }

    private String storeAudio(byte[] audioContent, String word) throws IOException {
        File audioDir = ResourceUtils.getFile("classpath:audio/");
        if (!audioDir.exists()) {
            audioDir.mkdirs(); // Create the directory if it doesn't exist
        }

        String fileName = UUID.randomUUID().toString() + "_" + word + ".mp3";
        Path filePath = Paths.get(audioDir.getAbsolutePath(), fileName);

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(audioContent);
        }

        return "/audio/" + fileName; // Return the relative path to access the audio
    }
}