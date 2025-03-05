package tn.esprit.Utils;

import tn.esprit.Entities.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private static Utilisateur utilisateurConnecte;

    private SessionManager() {}

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

    public boolean isUserLoggedIn() {
        return utilisateurConnecte != null;
    }

    public void logout() {
        utilisateurConnecte = null;
    }

    /*******Cette classe stockera l'utilisateur actuellement connecté et permettra d'y accéder partout dans l'application.********/

}
