import java.util.List;

public class ThingRemover {

    public static List<Thing> removeDeads(List<Thing> items) {
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).dead) {
                Thing retainedItem = removeSomeFromEnd(items, i);
                if (retainedItem == null) {
                    items.remove(i);
                    break;
                }
                items.set(i, retainedItem);
            }
        }

        return items;
    }

    private static Thing removeSomeFromEnd(List<Thing> items, int lowerBound) {
        for (int i = items.size(); --i > lowerBound;) {
            Thing item = items.get(i);
            items.remove(i);
            if (!item.dead) {
                return item;
            }
        }
        return null;
    }
}


