package com.techelevator.tenmo.controller;

import javax.validation.Valid;
import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

@Component
@RestController
@PreAuthorize("isAuthenticated()")
public class AccountController {

    private AccountDao accountDao;

    @Autowired
    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }

    @RequestMapping(path = "/accounts/{account_id}", method = RequestMethod.GET)
    public Account getAccountById(@PathVariable int account_id) {
        Account account = null;
//        try {
        account = accountDao.getAccountById(account_id);
//        } catch () {
//
//        }
        return account;
    }

    @RequestMapping(path = "/accountsByUserId/{user_id}", method = RequestMethod.GET)
    public Account getAccountByUserId(@PathVariable int user_id) {
        Account account = null;
//        try {
        account = accountDao.getAccountByUserId(user_id);
//        } catch () {
//
//        }
        return account;
    }

}
