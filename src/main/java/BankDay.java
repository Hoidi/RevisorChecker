import java.util.*;

public class BankDay<T extends Item> {

    private final String date;

    private List<T> items = new ArrayList<>();

    public BankDay(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public List<T> getItems() {
        return items;
    }

    public void addItem(T item) {
        if (item.getDate().equals(date)) {
            items.add(item);
        } else {
            throw new RuntimeException("Item does not have the same date as BankDay");
        }
    }

    public boolean equals(BankDay<T> tBankDay) {
        // this method removes the items from the list but it shouldn't be a problem
        Collections.sort(items);
        Collections.sort(tBankDay.items);
        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < tBankDay.items.size(); j++) {
                if (items.get(i).equals(tBankDay.items.get(j))) {
                    items.remove(i);
                    tBankDay.items.remove(j--);
                }
            }
        }

        // all matching bookkeeping removed
        if (items.size() == 0 && tBankDay.items.size() == 0) {
            return true;
        } else {
            int oldSize1 = items.size();
            int oldSize2 = tBankDay.items.size();

            while (!combinationOfSet(items,tBankDay.items)) {
                if (oldSize1 == items.size() && oldSize2 == tBankDay.items.size()) {
                    // no items were removed -> no combination exists -> bookkeeping is wrong
                    return false;
                } else {
                    // items were removed
                    // if 0 items remain -> wouldn't be here, while would cancel
                    // if >0 items remain -> update values and run again
                    oldSize1 = items.size();
                    oldSize2 = tBankDay.items.size();
                }
            }

            return false;
        }
    }

    private boolean combinationOfSet(List<T> items1, List<T> items2) {
        Set<T> set1 = new HashSet<>(items1);
        Set<T> set2 = new HashSet<>(items2);
        for (Set<T> s : powerSet(set1)) {
            for (Set<T> t : powerSet(set2)) {
                if (s.isEmpty() || t.isEmpty()) {
                    continue;
                }
                double kreSum = 0.0;
                double debSum = 0.0;
                for (T u : s) {
                    kreSum += u.getKredit();
                    debSum += u.getDebet();
                }
                for (T u : t) {
                    kreSum -= u.getKredit();
                    debSum -= u.getDebet();
                }
                kreSum = Rev.round(kreSum,2);
                debSum = Rev.round(debSum,2);
                if (kreSum == 0 && debSum == 0) {
                    for (T u : s) {
                        items1.remove(u);
                    }
                    for (T u : t) {
                        items2.remove(u);
                    }
                    if (items1.size() == 0 && items2.size() == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------  ");
        sb.append("Day: ").append(date).append(" had some errors");
        sb.append("  ---------------------------------\n");
        for (Item i : items) {
            sb.append(i.toString());
        }

        sb.append("\n");


        return sb.toString();
    }
}
