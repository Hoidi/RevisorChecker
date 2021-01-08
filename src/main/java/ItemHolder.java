import java.util.*;

public class ItemHolder<T extends Item> {

    private final double saldoIn;
    private final double saldoUt;

    HashMap<String,BankDay<T>> bankDays = new HashMap<>();

    public ItemHolder(double saldoIn, double saldoUt) {
        this.saldoIn = saldoIn;
        this.saldoUt = saldoUt;
    }

    public double getSaldoIn() {
        return saldoIn;
    }

    public double getSaldoUt() {
        return saldoUt;
    }

    void addItem(T item) {
        String date = item.getDate();
        if (!bankDays.containsKey(date)) {
            bankDays.put(date,new BankDay<>(date));
        }
        bankDays.get(date).addItem(item);
    }

    public BankDay<T> getDay(String date) {
        return bankDays.get(date);
    }

    public void printDays() {
        List<String> list = new ArrayList<>(bankDays.keySet());
        Collections.sort(list);
        for (String s : list) {
            System.out.println(s);
        }
    }

    public HashMap<String,BankDay<T>> equals(ItemHolder<T> bItemHolder) {
        HashMap<String,BankDay<T>> errorMap = new HashMap<>();

        for (Map.Entry<String, BankDay<T>> set : bankDays.entrySet()) {
            if (set.getValue().getDate().equals("190918")) {
                System.out.println("error");
            }
            if (!bItemHolder.bankDays.containsKey(set.getValue().getDate())) {
                errorMap.put(set.getKey(),set.getValue());
            } else {
                BankDay<T> a = set.getValue();
                BankDay<T> b = bItemHolder.getDay(set.getKey());
                if (a.equals(b)) {
                    System.out.println("Day " + set.getKey() + " is good");
                } else {
                    a.getItems().addAll(b.getItems());
                    if (a.getItems().size() > 0) {
                        errorMap.put(set.getKey(), a);
                    }
                }
            }
        }

        for (Map.Entry<String, BankDay<T>> set : bItemHolder.bankDays.entrySet()) {
            if (!bankDays.containsKey(set.getValue().getDate())) {
                errorMap.put(set.getKey(),set.getValue());
            }
        }

        return errorMap;
    }
}
