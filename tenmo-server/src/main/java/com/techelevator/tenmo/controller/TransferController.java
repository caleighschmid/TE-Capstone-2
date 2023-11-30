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

    @PreAuthorize("permitAll")
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
}
