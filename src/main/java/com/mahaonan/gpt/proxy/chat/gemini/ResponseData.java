package com.mahaonan.gpt.proxy.chat.gemini;

import lombok.Data;

import java.util.List;

/**
 * @author mahaonan
 */
@Data
public class ResponseData {
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
        private int index;
        private List<SafetyRating> safetyRatings;

    }

    @Data
    public static class Content {
        private List<Part> parts;
        private String role;

        // Getter and Setter methods

        @Override
        public String toString() {
            return "Content{" +
                    "parts=" + parts +
                    ", role='" + role + '\'' +
                    '}';
        }
    }

    @Data
    public static class Part {
        private String text;
    }

    @Data
    public static class SafetyRating {
        private String category;
        private String probability;
    }

    @Data
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;

    }

}

