import java.util.ArrayList;
import java.util.HashMap;

public class Ledger {

    // each ItemHolder corresponds to one account number in the ledger
    HashMap<Integer, HashMap<String, BankDay<Item>>> accounts = new HashMap<Integer, HashMap<String, BankDay<Item>>>();

    // used to calculate the limits of the policies
    private final int numberOfMembers;

    public Ledger(int numberOfMembers) {
        this.numberOfMembers = numberOfMembers;
    }

    public double getKredit(int account) {
        double kredit = 0.0;

        // TODO

        return Rev.round(kredit,2);
    }

    public double getDebet(int account) {
        double debet = 0.0;

        // TODO

        return Rev.round(debet,2);
    }

    public void addToAccount(int account, ArrayList<BookItem> bookItems) {
        if (!accounts.containsKey(account)) {
            // account didn't exist
            accounts.put(account,new HashMap<>());
        }
        addToBankDay(account, bookItems);
    }

    private void addToBankDay(int account, ArrayList<BookItem> bookItems) {
        for (BookItem b : bookItems) {
            if (accounts.get(account).containsKey(b.getDate())) {
                // day already existed on this account
                accounts.get(account).get(b.getDate()).addItem(b);
            } else {
                BankDay<Item> day = new BankDay<>(b.getDate());
                day.addItem(b);
                accounts.get(account).put(b.getDate(),day);
            }

        }
    }

    public int getNumberOfMembers() {
        return numberOfMembers;
    }
}
