public class Item {
    private final String date;
    private final String comment;
    private final double kredit;
    private final double debet;

    public Item(String date, String comment, double kredit, double debet) {
        this.date = date;
        this.comment = comment;
        this.kredit = kredit;
        this.debet = debet;
    }

    public String getDate() {
        return date;
    }

    public String getComment() {
        return comment;
    }

    public double getKredit() {
        return kredit;
    }

    public double getDebet() {
        return debet;
    }
}
