package editor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.xml.sax.SAXException;

import mapper.EnumClasses.GMLRelTypes;
import matcher.Matcher.DeletePropertyNodeProperties;
import matcher.Matcher.EditOperators;
import matcher.Matcher.EditRelTypes;
import matcher.Matcher.InsertPropertyNodeProperties;
import matcher.Matcher.InsertRelationshipNodeProperties;
import matcher.Matcher.UpdatePropertyNodeProperties;
import util.ClientUtil;
import util.GraphUtil;
import util.SETTINGS;
import util.StAXUtil;
import util.StAXUtil.Namespaces;
import util.StAXUtil.Prefixes;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class Editor {

	private GraphDatabaseService graphDb;
	private Logger logger;

	private String oldFilename;
	private String newFilename;

	private String wfsServerUrl;

	private final String SERVICE = "service=\"WFS\"";
	private final String VERSION = "version=\"2.0.0\"";

	public Editor(GraphDatabaseService graphDb, Logger logger, String oldFilename, String newFilename, String wfsServerUrl) {
		this.graphDb = graphDb;
		this.logger = logger;
		this.oldFilename = oldFilename;
		this.newFilename = newFilename;
		this.wfsServerUrl = wfsServerUrl;
	}

	// TODO for cases such as CLAZZ has an element CODE in Neo4j but is an element in CityGML
	public StringBuilder requestUpdateProperty(Node opNode) {
		StringBuilder request = new StringBuilder();

		try (Transaction tx = graphDb.beginTx()) {
			request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			request.append("<wfs:Transaction " + SERVICE + " " + VERSION + " "
					+ Namespaces.FES + " " + Namespaces.WFS + " " + Namespaces.GML + " " + Namespaces.BLDG + ">\n");
			request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
			request.append("\t\t<wfs:Property>\n");

			String propertyName = opNode.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString();
			if (propertyName.equals("VALUE")) {
				// not an attribute, but an element such as posList
				request.append("\t\t\t<wfs:ValueReference>\n"
						+ "\t\t\t\t//*[@id='" + opNode.getProperty(UpdatePropertyNodeProperties.OLD_NEAREST_ID.toString()).toString()
						+ "']//*[text()[contains(.,'" + opNode.getProperty(UpdatePropertyNodeProperties.OLD_VALUE.toString()).toString().replaceAll(";", " ") + "')]]\n"
						+ "\t\t\t</wfs:ValueReference>\n");
				request.append("\t\t\t<wfs:Value>\n"
						+ "\t\t\t\t" + opNode.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString().replaceAll(";", " ") + "\n"
						+ "\t\t\t</wfs:Value>\n");
			} else {
				// attribute
				request.append("\t\t\t<wfs:ValueReference>\n"
						+ "\t\t\t\t//*[@id='" + opNode.getProperty(UpdatePropertyNodeProperties.OLD_NEAREST_ID.toString()).toString()
						+ "']//@*[contains(name(),'" + opNode.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString() + "')]\n"
						+ "\t\t\t</wfs:ValueReference>\n");
				request.append("\t\t\t<wfs:Value>\n"
						+ "\t\t\t\t" + opNode.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString() + "\n"
						+ "\t\t\t</wfs:Value>\n");
			}

			request.append("\t\t</wfs:Property>\n");
			request.append("\t\t<wfs:Filter>\n");
			request.append("\t\t\t<wfs:ResourceId rid=\"" + opNode.getProperty(UpdatePropertyNodeProperties.OLD_BUILDING_ID.toString()).toString() + "\">\n");
			request.append("\t\t</wfs:Filter>\n");
			request.append("\t</wfs:Update>\n");
			request.append("</wfs:Transaction>\n");

			tx.success();
		}

		return request;
	}

	public StringBuilder requestUpdateGenericProperty(Node opNode) {
		StringBuilder request = new StringBuilder();

		// TODO

		return request;
	}

	// replace the whole old content with the new one based on gmlid
	public StringBuilder requestUpdateNodeWithId(String oldNearestElementId, String newNearestElementId, String oldBuildingId) throws FileNotFoundException, XMLStreamException {
		StringBuilder request = new StringBuilder();

		try (Transaction tx = graphDb.beginTx()) {
			request.append("-------------------------------------------------------------------------------------------\n");
			request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			request.append("<wfs:Transaction "
					+ SERVICE + " "
					+ VERSION + " "
					+ Namespaces.DEFAULT + " "
					+ Namespaces.FES + " "
					+ Namespaces.WFS + " "
					+ Namespaces.GML + " "
					+ Namespaces.BLDG + " "
					+ Namespaces.GEN + " "
					+ Namespaces.CORE + " "
					+ Namespaces.XSI + " "
					+ Namespaces.XAL + " "
					+ Namespaces.SCHEMA_LOCATION + ">\n");
			request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
			request.append("\t\t<wfs:Property>\n");

			request.append("\t\t\t<wfs:ValueReference>\n"
					+ "\t\t\t\t//*[@gml:id='" + oldNearestElementId + "']\n"
					+ "\t\t\t</wfs:ValueReference>\n");
			request.append("\t\t\t<wfs:Value>\n");
			request.append(StAXUtil.formatContents(
					// TODO if new file has different id then OLD_NEAREST_ID?
					StAXUtil.extractAsXmlContent(this.newFilename, newNearestElementId),
					"\t\t\t"));
			request.append("\t\t\t</wfs:Value>\n");

			request.append("\t\t</wfs:Property>\n");
			request.append("\t\t<fes:Filter>\n");
			request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
			request.append("\t\t</fes:Filter>\n");
			request.append("\t</wfs:Update>\n");
			request.append("</wfs:Transaction>\n");
			request.append("-------------------------------------------------------------------------------------------\n");

			tx.success();
		}

		return request;
	}

	/*
	 * Update function
	 */
	public void executeUpdate(Node mapperRootNode) throws InterruptedException {
		try (Transaction tx = graphDb.beginTx()) {
			logger.info("\n---------------------------------------\n"
					+ "HTTP-POST CONTENTS FOR WFS-TRANSACTIONS"
					+ "\n---------------------------------------");

			// create a fixed thread pool
			int nThreads = Runtime.getRuntime().availableProcessors() * 2;

			// logger.info("... setting up thread pool with " + nThreads + " threads ...");

			ExecutorService service = Executors.newFixedThreadPool(nThreads);

			// get buildings from the old city model
			for (Node oldBuilding : GraphUtil.findBuildings(mapperRootNode.getRelationships(Direction.OUTGOING, GMLRelTypes.OLD_CITY_MODEL).iterator().next().getOtherNode(mapperRootNode))) {
				service.execute(new Runnable() {
					@Override
					public void run() {
						try (Transaction tx = graphDb.beginTx()) {
							String oldBuildingId = oldBuilding.getProperty("id").toString();

							// first element [old, new (optional)] is building's ID, others are nearest ID's that belong to this building
							// if an element is null -> use only building and none else
							// if none is null -> use elements with index starting from 1 (ie exclusive building's ID)
							ArrayList<String> foundIds = new ArrayList<String>();
							foundIds.add(oldBuildingId);

							findNearestEditor(foundIds, oldBuilding, oldBuildingId);

							// System.out.println(foundIds.toString());

							// foundIds.get(0) has the new building's ID
							// oldBuildingId is the old ID
							for (String s : foundIds) {
								if (s == null) {
									String tmp = foundIds.get(0);
									logger.info("\n" + requestUpdateNodeWithId(oldBuildingId, tmp.contains(";") ? tmp.split(";")[1] : tmp, oldBuildingId).toString());
									return;
								}
							}

							for (int i = 1; i < foundIds.size(); i++) {
								String tmp = foundIds.get(i);
								boolean containsNewValue = tmp.contains(";");
								logger.info("\n" + requestUpdateNodeWithId(containsNewValue ? tmp.split(";")[0] : tmp, containsNewValue ? tmp.split(";")[1] : tmp, oldBuildingId).toString());
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

			// wait for all threads to finish
			// logger.info("... shutting down threadpool ...");
			service.shutdown();
			service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);

			tx.success();
		}
	}

	// find the nearest element (with id) (OUTGOING direction) to the given node that contains at least one edit operation
	public void findNearestEditor(ArrayList<String> foundIds, Node curNode, String nearestId) throws FileNotFoundException, XMLStreamException {
		if (curNode.hasProperty("id")) {
			nearestId = curNode.getProperty("id").toString();
		}

		if (GraphUtil.isAttachedWithNonOptionalEditor(curNode)) {
			// if the current ID was also changed in the new model -> remember this id
			for (Node editorNode : GraphUtil.getNonOptionalAttachedEditors(curNode)) {
				if (editorNode.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))
						&& editorNode.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("id")) {

					nearestId += ";" + editorNode.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString();

					if (curNode.hasLabel(Label.label(CityGMLClass.BUILDING + ""))) {
						foundIds.set(0, nearestId);
					}

					break;
				}
			}

			if (nearestId.equals(foundIds.get(0))) {
				foundIds.add(null);
				return;
			}

			if (foundIds.size() == 1) {
				foundIds.add(nearestId);
				return;
			}

			for (int i = 1; i < foundIds.size(); i++) {
				if (foundIds.get(i) == null) {
					continue;
				}

				switch (StAXUtil.areSamePath(oldFilename, foundIds.get(i), nearestId)) {
				case -1:
					// ele2 --..--> ele1
					foundIds.set(i, nearestId);
					return;

				case 0:
					// ele1 ~ ele2
					return;

				case 1:
					// ele1 --..--> ele2
					return;

				default:
					// not on the same path
					break;
				}

				// not on the same path
				foundIds.add(nearestId);
				return;
			}
		}

		for (Node child : GraphUtil.findChildrenOfNode(curNode)) {
			findNearestEditor(foundIds, child, nearestId);
		}
	}

	/**
	 * 
	 * @param mapperRootNode
	 * @throws InterruptedException
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public void executeEditors(Node mapperRootNode, Node matcherRootNode) throws InterruptedException, ClientProtocolException, IOException, XMLStreamException {
		try (Transaction tx = graphDb.beginTx()) {
			logger.info("\n---------------------------------------\n"
					+ "HTTP-POST CONTENTS FOR WFS-TRANSACTIONS"
					+ "\n---------------------------------------");

			logger.info("Execute optional transactions: " + SETTINGS.EXECUTE_OPTIONAL);

			// ----------------------------------------------------------------------
			// insert new buildings if available
			// create a fixed thread pool
			int nThreads = Runtime.getRuntime().availableProcessors() * 2;
			logger.info("... setting up thread pool with " + nThreads + " threads ...");
			ExecutorService service = Executors.newFixedThreadPool(nThreads);

			// get buildings from the new city model
			ArrayList<Node> newBuildings = GraphUtil.findBuildings(mapperRootNode.getRelationships(Direction.OUTGOING, GMLRelTypes.NEW_CITY_MODEL).iterator().next().getOtherNode(mapperRootNode));
			for (Node newBuilding : newBuildings) {
				service.execute(new Runnable() {
					@Override
					public void run() {
						try (Transaction tx = graphDb.beginTx()) {
							ArrayList<Node> attachedInsertEditors = GraphUtil.getNonOptionalAttachedEditors(newBuilding, Label.label(EditOperators.INSERT_NODE + ""));
							if (attachedInsertEditors == null || attachedInsertEditors.isEmpty()) {
								// CITY_OBJECT_MEMBER
								attachedInsertEditors = GraphUtil.getNonOptionalAttachedEditors(
										GraphUtil.findFirstParentOfNode(newBuilding, GMLRelTypes.OBJECT), Label.label(EditOperators.INSERT_NODE + ""));
							}

							if (!attachedInsertEditors.isEmpty()) {

								// TODO if this new building has different XLinks

								insertBuilding(newBuilding.getProperty("id").toString());
							}

							tx.success();
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
			}

			// wait for all threads to finish
			// logger.info("... shutting down threadpool ...");
			service.shutdown();
			service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);

			// ----------------------------------------------------------------------
			// create a fixed thread pool
			service = Executors.newFixedThreadPool(nThreads);

			// get buildings from the old city model
			ArrayList<Node> buildings = GraphUtil.findBuildings(mapperRootNode.getRelationships(Direction.OUTGOING, GMLRelTypes.OLD_CITY_MODEL).iterator().next().getOtherNode(mapperRootNode));
			for (Node oldBuilding : buildings) {
				service.execute(new Runnable() {

					@Override
					public void run() {
						try (Transaction tx = graphDb.beginTx()) {
							executeBuildingEditors(oldBuilding);
							tx.success();
						} catch (ClientProtocolException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				});
			}

			// wait for all threads to finish
			// logger.info("... shutting down threadpool ...");
			service.shutdown();
			service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);

			tx.success();
		}
	}

	public void executeBuildingEditors(Node oldBuilding) throws ClientProtocolException, IOException, XMLStreamException {
		if (!GraphUtil.getNonOptionalAttachedEditors(oldBuilding, Label.label(EditOperators.DELETE_NODE + "")).isEmpty()
				|| !GraphUtil.getNonOptionalAttachedEditors( // CITY_OBJECT_MEMBER
						GraphUtil.findFirstParentOfNode(oldBuilding, GMLRelTypes.OBJECT), Label.label(EditOperators.DELETE_NODE + "")).isEmpty()) {
			deleteBuilding(oldBuilding.getProperty("id").toString());

			// TODO if this building has elements who are referenced to by other buildings

			return;
		}

		// check if building ID has been changed
		String oldBuildingId = oldBuilding.getProperty("id").toString();
		String newBuildingId = null;
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(oldBuilding, Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
			if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("id")) {
				newBuildingId = editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString();
				break;
			}
		}

		// check if complex elements are to be updated -> replace building
		for (Relationship rel : oldBuilding.getRelationships(Direction.OUTGOING)) {
			if (!GraphUtil.isSimpleBuildingElement(oldBuilding, rel.getType().toString())
					&& GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
				// replace old building with the new one
				updateBuilding(oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId);
				return;
			}
		}

		// check if complex elements are to be inserted to building -> replace building
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(oldBuilding, Label.label(EditOperators.INSERT_NODE + ""))) {
			if (!GraphUtil.isSimpleBuildingElement(oldBuilding, editor.getProperty(InsertRelationshipNodeProperties.RELATIONSHIP_TYPE.toString()).toString())) {
				// replace old building with the new one
				updateBuilding(oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId);
				return;
			}
		}

		// Solid
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD1_SOLID, "lod1Solid");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD2_SOLID, "lod2Solid");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD3_SOLID, "lod3Solid");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD4_SOLID, "lod4Solid");

		// MultiCurve
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD1_TERRAIN_INTERSECTION, "lod1TerrainIntersection");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD2_TERRAIN_INTERSECTION, "lod2TerrainIntersection");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD3_TERRAIN_INTERSECTION, "lod3TerrainIntersection");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD4_TERRAIN_INTERSECTION, "lod4TerrainIntersection");

		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD2_MULTI_CURVE, "lod2MultiCurve");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD3_MULTI_CURVE, "lod3MultiCurve");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD4_MULTI_CURVE, "lod4MultiCurve");

		// MultiSurface
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD0_FOOT_PRINT, "lod0FootPrint");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD0_ROOF_EDGE, "lod0RoofEdge");

		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD1_MULTI_SURFACE, "lod1MultiSurface");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD2_MULTI_SURFACE, "lod2MultiSurface");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD3_MULTI_SURFACE, "lod3MultiSurface");
		executeEditorBuildingGeometryProperty(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.LOD4_MULTI_SURFACE, "lod4MultiSurface");

		// RelativeToTerrain & RelativeToWater
		executeEditorRelativeToTerrainOrWater(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.RELATIVE_TO_TERRAIN, "relativeToTerrain");
		executeEditorRelativeToTerrainOrWater(oldBuilding, oldBuildingId, newBuildingId, GMLRelTypes.RELATIVE_TO_WATER, "relativeToWater");

		// simple building attributes
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(oldBuilding)) {
			if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
				String propertyName = editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString();
				String newValue = editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString();

				if (propertyName.equals("yearOfConstruction")
						|| propertyName.equals("yearOfDemolition")
						|| propertyName.equals("storeysAboveGround")
						|| propertyName.equals("storeysBelowGround")) {
					updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), propertyName, newValue);
				} else if (propertyName.equals("creationDate")
						|| propertyName.equals("terminationDate")) {
					updateBuildingProperty(oldBuildingId, Prefixes.CORE.toString(), propertyName, newValue);
				}
			} else if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
				String propertyName = editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString();

				if (propertyName.equals("yearOfConstruction")
						|| propertyName.equals("yearOfDemolition")
						|| propertyName.equals("storeysAboveGround")
						|| propertyName.equals("storeysBelowGround")) {
					deleteBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), propertyName);
				} else if (propertyName.equals("creationDate")
						|| propertyName.equals("terminationDate")) {
					deleteBuildingProperty(oldBuildingId, Prefixes.CORE.toString(), propertyName);
				}
			} else if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
				String propertyName = editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString();
				String newValue = editor.getProperty(InsertPropertyNodeProperties.NEW_VALUE.toString()).toString();

				if (propertyName.equals("yearOfConstruction")
						|| propertyName.equals("yearOfDemolition")
						|| propertyName.equals("storeysAboveGround")
						|| propertyName.equals("storeysBelowGround")) {
					insertBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), propertyName, newValue);
				} else if (propertyName.equals("creationDate")
						|| propertyName.equals("terminationDate")) {
					insertBuildingProperty(oldBuildingId, Prefixes.CORE.toString(), propertyName, newValue);
				}
			}
		}

		// insert simple properties to building
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(oldBuilding, Label.label(EditOperators.INSERT_NODE + ""))) {
			String relToInsert = editor.getProperty(InsertRelationshipNodeProperties.RELATIONSHIP_TYPE.toString()).toString();

			boolean functionEdited = false;
			boolean usageEdited = false;
			boolean nameEdited = false;

			if (relToInsert.equals(GMLRelTypes.CLAZZ + "")) {
				String newValue = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE).getProperty("value").toString();
				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "class", newValue);
			} else if (relToInsert.equals(GMLRelTypes.FUNCTION + "") && !functionEdited) {
				StringBuilder functions = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "function",
						Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "function", functions.toString());

				functionEdited = true;
			} else if (relToInsert.equals(GMLRelTypes.USAGE + "") && !usageEdited) {
				StringBuilder usages = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "usage",
						Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "usage", usages.toString());

				usageEdited = true;
			} else if (relToInsert.equals(GMLRelTypes.ROOF_TYPE + "")) {
				String newValue = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE).getProperty("value").toString();
				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "roofType", newValue);
			} else if (relToInsert.equals(GMLRelTypes.MEASURED_HEIGHT + "")) {
				Node nodeToInsert = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE);
				String newPropertyValue = nodeToInsert.getProperty("value").toString();

				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", newPropertyValue);

				if (nodeToInsert.hasProperty("uom")) {
					String newAttributeValue = nodeToInsert.getProperty("uom").toString();
					updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", Prefixes.GML.toString(), "uom", newAttributeValue);
				}
			} else if (relToInsert.equals(GMLRelTypes.STOREY_HEIGHTS_ABOVE_GROUND + "")) {
				Node nodeToInsert = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE);
				StringBuilder newPropertyValue = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "storeyHeightsAboveGround",
						Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);

				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", newPropertyValue.toString());

				if (nodeToInsert.hasProperty("uom")) {
					String newAttributeValue = nodeToInsert.getProperty("uom").toString();
					updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", Prefixes.BLDG.toString(), "uom", newAttributeValue);
				}
			} else if (relToInsert.equals(GMLRelTypes.STOREY_HEIGHTS_BELOW_GROUND + "")) {
				Node nodeToInsert = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE);
				StringBuilder newPropertyValue = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "storeyHeightsBelowGround",
						Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);

				updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", newPropertyValue.toString());

				if (nodeToInsert.hasProperty("uom")) {
					String newAttributeValue = nodeToInsert.getProperty("uom").toString();
					updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", Prefixes.BLDG.toString(), "uom", newAttributeValue);
				}
			} else if (relToInsert.equals(GMLRelTypes.DESCRIPTION + "")) {
				Node nodeToInsert = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE);
				String newPropertyValue = nodeToInsert.getProperty("value").toString();

				updateBuildingProperty(oldBuildingId, Prefixes.GML.toString(), "description", newPropertyValue);

				// TODO XLinkActuate, XLinkShow object in StringOrRef class

				Iterator<Entry<String, Object>> it = nodeToInsert.getAllProperties().entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> pair = (Map.Entry<String, Object>) it.next();

					if (pair.getKey().equals("value")) {
						continue;
					}

					updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), pair.getKey(), pair.getValue().toString());
				}
			} else if (relToInsert.equals(GMLRelTypes.NAME + "") && !nameEdited) {
				StringBuilder names = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.GML.toString(), "name",
						Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
				updateBuildingProperty(oldBuildingId, Prefixes.GML.toString(), "name", names.toString());

				nameEdited = true;
			} else if (relToInsert.equals(GMLRelTypes.EXTERNAL_REFERENCE + "")) {
				insertBuildingExternalReference(Prefixes.BLDG.toString(), oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId);
			} else if (relToInsert.equals(GMLRelTypes.GENERIC_ATTRIBUTE + "")) {
				Node nodeToInsert = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE);
				insertGenericsBuildingProperty(Prefixes.BLDG.toString(), oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId,
						getGenericsPropertyName(nodeToInsert.getLabels().iterator().next()), nodeToInsert.getProperty("name").toString());
			}
		}

		// simple editors
		for (Relationship rel : oldBuilding.getRelationships(Direction.OUTGOING)) {
			boolean functionEdited = false;
			boolean usageEdited = false;
			boolean nameEdited = false;

			if (rel.getType().toString().equals(GMLRelTypes.CLAZZ + "")) {
				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) { // actually only one loop
					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))
							&& editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("value")) {
						updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "class", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
						updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "class", "");
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.FUNCTION + "") && !functionEdited) {
				// replace function property with the new one
				if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
					StringBuilder functions = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "function", Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
					updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "function", functions.toString());
				}

				// multiple relationships FUNCTION can exist within a building -> only edit once
				functionEdited = true;
			} else if (rel.getType().toString().equals(GMLRelTypes.USAGE + "") && !usageEdited) {
				// replace usage property with the new one
				if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
					StringBuilder usages = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "usage", Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
					updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "usage", usages.toString());
				}

				// multiple relationships FUNCTION can exist within a building -> only edit once
				usageEdited = true;
			} else if (rel.getType().toString().equals(GMLRelTypes.ROOF_TYPE + "")) {
				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) {
					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))
							&& editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("value")) {
						updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "roofType", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
						updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "roofType", "");
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.MEASURED_HEIGHT + "")) {
				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) {
					if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
						updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", "");
						break;
					}

					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
						if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("value")) {
							updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", Prefixes.GML.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
						if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", Prefixes.GML.toString(), "uom", "");
						}
					} else if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
						if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "measuredHeight", Prefixes.GML.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.STOREY_HEIGHTS_ABOVE_GROUND + "")) {
				// replace function property with the new one
				if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
					StringBuilder functions = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "storeyHeightsAboveGround",
							Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
					updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", functions.toString());
				}

				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) {
					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
						if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", Prefixes.BLDG.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
						if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", Prefixes.BLDG.toString(), "uom", "");
						}
					} else if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
						if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsAboveGround", Prefixes.BLDG.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.STOREY_HEIGHTS_BELOW_GROUND + "")) {
				// replace function property with the new one
				if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
					StringBuilder functions = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.BLDG.toString(), "storeyHeightsBelowGround",
							Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
					updateBuildingProperty(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", functions.toString());
				}

				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) {
					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
						if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", Prefixes.BLDG.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
						if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", Prefixes.BLDG.toString(), "uom", "");
						}
					} else if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
						if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("uom")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.BLDG.toString(), "storeyHeightsBelowGround", Prefixes.BLDG.toString(), "uom", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.DESCRIPTION + "")) {
				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding))) {
					if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
						updateBuildingProperty(oldBuildingId, Prefixes.GML.toString(), "description", "");
						break;
					}

					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
						if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("value")) {
							updateBuildingProperty(oldBuildingId, Prefixes.GML.toString(), "description", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("arcrole")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "arcrole", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("href")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "href", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("remoteSchema")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "remoteSchema", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("role")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "role", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("title")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "title", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					} else if (editor.hasLabel(Label.label(EditOperators.INSERT_PROPERTY + ""))) {
						if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("arcrole")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "arcrole", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("href")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "href", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("remoteSchema")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "remoteSchema", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("role")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "role", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(InsertPropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("title")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "title", editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						}
					} else if (editor.hasLabel(Label.label(EditOperators.DELETE_PROPERTY + ""))) {
						if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("arcrole")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "arcrole", "");
						} else if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("href")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "href", "");
						} else if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("remoteSchema")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "remoteSchema", "");
						} else if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("role")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "role", "");
						} else if (editor.getProperty(DeletePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("title")) {
							updateBuildingPropertyOrAttribute(oldBuildingId, Prefixes.GML.toString(), "description", Prefixes.GML.toString(), "title", "");
						}
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.NAME + "") && !nameEdited) {
				// replace property with the new one
				if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
					StringBuilder functions = StAXUtil.extractPropertyValueOfBuildingWithoutTags(newFilename, Prefixes.GML.toString(), "name", Prefixes.BLDG.toString(), newBuildingId == null ? oldBuildingId : newBuildingId);
					updateBuildingProperty(oldBuildingId, Prefixes.GML.toString(), "name", functions.toString());
				}

				// multiple relationships FUNCTION can exist within a building -> only edit once
				nameEdited = true;
			} else if (rel.getType().toString().equals(GMLRelTypes.EXTERNAL_REFERENCE + "")) {
				if (!GraphUtil.getNonOptionalAttachedEditors(rel.getOtherNode(oldBuilding), Label.label(EditOperators.DELETE_NODE + "")).isEmpty()) {
					// delete
					deleteBuildingExternalReference(oldBuildingId);
				} else {
					// replace property with the new one
					if (GraphUtil.hasNonOptionalEditors(rel.getOtherNode(oldBuilding))) {
						deleteBuildingExternalReference(oldBuildingId);
						insertBuildingExternalReference(Prefixes.BLDG.toString(), oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId);
					}
				}
			} else if (rel.getType().toString().equals(GMLRelTypes.GENERIC_ATTRIBUTE + "")) {
				Node genericNode = rel.getOtherNode(oldBuilding);

				Label saveLabel = null;
				String saveName = null;
				String saveValue = null;

				for (Node editor : GraphUtil.getNonOptionalAttachedEditors(genericNode)) {
					if (editor.hasLabel(Label.label(EditOperators.DELETE_NODE + ""))) {
						Node nodeToDelete = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.OLD_NODE);
						deleteGenericsBuildingProperty(oldBuildingId, nodeToDelete.getLabels().iterator().next(), nodeToDelete.getProperty("name").toString());
						break;
					}

					// replace content for measureAttribute and genericAttributeSet
					if ((genericNode.hasLabel(Label.label(CityGMLClass.MEASURE_ATTRIBUTE + ""))
							|| genericNode.hasLabel(Label.label(CityGMLClass.GENERIC_ATTRIBUTE_SET + "")))
							&& GraphUtil.hasNonOptionalEditors(genericNode)) {
						deleteGenericsBuildingProperty(oldBuildingId, genericNode.getLabels().iterator().next(), genericNode.getProperty("name").toString());

						// check if name is changed
						String newName = genericNode.getProperty("name").toString();
						for (Node tmpEditor : GraphUtil.getNonOptionalAttachedEditors(genericNode, Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
							if (tmpEditor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).equals("name")) {
								newName = tmpEditor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString();
								break;
							}
						}

						insertGenericsBuildingProperty(
								Prefixes.BLDG.toString(), oldBuildingId, newBuildingId == null ? oldBuildingId : newBuildingId,
								getGenericsPropertyName(genericNode.getLabels().iterator().next()), newName);
						break;
					}

					if (editor.hasLabel(Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
						Node nodeToUpdate = GraphUtil.findFirstChildOfNode(editor, EditRelTypes.OLD_NODE);
						if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("value")) {
							updateGenericsBuildingPropertyOrAttribute(
									oldBuildingId, nodeToUpdate.getLabels().iterator().next(),
									nodeToUpdate.getProperty("name").toString(), null,
									editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
						} else if (editor.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME.toString()).toString().equals("name")) {
							saveLabel = nodeToUpdate.getLabels().iterator().next();
							saveName = nodeToUpdate.getProperty("name").toString();
							saveValue = editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString();
						}
					}
				}

				// names of generic attributes define them -> change these at the end
				if (saveName != null) {
					updateGenericsBuildingPropertyOrAttribute(
							oldBuildingId, saveLabel,
							saveName, "name",
							saveValue);
				}
			}
		}

		// change ID (if needed) after all attributes have been updated !
		if (newBuildingId != null) {
			updateBuildingAttribute(oldBuildingId, Prefixes.GML.toString(), "id", newBuildingId);
		}
	}

	public void updateBuildingProperty(String oldBuildingId, String propertyPrefix, String propertyName, String newValue) throws ClientProtocolException, IOException {
		updateBuildingPropertyOrAttribute(oldBuildingId, propertyPrefix, propertyName, null, null, newValue);
	}

	// delete = update an empty value for an element
	public void deleteBuildingProperty(String oldBuildingId, String prefix, String propertyName) throws ClientProtocolException, IOException {
		updateBuildingProperty(oldBuildingId, prefix, propertyName, "");
	}

	// insert = update a not yet existing element
	public void insertBuildingProperty(String oldBuildingId, String prefix, String propertyName, String newValue) throws ClientProtocolException, IOException {
		updateBuildingProperty(oldBuildingId, prefix, propertyName, newValue);
	}

	public void updateBuildingAttribute(String oldBuildingId, String attributePrefix, String attributeName, String newValue) throws ClientProtocolException, IOException {
		updateBuildingPropertyOrAttribute(oldBuildingId, null, null, attributePrefix, attributeName, newValue);
	}

	// delete = update an empty value for an element
	public void deleteBuildingAttribute(String oldBuildingId, String attributePrefix, String attributeName, String newValue) throws ClientProtocolException, IOException {
		updateBuildingAttribute(oldBuildingId, attributePrefix, attributeName, "");
	}

	// insert = update a not yet existing element
	public void insertBuildingAttribute(String oldBuildingId, String attributePrefix, String attributeName, String newValue) throws ClientProtocolException, IOException {
		updateBuildingAttribute(oldBuildingId, attributePrefix, attributeName, newValue);
	}

	/**
	 * Update a simple building property (eg. measuredHeight) or attribute (eg. uom of measuredHeight).
	 * 
	 * @param oldBuildingId
	 * @param propertyPrefix
	 *            null if only building attribute (eg. ID) is to be updated
	 * @param propertyName
	 *            null if only building attribute (eg. ID) is to be updated
	 * @param attributePrefix
	 *            null if only building property (eg. yearOrCreation) is to be updated
	 * @param attributeName
	 *            null if only building property (eg. yearOrCreation) is to be updated
	 * @param newValue
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void updateBuildingPropertyOrAttribute(String oldBuildingId, String propertyPrefix, String propertyName, String attributePrefix, String attributeName, String newValue) throws ClientProtocolException, IOException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference>\n");

		if (propertyPrefix == null || propertyName == null) {
			request.append("\t\t\t\t@" + attributePrefix + ":" + attributeName + "\n");
		} else {
			if (attributePrefix == null || attributeName == null) {
				request.append("\t\t\t\t" + (propertyPrefix.equals("") ? "" : propertyPrefix + ":") + propertyName + "\n");
			} else {
				request.append("\t\t\t\t" + (propertyPrefix.equals("") ? "" : propertyPrefix + ":") + propertyName
						+ "/@" + (attributePrefix.equals("") ? "" : attributePrefix + ":") + attributeName + "\n");
			}
		}

		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t\t<wfs:Value>" + newValue + "</wfs:Value>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void updateBuilding(String oldBuildingId, String newBuildingId) throws ClientProtocolException, IOException, XMLStreamException {
		deleteBuilding(oldBuildingId);
		insertBuilding(newBuildingId);
	}

	public String getGenericsPropertyName(Label label) {
		if (label.toString().equals(CityGMLClass.DATE_ATTRIBUTE + "")) {
			return "dateAttribute";
		}

		if (label.toString().equals(CityGMLClass.DOUBLE_ATTRIBUTE + "")) {
			return "doubleAttribute";
		}

		if (label.toString().equals(CityGMLClass.GENERIC_ATTRIBUTE_SET + "")) {
			return "genericAttributeSet";
		}

		if (label.toString().equals(CityGMLClass.INT_ATTRIBUTE + "")) {
			return "intAttribute";
		}

		if (label.toString().equals(CityGMLClass.MEASURE_ATTRIBUTE + "")) {
			return "measureAttribute";
		}

		if (label.toString().equals(CityGMLClass.STRING_ATTRIBUTE + "")) {
			return "stringAttribute";
		}

		if (label.toString().equals(CityGMLClass.URI_ATTRIBUTE + "")) {
			return "uriAttribute";
		}

		return null;
	}

	public void updateGenericsBuildingPropertyOrAttribute(String oldBuildingId, Label label, String propertyName, String attributeName, String newValue) throws ClientProtocolException, IOException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference>\n");

		String stringLabel = getGenericsPropertyName(label);
		request.append("\t\t\t\tgen:" + stringLabel + "[@gen:name='" + propertyName + "']");

		// TODO genericAttributeSet

		// <gen:genericAttributeSet name="Base Quantities">
		// <gen:measureAttribute name="Height">
		// <gen:value uom="#m">9.00</gen:value>
		// </gen:measureAttribute>
		// <gen:measureAttribute name="Area">
		// <gen:value uom="#m2">80.00</gen:value>
		// </gen:measureAttribute>
		// <gen:measureAttribute name="Volume">
		// <gen:value uom="#m3">720.00</gen:value>
		// </gen:measureAttribute>
		// </gen:genericAttributeSet>

		if (attributeName != null) {
			request.append("/@gen:" + attributeName + "\n");
		} else {
			request.append("/gen:value\n");
		}

		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t\t<wfs:Value>" + newValue + "</wfs:Value>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void insertGenericsBuildingProperty(String buildingPrefix, String oldBuildingId, String newBuildingId,
			String genType, String genName) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");

		request.append("\t<wfs:Native vendorId=\"VCS\" safeToIgnore=\"false\">\n");
		request.append("\t\t<vcs:InsertComplexProperty typeName=\"bldg:Building\">\n");
		request.append("\t\t\t<vcs:Property>\n");

		request.append("\t\t\t\t<vcs:Value>\n");

		StringBuilder content = StAXUtil.extractXmlContentOfBuildingElement(buildingPrefix, newBuildingId, newFilename,
				Prefixes.GEN.toString(), genType, "", "name", genName);
		request.append(StAXUtil.formatContents(content, "\t\t\t\t\t"));

		request.append("\t\t\t\t</vcs:Value>\n");
		request.append("\t\t\t</vcs:Property>\n");
		request.append("\t\t\t<fes:Filter>\n");
		request.append("\t\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t\t</fes:Filter>\n");
		request.append("\t\t</vcs:InsertComplexProperty>\n");
		request.append("\t</wfs:Native>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void insertBuildingExternalReference(String buildingPrefix, String oldBuildingId, String newBuildingId) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");

		request.append("\t<wfs:Native vendorId=\"VCS\" safeToIgnore=\"false\">\n");
		request.append("\t\t<vcs:InsertComplexProperty typeName=\"bldg:Building\">\n");
		request.append("\t\t\t<vcs:Property>\n");

		request.append("\t\t\t\t<vcs:Value>\n");

		StringBuilder content = StAXUtil.extractXmlContentOfBuildingElement(buildingPrefix, newBuildingId, newFilename,
				Prefixes.CORE.toString(), "externalReference", null, null, null);
		request.append(StAXUtil.formatContents(content, "\t\t\t\t\t").toString()
				.replace("<core:externalReference>", "<vcs:externalReference>")
				.replace("</core:externalReference>", "</vcs:externalReference>"));

		request.append("\t\t\t\t</vcs:Value>\n");
		request.append("\t\t\t</vcs:Property>\n");
		request.append("\t\t\t<fes:Filter>\n");
		request.append("\t\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t\t</fes:Filter>\n");
		request.append("\t\t</vcs:InsertComplexProperty>\n");
		request.append("\t</wfs:Native>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void deleteBuildingExternalReference(String oldBuildingId) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference action=\"remove\">\n");
		request.append("\t\t\t\tcore:externalReference\n");
		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void deleteGenericsBuildingProperty(String oldBuildingId, Label label, String genName) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference action=\"remove\">\n");
		request.append("\t\t\t\tgen:" + getGenericsPropertyName(label) + "[@gen:name='" + genName + "']\n");
		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void insertBuilding(String newBuildingId) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Insert>\n");
		request.append(StAXUtil.formatContents(StAXUtil.extractAsXmlContent(newFilename, newBuildingId), "\t\t"));
		request.append("\t</wfs:Insert>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	public void deleteBuilding(String oldBuildingId) throws ClientProtocolException, IOException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Delete typeName=\"bldg:Building\">\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Delete>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	/*
	 * Building geometry
	 */
	public void executeEditorBuildingGeometryProperty(Node buildingNode, String oldBuildingId, String newBuildingId, RelationshipType solidRelType, String lodXSolid) throws ClientProtocolException, IOException, XMLStreamException {
		String xPath = Prefixes.BLDG.toString() + ":" + lodXSolid;

		// insert geometry
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(buildingNode, Label.label(EditOperators.INSERT_NODE + ""))) {
			if (editor.getProperty(InsertRelationshipNodeProperties.RELATIONSHIP_TYPE.toString()).toString().equals(solidRelType.toString())) {
				updateElement(oldBuildingId, xPath, false,
						StAXUtil.formatContents(
								StAXUtil.extractXmlContentOfBuildingElementWithoutTags(
										Prefixes.BLDG.toString(), newBuildingId, newFilename, Prefixes.BLDG.toString(),
										lodXSolid, null, null, null),
								"\t\t\t\t").toString());
				return;
			}
		}

		Node solidPropertyNode = GraphUtil.findFirstChildOfNode(buildingNode, solidRelType);

		if (solidPropertyNode == null) {
			return;
		}

		// delete geometry
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(solidPropertyNode, Label.label(EditOperators.DELETE_NODE + ""))) {
			updateElement(oldBuildingId, xPath, true, "");
			return;
		}

		// replace geometry
		if (GraphUtil.hasNonOptionalEditors(solidPropertyNode)) {
			updateElement(oldBuildingId, xPath, false,
					StAXUtil.formatContents(
							StAXUtil.extractXmlContentOfBuildingElementWithoutTags(
									Prefixes.BLDG.toString(), newBuildingId, newFilename, Prefixes.BLDG.toString(),
									lodXSolid, null, null, null),
							"\t\t\t\t").toString());
			return;
		}

	}

	public void executeEditorRelativeToTerrainOrWater(Node buildingNode, String oldBuildingId, String newBuildingId, RelationshipType relType, String elementName) throws ClientProtocolException, IOException, XMLStreamException {
		String xPath = Prefixes.CORE.toString() + ":" + elementName;

		// insert element
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(buildingNode, Label.label(EditOperators.INSERT_NODE + ""))) {
			if (editor.getProperty(InsertRelationshipNodeProperties.RELATIONSHIP_TYPE.toString()).toString().equals(relType.toString())) {
				updateSimpleElement(oldBuildingId, xPath, GraphUtil.findFirstChildOfNode(editor, EditRelTypes.NEW_NODE).getProperty("value").toString());
				return;
			}
		}

		Node propertyNode = GraphUtil.findFirstChildOfNode(buildingNode, relType);

		if (propertyNode == null) {
			return;
		}

		// delete element
		for (Node editor : GraphUtil.getNonOptionalAttachedEditors(propertyNode, Label.label(EditOperators.DELETE_NODE + ""))) {
			updateSimpleElement(oldBuildingId, xPath, "");
			return;
		}

		// update element
		if (GraphUtil.hasNonOptionalEditors(propertyNode)) {
			for (Node editor : GraphUtil.getNonOptionalAttachedEditors(propertyNode, Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
				updateSimpleElement(oldBuildingId, xPath, editor.getProperty(UpdatePropertyNodeProperties.NEW_VALUE.toString()).toString());
				return;
			}
		}

	}

	/**
	 * Universal update/delete/insert function for building (geometric) elements.
	 * 
	 * @param oldBuildingId
	 * @param xPath
	 * @param remove
	 *            false if insert/update, then updateValue is mandatory; true if delete, then updateValue is optional
	 * @param updateValue
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public void updateElement(String oldBuildingId, String xPath, boolean remove, String updateValue) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference" + (remove ? " action=\"remove\"" : "") + ">\n");
		request.append("\t\t\t\t" + xPath + "\n");
		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t\t<wfs:Value>\n");
		request.append(updateValue);
		request.append("\t\t\t</wfs:Value>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	/**
	 * Universal update/delete/insert function for building (non-geometric) elements (eg. relativeToTerrain).
	 * 
	 * @param oldBuildingId
	 * @param xPath
	 * @param updateValue
	 *            empty if remove
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public void updateSimpleElement(String oldBuildingId, String xPath, String updateValue) throws ClientProtocolException, IOException, XMLStreamException {
		StringBuilder request = new StringBuilder();
		request.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		request.append("<wfs:Transaction "
				+ SERVICE + " "
				+ VERSION + " "
				+ Namespaces.DEFAULT + " "
				+ Namespaces.FES + " "
				+ Namespaces.FRN + " "
				+ Namespaces.WFS + " "
				+ Namespaces.GML + " "
				+ Namespaces.BLDG + " "
				+ Namespaces.GEN + " "
				+ Namespaces.CORE + " "
				+ Namespaces.XLINK + " "
				+ Namespaces.XSI + " "
				+ Namespaces.XAL + " "
				+ Namespaces.VCS + " "
				+ Namespaces.SCHEMA_LOCATION + ">\n");
		request.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
		request.append("\t\t<wfs:Property>\n");
		request.append("\t\t\t<wfs:ValueReference>\n");
		request.append("\t\t\t\t" + xPath + "\n");
		request.append("\t\t\t</wfs:ValueReference>\n");
		request.append("\t\t\t<wfs:Value>" + updateValue + "</wfs:Value>\n");
		request.append("\t\t</wfs:Property>\n");
		request.append("\t\t<fes:Filter>\n");
		request.append("\t\t\t<fes:ResourceId rid=\"" + oldBuildingId + "\"/>\n");
		request.append("\t\t</fes:Filter>\n");
		request.append("\t</wfs:Update>\n");
		request.append("</wfs:Transaction>\n");

		ClientUtil.sendHttpPost(this.wfsServerUrl, request, logger);
	}

	/*
	 * Simple update for buildings' creationDate
	 */
	public void executeSimpleUpdate(Node matcherRootNode) throws ClientProtocolException, IOException {
		try (Transaction tx = graphDb.beginTx()) {
			for (Node node : GraphUtil.findChildrenOfNode(matcherRootNode, Label.label(EditOperators.UPDATE_PROPERTY + ""))) {
				// xpath //*/text()[normalize-space(.)='text']/parent::*
				StringBuilder xpath = new StringBuilder();
				if (GraphUtil.findFirstChildOfNode(node, EditRelTypes.OLD_NODE).hasLabel(Label.label(CityGMLClass.BUILDING_ROOF_SURFACE + ""))) {
					xpath.append("bldg:boundedBy/bldg:RoofSurface");
					xpath.append("[@gml:id='" + node.getProperty(UpdatePropertyNodeProperties.OLD_NEAREST_ID + "").toString() + "']");
				} else if (GraphUtil.findFirstChildOfNode(node, EditRelTypes.OLD_NODE).hasLabel(Label.label(CityGMLClass.BUILDING_GROUND_SURFACE + ""))) {
					xpath.append("bldg:boundedBy/bldg:GroundSurface");
					xpath.append("[@gml:id='" + node.getProperty(UpdatePropertyNodeProperties.OLD_NEAREST_ID + "").toString() + "']");
				} else if (GraphUtil.findFirstChildOfNode(node, EditRelTypes.OLD_NODE).hasLabel(Label.label(CityGMLClass.BUILDING_WALL_SURFACE + ""))) {
					xpath.append("bldg:boundedBy/bldg:WallSurface");
					xpath.append("[@gml:id='" + node.getProperty(UpdatePropertyNodeProperties.OLD_NEAREST_ID + "").toString() + "']");
				}
				if (xpath.length() != 0) {
					xpath.append("/");
				}
				xpath.append("core:" + node.getProperty(UpdatePropertyNodeProperties.PROPERTY_NAME + "").toString());

				String buildingId = node.getProperty(UpdatePropertyNodeProperties.OLD_BUILDING_ID + "").toString();

				StringBuilder xmlContent = new StringBuilder();
				xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				xmlContent.append("<wfs:Transaction service=\"WFS\" version=\"2.0.0\" \n"
						+ "xmlns=\"http://www.opengis.net/citygml/2.0\" \n"
						+ "xmlns:fes=\"http://www.opengis.net/fes/2.0\" \n"
						+ "xmlns:wfs=\"http://www.opengis.net/wfs/2.0\" \n"
						+ "xmlns:gml=\"http://www.opengis.net/gml\" \n"
						+ "xmlns:bldg=\"http://www.opengis.net/citygml/building/2.0\" \n"
						+ "xmlns:gen=\"http://www.opengis.net/citygml/generics/2.0\" \n"
						+ "xmlns:core=\"http://www.opengis.net/citygml/2.0\" \n"
						+ "xmlns:xal=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\" \n"
						+ "xmlns:schemaLocation=\"http://www.opengis.net/citygml/building/2.0 \n"
						+ "http://schemas.opengis.net/citygml/building/2.0/building.xsd \n"
						+ "http://www.opengis.net/citygml/generics/2.0 \n"
						+ "http://schemas.opengis.net/citygml/generics/2.0/generics.xsd\">\n");
				xmlContent.append("\t<wfs:Update typeName=\"bldg:Building\">\n");
				xmlContent.append("\t\t<wfs:Property>\n");
				xmlContent.append("\t\t\t<wfs:ValueReference>\n");

				xmlContent.append("\t\t\t\t" + xpath + "\n");

				xmlContent.append("\t\t\t" + "</wfs:ValueReference>\n");
				xmlContent.append("\t\t\t" + "<wfs:Value>\n");

				xmlContent.append("\t\t\t\t" + node.getProperty(UpdatePropertyNodeProperties.NEW_VALUE + "").toString().replaceAll(" ", "-") + "\n");

				xmlContent.append("\t\t\t</wfs:Value>\n");
				xmlContent.append("\t\t</wfs:Property>\n");
				xmlContent.append("\t\t<fes:Filter>\n");
				xmlContent.append("\t\t\t<fes:ResourceId rid=\"" + buildingId + "\"/>\n");
				xmlContent.append("\t\t</fes:Filter>\n");
				xmlContent.append("\t</wfs:Update>\n");
				xmlContent.append("</wfs:Transaction>\n");

				ClientUtil.sendHttpPost(this.wfsServerUrl, xmlContent, logger);
			}
			tx.success();
		}
	}

	// old function
	// public void executeUpdateOld(Node rootMatcher) throws InterruptedException {
	// // create a fixed thread pool
	// int nThreads = Runtime.getRuntime().availableProcessors() * 2;
	// logger.info("... setting up thread pool with " + nThreads + " threads ...");
	// ExecutorService service = Executors.newFixedThreadPool(nThreads);
	//
	// // Always write database operations in transactions
	// try (Transaction tx = graphDb.beginTx()) {
	// ArrayList<String> alreadyUpdatedIds = new ArrayList<String>();
	// for (Relationship rel : rootMatcher.getRelationships(Direction.OUTGOING, EditRelTypes.CONSISTS_OF)) {
	// Node node = rel.getOtherNode(rootMatcher);
	//
	// // for all transactions, not onyl update property
	// String id = node.getProperty(InsertPropertyNodeProperties.OLD_NEAREST_ID.toString()).toString();
	// if (alreadyUpdatedIds.contains(id)) {
	// continue;
	// }
	//
	// alreadyUpdatedIds.add(id);
	//
	// service.execute(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// logger.info(requestUpdateNodeWithId(node).toString());
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
	// tx.success();
	// }
	//
	// // wait for all threads to finish
	// // logger.info("... shutting down threadpool ...");
	// service.shutdown();
	// service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);
	// }

	public static void main(String[] args) throws FileNotFoundException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		String sourceFile = "src/main/java/CityGML2GraphDB/editor/Test_Editor.gml";
		String outputFile = "src/main/java/CityGML2GraphDB/editor/Test_Editor_Formatted.gml";
		String stylesheetFile = "src/main/java/CityGML2GraphDB/editor/EditorFormatter.xsl";

		String extractedFile = "src/main/java/CityGML2GraphDB/editor/Test_Editor_Extracted.gml";
		String extractNodeStylesheetFile = "src/main/java/CityGML2GraphDB/editor/ExtractNodeFormatter.xsl";
	}
}
