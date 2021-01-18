import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Rev {
    public static void main(String[] args)  {
        boolean twoDocs = args[0].equals("t");

        if (twoDocs) {
            bookkeepingCheck(args[1], args[2], args[3], args[4], args[5], Integer.parseInt(args[6]), Integer.parseInt(args[7]));
        } else {
            bookkeepingCheck(args[1], args[2], args[3], Integer.parseInt(args[4]), Integer.parseInt(args[5]));
        }
    }

    private static void bookkeepingCheck(String bankPath, String bookPath1, String bookPath2, String ledgerPath1, String ledgerPath2, int numberOfMembers, int numberOfLastMembers) {
        bankCheck(bankPath, bookPath1, bookPath2);
        ledgerCheck(ledgerPath1,ledgerPath2, numberOfMembers,numberOfLastMembers);
    }

    private static void bookkeepingCheck(String bankPath, String bookPath, String ledgerPath, int numberOfMembers, int numberOfLastMembers) {
        bankCheck(bankPath, bookPath);
        ledgerCheck(ledgerPath,numberOfMembers,numberOfLastMembers);
    }

    private static void ledgerCheck(String ledgerPath1, String ledgerPath2, int numberOfMembers, int numberOfLastMembers) {
        Ledger ledger = null;

        PDDocument pd;
        try {
            String text1 = "";
            String text2 = "";

            pd = PDDocument.load(new File(ledgerPath1));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                text1 = stripper.getText(pd);
            }
            pd.close();

            pd = PDDocument.load(new File(ledgerPath2));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                text2 = stripper.getText(pd);
                BookParser.getLinesBook(text2);
            }
            pd.close();
            ledger = LedgerParser.createLedger(text1,text2,numberOfMembers,numberOfLastMembers);

        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

    private static void ledgerCheck(String ledgerPath, int numberOfMembers, int numberOfLastMembers) {
        Ledger ledger = null;

        PDDocument pd;
        try {
            pd = PDDocument.load(new File(ledgerPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                ledger = LedgerParser.createLedger(LedgerParser.getLinesLedger(text),numberOfMembers,numberOfLastMembers);
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

    /**
     * Checks if the given ledger has followed the economic policy
     */
    private static void evaluateLedger(Ledger ledger) {
        StringBuilder sb = new StringBuilder();

        sb.append("------------------------------- Fordringar --------------------------------\n");
        {
            checkDebAndKred(ledger, sb, 1510, "Kundfordringar");
            checkDebAndKred(ledger, sb, 1610, "Fordringar PRIT");
            checkDebAndKred(ledger, sb, 1611, "Fordringar sexIT");
            checkDebAndKred(ledger, sb, 1612, "Fordringar NollKIT");
            checkDebAndKred(ledger, sb, 1613, "Fordringar frITid");
            checkDebAndKred(ledger, sb, 1614, "Fordringar armIT");
            checkDebAndKred(ledger, sb, 1615, "Fordringar digIT");
            checkDebAndKred(ledger, sb, 1616, "Fordringar MRCIT");
            checkDebAndKred(ledger, sb, 1617, "Fordringar StyrIT");
            checkDebAndKred(ledger, sb, 1618, "Fordringar Medlemmar");
            checkDebAndKred(ledger, sb, 1619, "Fordringar PL");
            checkDebAndKred(ledger, sb, 1620, "Övriga fordringar");
            checkDebAndKred(ledger, sb, 1621, "Fordringar FanbärerIT");
            checkDebAndKred(ledger, sb, 1622, "Fordringar snIT");
            checkDebAndKred(ledger, sb, 1623, "Fordringar EqualIT");
        }

        sb.append("------------------------------- Leverantörsskulder ------------------------\n");
        {
            checkDebAndKred(ledger, sb, 2440,"Leverantörsskulder");
        }

        sb.append("------------------------------- Intern reps -------------------------------\n");
        {
            if (ledger.accounts.containsKey(4510)) {
                boolean status = true;
                for (BankDay<Item> b : ledger.accounts.get(4510).values()) {
                    if (b.getDebetSum() > round(90 * ledger.getNumberOfMembers(), 2)) {
                        status = false;
                        sb.append("Day ").append(b.getDate()).append(" went above policy of 90kr/person\n");
                    }
                }
                double totalDebet = ledger.getDebet(4510);
                if (totalDebet > round(630 * ledger.getNumberOfMembers(), 2)) {
                    status = false;
                    sb.append("Total intern reps went above policy of 630kr/person. Total was ").append(totalDebet).
                            append("(allowed ").append(round(630 * ledger.getNumberOfMembers(), 2)).append(")\n");
                }
                if (status) {
                    sb.append("Total per person: \t").append(round(totalDebet / ledger.getNumberOfMembers(), 2)).append(" kr\n");
                    sb.append("Intern reps was cleared. But all calculations was done with the maximum people\n");
                }
            }
        }

        sb.append("------------------------------- Aspning -----------------------------------\n");
        {
            int[] aspAccounts = {4242,4254,4255};

            for (int account : aspAccounts) {
                if (ledger.accounts.containsKey(account)) {
                    double aspningSum = ledger.getDebet(account);
                    if (aspningSum > 3000.0) {
                        sb.append("Total aspning cost went above 3000 kr. Total was: ").append(aspningSum).append(" kr\n");
                    } else {
                        sb.append("Total: \t").append(aspningSum).append(" kr\n");
                        sb.append("Aspning was cleared.\n");
                    }
                }
            }
        }

        sb.append("------------------------------- Överlämning -------------------------------\n");
        {
            int[] handoverAccounts = {4241,4250,4251,4252};

            for (int account : handoverAccounts) {
                if (ledger.accounts.containsKey(account)) {
                    double handoverSum = ledger.getDebet(account);
                    double max = Math.min(3000, (ledger.getNumberOfLastMembers() + ledger.getNumberOfMembers()) * 215);
                    if (handoverSum > max) {
                        sb.append("Total överlämning cost went above ")
                                .append(round(max,2))
                                .append(" kr. Total was: ").append(handoverSum).append(" kr\n");
                    } else {
                        sb.append("Total: \t").append(handoverSum).append(" kr\n");
                        sb.append("Överlämning was cleared.\n");
                    }
                }
            }
        }

        System.out.println(sb.toString());
    }

    /**
     * Checks that debet and kredit for the given account is the same
     *
     */
    private static void checkDebAndKred(Ledger ledger, StringBuilder sb, int account, String name) {
        if (ledger.accounts.containsKey(account)) {
            double kredit = ledger.getKredit(account);
            double debet = ledger.getKredit(account);
            if (kredit == debet) {
                sb.append("Total debet: \t").append(debet).append(" kr\n");
                sb.append("Total kredit: \t").append(kredit).append(" kr\n");
                sb.append(name).append(" was cleared.\n");
            } else {
                sb.append(name).append(" debet and kredit was no the same\n");
                sb.append("Debet: \t\t").append(ledger.getDebet(account)).append("\n");
                sb.append("Kredit: \t\t").append(ledger.getDebet(account)).append("\n");
            }
        }
    }

    /**
     * This method is used if two account analysis is being checked
     *
     * @param bankPath  Path to the bank pdf
     * @param bookPath1 Path to the first account analysis pdf
     * @param bookPath2 Path to the second account analysis pdf
     */
    private static void bankCheck(String bankPath, String bookPath1, String bookPath2) {
        ItemHolder book = null;
        ItemHolder bank = null;

        PDDocument pd;
        try {
            String text1 = "";
            String text2 = "";

            pd = PDDocument.load(new File(bookPath1));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                text1 = stripper.getText(pd);
            }
            pd.close();

            pd = PDDocument.load(new File(bookPath2));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                text2 = stripper.getText(pd);
                BookParser.getLinesBook(text2);
            }
            pd.close();
            book = BookParser.createBookBank(text1,text2);

            pd = PDDocument.load(new File(bankPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                bank = BankParser.createBankBank(text, book.getFromDate());
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateBookkeeping(book,bank);
    }

    /**
     * This method is used if only one account analysis is being checked
     *
     * @param bankPath  Path to the bank pdf
     * @param bookPath Path to the account analysis pdf
     */
    private static void bankCheck(String bankPath, String bookPath) {
        ItemHolder book = null;
        ItemHolder bank = null;

        PDDocument pd;
        try {
            pd = PDDocument.load(new File(bookPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                book = BookParser.createBookBank(BookParser.getLinesBook(text));
            }
            pd.close();

            pd = PDDocument.load(new File(bankPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                bank = BankParser.createBankBank(text,book.getFromDate());
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateBookkeeping(book,bank);
    }

    /**
     * Given a bank and a account analysis, it checks that all numbers match
     */
    private static void evaluateBookkeeping(ItemHolder book, ItemHolder bank) {
        HashMap errorMap = book.equals(bank);
        if (errorMap.size() == 0 && book.getSaldoIn() == bank.getSaldoIn() && book.getSaldoUt() == bank.getSaldoUt()) {
            System.out.println("Good job! All the numbers match :D");
        } else {
            System.out.println("Book in: " + book.getSaldoIn() + "\t\t" + "Book ut: " + book.getSaldoUt());
            System.out.println("Bank in: " + bank.getSaldoIn() + "\t\t" + "Bank ut: " + bank.getSaldoUt());

            List<BankDay> list = new ArrayList<BankDay>(errorMap.values());

            list.forEach((v) -> System.out.println(
                    "---------------------------------  Day: " + v.getDate() +
                    " had some errors  ---------------------------------\n" +
                    v.toString()));
            System.out.println("All the numbers don't match. Look at the things printed above.");
        }
    }

    /**
     * Rounds value to the given number of decimals
     */
    public static double round(double value, int decimals) {
        if (decimals < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // This is an ugly hack :(
    static class MyDouble {
        private double d;

        public MyDouble(double d) {
            this.d = d;
        }

        public double getD() {
            return d;
        }

        public void setD(double d) {
            this.d = d;
        }
    }
}
