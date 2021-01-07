public class BankItem implements Comparable<BankItem> {

    // TODO: Add From and To dates

    private final String date;
    private final String comment;
    private final double kredit;
    private final double debet;

    public BankItem(String date, String comment, double kredit, double debet) {
        this.date = date;
        this.comment = comment;
        this.kredit = kredit;
        this.debet = debet;
    }

    @Override
    public int compareTo(BankItem bankItem) {
        if (date.compareTo(bankItem.date) != 0) {
            return 1;
        }
        if (kredit != bankItem.kredit) {
            return 1;
        }
        if (debet != bankItem.debet) {
            return 1;
        }
        // should only come here if everything is the same
        return 0;
    }

    public String getDate() {
        return date;
    }

    public double getKredit() {
        return kredit;
    }

    public double getDebet() {
        return debet;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(date + "\t");
        sb.append(debet - kredit + "\t");

        return sb.toString();
    }
}

