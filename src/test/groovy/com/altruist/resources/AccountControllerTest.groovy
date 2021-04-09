package com.altruist.resources

import com.altruist.config.ApplicationConfiguration
import com.altruist.model.Account
import com.altruist.model.Address
import com.altruist.model.State
import com.altruist.service.AccountService
import com.altruist.service.AddressService
import com.altruist.service.impl.AccountServiceImpl
import com.altruist.service.impl.AddressServiceImpl
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import static org.hamcrest.Matchers.containsString
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [AccountController])
@Import(value=[ApplicationConfiguration])
class AccountControllerTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    AccountService mockAccountService

    def "Should accept complete account requests"() {
        given: "an account request"
        Address address = new Address(
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )
        Account req = new Account(
                username: "username123",
                email: "email@example.com",
                address: address
        )
        UUID expectedId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountService.create(req) >> expectedId

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the order ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/accounts/$expectedId")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))
    }


    def "Should accept account without address"() {
        given: "an account request"
        Account req = new Account(
                username: "username123",
                email: "email@example.com"
        )
        UUID expectedId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "the request is processed"
        1 * mockAccountService.create(req) >> expectedId

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the account ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location", containsString("/accounts/$expectedId")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))
    }

    def "Should not accept account with address without zipcode"() {
        given: "an account request"
        Account req = new Account(
                username: "username123",
                email: "email@example.com",
                address: new Address(
                        name: "Some Name",
                        street: "Some street",
                        city: "Some city",
                        state: "CA"
                )
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())
    }


    def "Should not accept account without username"() {
        given: "an account request"
        Account req = new Account(
                email: "email@example.com"
        )

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )

        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())
    }

    def "Should validate for missing address field #field"() {
        given: "an address missing fields"
        Address address = new Address(
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: State.CA,
                zipcode: 99999
        )
        Account account = new Account(
                username: "username123",
                email: "email@example.com",
                address: address
        )
        account.address[field] = null

        when: "the request is submitted"
        ResultActions results = mvc.perform(post("/accounts")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(account))
        )

        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())

        where:
        field << ["name", "street", "city", "state", "zipcode"]
    }

    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AccountService accountService() {
            factory.Mock(AccountServiceImpl)
        }

        @Bean
        AddressService addressService() {
            factory.Mock(AddressServiceImpl)
        }
    }
}
