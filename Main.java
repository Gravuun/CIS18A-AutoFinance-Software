import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.Arrays;

class Car {
    private int vin;
    private String make;
    private String model;
    private int year;

    Car() {
        vin = 0;
        make = "";
        model = "";
        year = 0;
    }

    Car(int vin, String make, String model, int year) {
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
    }

    int getVin() {
        return vin;
    }

    String getMake() {
        return make;
    }

    String getModel() {
        return model;
    }

    int getYear() {
        return year;
    }
}

class Person {
    String firstName;
    String lastName;
    int ssn;
    int creditScore;
    int annualSalary;

    Person() {
        firstName = "";
        lastName = "";
        ssn = 0;
        creditScore = 0;
        annualSalary = 0;
    }

    Person(String firstName, String lastName, int ssn, int creditScore, int annualSalary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ssn = ssn;
        this.creditScore = creditScore;
        this.annualSalary = annualSalary;
    }
}

// This class extends Car because each record is intrinsically linked to a single car
// There should never be a time we expect the car to change for a loan
// This is also the reason there are no "setters" for the Car class
class LoanRecord extends Car {
    private int accountNumber;
    private double principal;
    private double interest;
    private double monthlyPayment;
    private double interestRate;
    private LocalDate paymentDue;
    private LocalDate lastPayment;
    private Person borrower;
    private Boolean repossess;

    LoanRecord() {
        super();
        this.accountNumber = 0;
        this.principal = 0;
        interest = 0;
        this.monthlyPayment = 0;
        this.interestRate = 0;
        this.paymentDue = LocalDate.of(1970, 1, 1);
        lastPayment = LocalDate.of(1970, 1, 1);
        this.borrower = new Person();
        repossess = false;
    }

    // For creation of new Loan Records which should never be in repo and have accrued no interest
    LoanRecord(int vin, String make, String model, int year, int accountNumber,
               double principal, double monthlyPayment, double interestRate, LocalDate paymentDue, Person borrower) {
        super(vin, make, model, year);
        this.accountNumber = accountNumber;
        this.principal = principal;
        interest = 0;
        this.monthlyPayment = monthlyPayment;
        this.interestRate = interestRate;
        this.paymentDue = paymentDue;
        lastPayment = LocalDate.now();
        this.borrower = borrower;
        repossess = false;
    }

    // For reading in of Loan Records which may be in repo
    LoanRecord(int vin, String make, String model, int year, int accountNumber,
               double principal, double monthlyPayment, double interestRate, LocalDate paymentDue,
               LocalDate lastPayment, Person borrower, Boolean repossess) {
        super(vin, make, model, year);
        this.accountNumber = accountNumber;
        this.principal = principal;
        this.monthlyPayment = monthlyPayment;
        this.interestRate = interestRate;
        this.paymentDue = paymentDue;
        this.lastPayment = lastPayment;
        this.borrower = borrower;
        this.repossess = repossess;
        calcInterest();
    }

    int getAccountNumber() {
        return accountNumber;
    }

    double getPrincipal() {
        return principal;
    }

    void setPrincipal(double principal) {
        this.principal = principal;
    }

    double getInterest() {
        return interest;
    }

    void setInterest(double interest) {
        this.interest = interest;
    }

    double getMonthlyPayment() {
        return monthlyPayment;
    }

    double getinterestRate() {
        return interestRate;
    }

    LocalDate getpaymentDue() {
        return paymentDue;
    }

    LocalDate getlastPayment() {
        return lastPayment;
    }

    Person getborrower() {
        return borrower;
    }

    Boolean getreposses() {
        return repossess;
    }

    void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    void setLastPayment(LocalDate lastPayment) {
        this.lastPayment = lastPayment;
    }

    void setPaymentDue(LocalDate paymentDue) {
        this.paymentDue = paymentDue;
    }

    void setRepossess(Boolean repossess) {
        this.repossess = repossess;
    }

    private int daysPastDue() {
        if (paymentDue.getDayOfYear() < LocalDate.now().getDayOfYear()) {
            int dpd = LocalDate.now().getDayOfYear() - paymentDue.getDayOfYear();
            if ((dpd + lastPayment.getDayOfYear()) > 75)
                repossess = true;
            return dpd;
        } else
            return 0;
    }

