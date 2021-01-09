
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

        if (ansvar) {
            if (twoDocs) {
                responsibilityCheck(args[2], args[3], args[4], args[5], args[6]);
            } else {
                responsibilityCheck(args[2], args[3], args[4]);
            }
        } else {
            if (twoDocs) {
                regularCheck(args[2], args[3], args[4]);
            } else {
                regularCheck(args[2], args[3]);
            }
        }
    }

    private static void responsibilityCheck(String bankPath, String bookPath1, String bookPath2, String thingPath1, String thingPath2) {
        regularCheck(bankPath, bookPath1, bookPath2);
        // TODO: Check policies
    }

    private static void responsibilityCheck(String bankPath, String bookPath, String thingPath) {
        regularCheck(bankPath, bookPath);
        //TODO: Check policies
    }

    private static void regularCheck(String bankPath, String bookPath1, String bookPath2) {
        //TODO: Fix with dates - remove things from bank that is earlier than the date the bookkeeping was exported

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
        //TODO: Fix with dates - remove things from bank that is earlier than the date the bookkeeping was exported

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
