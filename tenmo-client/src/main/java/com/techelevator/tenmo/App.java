package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;

import java.math.BigDecimal;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);
    private final AccountService accountService = new AccountService();
    private final TransferService transferService = new TransferService();
    private final UserService userService = new UserService();

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        // TODO Auto-generated method stub
        int userId = currentUser.getUser().getId();
        accountService.setAuthToken(currentUser.getToken());
        Account account = accountService.getAccountByUserId(userId);
        System.out.println("Your current balance is: $" + account.getBalance());
    }

    private void viewTransferHistory() {
        // TODO Auto-generated method stub
        consoleService.printTransferListHeader();
        int userId = currentUser.getUser().getId();
        transferService.setAuthToken(currentUser.getToken());
        userService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());
        Transfer[] transferHistory = transferService.listTransfersByUserId(userId);
        try {
            for (Transfer t : transferHistory) {
                if (t.getAccount_from() == accountService.getAccountByUserId(userId).getAccount_id()) {
                    System.out.printf("%d    To: %-15s    $%.2f%n", t.getTransfer_id(), userService.getUserByAccountId(t.getAccount_to()).getUsername(), t.getAmount());
                } else if (t.getAccount_to() == accountService.getAccountByUserId(userId).getAccount_id()) {
                    System.out.printf("%d    From: %-15s  $%.2f%n", t.getTransfer_id(), userService.getUserByAccountId(t.getAccount_from()).getUsername(), t.getAmount());
                }
            }
        } catch (Exception e) {
            System.out.println("No transfer history to display.");
        }

        System.out.println("-----------------------------------");
        int menuSelection = -1;
        menuSelection = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
        if (menuSelection == 0) {
            mainMenu();
        } else if (menuSelection != 0) {
            for (Transfer t : transferHistory) {
                if (menuSelection == t.getTransfer_id()) {
                    consoleService.printTransferDetailsHeader();
                    System.out.println("Id: " + t.getTransfer_id());
                    if (t.getAccount_from() == accountService.getAccountByUserId(userId).getAccount_id()) {
                        System.out.println("To: " + userService.getUserByAccountId(t.getAccount_to()).getUsername());
                    } else if (t.getAccount_to() == accountService.getAccountByUserId(userId).getAccount_id()) {
                        System.out.println("From: " + userService.getUserByAccountId(t.getAccount_from()).getUsername());
                    }
                    if (t.getTransfer_type_id() == 1) {
                        System.out.println("Type: Request");
                    } else if (t.getTransfer_type_id() == 2) {
                        System.out.println("Type: Send");
                    }
                    if (t.getTransfer_status_id() == 1) {
                        System.out.println("Status: Pending");
                    } else if (t.getTransfer_status_id() == 2) {
                        System.out.println("Status: Approved");
                    } else if (t.getTransfer_status_id() == 3) {
                        System.out.println("Status: Rejected");
                    }
                    System.out.println("Amount: $" + t.getAmount());
                }
            }
        } else {
            System.out.println("The transfer number you have entered in invalid.");
        }
    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub

    }

    private void sendBucks() {
        // TODO Auto-generated method stub
        userService.setAuthToken(currentUser.getToken());
        transferService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());

        User[] users = userService.getUsers();

        // Display list of users
        for (User u : users) {
            if (u.getId() != currentUser.getUser().getId()) {
                System.out.println(u.getId() + "     " + u.getUsername());
            }
        }

        System.out.println();
        int menuSelection = consoleService.promptForInt("Please enter the user ID you wish to request money from (0 to cancel): ");
        if (menuSelection == 0) {
            mainMenu();
        } else if (menuSelection != 0) {
            boolean matchFound = false;

            // Check if the selected user ID is valid
            for (User u : users) {
                if (u.getId() == menuSelection) {
                    matchFound = true;

                    // Prompt for the amount to request
                    BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount of money you'd like to request: ");
                    int comparisonResult = amount.compareTo(new BigDecimal(0));

                    // Check if the requested amount is valid
                    if (comparisonResult > 0) {
                        Transfer requestTransfer = transferService.sendOrRequestMoney(currentUser.getUser().getId(), u.getId(), amount, true);

                        // Check if the request was successful
                        if (requestTransfer != null) {
                            System.out.println("Request sent successfully!");
                        } else {
                            System.out.println("Failed to send request. Please try again.");
                        }
                    } else {
                        System.out.println("You have entered an invalid number.");
                    }
                }
            }

            if (!matchFound) {
                System.out.println("Sorry, the user ID you entered is invalid.");
            }
        }

    }

    private void requestBucks() {
        // TODO Auto-generated method stub

        userService.setAuthToken(currentUser.getToken());
        transferService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());

        User[] users = userService.getUsers();

        // Display list of users
        for (User u : users) {
            if (u.getId() != currentUser.getUser().getId()) {
                System.out.println(u.getId() + "     " + u.getUsername());
            }
        }

        System.out.println();
        int menuSelection = consoleService.promptForInt("Please enter the user ID you wish to request money from (0 to cancel): ");
        if (menuSelection == 0) {
            mainMenu();
        } else if (menuSelection != 0) {
            boolean matchFound = false;

            // Check if the selected user ID is valid
            for (User u : users) {
                if (u.getId() == menuSelection) {
                    matchFound = true;

                    // Prompt for the amount to request
                    BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount of money you'd like to request: ");
                    int comparisonResult = amount.compareTo(new BigDecimal(0));

                    // Check if the requested amount is valid
                    if (comparisonResult > 0) {
                        Transfer requestTransfer = transferService.sendOrRequestMoney(currentUser.getUser().getId(), u.getId(), amount, true);

                        // Check if the request was successful
                        if (requestTransfer != null) {
                            System.out.println("Request sent successfully!");
                        } else {
                            System.out.println("Failed to send request. Please try again.");
                        }
                    } else {
                        System.out.println("You have entered an invalid number.");
                    }
                }
            }

            if (!matchFound) {
                System.out.println("Sorry, the user ID you entered is invalid.");
            }
        }
    }

}
