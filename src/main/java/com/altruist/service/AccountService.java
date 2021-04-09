package com.altruist.service;

import com.altruist.model.Account;

import java.util.*;

public interface AccountService {
    UUID create(Account account);

    void update(Account account);

    Optional<Account> findById(UUID accountUuid);

    List<Account> listAll();
}
