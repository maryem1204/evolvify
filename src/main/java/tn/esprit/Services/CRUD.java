package tn.esprit.Services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CRUD<T> {
    int add(T t) throws SQLException;
    int update(T t) throws SQLException;


    int delete(T t) throws SQLException;
    List<T> showAll() throws SQLException;


}
