package tn.esprit.Services;

import tn.esprit.Entities.Utilisateur;

import java.sql.SQLException;

public interface CRUD_User<T> {

    byte[] getProfilePhoto(int userId) throws SQLException;
    int updateProfilePhoto(int userId, byte[] photo) throws SQLException;
    Utilisateur getUserById(int id) throws SQLException;
}
