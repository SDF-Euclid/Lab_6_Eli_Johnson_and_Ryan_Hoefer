import accounts.*;
import java.io.IOException;
import java.util.Scanner;

public class ATM {

    private final UserStore userStore;
    private final Scanner input = new Scanner(System.in);

    public ATM(UserStore userStore) {this.userStore = userStore;}

    public void run() throws IOException {
        IO.println("This ATM belongs to Ryan and Eli Banking®");
        boolean running = true;

        while (running) {
            IO.println("\nATM options:");
            IO.println("\t1. Create User");
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
        IO.println("To create a new user account, enter a username and password");
        System.out.print("Username: ");
        String username = input.next();
        System.out.print("Password: ");
        String password = input.next();

        if (userStore.findByUsername(username) != null) {
            IO.println("That username has been taken");
            return;
        }

        userStore.add(new User(username, password));
        IO.println("New user account created\n");
    }

    private void login() throws IOException {
        System.out.print("Username: ");
        String username = input.next();
        System.out.print("Password: ");
        String password = input.next();

        User user = userStore.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            IO.println("Invalid username or password");
            return;
        }

        if (user.getAccounts().isEmpty()) {
            createUserAccount(user);
        }
        showAccountMenu(user);
    }

    private void createUserAccount(User user) throws IOException {
        IO.println("\nYou currently have no current bank accounts. What type of account would you like to create?");
        System.out.print("Checking (c) or Savings (s): ");
        String selection = input.next();
        AbstractAccount newAccount = null;

        while (newAccount == null) {
            if (selection.equalsIgnoreCase("c")) {
                newAccount = new CheckingAccount(user, AccountType.CHECKING);
            } else if (selection.equalsIgnoreCase("s")) {
                newAccount = new SavingsAccount(user, AccountType.SAVINGS);
            } else {
                System.out.print("Invalid selection. Enter c or s: ");
                selection = input.next();
            }
        }

        user.getAccounts().add(newAccount);
        userStore.save();
        IO.println(newAccount.getAccountType() + " account created");
    }

    private void showAccountMenu(User user) throws IOException {
        while (true) {
            int accountCount = user.getAccounts().size();
            boolean canCreateAccount = accountCount < 2;
            int createOption = accountCount + 1;
            int logoutOption = canCreateAccount ? accountCount + 2 : accountCount + 1;

            IO.println("\nAccount(s)");
            for (int i = 0; i < accountCount; i++) {
                IO.println("\t" + (i + 1) + ". " + user.getAccounts().get(i));
            }

            if (canCreateAccount) {
                String accountName = user.getAccounts().getFirst().getAccountType() == AccountType.CHECKING
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
                createBankAccount(user);
                continue;
            }
            if (selection < 1 || selection > accountCount) {
                IO.println("Invalid selection.");
                continue;
            }

            showAccountActions(user, user.getAccounts().get(selection - 1));
        }
    }

    private void showAccountActions(User user, AbstractAccount account) throws IOException {
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
                case 4 -> transfer(user, checkingAccount);
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
                case 3 -> transfer(user, account);
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

    private void transfer(User user, AbstractAccount account) throws IOException {
        if (user.getAccounts().size() != 2) {
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

    private void createBankAccount(User user) throws IOException {
        if (user.getAccounts().getFirst().getAccountType() == AccountType.CHECKING) user.getAccounts().add(new SavingsAccount(user, AccountType.SAVINGS));
        else user.getAccounts().add(new CheckingAccount(user, AccountType.CHECKING));
        userStore.save();
        IO.println("Account successfully created");
    }
}
