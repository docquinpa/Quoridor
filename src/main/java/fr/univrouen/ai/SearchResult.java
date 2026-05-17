package fr.univrouen.ai;

/**
 * Représente le résultat d'une rechercheAlpha-Bêta.
 * 
 * Permet de retourner simultanément le coup recommandé et l'évaluation associée.
 */
public record SearchResult(Move move, int score) {
}
