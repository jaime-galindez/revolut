package com.revolut.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.transfer.model.Account;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.math.BigDecimal;

public class RestCallHelper {

    public static void cleanAll() throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://localhost:8080/account/clean/");
        client.execute(post);
    }

    public static Account getAccount(Long id) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:8080/account/" + id);
        get.addHeader("Accept", "*/*");
        HttpResponse response = client.execute(get);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity().getContent(), Account.class);
    }

    public static Account createAccount(BigDecimal amount) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        Account account = new Account();
        account.setAmount(amount);
        HttpPut put = new HttpPut("http://localhost:8080/account/");
        put.setEntity(new StringEntity("{\"amount\": " + amount.toString() + "}"));
        put.setHeader("Content-Type", "application/json");
        HttpResponse response = client.execute(put);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response.getEntity().getContent(), Account.class);
    }

    public static void transfer(Long originAccount, Long destinationAccount, BigDecimal amount) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://localhost:8080/account/" + originAccount + "/transfer/" + destinationAccount + "/" + amount.toString());
        HttpResponse response = client.execute(post);
    }
}
