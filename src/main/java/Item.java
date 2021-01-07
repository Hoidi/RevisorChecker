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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(date).append("\t\t");
        sb.append(debet).append("\t");
        sb.append(kredit).append("\t\t");
        sb.append(comment).append("\t");

        return sb.toString();
    }
}
