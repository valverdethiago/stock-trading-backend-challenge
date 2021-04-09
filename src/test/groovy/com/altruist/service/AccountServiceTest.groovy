package com.altruist.service

import com.altruist.exceptions.InvalidOperationException
import com.altruist.model.Account
import com.altruist.model.Address
import com.altruist.model.State
import com.altruist.repository.impl.AccountRepositoryImpl
import com.altruist.service.impl.AccountServiceImpl
import com.altruist.service.impl.AddressServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import spock.lang.Shared
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@ContextConfiguration(classes = [TestConfig])
class AccountServiceTest extends Specification {
    @Autowired
    AccountRepositoryImpl mockAccountRepository
    @Autowired
    AddressServiceImpl mockAddressService
    @Autowired
    AccountServiceImpl service

    @Shared
    Account account

    def setup() {
        account = new Account(
                username: "username123",
                email: "email@example.com",
                address : new Address(
                        name: "Some Name",
                        street: "Some street",
                        city: "Some city",
                        state: State.CA,
                        zipcode: 99999
                )
        )
    }

    def "Should save account and address"() {
        given: "an account"
        UUID expectedAddressId = UUID.randomUUID()
        UUID expectedAccountId = UUID.randomUUID()

        when:
        service.create(account)

        then: "the address is saved"
        1 * mockAddressService.create(account.address) >> expectedAddressId

        and: "the account is saved"
        1 * mockAccountRepository.save(_) >> { Account arg ->
            with(arg){
                username == account.username
                email == account.email
                addressUuid == expectedAddressId
            }

            arg.uuid = expectedAccountId
            arg
        }
    }

    def "should return the persisted account" () {
        given: "An account"
        account.uuid = UUID.randomUUID()

        when: "The service method is called"
        Account dbAccount = service.findById(account.uuid).get()

        then: "The repository method is called"
        1 * mockAccountRepository.findById(account.uuid) >> Optional.of(account)

        and: "Account data is equal"
        account.uuid == dbAccount.uuid
    }

    def "should return the persisted accounts" () {
        given: "some accounts"
        Account[] accounts = [account,
                              new Account(
                                      username: "username2",
                                      email: "somemail2@mail.com"
                              ),
                              new Account(
                                      username: "username3",
                                      email: "somemail2@mail.com"
                              ),
                              new Account(
                                      username: "username4",
                                      email: "somemail2@mail.com"
                              )]

        when: "The service method is called"
        List<Account> dbAccounts = service.listAll()

        then: "The repository method is called"
        1 * mockAccountRepository.listAll() >> accounts

        and: "Accounts size is equal"
        accounts.length == dbAccounts.size()
    }

    def "should throw an exception updating an entity without uuid" () {
        given: " some account without uuid"
        account.uuid = null

        when: "the update method is called"
        service.update(account)

        then: "an exception should be thrown"
        thrown(InvalidOperationException)
    }

    def "should update an account without address" () {
        given: " some account without address"
        account.address = null

        and: " and with an uuid"
        account.uuid = UUID.randomUUID()

        when: "The update method is called"
        service.update(account)

        then: "The delete address method should be called"
        1 * mockAddressService.deleteAddressFromAccount(account.uuid)

        and: "the update method on repository is called"
        1 * mockAccountRepository.update(account)
    }

    def "should update an account with address" () {
        given: " some account with an uuid"
        account.uuid = UUID.randomUUID()
        UUID expectedAddressUuid = UUID.randomUUID()
        account.address.uuid = expectedAddressUuid

        when: "The update method is called"
        service.update(account)

        then: "The findByAccountUuid method should be called on address service"
        1 * mockAddressService.findByAccountUuid(account.uuid) >> Optional.of(account.address)

        and: "the update method on address service is called"
        1 * mockAddressService.update(account.address)
    }


    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        AccountRepositoryImpl accountRepository() {
            factory.Mock(AccountRepositoryImpl)
        }

        @Bean
        AddressServiceImpl addressService() {
            factory.Mock(AddressServiceImpl)
        }

        @Bean
        AccountServiceImpl accountService() {
            return new AccountServiceImpl(accountRepository(), addressService())
        }
    }
}
