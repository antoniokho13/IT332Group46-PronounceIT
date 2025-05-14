package com.capstone.group46.pronounceit.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.capstone.group46.pronounceit.entity.LessonEntity;
import com.capstone.group46.pronounceit.entity.UserEntity;
import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.repository.LessonRepository;
import com.capstone.group46.pronounceit.repository.UserRepository;
import com.capstone.group46.pronounceit.repository.WordRepository;

@Service
public class WordService {
    private final WordRepository wordRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final TextToSpeechService textToSpeechService;

    public WordService(WordRepository wordRepository, LessonRepository lessonRepository, UserRepository userRepository, TextToSpeechService textToSpeechService) {
        this.wordRepository = wordRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.textToSpeechService = textToSpeechService;
    }

    public List<WordEntity> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<WordEntity> getWordById(Long wordId) {
        return wordRepository.findById(wordId);
    }

    public WordEntity createWord(WordEntity word) {
        // Fetch the LessonEntity from the database
        Long lessonId = word.getLesson().getLessonId();
        LessonEntity lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with ID " + lessonId + " not found"));

        // Fetch the UserEntity from the database
        Long userId = word.getCreatedBy().getId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));

        // Set the managed entities
        word.setLesson(lesson);
        word.setCreatedBy(user);

        // Generate audio for the word using Text-to-Speech
        try {
            byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
            String audioURL = storeAudio(audioContent, word.getWord());
            word.setAudioURL(audioURL);
        } catch (IOException e) {
            // Handle exception appropriately (e.g., log it and set a default audio URL or null)
            e.printStackTrace();
            word.setAudioURL(null); // Or a default audio URL
        }

        // Set the creation date
        word.setCreatedDate(LocalDateTime.now());

        // Save the WordEntity
        return wordRepository.save(word);
    }

    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord) {
        return wordRepository.findById(wordId).map(word -> {
            word.setWord(updatedWord.getWord());
            word.setImageURL(updatedWord.getImageURL());
            try {
                byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                String audioURL = storeAudio(audioContent, word.getWord());
                word.setAudioURL(audioURL);
            } catch (IOException e) {
                // Handle exception appropriately
                e.printStackTrace();
                word.setAudioURL(null); // Or a default audio URL
            }
            return wordRepository.save(word);
        });
    }

    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord, MultipartFile imageFile) {
        return wordRepository.findById(wordId).map(word -> {
            // Check if the word has been updated
            boolean isWordUpdated = !word.getWord().equals(updatedWord.getWord());

            // Update the word field
            word.setWord(updatedWord.getWord());

            // Update the image if provided
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String imageUrl = uploadImage(imageFile);
                    word.setImageURL(imageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Generate new audio only if the word has been updated
            if (isWordUpdated) {
                try {
                    byte[] audioContent = textToSpeechService.synthesizeText(word.getWord());
                    String audioURL = storeAudio(audioContent, word.getWord());
                    word.setAudioURL(audioURL);
                } catch (IOException e) {
                    e.printStackTrace();
                    word.setAudioURL(null); // Or retain the existing audio URL
                }
            }

            return wordRepository.save(word);
        });
    }

    public void deleteWord(Long wordId) {
        wordRepository.deleteById(wordId);
    }

    private String storeAudio(byte[] audioContent, String word) throws IOException {
        // Define the directory to store audio files (relative to the backend folder)
        Path audioDirPath = Paths.get("backend", "src", "main", "resources", "audio");
        File audioDir = audioDirPath.toFile();

        // Create the directory if it doesn't exist
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }

        // Generate a unique file name
        String fileName = UUID.randomUUID().toString() + "_" + word + ".mp3";
        Path filePath = audioDirPath.resolve(fileName);

        // Save the file to the directory
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(audioContent);
        }

        // Return the relative path to access the audio
        return "/audio/" + fileName;
    }

    public byte[] synthesizeAudioForWord(WordEntity word) throws IOException {
        return textToSpeechService.synthesizeText(word.getWord());
    }

    public String uploadImage(MultipartFile imageFile) throws IOException {
        // Define the directory to store images (relative to the backend folder)
        Path imageDirPath = Paths.get("backend", "src", "main", "resources", "images");
        File imageDir = imageDirPath.toFile();

        // Create the directory if it doesn't exist
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // Generate a unique file name
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        Path filePath = imageDirPath.resolve(fileName);

        // Save the file to the directory
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageFile.getBytes());
        }

        // Return the relative path to access the image
        return "/images/" + fileName;
    }

    public List<WordEntity> getWordsByLessonId(Long lessonId) {
        return wordRepository.findAll().stream()
                .filter(word -> word.getLesson().getLessonId().equals(lessonId))
                .toList();
    }
}