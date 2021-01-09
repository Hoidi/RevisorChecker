import java.util.*;

public class ItemHolder<T extends Item> {

    private final double saldoIn;
    private final double saldoUt;
    private final String fromDate;

    HashMap<String,BankDay<T>> bankDays = new HashMap<>();

    public ItemHolder(double saldoIn, double saldoUt,String fromDate) {
        this.saldoIn = saldoIn;
        this.saldoUt = saldoUt;
        this.fromDate = fromDate;
    }

    public double getSaldoIn() {
        return saldoIn;
    }

    public double getSaldoUt() {
        return saldoUt;
    }

    public String getFromDate() {
        return fromDate;
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

        List<BankDay<T>> list = new ArrayList<>(bankDays.values());
        Collections.sort(list);

        for (BankDay<T> day : list) {
            if (!bItemHolder.bankDays.containsKey(day.getDate())) {
                errorMap.put(day.getDate(),day);
            } else {
                BankDay<T> a = day;
                BankDay<T> b = bItemHolder.getDay(day.getDate());
                if (a.equals(b)) {
                    System.out.println("Day " + day.getDate() + " is good");
                } else {
                    a.getItems().addAll(b.getItems());
                    if (a.getItems().size() > 0) {
                        errorMap.put(day.getDate(), a);
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
