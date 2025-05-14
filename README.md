
# Evolvify JavaFX

**Evolvify JavaFX** est une application de bureau construite avec JavaFX et Maven, visant à assister la gestion des ressources humaines, du transport, des projets et du recrutement dans un environnement d’entreprise.

---

## 👥 Acteurs du système

- **RH** : Gère les utilisateurs, absences, recrutements, et trajets.
- **Employé** : Peut visualiser ses affectations, trajets, absences.
- **Chef de projet** : Planifie les projets, assigne des tâches aux employés, suit les livrables.

---

## 🧩 Modules Principaux

### 1. Gestion des Utilisateurs
- Interfaces pour l’ajout, modification, suppression d’utilisateurs.
- Gestion des rôles : RH, Employé, Chef de projet.
- Interface de connexion sécurisée.

### 2. Recrutement
- Ajout et affichage des offres d’emploi.
- Gestion des candidatures.
- Possibilité pour les candidats internes de postuler.

### 3. Gestion de Projet
- Création de projets et sous-tâches.
- Affectation de tâches aux employés.
- Suivi de l’avancement et livrables.
- Rapport des testeurs.

### 4. Absences
- Déclaration des absences par les employés.
- Validation des absences par RH.
- Historique et suivi de l'état.

### 5. Transport
- Ajout de trajets avec distance estimée.
- Abonnement à un trajet par l’employé.
- Visualisation des trajets affectés.

---

## ⚙️ Technologies utilisées

- **JavaFX** : UI Java pour applications de bureau.
- **Maven** : Gestionnaire de dépendances.
- **SceneBuilder** : Conception d'interfaces FXML.
- **MySQL** : Base de données relationnelle.

---

## 🚀 Installation & Lancement

### Prérequis

- Java JDK 17+
- Maven
- SceneBuilder (optionnel pour modifier les interfaces)

### Étapes

```bash
git clone https://github.com/votre-utilisateur/evolvify-javafx.git
cd evolvify-javafx
mvn clean install
mvn javafx:run
```

---

## 📂 Structure du projet

- `src/main/java/tn/esprit/Controllers` : Contient tous les contrôleurs JavaFX.
- `src/main/resources` : Contient les fichiers FXML, CSS et ressources.
- `pom.xml` : Fichier de configuration Maven.

---

## 📄 Licence

Projet sous licence **MIT**. Voir le fichier `LICENSE` pour plus de détails.

## 👨‍💻 Développé par

L’équipe Evolvify
