package stats;

import matcher.Matcher;
import org.citygml4j.model.citygml.CityGMLClass;

import java.util.HashMap;

public class TopLevelChange extends Change {

    public TopLevelChange() {
        super();
        this.initMapEntry(CityGMLClass.CITY_OBJECT_MEMBER.toString());
        this.initMapEntry(CityGMLClass.BUILDING.toString());
        this.initMapEntry(CityGMLClass.CITY_MODEL.toString());
    }

    @Override
    public boolean contains(String key, Matcher.EditOperators editOperator) {
        // Only accept edit operators on node levels, i.e. INSERT_NODE and DELETE_NODE
        if (!editOperator.equals(Matcher.EditOperators.INSERT_NODE)
                && !editOperator.equals(Matcher.EditOperators.DELETE_NODE)) {
            return false;
        }

        HashMap<Matcher.EditOperators, Long> value = this.map.get(key);

        if (value == null) {
            return false;
        }

        value.put(editOperator, value.get(editOperator) + 1);
        return true;
    }

    @Override
    public String getLabel() {
        return "Top-level Changes";
    }


}
