package com.techelevator.tenmo.controller;

import javax.validation.Valid;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//@PreAuthorize("isAuthenticated()")
@RestController
public class TransferController {

    private TransferDao transferDao;

    public TransferController(TransferDao transferDao) {
        this.transferDao = transferDao;
    }

    @Autowired
    private AccountController accountController;

    @RequestMapping(path = "/transfers/{transfer_id}", method = RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int transfer_id) {
        Transfer transfer = transferDao.getTransferDetailsById(transfer_id);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
        } else {
            return transfer;
        }
    }

    @RequestMapping(path = "/transfers/send/{user_id_from}/{user_id_to}/{amount}", method = RequestMethod.POST)
    public Transfer sendMoney(@PathVariable int user_id_from,
                              @PathVariable int user_id_to,
                              @PathVariable BigDecimal amount) {

        int account_from = accountController.getAccountByUserId(user_id_from).getAccount_id();
        int account_to = accountController.getAccountByUserId(user_id_to).getAccount_id();

        Transfer sendMoneyTransfer = transferDao.sendMoney(account_from, account_to, amount);
        return sendMoneyTransfer;
    }

    @RequestMapping(path = "/user/{user_id}/transfers", method = RequestMethod.GET)
    public List<Transfer> getTransfersByUserId(@PathVariable int user_id) {
        List<Transfer> pastTransfers = transferDao.getTransfersByUserId(user_id);
        return pastTransfers;
    }

    @RequestMapping(path = "/user/{user_id}/pendingtransfers", method = RequestMethod.GET)
    public List<Transfer> getPendingTransfersByUserId(@PathVariable int user_id) {
        List<Transfer> pendingTransfers = new ArrayList<>();
        List<Transfer> pastTransfers = transferDao.getTransfersByUserId(user_id);
        int account_id = accountController.getAccountByUserId(user_id).getAccount_id();

        for (Transfer t : pastTransfers) {
            if (t.getTransfer_status_id() == 1 && t.getAccount_from() == account_id) {
                pendingTransfers.add(t);
            }
        }
        return pendingTransfers;
    }


    @RequestMapping(path = "/transfers/request/{user_id_from}/{user_id_to}/{amount}", method = RequestMethod.POST)
    public Transfer requestMoney(@PathVariable int user_id_from,
                                 @PathVariable int user_id_to,
                                 @PathVariable BigDecimal amount) {
        int account_from = accountController.getAccountByUserId(user_id_from).getAccount_id();
        int account_to = accountController.getAccountByUserId(user_id_to).getAccount_id();

        Transfer requestMoneyTransfer = transferDao.requestMoney(account_from, account_to, amount);
        return requestMoneyTransfer;
    }


    @RequestMapping(path = "/transfer/{transfer_id}/approve", method = RequestMethod.PUT)
    public Transfer approveRequest(@RequestBody Transfer transfer, @PathVariable int transfer_id) {
        transfer.setTransfer_id(transfer_id);
        try {
            Transfer approvedTransfer = transferDao.approveRequest(transfer);
            return approvedTransfer;
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
        }
    }

    @RequestMapping(path = "/transfer/{transfer_id}/reject", method = RequestMethod.PUT)
    public Transfer rejectRequest(@RequestBody Transfer transfer, @PathVariable int transfer_id) {
        transfer.setTransfer_id(transfer_id);
        try {
            Transfer rejectedTransfer = transferDao.rejectRequest(transfer);
            return rejectedTransfer;
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found.");
        }
    }


}
