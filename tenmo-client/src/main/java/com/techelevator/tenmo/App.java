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
        //Check if past transfer history exists
        boolean hasHistory = false;
        if (transferHistory.length != 0) {
            hasHistory = true;
        }
        //If history exists, print list of past transfers
        if (hasHistory) {
            for (Transfer t : transferHistory) {
                //Do not print pending transfers
                if (t.getTransfer_status_id() != 1) {
                    if (t.getAccount_from() == accountService.getAccountByUserId(userId).getAccount_id()) {
                        System.out.printf("%d    To: %-15s    $%.2f%n", t.getTransfer_id(), userService.getUserByAccountId(t.getAccount_to()).getUsername(), t.getAmount());
                    } else if (t.getAccount_to() == accountService.getAccountByUserId(userId).getAccount_id()) {
                        System.out.printf("%d    From: %-15s  $%.2f%n", t.getTransfer_id(), userService.getUserByAccountId(t.getAccount_from()).getUsername(), t.getAmount());
                    }
                }
            }
            //If no history exists, do not print list or prompt for details
        } else if (!hasHistory) {
            System.out.println("No transfer history to display.");
        }

        //If history exists, prompt for details
        if (hasHistory) {
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
                        System.out.println("From: " + userService.getUserByAccountId(t.getAccount_from()).getUsername());
                        System.out.println("To: " + userService.getUserByAccountId(t.getAccount_to()).getUsername());
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
                System.out.println("The transfer number you have entered is invalid.");
            }
        }
    }

    private void viewPendingRequests() {
        // TODO Auto-generated method stub

        consoleService.printPendingTransferListHeader();
        int userId = currentUser.getUser().getId();
        transferService.setAuthToken(currentUser.getToken());
        userService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());

        // Retrieve pending requests
        Transfer[] pendingRequests = transferService.listPendingRequestsByUserId(userId);

        if (pendingRequests.length == 0) {
            System.out.println("No pending requests to display.");
        } else {
            for (Transfer request : pendingRequests) {
                // Display pending request details
                System.out.printf("%d    %-15s    $%.2f%n",
                        request.getTransfer_id(),
                        userService.getUserByAccountId(request.getAccount_to()).getUsername(),
                        request.getAmount());
            }
            //Call method to approve or reject requests
            approveOrRejectRequests(pendingRequests);
        }
    }

    private void approveOrRejectRequests(Transfer[] pendingRequests) {
        transferService.setAuthToken(currentUser.getToken());
        accountService.setAuthToken(currentUser.getToken());
        userService.setAuthToken(currentUser.getToken());
        System.out.println("-----------------------------------");
        int menuSelection = -1;
        menuSelection = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");
        if (menuSelection == 0) {
            mainMenu();
        } else if (menuSelection != 0) {
            boolean matchFound = false;
            for (Transfer request : pendingRequests) {
                if (menuSelection == request.getTransfer_id()) {
                    matchFound = true;
                    consoleService.printPendingTransferMenu();
                    System.out.println("-----------------------------------");
                    menuSelection = consoleService.promptForInt("Please select an option: ");
                    //Process to approve request
                    if (menuSelection == 1) {
                        //Check for sufficient funds
                        BigDecimal accountBalance = accountService.getAccountByUserId(currentUser.getUser().getId()).getBalance();
                        int balanceComparisonResult = accountBalance.compareTo(request.getAmount());
                        if (balanceComparisonResult > 0) {
                            boolean success = transferService.approveRequest(request);
                            if (success) {
                                System.out.println("You have successfully approved the transfer.");
                            } else {
                                System.out.println("Something went wrong.  Please try again.");
                            }
                        } else {
                            System.out.println("You do not have sufficient funds to approve this request.");
                        }
                        //Process to reject request
                    } else if (menuSelection == 2) {
                        boolean success = transferService.rejectRequest(request);
                        if (success) {
                            System.out.println("You have successfully rejected the transfer.");
                        } else {
                            System.out.println("Something went wrong.  Please try again.");
                        }
                    } else if (menuSelection != 0) {
                        System.out.println("You have selected an invalid option.  Please try again.");
                    }
                }
            }
            if (!matchFound) {
                System.out.println("You have entered an invalid transfer ID.");
            }
        }
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
        int menuSelection = consoleService.promptForInt("Please enter the user ID you wish to send money to (0 to cancel): ");
        if (menuSelection == 0) {
            mainMenu();
        } else if (menuSelection != 0) {
            boolean matchFound = false;

            // Check if the selected user ID is valid
            for (User u : users) {
                if (u.getId() == menuSelection) {
                    matchFound = true;

                    // Prompt for the amount to request
                    BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount of money you'd like to send: ");
                    int comparisonResult = amount.compareTo(new BigDecimal(0));
                    BigDecimal accountBalance = accountService.getAccountByUserId(currentUser.getUser().getId()).getBalance();
                    int balanceComparisonResult = accountBalance.compareTo(amount);

                    // Check if the requested amount is valid
                    if (comparisonResult > 0 && balanceComparisonResult > 0) {
                        Transfer requestTransfer = transferService.sendOrRequestMoney(currentUser.getUser().getId(), u.getId(), amount, false);
                        // Check if the request was successful
                        if (requestTransfer != null) {
                            System.out.println("Transfer sent successfully!");
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
                        Transfer requestTransfer = transferService.sendOrRequestMoney(u.getId(), currentUser.getUser().getId(), amount, true);

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
