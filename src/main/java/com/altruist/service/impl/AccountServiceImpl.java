package com.altruist.service.impl;

import com.altruist.exceptions.InvalidOperationException;
import com.altruist.model.Account;
import com.altruist.repository.AccountRepository;
import com.altruist.service.AccountService;
import com.altruist.service.AddressService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
  private final AccountRepository accountRepository;
  private final AddressService addressService;

  public AccountServiceImpl(AccountRepository accountRepository,
                            AddressService addressService) {
    this.accountRepository = accountRepository;
    this.addressService = addressService;
  }

  @Override
  public UUID create(Account account) {
    if (account.getAddress() != null) {
      account.setAddressUuid(addressService.create(account.getAddress()));
    }
    return accountRepository.save(account).getUuid();
  }

  @Override
  public void update(Account account) {
    assertUuidIsInformed(account);
    if (account.getAddress() != null) {
      this.persistAddressFromAccount(account);
    }
    else {
      this.addressService.deleteAddressFromAccount(account.getUuid());
    }
    accountRepository.update(account);
  }

  @Override
  public Optional<Account> findById(UUID accountUuid) {
    return accountRepository.findById(accountUuid);
  }

  @Override
  public List<Account> listAll() {
    return accountRepository.listAll();
  }

  private void persistAddressFromAccount(Account account) {
    this.addressService.findByAccountUuid(account.getUuid())
        .ifPresentOrElse(
            (address)-> {
              account.getAddress().setUuid(address.getUuid());
              this.addressService.update(address);
            },
            () -> {
              throw new InvalidOperationException("There's no address for this account "+account.getUuid());
            }
        );
  }

  private void assertUuidIsInformed(Account account) {
    if(account.getUuid() == null) {
      throw new InvalidOperationException("In order to update an account you should provide its uuid");
    }
  }
}
