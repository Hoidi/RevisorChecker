import java.util.Objects;

public class Item implements Comparable<Item> {

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

    public boolean equals(Item o) {
        if (!this.date.equals(o.date)) {
            return false;
        } else if (this.kredit != o.kredit) {
            return false;
        } else if (this.debet != o.debet) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public int compareTo(Item item) {
        int a = Integer.parseInt(date);
        int b = Integer.parseInt(item.date);

        double c = kredit;
        double d = item.kredit;

        double e = debet;
        double f = item.debet;
        if (a != b){
            return b - a;
        } else if (c != d) {
            return (int) (c - d);
        } else if (e != f) {
            return (int) (e - f);
        } else {
            return 0;
        }
    }
}
