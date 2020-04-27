package stats;

import matcher.Matcher;

import java.util.HashMap;
import java.util.logging.Logger;

public class OtherChange extends Change {

    public OtherChange() {
        super();
    }

    // here every new property / node type shall be added to the list
    @Override
    public boolean contains(String key, Matcher.EditOperators editOperator) {
        HashMap<Matcher.EditOperators, Long> value = this.map.get(key);

        if (value == null) {
            this.initMapEntry(key);
            return false;
        }

        value.put(editOperator, value.get(editOperator) + 1);
        return true;
    }

    @Override
    public String getLabel() {
        return "Other Changes";
    }


}
