package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class StAXUtil {

	public static final String NAMESPACE_DEFAULT = "http://www.opengis.net/citygml/2.0";
	public static final String NAMESPACE_BLDG = "http://www.opengis.net/citygml/building/2.0";
	public static final String NAMESPACE_FES = "http://www.opengis.net/fes/2.0";
	public static final String NAMESPACE_CORE = "http://www.opengis.net/citygml/2.0";
	public static final String NAMESPACE_FRN = "http://www.opengis.net/citygml/cityfurniture/2.0";
	public static final String NAMESPACE_GEN = "http://www.opengis.net/citygml/generics/2.0";
	public static final String NAMESPACE_GML = "http://www.opengis.net/gml";
	public static final String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
	public static final String NAMESPACE_WFS = "http://www.opengis.net/wfs/2.0";
	public static final String NAMESPACE_XAL = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0";
	public static final String NAMESPACE_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String NAMESPACE_VCS = "http://www.virtualcitysystems.de/wfs/2.0";
	public static final String SCHEMA_LOCATINO = "http://www.opengis.net/citygml/building/2.0 "
			+ "http://schemas.opengis.net/citygml/building/2.0/building.xsd "
			+ "http://www.opengis.net/citygml/generics/2.0 "
			+ "http://schemas.opengis.net/citygml/generics/2.0/generics.xsd";

	// NAMESPACES
	public enum Namespaces {
		DEFAULT("xmlns=\"" + StAXUtil.NAMESPACE_DEFAULT + "\""),
		BLDG("xmlns:bldg=\"" + StAXUtil.NAMESPACE_BLDG + "\""),
		CORE("xmlns:core=\"" + StAXUtil.NAMESPACE_CORE + "\""),
		FES("xmlns:fes=\"" + StAXUtil.NAMESPACE_FES + "\""),
		FRN("xmlns:frn=\"" + StAXUtil.NAMESPACE_FRN + "\""),
		GEN("xmlns:gen=\"" + StAXUtil.NAMESPACE_GEN + "\""),
		GML("xmlns:gml=\"" + StAXUtil.NAMESPACE_GML + "\""),
		WFS("xmlns:wfs=\"" + StAXUtil.NAMESPACE_WFS + "\""),
		XAL("xmlns:xal=\"" + StAXUtil.NAMESPACE_XAL + "\""),
		XLINK("xmlns:xlink=\"" + StAXUtil.NAMESPACE_XLINK + "\""),
		XSI("xmlns:xsi=\"" + StAXUtil.NAMESPACE_XSI + "\""),
		VCS("xmlns:vcs=\"" + StAXUtil.NAMESPACE_VCS + "\""),
		SCHEMA_LOCATION("xmlns:schemaLocation=\"" + StAXUtil.SCHEMA_LOCATINO + "\"");

		private final String text;

		private Namespaces(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	// NAMESPACE PREFIXES
	public enum Prefixes {
		DEFAULT(""),
		BLDG("bldg"),
		CORE("core"),
		FRN("frn"),
		FES("fes"),
		GEN("gen"),
		GML("gml"),
		WFS("wfs"),
		XAL("xal"),
		XLINK("xlink"),
		XSI("xsi"),
		VCS("vcs");

		private final String text;

		private Prefixes(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/*
	 * test if an element is explicitly defined in a building (without href)
	 */
	public static boolean isDefinedInBuilding(String filename, String elementId, String buildingId) throws XMLStreamException, FileNotFoundException {
		// https://www.javacodegeeks.com/2013/05/parsing-xml-using-dom-sax-and-stax-parser-in-java.html
		if (elementId.equals(buildingId)) {
			return true;
		}

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		boolean foundBuilding = false;
		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getLocalName().equals("Building")
						&& reader.getAttributeValue(NAMESPACE_GML, "id").equals(buildingId)) {
					foundBuilding = true;
				} else if (foundBuilding
						&& reader.getAttributeValue(NAMESPACE_XLINK, "href") != null
						&& reader.getAttributeValue(NAMESPACE_XLINK, "href").replace("#", "").equals(elementId)) {
					return true;
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName().equals("Building")) {
					if (foundBuilding) {
						return false;
					}

					foundBuilding = false;
				}
				break;
			}
		}

		return false;
	}

	/*
	 * test if there is a path from a building to an element (without XLINK)
	 */
	public static boolean isReachableFromBuilding(String filename, String elementId, String buildingId) throws XMLStreamException, FileNotFoundException {
		if (elementId.equals(buildingId)) {
			return true;
		}

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		boolean foundBuilding = false;
		boolean foundXlink = false;
		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getLocalName().equals("Building")
						&& reader.getAttributeValue(NAMESPACE_GML, "id").equals(buildingId)) {
					foundBuilding = true;
				} else if (foundBuilding) {
					if (reader.getAttributeValue(NAMESPACE_XLINK, "href") != null
							&& reader.getAttributeValue(NAMESPACE_XLINK, "href").replace("#", "").equals(elementId)) {
						System.out.println(reader.getLocalName());
						foundXlink = true;
					}

					// StAX automatically resolves XLINK with real object
					// -> return false if the same XLINK has been found earlier
					if (reader.getAttributeValue(NAMESPACE_GML, "id") != null
							&& reader.getAttributeValue(NAMESPACE_GML, "id").equals(elementId)) {
						return !foundXlink;
					}
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName().equals("Building")) {
					if (foundBuilding) {
						return false;
					}

					foundBuilding = false;
				}
				break;
			}
		}

		return false;
	}

	/**
	 * Bi-directionally test if two nodes are located on the same path.
	 * 
	 * XLINKs also included.
	 * 
	 * @param filename
	 * @param elementId1
	 * @param elementId2
	 * @return -2 if not on the same path, -1 if elementId1 is descendant of elementId2, 0 if equal, 1 if ancestor
	 * @throws XMLStreamException
	 * @throws FileNotFoundException
	 */
	public static int areSamePath(String filename, String elementId1, String elementId2) throws XMLStreamException, FileNotFoundException {
		if (elementId1.equals(elementId2)) {
			return 0;
		}

		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		boolean found1 = false;
		boolean found2 = false;

		String name1 = null;
		String name2 = null;

		boolean end1 = false;
		boolean end2 = false;

		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				// System.out.println(reader.getLocalName() + " " + reader.getAttributeValue(NAMESPACE_GML, "id") + ", " + reader.getAttributeValue(NAMESPACE_XLINK, "href"));

				if (reader.getAttributeValue(NAMESPACE_GML, "id") != null) {
					if (reader.getAttributeValue(NAMESPACE_GML, "id").equals(elementId1)) {
						found1 = true;
						name1 = reader.getLocalName();
					} else if (reader.getAttributeValue(NAMESPACE_GML, "id").equals(elementId2)) {
						found2 = true;
						name2 = reader.getLocalName();
					}
				} else if (reader.getAttributeValue(NAMESPACE_XLINK, "href") != null) {
					if (reader.getAttributeValue(NAMESPACE_XLINK, "href").replace("#", "").equals(elementId1)) {
						found1 = true;
						name1 = reader.getLocalName();
					} else if (reader.getAttributeValue(NAMESPACE_XLINK, "href").replace("#", "").equals(elementId2)) {
						found2 = true;
						name2 = reader.getLocalName();
					}
				}

				break;

			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName().equals(name1)) {
					end1 = true;
					found1 = false;
				}

				if (reader.getLocalName().equals(name2)) {
					end2 = true;
					found2 = false;
				}

				if ((end1 && !found2) || (end2 && !found1)) {
					return -2;
				}

				if (end1 && found2) {
					return -1;
				}

				if (end2 && found1) {
					return 1;
				}
			}
		}

		return -2;

	}

	/*
	 * copy xml content of given node
	 */
	public static StringBuilder extractAsXmlContent(String filename, String elementId) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		StringBuilder result = new StringBuilder();
		boolean foundElement = false;
		String elementName = "";
		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getAttributeValue(NAMESPACE_GML, "id") != null
						&& reader.getAttributeValue(NAMESPACE_GML, "id").equals(elementId)) {
					foundElement = true;
					elementName = reader.getLocalName();
				}
				if (foundElement) {
					result.append("<" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart());

					// attributes
					for (int i = 0; i < reader.getAttributeCount(); i++) {
						result.append(" " +
								(reader.getAttributePrefix(i).isEmpty() ? "" : (reader.getAttributePrefix(i) + ":"))
								+ reader.getAttributeLocalName(i) + "=\"" + reader.getAttributeValue(i) + "\"");
					}

					result.append(">");
				}

				break;

			case XMLStreamConstants.CHARACTERS:
				if (foundElement) {
					result.append(reader.getText() + "");
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (reader.getLocalName().equals(elementName)) {
					result.append("</" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart() + ">");
					foundElement = false;
					return result;
				}

				if (foundElement && !reader.getLocalName().equals(elementName)) {
					result.append("</" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart() + ">");
				}

				break;
			}
		}

		return result;
	}

	/**
	 * Extract XML content of an arbitrary descendant of a given building based on its name and (optional) ID.
	 * 
	 * If an ID is given, the XML content of this exact element is returned.
	 * 
	 * If no ID is given, the first occurence of element with given name is returned.
	 * 
	 * @param filename
	 *            location of CityGML file, whose contents are to be extracted
	 * @param elementPrefix
	 *            namespace prefix, eg "bldg"
	 * @param elementName
	 *            element name, eg "boundedBy"
	 * @param elementId
	 *            (null if OPTIONAL) if the element can be uniquely identified by its name, ID is optional
	 * @param attributeNamespace
	 * @param attributeName
	 * @param attributeValue
	 * @param buildingPrefix
	 *            building namespace prefix, eg "bldg"
	 * @param buildingId
	 *            ID of building
	 * @return
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public static StringBuilder extractXmlContentOfBuildingElement(
			String buildingPrefix, String buildingId, String filename,
			String elementPrefix, String elementName,
			String attributeNamespace, String attributeName, String attributeValue) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		StringBuilder result = new StringBuilder();
		boolean foundBuilding = false;
		boolean foundElement = false;

		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getPrefix().contentEquals(buildingPrefix) && reader.getLocalName().equals("Building")) {
					foundBuilding = true;
				}

				if (foundBuilding
						&& reader.getPrefix().contentEquals(elementPrefix) && reader.getLocalName().equals(elementName)) {
					foundElement = true;

					if (attributeNamespace != null && attributeName != null && attributeValue != null) {
						foundElement = (reader.getAttributeValue(attributeNamespace, attributeName) != null
								&& reader.getAttributeValue(attributeNamespace, attributeName).equals(attributeValue));
					}
				}

				if (foundElement) {
					result.append("<" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart());

					// attributes
					for (int i = 0; i < reader.getAttributeCount(); i++) {
						result.append(" " +
								(reader.getAttributePrefix(i).isEmpty() ? "" : (reader.getAttributePrefix(i) + ":"))
								+ reader.getAttributeLocalName(i) + "=\"" + reader.getAttributeValue(i) + "\"");
					}

					result.append(">");
				}

				break;

			case XMLStreamConstants.CHARACTERS:
				if (foundElement) {
					result.append(reader.getText() + "");
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (foundElement) {
					result.append("</" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart() + ">");

					if (reader.getPrefix().equals(elementPrefix) && reader.getLocalName().equals(elementName)) {
						foundElement = false;
						foundBuilding = false;
						return result;
					}
				}

				break;
			}
		}

		return result;
	}

	/**
	 * Extract XML content of an arbitrary descendant of a given building based on its name and (optional) ID.
	 * 
	 * If an ID is given, the XML content of this exact element is returned.
	 * 
	 * If no ID is given, the first occurence of element with given name is returned.
	 * 
	 * @param filename
	 *            location of CityGML file, whose contents are to be extracted
	 * @param elementPrefix
	 *            namespace prefix, eg "bldg"
	 * @param elementName
	 *            element name, eg "boundedBy"
	 * @param elementId
	 *            (null if OPTIONAL) if the element can be uniquely identified by its name, ID is optional
	 * @param attributeNamespace
	 * @param attributeName
	 * @param attributeValue
	 * @param buildingPrefix
	 *            building namespace prefix, eg "bldg"
	 * @param buildingId
	 *            ID of building
	 * @return
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public static StringBuilder extractXmlContentOfBuildingElementWithoutTags(
			String buildingPrefix, String buildingId, String filename,
			String elementPrefix, String elementName,
			String attributeNamespace, String attributeName, String attributeValue) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		StringBuilder result = new StringBuilder();
		boolean foundBuilding = false;
		boolean foundElement = false;

		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getPrefix().contentEquals(buildingPrefix) && reader.getLocalName().equals("Building")) {
					foundBuilding = true;
				}

				if (foundBuilding
						&& reader.getPrefix().contentEquals(elementPrefix) && reader.getLocalName().equals(elementName)) {
					foundElement = true;

					if (attributeNamespace != null && attributeName != null && attributeValue != null) {
						foundElement = (reader.getAttributeValue(attributeNamespace, attributeName) != null
								&& reader.getAttributeValue(attributeNamespace, attributeName).equals(attributeValue));
					}
				}

				if (foundElement) {
					if (!reader.getName().getPrefix().equals(elementPrefix) || !reader.getName().getLocalPart().equals(elementName)) {
						result.append("<" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart());

						// attributes
						for (int i = 0; i < reader.getAttributeCount(); i++) {
							result.append(" " +
									(reader.getAttributePrefix(i).isEmpty() ? "" : (reader.getAttributePrefix(i) + ":"))
									+ reader.getAttributeLocalName(i) + "=\"" + reader.getAttributeValue(i) + "\"");
						}

						result.append(">");
					}
				}

				break;

			case XMLStreamConstants.CHARACTERS:
				if (foundElement) {
					result.append(reader.getText() + "");
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (foundElement) {
					if (!reader.getName().getPrefix().equals(elementPrefix) || !reader.getName().getLocalPart().equals(elementName)) {
						result.append("</" + (reader.getName().getPrefix().isEmpty() ? "" : (reader.getName().getPrefix() + ":")) + reader.getName().getLocalPart() + ">");
					} else {
						foundElement = false;
						foundBuilding = false;
						return result;
					}
				}

				break;
			}
		}

		return result;
	}

	public static StringBuilder extractXmlContentOfBuildingElementsWithId(
			String buildingPrefix, String buildingId, String filename,
			String elementPrefix, String elementName, String elementId) throws FileNotFoundException, XMLStreamException {

		return extractXmlContentOfBuildingElement(buildingPrefix, buildingId, filename,
				elementPrefix, elementName,
				NAMESPACE_GML.toString(), "id", elementId);
	}

	/**
	 * Return value of a simple building element without opening or ending tag.
	 * 
	 * Example: 12 of measuredHeight, instead of <bldg:measuredHeight>12</bldg:measuredHeight>
	 * 
	 * @param filename
	 * @param elementPrefix
	 * @param elementName
	 * @param elementId
	 * @param buildingPrefix
	 * @param buildingId
	 * @return
	 * @throws FileNotFoundException
	 * @throws XMLStreamException
	 */
	public static StringBuilder extractPropertyValueOfBuildingWithoutTags(
			String filename,
			String elementPrefix, String elementName,
			String buildingPrefix, String buildingId) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(
				new FileInputStream(filename));

		StringBuilder result = new StringBuilder();
		boolean foundBuilding = false;
		boolean foundElement = false;

		while (reader.hasNext()) {
			int event = reader.next();

			switch (event) {
			case XMLStreamConstants.START_ELEMENT:
				if (reader.getPrefix().contentEquals(buildingPrefix) && reader.getLocalName().equals("Building")) {
					foundBuilding = true;
				}

				if (foundBuilding
						&& reader.getPrefix().contentEquals(elementPrefix) && reader.getLocalName().equals(elementName)) {
					foundElement = true;
				}

				break;

			case XMLStreamConstants.CHARACTERS:
				if (foundElement) {
					result.append(reader.getText() + "");
				}
				break;

			case XMLStreamConstants.END_ELEMENT:
				if (foundElement) {
					if (reader.getPrefix().contentEquals(elementPrefix) && reader.getLocalName().equals(elementName)) {
						foundElement = false;
						foundBuilding = false;
						return result;
					}
				}

				break;
			}
		}

		return result;
	}

	/*
	 * Format xml contents
	 */
	public static StringBuilder repeatIndents(int n, String indent) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < n; i++) {
			result.append(indent);
		}

		return result;
	}

	public static StringBuilder formatContents(StringBuilder content, String indent) {
		String[] lines = content.toString().split("\\n");
		StringBuilder result = new StringBuilder();

		int j = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].isEmpty()) {
				continue;
			}

			if (lines[i].indexOf("<") != lines[i].indexOf("</")) {
				j++;
			} else {
				j--;
			}

			result.append(indent + repeatIndents(j, "  ") + lines[i].trim() + "\n");

			if (lines[i].indexOf("/>") != -1 || lines[i].indexOf("</") != -1) {
				j--;
			} else {
				j++;
			}

		}

		return result;
	}

	public static void main(String[] args) throws XMLStreamException, FileNotFoundException {
		System.out.println(isDefinedInBuilding("src/main/java/CityGML2GraphDB/test/TUM_Munich_0.gml",
				"DEBY_LOD2_4907138_396aa57d-85c5-4653-aaed-6b1f2c49df5d_poly",
				"DEBY_LOD2_4907138"));

		System.out.println(isReachableFromBuilding("src/main/java/CityGML2GraphDB/test/TUM_Munich_0.gml",
				"DEBY_LOD2_42364673_f4b64616-dbe3-496a-81ea-7e7b159e69c0",
				"DEBY_LOD2_4907138"));

		System.out.println(formatContents(extractAsXmlContent("src/main/java/CityGML2GraphDB/test/TUM_Munich_1.gml",
				"DEBY_LOD2_4959309_9e1fd31c-9124-45f1-8f63-f058743e46bb"), ""));

		System.out.println(areSamePath("src/main/java/CityGML2GraphDB/test/Test_XLink_0.gml",
				"GUID_1439827406056_2553965_2_1_0_",
				"GUID_1439827406056_2553965_2_2"));

		System.out.println(areSamePath("src/main/java/CityGML2GraphDB/test/Test_XLink_0.gml",
				"UUID_d679dbae-89a1-4b72-b62a-9e6b9334fc46",
				"GUID_1439827406056_2553965_2_2"));

		System.out.println(areSamePath("src/main/java/CityGML2GraphDB/test/Test_XLink_0.gml",
				"GUID_1439827406056_2553965_2_3",
				"DENW20AL0000nnRb"));

		System.out.println(formatContents(extractXmlContentOfBuildingElementsWithId(
				"bldg", "Test_Building", "src/main/java/CityGML2GraphDB/test/Demo_WFS_1.gml",
				"bldg", "RoofSurface", "osgb1000001802553202_28c77d1f-8e20-44a8-8ded-fd56b79806ff_2"), ""));

		System.out.println(formatContents(extractXmlContentOfBuildingElement(
				"bldg", "Test_Building", "src/main/java/CityGML2GraphDB/test/Demo_WFS_1.gml",
				"gen", "measureAttribute",
				"", "name", "MeasureD"), ""));

		System.out.println(formatContents(extractPropertyValueOfBuildingWithoutTags(
				"src/main/java/CityGML2GraphDB/test/Demo_WFS_1.gml",
				"core", "creationDate",
				"bldg", "Test_Building"), ""));

		System.out.println(formatContents(extractXmlContentOfBuildingElementWithoutTags(
				"bldg", "Test_Building", "src/main/java/CityGML2GraphDB/test/Demo_WFS_1.gml",
				"bldg", "lod2Solid",
				null, null, null), ""));
	}
}
