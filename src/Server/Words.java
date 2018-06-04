package Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 7/9/2017.
 */
public class Words {

    List<String> words;

    public void shuffle () {
        List<String> strings = getCopy();

    }

    public List<String> getCopy() {
        List<String> w = new ArrayList<>();
        for (String s: words) {
            w.add(s);
        }
        return w;
    }
}
