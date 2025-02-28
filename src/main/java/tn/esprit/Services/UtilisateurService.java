package tn.esprit.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.mindrot.jbcrypt.BCrypt;
import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;

import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class UtilisateurService implements CRUD<Utilisateur>, CRUD_User<Utilisateur> {
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement pst;


    /******* ❌Add user into database *********/
    @Override
    public int add(Utilisateur user) throws SQLException {
        if (isEmailExists(user.getEmail())) {
            System.out.println("Erreur : L'email est déjà utilisé !");
            return 0; // Échec
        }

        // 1️⃣ Générer un mot de passe aléatoire
        String rawPassword = generateRandomPassword(8);
        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        // 3️⃣ Enregistrer dans la base avec le mot de passe haché
        String req = "INSERT INTO Users (firstname, lastname, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, user.getFirstname());
            pst.setString(2, user.getLastname());
            pst.setString(3, user.getEmail());
            pst.setString(4, hashedPassword);
            pst.setString(5, user.getRole().toString());
            pst.executeUpdate();
        }

        // 4️⃣ Envoyer l'email avec le mot de passe en clair
        sendConfirmationEmail(user.getEmail(), rawPassword);

        return 1; // Succès
    }
    /******* ❌ Vérifier si l'email existe déjà *********/
    public boolean isEmailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM Users WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retourne true si l'email existe déjà
            }
        }
        return false;
    }


    /******* ❌Display list Users *********/
    @Override
    public List<Utilisateur> showAll() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        String req = "SELECT * FROM Users";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            utilisateurs.add(new Utilisateur(
                    rs.getInt("id_employe"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBytes("profilePhoto"),
                    rs.getDate("birthdayDate"), // Garder la date NULL si elle est absente
                    rs.getDate("joiningDate"),
                    Role.valueOf(rs.getString("role")), // Correction ici
                    rs.getInt("tt_restants"),  // Ajout de la virgule ici
                    rs.getInt("conge_restant"),
                    rs.getBytes("uploaded_cv"),
                    rs.getString("num_tel"),
                    Gender.valueOf(rs.getString("gender")) // Ajout du genre

            ));
        }


        return utilisateurs;
    }

    /******* ❌Delete User by its ID *********/
    @Override
    public int delete(Utilisateur utilisateur) throws SQLException {
        if (utilisateur.getId_employe() == 0) {
            System.out.println("Erreur : ID invalide pour la suppression !");
            return 0;
        }

        String req = "DELETE FROM Users WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);
        pst.setInt(1, utilisateur.getId_employe());
        return pst.executeUpdate();
    }

    /******* ❌Update User in the database (for HR only) *********/
    @Override
    public int update(Utilisateur user) throws SQLException {
        if (user.getId_employe() == 0) {
            System.out.println(" Erreur : ID invalide pour la mise à jour !");
            return 0;
        }

        String req = "UPDATE Users SET firstname = ?, lastname = ?, email = ?, role = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);

        pst.setString(1, user.getFirstname());
        pst.setString(2, user.getLastname());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getRole().toString());
        pst.setInt(5, user.getId_employe());

        return pst.executeUpdate();
    }

    /******* ❌Update User Profile in the database (for employee or HR or project manager) *********/
    public int updateProfile(Utilisateur user) throws SQLException {
        if (user.getId_employe() == 0) {
            System.out.println("❌ Erreur : ID invalide pour la mise à jour !");
            return 0;
        }

        String req = "UPDATE Users SET profilePhoto = ?, birthdayDate = ?, tt_restants = ?, conge_restant = ?, uploaded_cv = ?, num_tel = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);

        pst.setBytes(1, user.getProfilePhoto());
        pst.setDate(2, user.getBirthdayDate() != null ? user.getBirthdayDate() : Date.valueOf("2000-01-01"));
        pst.setInt(3, user.getTtRestants());
        pst.setInt(4, user.getCongeRestant());
        pst.setBytes(5, user.getUploadedCv());
        pst.setString(6, user.getNum_tel());
        pst.setInt(7, user.getId_employe());

        return pst.executeUpdate();
    }



    /******* ❌Get User profile photo to display in Profile *********/
    @Override
    public byte[] getProfilePhoto(int userId) throws SQLException {
        String query = "SELECT profilePhoto FROM Users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getBytes("profilePhoto");
        }
        return null;
    }

    /******* ❌Update User in the database (for HR only) *********/
    @Override
    public int updateProfilePhoto(int userId, byte[] photo) throws SQLException {
        String query = "UPDATE Users SET profilePhoto = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setBytes(1, photo);
        pst.setInt(2, userId);
        return pst.executeUpdate();
    }

    /******* ❌Get User by its ID *********/

    public Utilisateur getUserById(int id) throws SQLException {
        String req = "SELECT * FROM Users WHERE id_employe = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBytes("profilePhoto"),
                        rs.getDate("birthdayDate") != null ? rs.getDate("birthdayDate") : Date.valueOf("2000-01-01"),
                        rs.getDate("joiningDate") != null ? rs.getDate("joiningDate") : Date.valueOf("2000-01-01"),
                        Role.valueOf(rs.getString("role")), // ✅ Correction ici
                        rs.getInt("tt_restants"),
                        rs.getInt("conge_restant"),
                        rs.getBytes("uploaded_cv"),
                        rs.getString("num_tel"),
                        Gender.valueOf(rs.getString("gender")) // Ajout du genre

                );
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }

        return null;
    }

    /******* ❌Get User by its email *********/
    public Utilisateur getUserByEmail(String email) throws SQLException {
        String req = "SELECT * FROM Users WHERE email = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, email);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBytes("profilePhoto"),
                        rs.getDate("birthdayDate") != null ? rs.getDate("birthdayDate") : Date.valueOf("2000-01-01"),
                        rs.getDate("joiningDate") != null ? rs.getDate("joiningDate") : Date.valueOf("2000-01-01"),
                        Role.valueOf(rs.getString("role")), // ✅ Correction ici
                        rs.getInt("tt_restants"),
                        rs.getInt("conge_restant"),
                        rs.getBytes("uploaded_cv"),
                        rs.getString("num_tel"),
                        Gender.valueOf(rs.getString("gender")) // Ajout du genre

                );
            }
        }
        return null;
    }
    public Utilisateur findByEmail(String email) {
        String query = "SELECT id_employe, email, password, role FROM users WHERE email = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                return new Utilisateur(
                        resultSet.getInt("id_employe"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        Role.valueOf(resultSet.getString("role"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getEmployeNameById(int id) {
        String req = "SELECT firstname, lastname FROM Users WHERE id_employe = ?";

        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getString("firstname") + " " + rs.getString("lastname");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'employé : " + e.getMessage());
        }

        return "Inconnu";  // Si l'ID n'existe pas, on retourne "Inconnu"
    }


    public List<Utilisateur> getAllEmployees() throws SQLException {
        List<Utilisateur> employees = new ArrayList<>();
        String req = "SELECT * FROM Users WHERE role = 'EMPLOYEE'";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            employees.add(new Utilisateur(
                    rs.getInt("id_employe"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getBytes("profilePhoto"),
                    rs.getDate("birthdayDate"),
                    rs.getDate("joiningDate"),
                    Role.valueOf(rs.getString("role")),
                    rs.getInt("tt_restants"),
                    rs.getInt("conge_restant"),
                    rs.getBytes("uploaded_cv"),
                    rs.getString("num_tel"),
                    Gender.valueOf(rs.getString("gender"))

            ));
        }
        return employees;
    }
    public int countByGender(Gender gender) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM users WHERE gender = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, gender.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int countByRole(Role role) {
        int count = 0;
        String query = "SELECT COUNT(*) FROM users WHERE role = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public int countAcceptedLeaves() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM congé WHERE status = 'Accepte'";
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    public int countUsers() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM users"; // Compter tous les utilisateurs
        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    /******* ❌LOGIN *********/

    public Utilisateur authenticateUser(String email, String password) {
        String query = "SELECT id_employe, email, password, role FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet resultSet = pst.executeQuery();

            if (resultSet.next()) {
                return new Utilisateur(
                        resultSet.getInt("id_employe"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        Role.valueOf(resultSet.getString("role")) // Convertit la chaîne de la BD en Role ENUM
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Si aucun utilisateur trouvé
    }

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    public void sendConfirmationEmail(String toEmail, String generatedPassword) {
        String subject = "Votre compte a été créé !";
        String content = "<p>Bonjour,</p>"
                + "<p>Votre compte a été créé avec succès.</p>"
                + "<p>Voici vos informations de connexion :</p>"
                + "<ul>"
                + "<li><b>Email :</b> " + toEmail + "</li>"
                + "<li><b>Mot de passe :</b> " + generatedPassword + "</li>"
                + "</ul>"
                + "<p>Nous vous conseillons de modifier votre mot de passe après votre première connexion.</p>"
                + "<p>Ouvrez l'application et connectez-vous avec vos identifiants.</p>";

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", "smtp.gmail.com");
            properties.put("mail.smtp.port", "587");

            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("maryemsassi.dev@gmail.com", "jlej mknk aukk iqlx");
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("maryemsassi.dev@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            System.out.println("E-mail envoyé avec succès !");
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }

    public int updateAbsenceCount() {
        String req = "SELECT COUNT(*) FROM absence WHERE status = 'absent' AND date = CURDATE()";
        int count = 0;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(req);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count =resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    public int getAbonnementCount() {
        String query = "SELECT COUNT(*) FROM abonnement WHERE status = 'actif'";
        int count = 0;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }
    public int getProjectCount() {
        String query = "SELECT COUNT(*) FROM projet WHERE status = 'IN_PROGRESS'";
        int count = 0;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public int getTaskCount() {
        String query = "SELECT COUNT(*) FROM tache WHERE status = 'TO_DO'";
        int count = 0;

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public Map<String, Integer> getAbonnementStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT type_Ab, COUNT(*) FROM abonnement GROUP BY type_Ab";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                stats.put(resultSet.getString("type_Ab"), resultSet.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }
    /******* ❌FORGET PASSWORD *********/

    public void savePasswordResetToken(int userId, String token) {
        String query = "UPDATE users SET reset_token = ? WHERE id_employe = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, token);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Utilisateur findByResetToken(String token) {
        String query = "SELECT id_employe, email, password, role FROM users WHERE reset_token = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, token);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                return new Utilisateur(
                        resultSet.getInt("id_employe"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        Role.valueOf(resultSet.getString("role"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendConfirmationCode(String email, int code) {
        String subject = "Code de vérification";
        String content = "<p>Votre code de vérification est : <b>" + code + "</b></p>";

        sendEmail(email, subject, content);
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt()); // Hachage du mot de passe
        String query = "UPDATE users SET password = ? WHERE id_employe = ?";

        PreparedStatement statement = cnx.prepareStatement(query);
        statement.setString(1, hashedPassword); // Stocke le mot de passe haché
        statement.setInt(2, userId);

        statement.executeUpdate();
    }

    private void sendEmail(String toEmail, String subject, String content) {
        final String fromEmail = "maryemsassi.dev@gmail.com";
        final String password = "jlej mknk aukk iqlx"; // Remplacez par votre vrai mot de passe

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(content, "text/html");

            Transport.send(message);
            System.out.println("📧 E-mail envoyé !");
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }





}
