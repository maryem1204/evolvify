package tn.esprit.Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.mail.Authenticator;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.mindrot.jbcrypt.BCrypt;

import tn.esprit.Entities.Gender;
import tn.esprit.Entities.Role;
import tn.esprit.Entities.Utilisateur;
import tn.esprit.Utils.MyDataBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.*;
import java.sql.Date;
import java.util.*;


public class UtilisateurService implements CRUD<Utilisateur>, CRUD_User<Utilisateur> {
    private Connection cnx = MyDataBase.getInstance().getCnx();
    private PreparedStatement pst;
    private static final String UPLOAD_DIR = "C:/xampp/htdocs/evolvify/";
    private static final String BASE_URL = "http://localhost/evolvify/";

    private static final String ACCOUNT_SID = "AC61b477b529caa61701b41634160f437a";
    private static final String AUTH_TOKEN = "938ea525d8cbdd35b9f49f46a1b06d4d";
    private static final String TWILIO_PHONE_NUMBER = "+15078000957";
    private static final String FROM_EMAIL = "maryemsassi.dev@gmail.com";
    private static final String EMAIL_PASSWORD = "jlej mknk aukk iqlx";

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
        String req = "INSERT INTO Users (firstname, lastname, email, password, role, first_login) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, user.getFirstname());
            pst.setString(2, user.getLastname());
            pst.setString(3, user.getEmail());
            pst.setString(4, hashedPassword);
            pst.setString(5, user.getRole().toString());
            pst.setBoolean(6, true);
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
                    rs.getString("profilePhoto"),
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


    /******* ❌ Mise à jour spécifique du first_login *********/
    public int updateFirstLogin(Utilisateur user, boolean status) throws SQLException {
        String req = "UPDATE Users SET first_login = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(req);

        pst.setBoolean(1, status);
        pst.setInt(2, user.getId_employe());

        return pst.executeUpdate();
    }

