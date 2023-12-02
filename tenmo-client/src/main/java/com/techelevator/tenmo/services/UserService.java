package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class UserService {

    public String API_BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();
    private String authToken = null;
    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public User[] getUsers(){
        User[] users = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Account> entity = new HttpEntity<>(headers);

        try {
//            users = restTemplate.getForObject(API_BASE_URL + "/users", User[].class);
            users = restTemplate.exchange(API_BASE_URL + "/users", HttpMethod.GET, entity, User[].class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public User getUserByAccountId(int account_id) {
        User user = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity <Account> entity = new HttpEntity<>(headers);
        try {
//            user = restTemplate.getForObject(API_BASE_URL + "/userByAccountId/" + account_id, User.class);
            user = restTemplate.exchange(API_BASE_URL + "/userByAccountId/" + account_id, HttpMethod.GET, entity, User.class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return user;
    }

}
