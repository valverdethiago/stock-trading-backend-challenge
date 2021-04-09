package com.altruist.resources

import com.altruist.config.ApplicationConfiguration
import com.altruist.model.Address
import com.altruist.model.State
import com.altruist.service.AddressService
import com.altruist.service.impl.AddressServiceImpl
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
import static org.hamcrest.Matchers.equalTo
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [AddressController])
@Import(value = [ApplicationConfiguration])
class AddressControllerTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    AddressService mockAddressService

    @Shared
    Address address

    def setup() {
        address = new Address(
                name: "Some Name",
                street: "Some street",
                city: "Some city",
                state: State.CA,
                zipcode: 99999
        )
    }

    def "Should accept address request"() {
        given: "an address request"
        UUID expectedId = UUID.randomUUID()
        UUID expectedAccountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$expectedAccountId/address")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address))
        )

        then: "the request is processed"
        1 * mockAddressService.create(expectedAccountId, address) >> expectedId

        and: "a Created response is returned"
        results.andExpect(status().isCreated())

        and: "the order ID is returned"
        results.andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        containsString("/accounts/$expectedAccountId/address")))
        results.andExpect(content().json("""{"id":"$expectedId"}"""))
    }

    @Unroll
    def "Should not accept address without required field #field"() {
        given: "an address request"
        UUID accountId = UUID.randomUUID()
        address[field] = null

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                post("/accounts/$accountId/address")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address))
        )


        then: "a BadRequest response is returned"
        results.andExpect(status().isBadRequest())

        where:
        field << ["name", "street", "city", "state", "zipcode"]
    }

    def "Should return the address for the account"() {
        given: "an address request"
        UUID accountId = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                get("/accounts/$accountId/address")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
        )

        then: "the list method on server is called"
        1 * mockAddressService.findByAccountUuid(accountId) >> Optional.of(address)

        and: "request status is ok"
        results.andExpect(status().isOk())

        and: "the returned element is the same"
        results.andExpect(jsonPath("\$.name", equalTo(address.name)))
        results.andExpect(jsonPath("\$.zipcode", equalTo(address.zipcode)))
        results.andExpect(jsonPath("\$.state", equalTo(address.state.toString())))
        results.andExpect(jsonPath("\$.city", equalTo(address.city)))
        results.andExpect(jsonPath("\$.street", equalTo(address.street)))

    }

    def "Should return no content for missing address"() {
        given: "an address request"
        UUID accountId = UUID.randomUUID()

        and: "A service can't find the address for the account"
        1 * mockAddressService.findByAccountUuid(accountId) >> Optional.empty()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                get("/accounts/$accountId/address")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
        )

        then: "request status is no content"
        results.andExpect(status().isNoContent())
    }

    def "should update an address"() {
        given: "an address"
        UUID accountUuid = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                put("/accounts/$accountUuid/address")
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address))
        )

        then: "The service method is called"
        1 * mockAddressService.update(accountUuid, address)

        and: "request status is accepted"
        results.andExpect(status().isAccepted())
    }

    def "should delete an address"() {
        given: "an address"
        UUID accountUuid = UUID.randomUUID()

        when: "the request is submitted"
        ResultActions results = mvc.perform(
                delete("/accounts/$accountUuid/address")
        )

        then: "The service method is called"
        1 * mockAddressService.deleteAddressFromAccount(accountUuid)

        and: "request status is accepted"
        results.andExpect(status().isAccepted())
    }


    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AddressService addressService() {
            factory.Mock(AddressServiceImpl)
        }

    }
}
