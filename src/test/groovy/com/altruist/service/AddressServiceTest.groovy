package com.altruist.service

import com.altruist.exceptions.EntityNotFoundException
import com.altruist.exceptions.InvalidOperationException
import com.altruist.model.Account
import com.altruist.model.Address
import com.altruist.model.State
import com.altruist.repository.AccountRepository
import com.altruist.repository.AddressRepository
import com.altruist.repository.impl.AccountRepositoryImpl
import com.altruist.repository.impl.AddressRepositoryImpl
import com.altruist.service.impl.AddressServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@ContextConfiguration(classes = [TestConfig])
class AddressServiceTest extends Specification {
    @Autowired
    AddressRepository mockAddressRepository
    @Autowired
    AccountRepository mockAccountRepository
    @Autowired
    AddressService service

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


    def "Should save address"() {
        given: "an address"
        UUID expectedAddressId = UUID.randomUUID()

        when:
        service.create(address)

        then: "the address is saved"
        1 * mockAddressRepository.save(_) >> { Address arg ->
            with(arg) {
                name == address.name
                street == address.street
                city == address.city
                state == address.state
                zipcode == address.zipcode
            }

            arg.uuid = expectedAddressId
            arg
        }
    }

    def "Should save address for an account"() {
        given: "an address and an account"
        UUID expectedAccountId = UUID.randomUUID()
        UUID expectedAddressId = UUID.randomUUID()
        Account account = new Account(
                uuid: expectedAccountId,
                username: "someusername",
                email: "someemail@email.com",
                address: address
        )

        when:
        service.create(expectedAccountId, address)

        then: "the account is fetched from the database"
        1 * mockAccountRepository.findById(expectedAccountId) >> Optional.of(account)

        and: "the address doesn't exist for this account"
        1 * mockAddressRepository.findByAccountId(expectedAccountId) >> Optional.empty()

        and: "the address is saved"
        1 * mockAddressRepository.save(_) >> { Address arg ->
            with(arg) {
                name == address.name
                street == address.street
                city == address.city
                state == address.state
                zipcode == address.zipcode
            }

            arg.uuid = expectedAddressId
            arg
        }

        and: "the account is updated"
        1 * mockAccountRepository.update(account)
    }

    def "Should not save address for an invalid account"() {
        given: "an address and an account"
        UUID expectedAccountId = UUID.randomUUID()

        and: "the repository can't find the account"
        1 * mockAccountRepository.findById(expectedAccountId) >> Optional.empty()

        when:
        service.create(expectedAccountId, address)

        then: "should throw na exception"
        thrown(EntityNotFoundException)

    }

    def "Should update address"() {
        given: "an address with a valid uuid"
        UUID expectedAddressId = UUID.randomUUID()
        address.uuid = expectedAddressId

        when:
        service.update(address)

        then: "the address is updated"
        1 * mockAddressRepository.update(address)
    }

    def "Should not update address without an uuid"() {
        given: "an address without a valid uuid"
        address.uuid = null

        when:
        service.update(address)

        then: "the address is saved"
        thrown(InvalidOperationException)
    }

    def "Should update address for an account"() {
        given: "an address and an account"
        UUID expectedAccountId = UUID.randomUUID()
        UUID expectedAddressId = UUID.randomUUID()
        Account account = new Account(
                uuid: expectedAccountId,
                username: "someusername",
                email: "someemail@email.com",
                address: address
        )
        address.uuid = expectedAddressId

        when:
        service.update(expectedAccountId, address)

        then: "the account is fetched from the database"
        1 * mockAccountRepository.findById(expectedAccountId) >> Optional.of(account)

        and: "the address is fetched from the database"
        1 * mockAddressRepository.findByAccountId(expectedAccountId) >> Optional.of(address)

        and: "the address is updated"
        1 * mockAddressRepository.update(address)
    }

    def "Should not update nonexistent address for an account"() {
        given: "an account without address"
        UUID expectedAccountId = UUID.randomUUID()
        Account account = new Account(
                uuid: expectedAccountId,
                username: "someusername",
                email: "someemail@email.com"
        )
        address.uuid = UUID.randomUUID()

        and: "The repository can find the account "
        1 * mockAccountRepository.findById(expectedAccountId) >> Optional.of(account)

        and: "The repository can't find the address for the given account"
        1 * mockAddressRepository.findByAccountId(expectedAccountId) >> Optional.empty()

        when:
        service.update(expectedAccountId, address)

        then: "an exception is thrown"
        thrown(InvalidOperationException)
    }

    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AddressRepository addressRepository() {
            factory.Mock(AddressRepositoryImpl)
        }

        @Bean
        AccountRepository accountRepository() {
            factory.Mock(AccountRepositoryImpl)
        }

        @Bean
        AddressService addressService(AddressRepository addressRepository,
                                          AccountRepository accountRepository) {
            return new AddressServiceImpl(addressRepository, accountRepository)
        }

    }
}
