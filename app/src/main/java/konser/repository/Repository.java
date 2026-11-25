package konser.repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import konser.model.Identifiable;

public interface Repository<T extends Identifiable, ID> {

    List<T> findAll() throws SQLException;

    Optional<T> findById(ID id) throws SQLException;

}
