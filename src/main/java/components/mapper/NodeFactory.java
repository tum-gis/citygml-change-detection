package components.mapper;

import org.neo4j.graphdb.*;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class NodeFactory {
    private GraphDatabaseService graphDb;

    public Node create(Object object) throws IntrospectionException {
        Node result = null;
        try (Transaction tx = this.graphDb.beginTx()) {
            // Get the object's class
            Class objectClass = object.getClass();

            // Get class hierarchy
            ArrayList<Label> objectClassHierarchyLabels = new ArrayList<>();
            Class tmpObjectClass = objectClass;
            while (tmpObjectClass.getSuperclass() != null) {
                objectClassHierarchyLabels.add(Label.label(tmpObjectClass.toString()));
                tmpObjectClass = tmpObjectClass.getSuperclass();
            }

            // Create a node with labels named after its class hierarchy
            result = tx.createNode((Label[]) objectClassHierarchyLabels.toArray());

            // Get all properties and methods inherited except from Object class
            for (PropertyDescriptor propertyDescriptor
                    : Introspector.getBeanInfo(objectClass, Object.class).getPropertyDescriptors()) {
                Method getter = propertyDescriptor.getReadMethod();
                if (getter == null) {
                    // The current property does not have a getter
                    break;
                }

                if (getter.getReturnType().isPrimitive()) {
                    result.setProperty(getter.getName(), getter.invoke(object));
                } else if (getter.getReturnType().isArray()) {
                    Object[] values = (Object[]) getter.invoke(object);
                    int count = 0;
                    for (Object v : values) {
                        // Recursively map sub-elements
                        Node vNode = this.create(v);
                        Relationship rel
                                = result.createRelationshipTo(vNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty("TYPE", "array"); // TODO Store these rel values in an enum class?
                        rel.setProperty("INDEX", count++);
                    }
                } else if (Collection.class.isAssignableFrom(getter.getReturnType())) {
                    Collection<?> values = (Collection<?>) getter.invoke(object);
                    int count = 0;
                    for (Object v : values) {
                        // Recursively map sub-elements
                        Node vNode = this.create(v);
                        Relationship rel
                                = result.createRelationshipTo(vNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty("TYPE", "collection");
                        rel.setProperty("INDEX", count++);
                    }
                } else if (Map.class.isAssignableFrom(getter.getReturnType())) {
                    // Fill in the node with map entries
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) getter.invoke(object)).entrySet()) {
                        // TODO Store all entries in one single node if they are of primitive type?
                        // Recursively map sub-elements
                        Node entryNode = this.create(entry.getValue());
                        Relationship rel
                                = result.createRelationshipTo(entryNode, RelationshipType.withName(getter.getName()));
                        // Additional metadata
                        rel.setProperty("TYPE", "map");
                        rel.setProperty("KEY", entry.getKey());
                    }
                } else {
                    // Is a complex type
                    Node childNode = this.create(getter.invoke(object));
                    Relationship rel
                            = result.createRelationshipTo(childNode, RelationshipType.withName(getter.getName()));
                }
            }

            tx.commit();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
