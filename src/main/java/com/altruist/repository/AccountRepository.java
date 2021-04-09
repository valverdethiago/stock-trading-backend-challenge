package com.altruist.repository;

import com.altruist.model.Account;

import java.util.*;

public interface AccountRepository {
    Account save(Account account);

    void update(Account account);

    Optional<Account> findById(UUID accountUuId);

    List<Account> listAll();
}
