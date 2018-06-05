package com.revolut.transfer.rest;

import com.revolut.transfer.manager.AccountManager;
import com.revolut.transfer.model.Account;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;

@Path("/account")
public class AccountResource {


    private AccountManager accountManager = AccountManager.getInstance();

    public void setAccountManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        account = accountManager.createAccount(account);
        return Response.ok().entity(account).build();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAccounts() {
        List<Account> accounts = accountManager.getAllAccounts();
        return Response.ok().entity(accounts).build();
    }

    @GET
    @Path("/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@PathParam("accountId") Long accountId) {
        Account account = accountManager.getAccountById(accountId);
        return Response.ok().entity(account).build();
    }

    @POST
    @Path("/{originAccountId}/transfer/{destinationAccountId}/{amount}")
    public Response transferMoney(@PathParam("originAccountId") Long originAccountId, @PathParam("destinationAccountId") Long destinationAccountId, @PathParam("amount") BigDecimal amount) {
        accountManager.transfer(originAccountId, destinationAccountId, amount);
        return Response.ok().build();
    }

    @POST
    @Path("/clean/")
    public Response cleanAll() {
        accountManager.cleanAll();
        return Response.ok().build();
    }
}
