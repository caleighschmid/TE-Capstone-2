package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.*;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;


public class TransferService {
    public String API_BASE_URL = "http://localhost:8080";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;
    public void setAuthToken(String authToken){
        this.authToken = authToken;
    }

    public Transfer getTransferById(int transferId) {
        Transfer transfer = null;
        transfer = restTemplate.getForObject(API_BASE_URL + "/transfers/" + transferId, Transfer.class);
        return transfer;
    }

    public Transfer[] listTransfersByUserId(int user_id){
        Transfer[] transfers = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity <Account> entity = new HttpEntity<>(headers);
        try {
            transfers = restTemplate.exchange(API_BASE_URL + "/user/" + user_id + "/transfers", HttpMethod.GET, entity, Transfer[].class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer sendMoney(int user_id_from, int user_id_to, BigDecimal amount) {
//        Transfer transfer = new Transfer();
//        transfer.setAccount_from(account_from);
//        transfer.setAccount_to(account_to);
//        transfer.setAmount(amount);

        TransferDto transfer = new TransferDto();
        transfer.setUser_from_id(user_id_from);
        transfer.setUser_to_id(user_id_to);
        transfer.setAmount(amount);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransferDto> entity = new HttpEntity<>(transfer, headers);


        Transfer sendTransfer = null;

        sendTransfer = restTemplate.postForObject(API_BASE_URL + "/transfers/send/" + user_id_from + "/" +
                user_id_to + "/" + amount, entity, Transfer.class);

        return sendTransfer;
    }
}
