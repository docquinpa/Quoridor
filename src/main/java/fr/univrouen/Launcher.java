package fr.univrouen;

/**
 * Point d'entrée de contournement pour exécuter l'application JavaFX sous forme de JAR autonome ("fat JAR").
 * 
 * Puisque la classe principale {@link QuoridorApp} hérite directement de {@link javafx.application.Application},
 * tenter de l'exécuter directement depuis un JAR sans configurer le module-path génère une exception JavaFX.
 * Cette classe Launcher résout ce problème en démarrant le programme sans hériter de Application.
 */
public final class Launcher {
    private Launcher() {
        // Classe utilitaire de démarrage. Instanciation interdite.
    }

    /**
     * Lance l'application en transmettant les arguments à QuoridorApp.
     * 
     * @param args Arguments passés en ligne de commande.
     */
    public static void main(String[] args) {
        QuoridorApp.main(args);
    }
}
