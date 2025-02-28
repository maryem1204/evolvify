package tn.esprit.Entities;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.*;
import java.net.URL;
import java.sql.Date;
import java.util.Arrays;
import java.util.Objects;

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
    private String profilePhotoPath;
    private Gender gender; // Ajout du genreµ
    // private boolean firstLogin = true; // Default to true for new users


    public Utilisateur() {}

    public Utilisateur(int id_employe, String firstname, String lastname, String email, String password, byte[] profilePhoto, Date birthdayDate, Date joiningDate, Role role, int tt_restants, int conge_restant, byte[] uploaded_cv, String num_tel, Gender gender) {
        this.id_employe = id_employe;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.birthdayDate = birthdayDate;
        this.joiningDate = joiningDate;
        this.role = role;
        this.tt_restants = tt_restants;
        this.conge_restant = conge_restant;
        this.uploaded_cv = uploaded_cv;
        this.num_tel = num_tel;
        this.gender = (gender != null) ? gender : Gender.HOMME; // 🔥 Assurer une valeur par défaut
    }

    public Utilisateur(String firstname, String lastname, String email, String password, byte[] profilePhoto, Date birthdayDate, Date joiningDate, Role role, int tt_restants, int conge_restant, byte[] uploaded_cv, String num_tel, Gender gender) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.profilePhoto = profilePhoto;
        this.birthdayDate = birthdayDate;
        this.joiningDate = joiningDate;
        this.role = role;
        this.tt_restants = tt_restants;
        this.conge_restant = conge_restant;
        this.uploaded_cv = uploaded_cv;
        this.num_tel = num_tel;
        this.gender = (gender != null) ? gender : Gender.HOMME; // 🔥 Assurer une valeur par défaut
    }

    public Utilisateur(int id_employe, String email, String password, Role role) {
        this.id_employe = id_employe;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters et Setters
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

    public int getTtRestants() {
        return tt_restants;
    }

    public void setTtRestants(int tt_restants) {
        this.tt_restants = tt_restants;
    }

    public int getCongeRestant() {
        return conge_restant;
    }

    public void setCongeRestant(int conge_restant) {
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }

    public void setProfilePhotoPath(String profilePhotoPath) {
        this.profilePhotoPath = profilePhotoPath;
    }

    // Image par défaut si la photo de profil n'existe pas
    public ImageView getProfilePhotoImageView() {
        Image image;
        if (profilePhotoPath == null || profilePhotoPath.isEmpty()) {
            URL imageUrl = getClass().getResource("/images/profile.png");
            if (imageUrl == null) {
                return new ImageView();
            } else {
                image = new Image(imageUrl.toExternalForm());
            }
        } else {
            try {
                File file = new File(profilePhotoPath);
                if (file.exists()) {
                    image = new Image(new FileInputStream(file));
                } else {
                    URL imageUrl = getClass().getResource("/images/profile.png");
                    if (imageUrl == null) {
                        return new ImageView();
                    }
                    image = new Image(imageUrl.toExternalForm());
                }
            } catch (FileNotFoundException e) {
                URL imageUrl = getClass().getResource("/images/profile.png");
                if (imageUrl == null) {
                    return new ImageView();
                }
                image = new Image(imageUrl.toExternalForm());
            }
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        return imageView;
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
                ", gender='" + gender + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return id_employe == that.id_employe && tt_restants == that.tt_restants && conge_restant == that.conge_restant && Objects.equals(firstname, that.firstname) && Objects.equals(lastname, that.lastname) && Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.deepEquals(profilePhoto, that.profilePhoto) && Objects.equals(birthdayDate, that.birthdayDate) && Objects.equals(joiningDate, that.joiningDate) && role == that.role && Objects.deepEquals(uploaded_cv, that.uploaded_cv) && Objects.equals(num_tel, that.num_tel) && Objects.equals(profilePhotoPath, that.profilePhotoPath) && gender == that.gender;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_employe, firstname, lastname, email, password, Arrays.hashCode(profilePhoto), birthdayDate, joiningDate, role, tt_restants, conge_restant, Arrays.hashCode(uploaded_cv), num_tel, profilePhotoPath, gender);
    }
}
