package com.example.demo.service;

import com.example.demo.exception.CustomException;
import com.example.demo.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProfanityFilterService {

    private static final List<String> FORBIDDEN_WORDS = Arrays.asList(
            "바보", "멍청이", "개새끼", "씨발", "존나", "fuck", "shit"
    );

    public void validateText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String lowerText = text.toLowerCase().replaceAll("\\s+", "");
        for (String word : FORBIDDEN_WORDS) {
            if (lowerText.contains(word.toLowerCase())) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "부적절한 단어(" + word + ")가 포함되어 있습니다.");
            }
        }
    }
}
