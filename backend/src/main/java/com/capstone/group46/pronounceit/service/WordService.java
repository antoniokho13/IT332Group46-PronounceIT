package com.capstone.group46.pronounceit.service;

import com.capstone.group46.pronounceit.entity.WordEntity;
import com.capstone.group46.pronounceit.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WordService {
    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public List<WordEntity> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<WordEntity> getWordById(Long wordId) {
        return wordRepository.findById(wordId);
    }

    public WordEntity createWord(WordEntity word) {
        return wordRepository.save(word);
    }

    public Optional<WordEntity> updateWord(Long wordId, WordEntity updatedWord) {
        return wordRepository.findById(wordId).map(word -> {
            word.setWord(updatedWord.getWord());
            word.setImageURL(updatedWord.getImageURL());
            word.setAudioURL(updatedWord.getAudioURL());
            return wordRepository.save(word);
        });
    }

    public void deleteWord(Long wordId) {
        wordRepository.deleteById(wordId);
    }
}