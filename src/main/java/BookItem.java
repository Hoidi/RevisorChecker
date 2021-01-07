import java.sql.Date;

public class BookItem implements Comparable<BookItem> {

    private final String date;
    private final String number;
    private final String comment;
    private final double kredit;
    private final double debet;

    public BookItem(String date, String number, String comment, double kredit, double debet) {
        this.date = date;
        this.number = number;
        this.comment = comment;
        this.kredit = kredit;
        this.debet = debet;
    }

    @Override
    public int compareTo(BookItem bookItem) {
        if (date.compareTo(bookItem.date) != 0) {
            return 1;
        }
        if (kredit != bookItem.kredit) {
            return 1;
        }
        if (debet != bookItem.debet) {
            return 1;
        }
        // should only come here if everything is the same
        return 0;
    }

    public String getDate() {
        return date;
    }

    public String getNumber() {
        return number;
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
