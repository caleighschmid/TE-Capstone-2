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

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Transfer getTransferById(int transferId) {
        Transfer transfer = null;
        transfer = restTemplate.getForObject(API_BASE_URL + "/transfers/" + transferId, Transfer.class);
        return transfer;
    }

    public Transfer[] listTransfersByUserId(int user_id) {
        Transfer[] transfers = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Account> entity = new HttpEntity<>(headers);
        try {
            transfers = restTemplate.exchange(API_BASE_URL + "/user/" + user_id + "/transfers",
                    HttpMethod.GET, entity, Transfer[].class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transfers;
    }

    public Transfer sendOrRequestMoney(int user_id_from, int user_id_to, BigDecimal amount, boolean isRequest) {

        TransferDto transfer = new TransferDto();
        transfer.setUser_from_id(user_id_from);
        transfer.setUser_to_id(user_id_to);
        transfer.setAmount(amount);
        transfer.setTransferType(isRequest ? "Request" : "Send");


        // Set initial status to "Pending" for request transfers
        transfer.setTransfer_status_id(isRequest ? 1 : 2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransferDto> entity = new HttpEntity<>(transfer, headers);

        Transfer sendOrRequestTransfer = null;

        String url = isRequest ? "/transfers/request/" : "/transfers/send/";

        sendOrRequestTransfer = restTemplate.postForObject(API_BASE_URL + url + user_id_from + "/" + user_id_to + "/" + amount, entity, Transfer.class);

        return sendOrRequestTransfer;


//        Transfer transfer = new Transfer();
//        transfer.setAccount_from(account_from);
//        transfer.setAccount_to(account_to);
//        transfer.setAmount(amount);
//
//        TransferDto transfer = new TransferDto();
//        transfer.setUser_from_id(user_id_from);
//        transfer.setUser_to_id(user_id_to);
//        transfer.setAmount(amount);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(authToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<TransferDto> entity = new HttpEntity<>(transfer, headers);
//
//
//        Transfer sendTransfer = null;
//
//        sendTransfer = restTemplate.postForObject(API_BASE_URL + "/transfers/send/" + user_id_from + "/" +
//                user_id_to + "/" + amount, entity, Transfer.class);
//
//        return sendTransfer;
    }

    public Transfer[] listPendingRequestsByUserId(int user_id) {
        Transfer[] pendingTransfers = null;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        HttpEntity<Account> entity = new HttpEntity<>(headers);

        try {
            pendingTransfers = restTemplate.exchange(API_BASE_URL + "/user/" + user_id + "/pendingtransfers",
                    HttpMethod.GET, entity, Transfer[].class).getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return pendingTransfers;
    }

    public boolean approveRequest(Transfer approvedTransfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transfer> entity = new HttpEntity<>(approvedTransfer, headers);

        boolean success = false;
        try {
            restTemplate.put(API_BASE_URL + "/transfer/" + approvedTransfer.getTransfer_id() + "/approve", entity);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public boolean rejectRequest(Transfer rejectedTransfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Transfer> entity = new HttpEntity<>(rejectedTransfer, headers);

        boolean success = false;
        try {
            restTemplate.put(API_BASE_URL + "/transfer/" + rejectedTransfer.getTransfer_id() + "/reject", entity);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }


}
