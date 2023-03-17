package com.mybank.atm;

import com.mybank.atm.bank.*;
import com.mybank.atm.cashdispenser.CashDispenser;
import com.mybank.atm.cashdispenser.ICashDispenser;
import com.mybank.atm.display.ConsoleDisplay;
import com.mybank.atm.display.IDisplay;
import com.mybank.atm.keypad.ConsoleKeypad;
import com.mybank.atm.keypad.IKeypad;
import com.mybank.atm.logging.FileTransactionLogger;
import com.mybank.atm.logging.ITransactionLogger;
import com.mybank.atm.receiptprinter.IReceiptPrinter;
import com.mybank.atm.receiptprinter.Receipt;
import com.mybank.atm.receiptprinter.ReceiptPrinter;
import com.mybank.atm.security.BiometricScanner;
import com.mybank.atm.security.IBiometricScanner;
import com.mybank.atm.session.ISession;
import com.mybank.atm.session.Session;
import com.mybank.atm.session.SessionExpiredException;
import com.mybank.atm.session.SessionNotFoundException;
import com.mybank.atm.transaction.Transaction;
import com.mybank.atm.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ATM implements IATM {
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final long ACCOUNT_LOCK_TIME = 5 * 60 * 1000; // 5 minutes
    public static BigDecimal MAX_WITHDRAWAL_AMOUNT = new BigDecimal("500.00");
    public static BigDecimal MAX_TRANSFER_AMOUNT = new BigDecimal("1000.00");
    public static final BigDecimal TRANSACTION_FEE_AMOUNT = new BigDecimal("2.50");
    public static final long TIMEOUT_PERIOD = 2 * 60 * 1000; // 2 minutes

    private IDisplay display;
    private IKeypad keypad;
    private ICashDispenser cashDispenser;
    private IReceiptPrinter receiptPrinter;
    private IBank bank;
    private IBiometricScanner biometricScanner;
    private ITransactionLogger transactionLogger;
    private ISession session;

    public ATM(IDisplay display, IKeypad keypad, ICashDispenser cashDispenser, IReceiptPrinter receiptPrinter, IBank bank, IBiometricScanner biometricScanner, ITransactionLogger transactionLogger, ISession session) {
        this.display = display;
        this.keypad = keypad;
        this.cashDispenser = cashDispenser;
        this.receiptPrinter = receiptPrinter;
        this.bank = bank;
        this.biometricScanner = biometricScanner;
        this.transactionLogger = transactionLogger;
        this.session = session;
    }

    public void login(String ClientId, int PIN) throws ATMException {
            if (biometricScanner.authenticateUser(ClientId)) {
                // Authenticate user using biometric data
            } else {
                // Authenticate user using PIN
                if (!bank.authenticateClient(ClientId, PIN)) {
                    throw new ATMException("Invalid user ID or PIN.");
                }
            }

            try {
                if (bank.isAccountLocked(ClientId)) {
                    throw new ATMException("Your account is locked. Please contact customer support.");
                }
            } catch (ClientNotFoundException e) {
                throw new ATMException(e.getMessage());
            }


            session.createSession(ClientId);
    }

    public Receipt deposit(BigDecimal amount, AccountType type) throws ATMException {
        String ClientId = session.getClientId();

        try {

            BigDecimal balance = bank.getAccountBalance(ClientId, type);
            BigDecimal newBalance = balance.add(amount);
            bank.updateBalance(ClientId, type, newBalance);

            Transaction transaction = new Transaction(TransactionType.DEPOSIT, amount, ClientId, Account.getAccountId(type));
            transactionLogger.logTransaction(transaction);
        } catch (AccountNotFoundException e) {
            throw new ATMException("Account not found.");
        } catch (ClientNotFoundException e) {
            throw new ATMException("Client not found.");
        }
        return null;
    }

    public Receipt withdraw(BigDecimal amount, AccountType type) throws ATMException {
        String ClientId = session.getClientId();

        try {
            BigDecimal balance = bank.getAccountBalance(ClientId, type);
            if (balance.compareTo(amount) < 0) {
                throw new ATMException("Insufficient funds.");
            }

            if (amount.compareTo(MAX_WITHDRAWAL_AMOUNT) > 0) {
                throw new ATMException("Exceeded maximum withdrawal amount.");
            }

            if (!cashDispenser.hasEnoughCash(amount)) {
                throw new ATMException("Unable to dispense cash. Please try a smaller amount.");
            }

            MAX_WITHDRAWAL_AMOUNT = MAX_WITHDRAWAL_AMOUNT.subtract(amount);
            cashDispenser.dispenseCash(amount);

            BigDecimal newBalance = balance.subtract(amount.add(TRANSACTION_FEE_AMOUNT));
            bank.updateBalance(ClientId, type, newBalance);

            Transaction transaction = new Transaction(TransactionType.WITHDRAWAL, amount, ClientId, Account.getAccountId(type));
            transactionLogger.logTransaction(transaction);
        } catch (AccountNotFoundException e) {
            throw new ATMException("Account not found.");
        } catch (ClientNotFoundException e) {
            throw new ATMException("Client not found.");
        }
        return null;
    }

    public Receipt InternalTransfer(AccountType fromAccount, AccountType toAccount, BigDecimal amount) throws ATMException {
        String ClientId = session.getClientId();

        try {
            BigDecimal sendingAccountBalance = bank.getAccountBalance(ClientId, fromAccount);
            if (sendingAccountBalance.compareTo(amount) < 0) {
                throw new ATMException("Insufficient funds.");
            }

            BigDecimal newSendingAccountBalance = sendingAccountBalance.subtract(amount);
            bank.updateBalance(ClientId, fromAccount, newSendingAccountBalance);

            if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
                throw new ATMException("Exceeded maximum transfer amount.");
            }

            MAX_TRANSFER_AMOUNT = MAX_TRANSFER_AMOUNT.subtract(amount);

            BigDecimal receivingAccountBalance = bank.getAccountBalance(ClientId, toAccount);
            BigDecimal newReceivingAccountBalance = receivingAccountBalance.add(amount);
            bank.updateBalance(ClientId, toAccount, newReceivingAccountBalance);

            Transaction transaction = new Transaction(TransactionType.TRANSFER, amount, ClientId,
                    Account.getAccountId(fromAccount), Account.getAccountId(toAccount));
            transactionLogger.logTransaction(transaction);

        } catch (AccountNotFoundException e) {
            throw new ATMException("Account not found.");
        } catch (ClientNotFoundException e) {
            throw new ATMException("Client not found.");
        }
        return null;
    }

    public Receipt ExternalTransfer(AccountType fromAccount, AccountType toAccount, String toClientId, BigDecimal amount) throws ATMException {
        String fromClientId = session.getClientId();

        try {
            BigDecimal sendingAccountBalance = bank.getAccountBalance(fromClientId, fromAccount);
            if (sendingAccountBalance.compareTo(amount) < 0) {
                throw new ATMException("Insufficient funds.");
            }

            BigDecimal newSendingAccountBalance = sendingAccountBalance.subtract(amount.add(TRANSACTION_FEE_AMOUNT));
            bank.updateBalance(fromClientId, fromAccount, newSendingAccountBalance);

            if (amount.compareTo(MAX_TRANSFER_AMOUNT) > 0) {
                throw new ATMException("Exceeded maximum transfer amount.");
            }

            MAX_TRANSFER_AMOUNT = MAX_TRANSFER_AMOUNT.subtract(amount);

            BigDecimal receivingAccountBalance = bank.getAccountBalance(toClientId, toAccount);
            BigDecimal newReceivingAccountBalance = receivingAccountBalance.add(amount);
            bank.updateBalance(toClientId, toAccount, newReceivingAccountBalance);

            Transaction transaction = new Transaction(TransactionType.TRANSFER, amount, fromClientId, Account.getAccountId(fromAccount),
                    toClientId, Account.getAccountId(toAccount));
            transactionLogger.logTransaction(transaction);
        } catch (AccountNotFoundException e) {
            throw new ATMException("Account not found.");
        } catch (ClientNotFoundException e) {
            throw new ATMException("Client not found.");
        }
        return null;
    }

    public void viewBalance(AccountType type) throws ATMException {
        String ClientId = session.getClientId();

        try {
            BigDecimal balance = bank.getAccountBalance(ClientId, type);
            display.show("Checking account balance: " + balance);
        } catch (AccountNotFoundException e) {
            throw new ATMException("Account not found.");
        } catch (ClientNotFoundException e) {
            throw new ATMException("Client not found.");
        }
    }

    public void endSession() throws ATMException {
        try {
            session.endSession();
        } catch (SessionNotFoundException e) {
            throw new ATMException("Session not found.");
        } catch (SessionExpiredException e) {
            throw new ATMException("Session expired.");
        }
    }

    public void run() {
        try {
            display.show("Welcome to the ATM. Please select an option:\n");
            display.show("1. Create Account\n");
            display.show("2. Login\n");
            display.show("3. Exit\n");
            display.show("Choice: ");
            String input = getInput();

            while (true) {
                try {
                    if (input.equals("1")) {

                        createAccount();

                    } else if (input.equals("2")) {

                        if (!performLogin()) break;

                        display.show("\nWhat do you want to do today?\n");
                        display.show("1. Withdrawal with receipt\n");
                        display.show("2. Withdrawal without receipt\n");
                        display.show("3. Deposit\n");
                        display.show("4. Transfer\n");
                        display.show("5. Information on the account\n");
                        display.show("6. Exit\n");
                        display.show("Choice: ");
                        input = getInput();

                        boolean withReceipt = input.equals("1");

                        switch (input) {
                            case "1":
                            case "2":
                                performWithdrawal(withReceipt);
                                break;

                            case "3":
                                performDeposit();
                                break;

                            case "4":
                                display.show("Transfer not implemented yet\n");
                                break;

                            case "5":
                                display.show("Choose an option\n");
                                display.show("1. Client ID");
                                display.show("2. List of accounts\n");
                                display.show("3. Query balance\n");
                                display.show("Choice: ");
                                input = getInput();

                                switch (input) {
                                    case "1":
                                        display.show("Small receipt not implemented yet\n");
                                        break;

                                    case "2":
                                        display.show("List of accounts not implemented yet\n");
                                        break;

                                    case "3":
                                        display.show("Query balance not implemented yet\n");
                                        break;

                                    default:
                                        display.show("Invalid input\n");
                                        break;
                                }
                                break;

                            case "6":
                                break;

                            default:
                                display.show("Invalid input\n");
                                break;
                        }
                    } else if (input.equals("3")) {
                        break;
                    } else {
                        display.show("Invalid input\n");
                    }

                    display.show("\nPlease select an option:\n");
                    display.show("1. Create Account\n");
                    display.show("2. Login\n");
                    display.show("3. Exit\n");
                    display.show("Choice: ");
                    input = keypad.getInput();

                } catch (NumberFormatException | NoSuchElementException e) {
                    display.show("Error: Invalid input\n");
                    display.show("Choice: ");
                    input = keypad.getInput();
                }
            }
        } catch (NoSuchElementException e){
            display.show("Error: Invalid input\n");
            run();
        }
        display.clear();
        display.show("Good bye");
    }

    private boolean performLogin() {
        while (true) {
            try {
                display.show("Enter your Client ID: ");
                String clientId = getInput();

                display.show("Enter your PIN: ");
                int PIN = Integer.parseInt(keypad.getInput());

                login(clientId, PIN);
                return true;
            }catch (ATMException e){
                display.show(e.getMessage());
                if (e.getMessage().equals("Your account is locked. Please contact customer support.")){
                    System.exit(0);
                }else {
                    display.show("\nDo you want to continue (yes/no): ");
                    String input = getInput().toLowerCase();
                    while (!input.matches("(yes|no)")) {
                        display.show("Invalid choice.");
                        display.show("\nDo you want to continue (yes/no): ");
                        input = getInput().toLowerCase();
                    }
                    if (input.equals("no")) {
                        return false;
                    }
                }
            }
        }
    }

    private void performWithdrawal(boolean withReceipt) {
        display.show("Which account do you want to withdraw from?\n");
        display.show("1. CHECKING_ACCOUNT\n");
        display.show("2. SAVINGS_ACCOUNT\n");
        display.show("3. INVESTMENT_ACCOUNT\n");
        display.show("4. CREDIT_ACCOUNT\n");
        display.show("Choice: ");
        String input = getInput();
        AccountType accountType;

        switch (input) {
            case "1":
                accountType = AccountType.CHECKING_ACCOUNT;
                break;

            case "2":
                accountType = AccountType.SAVINGS_ACCOUNT;
                break;

            case "3":
                accountType = AccountType.INVESTMENT_ACCOUNT;
                break;

            case "4":
                accountType = AccountType.CREDIT_ACCOUNT;
                break;

            default:
                display.show("Invalid input\n");
                return;
        }

        while (true) {
            display.show("How much do you want to withdraw: ");
            String amountStr = getInput();

            try {
                BigDecimal amount = new BigDecimal(amountStr);
                Receipt receipt = withdraw(amount, accountType);

                if (withReceipt) {
                    receiptPrinter.printReceipt(receipt);
                }

                break;
            } catch (NumberFormatException e) {
                display.show("Error: Invalid amount\n");
            } catch (ATMException e) {
                display.show("Error: " + e.getMessage() + "\n");
                break;
            }
        }
    }

    private void createAccount(){
        display.show("\nEnter your name: ");
        String name = getInput();

        display.show("\nEnter a four digit PIN: ");
        String PIN = getInput();
        while (!PIN.matches("\\d{4}")){
            display.show("Invalid PIN. ");
            display.show("Enter a four digit PIN: ");
            PIN = getInput();
        }


        display.show("\nEnter your initial deposit: ");
        String amountStr = getInput();
        while (amountStr.matches("[\\-\\D]")){
            display.show("\nInvalid Amount");
            display.show("Enter your initial deposit: ");
            amountStr = getInput();
        }

        BigDecimal amount = new BigDecimal(amountStr);

        String clientId = bank.generateClientId();
        Client client = new Client(clientId, name, Integer.parseInt(PIN), new HashMap<>());
        Account account = new Account(clientId, AccountType.CHECKING_ACCOUNT, "Checking Account", "USD", amount);
        bank.addClient(client);
        bank.addAccount(client, account);

        display.show(String.format("%nAccount Creation was successful!%n%s your ClientID Is: %s%nYou can now login",name,clientId));
    }


    private void performDeposit() {
        display.show("In which account do you want to deposit?\n");
        display.show("1. CHECKING_ACCOUNT\n");
        display.show("2. SAVINGS_ACCOUNT\n");
        display.show("3. INVESTMENT_ACCOUNT\n");
        display.show("4. CREDIT_ACCOUNT\n");
        display.show("Choice: ");
        String input = getInput();
        AccountType accountType;

        switch (input) {
            case "1":
                accountType = AccountType.CHECKING_ACCOUNT;
                break;

            case "2":
                accountType = AccountType.SAVINGS_ACCOUNT;
                break;

            case "3":
                accountType = AccountType.INVESTMENT_ACCOUNT;
                break;

            case "4":
                accountType = AccountType.CREDIT_ACCOUNT;
                break;

            default:
                display.show("Invalid input\n");
                return;
        }

        while (true) {
            display.show("How much do you want to deposit: ");
            String amountStr = getInput();

            try {
                BigDecimal amount = new BigDecimal(amountStr);
                Receipt receipt = deposit(amount, accountType);

                receiptPrinter.printReceipt(receipt);
                break;
            } catch (NumberFormatException e) {
                display.show("Error: Invalid amount\n");
            } catch (ATMException | NoSuchElementException e) {
                display.show("Error: " + e.getMessage() + "\n");
                break;
            }
        }
    }
        public String getInput(){
        return keypad.getInput();
        }
        public static void main (String[]args){
        IBiometricScanner biometricScanner = new BiometricScanner();
        IDisplay display = new ConsoleDisplay();
        IKeypad keypad = new ConsoleKeypad();
        ICashDispenser cashDispenser = new CashDispenser();
        IReceiptPrinter receiptPrinter = new ReceiptPrinter();
        IBank bank = new Bank();
        ISession session = new Session();
        ITransactionLogger transactionLogger = new FileTransactionLogger();
        ATM atm = new ATM(display, keypad, cashDispenser, receiptPrinter, bank, biometricScanner,transactionLogger,session);
        atm.run();

        }
    }
