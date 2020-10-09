package exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import matcher.EditOperationEnums.DeletePropertyNodeProperties;
import matcher.EditOperationEnums.DeleteRelationshipNodeProperties;
import matcher.EditOperationEnums.InsertPropertyNodeProperties;
import matcher.EditOperationEnums.InsertRelationshipNodeProperties;
import matcher.EditOperationEnums.UpdatePropertyNodeProperties;
import matcher.Matcher.EditOperators;
import matcher.Matcher.EditRelTypes;
import util.EnumUtil;
import util.FileUtil;
import util.GraphUtil;

public class EditOperationExporter {
    private String delimiter;
    private long nrOfOptionalTransactions;
    private long totalNrOfEditOperations;
    public HashMap<String, Long> nrOfEditOperations;

    public EditOperationExporter() {
        this.delimiter = ";";
        this.nrOfOptionalTransactions = 0;
        this.nrOfEditOperations = new HashMap<String, Long>();
    }

    public EditOperationExporter(String delimiter) {
        this.delimiter = delimiter;
        this.nrOfOptionalTransactions = 0;
        this.nrOfEditOperations = new HashMap<String, Long>();
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public HashMap<String, Long> getNrOfEditOperations() {
        return nrOfEditOperations;
    }

    public void setNrOfEditOperations(HashMap<String, Long> nrOfEditOperations) {
        this.nrOfEditOperations = nrOfEditOperations;
    }

    public long getNrOfOptionalTransactions() {
        return nrOfOptionalTransactions;
    }

    public void setNrOfOptionalTransactions(long nrOfOptionalTransactions) {
        this.nrOfOptionalTransactions = nrOfOptionalTransactions;
    }

    public long getTotalNrOfEditOperations() {
        return totalNrOfEditOperations;
    }

    public void setTotalNrOfEditOperations(long totalNrOfEditOperations) {
        this.totalNrOfEditOperations = totalNrOfEditOperations;
    }

    public String exportEditOperationsToCSV(GraphDatabaseService graphDb, Node matcherRootNode, String exportFolder) {
        String resultLogs = "";

        String filename = exportFolder + "EditOperations";

        HashMap<String, String[]> propertiesOfEditOperations = new HashMap<String, String[]>();
        propertiesOfEditOperations.put(EditOperators.UPDATE_PROPERTY.toString(),
                EnumUtil.enumValuesToStrings(UpdatePropertyNodeProperties.class));
        propertiesOfEditOperations.put(EditOperators.DELETE_PROPERTY.toString(),
                EnumUtil.enumValuesToStrings(DeletePropertyNodeProperties.class));
        propertiesOfEditOperations.put(EditOperators.INSERT_PROPERTY.toString(),
                EnumUtil.enumValuesToStrings(InsertPropertyNodeProperties.class));
        propertiesOfEditOperations.put(EditOperators.DELETE_NODE.toString(),
                EnumUtil.enumValuesToStrings(DeleteRelationshipNodeProperties.class));
        propertiesOfEditOperations.put(EditOperators.INSERT_NODE.toString(),
                EnumUtil.enumValuesToStrings(InsertRelationshipNodeProperties.class));

        this.totalNrOfEditOperations = 0;
        long countUpdatePropertyOperations = 0;
        long countDeletePropertyOperations = 0;
        long countInsertPropertyOperations = 0;
        long countDeleteNodeOperations = 0;
        long countInsertNodeOperations = 0;

        Writer writerEditOperations = null;
        Writer writerUpdatePropertyOperations = null;
        Writer writerDeletePropertyOperations = null;
        Writer writerInsertPropertyOperations = null;
        Writer writerDeleteNodeOperations = null;
        Writer writerInsertNodeOperations = null;

        // use StringBuilder to store contents first, then write to files -->
        // faster and more efficient
        StringBuilder sbEditOperations = new StringBuilder();
        StringBuilder sbUpdatePropertyOperations = new StringBuilder();
        StringBuilder sbDeletePropertyOperations = new StringBuilder();
        StringBuilder sbInsertPropertyOperations = new StringBuilder();
        StringBuilder sbDeleteNodeOperations = new StringBuilder();
        StringBuilder sbInsertNodeOperations = new StringBuilder();

        try {
            // create files
            File fEditOperations = FileUtil.createFile(filename + ".csv");
            File fUpdatePropertyOperations = FileUtil.createFile(filename + "_UpdateProperty.csv");
            File fDeletePropertyOperations = FileUtil.createFile(filename + "_DeleteProperty.csv");
            File fInsertPropertyOperations = FileUtil.createFile(filename + "_InsertProperty.csv");
            File fDeleteNodeOperations = FileUtil.createFile(filename + "_DeleteNode.csv");
            File fInsertNodeOperations = FileUtil.createFile(filename + "_InsertNode.csv");

            writerEditOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fEditOperations), "utf-8"));
            writerUpdatePropertyOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fUpdatePropertyOperations), "utf-8"));
            writerDeletePropertyOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fDeletePropertyOperations), "utf-8"));
            writerInsertPropertyOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fInsertPropertyOperations), "utf-8"));
            writerDeleteNodeOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fDeleteNodeOperations), "utf-8"));
            writerInsertNodeOperations = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(fInsertNodeOperations), "utf-8"));

            // write column names in the CSV files
            sbEditOperations
                    .append("COUNT" + delimiter + "OP_ID" + delimiter + "EDIT_OPERATION_TYPE" + delimiter + "\n");
            sbUpdatePropertyOperations.append("COUNT" + delimiter
                    + EnumUtil.enumValuesToHeaders(UpdatePropertyNodeProperties.class, delimiter) + "\n");
            sbDeletePropertyOperations.append("COUNT" + delimiter
                    + EnumUtil.enumValuesToHeaders(DeletePropertyNodeProperties.class, delimiter) + "\n");
            sbInsertPropertyOperations.append("COUNT" + delimiter
                    + EnumUtil.enumValuesToHeaders(InsertPropertyNodeProperties.class, delimiter) + "\n");
            sbDeleteNodeOperations.append("COUNT" + delimiter
                    + EnumUtil.enumValuesToHeaders(DeleteRelationshipNodeProperties.class, delimiter) + "\n");
            sbInsertNodeOperations.append("COUNT" + delimiter
                    + EnumUtil.enumValuesToHeaders(InsertRelationshipNodeProperties.class, delimiter) + "\n");

            Transaction tx = graphDb.beginTx();
            try {
                for (Iterator<Node> nodeIt = GraphUtil.findChildrenOfNode(matcherRootNode, EditRelTypes.CONSISTS_OF)
                        .iterator(); nodeIt.hasNext(); ) {
                    Node node = (Node) nodeIt.next();

                    if (Boolean.parseBoolean(
                            node.getProperty(UpdatePropertyNodeProperties.IS_OPTIONAL.toString()).toString())) {
                        nrOfOptionalTransactions++;
                    }

                    String editOperationType = GraphUtil.getLabelString(node);

                    // OP_ID is the same in all enums
                    sbEditOperations.append(++this.totalNrOfEditOperations + delimiter
                            + node.getProperty(UpdatePropertyNodeProperties.OP_ID.toString()).toString() + delimiter
                            + editOperationType + "\n");

                    if (editOperationType.equals(EditOperators.UPDATE_PROPERTY.toString())) {
                        sbUpdatePropertyOperations.append(
                                ++countUpdatePropertyOperations + delimiter + EnumUtil.getNodePropertiesWithEnums(node,
                                        UpdatePropertyNodeProperties.values(), delimiter) + "\n");
                    } else if (editOperationType.equals(EditOperators.DELETE_PROPERTY.toString())) {
                        sbDeletePropertyOperations.append(
                                ++countDeletePropertyOperations + delimiter + EnumUtil.getNodePropertiesWithEnums(node,
                                        DeletePropertyNodeProperties.values(), delimiter) + "\n");
                    } else if (editOperationType.equals(EditOperators.INSERT_PROPERTY.toString())) {
                        sbInsertPropertyOperations.append(
                                ++countInsertPropertyOperations + delimiter + EnumUtil.getNodePropertiesWithEnums(node,
                                        InsertPropertyNodeProperties.values(), delimiter) + "\n");
                    } else if (editOperationType.equals(EditOperators.INSERT_NODE.toString())) {
                        sbInsertNodeOperations.append(
                                ++countInsertNodeOperations + delimiter + EnumUtil.getNodePropertiesWithEnums(node,
                                        InsertRelationshipNodeProperties.values(), delimiter) + "\n");
                    } else if (editOperationType.equals(EditOperators.DELETE_NODE.toString())) {
                        sbDeleteNodeOperations.append(
                                ++countDeleteNodeOperations + delimiter + EnumUtil.getNodePropertiesWithEnums(node,
                                        DeleteRelationshipNodeProperties.values(), delimiter) + "\n");
                    }

                }
                tx.success();
            } finally {
                tx.close();
            }

            // fill hash map with edit operation types and their numbers
            this.nrOfEditOperations.put(EditOperators.UPDATE_PROPERTY.toString(), countUpdatePropertyOperations);
            this.nrOfEditOperations.put(EditOperators.DELETE_PROPERTY.toString(), countDeletePropertyOperations);
            this.nrOfEditOperations.put(EditOperators.INSERT_PROPERTY.toString(), countInsertPropertyOperations);
            this.nrOfEditOperations.put(EditOperators.INSERT_NODE.toString(), countInsertNodeOperations);
            this.nrOfEditOperations.put(EditOperators.DELETE_NODE.toString(), countDeleteNodeOperations);

            // write contents to files
            resultLogs += String.format("%20s", "") + "Writing main table of all edit operations to "
                    + fEditOperations.getAbsolutePath() + "\n";
            writerEditOperations.write(sbEditOperations.toString());

            resultLogs += String.format("%20s", "") + "Writing UPDATE_PROPERTY operations to "
                    + fUpdatePropertyOperations.getAbsolutePath() + "\n";
            writerUpdatePropertyOperations.write(sbUpdatePropertyOperations.toString());

            resultLogs += String.format("%20s", "") + "Writing DELETE_PROPERTY operations to "
                    + fDeletePropertyOperations.getAbsolutePath() + "\n";
            writerDeletePropertyOperations.write(sbDeletePropertyOperations.toString());

            resultLogs += String.format("%20s", "") + "Writing INSERT_PROPERTY operations to "
                    + fInsertPropertyOperations.getAbsolutePath() + "\n";
            writerInsertPropertyOperations.write(sbInsertPropertyOperations.toString());

            resultLogs += String.format("%20s", "") + "Writing DELETE_NODE operations to "
                    + fDeleteNodeOperations.getAbsolutePath() + "\n";
            writerDeleteNodeOperations.write(sbDeleteNodeOperations.toString());

            resultLogs += String.format("%20s", "") + "Writing INSERT_NODE operations to "
                    + fInsertNodeOperations.getAbsolutePath();
            writerInsertNodeOperations.write(sbInsertNodeOperations.toString());
        } catch (IOException ex) {
        } finally {
            try {
                writerEditOperations.close();
                writerUpdatePropertyOperations.close();
                writerDeletePropertyOperations.close();
                writerInsertPropertyOperations.close();
                writerDeleteNodeOperations.close();
                writerInsertNodeOperations.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }

        return resultLogs;
    }
}
