package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    Transfer createNewTransfer(int transfer_type_id, int transfer_status_id, int account_from, int account_to, BigDecimal amount);
    Transfer requestMoney(int account_from, int account_to, BigDecimal amount);
    Transfer sendMoney(int account_from, int account_to, BigDecimal amount);
    Transfer getTransferDetailsById(int transfer_id);
    List<Transfer> getTransfersByUserId(int user_id);
    Transfer approveRequest(Transfer transfer);
    Transfer rejectRequest(Transfer transfer);
}
