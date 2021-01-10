
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

        if (ansvar) {
            if (twoDocs) {
                responsibilityCheck(args[2], args[3], args[4], args[5], args[6], numberOfMembers);
            } else {
                responsibilityCheck(args[2], args[3], args[4], numberOfMembers);
            }
        } else {
            if (twoDocs) {
                regularCheck(args[2], args[3], args[4]);
            } else {
                regularCheck(args[2], args[3]);
            }
        }
    }

    private static void responsibilityCheck(String bankPath, String bookPath1, String bookPath2, String ledgerPath1, String ledgerPath2, int numberOfMembers) {
        regularCheck(bankPath, bookPath1, bookPath2);
        ledgerCheck(ledgerPath1,ledgerPath2, numberOfMembers);
    }

    private static void responsibilityCheck(String bankPath, String bookPath, String ledgerPath, int numberOfMembers) {
        regularCheck(bankPath, bookPath);
        ledgerCheck(ledgerPath,numberOfMembers);
    }

    private static void ledgerCheck(String ledgerPath1, String ledgerPath2, int numberOfMembers) {
        Ledger ledger = null;

        PDDocument pd = null;
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
            ledger = LedgerParser.createLedger(text1,text2,numberOfMembers);

        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

    private static void ledgerCheck(String ledgerPath, int numberOfMembers) {
        Ledger ledger = null;

        PDDocument pd = null;
        try {
            pd = PDDocument.load(new File(ledgerPath));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);
                ledger = LedgerParser.createLedger(LedgerParser.getLinesLedger(text),numberOfMembers);
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        evaluateLedger(ledger);
    }

    private static void evaluateLedger(Ledger ledger) {
        StringBuilder sb = new StringBuilder();
        // TODO: Check fodringar (lots of account to all committees) (1510, 1617, etc)
        // TODO: Check Leverantorsskulder (2240)

        // TODO intern reps - 4510
        sb.append("------------------------------- Intern reps -------------------------------\n");
        if (ledger.accounts.containsKey(4510)) {
            boolean status = true;
            for (BankDay b : ledger.accounts.get(4510).values()) {
                if (b.getDebetSum() > round(90 * ledger.getNumberOfMembers(),2)) {
                    status = false;
                    sb.append("Day ").append(b.getDate()).append(" went above policy of 90kr/person\n");
                }
            }
            double totalDebet = ledger.getDebet(4510);
            if (totalDebet > round(630 * ledger.getNumberOfMembers(),2)) {
                status = false;
                sb.append("Total intern reps went above policy of 630kr/person. Total was ").append(totalDebet).
                        append("(allowed ").append(round(630 * ledger.getNumberOfMembers(),2)).append(")\n");
            }
            if (status) {
                sb.append("Intern reps was cleared. But all calculations was done with the maximum people\n");
            }
        }

        // TODO aspning - 4242
        // 4243 	Kostnader Aspning PRIT
        // 4244 	Kostnader Aspning sexIT
        // 4245 	Kostnader Aspning NollKIT
        // 4246 	Kostnader Aspning frITid
        // 4247 	Kostnader Aspning armIT
        // 4248 	Kostnader Aspning digIT
        // 4254 	Kostnader Aspning Revisorer
        // 4255 	Kostnader Aspning Dataskyddsombud

        // TODO overlamning - 4241
        // 4250 	Kostnader Overlamning Revisorer
        // 4251 	Kostnader Overlamning Dataskyddsombud
        // 4252 	Kostnader Overlamning Valberedning

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

        PDDocument pd = null;
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
        double d;

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
