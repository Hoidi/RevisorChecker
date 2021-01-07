import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BookParser {

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

    public static ItemHolder createBookBank(String text) {
        ArrayList<ArrayList<String>> lines = getLinesBook(text);

        double saldoIn = getSaldoIn(lines.remove(0));
        double saldoUt = getSaldoOut(lines.remove(lines.size()-1));

        ItemHolder bookBank = new ItemHolder<BookItem>(saldoIn,saldoUt);

        lines.remove(0);
        lines.remove(lines.size()-1);

        Rev.MyDouble lastSaldo = new Rev.MyDouble(saldoIn);

        for (ArrayList<String> s : lines) {
            BookItem b = createBookItem(s,lastSaldo);
            bookBank.addItem(b);
        }

        return bookBank;
    }

    private static BookItem createBookItem(ArrayList<String> s, Rev.MyDouble lastSaldoDouble) {
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

        double saldoDiff = Rev.round(lastSaldo - newSaldo, 2);;

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
}
