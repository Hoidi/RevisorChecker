import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class LedgerParser {

    public static ArrayList<ArrayList<String>> getLinesLedger(String text) {
        ArrayList<String> tmpLines = new ArrayList<>(Arrays.asList(text.split("\\r?\\n")));
        ArrayList<ArrayList<String>> lines = new ArrayList<>();

        for (String tmpLine : tmpLines) {
            lines.add(new ArrayList<>(Arrays.asList(tmpLine.split("\\s"))));
        }

        // remove first few lines
        lines.subList(0, 12).clear();

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

    public static Ledger createLedger(ArrayList<ArrayList<String>> lines, int numberOfMembers) {
        Ledger ledger = new Ledger(numberOfMembers);

        lines.remove(lines.size()-1);

        while (!lines.isEmpty()) {
            ArrayList<ArrayList<String>> accountLines = new ArrayList<>();
            int accountNumber = Integer.parseInt(lines.remove(0).get(0));
            while (!lines.isEmpty() && lines.get(0).get(0).equals("")) {
                accountLines.add(lines.remove(0));
            }
            ledger.addToAccount(accountNumber,createBookItems(accountLines));
        }

        return ledger;
    }

    private static ArrayList<BookItem> createBookItems(ArrayList<ArrayList<String>> lines) {
        ArrayList<BookItem> items = new ArrayList<>();

        // only some account have the "balans" line
        if (lines.get(0).get(4).equals("balans")) {
            lines.remove(0); // remove "Ingaende balans"
        }
        lines.remove(lines.size()-1); // remove "Utgaende saldo"
        lines.remove(lines.size()-1); // remove "Omslutning"
        double saldoIn = getSaldoIn(lines.remove(0));

        Rev.MyDouble lastSaldo = new Rev.MyDouble(saldoIn);

        for (ArrayList<String> s : lines) {
            BookItem b = createBookItem(s,lastSaldo);
            items.add(b);
        }

        return items;
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

    public static Ledger createLedger(String text1, String text2, int numberOfMembers) {
        ArrayList<ArrayList<String>> arr1 = getLinesLedger(text1);
        ArrayList<ArrayList<String>> arr2 = getLinesLedger(text2);

        arr1.remove(arr1.size()-1);
        arr1.remove(arr1.size()-1);

        arr2.remove(0);
        arr2.remove(0);
        arr2.remove(0);
        arr1.addAll(arr2);

        return createLedger(arr1, numberOfMembers);
    }
}
