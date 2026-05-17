package fr.univrouen.model;

public class GameSettings {
    public enum Difficulty {
        EASY("Facile", 1),
        MEDIUM("Moyen", 2),
        HARD("Difficile", 3);

        private final String label;
        private final int searchDepth;

        Difficulty(String label, int searchDepth) {
            this.label = label;
            this.searchDepth = searchDepth;
        }

        public String getLabel() {
            return label;
        }

        public int getSearchDepth() {
            return searchDepth;
        }
    }

    public enum AiAlgorithm {
        MINIMAX("Minimax"),
        ALPHA_BETA("Alpha-Beta"),
        NEG_ALPHA_BETA("Nega-Alpha-Beta"),
        SSS_STAR("SSS*");

        private final String label;

        AiAlgorithm(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private Difficulty difficulty = Difficulty.MEDIUM;
    private AiAlgorithm aiAlgorithm = AiAlgorithm.NEG_ALPHA_BETA;
    private boolean aiEnabled = true;
    private String aiPlayerId = "p2";

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty cannot be null");
        }
        this.difficulty = difficulty;
    }

    public int getSearchDepth() {
        return difficulty.getSearchDepth();
    }

    public String getDifficultyLabel() {
        return difficulty.getLabel();
    }

    public AiAlgorithm getAiAlgorithm() {
        return aiAlgorithm;
    }

    public void setAiAlgorithm(AiAlgorithm aiAlgorithm) {
        if (aiAlgorithm == null) {
            throw new IllegalArgumentException("aiAlgorithm cannot be null");
        }
        this.aiAlgorithm = aiAlgorithm;
    }

    public String getAiAlgorithmLabel() {
        return aiAlgorithm.getLabel();
    }

    public boolean isAiEnabled() {
        return aiEnabled;
    }

    public void setAiEnabled(boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    public String getAiPlayerId() {
        return aiPlayerId;
    }

    public void setAiPlayerId(String aiPlayerId) {
        if (aiPlayerId == null || aiPlayerId.isBlank()) {
            throw new IllegalArgumentException("aiPlayerId cannot be blank");
        }
        this.aiPlayerId = aiPlayerId;
    }

    public boolean isAiPlayer(String playerId) {
        if (!aiEnabled || playerId == null) return false;
        return playerId.equals(aiPlayerId);
    }
}
