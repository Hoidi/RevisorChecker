
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Rev {
    public static void main(String[] args)  {
        ItemHolder book = null;
        ItemHolder bank = null;

        PDDocument pd = null;
        try {
            pd = PDDocument.load(new File("./src/main/resources/test_bok_sexIT1.pdf"));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);

                boolean twoPages = true;

                if (twoPages) {
                    // TODO: Add multipage support for bookkeeping
                    PDDocument pd2 = PDDocument.load(new File("./src/main/resources/test_bok_sexIT2.pdf"));
                    String text2 = "";
                    if (!pd.isEncrypted()) {
                        PDFTextStripper stripper2 = new PDFTextStripper();
                        text2 = stripper2.getText(pd2);
                        BookParser.getLinesBook(text2);
                    }
                    pd2.close();
                    book = BookParser.createBookBank(text,text2);
                } else {
                    book = BookParser.createBookBank(BookParser.getLinesBook(text));
                }

            }
            pd.close();

            pd = PDDocument.load(new File("./src/main/resources/test_bank_sexIT.pdf"));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);

                bank = BankParser.createBankBank(text);
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