    /******* ❌Get User profile photo to display in Profile *********/
    @Override
    public String getProfilePhoto(int userId) throws SQLException {
        String query = "SELECT profilePhoto FROM Users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getString("profilePhoto");
        }
        return null;
    }

    /******* ❌Update User in the database (for HR only) *********/
    @Override
    public int updateProfilePhoto(int userId, String photo) throws SQLException {
        String query = "UPDATE Users SET profilePhoto = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setString(1, photo);
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
                        rs.getString("profilePhoto"),
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
                        rs.getString("profilePhoto"),
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
                    rs.getString("profilePhoto"),
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

    // 📧 Envoi d'un email de confirmation
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

        sendEmail(toEmail, subject, content);
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

    // 📧 Méthode générique pour envoyer un e-mail
    private void sendEmail(String toEmail, String subject, String content) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, EMAIL_PASSWORD);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(toEmail));


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
            System.out.println("📧 E-mail envoyé avec succès !");

            System.out.println("📧 E-mail envoyé !");
 
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail : " + e.getMessage());
        }
    }
    // Method to upload and save profile photo
    public String uploadProfilePhoto(int userId, File file) throws SQLException, IOException {
        // Get user information to create filename
        String firstName = "", lastName = "";
        String query = "SELECT firstname, lastname FROM users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            firstName = rs.getString("firstname");
            lastName = rs.getString("lastname");
        }

        // Create filename using first and last name
        String fileExtension = getFileExtension(file.getName());
        String fileName = firstName + "_" + lastName + "_" + System.currentTimeMillis() + fileExtension;

        // Ensure upload directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // Copy file to htdocs folder
        Path destination = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        // Generate and save URL to database
        String photoUrl = BASE_URL + fileName;
        updateProfilePhoto(userId, photoUrl);

        return photoUrl;
    }

    // Helper method to get file extension
    private String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // No extension
        }
        return fileName.substring(lastIndexOf);
    }

    // Method to get user profile information
    public Utilisateur getUserProfile(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();
        Utilisateur user = null;

        if (rs.next()) {
            user = new Utilisateur();
            user.setId_employe(rs.getInt("id_employe"));
            user.setFirstname(rs.getString("firstname"));
            user.setLastname(rs.getString("lastname"));
            user.setEmail(rs.getString("email"));
            user.setProfilePhoto(rs.getString("profilePhoto"));

            // Set gender with default value HOMME if null
            String genderStr = rs.getString("gender");
            if (genderStr == null || genderStr.isEmpty()) {
                user.setGender(Gender.HOMME);
            } else {
                user.setGender(Gender.valueOf(genderStr));
            }

            user.setNum_tel(rs.getString("num_tel"));

            java.sql.Date birthDate = rs.getDate("birthdayDate");
            if (birthDate != null) {
                user.setBirthdayDate(new Date(birthDate.getTime()));
            }

            java.sql.Date joinDate = rs.getDate("joiningDate");
            if (joinDate != null) {
                user.setJoiningDate(new Date(joinDate.getTime()));
            }

            // Set role
            String roleStr = rs.getString("role");
            if (roleStr != null && !roleStr.isEmpty()) {
                user.setRole(Role.valueOf(roleStr));
            }

            // Check if we have a flag for if birthday has been edited
            // If your database has this column:
            // user.setBirthDateEdited(rs.getBoolean("birthdate_edited"));
        }

        return user;
    }

    // Method to update user profile
    public int updateUserProfile(Utilisateur user) throws SQLException {
        String query = "UPDATE users SET profilePhoto = ?, gender = ?, num_tel = ?, " +
                "birthdayDate = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setString(1, user.getProfilePhoto());
        pst.setString(2, user.getGender().name()); // Use enum name
        pst.setString(3, user.getNum_tel());

        if (user.getBirthdayDate() != null) {
            java.sql.Date sqlDate = new java.sql.Date(user.getBirthdayDate().getTime());
            pst.setDate(4, sqlDate);
        } else {
            pst.setNull(4, java.sql.Types.DATE);
        }

        pst.setInt(5, user.getId_employe());

        return pst.executeUpdate();
    }

    // Check if birthdate has been edited before (if you have this flag in database)
    public boolean hasBirthDateBeenEdited(int userId) throws SQLException {
        // If you don't have this column, you might need to implement another way to check
        // or add this column to your database
        String query = "SELECT birthdate_edited FROM users WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setInt(1, userId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            return rs.getBoolean("birthdate_edited");
        }

        return false;
    }


    // Add a method to update the birthdate_edited flag if you have this column
    public void setBirthDateEdited(int userId, boolean edited) throws SQLException {
        String query = "UPDATE users SET birthdate_edited = ? WHERE id_employe = ?";
        pst = cnx.prepareStatement(query);
        pst.setBoolean(1, edited);
        pst.setInt(2, userId);
        pst.executeUpdate();
    }
    public Utilisateur findByPhone(String phoneNumber) {
        String query = "SELECT * FROM users WHERE num_tel = ?";
        try {
            pst = cnx.prepareStatement(query);
            pst.setString(1, phoneNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("id_employe"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("num_tel")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) pst.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    // 📲 Envoi d'un SMS avec Twilio
    public boolean sendSMSConfirmationCode(String phoneNumber, int code) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            // Vérifier si le numéro commence par '+', sinon ajouter l'indicatif Tunisie
            if (!phoneNumber.startsWith("+")) {
                phoneNumber = "+216" + phoneNumber;
            }

            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),   // ✅ Format correct
                    new PhoneNumber(TWILIO_PHONE_NUMBER),
                    "Votre code de vérification est : " + code
            ).create();

            System.out.println("📱 SMS envoyé avec succès : " + message.getSid());
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur SMS : " + e.getMessage());
            return false;
        }
    }

    public Map<String, Integer> getTaskStatusCount() {
        Map<String, Integer> statusCount = new HashMap<>();
        String query = "SELECT status, COUNT(*) FROM tache GROUP BY status;";

        try (
                PreparedStatement stmt = cnx.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                System.out.println("Statut : " + status + ", Nombre : " + count);
                statusCount.put(status, count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusCount;
    }
    public Map<Integer, Integer> getProjectsPerWeekForLastFourWeeks() {
        Map<Integer, Integer> projectsPerWeek = new HashMap<>();
        String query = "SELECT WEEK(starter_at) AS weekNum, COUNT(*) AS count FROM projet " +
                "WHERE starter_at >= DATE_SUB(CURDATE(), INTERVAL 4 WEEK) GROUP BY weekNum";

        try (PreparedStatement stmt = cnx.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                projectsPerWeek.put(rs.getInt("weekNum"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectsPerWeek;
    }
    public Map<Integer, Integer> getTasksPerWeekForLastFourWeeks() {
        Map<Integer, Integer> tasksPerWeek = new HashMap<>();
        String query = "SELECT WEEK(created_at) AS weekNum, COUNT(*) AS count FROM tache " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 4 WEEK) GROUP BY weekNum";

        try (
                PreparedStatement stmt = cnx.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tasksPerWeek.put(rs.getInt("weekNum"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasksPerWeek;
    }
}
