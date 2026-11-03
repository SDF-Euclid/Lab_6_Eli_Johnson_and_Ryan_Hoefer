import accounts.*;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class ATM {

    private final UserStore userStore;
    private final UtilityStore utilityStore;
    private final Scanner input = new Scanner(System.in);

    public ATM(UserStore userStore, UtilityStore utilityStore) {
        this.userStore = userStore;
        this.utilityStore = utilityStore;
    }

    public void run() throws IOException {
        IO.println("This ATM belongs to Ryan and Eli Banking®");
        boolean running = true;

        while (running) {
            IO.println("\nATM options:");
            IO.println("\t1. Create BankUser");
            IO.println("\t2. Login");
            IO.println("\t3. Quit");

            System.out.print("Select an option: ");
            int selection = input.nextInt();
            IO.println();

            switch (selection) {
                case 1 -> createUser();
                case 2 -> login();
                case 3 -> {
                    IO.println("Bye!");
                    running = false;
                }
                default -> IO.println("Please select a valid option");
            }
        }
    }

    private void createUser() throws IOException {
        IO.println("What account do you want to create?");
        IO.println("\t1. Create bank user");
        IO.println("\t2. Utility user");

        System.out.print("Select an option: ");
        int selection = input.nextInt();
        IO.println();

        switch (selection) {
            case 1 -> {
                System.out.print("Username: ");
                String username = input.next();
                System.out.print("Password: ");
                String password = input.next();

                if (userStore.findByUsername(username) != null) {
                    IO.println("That username has been taken");
                    return;
                }
                userStore.add(new BankUser(username, password));
                IO.println("New bank user created");
            }
            case 2 -> {
                System.out.print("Username: ");
                String username = input.next();
                System.out.print("Password: ");
                String password = input.next();

                if (userStore.findByUsername(username) != null) {
                    IO.println("That username has been taken");
                    return;
                }
                userStore.add(new BankUser(username, password));
                IO.println("New utility user created");
            }
            default -> IO.println("Please select a valid option");
        }
    }

    private void login() throws IOException {
        System.out.print("Username: ");
        String username = input.next();
        System.out.print("Password: ");
        String password = input.next();

        BankUser bankUser = userStore.findByUsername(username);
        if (bankUser == null || !bankUser.getPassword().equals(password)) {
            IO.println("Invalid username or password");
            return;
        }

        if (bankUser.getAccounts().isEmpty()) {
            createUserBankAccount(bankUser);
        }
        showAccountMenu(bankUser);
    }

    private void createUserBankAccount(BankUser bankUser) throws IOException {
        IO.println("\nYou currently have no current bank accounts. What type of account would you like to create?");
        System.out.print("Checking (c) or Savings (s): ");
        String selection = input.next();
        AbstractAccount newAccount = null;

        while (newAccount == null) {
            if (selection.equalsIgnoreCase("c")) {
                newAccount = new CheckingAccount(bankUser, AccountType.CHECKING);
            } else if (selection.equalsIgnoreCase("s")) {
                newAccount = new SavingsAccount(bankUser, AccountType.SAVINGS);
            } else {
                System.out.print("Invalid selection. Enter c or s: ");
                selection = input.next();
            }
        }

        bankUser.getAccounts().add(newAccount);
        userStore.save();
        IO.println(newAccount.getAccountType() + " account created");
    }

    private void showAccountMenu(BankUser bankUser) throws IOException {
        while (true) {
            int accountCount = bankUser.getAccounts().size();
            boolean canCreateAccount = accountCount < 2;
            int createOption = accountCount + 1;
            int logoutOption = canCreateAccount ? accountCount + 2 : accountCount + 1;

            IO.println("\nAccount(s)");
            for (int i = 0; i < accountCount; i++) {
                IO.println("\t" + (i + 1) + ". " + bankUser.getAccounts().get(i));
            }

            if (canCreateAccount) {
                String accountName = bankUser.getAccounts().getFirst().getAccountType() == AccountType.CHECKING
                        ? "savings"
                        : "checking";
                IO.println("\t" + createOption + ". Create " + accountName + " account");
            }
            IO.println("\t" + logoutOption + ". Log out");

            System.out.print("Select an option: ");
            int selection = input.nextInt();
            IO.println();

            if (selection == logoutOption) {
                IO.println("Logged out");
                return;
            }
            if (canCreateAccount && selection == createOption) {
                createAdditionalBankAccount(bankUser);
                continue;
            }
            if (selection < 1 || selection > accountCount) {
                IO.println("Invalid selection.");
                continue;
            }

            showAccountActions(bankUser, bankUser.getAccounts().get(selection - 1));
        }
    }

    private void showAccountActions(BankUser bankUser, AbstractAccount account) throws IOException {
        if (account instanceof CheckingAccount checkingAccount) {
            IO.println("Checking account:");
            IO.println("\t1. View balance");
            IO.println("\t2. Withdraw");
            IO.println("\t3. Deposit");
            IO.println("\t4. Transfer");

            System.out.print("Select an option: ");
            int selection = input.nextInt();
            IO.println();

            switch (selection) {
                case 1 -> IO.println("Account amount: " + checkingAccount.getBalance());
                case 2 -> withdraw(checkingAccount);
                case 3 -> deposit(checkingAccount);
                case 4 -> transfer(bankUser, checkingAccount);
                default -> IO.println("Invalid selection.");
            }
        } else {
            IO.println("Savings account:");
            IO.println("\t1. View balance");
            IO.println("\t2. Deposit");
            IO.println("\t3. Transfer");

            System.out.print("Select an option: ");
            int selection = input.nextInt();
            IO.println();

            switch (selection) {
                case 1 -> IO.println("Account amount: " + account.getBalance());
                case 2 -> deposit(account);
                case 3 -> transfer(bankUser, account);
                default -> IO.println("Invalid selection.");
            }
        }
    }

    private void withdraw (CheckingAccount account) throws IOException {
        System.out.print("Amount to withdraw: ");
        long amount = input.nextLong();
        try {
            account.withdraw(amount);
            userStore.save();
        } catch (IllegalArgumentException e) {
            IO.println(e.getMessage());
        }
    }

    private void deposit(AbstractAccount account) throws IOException {
        System.out.print("Amount to deposit: ");
        long amount = input.nextLong();
        try {
            account.deposit(amount);
            userStore.save();
        } catch (IllegalArgumentException e) {
            IO.println(e.getMessage());
        }
    }

    private void transfer(BankUser bankUser, AbstractAccount account) throws IOException {
        if (bankUser.getAccounts().size() != 2) {
            IO.println("You do not have another account to transfer money to.");
            return;
        }
        System.out.print("Amount to transfer: ");
        long amount = input.nextLong();
        try {
            AccountType accountType = account.getAccountType();
            if (accountType == AccountType.CHECKING) {
                CheckingAccount checkingAccount = (CheckingAccount) account;
                checkingAccount.transfer(amount, AccountType.SAVINGS);
            } else if (accountType == AccountType.SAVINGS) {
                SavingsAccount savingsAccount = (SavingsAccount) account;
                savingsAccount.transfer(amount, AccountType.CHECKING);
            }
            userStore.save();
        } catch (IllegalArgumentException e) {
            IO.println(e.getMessage());
        }
    }

    private void createAdditionalBankAccount(BankUser bankUser) throws IOException {
        if (bankUser.getAccounts().getFirst().getAccountType() == AccountType.CHECKING) bankUser.getAccounts().add(new SavingsAccount(bankUser, AccountType.SAVINGS));
        else bankUser.getAccounts().add(new CheckingAccount(bankUser, AccountType.CHECKING));
        userStore.save();
        IO.println("Account successfully created");
    }

    private void showUtilityCompanyMenu() throws IOException {
        System.out.println("\nUtility Company");
        System.out.println("\t1. Create account");
        System.out.println("\t2. Login");
        System.out.println("\t3. Back");
        System.out.print("Select an option: ");

        switch (input.nextInt()) {
            case 1 -> createUtilityUser();
            case 2 -> utilityLogin();
            case 3 -> { }
            default -> System.out.println("Invalid selection.");
        }
    }

    private void createUtilityUser() throws IOException {
        System.out.print("Username: ");
        String username = input.next();
        System.out.print("Password: ");
        String password = input.next();

        if (UtilityStore.find(username) != null) {
            System.out.println("That username has been taken");
            return;
        }

        UtilityUser account = UtilityStore.create(username, password);
        System.out.println("Your account number is " + account.getAccountNumber());
    }

    private void utilityLogin() {
        System.out.print("Username or user number: ");
        String login = input.next();
        System.out.print("Password: ");
        String password = input.next();

        UtilityUser user = UtilityStore.find(login);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("Invalid login");
            return;
        }

        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nUtility user " + user.getAccountNumber());
            System.out.println("\t1. View payment history");
            System.out.println("\t2. View next bill");
            System.out.println("\t3. Log out");
            System.out.print("Select an option: ");

            switch (input.nextInt()) {
                case 1 -> showPaymentHistory(user);
                case 2 -> System.out.println("$" + user.getNextBill().getAmount()
                        + " due " + user.getNextBill().getDueDate());
                case 3 -> loggedIn = false;
                default -> System.out.println("Invalid selection.");
            }
        }
    }

    private void showPaymentHistory(UtilityUser user) {
        List<Bill> bills = user.getPaidBills();

        if (bills.isEmpty()) {
            System.out.println("No bills have been paid yet");
            return;
        }

        // Newest first, so the last three paid bills are at the top
        for (int i = bills.size() - 1; i >= 0; i--) {
            Bill bill = bills.get(i);
            System.out.println("$" + bill.getAmount() + " due " + bill.getDueDate() + ", paid " + bill.getPaidAt());
        }
    }

    private void payUtilities(CheckingAccount account) throws IOException {
        System.out.print("Utility account number: ");
        UtilityUser user = UtilityStore.find(input.next());

        if (user == null) {
            System.out.println("Utility account not found");
            return;
        }

        try {
            account.payUtilities(user);
            userStore.save();
            utilityStore.save();
            System.out.println("Bill paid");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}
