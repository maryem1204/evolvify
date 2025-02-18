package tn.esprit.Entities;

import java.io.Serializable;
import java.sql.Date;
import java.util.Objects;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilisateur implements Serializable {
    private int id_employe;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private byte[] profilePhoto;
    private Date birthdayDate;
    private Date joiningDate;
    private Role role;
    private int tt_restants;
    private int conge_restant;
    private byte[] uploaded_cv;
    private String num_tel;

    public Utilisateur() {}

    public Utilisateur(int id_employe, String firstname, String lastname, String email, String password, byte[] profilePhoto, Date birthdayDate, Date joiningDate, Role role, int tt_restants, int conge_restant, byte[] uploaded_cv, String num_tel) {
        this.id_employe = id_employe;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.birthdayDate = birthdayDate != null ? birthdayDate : Date.valueOf("2000-01-01");
        this.joiningDate = joiningDate != null ? joiningDate : Date.valueOf("2000-01-01");
        this.role = role;
        this.tt_restants = tt_restants;
        this.conge_restant = conge_restant;
        this.uploaded_cv = uploaded_cv;
        this.num_tel = num_tel;
    }

    public Utilisateur(String firstname, String lastname, String email, String password, byte[] profilePhoto, Date birthdayDate, Date joiningDate, Role role, int tt_restants, int conge_restant, byte[] uploaded_cv, String num_tel) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.birthdayDate = birthdayDate != null ? birthdayDate : Date.valueOf("2000-01-01");
        this.joiningDate = joiningDate != null ? joiningDate : Date.valueOf("2000-01-01");
        this.role = role;
        this.tt_restants = tt_restants;
        this.conge_restant = conge_restant;
        this.uploaded_cv = uploaded_cv;
        this.num_tel = num_tel;
    }
    public Utilisateur(String firstname, String lastname, String email,byte[] profilePhoto,  Date birthdayDate, Date joiningDate, byte[] uploaded_cv,String num_tel) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password="";
        this.profilePhoto = profilePhoto;
        this.birthdayDate = birthdayDate != null ? birthdayDate : Date.valueOf("2000-01-01");
        this.joiningDate = joiningDate != null ? joiningDate : Date.valueOf("2000-01-01");

        this.tt_restants = 0;
        this.conge_restant = 0;
        this.uploaded_cv = uploaded_cv;
        this.num_tel = num_tel;
    }

    // Convertir un fichier en byte[]
    public static byte[] fileToBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    // Convertir un byte[] en fichier
    public static void bytesToFile(byte[] fileData, String outputPath) throws IOException {
        Path path = Paths.get(outputPath);
        Files.write(path, fileData);
    }


    public int getId_employe() {
        return id_employe;
    }

    public void setId_employe(int id_employe) {
        this.id_employe = id_employe;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public Date getBirthdayDate() {
        return birthdayDate;
    }

    public void setBirthdayDate(Date birthdayDate) {
        this.birthdayDate = birthdayDate;
    }

    public Date getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(Date joiningDate) {
        this.joiningDate = joiningDate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getTt_restants() {
        return tt_restants;
    }

    public void setTt_restants(int tt_restants) {
        this.tt_restants = tt_restants;
    }

    public int getConge_restant() {
        return conge_restant;
    }

    public void setConge_restant(int conge_restant) {
        this.conge_restant = conge_restant;
    }

    public byte[] getUploadedCv() {
        return uploaded_cv;
    }

    public void setUploadedCv(byte[] uploaded_cv) {
        this.uploaded_cv = uploaded_cv;
    }

    public String getNum_tel() {
        return num_tel;
    }

    public void setNum_tel(String num_tel) {
        this.num_tel = num_tel;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id_employe=" + id_employe +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", profilePhoto=" + (profilePhoto != null ? "Exists" : "Not Uploaded") +
                ", birthdayDate=" + birthdayDate +
                ", joiningDate=" + joiningDate +
                ", role='" + role + '\'' +
                ", tt_restants=" + tt_restants +
                ", conge_restant=" + conge_restant +
                ", uploadedCv=" + (uploaded_cv != null ? "Exists" : "Not Uploaded") +
                ", num_tel='" + num_tel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return id_employe == that.id_employe &&
                tt_restants == that.tt_restants &&
                conge_restant == that.conge_restant &&
                Objects.equals(firstname, that.firstname) &&
                Objects.equals(lastname, that.lastname) &&
                Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) &&
                Objects.equals(profilePhoto, that.profilePhoto) &&
                Objects.equals(birthdayDate, that.birthdayDate) &&
                Objects.equals(joiningDate, that.joiningDate) &&
                Objects.equals(role, that.role) &&
                Objects.equals(uploaded_cv, that.uploaded_cv) &&
                Objects.equals(num_tel, that.num_tel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_employe, firstname, lastname, email, password, profilePhoto, birthdayDate, joiningDate, role, tt_restants, conge_restant, uploaded_cv, num_tel);
    }
}