    // The way this conceived program updates makes the methods have to jump through some hoops so that
    // processes are not duplicated.
    private void calcInterest() {
        int dpd = daysPastDue();
        LocalDate dayToPay = paymentDue;
        if(paymentDue.isAfter(LocalDate.now()))
            dayToPay = LocalDate.now();
        int daysSinceLastPayment = dpd + (dayToPay.getDayOfYear() - lastPayment.getDayOfYear());
        // We have an annual interest rate given so we find the daily
        interest = (principal * (daysSinceLastPayment * (interestRate / (dayToPay.lengthOfYear() * 100))));
        while (dpd >= 30) // totally arbitrary timing to capitalize interest
        {
            dpd -= 30;
            // set payment date so that uncapitalized dpd are sill reflected
            paymentDue = LocalDate.now().minusDays(dpd);
            // Our rudimentary interest formula with a static 30 day calculation for capitalization purposes
            double capInterest = (principal * (30 * (interestRate / paymentDue.lengthOfYear())));
            // move capitalized interest from interest to principal
            principal += capInterest;
            interest -= capInterest;
        }
    }
}

public class Main {
    public static void main(String[] args)
            throws java.io.IOException {
        Main obj = new Main();
        obj.run();
    }

    private void run()
            throws java.io.IOException {
        File f = new File("LoanAccounts.csv");
        // check for "database" existence, if it does not exist create database
        if (!f.exists())
            f.createNewFile();
        LoanRecord[] dataBase = new LoanRecord[30];
        importData(dataBase);
        Scanner in = new Scanner(System.in);
        int menuInput;
        while (true) {
            System.out.println("Please enter the number of your desired action:\n"
                    + "1) Find an account\n" + "2) Enter a new account\n" + "3) Exit program\n");

            menuInput = in.nextInt();

            if (menuInput == 1) {
                int accountNumber;
                System.out.println("Enter the account number you wish to find: ");
                accountNumber = in.nextInt();
                int location = findAccount(accountNumber, dataBase);
                if (location == -1) {
                    System.out.println("Error account not found!");
                } else {
                    displayRecord(dataBase[location]);
                    System.out.println("\nWould you like to edit the record?\n" + "1) Record Payment\n"
                            + "2) Edit another field\n" + "3) No\n\n");
                    menuInput = in.nextInt();
                    switch (menuInput) {
                        case 1: {
                            paymentProcessing(dataBase[location]);
                            break;
                        }
                        case 2: {
                            editRecord(dataBase[location]);
                        }
                    }
                }
            } else if (menuInput == 2) {
                LoanRecord newRecord = createRecord();
                insertRecord(newRecord, dataBase);
            } else {
                break;
            }
        }

        saveAccounts(dataBase);
    }

    // There is a lot of error checking missing in this method, due to the sheer number of user inputs received
    // The credit score and annual salary are requested so that a future iteration could evaluate the
    // decision of whether to lend or not.
    private LoanRecord createRecord() {
        int accountNumber;
        double principal;
        double monthlyPayment;
        double interestRate;
        int vin;
        String make;
        String model;
        int year;
        String firstName;
        String lastName;
        int ssn;
        int creditScore;
        int annualSalary;
        LocalDate lastPayment = LocalDate.now();
        LocalDate paymentDue = lastPayment.plusDays(LocalDate.now().lengthOfMonth());
        Scanner in = new Scanner(System.in);
        // In a future iteration I would want the account number to auto-generate
        System.out.println("Enter new account number: ");
        accountNumber = in.nextInt();
        System.out.println("Enter VIN of the vehicle: ");
        vin = in.nextInt();
        in.nextLine();
        System.out.println("Enter make of the vehicle: ");
        make = in.nextLine();
        System.out.println("Enter model of the vehicle: ");
        model = in.nextLine();
        System.out.println("Enter year of the vehicle: ");
        year = in.nextInt();
        in.nextLine();
        System.out.println("Enter the borrower's first name: ");
        firstName = in.nextLine();
        System.out.println("Enter the borrower's last name: ");
        lastName = in.nextLine();
        System.out.println("Enter the borrower's Social Security Number: ");
        ssn = in.nextInt();
        System.out.println("Enter the borrower's credit score: ");
        creditScore = in.nextInt();
        System.out.println("Enter the borrower's annual salary: ");
        annualSalary = in.nextInt();
        System.out.println("Enter the principal: ");
        principal = in.nextDouble();
        System.out.println("Enter the monthly payment: ");
        monthlyPayment = in.nextDouble();
        System.out.println("Enter the interest rate: ");
        interestRate = in.nextDouble();

        Person borrower = new Person(firstName, lastName, ssn, creditScore, annualSalary);
        LoanRecord temp = new LoanRecord(vin, make, model, year, accountNumber, principal, monthlyPayment,
                interestRate, paymentDue, borrower);
        temp.setLastPayment(lastPayment);

        return temp;
    }

