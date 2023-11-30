package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Transfer createNewTransfer(int transfer_type_id, int transfer_status_id, int account_from, int account_to, BigDecimal amount) {
        Transfer newTransfer = null;
        String sql =
                "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount)\n" +
                        "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";

        try {
            int newTransferId = jdbcTemplate.queryForObject(sql, int.class, transfer_type_id, transfer_status_id, account_from, account_to, amount);
            newTransfer = getTransferDetailsById(newTransferId);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return newTransfer;
    }

    @Override
    public Transfer requestMoney(int account_from, int account_to, BigDecimal amount) {
        Transfer requestTransfer = createNewTransfer(1, 1, account_from, account_to, amount);
        return requestTransfer;
    }

    @Override
    public Transfer sendMoney(int account_from, int account_to, BigDecimal amount) {
        String sendMoneySql = "BEGIN TRANSACTION;\n" +
                "UPDATE account SET balance=balance-? WHERE account_id=?;\n" +
                "UPDATE account SET balance=balance+? WHERE account_id=?;\n" +
                "COMMIT;";
        Transfer sendTransfer = createNewTransfer(2, 2, account_from, account_to, amount);
        jdbcTemplate.update(sendMoneySql, amount, account_from, amount, account_to);
        return sendTransfer;
    }

    @Override
    public Transfer getTransferDetailsById(int transfer_id) {
        Transfer transfer = null;
        String sql = "SELECT * FROM transfer WHERE transfer_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transfer_id);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return transfer;
    }

    @Override
    public List<Transfer> getTransfersByUserId(int user_id) {
        List<Transfer> pastTransfers = new ArrayList<>();
        String sqlSent = "SELECT * FROM transfer \n" +
                "JOIN account ON transfer.account_from = account.account_id\n" +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id\n" +
                "WHERE tenmo_user.user_id = ?;";
        String sqlReceived = "SELECT * FROM transfer\n" +
                "JOIN account ON transfer.account_to = account.account_id\n" +
                "JOIN tenmo_user ON tenmo_user.user_id = account.user_id\n" +
                "WHERE tenmo_user.user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sqlSent, user_id);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                pastTransfers.add(transfer);
            }
            results = jdbcTemplate.queryForRowSet(sqlReceived, user_id);
            while (results.next()) {
                Transfer transfer = mapRowToTransfer(results);
                pastTransfers.add(transfer);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return pastTransfers;
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rs.getInt("transfer_id"));
        transfer.setTransfer_type_id(rs.getInt("transfer_type_id"));
        transfer.setTransfer_status_id(rs.getInt("transfer_status_id"));
        transfer.setAccount_from(rs.getInt("account_from"));
        transfer.setAccount_to(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        return transfer;
    }
}
