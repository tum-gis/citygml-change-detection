package matcher;

import java.awt.geom.Area;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.common.base.ModelClassEnum;
import org.citygml4j.model.gml.GMLClass;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import mapper.BoundingBoxCalculator;
import mapper.EnumClasses.GMLRelTypes;
import mapper.Mapper.InnerTileNodeProperties;
import mapper.Mapper.InternalMappingProperties;
import mapper.Mapper.TileNodes;
import mapper.Mapper.TileOrBorderRootNodeProperties;
import matcher.EditOperationEnums.DeletePropertyNodeProperties;
import matcher.EditOperationEnums.DeleteRelationshipNodeProperties;
import matcher.EditOperationEnums.InsertPropertyNodeProperties;
import matcher.EditOperationEnums.InsertRelationshipNodeProperties;
import matcher.EditOperationEnums.UpdatePropertyNodeProperties;
import util.Area3D;
import util.BooleanObject;
import util.GeometryUtil;
import util.GraphUtil;
import util.ProducerConsumerUtil.BuildingNodeConsumer;
import util.ProducerConsumerUtil.BuildingNodeProducer;
import util.SETTINGS;
import util.SETTINGS.MatchingStrategies;
import util.StAXUtil;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class Matcher {
	public enum EditOperators implements ModelClassEnum {
		EDIT_OPERATOR,
		INSERT_PROPERTY,
		DELETE_PROPERTY,
		UPDATE_PROPERTY,
		WARNING_REP_CHANGED_PROPERTY, // semantically equivalent but different representations
		INSERT_NODE,
		DELETE_NODE,
		UPDATE_NODE,
		WARNING_DUPLICATED_NODE, // duplicate node/relationship
		WARNING_REP_CHANGED_NODE, // semantically equivalent but different representations
	}

	public enum EditRelTypes implements RelationshipType {
		CONSISTS_OF,
		OLD_NODE,
		NEW_NODE,
		INSERT_PARENT, // insert nodes to this parent
	}

	// edit relationship types attached on old nodes
	public enum EditRelTypesAttachedOld implements RelationshipType {
		OLD_NODE,
		INSERT_PARENT,
	}

	public enum TmpRelTypes implements RelationshipType {
		CONSISTS_OF,
		CONTENT_MATCHED,
		GEOMETRY_MATCHED,
		TILE_CONSISTS_OF,
		RTREE_DATA, // relationship from an neo4j spatial RTREE node to a citygml data node e.g. building's bounding shape
	}

	private boolean enableLogger = true;
	private boolean warningOnDuplicatedNodes = false;

	private boolean suppressLogger = false;

	// private Node rootTmp; // save info about matched nodes

	private GraphDatabaseService graphDb;
	private Logger logger;
	// private StringBuilder extraLoggerInfo = new StringBuilder();
	// private final String compactIndent = String.format("%-30s", "") + "> ";
	// private final String loggerIndent = "\n" + compactIndent;

	private String oldFilename;
	private String newFilename;

	// private static HashMap<String, Long> stats = new HashMap<String, Long>(); // statistics
	// private static long nrOfOptionalTransactions = 0;

	private long countTrans = 0;
	private long countBuildings = 0;
	private Transaction matcherTx;

	public Matcher(GraphDatabaseService graphDb, Logger logger, String oldFilename, String newFilename) {
		this.enableLogger = true;
		this.warningOnDuplicatedNodes = false;
		this.suppressLogger = false;

		this.graphDb = graphDb;
		this.logger = logger;

		// this.rootTmp = graphDb.createNode(Label.label("ROOT_TMP"));

		this.oldFilename = oldFilename;
		this.newFilename = newFilename;

		logger.info("\n-------------------------------" + "\nINITIALIZING MATCHING COMPONENT\n-------------------------------");
	}

	private Node createNodeWithLabel(ModelClassEnum label) {
		return createNodeWithLabel(label.toString());
	}

	private Node createNodeWithLabel(String label) {
		Node node = graphDb.createNode();
		node.addLabel(Label.label(label));

		logger.info("... creating " + label + " ...");

		// statistics
		// if (stats.containsKey(label)) {
		// Long oldValue = stats.get(label);
		// stats.replace(label, oldValue, oldValue + 1);
		// } else {
		// stats.put(label, new Long(1));
		// }

		return node;
	}

	public boolean isEnableLogger() {
		return enableLogger;
	}

	public void setEnableLogger(boolean enableLogger) {
		this.enableLogger = enableLogger;
	}

	public boolean isWarningOnDuplicatedNodes() {
		return warningOnDuplicatedNodes;
	}

	public void setWarningOnDuplicatedNodes(boolean warningOnDuplicatedNodes) {
		this.warningOnDuplicatedNodes = warningOnDuplicatedNodes;
	}

	public boolean isSuppressLogger() {
		return suppressLogger;
	}

	public void setSuppressLogger(boolean suppressLogger) {
		this.suppressLogger = suppressLogger;
	}

	// public static HashMap<String, Long> getStats() {
	// return stats;
	// }

	// public static long getNrOfOptionalTransactions() {
	// return nrOfOptionalTransactions;
	// }

	/*
	 * Auxiliary functions
	 */
	// find all neighbors given a specific outgoing relationship and a start node
	// public ArrayList<Node> findDescendants(Node startNode, RelationshipType relType, int depth) {
	// ArrayList<Node> tmpNodes = new ArrayList<Node>();
	// for (Path position : graphDb.traversalDescription()
	// .evaluator(Evaluators.fromDepth(1))
	// .evaluator(Evaluators.toDepth(depth))
	// .evaluator(Evaluators.excludeStartPosition())
	// .relationships(relType, Direction.OUTGOING)
	// .traverse(startNode)) {
	// tmpNodes.add(position.endNode());
	// }
	//
	// // sort in order of nodes' ids
	// Collections.sort(tmpNodes, new Comparator<Node>() {
	// @Override
	// public int compare(Node n1, Node n2) {
	// return (int) (n1.getId() - n2.getId());
	// }
	// });
	//
	// return tmpNodes;
	// }

	// public Node findFirstDescendant(Node startNode, RelationshipType relType, int depth) {
	// Node tmpNode = null;
	// for (Path position : graphDb.traversalDescription()
	// .evaluator(Evaluators.fromDepth(1))
	// .evaluator(Evaluators.toDepth(depth))
	// .evaluator(Evaluators.excludeStartPosition())
	// .relationships(relType, Direction.OUTGOING)
	// .traverse(startNode)) {
	// tmpNode = position.endNode();
	// break;
	// }
	//
	// return tmpNode;
	// }

	private void logging(String content) {
		if (!this.suppressLogger) {
			logger.log(Level.FINE, content);
		}
	}

	/*
	 * post processing
	 */
	private boolean isEditorRelationshipType(Relationship rel) {
		for (RelationshipType relType : EditRelTypes.values()) {
			if (rel.getType().equals(relType)) {
				return true;
			}
		}

		for (RelationshipType relType : TmpRelTypes.values()) {
			if (rel.getType().equals(relType)) {
				return true;
			}
		}

		return false;
	}

	private String retrieveBuildingId(Node node, String elementNearestId) throws FileNotFoundException, XMLStreamException {
		if (node.hasLabel(Label.label(CityGMLClass.BUILDING + ""))) {
			return node.getProperty("id").toString();
		}

		for (Relationship rel : node.getRelationships(Direction.INCOMING)) {
			if (isEditorRelationshipType(rel)) {
				continue;
			}

			String tmp = retrieveBuildingId(rel.getOtherNode(node), elementNearestId);
			
			return tmp;
			// OR
			// if a node have multiple parent buildings -> choose the one building that explicitly declares it without XLINK
			// if (StAXUtil.isReachableFromBuilding(oldFilename,
			// elementNearestId, tmp)) {
			// return tmp;
			// }
		}

		return "";
	}

	private String retrieveNearestId(Node node) {
		if (node.hasProperty("id")) {
			return node.getProperty("id").toString();
		}

		for (Relationship rel : node.getRelationships(Direction.INCOMING)) {
			if (isEditorRelationshipType(rel)) {
				continue;
			}

			// a node can only have max 1 incoming edge (excluded editor & tmp relationship)
			return retrieveNearestId(rel.getOtherNode(node));
		}

		return "";
	}

	/*
	 * Auxiliary modular functions
	 * 
	 * editor == null ? (only scout) : (create edit nodes)
	 * 
	 * oldNode or newNode == null ? result from outgoing edges -> do nothing
	 * 
	 */
	public void createUpdatePropertyNode(Node oldNode, Node editor, Object propertyName, Object oldValue, Object newValue, boolean isOptional) throws FileNotFoundException, XMLStreamException {
		if (oldNode == null || editor == null) {
			// outgoing node or only scout
			return;
		}

		// test if an exact same node already exists (due to XLINKs)
		for (Node node : GraphUtil.findParentsOfNode(oldNode, Label.label(EditOperators.UPDATE_PROPERTY.toString()))) {
			Object val = node.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString(), null);
			if (val != null && (val + "").equals(propertyName + "")) {
				return;
			}
		}

		Node result = createNodeWithLabel(EditOperators.UPDATE_PROPERTY);
		result.setProperty(UpdatePropertyNodeProperties.OP_ID.toString(), UUID.randomUUID().toString());
		
		result.setProperty(UpdatePropertyNodeProperties.OLD_PARENT_NODE_TYPE.toString(), GraphUtil.getLabelString(oldNode));
		Object parent_node_gmlid = oldNode.getProperty("id", null);
		result.setProperty(UpdatePropertyNodeProperties.OLD_PARENT_NODE_GMLID.toString(), parent_node_gmlid == null ? "" : parent_node_gmlid.toString());
		
		String oldNearestId = retrieveNearestId(oldNode);
		result.setProperty(UpdatePropertyNodeProperties.OF_OLD_NEAREST_GMLID.toString(), oldNearestId);
		result.setProperty(UpdatePropertyNodeProperties.OF_OLD_BUILDING_GMLID.toString(), retrieveBuildingId(oldNode, oldNearestId));
		
		result.setProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString(), propertyName);
		result.setProperty(UpdatePropertyNodeProperties.OLD_VALUE.toString(), oldValue);
		result.setProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString(), newValue);
		
		result.setProperty(UpdatePropertyNodeProperties.IS_OPTIONAL.toString(), isOptional);
		// if (isOptional) {
		// nrOfOptionalTransactions++;
		// }
		
		result.setProperty(UpdatePropertyNodeProperties.MESSAGE.toString(), "Property has been updated in the new city model.");

		// result.setProperty(UpdatePropertyNodeProperties.IS_GENERIC.toString(), oldNode.hasRelationship(Direction.INCOMING, GMLRelTypes.GENERIC_ATTRIBUTE));

		result.createRelationshipTo(oldNode, EditRelTypes.OLD_NODE);

		// editor.createRelationshipTo(result, EditRelTypes.CONSISTS_OF);
	}

	public void createDeletePropertyNode(Node oldNode, Node editor, Object propertyName, boolean isOptional) throws FileNotFoundException, XMLStreamException {
		if (oldNode == null || editor == null) {
			// outgoing node or only scout
			return;
		}

		// test if an exact same node already exists (due to XLINKs)
		for (Node node : GraphUtil.findParentsOfNode(oldNode, Label.label(EditOperators.DELETE_PROPERTY.toString()))) {
			Object val = node.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString(), null);
			if (val != null && (val + "").equals(propertyName + "")) {
				return;
			}
		}

		Node result = createNodeWithLabel(EditOperators.DELETE_PROPERTY);		
		result.setProperty(DeletePropertyNodeProperties.OP_ID.toString(), UUID.randomUUID().toString());
		
		result.setProperty(DeletePropertyNodeProperties.OLD_PARENT_NODE_TYPE.toString(), GraphUtil.getLabelString(oldNode));
		Object parent_node_gmlid = oldNode.getProperty("id", null);
		result.setProperty(DeletePropertyNodeProperties.OLD_PARENT_NODE_GMLID.toString(), parent_node_gmlid == null ? "" : parent_node_gmlid.toString());
		
		String oldNearestId = retrieveNearestId(oldNode);
		result.setProperty(DeletePropertyNodeProperties.OF_OLD_NEAREST_GMLID.toString(), oldNearestId);
		result.setProperty(DeletePropertyNodeProperties.OF_OLD_BUILDING_GMLID.toString(), retrieveBuildingId(oldNode, oldNearestId));

		result.setProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString(), propertyName);
		result.setProperty(DeletePropertyNodeProperties.OLD_VALUE.toString(), oldNode.getProperty((String) propertyName).toString());
		
		result.setProperty(DeletePropertyNodeProperties.IS_OPTIONAL.toString(), isOptional);
		// if (isOptional) {
		// nrOfOptionalTransactions++;
		// }

		result.setProperty(DeletePropertyNodeProperties.MESSAGE.toString(), "Property has been deleted from the old city model.");

		result.createRelationshipTo(oldNode, EditRelTypes.OLD_NODE);

		// editor.createRelationshipTo(result, EditRelTypes.CONSISTS_OF);
	}

	public void createInsertPropertyNode(Node oldNode, Node editor, Object propertyName, Object newValue, boolean isOptional) throws FileNotFoundException, XMLStreamException {
		if (oldNode == null || editor == null) {
			// outgoing node or only scout
			return;
		}

		// test if an exact same node already exists (due to XLINKs)
		for (Node node : GraphUtil.findParentsOfNode(oldNode, Label.label(EditOperators.INSERT_PROPERTY.toString()))) {
			Object val = node.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString(), null);
			if (val != null && (val + "").equals(propertyName + "")) {
				return;
			}
		}

		Node result = createNodeWithLabel(EditOperators.INSERT_PROPERTY);
		result.setProperty(InsertPropertyNodeProperties.OP_ID.toString(), UUID.randomUUID().toString());
		
		result.setProperty(InsertPropertyNodeProperties.OLD_PARENT_NODE_TYPE.toString(), GraphUtil.getLabelString(oldNode));
		Object parent_node_gmlid = oldNode.getProperty("id", null);
		result.setProperty(InsertPropertyNodeProperties.OLD_PARENT_NODE_GMLID.toString(), parent_node_gmlid == null ? "" : parent_node_gmlid.toString());
		
		String oldNearestId = retrieveNearestId(oldNode);
		result.setProperty(InsertPropertyNodeProperties.OF_OLD_NEAREST_GMLID.toString(), oldNearestId);
		result.setProperty(InsertPropertyNodeProperties.OF_OLD_BUILDING_GMLID.toString(), retrieveBuildingId(oldNode, oldNearestId));

		result.setProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString(), propertyName);
		result.setProperty(InsertPropertyNodeProperties.NEW_VALUE.toString(), newValue);
		
		result.setProperty(InsertPropertyNodeProperties.IS_OPTIONAL.toString(), isOptional);
		// if (isOptional) {
		// nrOfOptionalTransactions++;
		// }

		result.setProperty(InsertPropertyNodeProperties.MESSAGE.toString(), "Property has been inserted to the new city model.");

		result.createRelationshipTo(oldNode, EditRelTypes.OLD_NODE);

		// editor.createRelationshipTo(result, EditRelTypes.CONSISTS_OF);
	}

	public void createDeleteRelationshipNode(Node oldNode, Node parent, Node editor, boolean isOptional) throws FileNotFoundException, XMLStreamException {
		if (oldNode == null || editor == null) {
			// outgoing node or only scout
			return;
		}

		// test if a building does not belong to a tile
		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) && oldNode.hasLabel(Label.label(CityGMLClass.BUILDING + "")) && parent != null) {

			Iterable<Relationship> rels = oldNode.getRelationships(TmpRelTypes.TILE_CONSISTS_OF, Direction.INCOMING);

			int count = 0;
			for (Relationship rel : rels) {
				count++;
			}

			if (count > 1) {
				// this building belongs to multiple tiles

				// do NOT compare tile IDs because city's grids can have different sizes, hence different start X's and Y's
				String parentLowerX = parent.getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()).toString();
				String parentLowerY = parent.getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()).toString();

				for (Relationship rel : rels) {
					String curLowerX = rel.getOtherNode(oldNode).getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()).toString();
					String curLowerY = rel.getOtherNode(oldNode).getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()).toString();

					if (parentLowerX.equals(curLowerX) && parentLowerY.equals(curLowerY)) {
						rel.delete();
						return;
					}
				}
			}
		}

		// test if an exact same node already exists (due to XLINKs)
		if (!GraphUtil.findParentsOfNode(oldNode, Label.label(EditOperators.DELETE_NODE + "")).isEmpty()) {
			return;
		}

		Node result = createNodeWithLabel(EditOperators.DELETE_NODE);
		result.setProperty(DeleteRelationshipNodeProperties.OP_ID.toString(), UUID.randomUUID().toString());
		
		result.setProperty(DeleteRelationshipNodeProperties.DELETE_NODE_TYPE.toString(), GraphUtil.getLabelString(oldNode));
		Object delete_node_gmlid = oldNode.getProperty("id", null);
		result.setProperty(DeleteRelationshipNodeProperties.DELETE_NODE_GMLID.toString(), delete_node_gmlid == null ? "" : delete_node_gmlid.toString());
		
		String oldNearestId = retrieveNearestId(oldNode);
		result.setProperty(DeleteRelationshipNodeProperties.OF_OLD_NEAREST_GMLID.toString(), oldNearestId);
		result.setProperty(DeleteRelationshipNodeProperties.OF_OLD_BUILDING_GMLID.toString(), retrieveBuildingId(oldNode, oldNearestId));

		result.setProperty(DeleteRelationshipNodeProperties.IS_OPTIONAL.toString(), isOptional);
		// if (isOptional) {
		// nrOfOptionalTransactions++;
		// }
		
		result.setProperty(DeleteRelationshipNodeProperties.MESSAGE.toString(), "Sub graph has been deleted from the old city model.");

		// result.setProperty(DeleteRelationshipNodeProperties.IS_GENERIC.toString(), oldNode.hasRelationship(Direction.INCOMING, GMLRelTypes.GENERIC_ATTRIBUTE));

		result.createRelationshipTo(oldNode, EditRelTypes.OLD_NODE);

		// editor.createRelationshipTo(result, EditRelTypes.CONSISTS_OF);
	}

	public void createInsertRelationshipNode(Node newNode, Node oldNode, RelationshipType relType, Node editor, boolean isOptional, Node oldTileNode) throws FileNotFoundException, XMLStreamException {
		if (oldNode == null || newNode == null || editor == null) {
			// outgoing node or only scout
			return;
		}

		// test if a building does not belong to a tile
		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) && newNode.hasLabel(Label.label(CityGMLClass.CITY_OBJECT_MEMBER + "")) && oldTileNode != null) {

			Node buildingNode = GraphUtil.findFirstChildOfNode(newNode, Label.label(CityGMLClass.BUILDING + ""));

			if (buildingNode != null) {
				Iterable<Relationship> rels = buildingNode.getRelationships(TmpRelTypes.TILE_CONSISTS_OF, Direction.INCOMING);

				int count = 0;
				for (Relationship rel : rels) {
					count++;
				}

				if (count > 1) {
					// this building belongs to multiple tiles

					// do NOT compare tile IDs because city's grids can have different sizes, hence different start X's and Y's
					String parentLowerX = oldTileNode.getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()).toString();
					String parentLowerY = oldTileNode.getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()).toString();

					for (Relationship rel : rels) {
						String curLowerX = rel.getOtherNode(buildingNode).getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()).toString();
						String curLowerY = rel.getOtherNode(buildingNode).getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()).toString();

						if (parentLowerX.equals(curLowerX) && parentLowerY.equals(curLowerY)) {
							rel.delete();
							return;
						}
					}
				}
			}
		}

		// test if an exact same node already exists (due to XLINKs)
		for (Node node : GraphUtil.findParentsOfNode(oldNode, Label.label(EditOperators.INSERT_NODE + ""))) {
			Object val = node.getProperty(InsertRelationshipNodeProperties.INSERT_RELATIONSHIP_TYPE.toString(), null);
			if (val != null && (val + "").equals(relType + "")
					&& GraphUtil.findFirstChildOfNode(node, EditRelTypes.NEW_NODE).equals(newNode)) {
				return;
			}
		}

		Node result = createNodeWithLabel(EditOperators.INSERT_NODE);
		result.setProperty(InsertRelationshipNodeProperties.OP_ID.toString(), UUID.randomUUID().toString());

		result.setProperty(InsertRelationshipNodeProperties.INSERT_RELATIONSHIP_TYPE.toString(), relType.toString());
		
		result.setProperty(InsertRelationshipNodeProperties.INSERT_NODE_TYPE.toString(), GraphUtil.getLabelString(oldNode));
		Object insert_node_gmlid = oldNode.getProperty("id", null);
		result.setProperty(InsertRelationshipNodeProperties.INSERT_NODE_GMLID.toString(), insert_node_gmlid == null ? "" : insert_node_gmlid.toString());
		
		String oldNearestId = retrieveNearestId(oldNode);
		result.setProperty(InsertRelationshipNodeProperties.OF_OLD_NEAREST_GMLID.toString(), oldNearestId);
		result.setProperty(InsertRelationshipNodeProperties.OF_OLD_BUILDING_GMLID.toString(), retrieveBuildingId(oldNode, oldNearestId));
		
		String newNearestId = retrieveNearestId(newNode);
		result.setProperty(InsertRelationshipNodeProperties.OF_NEW_NEAREST_GMLID.toString(), newNearestId);
		result.setProperty(InsertRelationshipNodeProperties.OF_NEW_BUILDING_GMLID.toString(), retrieveBuildingId(newNode, newNearestId));
		
		result.setProperty(InsertRelationshipNodeProperties.IS_OPTIONAL.toString(), isOptional);
		// if (isOptional) {
		// nrOfOptionalTransactions++;
		// }

		result.setProperty(InsertRelationshipNodeProperties.MESSAGE.toString(), "Sub graph has been inserted to the new city model.");

		// result.setProperty(InsertRelationshipNodeProperties.IS_GENERIC.toString(), newNode.hasRelationship(Direction.INCOMING, GMLRelTypes.GENERIC_ATTRIBUTE));

		result.createRelationshipTo(newNode, EditRelTypes.NEW_NODE);
		result.createRelationshipTo(oldNode, EditRelTypes.INSERT_PARENT);

		// editor.createRelationshipTo(result, EditRelTypes.CONSISTS_OF);
	}

	/*
	 * Temporary info nodes for matched contents
	 */
	// tmp node indicating if two nodes are matched
	private void createContentMatchedInfoNode(Node oldNode, Node newNode) {
		if (isContentMatched(oldNode, newNode)) {
			return;
		}

		Node node = graphDb.createNode(Label.label("CONTENT_MATCHED"));
		// this.rootTmp.createRelationshipTo(node, TmpRelTypes.CONSISTS_OF);

		node.createRelationshipTo(oldNode, TmpRelTypes.CONTENT_MATCHED);
		node.createRelationshipTo(newNode, TmpRelTypes.CONTENT_MATCHED);
	}

	// tmp node indicating if two nodes are matched
	private void createGeometryMatchedInfoNode(Node oldNode, Node newNode) {
		if (isGeometryMatched(oldNode, newNode)) {
			return;
		}

		Node node = graphDb.createNode(Label.label("GEOMETRY_MATCHED"));
		// this.rootTmp.createRelationshipTo(node, TmpRelTypes.CONSISTS_OF);

		node.createRelationshipTo(oldNode, TmpRelTypes.GEOMETRY_MATCHED);
		node.createRelationshipTo(newNode, TmpRelTypes.GEOMETRY_MATCHED);
	}

	// between 2 nodes max 1 matched reType, but a node can have the same relType to 2 other nodes
	private boolean isMatched(Node oldNode, Node newNode, RelationshipType relType) {
		// if (relType.equals(TmpRelTypes.CONTENT_MATCHED)) {
		// if (!SETTINGS.CREATE_MATCHED_CONTENT_NODE) {
		// return false;
		// }
		// } else if (relType.equals(TmpRelTypes.GEOMETRY_MATCHED)) {
		// if (!SETTINGS.CREATE_MATCHED_GEOMETRY_NODE) {
		// return false;
		// }
		// }

		for (Relationship oldRel : oldNode.getRelationships(relType, Direction.INCOMING)) {
			Node parent = oldRel.getOtherNode(oldNode);
			for (Relationship newRel : newNode.getRelationships(relType, Direction.INCOMING)) {
				if (parent.equals(newRel.getOtherNode(newNode))) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isContentMatched(Node oldNode, Node newNode) {
		return isMatched(oldNode, newNode, TmpRelTypes.CONTENT_MATCHED);
	}

	private boolean isGeometryMatched(Node oldNode, Node newNode) {
		return isContentMatched(oldNode, newNode)
				|| isMatched(oldNode, newNode, TmpRelTypes.GEOMETRY_MATCHED);
	}

	public void matcherInit(Node mapperRootNode, Node matcherRootNode) throws InterruptedException, FileNotFoundException, XMLStreamException {
		countTrans = 0;

		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) || SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
			// match properties of city models
			matcherTx = graphDb.beginTx();
			try {
				matchNode(
						GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
						GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL),
						matcherRootNode, new BooleanObject(false), mapperRootNode, GMLRelTypes.CITY_OBJECT_MEMBER);

				matcherTx.success();
			} finally {
				matcherTx.close();
			}

			if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
				// nr of tiles
				int oldSizeX;
				int oldSizeY;
				int newSizeX;
				int newSizeY;

				// position/index where both models have the same tiles
				// eg. newTiles[0][0] is actually at oldTiles[2][2]
				int oldStartSyncX;
				int oldStartSyncY;
				int newStartSyncX;
				int newStartSyncY;

				Node[][] oldTiles;
				Node[][] newTiles;

				countTrans = 0;
				matcherTx = graphDb.beginTx();
				try {

					Node oldTileRootNode = graphDb.findNodes(Label.label(TileNodes.ROOT_OLD_INNER_TILE + "")).next();
					Node newTileRootNode = graphDb.findNodes(Label.label(TileNodes.ROOT_NEW_INNER_TILE + "")).next();

					ArrayList<Node> oldTileNodes = GraphUtil.findChildrenOfNode(oldTileRootNode);
					ArrayList<Node> newTileNodes = GraphUtil.findChildrenOfNode(newTileRootNode);

					// empty old city model -> insert all buildings from new city model
					if (oldTileNodes == null || oldTileNodes.isEmpty()) {
						for (Node cityObjectMember : GraphUtil.findChildrenOfNode(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL), GMLRelTypes.CITY_OBJECT_MEMBER)) {
							countTrans++;

							if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
								matcherTx.success();
								matcherTx.close();
								matcherTx = graphDb.beginTx();
							}

							createInsertRelationshipNode(
									cityObjectMember,
									GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
									GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
						}

						matcherTx.success();
						return;
					}

					// empty new city model -> delete all buildings from new city model
					if (newTileNodes == null || newTileNodes.isEmpty()) {
						for (Node building : GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL))) {
							countTrans++;

							if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
								matcherTx.success();
								matcherTx.close();
								matcherTx = graphDb.beginTx();
							}

							createDeleteRelationshipNode(building, null, matcherRootNode, false);
						}

						matcherTx.success();
						return;
					}

					// !!! old and new city model can have different number of tiles in each dimension !!!
					oldSizeX = (int) oldTileRootNode.getProperty(TileOrBorderRootNodeProperties.SIZE_X.toString());
					oldSizeY = (int) oldTileRootNode.getProperty(TileOrBorderRootNodeProperties.SIZE_Y.toString());
					newSizeX = (int) newTileRootNode.getProperty(TileOrBorderRootNodeProperties.SIZE_X.toString());
					newSizeY = (int) newTileRootNode.getProperty(TileOrBorderRootNodeProperties.SIZE_Y.toString());

					oldTiles = new Node[oldSizeY][oldSizeX];
					newTiles = new Node[newSizeY][newSizeX];

					for (Node node : oldTileNodes) {
						oldTiles[(int) node.getProperty(InnerTileNodeProperties.INDEX_Y.toString())][(int) node.getProperty(InnerTileNodeProperties.INDEX_X.toString())] = node;
					}

					for (Node node : newTileNodes) {
						newTiles[(int) node.getProperty(InnerTileNodeProperties.INDEX_Y.toString())][(int) node.getProperty(InnerTileNodeProperties.INDEX_X.toString())] = node;
					}

					// in case city models have different envelopse
					// eg. newTiles[0][0] is actually at oldTiles[2][2]
					int i = 0;
					int j = 0;

					while (i < oldSizeY && j < newSizeY) {
						double diff = (double) (oldTiles[i][0].getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()))
								- (double) (newTiles[j][0].getProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString()));

						if (diff < -SETTINGS.ERR_TOLERANCE) {
							// old tile i is vertically before new tile j
							i++;
						} else if (diff > SETTINGS.ERR_TOLERANCE) {
							// old tile i is vertically after new tile j
							j++;
						} else {
							break;
						}
					}

					oldStartSyncY = i;
					newStartSyncY = j;

					i = 0;
					j = 0;

					while (i < oldSizeX && j < newSizeX) {
						double diff = (double) (oldTiles[0][i].getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()))
								- (double) (newTiles[0][j].getProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString()));

						if (diff < -SETTINGS.ERR_TOLERANCE) {
							// old tile i is horizontally before new tile j
							i++;
						} else if (diff > SETTINGS.ERR_TOLERANCE) {
							// old tile i is horizontally after new tile j
							j++;
						} else {
							break;
						}
					}

					oldStartSyncX = i;
					newStartSyncX = j;

					// match vertically and horizontally sync tiles
					int nThreads = Runtime.getRuntime().availableProcessors() * 2;

					logger.info("... setting up thread pool with " + nThreads + " threads ...");

					logger.info("Matching buildings in [old city model's tile] and [new city model's tile] ...");

					int nrOfDigitsX = (Math.max(oldSizeX, newSizeX) + "").length();
					int nrOfDigitsY = (Math.max(oldSizeY, newSizeY) + "").length();
					final String formatterX = "%" + nrOfDigitsX + "s";
					final String formatterY = "%" + nrOfDigitsY + "s";

					int[] addToIndX = new int[] { 0, 1, 2, 0, 1, 2, 0, 1, 2 };
					int[] addToIndY = new int[] { 0, 0, 0, 1, 1, 1, 2, 2, 2 };

					for (int k = 0; k < addToIndX.length; k++) {
						ExecutorService service = Executors.newFixedThreadPool(nThreads);

						int oldIndY = oldStartSyncY + addToIndY[k];
						int newIndY = newStartSyncY + addToIndY[k];

						while (oldIndY < oldSizeY && newIndY < newSizeY) {
							int oldIndX = oldStartSyncX + addToIndX[k];
							int newIndX = newStartSyncX + addToIndX[k];

							while (oldIndX < oldSizeX && newIndX < newSizeX) {

								final int old_inner_y = oldIndY;
								final int old_inner_x = oldIndX;
								final int new_inner_y = newIndY;
								final int new_inner_x = newIndX;

								// match buildings in tiles
								service.execute(new Runnable() {
									@Override
									public void run() {
										try (Transaction tx = graphDb.beginTx()) {
											logger.info("Matched tiles [" + String.format(formatterY, old_inner_y + "")
													+ ", " + String.format(formatterX, old_inner_x + "") + "]"
													+ " and [" + String.format(formatterY, new_inner_y + "")
													+ ", " + String.format(formatterX, new_inner_x + "") + "]");

											matchNode(
													oldTiles[old_inner_y][old_inner_x],
													newTiles[new_inner_y][new_inner_x],
													matcherRootNode, new BooleanObject(false), mapperRootNode, null);

											tx.success();
										} catch (FileNotFoundException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (XMLStreamException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								});

								oldIndX += 3;
								newIndX += 3;
							}

							oldIndY += 3;
							newIndY += 3;
						}

						// wait for all threads to finish
						// logger.info("... shutting down threadpool ...");
						service.shutdown();
						service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);
					}

					// insert buildings in remaining tiles from new city model
					boolean breakLoops = false;
					for (i = 0; i < newSizeY && !breakLoops; i++) {
						for (j = 0; j < newSizeX && !breakLoops; j++) {
							if (i >= newStartSyncY && j >= newStartSyncX) {
								breakLoops = true;
							} else {
								for (Node building : GraphUtil.findChildrenOfNode(newTiles[i][j])) {
									countTrans++;

									if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
										matcherTx.success();
										matcherTx.close();
										matcherTx = graphDb.beginTx();
									}

									createInsertRelationshipNode(
											GraphUtil.findFirstParentOfNode(building, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
											GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
											GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
								}
							}
						}
					}

					// delete buildings in remaining tiles from old city model
					breakLoops = false;
					for (i = 0; i < oldSizeY && !breakLoops; i++) {
						for (j = 0; j < oldSizeX && !breakLoops; j++) {
							if (i >= oldStartSyncY && j >= oldStartSyncX) {
								breakLoops = true;
							} else {
								for (Node building : GraphUtil.findChildrenOfNode(oldTiles[i][j])) {
									countTrans++;

									if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
										matcherTx.success();
										matcherTx.close();
										matcherTx = graphDb.beginTx();
									}

									createDeleteRelationshipNode(building, null, matcherRootNode, false);
								}
							}
						}
					}

					matcherTx.success();
				} finally {
					matcherTx.close();
				}
			} else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
				SpatialDatabaseService spatialDb = new SpatialDatabaseService(graphDb);
				EditableLayer oldBuildingLayer = (EditableLayer) spatialDb.getOrCreateEditableLayer("oldBuildingLayer");
				EditableLayer newBuildingLayer = (EditableLayer) spatialDb.getOrCreateEditableLayer("newBuildingLayer");

				if (SETTINGS.ENABLE_MULTI_THREADED_MATCHING) {
					/*
					 * Classic multi-threaded approach
					 */
					// ArrayList<Node> oldBuildingNodes;
					// try (Transaction tx = graphDb.beginTx()) {
					// oldBuildingNodes = GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL));
					// tx.success();
					// }
					//
					// int nThreads = Runtime.getRuntime().availableProcessors() * 2;
					// ExecutorService service = Executors.newFixedThreadPool(nThreads);
					//
					// int processedBuildings = 0;
					//
					// for (Node oldBuildingNode : oldBuildingNodes) {
					// processedBuildings++;
					// final int processedBuildingsInner = processedBuildings;
					//
					// service.execute(new Runnable() {
					// @Override
					// public void run() {
					// try (Transaction tx = graphDb.beginTx()) {
					// if (processedBuildingsInner % 50 == 0) {
					// logger.info("Processed buildings: " + processedBuildingsInner);
					// }
					//
					// Node newBuildingNode = GraphUtil.findBuildingInRTree(oldBuildingNode, newBuildingLayer, logger, graphDb);
					//
					// if (newBuildingNode == null) {
					// // delete from old city model
					// createDeleteRelationshipNode(oldBuildingNode, null, matcherRootNode, false);
					// } else {
					// matchNode(oldBuildingNode, newBuildingNode, matcherRootNode, new BooleanObject(false), mapperRootNode, null);
					// }
					//
					// tx.success();
					// } catch (FileNotFoundException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// } catch (XMLStreamException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
					// }
					// });
					// }
					//
					// wait for all threads to finish
					// logger.info("... shutting down threadpool ...");
					// service.shutdown();
					// service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);

					/*
					 * GraphAware multi-threaded approach
					 */
					// IterableInputBatchTransactionExecutor<?> batchExecutor = new IterableInputBatchTransactionExecutor<>(
					// graphDb,
					// 100,
					// oldBuildingNodes,
					// new UnitOfWork<Node>() {
					// @Override
					// public void execute(GraphDatabaseService database, Node oldBuildingNode, int batchNumber, int stepNumber) {
					//
					// if (batchNumber % 100 == 0) {
					// logger.info("Processed buildings: " + batchNumber);
					// }
					//
					// Node newBuildingNode = GraphUtil.findBuildingInRTree(oldBuildingNode, newBuildingLayer, logger, graphDb);
					//
					// try {
					// if (newBuildingNode == null) {
					// // delete from old city model
					// createDeleteRelationshipNode(oldBuildingNode, null, matcherRootNode, false);
					// } else {
					// matchNode(oldBuildingNode, newBuildingNode, matcherRootNode, new BooleanObject(false), mapperRootNode, null);
					// }
					// } catch (Exception e) {
					//
					// }
					//
					// }
					// });
					// BatchTransactionExecutor multiThreadedExecutor = new MultiThreadedBatchTransactionExecutor(batchExecutor);
					// multiThreadedExecutor.execute();

					/*
					 * Producer-Consumer approach
					 */
					int nThreads = Runtime.getRuntime().availableProcessors() * 2;
					ExecutorService service = Executors.newFixedThreadPool(nThreads);

					ArrayList<Node> oldBuildingNodes;
					try (Transaction tx = graphDb.beginTx()) {
						oldBuildingNodes = GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL));
						tx.success();
					}

					int producerSize = oldBuildingNodes.size() / SETTINGS.NR_OF_PRODUCERS;

					BuildingNodeConsumer.resetCounter();

					// the poison pill approach only reliably works in unbounded blocking queues
					BlockingQueue<Node> queue = new LinkedBlockingQueue<Node>(3 * SETTINGS.NR_OF_PRODUCERS * SETTINGS.CONSUMERS_PRO_PRODUCER * SETTINGS.NR_OF_COMMIT_BUILDINGS);

					for (int i = 0; i < SETTINGS.NR_OF_PRODUCERS; i++) {
						List<Node> tmpBuildingNodes;
						if (i == SETTINGS.NR_OF_PRODUCERS - 1) {
							// last producer receives the rest
							tmpBuildingNodes = oldBuildingNodes.subList(i * producerSize, oldBuildingNodes.size());
						} else {
							tmpBuildingNodes = oldBuildingNodes.subList(i * producerSize, (i + 1) * producerSize);
						}

						Thread producer = new Thread(new BuildingNodeProducer(tmpBuildingNodes, queue));
						service.execute(producer);

						for (int j = 0; j < SETTINGS.CONSUMERS_PRO_PRODUCER; j++) {
							Thread consumer = new Thread(new BuildingNodeConsumer(queue, graphDb, mapperRootNode, matcherRootNode, this, newBuildingLayer, logger));
							service.execute(consumer);
						}
					}

					// wait for all threads to finish
					// logger.info("... shutting down threadpool ...");
					service.shutdown();
					service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);
				} else {
					// single-threaded
					matcherTx = graphDb.beginTx();
					try {
						ArrayList<Node> oldBuildingNodes = GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL));

						for (Node oldBuildingNode : oldBuildingNodes) {

							Node newBuildingNode = GraphUtil.findBuildingInRTree(oldBuildingNode, newBuildingLayer, logger, graphDb);

							if (newBuildingNode == null) {
								// delete from old city model
								createDeleteRelationshipNode(oldBuildingNode, null, matcherRootNode, false);
							} else {
								matchNode(oldBuildingNode, newBuildingNode, matcherRootNode, new BooleanObject(false), mapperRootNode, null);
							}
						}

						matcherTx.success();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XMLStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						matcherTx.close();
					}
				}

				// then insert remaining buildings from new city model
				countTrans = 0;

				if (SETTINGS.ENABLE_MULTI_THREADED_MATCHING) {
					// multi-threaded
					ArrayList<Node> newBuildingNodes;
					try (Transaction tx = graphDb.beginTx()) {
						newBuildingNodes = GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL));
						tx.success();
					}

					int nThreads = Runtime.getRuntime().availableProcessors() * 2;
					ExecutorService service = Executors.newFixedThreadPool(nThreads);

					// to find buildings that have not been matched:
					// Either: use CONTENT_MATCHED / GEOMETRY_MATCHED nodes if available
					// Or: find them spatially using RTree

					if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE || SETTINGS.CREATE_MATCHED_CONTENT_NODE) {
						for (Node newBuildingNode : newBuildingNodes) {
							service.execute(new Runnable() {
								@Override
								public void run() {
									try (Transaction tx = graphDb.beginTx()) {
										if (!newBuildingNode.hasRelationship(Direction.INCOMING, TmpRelTypes.GEOMETRY_MATCHED)
												&& !newBuildingNode.hasRelationship(Direction.INCOMING, TmpRelTypes.CONTENT_MATCHED)) {
											createInsertRelationshipNode(
													GraphUtil.findFirstParentOfNode(newBuildingNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
													GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
													GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
										}

										tx.success();
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (XMLStreamException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					} else {
						for (Node newBuildingNode : newBuildingNodes) {
							service.execute(new Runnable() {
								@Override
								public void run() {
									try (Transaction tx = graphDb.beginTx()) {
										Node oldBuildingNode = GraphUtil.findBuildingInRTree(newBuildingNode, oldBuildingLayer, logger, graphDb);
										if (oldBuildingNode == null) {
											createInsertRelationshipNode(
													GraphUtil.findFirstParentOfNode(newBuildingNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
													GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
													GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
										}

										tx.success();
									} catch (FileNotFoundException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (XMLStreamException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							});
						}
					}

					// wait for all threads to finish
					// logger.info("... shutting down threadpool ...");
					service.shutdown();
					service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);
				} else {
					// single-threaded
					matcherTx = graphDb.beginTx();
					try {
						ArrayList<Node> newBuildingNodes = GraphUtil.findBuildings(GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL));

						// to find buildings that have not been matched:
						// Either: use CONTENT_MATCHED / GEOMETRY_MATCHED nodes if available
						// Or: find them spatially using RTree

						if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE) {
							for (Node newBuildingNode : newBuildingNodes) {
								countTrans++;

								if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
									matcherTx.success();
									matcherTx.close();
									matcherTx = graphDb.beginTx();
								}

								if (!newBuildingNode.hasRelationship(Direction.INCOMING, TmpRelTypes.GEOMETRY_MATCHED)) {
									createInsertRelationshipNode(
											GraphUtil.findFirstParentOfNode(newBuildingNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
											GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
											GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
								}
							}
						} else {
							for (Node newBuildingNode : newBuildingNodes) {
								countTrans++;

								if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
									matcherTx.success();
									matcherTx.close();
									matcherTx = graphDb.beginTx();
								}

								Node oldBuildingNode = GraphUtil.findBuildingInRTree(newBuildingNode, oldBuildingLayer, logger, graphDb);
								if (oldBuildingNode == null) {
									createInsertRelationshipNode(
											GraphUtil.findFirstParentOfNode(newBuildingNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
											GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
											GMLRelTypes.CITY_OBJECT_MEMBER, matcherRootNode, false, null);
								}
							}
						}

						matcherTx.success();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XMLStreamException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						matcherTx.close();
					}
				}
			}
		} else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.NONE)) {
			matcherTx = graphDb.beginTx();
			try {
				matchNode(
						GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
						GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.NEW_CITY_MODEL),
						matcherRootNode, new BooleanObject(false), mapperRootNode, null);

				matcherTx.success();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				matcherTx.close();
			}
		}
	}

	/**
	 * Connect all edit operators to a root node for easier retrieval.
	 * 
	 * @param matcherRootNode
	 * @param graphDb
	 */
	public void matcherPostProcessing(Node matcherRootNode, GraphDatabaseService graphDb) {
		matcherTx = graphDb.beginTx();
		countTrans = 0;

		logger.info("Post processing matcher ...");

		try {
			for (EditOperators enu : EditOperators.values()) {
				ResourceIterator<Node> itEditors = graphDb.findNodes(Label.label(enu + ""));
				while (itEditors.hasNext()) {
					countTrans++;

					if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
						matcherTx.success();
						matcherTx.close();
						matcherTx = graphDb.beginTx();
					}

					Node editor = itEditors.next();
					matcherRootNode.createRelationshipTo(editor, EditRelTypes.CONSISTS_OF);
				}
			}

			matcherTx.success();
		} finally {
			matcherTx.close();
		}
	}

	// editor == null ? (only scout) : (create edit nodes)
	public boolean matchNode(Node oldNode, Node newNode, Node editor, BooleanObject isOptional, Node mapperRootNode, RelationshipType ignoreRelType) throws FileNotFoundException, XMLStreamException {
		if (!SETTINGS.ENABLE_MULTI_THREADED_MATCHING) {
			countTrans++;

			if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
				if (countBuildings % SETTINGS.LOG_EVERY_N_BUILDINGS == 0) {
					logger.info("Processed buildings: " + countBuildings);
				}

				matcherTx.success();
				matcherTx.close();
				matcherTx = graphDb.beginTx();
			}
		} else if (ignoreRelType != null) {
			countTrans++;

			if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
				matcherTx.success();
				matcherTx.close();
				matcherTx = graphDb.beginTx();
			}
		}

		boolean matchProperties;
		boolean matchRelationships;

		// test if both nodes have already been matched
		if (SETTINGS.CREATE_MATCHED_CONTENT_NODE && isContentMatched(oldNode, newNode)) {
			return true;
		}

		/*
		 * MATCH LABELS
		 */
		String oldNodeLabel = oldNode.getLabels().iterator().next().toString();
		String newNodeLabel = newNode.getLabels().iterator().next().toString();

		if (!oldNodeLabel.equals(newNodeLabel)) {
			return false;
		}

		if (oldNodeLabel.equals(CityGMLClass.BUILDING + "")) {
			countBuildings += 2;
			// logger.info("... matching BUILDING " + oldNode.getProperty("id").toString() + " and " + newNode.getProperty("id").toString() + " ...");
		}

		/*
		 * MATCH ALL PROPERTIES
		 */
		matchProperties = matchProperties(oldNode, newNode, editor, isOptional);
		if (editor == null && !matchProperties) {
			return false;
		}

		/*
		 * MATCH ALL RELATIONSHIPS
		 */
		matchRelationships = true;

		Iterable<RelationshipType> oldRelTypes = GraphUtil.findRelationshipTypesExclude(oldNode, ignoreRelType);
		Iterable<RelationshipType> newRelTypes = GraphUtil.findRelationshipTypesExclude(newNode, ignoreRelType);

		// match relationships contained in both nodes
		for (Iterator<RelationshipType> oldIt = oldRelTypes.iterator(); oldIt.hasNext();) {
			RelationshipType oldRelType = (RelationshipType) oldIt.next();

			for (Iterator<RelationshipType> newIt = newRelTypes.iterator(); newIt.hasNext();) {
				RelationshipType newRelType = (RelationshipType) newIt.next();

				if ((oldRelType.toString()).equals(newRelType.toString())) {
					// do not match if both bounding shapes were internally generated
					if ((oldNode.hasLabel(Label.label(CityGMLClass.BUILDING + "")) || oldNode.hasLabel(Label.label(CityGMLClass.BUILDING_PART + "")))
							&& oldRelType.toString().equals(GMLRelTypes.BOUNDED_BY.toString())) {

						String oldVal = null;
						String newVal = null;

						try {
							oldVal = oldNode.getProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString()).toString();
							newVal = newNode.getProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString()).toString();
						} catch (Exception ex) {
							logger.warning("WARNING: ID = " + oldNode.getProperty("id", null) + " " + ex.getMessage());
						}

						if (oldVal.equals("true") || newVal.equals("true")) {
							if (oldVal.equals("true") && newVal.equals("false")) {
								createInsertRelationshipNode(GraphUtil.findFirstChildOfNode(newNode, GMLRelTypes.BOUNDED_BY), oldNode, GMLRelTypes.BOUNDED_BY, editor, isOptional.getValue(), null);
							} else if (oldVal.equals("false") && newVal.equals("true")) {
								createDeleteRelationshipNode(GraphUtil.findFirstChildOfNode(oldNode, GMLRelTypes.BOUNDED_BY), oldNode, editor, isOptional.getValue());
							}

							oldIt.remove();
							newIt.remove();
							continue;
						}
					}

					// list of child-nodes of the current relationship type
					ArrayList<Node> oldNodes = GraphUtil.findSortedChildrenOfNode(oldNode, oldRelType);
					ArrayList<Node> newNodes = GraphUtil.findSortedChildrenOfNode(newNode, newRelType);

					boolean[][] rounds = new boolean[][] {
							// [id, geo, content, order]
							{ true, true, false, false }, // ROUND 1: MATCH BY ID AND GEOMETRY
							{ false, true, false, false }, // ROUND 2: MATCH BY GEOMETRY
							{ false, false, true, false }, // ROUND 3: MATCH BY CONTENTS
							{ false, false, false, true } // ROUND 4: MATCH BY ORDER OF APPEARANCE
					};
					for (int i = 0; i < rounds.length; i++) {
						for (Iterator<Node> oldIterator = oldNodes.iterator(); oldIterator.hasNext();) {
							BooleanObject contentMatched = new BooleanObject(false);
							BooleanObject tmpIsOptional = new BooleanObject(false);

							Node oldChildNode = (Node) oldIterator.next();
							Node newChildNode = null;

							// if there is only one node with the same label -> accept
							if (oldNodes.size() == 1 && newNodes.size() == 1
									&& (GraphUtil.getLabelString(oldNodes.get(0))).equals(GraphUtil.getLabelString(newNodes.get(0)))
							// && (oldNodes.get(0).getLabels().iterator().next().equals(Label.label(CityGMLClass.CITY_MODEL + "")))
							) {

								// logging("... accepting " + oldChildNode.getLabels().iterator().next() + " nodes because each context parent has only one of them ...");

								// remove found candidate from the list
								newChildNode = newNodes.get(0);
								newNodes.remove(0);

								// test if both nodes have already been geometrically matched
								BooleanObject candidateFound = null;
								if ((SETTINGS.CREATE_MATCHED_GEOMETRY_NODE && isGeometryMatched(oldChildNode, newChildNode))
										|| ((candidateFound = matchGeometry(oldChildNode, newChildNode)) != null && candidateFound.getValue())) {
									// tmp node indicating that these two nodes are already successfully matched
									if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE) {
										createGeometryMatchedInfoNode(oldChildNode, newChildNode);
									}

									// logging("... found same geometry of " + oldChildNode.getLabels().iterator().next() + " nodes ...");

									contentMatched.setValue(false);

									String oldChildNodeLabel = GraphUtil.getLabelString(oldChildNode);
									tmpIsOptional.setValue(
											(!oldChildNodeLabel.equals(CityGMLClass.CITY_OBJECT_MEMBER + ""))
													&& (!oldChildNodeLabel.equals(CityGMLClass.BUILDING_PART_PROPERTY + ""))
													&& (!oldChildNodeLabel.equals(CityGMLClass.BUILDING + ""))
													&& (!oldChildNodeLabel.equals(CityGMLClass.BUILDING_PART + "")));
								}
							} else {
								newChildNode = findCandidate(oldChildNode, newNodes, rounds[i], editor, contentMatched, tmpIsOptional, mapperRootNode, ignoreRelType);
							}

							if (newChildNode == null) {
								if (i == rounds.length - 1) { // last round
									if (editor == null) {
										return false;
									}

									// node has been deleted
									matchRelationships = false;
									createDeleteRelationshipNode(oldChildNode, oldNode, editor, isOptional.getValue());

									// after matching is finished, remove these nodes
									// newChildNode has already been removed from newNodes in findCandidate function
									oldIterator.remove();
								} else {
									continue;
								}
							} else {
								// if a building is located in this tile, remove its links to other tiles
								if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) && oldChildNode.hasLabel(Label.label(CityGMLClass.BUILDING + ""))) {
									// oldChildNode is a building, oldNode is a tile
									for (Relationship rel : oldChildNode.getRelationships(Direction.INCOMING, TmpRelTypes.TILE_CONSISTS_OF)) {
										if (!rel.getOtherNode(oldChildNode).equals(oldNode)) {
											rel.delete();
										}
									}

									for (Relationship rel : newChildNode.getRelationships(Direction.INCOMING, TmpRelTypes.TILE_CONSISTS_OF)) {
										if (!rel.getOtherNode(newChildNode).equals(newNode)) {
											rel.delete();
										}
									}
								}

								// if Round 3 found a candidate, this candidate must have the SAME content -> no need to match anymore
								if (!contentMatched.getValue()) {
									// once isOptional == true, it will never change
									tmpIsOptional.setOr(isOptional, tmpIsOptional);

									// compare contents of candidates
									if (matchNode(oldChildNode, newChildNode, editor, tmpIsOptional, mapperRootNode, ignoreRelType)) {
										// tmp node indicating that these two nodes are already successfully matched
										if (SETTINGS.CREATE_MATCHED_CONTENT_NODE) {
											createContentMatchedInfoNode(oldChildNode, newChildNode);
										}
									} else {
										matchRelationships = false;
										if (editor == null) {
											return false;
										}
									}
								}

								oldIterator.remove();
							}
						}
					}

					// after oldNodes is done iterated
					for (Iterator<Node> newIterator = newNodes.iterator(); newIterator.hasNext();) {
						Node newChildNode = (Node) newIterator.next();

						if (editor == null) {
							return false;
						}

						// node has been inserted
						matchRelationships = false;
						if ((SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) || SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE))
								&& GraphUtil.getLabelString(newChildNode).equals(CityGMLClass.BUILDING + "")) {
							createInsertRelationshipNode(
									GraphUtil.findFirstParentOfNode(newChildNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
									GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
									GMLRelTypes.CITY_OBJECT_MEMBER, editor, false, oldNode);
						} else {
							createInsertRelationshipNode(newChildNode, oldNode, oldRelType, editor, isOptional.getValue(), null);
						}

						newIterator.remove();
					}

					// after matching is done, remove these relationship types
					oldIt.remove();
					newIt.remove();
					break;
				}
			}
		}

		// remaining relationships of oldNode
		for (Iterator<RelationshipType> oldIt = oldRelTypes.iterator(); oldIt.hasNext();) {
			RelationshipType oldRelType = (RelationshipType) oldIt.next();

			if (editor == null) {
				return false;
			}

			// newNode does not have this relationship type -> delete
			matchRelationships = false;
			for (Node oldChildNode : GraphUtil.findChildrenOfNode(oldNode, oldRelType)) {
				createDeleteRelationshipNode(oldChildNode, oldNode, editor, isOptional.getValue());
			}

			oldIt.remove();
		}

		// remaining relationships of newNode
		for (Iterator<RelationshipType> newIt = newRelTypes.iterator(); newIt.hasNext();) {
			RelationshipType newRelType = (RelationshipType) newIt.next();

			if (editor == null) {
				return false;
			}

			// oldNode does not have this relationship type -> insert
			matchRelationships = false;
			for (Node newChildNode : GraphUtil.findChildrenOfNode(newNode, newRelType)) {
				if ((SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) || SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE))
						&& GraphUtil.getLabelString(newChildNode).equals(CityGMLClass.BUILDING + "")) {
					createInsertRelationshipNode(
							GraphUtil.findFirstParentOfNode(newChildNode, GMLRelTypes.OBJECT), // CITY_OBJECT_MEMBER
							GraphUtil.findFirstChildOfNode(mapperRootNode, GMLRelTypes.OLD_CITY_MODEL),
							GMLRelTypes.CITY_OBJECT_MEMBER, editor, false, oldNode);
				} else {
					createInsertRelationshipNode(newChildNode, oldNode, newRelType, editor, isOptional.getValue(), null);
				}
			}

			newIt.remove();
		}

		return matchProperties && matchRelationships;
	}

	// find the best matching candidate based on its gmlid
	private Node findCandidate(Node oldChildNode, ArrayList<Node> newNodes, boolean[] round, Node editor,
			BooleanObject contentMatched, BooleanObject isOptional, Node mapperRootNode, RelationshipType ignoreRelType) throws FileNotFoundException, XMLStreamException {
		for (Iterator<Node> newIterator = newNodes.iterator(); newIterator.hasNext();) {
			Node newChildNode = (Node) newIterator.next();

			// test if both nodes have already been matched
			if (SETTINGS.CREATE_MATCHED_CONTENT_NODE && isContentMatched(oldChildNode, newChildNode)) {
				// remove found candidate from the list

				// logging("... skipping already content-matched " + oldChildNode.getLabels().iterator().next().toString() + " nodes ...");

				newIterator.remove();
				contentMatched.setValue(true);
				isOptional.setValue(false);
				return newChildNode;
			}

			// accept only same labels
			String oldLabel = GraphUtil.getLabelString(oldChildNode);
			String newLabel = GraphUtil.getLabelString(newChildNode);
			if (!oldLabel.equals(newLabel)) {
				continue;
			}

			// ROUND 1: FIND BY ID FIRST
			if (round[0]) {
				String oldId = (String) oldChildNode.getProperty("id", null);
				String newId = (String) newChildNode.getProperty("id", null);

				if (oldId == null || newId == null) {
					// nodes do not have IDs
					break;
				}

				if (!oldId.equals(newId)) {
					continue;
				}

				// logging("... found same IDs = " + oldId + " of " + oldLabel + " nodes ...");
			}

			// ROUND 2: FIND BY SPATIAL PROPERTIES
			if (round[1]) {
				// test if both nodes have already been matched
				if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE && isGeometryMatched(oldChildNode, newChildNode)) {
					// remove found candidate from the list

					// logging("... skipping already geometry-matched nodes ...");

					newIterator.remove();
					contentMatched.setValue(false);
					isOptional.setValue(
							(!oldLabel.equals(CityGMLClass.CITY_OBJECT_MEMBER + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING_PART_PROPERTY + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING_PART + "")));
					return newChildNode;
				}

				BooleanObject candidateFound = matchGeometry(oldChildNode, newChildNode);
				if (candidateFound != null && candidateFound.getValue()) {
					// tmp node indicating that these two nodes are already successfully matched
					if (SETTINGS.CREATE_MATCHED_GEOMETRY_NODE) {
						createGeometryMatchedInfoNode(oldChildNode, newChildNode);
					}

					// remove found candidate from the list

					// logging("... found same geometry of " + oldLabel + " nodes ...");

					newIterator.remove();
					contentMatched.setValue(false);
					isOptional.setValue(
							(!oldLabel.equals(CityGMLClass.CITY_OBJECT_MEMBER + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING_PART_PROPERTY + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING + ""))
									&& (!oldLabel.equals(CityGMLClass.BUILDING_PART + "")));
					return newChildNode;
				}
			}

			// ROUND 3: FIND BY CONTENTS
			if (round[2]) {
				this.suppressLogger = true;
				boolean candidateFound = matchNode(oldChildNode, newChildNode, null, null, mapperRootNode, ignoreRelType);
				this.suppressLogger = false;

				// logging("... probing of " + oldChildNode.getLabels().iterator().next() + " nodes for content equivalence yields: " + candidateFound + " ...");

				if (candidateFound) {
					// remove found candidate from the list
					// logging("... found same contents of " + oldLabel + " nodes ...");
					newIterator.remove();
					contentMatched.setValue(true);
					isOptional.setValue(false);
					return newChildNode;
				}
			}

			// ROUND 4: FIND BY ORDER OR APPEARANCE
			if (round[3]) {
				// remove found candidate from the list

				// logging("... matching " + oldLabel + " nodes by order of appearance...");

				newIterator.remove();
				contentMatched.setValue(false);
				isOptional.setValue(false);
				return newChildNode;
			}
		}

		contentMatched.setValue(false);
		isOptional.setValue(false);
		return null;
	}

	private boolean matchProperties(Node oldNode, Node newNode, Node editor, BooleanObject isOptional) throws FileNotFoundException, XMLStreamException {
		// ignore properties of tile nodes
		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
			for (ModelClassEnum enu : TileNodes.values()) {
				if (oldNode.hasLabel(Label.label(enu + ""))) {
					return true;
				}
			}
		}

		boolean matched = true;

		Set<Entry<String, Object>> oldProperties = oldNode.getAllProperties().entrySet();
		Set<Entry<String, Object>> newProperties = newNode.getAllProperties().entrySet();

		// StringBuilder tmp = new StringBuilder();
		// tmp.append("... matching properties of " + oldNode.getLabels().iterator().next() + " ...");
		for (Iterator<Entry<String, Object>> oldIt = oldProperties.iterator(); oldIt.hasNext();) {
			Entry<String, Object> oldPair = oldIt.next();

			for (Iterator<Entry<String, Object>> newIt = newProperties.iterator(); newIt.hasNext();) {
				Entry<String, Object> newPair = newIt.next();

				if (oldPair.getKey().equals(newPair.getKey())) {
					boolean shouldIgnore = false;
					for (InternalMappingProperties ignoreProp : InternalMappingProperties.values()) {
						if (ignoreProp.toString().equals(oldPair.getKey())) {
							shouldIgnore = true;
							break;
						}
					}

					// if (!oldPair.getKey().equals("href")
					if (!shouldIgnore) {
						boolean matchValue = (oldPair.getValue()).equals(newPair.getValue());
						// tmp.append("\n" + compactIndent + oldPair.getKey() + ": " + matchValue);
						if (!matchValue) {
							if (editor == null) {
								return false;
							}

							matched = false;

							if (((String) oldPair.getKey()).equals(GMLRelTypes.VALUE + "")) {
								createUpdatePropertyNode(oldNode, editor, oldPair.getKey(), oldPair.getValue(), newPair.getValue(), isOptional.getValue());
							} else {
								createUpdatePropertyNode(oldNode, editor, oldPair.getKey(), oldPair.getValue(), newPair.getValue(), false);
							}
						}
					}

					// after matching is finished, remove these attribute names
					oldIt.remove();
					newIt.remove();
				}
			}
		}

		// logging(tmp.toString());

		// remaining properties of oldNode
		for (Iterator<Entry<String, Object>> oldIt = oldProperties.iterator(); oldIt.hasNext();) {
			Entry<String, Object> oldPair = oldIt.next();

			if (editor == null) {
				return false;
			}

			// newNode does not have this property -> delete
			matched = false;

			// logging("... matching property " + oldPair.getKey() + ": DELETE ...");

			if (((String) oldPair.getKey()).equals(GMLRelTypes.VALUE + "")) {
				createDeletePropertyNode(oldNode, editor, oldPair.getKey(), isOptional.getValue());
			} else {
				createDeletePropertyNode(oldNode, editor, oldPair.getKey(), false);
			}
			oldIt.remove();
		}

		// remaining properties of newNode
		for (Iterator<Entry<String, Object>> newIt = newProperties.iterator(); newIt.hasNext();) {
			Entry<String, Object> newPair = newIt.next();

			if (editor == null) {
				return false;
			}

			// oldNode does not have this property -> insert
			matched = false;

			// logging("... matching property " + newPair.getKey() + ": INSERT ...");

			if (((String) newPair.getKey()).equals(GMLRelTypes.VALUE + "")) {
				createInsertPropertyNode(oldNode, editor, newPair.getKey(), newPair.getValue(), isOptional.getValue());
			} else {
				createInsertPropertyNode(oldNode, editor, newPair.getKey(), newPair.getValue(), false);
			}

			newIt.remove();
		}

		return matched;
	}

	private BooleanObject matchGeometry(Node oldNode, Node newNode) {
		String oldLabel = GraphUtil.getLabelString(oldNode);
		
		BooleanObject result = null;

		if (oldLabel.equals(CityGMLClass.CITY_OBJECT_MEMBER + "")) {
			result = new BooleanObject(matchGeometryCityObjectMember(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING + "")) {
			result = new BooleanObject(matchGeometryBuildingOrBuildingPart(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_PART_PROPERTY + "")) {
			result = new BooleanObject(matchGeometryBuildingOrBuildingPartProperty(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_PART + "")) {
			result = new BooleanObject(matchGeometryBuildingOrBuildingPart(oldNode, newNode));
		} 
		
		else if (oldLabel.equals(CityGMLClass.BUILDING_BOUNDARY_SURFACE_PROPERTY + "")) {
			result = new BooleanObject(matchGeometryBoundarySurfaceProperty(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_CEILING_SURFACE + "")) {
			result = new BooleanObject(matchGeometryCeilingSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_CLOSURE_SURFACE + "")) {
			result = new BooleanObject(matchGeometryClosureSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_FLOOR_SURFACE + "")) {
			result = new BooleanObject(matchGeometryFloorSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_GROUND_SURFACE + "")) {
			result = new BooleanObject(matchGeometryGroundSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE + "")) {
			result = new BooleanObject(matchGeometryInteriorWallSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE + "")) {
			result = new BooleanObject(matchGeometryOuterCeilingSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE + "")) {
			result = new BooleanObject(matchGeometryOuterFloorSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_ROOF_SURFACE + "")) {
			result = new BooleanObject(matchGeometryRoofSurface(oldNode, newNode));
		} else if (oldLabel.equals(CityGMLClass.BUILDING_WALL_SURFACE + "")) {
			result = new BooleanObject(matchGeometryWallSurface(oldNode, newNode));
		} 
		
		else if (oldLabel.equals(GMLClass.POLYGON + "")) {
			result = new BooleanObject(matchGeometryPolygon(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.LINEAR_RING + "")) {
			result = new BooleanObject(matchGeometryLinearRing(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.RING + "")) {
			result = new BooleanObject(matchGeometryRing(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.ENVELOPE + "")) {
			result = new BooleanObject(matchGeometryEnvelope(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.POINT + "")) {
			result = new BooleanObject(matchGeometryPoint(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.POINT_PROPERTY + "")) {
			result = new BooleanObject(matchGeometryPointProperty(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.POINT_REP + "")) {
			result = new BooleanObject(matchGeometryPointRep(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.DIRECT_POSITION + "")) {
			result = new BooleanObject(matchGeometryDirectPosition(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.COORD + "")) {
			result = new BooleanObject(matchGeometryCoord(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.DIRECT_POSITION_LIST + "")) {
			result = new BooleanObject(matchGeometryDirectPositionList(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.COORDINATES + "")) {
			result = new BooleanObject(matchGeometryCoordinates(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.LINE_STRING_PROPERTY + "")) {
			result = new BooleanObject(matchGeometryLineStringProperty(oldNode, newNode));
		} else if (oldLabel.equals(GMLClass.LINE_STRING + "")) {
			result = new BooleanObject(matchGeometryLineString(oldNode, newNode));
		} else{
			// non-geometric nodes
			result = null;
		}
		
		if ((result != null) && (!suppressLogger) && (result.getValue()) && (oldNode.hasProperty("id")) && (newNode.hasProperty("id"))) {
			logger.info("MATCHED [" + oldLabel + "] " + oldNode.getProperty("id").toString() + "\n" 
						+ String.format("%20s", "") + "   WITH [" + oldLabel + "] " + newNode.getProperty("id").toString());
		}

		return result;
	}

	/*
	 * Modular geometric functions
	 */
	public boolean matchGeometryCityObjectMember(Node oldNode, Node newNode) {
		Node oldChild = GraphUtil.findFirstChildOfNode(oldNode, Label.label(CityGMLClass.BUILDING + ""));
		Node newChild = GraphUtil.findFirstChildOfNode(newNode, Label.label(CityGMLClass.BUILDING + ""));

		return (oldChild != null && newChild != null && matchGeometryBuildingOrBuildingPart(oldChild, newChild));
	}

	public boolean matchGeometryBuildingOrBuildingPartProperty(Node oldNode, Node newNode) {
		Node oldChild = GraphUtil.findFirstChildOfNode(oldNode, Label.label(CityGMLClass.BUILDING_PART + ""));
		Node newChild = GraphUtil.findFirstChildOfNode(newNode, Label.label(CityGMLClass.BUILDING_PART + ""));

		return (oldChild != null && newChild != null && matchGeometryBuildingOrBuildingPart(oldChild, newChild));
	}

	public boolean matchGeometryBuildingOrBuildingPart(Node oldNode, Node newNode) {
		Node[] cornerNodes = {
				GraphUtil.getLowerCornerBoundingShapeOfBuilding(oldNode), // old lower corner
				GraphUtil.getUpperCornerBoundingShapeOfBuilding(oldNode), // old upper corner
				GraphUtil.getLowerCornerBoundingShapeOfBuilding(newNode), // new lower corner
				GraphUtil.getUpperCornerBoundingShapeOfBuilding(newNode) // new upper corner
		};

		String[] cornerValues = new String[4];
		for (int i = 0; i < cornerNodes.length; i++) {
			if (cornerNodes[i] == null) {
				return false;
			}

			cornerValues[i] = (String) cornerNodes[i].getProperty(GMLRelTypes.VALUE + "", null);
		}

		String[][] cornerPoints = new String[4][3];
		for (int i = 0; i < cornerValues.length; i++) {
			if (cornerValues[i] == null) {
				return false;
			}

			cornerPoints[i] = cornerValues[i].split(";");
		}

		// double vol = GeometryUtil.calcSharedVolOfBoxes(cornerPoints[0], cornerPoints[1], cornerPoints[2], cornerPoints[3])
		// / GeometryUtil.calcBoxVol(cornerPoints[0], cornerPoints[1]);
		// boolean result = (vol >= this.SHARED_VOL_THRESHOLD);

		double buildingVolume = GeometryUtil.calcBoxVol(cornerPoints[0], cornerPoints[1]);
		double otherVolume = GeometryUtil.calcBoxVol(cornerPoints[2], cornerPoints[3]);
		double sharedVolume = GeometryUtil.calcSharedVolOfBoxes(cornerPoints[0], cornerPoints[1], cornerPoints[2], cornerPoints[3]);

		double buildingRatio = sharedVolume / buildingVolume;
		double otherRatio = sharedVolume / otherVolume;
		double minOfBothRatios = Math.min(buildingRatio, otherRatio);

		boolean result = minOfBothRatios >= SETTINGS.BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD;

		if (logger.isLoggable(Level.FINE)) {
			logging("... comparing minimum percentage of " + oldNode.getLabels().iterator().next() + "s' shared volume of both buildings "
					+ minOfBothRatios * 100 + "% >= " + SETTINGS.BUILDING_SHARED_VOL_PERCENTAGE_THRESHOLD * 100 + "%: " + result + " ...");
		}

		return result;
	}

	public boolean matchGeometryBoundarySurfaceProperty(Node oldNode, Node newNode) {
		Area3D oldArea = calcBoundarySurfaceProperty(oldNode);
		Area3D newArea = calcBoundarySurfaceProperty(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryCeilingSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcCeilingSurface(oldNode);
		Area3D newArea = calcCeilingSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryClosureSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcClosureSurface(oldNode);
		Area3D newArea = calcClosureSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryFloorSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcFloorSurface(oldNode);
		Area3D newArea = calcFloorSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryGroundSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcGroundSurface(oldNode);
		Area3D newArea = calcGroundSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryInteriorWallSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcInteriorWallSurface(oldNode);
		Area3D newArea = calcInteriorWallSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryOuterCeilingSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcOuterCeilingSurface(oldNode);
		Area3D newArea = calcOuterCeilingSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryOuterFloorSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcOuterFloorSurface(oldNode);
		Area3D newArea = calcOuterFloorSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryRoofSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcRoofSurface(oldNode);
		Area3D newArea = calcRoofSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryWallSurface(Node oldNode, Node newNode) {
		Area3D oldArea = calcWallSurface(oldNode);
		Area3D newArea = calcWallSurface(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryPolygon(Node oldNode, Node newNode) {
		Area3D oldArea = calcPolygon(oldNode);
		Area3D newArea = calcPolygon(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryLinearRing(Node oldNode, Node newNode) {
		Area3D oldArea = calcLinearRing(oldNode);
		Area3D newArea = calcLinearRing(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryRing(Node oldNode, Node newNode) {
		Area3D oldArea = calcRing(oldNode);
		Area3D newArea = calcRing(newNode);

		if (oldArea.isEmpty() || newArea.isEmpty()) {
			return false;
		}

		// boolean result = oldArea.equals(newArea);
		boolean result = GeometryUtil.fuzzyEquals(oldArea, newArea);

		if (logger.isLoggable(Level.FINE)) {
			StringBuilder tmp = new StringBuilder();
			tmp.append("... equivalent geometry: " + result + " ...");
			logging(tmp.toString());
		}

		return result;
	}

	public boolean matchGeometryEnvelope(Node oldNode, Node newNode) {
		double[][] oldLowerUpperCorner = GeometryUtil.getLowerUpperCorner(BoundingBoxCalculator.createEnvelope(oldNode), logger);
		double[][] newLowerUpperCorner = GeometryUtil.getLowerUpperCorner(BoundingBoxCalculator.createEnvelope(newNode), logger);

		for (int i = 0; i < oldLowerUpperCorner.length; i++) {
			if (oldLowerUpperCorner[i].length != newLowerUpperCorner[i].length) {
				return false;
			}

			for (int j = 0; j < oldLowerUpperCorner[i].length; j++) {
				if (Math.abs(oldLowerUpperCorner[i][j] - newLowerUpperCorner[i][j]) > SETTINGS.ERR_TOLERANCE) {
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * Point
	 */
	public boolean matchGeometryPoint(Node oldNode, Node newNode) {
		double[] oldPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createPoint(oldNode));
		double[] newPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createPoint(newNode));

		return GeometryUtil.fuzzyEquals(oldPoint, newPoint);
	}

	public boolean matchGeometryPointProperty(Node oldNode, Node newNode) {
		return matchGeometryPoint(GraphUtil.findFirstChildOfNode(oldNode, GMLRelTypes.OBJECT), GraphUtil.findFirstChildOfNode(newNode, GMLRelTypes.OBJECT));
	}

	public boolean matchGeometryPointRep(Node oldNode, Node newNode) {
		return matchGeometryPoint(GraphUtil.findFirstChildOfNode(oldNode, GMLRelTypes.OBJECT), GraphUtil.findFirstChildOfNode(newNode, GMLRelTypes.OBJECT));
	}

	public boolean matchGeometryDirectPosition(Node oldNode, Node newNode) {
		double[] oldPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createDirectPosition(oldNode));
		double[] newPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createDirectPosition(newNode));

		return GeometryUtil.fuzzyEquals(oldPoint, newPoint);
	}

	public boolean matchGeometryCoord(Node oldNode, Node newNode) {
		double[] oldPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createCoord(oldNode));
		double[] newPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createCoord(newNode));

		return GeometryUtil.fuzzyEquals(oldPoint, newPoint);
	}

	public boolean matchGeometryDirectPositionList(Node oldNode, Node newNode) {
		double[][] oldPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createDirectPositionList(oldNode));
		double[][] newPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createDirectPositionList(newNode));

		return GeometryUtil.fuzzyEquals(oldPoint, newPoint);
	}

	public boolean matchGeometryCoordinates(Node oldNode, Node newNode) {
		double[][] oldPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createCoordinates(oldNode));
		double[][] newPoint = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createCoordinates(newNode));

		return GeometryUtil.fuzzyEquals(oldPoint, newPoint);
	}

	/*
	 * Curve/LineString
	 */
	public boolean matchGeometryLineStringProperty(Node oldNode, Node newNode) {
		return matchGeometryLineString(GraphUtil.findFirstChildOfNode(oldNode, GMLRelTypes.OBJECT), GraphUtil.findFirstChildOfNode(newNode, GMLRelTypes.OBJECT));
	}

	public boolean matchGeometryLineString(Node oldNode, Node newNode) {
		ArrayList<double[]> oldPoints = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createLineString(oldNode));
		ArrayList<double[]> newPoints = GeometryUtil.getDoubleArray(BoundingBoxCalculator.createLineString(newNode));

		return GeometryUtil.fuzzyEquals(oldPoints, newPoints);
	}

	/*
	 * Hierarchical auxiliary functions for area calculation
	 */
	public Area3D calcSearchAbstractRing(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcLinearRing(node));
		result.add(calcRing(node));

		return result;
	}

	public Area3D calcSearchListPosOrPointPropertyOrPointRep(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		ArrayList<String[]> points = new ArrayList<String[]>();
		for (Node node : nodeList) {
			points.add(calcPosOrPointPropertyOrPointRep(node));
		}

		String pointsString = "";
		int dimension = -1;
		for (String[] st : points) {
			if (st == null) {
				continue;
			}

			if (dimension < 0) {
				dimension = st.length;
			}

			String point = "";
			for (int i = 0; i < st.length; i++) {
				point += st[i] + ";";
			}

			pointsString += point;
		}
		if (pointsString.length() > 0) {
			pointsString = pointsString.substring(0, pointsString.length() - 1);
			return GeometryUtil.createArea(pointsString, dimension, ";", logger, suppressLogger);
		}

		return result;
	}

	public String[] calcSearchPointProperty(Node node) {
		if (node == null) {
			return null;
		}

		String[] result = calcPointProperty(node);
		if (result == null || result.length == 0) {
			return calcPointRep(node);
		}

		return result;
	}

	public Area3D calcSearchListCoord(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		ArrayList<String[]> points = new ArrayList<String[]>();
		for (Node node : nodeList) {
			points.add(calcCoord(node));
		}

		String pointsString = "";
		int dimension = -1;
		for (String[] st : points) {
			if (st == null) {
				continue;
			}

			if (dimension < 0) {
				dimension = st.length;
			}

			String point = "";
			for (int i = 0; i < st.length; i++) {
				point += st[i] + ";";
			}

			pointsString += point;
		}
		if (pointsString.length() > 0) {
			pointsString = pointsString.substring(0, pointsString.length() - 1);
			return GeometryUtil.createArea(pointsString, dimension, ";", logger, suppressLogger);
		}

		return result;
	}

	public Area3D calcSearchListCurveProperty(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		ArrayList<String[]> points = new ArrayList<String[]>();
		for (Node node : nodeList) {
			points.addAll(calcCurveProperty(node));
		}

		String pointsString = "";
		int dimension = -1;
		for (String[] st : points) {
			if (st == null) {
				continue;
			}

			if (dimension < 0) {
				dimension = st.length;
			}

			String point = "";
			for (int i = 0; i < st.length; i++) {
				point += st[i] + ";";
			}

			pointsString += point;
		}
		if (pointsString.length() > 0) {
			pointsString = pointsString.substring(0, pointsString.length() - 1);
			return GeometryUtil.createArea(pointsString, dimension, ";", logger, suppressLogger);
		}

		return result;
	}

	public ArrayList<String[]> calcSearchAbstractCurve(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null) {
			return result;
		}

		result.addAll(calcCompositeCurve(node));
		result.addAll(calcCurve(node));
		result.addAll(calcLineString(node));
		result.addAll(calcOrientableCurve(node));

		return result;
	}

	public Area3D calcSearchListAbstractRingProperty(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		for (Node node : nodeList) {
			result.add(calcSearchAbstractRingProperty(node));
		}

		return result;
	}

	public Area3D calcSearchAbstractRingProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcExterior(node));
		result.add(calcInnerBoundaryIs(node));
		result.add(calcInterior(node));
		result.add(calcOuterBoundaryIs(node));

		return result;
	}

	/*
	 * Modular geometric functions for area calculation
	 */
	public Area3D calcPolygon(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.POLYGON + ""))) {
			return result;
		}

		result.add(calcSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR)));
		result.subtract(calcSearchListAbstractRingProperty(GraphUtil.findChildrenOfNode(node, GMLRelTypes.INTERIOR)));

		return result;
	}

	public Area3D calcExterior(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.EXTERIOR + ""))) {
			return result;
		}

		result.add(calcSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcLinearRing(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.LINEAR_RING + ""))) {
			return result;
		}

		result.add(calcSearchListPosOrPointPropertyOrPointRep(GraphUtil.findSortedChildrenOfNode(node, GMLRelTypes.CONTROL_POINTS)));

		result.add(calcDirectPositionList(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS_LIST)));

		result.add(calcCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES)));

		result.add(calcSearchListCoord(GraphUtil.findSortedChildrenOfNode(node, GMLRelTypes.COORD)));

		return result;
	}

	public String[] calcPosOrPointPropertyOrPointRep(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.POS_OR_POINT_PROPERTY_OR_POINT_REP + ""))) {
			return result;
		}

		Node tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS);
		if (tmp != null && !tmp.equals(node)) {
			return calcDirectPosition(tmp);
		}

		tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_PROPERTY);
		if (tmp != null && !tmp.equals(node)) {
			return calcSearchPointProperty(tmp);
		}

		tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_REP);
		if (tmp != null && !tmp.equals(node)) {
			return calcPointRep(tmp);
		}

		// TODO parent

		return result;
	}

	public String[] calcDirectPosition(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.DIRECT_POSITION + ""))) {
			return result;
		}

		// TODO parent

		return ((String) node.getProperty(GMLRelTypes.VALUE + "")).split(";");
	}

	public String[] calcPointRep(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.POINT_REP + ""))) {
			return result;
		}

		// TODO parent

		return calcPoint(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
	}

	public String[] calcPointProperty(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.POINT_PROPERTY + ""))) {
			return result;
		}

		// TODO parent

		return calcPoint(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
	}

	public String[] calcPoint(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.POINT + ""))) {
			return result;
		}

		Node tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS);
		if (!tmp.equals(node)) {
			return calcDirectPosition(tmp);
		}

		tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES);
		if (!tmp.equals(node)) {
			return calcCoordinate(tmp);
		}

		tmp = GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORD);
		if (!tmp.equals(node)) {
			return calcCoord(tmp);
		}

		return result;
	}

	public Area3D calcCoordinates(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.COORDINATES + ""))) {
			return result;
		}

		// decimal [0..1] xsd:string Default value is ".".
		// cs [0..1] xsd:string Default value is ",".
		// ts [0..1] xsd:string Default value is " ".

		String cs = (String) node.getProperty("cs");
		String ts = (String) node.getProperty("ts");
		String value = (String) node.getProperty("value");
		int dimension = value.split(ts)[0].split(cs).length;
		value = value.replaceAll(cs, ";").replaceAll(ts, ";");
		result.add(GeometryUtil.createArea(value, dimension, ";", logger, suppressLogger));

		// TODO parent

		return result;
	}

	public String[] calcCoordinate(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label("COORDINATE"))) {
			return result;
		}

		// TODO parent

		// decimal [0..1] xsd:string Default value is ".".
		// cs [0..1] xsd:string Default value is ",".
		// ts [0..1] xsd:string Default value is " ".

		String cs = (String) node.getProperty("cs");
		String ts = (String) node.getProperty("ts");
		String value = (String) node.getProperty("value");
		value = value.replaceAll(cs, ";").replaceAll(ts, ";");

		return value.split(";");
	}

	public String[] calcCoord(Node node) {
		String[] result = null;
		if (node == null || !node.hasLabel(Label.label(GMLClass.COORD + ""))) {
			return result;
		}

		// TODO parent
		String x = (String) node.getProperty("x", null);
		String y = (String) node.getProperty("y", null);
		String z = (String) node.getProperty("z", null);

		if (z == null) {
			return new String[] { x, y };
		}

		return new String[] { x, y, z };
	}

	public Area3D calcDirectPositionList(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.DIRECT_POSITION_LIST + ""))) {
			return result;
		}

		// TODO parent

		int dim = 3;
		if (node.hasProperty("srsDimension")) {
			dim = Integer.parseInt((String) node.getProperty("srsDimension"));
		}

		return GeometryUtil.createArea(
				(String) node.getProperty(GMLRelTypes.VALUE + ""),
				dim,
				";",
				logger, suppressLogger);
	}

	public Area3D calcRing(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.RING + ""))) {
			return result;
		}

		result.add(calcSearchListCurveProperty(GraphUtil.findChildrenOfNode(node, GMLRelTypes.CURVE_MEMBER)));

		return result;
	}

	public ArrayList<String[]> calcCurveProperty(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null || !node.hasLabel(Label.label(GMLClass.RING + ""))) {
			return result;
		}

		result.addAll(calcSearchAbstractCurve(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public ArrayList<String[]> calcCompositeCurve(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null || !node.hasLabel(Label.label(GMLClass.COMPOSITE_CURVE + ""))) {
			return result;
		}

		// TODO

		return result;
	}

	public ArrayList<String[]> calcCurve(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null || !node.hasLabel(Label.label(GMLClass.CURVE + ""))) {
			return result;
		}

		// TODO

		return result;
	}

	public ArrayList<String[]> calcLineString(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null || !node.hasLabel(Label.label(GMLClass.LINE_STRING + ""))) {
			return result;
		}

		// TODO

		return result;
	}

	public ArrayList<String[]> calcOrientableCurve(Node node) {
		ArrayList<String[]> result = new ArrayList<String[]>();
		if (node == null || !node.hasLabel(Label.label(GMLClass.ORIENTABLE_CURVE + ""))) {
			return result;
		}

		// TODO

		return result;
	}

	public Area3D calcInnerBoundaryIs(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.INNER_BOUNDARY_IS + ""))) {
			return result;
		}

		return result;
	}

	public Area3D calcInterior(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.INTERIOR + ""))) {
			return result;
		}

		result.add(calcSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcOuterBoundaryIs(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.OUTER_BOUNDARY_IS + ""))) {
			return result;
		}

		return result;
	}

	/*
	 * BUILDING BOUNDARY SURFACE PROPERTY
	 */

	public Area3D calcSearchAbstractBoundarySurface(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcCeilingSurface(node));
		result.add(calcClosureSurface(node));
		result.add(calcFloorSurface(node));
		result.add(calcGroundSurface(node));
		result.add(calcInteriorWallSurface(node));
		result.add(calcOuterCeilingSurface(node));
		result.add(calcOuterFloorSurface(node));
		result.add(calcRoofSurface(node));
		result.add(calcWallSurface(node));

		return result;
	}

	public Area3D calcBoundarySurfaceProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_BOUNDARY_SURFACE_PROPERTY + ""))) {
			return result;
		}

		result.add(calcSearchAbstractBoundarySurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcCeilingSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_CEILING_SURFACE + ""))) {
			return result;
		}

		// TODO ade

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	// only abstract
	public Area3D calcAbstractBoundarySurface(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE + "")))) {
			return result;
		}

		result.add(calcMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE)));
		result.add(calcMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE)));
		result.add(calcMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE)));
		result.add(calcSearchListOpeningProperty(GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)));

		// TODO ade

		return result;
	}

	public Area3D calcMultiSurfaceProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.MULTI_SURFACE_PROPERTY + ""))) {
			return result;
		}

		result.add(calcMultiSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcMultiSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.MULTI_SURFACE + ""))) {
			return result;
		}

		result.add(calcSearchListSurfaceProperty(GraphUtil.findChildrenOfNode(node, GMLRelTypes.SURFACE_MEMBER)));
		result.add(calcSurfaceArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.SURFACE_MEMBERS)));

		return result;
	}

	public Area3D calcSearchListSurfaceProperty(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		for (Node node : nodeList) {
			result.add(calcSurfaceProperty(node));
		}

		return result;
	}

	public Area3D calcSurfaceProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.SURFACE_PROPERTY + ""))) {
			return result;
		}

		result.add(calcSearchAbstractSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcSearchAbstractSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcCompositeSurface(node));
		result.add(calcOrientableSurface(node, false));
		result.add(calc_TexturedSurface(node));
		result.add(calcPolygon(node));
		result.add(calcSurface(node, false));
		result.add(calcTriangulatedSurface(node, false));
		result.add(calcTin(node));

		return result;
	}

	public Area3D calcCompositeSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.COMPOSITE_SURFACE + ""))) {
			return result;
		}

		result.add(calcSearchListSurfaceProperty(GraphUtil.findChildrenOfNode(node, GMLRelTypes.SURFACE_MEMBER)));

		return result;
	}

	public Area3D calcOrientableSurface(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(GMLClass.ORIENTABLE_SURFACE + "")))) {
			return result;
		}

		result.add(calcSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BASE_SURFACE)));

		// TODO orientation

		return result;
	}

	public Area3D calc_TexturedSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass._TEXTURED_SURFACE + ""))) {
			return result;
		}

		result.add(calcOrientableSurface(node, true));

		// TODO orientation

		return result;
	}

	public Area3D calcSurface(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(GMLClass.SURFACE + "")))) {
			return result;
		}

		result.add(calcSearchSurfacePatchArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.PATCHES)));

		return result;
	}

	public Area3D calcSearchSurfacePatchArrayProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcSurfacePatchArrayProperty(node, false));
		result.add(calcTrianglePatchArrayProperty(node));

		return result;
	}

	public Area3D calcSurfacePatchArrayProperty(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(GMLClass.SURFACE_PATCH_ARRAY_PROPERTY + "")))) {
			return result;
		}

		result.add(calcSearchAbstractSurfacePatch(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcSearchAbstractSurfacePatch(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcRectangle(node));
		result.add(calcTriangle(node));

		return result;
	}

	public Area3D calcRectangle(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.RECTANGLE + ""))) {
			return result;
		}

		result.add(calcSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR)));

		// TODO interpolation

		// TODO parent

		return result;
	}

	public Area3D calcTriangle(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.TRIANGLE + ""))) {
			return result;
		}

		result.add(calcSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR)));

		// TODO interpolation

		// TODO parent

		return result;
	}

	public Area3D calcTrianglePatchArrayProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.TRIANGLE_PATCH_ARRAY_PROPERTY + ""))) {
			return result;
		}

		result.add(calcSurfacePatchArrayProperty(node, true));

		return result;
	}

	public Area3D calcTriangulatedSurface(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(GMLClass.TRIANGULATED_SURFACE + "")))) {
			return result;
		}

		result.add(calcSurface(node, true));

		return result;
	}

	public Area3D calcTin(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.TIN + ""))) {
			return result;
		}

		// TODO stopLines

		// TODO breakLines

		// TODO maxLength

		// TODO controlPoints

		result.add(calcTriangulatedSurface(node, true));

		return result;
	}

	public Area3D calcSurfaceArrayProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(GMLClass.SURFACE_ARRAY_PROPERTY + ""))) {
			return result;
		}

		result.add(calcSearchAbstractSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcSearchListOpeningProperty(ArrayList<Node> nodeList) {
		Area3D result = new Area3D();
		if (nodeList == null) {
			return result;
		}

		for (Node node : nodeList) {
			result.add(calcOpeningProperty(node));
		}

		return result;
	}

	public Area3D calcOpeningProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_OPENING_PROPERTY + ""))) {
			return result;
		}

		result.add(calcSearchAbstractOpening(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcSearchAbstractOpening(Node node) {
		Area3D result = new Area3D();
		if (node == null) {
			return result;
		}

		result.add(calcDoor(node));
		result.add(calcWindow(node));

		return result;
	}

	public Area3D calcDoor(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_DOOR + ""))) {
			return result;
		}

		// TODO ade

		result.add(calcAbstractOpening(node, true));

		return result;
	}

	public Area3D calcAbstractOpening(Node node, boolean forwardedFromChild) {
		Area3D result = new Area3D();
		if (!forwardedFromChild && (node == null || !node.hasLabel(Label.label(CityGMLClass.ABSTRACT_BUILDING_OPENING + "")))) {
			return result;
		}

		result.add(calcMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE)));
		result.add(calcMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE)));
		result.add(calcImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION)));
		result.add(calcImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION)));

		// TODO ade

		return result;
	}

	public Area3D calcImplicitRepresentationProperty(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.IMPLICIT_REPRESENTATION_PROPERTY + ""))) {
			return result;
		}

		result.add(calcImplicitGeometry(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT)));

		return result;
	}

	public Area3D calcImplicitGeometry(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.IMPLICIT_GEOMETRY + ""))) {
			return result;
		}

		// TODO

		return result;
	}

	public Area3D calcWindow(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_WINDOW + ""))) {
			return result;
		}

		result.add(calcAbstractOpening(node, true));

		return result;
	}

	public Area3D calcClosureSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_CLOSURE_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcFloorSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_FLOOR_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcGroundSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_GROUND_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcInteriorWallSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcOuterCeilingSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcOuterFloorSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcRoofSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_ROOF_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

	public Area3D calcWallSurface(Node node) {
		Area3D result = new Area3D();
		if (node == null || !node.hasLabel(Label.label(CityGMLClass.BUILDING_WALL_SURFACE + ""))) {
			return result;
		}

		result.add(calcAbstractBoundarySurface(node, true));

		return result;
	}

}
