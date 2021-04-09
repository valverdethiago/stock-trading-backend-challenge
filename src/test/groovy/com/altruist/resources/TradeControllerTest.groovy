package com.altruist.resources

import com.altruist.config.ApplicationConfiguration
import com.altruist.exceptions.InvalidOperationException
import com.altruist.exceptions.InvalidTradeStatusException
import com.altruist.model.Trade
import com.altruist.model.TradeSide
import com.altruist.model.TradeStatus
import com.altruist.service.TradeService
import com.altruist.utils.TestHelper
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.mock.DetachedMockFactory

import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasSize
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [TradeController])
@Import(value=[ApplicationConfiguration])
class TradeControllerTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    TradeService mockTradeService

    @Shared
    Trade trade

    def setup() {
        trade = new Trade(
                accountUuid: UUID.randomUUID(),
                symbol: "GOGL",
                quantity: 100,
                side: TradeSide.BUY,
                price: BigDecimal.valueOf(100.50)

        )
    }

    def "Should accept trade request"() {
        given: "a trade request"
        UUID expectedId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$trade.accountUuid/trades")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(trade))
        )

        then: "the request is processed"
        1 * mockTradeService.create(trade) >> { Trade arg ->
            with(arg){
                accountUuid : trade.accountUuid
                symbol: trade.symbol
                quantity: trade.quantity
                side: trade.side
                price: trade.price
            }

            arg.uuid = expectedId
            arg
        }

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the order ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        containsString("/accounts/$trade.accountUuid/trades/$expectedId")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))
    }

    @Unroll
    def "Should not accept trade without required field #field"() {
        given: "a trade request"
        UUID accountId = UUID.randomUUID()
        trade[field] = null

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$accountId/trades")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trade))
        )


        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())

        where:
        field << ["symbol", "quantity", "side", "price"]
    }

    def "Should not accept trade with zero in price"() {
        given: "a trade request"
        UUID accountId = UUID.randomUUID()
        trade.price = BigDecimal.ZERO

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$accountId/trades")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trade))
        )


        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())
    }

    def "Should not accept trade with zero in quantity"() {
        given: "a trade request"
        UUID accountId = UUID.randomUUID()
        trade.quantity = 0

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$accountId/trades")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trade))
        )


        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())
    }

    def "Should return the list of trades for the account"() {
        given: "a trade list"
        Trade[] trades = [trade,
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade),
                          TestHelper.deepCopy(trade)]
        UUID accountId = trade.accountUuid

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                get("/accounts/$accountId/trades")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
        )

        then: "the list method on server is called"
        1 * mockTradeService.list(accountId) >> Arrays.asList(trades)

        and: "request status is ok"
        results.andExpect(status().isOk())

        and: "result list is the same size"
        results.andExpect(jsonPath("\$", hasSize(trades.length)))

    }

    def "should cancel a trade"() {
        given : "a trade in submitted status and attributes"
        trade.status = TradeStatus.SUBMITTED
        trade.uuid = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                delete("/accounts/$trade.accountUuid/trades/$trade.uuid")
        )

        then: "The service method is called"
        mockTradeService.cancelTrade(trade.accountUuid, trade.uuid)

        and: "request status is accepted"
        results.andExpect(status().isAccepted())
    }

    def "should not cancel a trade in an invalid status"() {
        given : "a trade in submitted status and attributes"
        trade.status = TradeStatus.COMPLETED
        trade.uuid = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                delete("/accounts/$trade.accountUuid/trades/$trade.uuid")
        )

        then: "The service method is called"
        1 * mockTradeService.cancelTrade(trade.accountUuid, trade.uuid) >> {
            throw new InvalidTradeStatusException();
        }

        and: "request status is conflict"
        results.andExpect(status().isUnavailableForLegalReasons())
    }



    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        TradeService tradeService() {
            factory.Mock(TradeService)
        }

    }
}
