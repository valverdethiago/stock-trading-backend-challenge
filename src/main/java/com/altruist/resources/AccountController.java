package com.altruist.resources;

import com.altruist.IdDto;
import com.altruist.model.Account;
import com.altruist.service.AccountService;
import com.altruist.utils.HttpUtils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Creates an account and return its id and url")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "201", description = "Account created"),
    })
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<IdDto> create(@RequestBody @Valid Account account,
                                        HttpServletRequest httpServletRequest) {
        log.info("Received Account creation request [{}].", account);
        UUID accountId = accountService.create(account);
        URI entityURI = HttpUtils.buildEntityUrl(httpServletRequest, accountId);
        return ResponseEntity.created(entityURI)
            .body(new IdDto(accountId));
    }

    @Operation(summary = "Lists all accounts on the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accounts list"),
        @ApiResponse(responseCode = "204", description = "No accounts found"),
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Account>> listAll() {
        log.info("Listing all accounts");
        List<Account> accounts = accountService.listAll();
        if(accounts == null || accounts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
            .body(accounts);
    }

    @Operation(summary = "Returns the account by the uuid informed on the path")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account information"),
        @ApiResponse(responseCode = "404", description = "Account Not found"),
    })
    @GetMapping(produces = APPLICATION_JSON_VALUE, value = "/{accountUuid}")
    public ResponseEntity<Account> get(@PathVariable("accountUuid") UUID accountUuid) {
        log.info("Fetching account with id[{}].", accountUuid);
        AtomicReference<ResponseEntity<Account>> result = new AtomicReference<>();
        accountService.findById(accountUuid).ifPresentOrElse(
            (account) -> result.set(ResponseEntity.ok(account)),
            () -> result.set(ResponseEntity.notFound().build())
        );
        return result.get();
    }

}
