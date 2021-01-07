
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


                ArrayList<ArrayList<String>> lines = getLinesBook(text);
                book = createBookBank(lines);
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            pd = PDDocument.load(new File("./src/main/resources/test_bank.pdf"));
            if (!pd.isEncrypted()) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(pd);


                ArrayList<ArrayList<String>> lines = getLinesBank(text);
                bank = createBankBank(lines);
                bank.toString();
            }
            pd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: Check for errors
    }

    private static ArrayList<ArrayList<String>> getLinesBank(String text) {
        ArrayList<String> tmpLines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        ArrayList<ArrayList<String>> lines = new ArrayList<>();

        for (int i = 0; i < tmpLines.size(); i++) {
            lines.add(new ArrayList<>(Arrays.asList(tmpLines.get(i).split("\\s"))));
        }

        // removes the first few lines on each page
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).get(0).equals("Skapad")) {
                if (i>0) {
                    lines.remove(i-- -1); // there are some hidden "(3)"
                }
                for (int x = 0; x < 8; x++) {
                    lines.remove(i);
                }
            }
        }

        lines.remove(lines.size()-1); // another hidden "(3)"

        return lines;
    }

    private static ItemHolder createBankBank(ArrayList<ArrayList<String>> lines) {
        // TODO: parsing
        lines.get(0).remove(0);
        lines.get(0).remove(0);
        lines.get(0).remove(0);
        double saldoIn = Double.parseDouble(String.join("",lines.get(0)).replace(",","."));
        lines.remove(0);

        lines.get(lines.size()-1).remove(0);
        lines.get(lines.size()-1).remove(0);
        double saldoUt = Double.parseDouble(String.join("",lines.get(lines.size()-1)).replace(",","."));
        lines.remove(lines.size()-1);

        ItemHolder bankBank = new ItemHolder<Item>(saldoIn,saldoUt);

        MyDouble lastSaldo = new MyDouble(saldoIn);

        for (ArrayList<String> s : lines) {
            Item b = createBankItem(s,lastSaldo);
            bankBank.addItem(b);
        }

        return bankBank;
    }

    private static Item createBankItem(ArrayList<String> s, MyDouble lastSaldoDouble) {
        String date = s.remove(0);
        s.remove(0);
        s.remove(0);

        double lastSaldo = lastSaldoDouble.getD();

        String newSaldoStr = String.join(" ", s);
        double newSaldo = Double.parseDouble(newSaldoStr.
                substring(newSaldoStr.lastIndexOf(",", newSaldoStr.lastIndexOf(",") - 1)).
                substring(4).
                replace(" ","").
                replace(",","."));

        lastSaldoDouble.setD(newSaldo);

        double saldoDiff = round(newSaldo - lastSaldo, 2);

        // TODO: This is so ugly
        if (Math.abs(newSaldo) < 1000.0) {
            s.remove(s.size()-1);
        } else if (Math.abs(newSaldo) < 1000000.0) {
            s.remove(s.size()-1);
            s.remove(s.size()-1);
        } else if (Math.abs(newSaldo) < 1000000000.0) {
            s.remove(s.size()-1);
            s.remove(s.size()-1);
            s.remove(s.size()-1);
        }

        double kredit = 0.0;
        double debet = 0.0;

        if (saldoDiff < 0) { // Kredit
            ArrayList<String> kredList = new ArrayList<>();
            if (Math.abs(saldoDiff) < 1000.0) {
                kredList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000.0) {
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000000.0) {
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
            }
            Collections.reverse(kredList);
            kredit = Math.abs(Double.parseDouble(
                    String.join("", kredList).
                            replace(" ", "").
                            replace(",",".")));
        } else { // Debet
            ArrayList<String> debList = new ArrayList<>();
            if (Math.abs(saldoDiff) < 1000.0) {
                debList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000.0) {
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000000.0) {
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
            }
            Collections.reverse(debList);
            debet = Double.parseDouble(
                    String.join("", debList).
                            replace(" ", "").
                            replace(",","."));
        }

        String comment = String.join(" ",s);

        return new Item(date,comment,kredit,debet);
    }

    private static ItemHolder createBookBank(ArrayList<ArrayList<String>> lines) {
        double saldoIn = getSaldoIn(lines.remove(0));
        double saldoUt = getSaldoOut(lines.remove(lines.size()-1));

        ItemHolder bookBank = new ItemHolder<BookItem>(saldoIn,saldoUt);

        lines.remove(0);
        lines.remove(lines.size()-1);

        MyDouble lastSaldo = new MyDouble(saldoIn);

        for (ArrayList<String> s : lines) {
            BookItem b = createBookItem(s,lastSaldo);
            bookBank.addItem(b);
        }

        return bookBank;
    }

    private static BookItem createBookItem(ArrayList<String> s, MyDouble lastSaldoDouble) {
        s.remove(0);

        ArrayList<String> nummerList = new ArrayList<>();
        nummerList.add(s.remove(0));
        nummerList.add(s.remove(0));

        // get "verifikatnummer"
        String number = String.join("", nummerList);

        s.remove(0);
        s.remove(0);

        // get date
        String date = s.remove(0);
        date = date.substring(2); // remove first 20
        date = date.replace("-","");

        // calc new saldo
        double lastSaldo = lastSaldoDouble.getD();

        String newSaldoStr = String.join(" ", s);
        double newSaldo = Double.parseDouble(newSaldoStr.
                substring(newSaldoStr.lastIndexOf(",", newSaldoStr.lastIndexOf(",") - 1)).
                substring(4).
                replace(" ","").
                replace(",","."));

        lastSaldoDouble.setD(newSaldo);


        // TODO: This is so ugly
        if (Math.abs(newSaldo) < 1000.0) {
            s.remove(s.size()-1);
        } else if (Math.abs(newSaldo) < 1000000.0) {
            s.remove(s.size()-1);
            s.remove(s.size()-1);
        } else if (Math.abs(newSaldo) < 1000000000.0) {
            s.remove(s.size()-1);
            s.remove(s.size()-1);
            s.remove(s.size()-1);
        }

        double saldoDiff = round(lastSaldo - newSaldo, 2);;

        double kredit = 0.0;
        double debet = 0.0;

        if (s.get(s.size()-1).isEmpty()) { // is Debet
            ArrayList<String> debList = new ArrayList<>();
            s.remove(s.size()-1); // remove what would be Kredit
            if (Math.abs(saldoDiff) < 1000.0) {
                debList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000.0) {
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000000.0) {
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
                debList.add(s.remove(s.size()-1));
            }
            Collections.reverse(debList);
            debet = Double.parseDouble(
                    String.join("", debList).
                    replace(" ", "").
                    replace(",","."));
        } else { // is Kredit
            ArrayList<String> kredList = new ArrayList<>();
            if (Math.abs(saldoDiff) < 1000.0) {
                kredList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000.0) {
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
            } else if (Math.abs(saldoDiff) < 1000000000.0) {
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
                kredList.add(s.remove(s.size()-1));
            }
            s.remove(s.size()-1); // remove what would be Debet
            Collections.reverse(kredList);
            kredit = Double.parseDouble(
                    String.join("", kredList).
                            replace(" ", "").
                            replace(",","."));
        }

        String comment = String.join(" ",s);

        return new BookItem(date,number,comment,kredit,debet);
    }

    private static double getSaldoOut(ArrayList<String> s) {
        for (int i = 0; i < 5; i++) {
            s.remove(0);
        }
        String saldo = String.join("", s);
        saldo = saldo.substring(saldo.indexOf(",")+1);
        saldo = saldo.substring(saldo.indexOf(",")+1);
        saldo = saldo.substring(2);

        return Double.parseDouble(saldo.replace(",","."));
    }

    private static double getSaldoIn(ArrayList<String> s) {
        for (int i = 0; i < 5; i++) {
            s.remove(0);
        }
        String saldo = String.join("", s);
        return Double.parseDouble(saldo.replace(",","."));
    }

    private static ArrayList<ArrayList<String>> getLinesBook(String text) {
        // TODO: Fix multi-page bookkeeping
        ArrayList<String> tmpLines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        ArrayList<ArrayList<String>> lines = new ArrayList<>();

        for (int i = 0; i < tmpLines.size(); i++) {
            lines.add(new ArrayList<>(Arrays.asList(tmpLines.get(i).split("\\s"))));
        }

        for (int i = 0; i < 13; i++) {
            lines.remove(0); // remove first trash
        }

        lines.remove(lines.size()-1); // remove last trash

        return lines;
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
