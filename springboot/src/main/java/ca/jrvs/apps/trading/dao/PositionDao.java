package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.Position;
import ca.jrvs.apps.trading.model.PositionId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

@org.springframework.stereotype.Repository
public interface PositionDao extends Repository<Position, PositionId> {

  Optional<Position> findById(PositionId id);

  List<Position> findAll();

  Iterable<Position> findAllById(Iterable<PositionId> ids);

  List<Position> findByAccountId(Integer accountId);

  boolean existsById(PositionId id);

  long count();
}
