package components.mapper;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipFactory implements RelationshipType {
    OLD_CITY_MODEL,
    NEW_CITY_MODEL,
    cityObjectMember,
    object;

    public static RelationshipFactory getCityModelRel(boolean isOld) {
        if (isOld) {
            return OLD_CITY_MODEL;
        }
        return NEW_CITY_MODEL;
    }
}