    private void paymentProcessing(LoanRecord record) {
        double payment;
        Scanner in = new Scanner(System.in);
        System.out.println("Enter the amount paid: $");
        payment = in.nextDouble();

        if ((record.getInterest() - payment) >= 0) {
            record.setInterest((record.getInterest() - payment));
        }

        // This is getting out of scope for the project
        // but just to note that I would want a function in the LoanRecord class
        // that determined a monthly payment via Loan Payment = (Amount)/(Discount Factor)
        // That way I could modify the monthly payment for partial payments and when it reaches 0 recalculate it.
        else {
            payment -= record.getInterest();
            record.setInterest(0);
            if(record.getPrincipal() > payment) {
                record.setPrincipal(record.getPrincipal() - payment);
                record.setPaymentDue(LocalDate.now().plusDays(LocalDate.now().lengthOfMonth()));
            }
            else{
                record.setPrincipal(0);
                record.setPaymentDue(LocalDate.now());
                record.setRepossess(false);
            }
            record.setLastPayment(LocalDate.now());

        }
    }

    private void editRecord(LoanRecord record) {
        Scanner in = new Scanner(System.in);
        int menuChoice;
        System.out.println("What would you like to edit?\n" + "1) Payment due date\n" + "2) Interest rate\n"
                + "3) Repossesion status\n" + "4) Exit to start screen\n\n");
        menuChoice = in.nextInt();
        if (menuChoice > 3 || menuChoice < 1) {
            return;
        }
        if (menuChoice == 1) {
            int month, day, year;
            String date = "";
            System.out.println("Enter the year of next payment: ");
            year = in.nextInt();
            date += Integer.toString(year);
            date += "-";
            System.out.println("Enter the month of next payment: ");
            month = in.nextInt();
            date += Integer.toString(month);
            date += "-";
            System.out.println("Enter the day of next payment: ");
            day = in.nextInt();
            date += Integer.toString(day);
            LocalDate temp = LocalDate.parse(date);
            if (temp.isBefore(LocalDate.now()) || temp.isEqual(LocalDate.now())) {
                System.out.println("Error payment must be in the future!");
            } else {
                record.setPaymentDue(temp);
            }
        } else if (menuChoice == 2) {
            System.out.println("Enter the new interest rate: ");
            record.setInterestRate(in.nextDouble());
        } else {
            record.setRepossess(!record.getreposses());
        }
    }

    private int findAccount(int accountNumber, LoanRecord[] records) {
        int arrayLength = findArraySize(records);
        for (int i = 0; i < arrayLength; i++) {
            if (records[i].getAccountNumber() == accountNumber)
                return i;
        }
        return -1;
    }

    private int findArraySize(LoanRecord[] records){
        int arrayLength = records.length - 1;

        while(records[arrayLength] == null)
        {
            arrayLength--;
        }
        return ++arrayLength;
    }

    private void insertRecord(LoanRecord newRecord, LoanRecord[] records) {
        if (arrayFull(records)) {
            System.out.println("Database is full and record cannot be inserted!");
            return;
        }

        if(records[0] == null)
        {
            records[0] = newRecord;
            return;
        }

        int arrayLength = findArraySize(records);

        int location = arrayLength / 2;
        //int right = 0;
        int left = arrayLength-1;
        while (true) {
            if (records[location].getAccountNumber() < newRecord.getAccountNumber()) {
                if(records[location+1] == null){
                    records[location+1] = newRecord;
                    return;
                }
                if (records[location + 1].getAccountNumber() > newRecord.getAccountNumber()) {
                    location += 1;
                    break;
                } else {
                    //right = location;
                    location = (left - location) / 2 + location;
                }
            } else if (records[location].getAccountNumber() > newRecord.getAccountNumber()) {
                if((location - 1) < 0){
                    break;
                }
                if (records[location - 1].getAccountNumber() < newRecord.getAccountNumber()) {
                    location -= 1;
                    break;
                } else {
                    left = location;
                    location = location / 2;
                }
            }
        }

        for (int i = arrayLength; i > location; i--) {
            records[i] = records[i - 1];
        }

        records[location] = newRecord;
    }

    private Boolean arrayFull(LoanRecord[] records) {
        for (LoanRecord x : records) {
            if (x == null)
                return false;
        }

        return true;
    }

    private Boolean emptyFile()
            throws java.io.IOException {
        boolean isEmpty;
        FileReader fr = new FileReader("LoanAccounts.csv");
        isEmpty = (fr.read() == -1);
        fr.close();
        return isEmpty;
    }

