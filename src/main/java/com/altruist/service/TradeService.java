package com.altruist.service;

import com.altruist.model.Trade;

import java.util.*;

public interface TradeService {

    Trade create(Trade trade);
    List<Trade> list(UUID accountUuid);
    void cancelTrade(UUID accountId, UUID tradeUuid);
    Optional<Trade> findByIdAndAccountId(UUID tradeUuid, UUID accountUuid);
}
