package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class UserService {

    public String API_BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();


    public User[] getUsers(){
        User[] users = null;
        try {
            users = restTemplate.getForObject(API_BASE_URL + "/users", User[].class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }


}
