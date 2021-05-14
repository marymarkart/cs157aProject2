import java.sql.*;
import java.util.Scanner;

public class p2 {
    private static Scanner scanner = new Scanner(System.in);    //scanner for input
    private static String cusNum;                               //customer id
    private static int cusID;
    private static final String driver = "com.ibm.db2.jcc.DB2Driver";
    private static final String url = "jdbc:db2://127.0.0.1:50000/cs157a";
    private static final String user = "db2inst1";
    private static final String pw = "kenward";

    private static Connection connection = null;
    private static Statement stmt = null;
    private static ResultSet rs = null;

    /**
     * psvm where program starts. Connection to database is initialized with cs157a database
     * @param argv
     * @throws ClassNotFoundException
     */
    public static void main(String[] argv) throws ClassNotFoundException {
        System.out.println(":: PROGRAM START");
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            mainmenu();     //go to main menu
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Main menu for banking program
     * @throws ClassNotFoundException
     */
    private static void mainmenu() throws ClassNotFoundException {
        String choice = "";         //user menu selection String
        boolean n, g, a, p;         // validity check variables n = name, g = gender, a = age, p = pin
        n = g = a = p = false;      //intialize variables

        //print main menu
        System.out.println("Welcome to the Self Services Banking System! - Main Menu\n" +
                "1.    New Customer\n" +
                "2.    Customer Login\n" +
                "3.    Exit\n" +
                "");
        choice = scanner.nextLine();            //read user input menu selection
        String name, gender, age, pin;          //create input String variables
        name = gender = age = pin = "";         //initialize input
        int y = 0;
        int x = 0;
        //switch statement for choices
        switch (choice) {
            //New customer login
            case "1":
                while (!n){                     //check that the length of the name won't cause an error
                    System.out.println("What is your name?");
                    name = scanner.nextLine();
                    if (name.length() > 15){    //check length
                        System.out.println("The name you entered was too long, please keep the length under 15 characters");
                        n = false;              //return to ask for name again
                    }
                    else{
                        n = true;               //move on to gender
                    }
                }

                while (!g) {                    //check that the gender input is valid
                    System.out.println("Please input your gender (M or F)");
                    gender = scanner.nextLine();
                    if (!gender.equals("M") && !gender.equals("F")){ //check valid gender
                        System.out.println("You have not entered an invalid gender");
                        g = false;
                    }
                    else{
                        g = true;           //move on to age
                    }

                }
                while (!a){             //check valid age
                    System.out.println("Please enter your age");
                    age = scanner.nextLine();
                    try {
                        x = Integer.parseInt(age);      //get int x from String age
                        if (x < 0){
                            System.out.println("You have entered an invalid age");
                            a = false;
                        } else{
                            a = true;           //move on to pin
                        }
                    } catch (Exception E){
                        System.out.println("You have not entered a valid age");
                        a = false;
                    }


                }
                while (!p){             //check valid pin
                    System.out.println("Please enter a pin");
                    pin = scanner.nextLine();

                    try {
                        y = Integer.parseInt(pin);      //get int y from String pin
                        if (y < 0){
                            System.out.println("You have entered an invalid pin");
                            p = false;
                        } else {
                            p = true;           //move on to create customer
                        }
                    } catch (Exception E){
                        System.out.println("You have entered an invalid pin");
                        p = false;
                    }
                }
                try{
                    String sql = "{CALL p2.CUST_CRT(?,?,?,?,?,?,?)}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setString(1,name);
                    cs.setString(2,gender);
                    cs.setInt(3,x);
                    cs.setInt(4,y);
                    cs.registerOutParameter(5, Types.INTEGER);
                    cs.registerOutParameter(6, Types.INTEGER);
                    cs.registerOutParameter(7, Types.CHAR);
                    cs.executeUpdate();
                    System.out.println("Welcome, your customer ID is: "+ cs.getInt(5));   //display customer ID

                }catch(Exception E){
                    E.printStackTrace();
                }
                mainmenu();
            case "2":           //customer login
                boolean v = false;
                System.out.println("Enter your customer ID:");
                cusNum = scanner.nextLine();
                try{
                    x = Integer.parseInt(cusNum);
                    cusID = x;
                } catch (Exception E){
                    System.out.println("You have entered an invalid customer id");
                }
                System.out.println("Enter your pin:");
                pin = scanner.nextLine();
                try{
                    y = Integer.parseInt(pin);
                    if (cusNum.equals("0") && pin.equals("0")){         //check if admin account
                        adminMenu();
                    }
                    else{
                        try {
                            String sql = "{CALL p2.CUST_LOGIN(?, ?, ?,?,?);}";
                            CallableStatement cs = connection.prepareCall(sql);
                            cs.setInt(1, x);
                            cs.setInt(2, y);
                            cs.registerOutParameter(3, Types.INTEGER);
                            cs.registerOutParameter(4, Types.INTEGER);
                            cs.registerOutParameter(5, Types.CHAR);
                            cs.executeUpdate();
                            if (cs.getInt(4) == -100){
                                System.out.println(cs.getString(5));
                                mainmenu();
                            }
                            if (cs.getInt(3) == 1){
                                submenu();
                            }
                        } catch (Exception E) {
                            E.printStackTrace();
                        }
                    }
                } catch (Exception E){
                    System.out.println("You have entered an invalid customer id");
                }
                mainmenu();                                 //go back to main menu
            case "3":
                System.out.println("Goodbye!\n" +
                        ":: PROGRAM END");             //Exit
                System.exit(0);                       //Exit program

            default:
                System.out.println("Invalid Choice");       //handle invalid input for main menu
                mainmenu();
        }
    }

    /**
     * Menu for admin
     * @throws ClassNotFoundException
     */
    private static void adminMenu() throws ClassNotFoundException {
        String min, max, svg, chk;
        min = max = svg = chk = "";
        int minNum, maxNum;
        minNum = maxNum = 0;
        double sv, ch;
        sv = ch = 0.0;
        System.out.println("Administrator Main Menu\n" +
                "1. Account Summary for a Customer\n" +
                "2. Report A :: Customer Information with Total Balances in Decreasing Order\n" +
                "3. Report B :: Find the Average Total Balance Between Age Groups\n" +
                "4. Add Interest :: Add Saving and Checking interest to all \n" +
                "5. Exit");
        String choice = scanner.nextLine();
        boolean v = false;              //boolean to check valid customer id
        switch (choice) {
            case "1":                   //Admin display account summary for specified customer
                while (!v) {            //check valid customer
                    System.out.println("Enter the customer ID:");
                    cusNum = scanner.nextLine();
                    v = checkValidCustomer(cusNum); //return boolean to check valid customer
                }
                accountSummary(cusNum);  //print account summary for specified customer
                adminMenu();                    //return to admin menu

            case "2":                           //Display Report A
                reportA();               //method to display Report A
                adminMenu();                    //return to admin menu
            case "3":                           //Display Report B
                v = false;
                while (!v) {
                    System.out.println("Enter the minimum age:");
                    min = scanner.nextLine();    //min age value
                    try {
                        minNum = Integer.parseInt(min);
                        if (minNum < 0) {
                            System.out.println("You have entered an invalid minimum age");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid minimum age");
                        v = false;
                    }
                }
                v = false;
                while (!v) {
                    System.out.println("Enter the maximum age:");
                    max = scanner.nextLine();    //max age value
                    try {
                        maxNum = Integer.parseInt(max);
                        if (maxNum < 0) {
                            System.out.println("You have entered an invalid maximum age");
                            v = false;
                        } else if (maxNum < minNum) {
                            System.out.println("You have entered an invalid maximum age");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid maximum age");
                    }
                }

                reportB(min, max);           //method to display Report B given min,max
                adminMenu();                        //return to admin menu
            case "4":                           //Add interest to accounts
                while (!v) {
                    System.out.println("Enter the percent interest for savings accounts as decimal value:");
                    svg = scanner.nextLine();    //max age value
                    try {
                        sv = Double.parseDouble(svg);
                        if (sv > 1) {
                            System.out.println("You have entered an invalid interest amount");
                            v = false;
                        } else if (sv < 0) {
                            System.out.println("You have entered an invalid interest amount");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid saving interest amount");
                    }
                }
                v = false;
                while (!v) {
                    System.out.println("Enter the percent interest for checking accounts as decimal value:");
                    chk = scanner.nextLine();    //chk  value
                    try {
                        ch = Double.parseDouble(chk);
                        if (ch > 1) {
                            System.out.println("You have entered an invalid interest amount");
                            v = false;
                        } else if (ch < 0) {
                            System.out.println("You have entered an invalid interest amount");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid checking interest amount");
                    }
                }
                    try {
                        String sql = "{CALL p2.ADD_INTEREST (?,?,?,?);}";
                        CallableStatement cs = connection.prepareCall(sql);
                        cs.setDouble(1, sv);
                        cs.setDouble(2, ch);
                        cs.registerOutParameter(3,Types.INTEGER);
                        cs.registerOutParameter(4, Types.CHAR);
                        cs.executeUpdate();
                        if (cs.getInt(3) == -100) {
                            System.out.println(cs.getString(4));
                            adminMenu();
                        }
                    } catch (Exception E) {
                        E.printStackTrace();
                    }
                    displayAllAccounts();
                    adminMenu();

            case "5":                           //Exit to main menu
                mainmenu();
            default:                            //Handle invalid input
                System.out.println("Invalid Option");
                adminMenu();
        }
    }

    /**
     * Customer menu
     * @throws ClassNotFoundException
     */
    private static void submenu() throws ClassNotFoundException {
        String choice, customer, accountType, balance, accNumber, deposit, withdraw,
                srcNumber, destNumber, transfer;            //create String variables
        choice = customer = accountType = balance = accNumber = deposit = withdraw =
                srcNumber = destNumber = transfer = "";     //initialize String variables
        boolean v = false;                                  //check valid input
        System.out.println("Customer - Main Menu\n" +
                "1.    Open Account\n" +
                "2.    Close Account\n" +
                "3.    Deposit\n" +
                "4.    Withdraw\n" +
                "5.    Transfer\n" +
                "6.    Account Summary\n" +
                "7.    Exit\n" +
                "");
        choice = scanner.nextLine();
        switch (choice) {
            case "1":                   //case 1: create a new bank account
                int cus = 0;
                int x = 0;

                while (!v) {
                    System.out.println("Please enter the customer ID or enter 'q' to exit");
                    customer = scanner.nextLine();      //obtain user customer id input
                    if (customer.equals("q")) {    //allow user to exit
                        submenu();
                    }

                    try{
                        cus = Integer.parseInt(customer);
                        v = checkValidCustomer(customer); //check that this is a valid customer ID
                    } catch (Exception E){
                        System.out.println("The customer id you have entered is invalid");
                        v = false;
                    }
                }
                v = false;
                while (!v) {            //check valid account type
                    System.out.println("Enter account type: \n" +
                            "S - Savings\n" +
                            "C - Checking\n");
                    accountType = scanner.nextLine();
                    if (!accountType.equals("S") && !accountType.equals("C")) {
                        System.out.println("You have entered an invalid account type. Select 'S' or 'C'");
                        v = false;
                    } else {
                        v = true;
                    }
                }
                v = false;
                while (!v) {                             //check valid deposit amount
                    System.out.println("Enter the amount you would like to deposit or enter 'q' to exit");
                    balance = scanner.nextLine();
                    if (balance.equals("q")) {    //allow user to exit
                        submenu();
                    }

                    try {
                        x = Integer.parseInt(balance);  //get int amount from String balance
                        if (x < 0) {
                            System.out.println("You have entered an invalid amount. Try again");
                            v = false;
                        } else{
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid amount. Try again");
                        v = false;
                    }
                }
                v = false;
                try {
                    String sql = "{CALL p2.ACCT_OPN(?,?,?,?,?,?);}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setInt(1, cus);
                    cs.setInt(2, x);
                    cs.setString(3,accountType);
                    cs.registerOutParameter(4, Types.INTEGER);
                    cs.registerOutParameter(5, Types.INTEGER);
                    cs.registerOutParameter(6, Types.CHAR);
                    cs.executeUpdate();
                    if (cs.getInt(5) == -100){
                        System.out.println(cs.getString(6));
                        submenu();
                    }
                    else{
                        System.out.println("Welcome, your account number is: "+ cs.getInt(4));   //display account #
                        submenu();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
                submenu();                                              //return to customer menu
            case "2":                                           //case 2: close account
                int y = 0;
                while (!v) {                                      //check account belongs to the customer
                    System.out.println("Please enter your account number or enter 'q' to exit:");
                    accNumber = scanner.nextLine();
                    if (accNumber.equals("q")) {    //allow user to exit
                        submenu();
                    }
                    try {
                        y = Integer.parseInt(accNumber);
                        boolean b = false;
                        b = checkValidAcc(accNumber);
                        if (b){
                            v = checkValidAcc(accNumber, cusNum); //check it is a valid account
                        }
                        else{
                            v = false;
                        }
                    } catch (Exception E){
                        System.out.println("You have entered an invalid account number");
                        v = false;
                    }
                }
                v = false;
                try {
                    String sql = "{CALL p2.ACCT_CLS(?,?,?);}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setInt(1,y );
                    cs.registerOutParameter(2, Types.INTEGER);
                    cs.registerOutParameter(3, Types.CHAR);
                    cs.executeUpdate();
                    if (cs.getInt(2) == -100){
                        System.out.println(cs.getString(3));
                        submenu();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
                submenu();

            case "3":                               //case 3: deposit into account
                int acNum = 0;
                int dep = 0;
                while (!v) {                          //check valid account and belongs to customer
                    System.out.println("Please enter your account number or enter 'q' to exit:");
                    accNumber = scanner.nextLine();
                    if (accNumber.equals("q")) { //allow the user to exit
                        submenu();
                    }
                    try {
                        acNum = Integer.parseInt(accNumber);
                        v = checkValidAcc(accNumber); //check that it is a valid account
                    } catch (Exception E){
                        System.out.println("The account number you entered is invalid");
                        v = false;
                    }
                }
                v = false;
                while (!v) {             //check if valid deposit amount
                    System.out.println("Please enter the amount you would like to deposit:");
                    deposit = scanner.nextLine();
                    try {
                        dep = Integer.parseInt(deposit);
                        if (dep < 0) {
                            System.out.println("Invalid deposit amount");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid amount");
                        v = false;
                    }

                }
                v = false;
                try {
                    String sql = "{CALL p2.ACCT_DEP(?,?,?,?);}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setInt(1, acNum);
                    cs.setInt(2, dep);
                    cs.registerOutParameter(3, Types.INTEGER);
                    cs.registerOutParameter(4, Types.CHAR);
                    cs.executeUpdate();
                    if (cs.getInt(3) == -100){
                        System.out.println(cs.getString(4));
                        submenu();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
                submenu();                              //return to customer menu
            case "4":                           //withdraw from an account
                int with = 0;
                int account = 0;
                while (!v) {                      //check if the account is valid and belongs to customer
                    System.out.println("Please enter your account number or enter 'q' to exit:");
                    accNumber = scanner.nextLine();
                    if (accNumber.equals("q")) { //allow customer to exit
                        submenu();
                    }
                    try {
                        account = Integer.parseInt(accNumber);
                        boolean b = false;
                        b = checkValidAcc(accNumber);
                        if (b){

                            v = checkValidAcc(accNumber, cusNum); //check valid account and belongs to customer
                        }
                        else {
                            v = false;
                        }
                    } catch (Exception E){
                        System.out.println("The account number you entered is invalid");
                        v= false;
                    }
                }
                v = false;
                while (!v) {                         //check valid withdraw amount
                    System.out.println("Please enter the amount you would like to withdraw or enter 'q' to exit:");
                    withdraw = scanner.nextLine();
                    if (withdraw.equals("q")) {    //allow user to exit
                        submenu();
                    }
                    try {
                        int validAmount = getBalance(accNumber); //get balance of account in question
                        with = Integer.parseInt(withdraw);          //get int from withdraw
                        if (with < 0){
                            System.out.println("The withdraw amount cannot be negative");
                            v = false;
                        }
                        else if (with > validAmount) {                        //check that the account has enough funds
                            System.out.println("You do not have enough funds");
                            v = false;
                        } else {
                            v = true;
                        }
                    } catch (Exception E) {
                        System.out.println("You have entered an invalid number. Try again");
                        v = false;
                    }
                }
                v = false;
                try {
                    String sql = "{CALL p2.ACCT_WTH(?,?,?,?);}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setInt(1, account);
                    cs.setInt(2, with);
                    cs.registerOutParameter(3, Types.INTEGER);
                    cs.registerOutParameter(4, Types.CHAR);
                    cs.executeUpdate();
                    if (cs.getInt(3) == -100){
                        System.out.println(cs.getString(4));
                        submenu();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
                submenu();                            //return to customer menu
            case "5":                           //case 5: transfer between accounts
                int s = 0;
                int d = 0;
                int tr = 0;
                while (!v) {                    //check valid source account
                    System.out.println("Please enter the source account or 'q' to exit:");
                    srcNumber = scanner.nextLine();
                    if (srcNumber.equals("q")) {    //allow user to exit
                        submenu();
                    }
                    boolean b = false;
                    try {
                        s = Integer.parseInt(srcNumber);
                        b = checkValidAcc(srcNumber); //check source account is valid
                        if (b){
                            v = checkValidAcc(srcNumber, cusNum); //check source account belongs to customer
                        }
                        else {
                            v = false;
                        }
                    } catch (Exception E){
                        System.out.println("The source account you have entered is invalid.");
                    }


                }
                v = false;
                while (!v) {                      //check destination account is valid
                    System.out.println("Please enter the destination account or 'q' to exit:");
                    destNumber = scanner.nextLine();
                    if (destNumber.equals("q")) { //allow user to return to menu
                        submenu();
                    }
                    try {
                        d = Integer.parseInt(destNumber);
                        v = checkValidAcc(destNumber); //check account is valid
                    } catch (Exception E){
                        System.out.println("The destination account you have entered is invalid.");
                    }

                }
                v = false;
                while (!v) {             //check transfer amount is valid and customer has enough funds
                    System.out.println("Please enter the amount you would like to transfer or enter 'q' to exit :");
                    transfer = scanner.nextLine();
                    if (transfer.equals("q")) {    //allow user to exit
                        submenu();
                    }
                    int validAmount = getBalance(srcNumber);
                    try {                       //check transfer amount is an integer
                        tr = Integer.parseInt(transfer);
                        if (tr < 0) {        //check account has enough funds
                            System.out.println("You have entered an invalid amount");
                            v = false;
                        }
                        else if (tr > validAmount) {     //check account has enough funds
                            System.out.println("You do not have enough funds");
                            v = false;
                        } else{
                            v = true;
                        }

                    } catch (Exception E) {
                        System.out.println("You have entered an invalid amount");
                        v = false;
                    }
                }
                v = false;
                try {
                    String sql = "{CALL p2.ACCT_TRX(?,?,?,?,?);}";
                    CallableStatement cs = connection.prepareCall(sql);
                    cs.setInt(1, s);
                    cs.setInt(2, d);
                    cs.setInt(3, tr);
                    cs.registerOutParameter(4, Types.INTEGER);
                    cs.registerOutParameter(5, Types.CHAR);
                    cs.executeUpdate();
                    if (cs.getInt(4) == -100){
                        System.out.println(cs.getString(5));
                        submenu();
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                }
                submenu();

            case "6":                           //case 6: Display account summary
                accountSummary(cusNum);         //displays account summmary
                submenu();
            case "7":                           //case 7: return to main menu
                mainmenu();                     //return to main menu
            default:                            //handle invalid input
                System.out.println("Invalid Choice");
                submenu();                      //return to submenu
        }
    }
    /**
     * Display account summary.
     * @param cusID customer ID
     */
    public static void accountSummary(String cusID)
    {
        System.out.println(":: ACCOUNT SUMMARY - RUNNING");
        try {
            stmt = connection.createStatement();

            String sql =String.format("SELECT number, balance from  p2.account WHERE " +
                    " id = %s AND status = 'A'", cusID);
            rs = stmt.executeQuery(sql);
            String n = "NUMBER";
            String a = "BALANCE";
            String header = String.format("%-11s %-11s", n, a);
            System.out.println(header);
            System.out.println("----------- -----------");
            int total = 0;
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                String str = String.format("%11d %11d", rs.getInt("number"),
                        rs.getInt("balance") );
                System.out.println(str);//Move to the next line to print the next row.
                total += rs.getInt("balance");
            }
            System.out.println("-----------------------");
            String totalstr = String.format("Total %17d", total );
            System.out.println(totalstr);

            System.out.println(":: ACCOUNT SUMMARY - SUCCESS");
        } catch (SQLException se) {
            String x = se.getSQLState();
            if (x.equals("42703")){
                try{
                    int idInt = Integer.parseInt(cusID);
                    if (idInt < 0){
                        System.out.println(":: ACCOUNT SUMMARY - ERROR - INVALID ACCOUNT" );
                    }
                } catch (NumberFormatException E){
                    System.out.println(":: ACCOUNT SUMMARY - ERROR - INVALID ACCOUNT" );
                }
            }
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    /**
     * Display Report A - Customer Information with Total Balance in Decreasing Order.
     */
    public static void reportA()
    {
        System.out.println(":: REPORT A - RUNNING");
        try {
            stmt = connection.createStatement();
            String sql = String.format("select p2.customer.id, name, gender,age, sum(balance) as total " +
                    "FROM p2.customer inner join p2.account " +
                    "ON p2.customer.id = p2.account.id " +
                    "WHERE p2.account.status = 'A'" +
                    "GROUP BY p2.customer.id, p2.customer.name, p2.customer.gender, p2.customer.age " +
                    "ORDER BY total DESC");
            rs = stmt.executeQuery(sql);
            String i = "ID";
            String name = "NAME";
            String g = "GENDER";
            String age = "AGE";
            String tot = "TOTAL";
            String header = String.format("%-11s %-15s %-6s %-11s %-11s", i, name, g, age, tot);
            System.out.println(header);
            System.out.println("----------- --------------- ------ ----------- -----------");
            int total = 0;
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                String str = String.format("%11d %-15s %-6s %11d %11d", rs.getInt("id"),
                        rs.getString("name"), rs.getString("gender"),
                        rs.getInt("age"), rs.getInt("total"));
                System.out.println(str);//Move to the next line to print the next row.
            }
            System.out.println(":: REPORT A - SUCCESS");
        } catch (SQLException se) {
            se.printStackTrace();
        }
        catch (Exception E) {
            E.printStackTrace();
        }

    }

    /**
     * Display Report B - Customer Information with Total Balance in Decreasing Order.
     * @param min minimum age
     * @param max maximum age
     */
    public static void reportB(String min, String max)
    {
        System.out.println(":: REPORT B - RUNNING");
        try {
            int minimum = 0;
            int maximum = 0;
            stmt = connection.createStatement();
            try {
                minimum = Integer.parseInt(min);
                try {
                    maximum = Integer.parseInt(max);
                    if (minimum < maximum) {
                        String view = String.format("CREATE VIEW p2.view(id,name,gender,age,total) " +
                                "AS SELECT p2.customer.id, name, gender,age, sum(balance) as total " +
                                "FROM p2.customer inner join p2.account " +
                                "ON p2.customer.id = p2.account.id " +
                                "WHERE p2.account.status = 'A'" +
                                "GROUP BY p2.customer.id, p2.customer.name, p2.customer.gender, p2.customer.age ");
                        stmt.executeUpdate(view);
                        String sql = String.format("select avg(total) as average " +
                                "FROM  p2.view " +
                                "WHERE p2.view.age >= %1$s AND p2.view.age <= %2$s ", min, max);
                        rs = stmt.executeQuery(sql);
                        String i = "AVERAGE";
                        String header = String.format("%-11s", i);
                        System.out.println(header);
                        System.out.println("-----------");
                        int total = 0;
                        // Iterate through the data in the result set and display it.
                        while (rs.next()) {
                            String str = String.format("%11d", rs.getInt("average"));
                            System.out.println(str);//Move to the next line to print the next row.
                        }
                        String viewDrop = String.format("DROP VIEW p2.view");
                        stmt.executeUpdate(viewDrop);

                        System.out.println(":: REPORT B - SUCCESS");
                    } else {
                        System.out.println(":: REPORT B - ERROR - INVALID MAX");
                    }

                } catch (Exception E){
                    System.out.println(":: REPORT B - ERROR - INVALID MAX");
                }
            } catch (Exception E) {
                System.out.println(":: REPORT B - ERROR - INVALID MIN");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        catch (Exception E) {
            E.printStackTrace();
        }
    }
    /**
     * Check valid customer
     * @param customer
     * @return
     */
    public static boolean checkValidCustomer(String customer) {
        int cust = 0;
        boolean success = false;
        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            stmt = connection.createStatement();

            String sql = String.format("SELECT p2.customer.id from p2.customer " +
                    "WHERE p2.customer.id = %1$s", customer);
            rs = stmt.executeQuery(sql);
            while (rs.next()){
                cust = rs.getInt("id");
            }
            if (cust == 0){
                System.out.println("The customer id you entered is invalid");
                success = false;
            }
            else{
                success = true;
            }

        } catch (SQLException | ClassNotFoundException se){
            se.printStackTrace();
        }
        return success;
    }

    /**
     * get account number
     * @param customer
     * @param accountType
     * @return
     */
    public static String getAccNum(String customer, String accountType) {
        String accNum = null;
        int acc = 0;
        try{

            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            stmt = connection.createStatement();

            String sql = String.format("SELECT p2.account.number from p2.account " +
                    "WHERE id = %s AND type = '%s'", customer, accountType);
            rs = stmt.executeQuery(sql);
            while (rs.next()){
                acc = rs.getInt("number");
            }
            accNum = String.valueOf(acc);

        } catch (SQLException | ClassNotFoundException se){
            se.printStackTrace();
        }
        return accNum;
    }

    /**
     * Check valid account only for account number
     * @param accNumber
     * @return if the account exists
     */
    public static boolean checkValidAcc(String accNumber) {
        int acc = 0;
        boolean success = false;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            stmt = connection.createStatement();

            String sql = String.format("SELECT p2.account.number from p2.account " +
                    "WHERE number = %1$s AND status = 'A'", accNumber);
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                acc = rs.getInt("number");
            }
            if (acc == 0) {
                System.out.println("The account number you entered is invalid");
                success = false;
            } else {
                success = true;
            }

        } catch (SQLException | ClassNotFoundException se) {
            se.printStackTrace();
        }
        return success;
    }

    /**
     * Check valid account and that the account belongs to the customer
     * @param accNumber - account number
     * @param cust - customer id
     * @return if the account exists and belongs to the customer
     */
    public static boolean checkValidAcc(String accNumber, String cust) {
        int acc = 0;
        boolean success = false;
        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            stmt = connection.createStatement();

            String sql = String.format("SELECT p2.account.number from p2.account " +
                    "WHERE number = %1$s AND id = %2$s AND status = 'A'", accNumber, cust);
            rs = stmt.executeQuery(sql);
            while (rs.next()){
                acc = rs.getInt("number");
            }
            if (acc == 0){
                System.out.println("The account number you entered does not belong to you");
                success = false;
            }
            else{
                success = true;
            }

        } catch (SQLException | ClassNotFoundException se){
            se.printStackTrace();
        }
        return success;
    }

    /**
     * Get account balance
     * @param accNumber
     * @return
     */
    public static int getBalance(String accNumber) {
        int acc = 0;
        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pw);
            stmt = connection.createStatement();

            String sql = String.format("SELECT p2.account.balance from p2.account " +
                    "WHERE number = %1$s", accNumber);
            rs = stmt.executeQuery(sql);
            while (rs.next()){
                acc = rs.getInt("balance");
            }
        } catch (SQLException | ClassNotFoundException se){
            se.printStackTrace();
        }
        return  acc;
    }
    public static void displayAllAccounts()
    {
        System.out.println(":: DISPLAY ALL ACCOUNTS - RUNNING");
        try {
            stmt = connection.createStatement();
            String sql = String.format("SELECT NUMBER, BALANCE FROM p2.account;");
            rs = stmt.executeQuery(sql);
            String n = "NUMBER";
            String b = "BALANCE";
            String header = String.format("%-11s %-11s", n,b);
            System.out.println(header);
            System.out.println("----------- -----------");
            int total = 0;
            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                String str = String.format("%11s %11s", rs.getInt("number"),
                        rs.getString("balance"));
                System.out.println(str);//Move to the next line to print the next row.
            }
            System.out.println(":: DISPLAY ALL ACCOUNTS - SUCCESS");
        } catch (SQLException se) {
            se.printStackTrace();
        }
        catch (Exception E) {
            E.printStackTrace();
        }

    }
}
