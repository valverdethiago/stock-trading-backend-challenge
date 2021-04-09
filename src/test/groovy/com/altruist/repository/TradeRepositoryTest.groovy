package com.altruist.repository


import com.altruist.config.DatabaseConfiguration
import com.altruist.config.RepositoryConfiguration
import com.altruist.model.Account
import com.altruist.model.Trade
import com.altruist.model.TradeSide
import com.altruist.model.TradeStatus
import com.altruist.repository.impl.AccountRepositoryImpl
import com.altruist.utils.TestHelper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Repository
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@ActiveProfiles("test")
@DataJdbcTest(includeFilters = [@ComponentScan.Filter(type = FilterType.ANNOTATION, value = [Repository])])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = [DatabaseConfiguration, RepositoryConfiguration])
@Stepwise
@Rollback(true)
class TradeRepositoryTest extends Specification {
    @Autowired
    TradeRepository repository
    @Autowired
    AccountRepositoryImpl accountRepository

    @Shared
    Account account

    def setup() {
        account = new Account(
                username: "someusername",
                email: "somemail@email.com"
        )
        account = accountRepository.save(account)
    }

    def "Inserts a trade"() {
        given: "an trade"
        Trade trade = new Trade(
                accountUuid: account.uuid,
                symbol: "APPL",
                quantity: 100,
                side: TradeSide.BUY,
                price: BigDecimal.valueOf(100.50)
        )

        when:
        trade = repository.save(trade)

        then: "the trade id is returned"
        trade.uuid

        and: "the trade status is the default one"
        trade.status == TradeStatus.SUBMITTED

        and: "the inserted trade is found "
        Trade insertedTrade = repository.findById(trade.uuid).get()
        insertedTrade
        insertedTrade.uuid == trade.uuid
        insertedTrade.quantity == trade.quantity
        insertedTrade.price == trade.price
        insertedTrade.symbol == trade.symbol
        insertedTrade.status == trade.status
        insertedTrade.accountUuid == trade.accountUuid
        insertedTrade.side == trade.side

        and: "the total amount is correct"
        insertedTrade.totalAmount == trade.price * trade.quantity
    }

    def "Updates a trade"() {
        given: "an trade"
        Trade trade = new Trade(
                accountUuid: account.uuid,
                symbol: "APPL",
                quantity: 100,
                side: TradeSide.BUY,
                price: BigDecimal.valueOf(100.50)
        )
        trade = repository.save(trade)
        trade.quantity = 150
        trade.symbol = "GOOGL"
        trade.price = BigDecimal.valueOf(800.65)

        when:
        repository.update(trade)
        Trade upToDateTrade = repository.findById(trade.uuid).get()

        then: "the trade is returned"
        upToDateTrade

        and: "the trade information is up to date"
        upToDateTrade.quantity == trade.quantity
        upToDateTrade.symbol == trade.symbol
        upToDateTrade.price == trade.price

    }

    def "Inserts a list of trades"() {
        given: "a trade"
        Trade trade = new Trade(
                accountUuid: account.uuid,
                symbol: "APPL",
                quantity: 100,
                side: TradeSide.BUY,
                price: BigDecimal.valueOf(100.50)
        )

        and: "a trade list"
        Trade[] trades = [trade,
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade)]

        and: "saving the trade list on the database"
        trades.each {item -> repository.save(item)}

        when:
        Trade[] dbTrades = repository.findByAccount(account.uuid)

        then: "the trades ids are returned"
        dbTrades
        dbTrades.each {item -> item.uuid}

        and: "the trades list is the same as expected"
        dbTrades.length == trades.length

        and: "the total amount is correct"
        dbTrades.each {item -> item.totalAmount == item.price * item.quantity }
    }

    def "A blank list should not cause an exception "() {
        when:
        Trade[] trades = repository.findByAccount(UUID.randomUUID());

        then: "the list is empty"
        trades.length == 0
    }
}
