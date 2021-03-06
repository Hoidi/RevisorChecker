import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BookParser {

    public static ArrayList<ArrayList<String>> getLinesBook(String text) {
        ArrayList<String> tmpLines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        ArrayList<ArrayList<String>> lines = new ArrayList<>();

        for (String tmpLine : tmpLines) {
            lines.add(new ArrayList<>(Arrays.asList(tmpLine.split("\\s"))));
        }

        // remove first few lines
        lines.subList(0, 4).clear();

        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).get(0).equals("Kostnadsställe")) {
                lines.remove(lines.get(i));
                i--;
            }
        }

        for (int i = 0; i < 9; i++) {
            lines.remove(1); // keeps the period date line (at index 0)
        }

        // removes the first few lines on each page
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).get(0).equals("Teknologsektionen")) {
                lines.remove(i-- -1); // remove page number
                for (int x = 0; x < 11; x++) {
                    lines.remove(i);
                }
            }
        }

        lines.remove(lines.size()-1); // remove last trash

        return lines;
    }

    public static ItemHolder<BookItem> createBookBank(ArrayList<ArrayList<String>> lines) {
        String fromDate = lines.remove(0).get(1).substring(2).replace("-","");
        double saldoIn = getSaldoIn(lines.remove(0));
        double saldoUt = getSaldoOut(lines.remove(lines.size()-1));


        ItemHolder<BookItem> bookBank = new ItemHolder<>(saldoIn, saldoUt, fromDate);

        lines.remove(lines.size()-1);

        Rev.MyDouble lastSaldo = new Rev.MyDouble(saldoIn);

        // Is true if no bookkeeping has been done
        if (lines.get(0).get(1).equals("")) {
            return bookBank;
        }

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
        s.subList(0, 5).clear();
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
        saldo = saldo.substring(saldo.indexOf(",")+1);
        saldo = saldo.substring(saldo.indexOf(",")+1);
        saldo = saldo.substring(2);

        return Double.parseDouble(saldo.replace(",","."));
    }

    public static ItemHolder<BookItem> createBookBank(String text1, String text2) {
        ArrayList<ArrayList<String>> arr1 = getLinesBook(text1);
        ArrayList<ArrayList<String>> arr2 = getLinesBook(text2);

        arr1.remove(arr1.size()-1);
        arr1.remove(arr1.size()-1);

        arr2.remove(0);
        arr2.remove(0);
        arr1.addAll(arr2);

        return createBookBank(arr1);
    }
}
