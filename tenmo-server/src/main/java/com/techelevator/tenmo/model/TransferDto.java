package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class TransferDto {

    private int user_from_id;
    private int user_to_id;
    private BigDecimal amount;
    private int transfer_id;
    private int transfer_type_id;
    private int transfer_status_id;
    private int account_from;
    private int account_to;

    public int getUser_from_id() {
        return user_from_id;
    }

    public void setUser_from_id(int user_from_id) {
        this.user_from_id = user_from_id;
    }

    public int getUser_to_id() {
        return user_to_id;
    }

    public void setUser_to_id(int user_to_id) {
        this.user_to_id = user_to_id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getTransfer_id() {
        return transfer_id;
    }

    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }

    public int getTransfer_type_id() {
        return transfer_type_id;
    }

    public void setTransfer_type_id(int transfer_type_id) {
        this.transfer_type_id = transfer_type_id;
    }

    public int getTransfer_status_id() {
        return transfer_status_id;
    }

    public void setTransfer_status_id(int transfer_status_id) {
        this.transfer_status_id = transfer_status_id;
    }

    public int getAccount_from() {
        return account_from;
    }

    public void setAccount_from(int account_from) {
        this.account_from = account_from;
    }

    public int getAccount_to() {
        return account_to;
    }

    public void setAccount_to(int account_to) {
        this.account_to = account_to;
    }
}
