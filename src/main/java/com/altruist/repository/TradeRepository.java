package com.altruist.repository;

import com.altruist.model.Trade;

import java.util.*;

public interface TradeRepository {

    Trade save(Trade trade);
    void update(Trade trade);
    Optional<Trade> findById(UUID uuid);
    List<Trade> findByAccount(UUID accountUuid);


    Optional<Trade> findByIdAndAccountId(UUID tradeUuid, UUID accountUuid);
}