    private void displayRecord(LoanRecord record) {
        System.out.println("Account Number: " + record.getAccountNumber());
        System.out.println("Vehicle: " + record.getYear() + " " + record.getMake() + " " + record.getModel());
        System.out.println("VIN: " + record.getVin());
        System.out.println("Borrower: " + record.getborrower().firstName + " " + record.getborrower().lastName);
        System.out.println("SSN: " + record.getborrower().ssn);
        System.out.println("Principal Balance: $" + record.getPrincipal());
        System.out.println("Interest Balance: $" + record.getInterest());
        System.out.println("Monthly Payment: $" + record.getMonthlyPayment());
        System.out.println("Annual Interest Rate: " + record.getinterestRate() + "%");
        System.out.println("Payment Date: " + record.getpaymentDue());
        System.out.println("Last Payment Date: " + record.getlastPayment());
        System.out.println("Repossession: " + record.getreposses());
        System.out.println();

    }

    private void importData(LoanRecord[] records)
            throws java.io.IOException {
        if (emptyFile()) return;
        FileReader readIn = new FileReader("LoanAccounts.csv");
        LoanRecord temp;
        for (int i = 0; i < records.length; i++) {
            temp = readFromFile(readIn);
            if (temp == null) break;
            else
                records[i] = temp;
        }
        readIn.close();
    }

    private LoanRecord readFromFile(FileReader readIn)
            throws java.io.IOException {
        LoanRecord nextRecord;
        String[] storage = new String[16];
        Arrays.fill(storage,"");
        int nextChar = -1;
        for (int i = 0; i < storage.length; i++) {
            nextChar = readIn.read();
            if (nextChar == -1) break;
            while ((char) nextChar != '|' && (char)nextChar != ',') {
                storage[i] += (char) nextChar;
                nextChar = readIn.read();
            }
        }
        if(nextChar == -1) {
            return null;
        }
        if (storage[0] == null)
            return null;
        else {
            int accountNum = Integer.parseInt(storage[0]);
            int year = Integer.parseInt(storage[1]);
            String make = storage[2];
            String model = storage[3];
            int vin = Integer.parseInt(storage[4]);
            double principal = Double.parseDouble(storage[5]);
            Person borrower = new Person(storage[6], storage[7],
                    Integer.parseInt(storage[8]), Integer.parseInt(storage[9]), Integer.parseInt(storage[10]));
            double rate = Double.parseDouble(storage[11]);
            double monthly = Double.parseDouble(storage[12]);
            LocalDate paymentDue = LocalDate.parse(storage[13]);
            LocalDate lastPayment = LocalDate.parse(storage[14]);
            Boolean repossess = Boolean.parseBoolean(storage[15]);
            nextRecord = new LoanRecord(vin, make, model, year, accountNum,
                    principal, monthly, rate, paymentDue, lastPayment,
                    borrower, repossess);

            return nextRecord;
        }
    }

    // While this formatting of writing the csv may look weird the intent is to make the csv semi-readable
    // when opened in a text editor. Ideally the csv would have an intuitive header noting the fields
    private void writeToFile(LoanRecord record, FileWriter write)
            throws java.io.IOException {
        write.append(Integer.toString(record.getAccountNumber()));
        write.append("|"); // Chosen field separation character
        write.append(Integer.toString(record.getYear()));
        write.append("|");
        write.append(record.getMake());
        write.append("|");
        write.append(record.getModel());
        write.append("|");
        write.append(Integer.toString(record.getVin()));
        write.append("|");
        write.append(Double.toString(record.getPrincipal()));
        write.append("|");
        write.append(record.getborrower().firstName);
        write.append("|");
        write.append(record.getborrower().lastName);
        write.append("|");
        write.append(Integer.toString(record.getborrower().ssn));
        write.append("|");
        write.append(Integer.toString(record.getborrower().creditScore));
        write.append("|");
        write.append(Integer.toString(record.getborrower().annualSalary));
        write.append("|");
        //write.append(Double.toString(record.getInterest()));
        //write.append("|");
        write.append(Double.toString(record.getinterestRate()));
        write.append("|");
        write.append(Double.toString(record.getMonthlyPayment()));
        write.append("|");
        write.append(record.getpaymentDue().toString());
        write.append("|");
        write.append(record.getlastPayment().toString());
        write.append("|");
        write.append(Boolean.toString(record.getreposses()));
        write.append(",");

    }

    private void saveAccounts(LoanRecord[] records)
            throws java.io.IOException {
        FileWriter write = new FileWriter("LoanAccounts.csv", false);
        int arrayLength = findArraySize(records);
        for (int i = 0; i < arrayLength; i++) {
            writeToFile(records[i], write);
        }
        write.flush();
        write.close();
    }
}
