import java.util.ArrayList;

public class BankBank {
    private final double saldoIn;
    private final double saldoUt;

    private ArrayList<BankItem> bankItems = new ArrayList<>();

    public BankBank(double saldoIn, double saldoUt) {
        this.saldoIn = saldoIn;
        this.saldoUt = saldoUt;
    }

    void addBankItem(BankItem bankItem) {
        bankItems.add(bankItem);
    }

    public double getSaldoIn() {
        return saldoIn;
    }

    public double getSaldoUt() {
        return saldoUt;
    }

    public ArrayList<BankItem> getBankItems() {
        return bankItems;
    }
}
