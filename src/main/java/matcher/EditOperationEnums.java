package matcher;

public class EditOperationEnums {
	// Update property
	public enum UpdatePropertyNodeProperties {
		OP_ID,
		OLD_PARENT_NODE_TYPE,
		OLD_PARENT_NODE_GMLID,
		OF_OLD_NEAREST_GMLID,
		OF_OLD_BUILDING_GMLID,
		PROPERTY_NAME,
		OLD_VALUE,
		NEW_VALUE,
		IS_OPTIONAL,
		MESSAGE,
	}

	// Delete property
	public enum DeletePropertyNodeProperties {
		OP_ID,
		OLD_PARENT_NODE_TYPE, 
		OLD_PARENT_NODE_GMLID, 
		OF_OLD_NEAREST_GMLID, 
		OF_OLD_BUILDING_GMLID, 
		PROPERTY_NAME, 
		OLD_VALUE, 
		IS_OPTIONAL, 
		MESSAGE,
	}

	// Insert property
	public enum InsertPropertyNodeProperties {
		OP_ID, 
		OLD_PARENT_NODE_TYPE, 
		OLD_PARENT_NODE_GMLID, 
		OF_OLD_NEAREST_GMLID, 
		OF_OLD_BUILDING_GMLID, 
		PROPERTY_NAME, 
		NEW_VALUE, 
		IS_OPTIONAL, 
		MESSAGE, 
	}

	// Delete node
	public enum DeleteRelationshipNodeProperties {
		OP_ID, 
		DELETE_NODE_TYPE, 
		DELETE_NODE_GMLID, 
		OF_OLD_NEAREST_GMLID, 
		OF_OLD_BUILDING_GMLID, 
		IS_OPTIONAL, 
		MESSAGE, 
	}

	// Insert node
	public enum InsertRelationshipNodeProperties {
		OP_ID, 
		INSERT_RELATIONSHIP_TYPE, 
		INSERT_NODE_TYPE, 
		INSERT_NODE_GMLID, 
		OF_OLD_NEAREST_GMLID, 
		OF_OLD_BUILDING_GMLID, 
		OF_NEW_NEAREST_GMLID, 
		OF_NEW_BUILDING_GMLID, 
		IS_OPTIONAL, 
		MESSAGE, 
	}
}
