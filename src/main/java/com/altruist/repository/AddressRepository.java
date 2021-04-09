package com.altruist.repository;

import com.altruist.model.Address;

import java.util.*;

public interface AddressRepository {
    Address save(Address address);

    void update(Address address);

    Optional<Address> findById(UUID addressUuId);

    Optional<Address> findByAccountId(UUID accountUuid);

    void deleteAddressFromAccount(UUID accountUuid);
}
