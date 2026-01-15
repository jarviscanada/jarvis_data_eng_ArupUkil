package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountDao extends JpaRepository<Account, Integer> {}
