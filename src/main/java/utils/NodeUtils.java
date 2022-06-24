package utils;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

public class NodeUtils {

    // Find first node by label, return null if not found
    public static Node findFirst(Transaction tx, Label label) {
        Node result = null;
        try (ResourceIterator<Node> nodes = tx.findNodes(label)) {
            if (nodes.hasNext()) {
                result = nodes.next();
            }
        }
        return result;
    }
}
