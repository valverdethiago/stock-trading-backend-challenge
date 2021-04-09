package com.altruist.repository

import com.altruist.config.DatabaseConfiguration
import com.altruist.config.RepositoryConfiguration
import com.altruist.model.Account
import com.altruist.repository.impl.AccountRepositoryImpl
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
class AccountRepositoryTest extends Specification {
    @Autowired
    AccountRepository repo

    @Shared
    Account account

    def setup() {
        account = new Account(
                username: "username123",
                email: "email@example.com"
        )
    }

    def "Inserts an account"() {
        given: "an account"

        when:
        repo.save(account)

        then: "the account id is returned"
        account.uuid
    }

    def "Returns list of accounts"() {
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

        and: "All accounts are saved"
        accounts.each {item -> repo.save(item)}

        when: "The list method is called"
        List<Account> accountList = repo.listAll()

        then: "The returned list has the same size"
        accountList.size() == accounts.length
    }

    def "Updates an account"() {
        given: "an account"
        account = repo.save(account)
        account.username = "updated-$account.username"
        account.email = "updated-$account.email"

        when:
        repo.update(account)
        Account upToDateAccount = repo.findById(account.uuid).get()

        then: "the account is returned"
        upToDateAccount

        and: "the account information is up to date"
        upToDateAccount.username == account.username
        upToDateAccount.email == account.email

    }

    def "A blank list should not cause an exception "() {
        when:
        Account[] accounts = repo.listAll()

        then: "the list is empty"
        accounts.length == 0
    }
}
