public class BookItem extends Item {

    private final String number;

    public BookItem(String date, String number, String comment, double kredit, double debet) {
        super(date,comment,kredit,debet);
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(number).append("\t\t");
        sb.append(super.toString());

        return sb.toString();
    }
}
