package konser.repository;

import konser.model.Identifiable;

import java.sql.SQLException;

import java.util.List;

import java.util.Optional;

public interface Repository<T extends Identifiable, ID> {

    List<T> findAll() throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

}
