package tn.esprit.Utils;

import tn.esprit.Entities.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private static Utilisateur utilisateurConnecte;

    private SessionManager() {} // Constructeur privé pour le Singleton

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUtilisateurConnecte(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public void logout() {
        utilisateurConnecte = null; // Déconnecter l'utilisateur
    }

    /*******Cette classe stockera l'utilisateur actuellement connecté et permettra d'y accéder partout dans l'application.********/
}
