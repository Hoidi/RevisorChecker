import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class BankParser {

    public static ArrayList<ArrayList<String>> getLinesBank(String text, String fromDate) {
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

        // remove all things that happened before the book keeping date
        for (int i = 1; i < lines.size(); i++) {
            if (Integer.parseInt(fromDate) > Integer.parseInt(lines.get(1).get(0))) {
                lines.remove(1);
            } else {
                // TODO: Could keep the else only
                if (i == 1) { // if no lines were removed
                    lines.get(0).remove(0);
                    lines.get(0).remove(0);
                    lines.get(0).remove(0);
                    String saldo = String.join("",lines.get(0));
                    lines.get(0).clear();
                    lines.get(0).add(saldo);
                } else {
                    lines.remove(0);
                    ArrayList<String> firstLine = new ArrayList<>(lines.get(0));
                    lines.get(0).remove(0);
                    lines.get(0).remove(0);
                    lines.get(0).remove(0);
                    String oldStr = String.join(" ", lines.get(0));
                    double old = Double.parseDouble(oldStr.
                            substring(oldStr.lastIndexOf(",", oldStr.lastIndexOf(",") - 1)).
                            substring(4).
                            replace(" ","").
                            replace(",","."));
                    // TODO: Fix this
                    if (old < 1000.0) {
                        lines.get(0).remove(lines.get(0).size()-1);
                    } else if (old < 1000000.0) {
                        lines.get(0).remove(lines.get(0).size()-1);
                        lines.get(0).remove(lines.get(0).size()-1);
                    } else if (old < 1000000000.0) {
                        lines.get(0).remove(lines.get(0).size()-1);
                        lines.get(0).remove(lines.get(0).size()-1);
                        lines.get(0).remove(lines.get(0).size()-1);
                    }
                    ArrayList<String> tmp = new ArrayList<>();
                    while (isNumeric(lines.get(0).get(lines.get(0).size()-1).replace(",","."))) {
                        tmp.add(lines.get(0).remove(lines.get(0).size()-1));
                    }
                    Collections.reverse(tmp);
                    double diff = Double.parseDouble(String.join("",tmp).replace(",","."));
                    String realSaldo = Double.toString(Rev.round(old - diff,2)).replace(".",",");
                    lines.get(0).clear();
                    lines.get(0).add(0,realSaldo);
                    lines.add(1,firstLine);
                }
                break;
            }
        }
        return lines;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    public static ItemHolder<Item> createBankBank(String text, String fromDate) {
        ArrayList<ArrayList<String>> lines = BankParser.getLinesBank(text,fromDate);

        double saldoIn = Double.parseDouble(String.join("",lines.remove(0)).replace(",","."));

        lines.get(lines.size()-1).remove(0);
        lines.get(lines.size()-1).remove(0);
        double saldoUt = Double.parseDouble(String.join("",lines.remove(lines.size()-1)).replace(",","."));

        ItemHolder<Item> bankBank = new ItemHolder<Item>(saldoIn,saldoUt,fromDate);

        Rev.MyDouble lastSaldo = new Rev.MyDouble(saldoIn);

        for (ArrayList<String> s : lines) {
            Item b = createBankItem(s,lastSaldo);
            bankBank.addItem(b);
        }

        return bankBank;
    }

    public static Item createBankItem(ArrayList<String> s, Rev.MyDouble lastSaldoDouble) {
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

        double saldoDiff = Rev.round(newSaldo - lastSaldo, 2);

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


}
