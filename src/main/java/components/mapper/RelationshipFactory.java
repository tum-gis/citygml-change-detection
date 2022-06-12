package components.mapper;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipFactory implements RelationshipType {
    OLD_CITY_MODEL,
    NEW_CITY_MODEL,
    HREF
}
