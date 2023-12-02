package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.UserCredentials;

public class AccountService {

    public String API_BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;
    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }
//
//    public Account getAccountById(int accountId) {
//        Account account = null;
//        account = restTemplate.getForObject(API_BASE_URL + "/accounts/" + accountId, Account.class);
//        return account;
//    }

    public Account getAccountByUserId(int userId) {
        Account account = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(authToken);
            HttpEntity <Account> entity = new HttpEntity<>(headers);
            account = restTemplate.exchange(API_BASE_URL + "/accountsByUserId/" + userId, HttpMethod.GET, entity, Account.class).getBody();
        } catch (RestClientResponseException e) {
            System.out.println(e.getMessage());
        }
        return account;
    }

}
