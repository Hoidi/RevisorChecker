public class BookItem extends Item {

    private final String number;

    public BookItem(String date, String number, String comment, double kredit, double debet) {
        super(date,comment,kredit,debet);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }
}
