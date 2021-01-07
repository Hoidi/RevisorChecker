
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Rev {
    public static void main(String[] args)  {
        ItemHolder book;
        ItemHolder bank;

        PDDocument pd = null;
        try {
            pd = PDDocument.load(new File("./src/main/resources/test_bok.pdf"));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);

                book = BookParser.createBookBank(text);
            }
            pd.close();

            pd = PDDocument.load(new File("./src/main/resources/test_bank.pdf"));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);

                bank = BankParser.createBankBank(text);
            }
            pd.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Check for errors
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
