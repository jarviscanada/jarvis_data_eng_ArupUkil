package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.SecurityOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityOrderDao extends JpaRepository<SecurityOrder, Integer> {

  List<SecurityOrder> findByAccountId(Integer accountId);

  void deleteByAccountId(Integer accountId);
}
