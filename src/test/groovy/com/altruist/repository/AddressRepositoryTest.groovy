package com.altruist.repository

import com.altruist.config.DatabaseConfiguration
import com.altruist.config.RepositoryConfiguration
import com.altruist.model.Account
import com.altruist.model.Address
import com.altruist.model.State
import com.altruist.repository.impl.AddressRepositoryImpl
import org.junit.Before
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
class AddressRepositoryTest extends Specification {
    @Autowired
    AddressRepositoryImpl repository

    @Shared
    Address address

    @Before
    def "Initializes the address"() {
        address = new Address(
                name : "Some name",
                street: "Some street",
                city: "Some city",
                state: "CA",
                zipcode: 99999
        )
    }

    def "Inserts an address"() {
        given: "an address"

        when:
        repository.save(address)

        then: "the address id is returned"
        address.uuid
    }

    def "Updates an address"() {
        given: "an address"
        address = repository.save(address)
        address.zipcode = address.zipcode+1
        address.state = State.AK
        address.city = "updated-$address.city"
        address.street = "updated-$address.street"
        address.name = "updated-$address.name"

        when:
        repository.update(address)
        Address upToDateAddress = repository.findById(address.uuid).get()

        then: "the trade is returned"
        upToDateAddress

        and: "the trade information is up to date"
        upToDateAddress.zipcode == address.zipcode
        upToDateAddress.state == address.state
        upToDateAddress.city == address.city
        upToDateAddress.street == address.street
        upToDateAddress.name == address.name

    }

}
