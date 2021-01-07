import java.util.ArrayList;

public class BookBank {
    private final double saldoIn;
    private final double saldoUt;

    private ArrayList<BookItem> bookItems = new ArrayList<>();

    public BookBank(double saldoIn, double saldoUt) {
        this.saldoIn = saldoIn;
        this.saldoUt = saldoUt;
    }

    void addBookItem(BookItem bookItem) {
        bookItems.add(bookItem);
    }

    public double getSaldoIn() {
        return saldoIn;
    }

    public double getSaldoUt() {
        return saldoUt;
    }
}
