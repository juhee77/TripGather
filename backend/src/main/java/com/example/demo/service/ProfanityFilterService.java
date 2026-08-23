package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class ProfanityFilterService {

    private final Set<String> forbiddenWords = new CopyOnWriteArraySet<>(Arrays.asList(
            "바보", "멍청이", "개새끼", "씨발", "존나", "fuck", "shit"
    ));

    public void validateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String normalizedText = text.toLowerCase().replaceAll("[^a-zA-Z0-9가-힣]", "");
        for (String word : forbiddenWords) {
            if (normalizedText.contains(word.toLowerCase())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어(" + word + ")가 포함되어 있습니다.");
            }
        }
    }

    public void addProfanityWord(String word) {
        if (word != null && !word.trim().isEmpty()) {
            forbiddenWords.add(word.trim().toLowerCase());
        }
    }

    public Set<String> getForbiddenWords() {
        return Set.copyOf(forbiddenWords);
    }
}
