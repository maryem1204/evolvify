package tn.esprit.Services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {
    // Your Twilio credentials - store these securely in a properties file
    private static final String ACCOUNT_SID = "AC9b6f27261cda37833226e23a7ccf2913";
    private static final String AUTH_TOKEN = "2c2de7d2f3f82ee7854086d7736adcab";
    private static final String FROM_NUMBER = "+14243534350"; // Format: +1234567890

    // HR phone number - ideally this should come from a configuration or database
    private static final String HR_PHONE_NUMBER = "+21697129381"; // Format: +1234567890

    // Initialize Twilio only once
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    /**
     * Sends an SMS notification to HR when an employee registers attendance
     *
     * @param employeeId The ID of the employee
     * @param employeeName The name of the employee
     * @param status The attendance status recorded
     * @return The SID of the sent message if successful
     */
    public String notifyHRAboutAttendance(int employeeId, String employeeName, String status) {
        String messageBody = String.format(
                "Un employé a enregistré sa présence:\n" +
                        "ID: %d\n" +
                        "Nom: %s\n" +
                        "Statut: %s\n\n" +
                        "Répondez 'VALIDER %d' pour confirmer ou 'REJETER %d' pour refuser.",
                employeeId, employeeName, status, employeeId, employeeId
        );

        try {
            Message message = Message.creator(
                    new PhoneNumber(HR_PHONE_NUMBER),
                    new PhoneNumber(FROM_NUMBER),
                    messageBody
            ).create();

            return message.getSid();
        } catch (Exception e) {
            System.err.println("Error sending SMS: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}