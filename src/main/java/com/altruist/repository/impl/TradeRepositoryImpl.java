package com.altruist.repository.impl;

import com.altruist.model.Trade;
import com.altruist.model.TradeSide;
import com.altruist.model.TradeStatus;
import com.altruist.repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Repository
@Slf4j
public class TradeRepositoryImpl implements TradeRepository {

    private final NamedParameterJdbcOperations jdbcOperations;
    private final JdbcTemplate jdbcTemplate;

    public TradeRepositoryImpl(NamedParameterJdbcOperations jdbcOperations,
                               JdbcTemplate jdbcTemplate) {
        this.jdbcOperations = jdbcOperations;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Trade save(Trade trade) {
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(trade);
        params.registerSqlType("side", Types.VARCHAR);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        log.info("Saving trade [{}].", trade);
        String sql = "INSERT INTO trade.trade (account_uuid, symbol, quantity, side, price) " +
            "VALUES (:accountUuid, :symbol, :quantity, :side::trade.trade_side, :price)";
        jdbcOperations.update(sql, params, keyHolder);
        Map<String, Object> keys = keyHolder.getKeys();
        if (null != keys) {
            UUID id = (UUID) keys.get("trade_uuid");
            TradeStatus status = TradeStatus.valueOf(keys.get("status").toString());
            log.info("Inserted trade record with id {} and status {}.", id, status);
            trade.setUuid(id);
            trade.setStatus(status);
        } else {
            log.warn("Insert of trade record failed. {}", trade);
            throw new RuntimeException("Insert failed for trade");
        }
        return trade;
    }

    @Override
    public void update(Trade trade) {
        BeanPropertySqlParameterSource params = new BeanPropertySqlParameterSource(trade);
        params.registerSqlType("side", Types.VARCHAR);
        params.registerSqlType("status", Types.VARCHAR);
        log.info("Saving trade [{}].", trade);
        String sql = "UPDATE trade.trade SET " +
            "  symbol = :symbol, " +
            "  quantity = :quantity, " +
            "  side = :side::trade.trade_side, " +
            "  price = :price, " +
            "  status = :status::trade.trade_status " +
            " WHERE trade_uuid = :uuid ";
        try {
            jdbcOperations.update(sql, params);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            log.warn("Update of trade record failed. {}", trade);
            throw new RuntimeException("Update failed for trade", ex);
        }
    }

    @Override
    public Optional<Trade> findById(UUID uuid) {
        try {
            return Optional.ofNullable(this.jdbcTemplate.queryForObject(
                "select trade.*, " +
                    "trade.quantity * trade.price as total_amount " +
                    "from trade.trade as trade " +
                    "where trade_uuid = ? ",
                new Object[] {uuid},
                new TradeMapper()));
        }
        catch (EmptyResultDataAccessException ex) {
            log.warn("No trade found for id {}", uuid);
            return Optional.empty();
        }
    }

    @Override
    public List<Trade> findByAccount(UUID accountUuid) {
        return this.jdbcTemplate.query(
            "select trade.*, " +
                "trade.quantity * trade.price as total_amount " +
                "from trade.trade as trade " +
                "where account_uuid = ? ",
            new Object[] {accountUuid},
            new TradeMapper());
    }

    @Override
    public Optional<Trade> findByIdAndAccountId(UUID tradeUuid, UUID accountUuid) {
        try {
            return Optional.ofNullable(this.jdbcTemplate.queryForObject(
                "select trade.*, " +
                    "trade.quantity * trade.price as total_amount " +
                    "from trade.trade as trade " +
                    "where trade_uuid = ? and account_uuid = ?",
                new Object[] {tradeUuid, accountUuid},
                new TradeMapper()));
        }
        catch (EmptyResultDataAccessException ex) {
            log.warn("No trade found for id {} on account {}", tradeUuid, accountUuid);
            return Optional.empty();
        }
    }

    private class TradeMapper implements RowMapper<Trade> {

        @Override
        public Trade mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Trade.builder()
                .uuid(UUID.fromString(rs.getString("trade_uuid")))
                .accountUuid(UUID.fromString(rs.getString("account_uuid")))
                .symbol(rs.getString("symbol"))
                .quantity(rs.getInt("quantity"))
                .side(TradeSide.valueOf(rs.getString("side")))
                .price(rs.getBigDecimal("price"))
                .status(TradeStatus.valueOf(rs.getString("status")))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .build();
        }
    }
}
