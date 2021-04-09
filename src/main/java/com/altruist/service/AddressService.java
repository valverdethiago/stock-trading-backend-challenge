package com.altruist.service;

import com.altruist.model.Address;
import lombok.NonNull;

import java.util.*;

public interface AddressService {
    UUID create(Address address);

    UUID create(@NonNull UUID accountUuid, Address address);

    void update(Address address);

    void update(@NonNull UUID accountUuid, Address address);

    Optional<Address> findByAccountUuid(@NonNull UUID accountUuid);

    void deleteAddressFromAccount(@NonNull UUID accountUuid);
}
