import java.util.ArrayList;

public class ItemHolder<T> {

    private final double saldoIn;
    private final double saldoUt;

    private ArrayList<T> items = new ArrayList<>();

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
        items.add(item);
    }

    public ArrayList<T> getItems() {
        return items;
    }
}
