package tn.esprit.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class MailSender {
    private static final String SMTP_HOST = "smtp.votre-serveur.com"; // Remplacez par un serveur SMTP interne
    private static final String SMTP_PORT = "25"; // Port standard pour SMTP sans authentification
    private static final String EMAIL = "no-reply@votre-entreprise.com"; // Expéditeur

    public static void sendEmailToEmployee(int id_employe, String subject, String content) {
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement("SELECT email FROM users WHERE id_employe = ?")) {

            ps.setInt(1, id_employe);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String recipientEmail = rs.getString("email");
                sendEmail(recipientEmail, subject, content);
            } else {
                System.err.println("❌ Aucun employé trouvé avec l'ID : " + id_employe);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de l'e-mail : " + e.getMessage());
        }
    }

    public static void sendEmail(String recipient, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "false"); // Désactiver l'authentification
        props.put("mail.smtp.starttls.enable", "false"); // Désactiver TLS

        Session session = Session.getInstance(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("✅ E-mail envoyé avec succès à " + recipient);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur lors de l'envoi de l'e-mail à " + recipient + " : " + e.getMessage());
        }
    }
}
