package tn.esprit.Services;

import tn.esprit.Entities.Utilisateur;

import java.sql.SQLException;

public interface CRUD_User<T> {

    String getProfilePhoto(int userId) throws SQLException;
    int updateProfilePhoto(int userId, String photo) throws SQLException;
    Utilisateur getUserById(int id) throws SQLException;
}
