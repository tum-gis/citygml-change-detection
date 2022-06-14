package components.multithreading;

import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.ParentInfo;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

// Used as end signal for the producer-consumer multi-threading concept
public class PoisonPillXMLChunk implements XMLChunk {
    public PoisonPillXMLChunk() {
    }

    @Override
    public CityGML unmarshal() throws UnmarshalException, MissingADESchemaException {
        return null;
    }

    @Override
    public QName getTypeName() {
        return null;
    }

    @Override
    public CityGMLClass getCityGMLClass() {
        return null;
    }

    @Override
    public boolean isSetParentInfo() {
        return false;
    }

    @Override
    public ParentInfo getParentInfo() {
        return null;
    }

    @Override
    public boolean hasPassedXMLValidation() {
        return false;
    }

    @Override
    public void send(ContentHandler handler, boolean release) throws SAXException {

    }
}
