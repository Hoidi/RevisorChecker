
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Rev {
    public static void main(String[] args)  {
        boolean ansvar = switch (args[0]) {
            case "a" -> true;
            case "r", "." -> false;
            default -> throw new RuntimeException("Wrong first input");
        };

        boolean twoDocs = switch (args[1]) {
            case "t" -> true;
            case "o", "." -> false;
            default -> throw new RuntimeException("Wrong second input");
        };

        int numberOfMembers = 8;
        int numberOfMembersAndLastMembers = 16;

        if (ansvar) {
            if (twoDocs) {
                responsibilityCheck(args[2], args[3], args[4], args[5], args[6], numberOfMembers, numberOfMembersAndLastMembers);
            } else {
                responsibilityCheck(args[2], args[3], args[4], numberOfMembers, numberOfMembersAndLastMembers);
            }
        } else {
            if (twoDocs) {
                regularCheck(args[2], args[3], args[4]);
            } else {
                regularCheck(args[2], args[3]);
            }
        }
    }

    private static void responsibilityCheck(String bankPath, String bookPath1, String bookPath2, String ledgerPath1, String ledgerPath2, int numberOfMembers, int numberOfMembersAndLastMembers) {
        regularCheck(bankPath, bookPath1, bookPath2);
        ledgerCheck(ledgerPath1,ledgerPath2, numberOfMembers,numberOfMembersAndLastMembers);
    }

    private static void responsibilityCheck(String bankPath, String bookPath, String ledgerPath, int numberOfMembers, int numberOfMembersAndLastMembers) {
        regularCheck(bankPath, bookPath);
        ledgerCheck(ledgerPath,numberOfMembers,numberOfMembersAndLastMembers);
    }

    private static void ledgerCheck(String ledgerPath1, String ledgerPath2, int numberOfMembers, int numberOfMembersAndLastMembers) {
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
            ledger = LedgerParser.createLedger(text1,text2,numberOfMembers,numberOfMembersAndLastMembers);

        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

    private static void ledgerCheck(String ledgerPath, int numberOfMembers, int numberOfMembersAndLastMembers) {
        Ledger ledger = null;

        PDDocument pd;
        try {
            pd = PDDocument.load(new File(ledgerPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                ledger = LedgerParser.createLedger(LedgerParser.getLinesLedger(text),numberOfMembers,numberOfMembersAndLastMembers);
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

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

    private static void evaluateLedger(Ledger ledger) {
        StringBuilder sb = new StringBuilder();

        {
            sb.append("------------------------------- Fordringar -------------------------------\n");
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

        {
            sb.append("------------------------------- Leverantörsskulder -------------------------------\n");
            checkDebAndKred(ledger, sb, 1623,"Leverantörsskulder");

        }

        {
            sb.append("------------------------------- Intern reps -------------------------------\n");
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

        {
            sb.append("------------------------------- Aspning -------------------------------\n");

            double aspningSum = 0.0;
            if (ledger.accounts.containsKey(4242)) { // 4242    Kostnader Aspning
                aspningSum += ledger.getDebet(4242);
            }
            if (ledger.accounts.containsKey(4243)) { // 4243 	Kostnader Aspning PRIT
                aspningSum += ledger.getDebet(4243);
            }
            if (ledger.accounts.containsKey(4244)) { // 4244 	Kostnader Aspning sexIT
                aspningSum += ledger.getDebet(4244);
            }
            if (ledger.accounts.containsKey(4245)) { // 4245 	Kostnader Aspning NollKIT
                aspningSum += ledger.getDebet(4245);
            }
            if (ledger.accounts.containsKey(4246)) { // 4246 	Kostnader Aspning frITid
                aspningSum += ledger.getDebet(4246);
            }
            if (ledger.accounts.containsKey(4247)) { // 4247 	Kostnader Aspning armIT
                aspningSum += ledger.getDebet(4247);
            }
            if (ledger.accounts.containsKey(4248)) { // 4248 	Kostnader Aspning digIT
                aspningSum += ledger.getDebet(4248);
            }
            if (ledger.accounts.containsKey(4254)) { // 4254 	Kostnader Aspning Revisorer
                aspningSum += ledger.getDebet(4254);
            }
            if (ledger.accounts.containsKey(4255)) { // 4255 	Kostnader Aspning Dataskyddsombud
                aspningSum += ledger.getDebet(4255);
            }

            aspningSum = round(aspningSum, 2);
            if (aspningSum > 3000.0) {
                sb.append("Total aspning cost went above 3000 kr. Total was: ").append(aspningSum).append(" kr\n");
            } else {
                sb.append("Total: \t").append(aspningSum).append(" kr\n");
                sb.append("Aspning was cleared.\n");
            }
        }

        {
            sb.append("------------------------------- Överlämning -------------------------------\n");
            double handoverSum = 0.0;
            if (ledger.accounts.containsKey(4241)) { // 4242    Kostnader Överlämning
                handoverSum += ledger.getDebet(4241);
            }
            if (ledger.accounts.containsKey(4250)) { // 4250 	Kostnader Overlamning Revisorer
                handoverSum += ledger.getDebet(4250);
            }
            if (ledger.accounts.containsKey(4251)) { // 4251 	Kostnader Overlamning Dataskyddsombud
                handoverSum += ledger.getDebet(4251);
            }
            if (ledger.accounts.containsKey(4252)) { // 4252 	Kostnader Overlamning Valberedning
                handoverSum += ledger.getDebet(4252);
            }


            // TODO: Change to right amount of ppl
            handoverSum = round(handoverSum, 2);
            if (handoverSum > 3000.0) {
                sb.append("Total överlämning cost went above ")
                        .append(round(ledger.getNumberOfMembersAndLastMembers()*215,2))
                        .append(" kr. Total was: ").append(handoverSum).append(" kr\n");
            } else {
                sb.append("Total: \t").append(handoverSum).append(" kr\n");
                sb.append("Aspning was cleared.\n");
            }
        }



        System.out.println(sb.toString());
    }

    private static void regularCheck(String bankPath, String bookPath1, String bookPath2) {
        ItemHolder book = null;
        ItemHolder bank = null;

        PDDocument pd = null;
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

    private static void regularCheck(String bankPath, String bookPath) {
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // TODO: This is an ugly hack :(
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
