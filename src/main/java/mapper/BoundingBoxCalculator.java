package mapper;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.CeilingSurface;
import org.citygml4j.model.citygml.building.ClosureSurface;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.FloorSurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.InteriorWallSurface;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.building.Window;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.basicTypes.Coordinates;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurveProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolidProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurfacePatch;
import org.citygml4j.model.gml.geometry.primitives.ControlPoint;
import org.citygml4j.model.gml.geometry.primitives.Coord;
import org.citygml4j.model.gml.geometry.primitives.Curve;
import org.citygml4j.model.gml.geometry.primitives.CurveArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveInterpolation;
import org.citygml4j.model.gml.geometry.primitives.CurveProperty;
import org.citygml4j.model.gml.geometry.primitives.CurveSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.GeometricPositionGroup;
import org.citygml4j.model.gml.geometry.primitives.InnerBoundaryIs;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.OuterBoundaryIs;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.PointRep;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRep;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRepOrCoord;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Ring;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceInterpolation;
import org.citygml4j.model.gml.geometry.primitives.SurfacePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.measures.Length;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import mapper.EnumClasses.GMLRelTypes;
import util.GraphUtil;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class BoundingBoxCalculator {
	/*
	 * Hierarchical functions
	 */
	public static AbstractSurface createSearchAbstractSurface(Node node) {
		if (node.hasLabel(Label.label(GMLClass.COMPOSITE_SURFACE.toString()))) {
			return createCompositeSurface(node);
		} else if (node.hasLabel(Label.label(GMLClass.ORIENTABLE_SURFACE + ""))) {
			return createOrientableSurface(node);
		} else if (node.hasLabel(Label.label(GMLClass._TEXTURED_SURFACE + ""))) {
			return create_TexturedSurface(node);
		} else if (node.hasLabel(Label.label(GMLClass.POLYGON + ""))) {
			return createPolygon(node);
		} else if (node.hasLabel(Label.label(GMLClass.SURFACE + ""))) {
			return createSurface(node);
		} else if (node.hasLabel(Label.label(GMLClass.TRIANGULATED_SURFACE + ""))) {
			return createTriangulatedSurface(node);
		} else if (node.hasLabel(Label.label(GMLClass.TIN + ""))) {
			return createTin(node);
		}

		return null;
	}

	public static AbstractRingProperty createSearchAbstractRingProperty(Node node) {
		if (node.hasLabel(Label.label(GMLClass.EXTERIOR + ""))) {
			return createExterior(node);
		} else if (node.hasLabel(Label.label(GMLClass.INNER_BOUNDARY_IS + ""))) {
			return createInnerBoundaryIs(node);
		} else if (node.hasLabel(Label.label(GMLClass.INTERIOR + ""))) {
			return createInterior(node);
		} else if (node.hasLabel(Label.label(GMLClass.OUTER_BOUNDARY_IS + ""))) {
			return createOuterBoundaryIs(node);
		}

		return null;
	}

	public static AbstractRing createSearchAbstractRing(Node node) {
		if (node.hasLabel(Label.label(GMLClass.LINEAR_RING + ""))) {
			return createLinearRing(node);
		} else if (node.hasLabel(Label.label(GMLClass.RING + ""))) {
			return createRing(node);
		}

		return null;
	}

	public static AbstractCurve createSearchAbstractCurve(Node node) {
		if (node.hasLabel(Label.label(GMLClass.COMPOSITE_CURVE + ""))) {
			return createCompositeCurve(node);
		} else if (node.hasLabel(Label.label(GMLClass.CURVE + ""))) {
			return createCurve(node);
		} else if (node.hasLabel(Label.label(GMLClass.LINE_STRING + ""))) {
			return createLineString(node);
		} else if (node.hasLabel(Label.label(GMLClass.ORIENTABLE_CURVE + ""))) {
			return createOrientableCurve(node);
		}

		return null;
	}

	public static PointProperty createSearchPointProperty(Node node) {
		if (node.hasLabel(Label.label(GMLClass.POINT_PROPERTY + ""))) {
			return createPointProperty(node);
		} else if (node.hasLabel(Label.label(GMLClass.POINT_REP + ""))) {
			return createPointRep(node);
		}

		return null;
	}

	public static AbstractCurveSegment createSearchAbstractCurveSegment(Node node) {
		if (node.hasLabel(Label.label(GMLClass.LINE_STRING_SEGMENT + ""))) {
			return createLineStringSegment(node);
		}

		return null;
	}

	public static AbstractSurfacePatch createSearchAbstractSurfacePatch(Node node) {
		if (node.hasLabel(Label.label(GMLClass.RECTANGLE + ""))) {
			return createRectangle(node);
		} else if (node.hasLabel(Label.label(GMLClass.TRIANGLE + ""))) {
			return createTriangle(node);
		}

		return null;
	}

	public static AbstractSolid createSearchAbstractSolid(Node node) {
		if (node.hasLabel(Label.label(GMLClass.COMPOSITE_SOLID + ""))) {
			return createCompositeSolid(node);
		} else if (node.hasLabel(Label.label(GMLClass.SOLID + ""))) {
			return createSolid(node);
		}

		return null;
	}

	public static AbstractBoundarySurface createSearchAbstractBoundarySurface(Node node) {
		if (node.hasLabel(Label.label(CityGMLClass.BUILDING_CEILING_SURFACE + ""))) {
			return createCeilingSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_CLOSURE_SURFACE + ""))) {
			return createClosureSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_FLOOR_SURFACE + ""))) {
			return createFloorSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_GROUND_SURFACE + ""))) {
			return createGroundSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE + ""))) {
			return createInteriorWallSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE + ""))) {
			return createOuterCeilingSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE + ""))) {
			return createOuterFloorSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_ROOF_SURFACE + ""))) {
			return createRoofSurface(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_WALL_SURFACE + ""))) {
			return createWallSurface(node);
		}

		return null;
	}

	public static AbstractOpening createSearchAbstractOpening(Node node) {
		if (node.hasLabel(Label.label(CityGMLClass.BUILDING_DOOR + ""))) {
			return createDoor(node);
		} else if (node.hasLabel(Label.label(CityGMLClass.BUILDING_WINDOW + ""))) {
			return createWindow(node);
		}

		return null;
	}

	public static GeometryProperty<? extends AbstractGeometry> createSearchGeometryProperty(Node node) {
		// TODO

		// if (node.hasLabel(Label.label(GMLClass.COMPOSITE_CURVE_PROPERTY + ""))) {
		// return createCompositeCurveProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.COMPOSITE_SOLID_PROPERTY + ""))) {
		// return createCompositeSolidProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.COMPOSITE_SURFACE_PROPERTY + ""))) {
		// return createCompositeSurfaceProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.CURVE_PROPERTY + ""))) {
		// return createCurveProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.DOMAIN_SET + ""))) {
		// return createDomainSet(node);
		// } else if (node.hasLabel(Label.label(GMLClass.RECTIFIED_GRID_DOMAIN + ""))) {
		// return createRectifiedGridDomain(node);
		// } else if (node.hasLabel(Label.label(GMLClass.GEOMETRIC_COMPLEX_PROPERTY + ""))) {
		// return createGeometricComplexProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.GEOMETRIC_PRIMITIVE_PROPERTY + ""))) {
		// return createGeometricPrimitiveProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.LINE_STRING_PROPERTY + ""))) {
		// return createLineStringProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.LOCATION_PROPERTY + ""))) {
		// return createLocationProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.PRIORITY_LOCATION_PROPERTY + ""))) {
		// return createPriorityLocationProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_CURVE_PROPERTY + ""))) {
		// return createMultiCurveProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_GEOMETRY_PROPERTY + ""))) {
		// return createMultiGeometryProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_LINE_STRING_PROPERTY + ""))) {
		// return createMultiLineStringProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_POINT_PROPERTY + ""))) {
		// return createMultiPointProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_POLYGON_PROPERTY + ""))) {
		// return createMultiPolygonProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_SOLID_PROPERTY + ""))) {
		// return createMultiSolidProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.MULTI_SURFACE_PROPERTY + ""))) {
		// return createMultiSurfaceProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.POINT_PROPERTY + ""))) {
		// return createPointProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.POINT_REP + ""))) {
		// return createPointRep(node);
		// } else if (node.hasLabel(Label.label(GMLClass.POLYGON_PROPERTY + ""))) {
		// return createPolygonProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.SOLID_PROPERTY + ""))) {
		// return createSolidProperty(node);
		// } else if (node.hasLabel(Label.label(GMLClass.SURFACE_PROPERTY + ""))) {
		// return createSurfaceProperty(node);
		// } else if (node.hasLabel(Label.label(CityGMLClass.TIN_PROPERTY + ""))) {
		// return createTinProperty(node);
		// }

		return null;
	}

	/*
	 * Surface
	 */
	public static MultiSurfaceProperty createMultiSurfaceProperty(Node node) {
		MultiSurfaceProperty result = new MultiSurfaceProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			MultiSurface obj = createMultiSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static MultiSurface createMultiSurface(Node node) {
		MultiSurface result = new MultiSurface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.SURFACE_MEMBER)) {
			List<SurfaceProperty> list = new ArrayList<SurfaceProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.SURFACE_MEMBER)) {
				list.add(createSurfaceProperty(n));
			}

			result.setSurfaceMember(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.SURFACE_MEMBERS)) {
			SurfaceArrayProperty surfaceMembers = createSurfaceArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.SURFACE_MEMBERS));
			result.setSurfaceMembers(surfaceMembers);
		}

		return result;
	}

	public static SurfaceProperty createSurfaceProperty(Node node) {
		SurfaceProperty result = new SurfaceProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractSurface obj = createSearchAbstractSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static SurfaceArrayProperty createSurfaceArrayProperty(Node node) {
		SurfaceArrayProperty result = new SurfaceArrayProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			List<AbstractSurface> list = new ArrayList<AbstractSurface>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OBJECT)) {
				list.add(createSearchAbstractSurface(n));
			}

			result.setObject(list);
		}

		return result;
	}

	public static CompositeSurface createCompositeSurface(Node node) {
		CompositeSurface result = new CompositeSurface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.SURFACE_MEMBER)) {
			List<SurfaceProperty> list = new ArrayList<SurfaceProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.SURFACE_MEMBER)) {
				list.add(createSurfaceProperty(n));
			}

			result.setSurfaceMember(list);
		}

		return result;
	}

	public static OrientableSurface createOrientableSurface(Node node) {
		OrientableSurface result = new OrientableSurface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BASE_SURFACE)) {
			SurfaceProperty obj = createSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BASE_SURFACE));
			result.setBaseSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.ORIENTATION)) {
			Sign obj = createSign(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.ORIENTATION));
			result.setOrientation(obj);
		}

		return result;
	}

	public static Sign createSign(Node node) {
		if (node.hasProperty("value")) {
			return Sign.fromValue(node.getProperty("value").toString());
		}

		return null;
	}

	public static _TexturedSurface create_TexturedSurface(Node node) {
		_TexturedSurface result = new _TexturedSurface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BASE_SURFACE)) {
			SurfaceProperty obj = createSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BASE_SURFACE));
			result.setBaseSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.ORIENTATION)) {
			Sign obj = createSign(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.ORIENTATION));
			result.setOrientation(obj);
		}

		return result;
	}

	public static Polygon createPolygon(Node node) {
		Polygon result = new Polygon();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.EXTERIOR)) {
			AbstractRingProperty obj = createSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR));
			result.setExterior(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.INTERIOR)) {
			List<AbstractRingProperty> list = new ArrayList<AbstractRingProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.INTERIOR)) {
				list.add(createSearchAbstractRingProperty(n));
			}

			result.setInterior(list);
		}

		return result;
	}

	public static Exterior createExterior(Node node) {
		Exterior result = new Exterior();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractRing obj = createSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static InnerBoundaryIs createInnerBoundaryIs(Node node) {
		InnerBoundaryIs result = new InnerBoundaryIs();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractRing obj = createSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static Interior createInterior(Node node) {
		Interior result = new Interior();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractRing obj = createSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static OuterBoundaryIs createOuterBoundaryIs(Node node) {
		OuterBoundaryIs result = new OuterBoundaryIs();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractRing obj = createSearchAbstractRing(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static LinearRing createLinearRing(Node node) {
		LinearRing result = new LinearRing();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CONTROL_POINTS)) {
			List<PosOrPointPropertyOrPointRep> list = new ArrayList<PosOrPointPropertyOrPointRep>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CONTROL_POINTS)) {
				list.add(createPosOrPointPropertyOrPointRep(n));
			}

			result.setPosOrPointPropertyOrPointRep(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS_LIST)) {
			DirectPositionList obj = createDirectPositionList(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS_LIST));
			result.setPosList(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORDINATES)) {
			Coordinates obj = createCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES));
			result.setCoordinates(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORD)) {
			List<Coord> list = new ArrayList<Coord>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.COORD)) {
				list.add(createCoord(n));
			}

			result.setCoord(list);
		}

		return result;
	}

	public static PosOrPointPropertyOrPointRep createPosOrPointPropertyOrPointRep(Node node) {
		PosOrPointPropertyOrPointRep result = new PosOrPointPropertyOrPointRep();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS));
			result.setPos(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POINT_PROPERTY)) {
			PointProperty obj = createSearchPointProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_PROPERTY));
			result.setPointProperty(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POINT_REP)) {
			PointRep obj = createPointRep(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_REP));
			result.setPointRep(obj);
		}

		return result;
	}

	public static PosOrPointPropertyOrPointRepOrCoord createPosOrPointPropertyOrPointRepOrCoord(Node node) {
		PosOrPointPropertyOrPointRepOrCoord result = new PosOrPointPropertyOrPointRepOrCoord();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS));
			result.setPos(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POINT_PROPERTY)) {
			PointProperty obj = createSearchPointProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_PROPERTY));
			result.setPointProperty(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POINT_REP)) {
			PointRep obj = createPointRep(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_REP));
			result.setPointRep(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORD)) {
			Coord obj = createCoord(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORD));
			result.setCoord(obj);
		}

		return result;
	}

	public static DirectPositionList createDirectPositionList(Node node) {
		DirectPositionList result = new DirectPositionList();

		if (node.hasProperty("count")) {
			result.setCount(Integer.parseInt(node.getProperty("count").toString()));
		}

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasProperty(GMLRelTypes.VALUE.toString())) {
			List<Double> list = new ArrayList<Double>();
			for (String s : node.getProperty(GMLRelTypes.VALUE.toString()).toString().split("\\s+|;")) {
				list.add(Double.parseDouble(s));
			}

			result.setValue(list);
		}

		return result;
	}

	public static Coordinates createCoordinates(Node node) {
		Coordinates result = new Coordinates();

		if (node.hasProperty("value")) {
			result.setValue(node.getProperty("value").toString());
		}

		if (node.hasProperty("decimal")) {
			result.setDecimal(node.getProperty("decimal").toString());
		}

		if (node.hasProperty("cs")) {
			result.setCs(node.getProperty("cs").toString());
		}

		if (node.hasProperty("ts")) {
			result.setTs(node.getProperty("ts").toString());
		}

		return result;
	}

	public static Coord createCoord(Node node) {
		Coord result = new Coord();

		if (node.hasProperty("x")) {
			result.setX(Double.parseDouble(node.getProperty("x").toString()));
		}

		if (node.hasProperty("y")) {
			result.setY(Double.parseDouble(node.getProperty("y").toString()));
		}

		if (node.hasProperty("z")) {
			result.setZ(Double.parseDouble(node.getProperty("z").toString()));
		}

		return result;
	}

	public static DirectPosition createDirectPosition(Node node) {
		DirectPosition result = new DirectPosition();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasProperty(GMLRelTypes.VALUE.toString())) {
			List<Double> list = new ArrayList<Double>();
			for (String s : node.getProperty(GMLRelTypes.VALUE.toString()).toString().split("\\s+|;")) {
				list.add(Double.parseDouble(s));
			}

			result.setValue(list);
		}

		return result;
	}

	public static PointProperty createPointProperty(Node node) {
		PointProperty result = new PointProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			Point obj = createPoint(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static PointRep createPointRep(Node node) {
		PointRep result = new PointRep();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			Point obj = createPoint(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static Point createPoint(Node node) {
		Point result = new Point();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS));
			result.setPos(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORDINATES)) {
			Coordinates obj = createCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES));
			result.setCoordinates(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORD)) {
			Coord obj = createCoord(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORD));
			result.setCoord(obj);
		}

		return result;
	}

	public static Ring createRing(Node node) {
		Ring result = new Ring();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CURVE_MEMBER)) {
			List<CurveProperty> list = new ArrayList<CurveProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CURVE_MEMBER)) {
				list.add(createCurveProperty(n));
			}

			result.setCurveMember(list);
		}

		return result;
	}

	public static CurveProperty createCurveProperty(Node node) {
		CurveProperty result = new CurveProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractCurve obj = createSearchAbstractCurve(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static CompositeCurve createCompositeCurve(Node node) {
		CompositeCurve result = new CompositeCurve();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CURVE_MEMBER)) {
			List<CurveProperty> list = new ArrayList<CurveProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CURVE_MEMBER)) {
				list.add(createCurveProperty(n));
			}

			result.setCurveMember(list);
		}

		return result;
	}

	public static Curve createCurve(Node node) {
		Curve result = new Curve();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.SEGMENTS)) {
			CurveSegmentArrayProperty obj = createCurveSegmentArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.SEGMENTS));
			result.setSegments(obj);
		}

		return result;
	}

	public static CurveSegmentArrayProperty createCurveSegmentArrayProperty(Node node) {
		CurveSegmentArrayProperty result = new CurveSegmentArrayProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			List<AbstractCurveSegment> list = new ArrayList<AbstractCurveSegment>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OBJECT)) {
				list.add(createSearchAbstractCurveSegment(n));
			}

			result.setObject(list);
		}

		return result;
	}

	public static LineStringSegment createLineStringSegment(Node node) {
		LineStringSegment result = new LineStringSegment();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CONTROL_POINTS)) {
			List<PosOrPointPropertyOrPointRep> list = new ArrayList<PosOrPointPropertyOrPointRep>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CONTROL_POINTS)) {
				list.add(createPosOrPointPropertyOrPointRep(n));
			}

			result.setPosOrPointPropertyOrPointRep(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS_LIST)) {
			DirectPositionList obj = createDirectPositionList(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS_LIST));
			result.setPosList(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORDINATES)) {
			Coordinates obj = createCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES));
			result.setCoordinates(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.INTERPOLATION)) {
			CurveInterpolation obj = createCurveInterpolation(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.INTERPOLATION));
			result.setInterpolation(obj);
		}

		return result;
	}

	public static CurveInterpolation createCurveInterpolation(Node node) {
		if (node.hasProperty("value")) {
			return CurveInterpolation.fromValue(node.getProperty("value").toString());
		}

		return null;
	}

	public static LineString createLineString(Node node) {
		LineString result = new LineString();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CONTROL_POINTS)) {
			List<PosOrPointPropertyOrPointRepOrCoord> list = new ArrayList<PosOrPointPropertyOrPointRepOrCoord>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CONTROL_POINTS)) {
				list.add(createPosOrPointPropertyOrPointRepOrCoord(n));
			}

			result.setPosOrPointPropertyOrPointRepOrCoord(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS_LIST)) {
			DirectPositionList obj = createDirectPositionList(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS_LIST));
			result.setPosList(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORDINATES)) {
			Coordinates obj = createCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES));
			result.setCoordinates(obj);
		}

		return result;
	}

	public static OrientableCurve createOrientableCurve(Node node) {
		OrientableCurve result = new OrientableCurve();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BASE_CURVE)) {
			CurveProperty obj = createCurveProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BASE_CURVE));
			result.setBaseCurve(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.ORIENTATION)) {
			Sign obj = createSign(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.ORIENTATION));
			result.setOrientation(obj);
		}

		return result;
	}

	public static Surface createSurface(Node node) {
		Surface result = new Surface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.PATCHES)) {
			SurfacePatchArrayProperty obj = createSurfacePatchArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.PATCHES));
			result.setPatches(obj);
		}

		return result;
	}

	public static SurfacePatchArrayProperty createSurfacePatchArrayProperty(Node node) {
		SurfacePatchArrayProperty result = new SurfacePatchArrayProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			List<AbstractSurfacePatch> list = new ArrayList<AbstractSurfacePatch>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OBJECT)) {
				list.add(createSearchAbstractSurfacePatch(n));
			}

			result.setObject(list);
		}

		return result;
	}

	public static Rectangle createRectangle(Node node) {
		Rectangle result = new Rectangle();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.EXTERIOR)) {
			AbstractRingProperty obj = createSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR));
			result.setExterior(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.INTERPOLATION)) {
			SurfaceInterpolation obj = createSurfaceInterpolation(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.INTERPOLATION));
			result.setInterpolation(obj);
		}

		return result;
	}

	public static SurfaceInterpolation createSurfaceInterpolation(Node node) {
		if (node.hasProperty("value")) {
			return SurfaceInterpolation.fromValue(node.getProperty("value").toString());
		}

		return null;
	}

	public static Triangle createTriangle(Node node) {
		Triangle result = new Triangle();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.EXTERIOR)) {
			AbstractRingProperty obj = createSearchAbstractRingProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR));
			result.setExterior(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.INTERPOLATION)) {
			SurfaceInterpolation obj = createSurfaceInterpolation(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.INTERPOLATION));
			result.setInterpolation(obj);
		}

		return result;
	}

	public static Surface createTriangulatedSurface(Node node) {
		TriangulatedSurface result = new TriangulatedSurface();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.PATCHES)) {
			SurfacePatchArrayProperty obj = createSurfacePatchArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.PATCHES));
			result.setPatches(obj);
		}

		return result;
	}

	public static Tin createTin(Node node) {
		Tin result = new Tin();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.PATCHES)) {
			SurfacePatchArrayProperty obj = createSurfacePatchArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.PATCHES));
			result.setPatches(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.STOP_LINES)) {
			List<LineStringSegmentArrayProperty> list = new ArrayList<LineStringSegmentArrayProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.STOP_LINES)) {
				list.add(createLineStringSegmentArrayProperty(n));
			}

			result.setStopLines(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BREAK_LINES)) {
			List<LineStringSegmentArrayProperty> list = new ArrayList<LineStringSegmentArrayProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.BREAK_LINES)) {
				list.add(createLineStringSegmentArrayProperty(n));
			}

			result.setBreakLines(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.MAX_LENGTH)) {
			Length obj = createLength(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.MAX_LENGTH));
			result.setMaxLength(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CONTROL_POINT)) {
			ControlPoint obj = createControlPoint(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.CONTROL_POINT));
			result.setControlPoint(obj);
		}

		return result;
	}

	public static LineStringSegmentArrayProperty createLineStringSegmentArrayProperty(Node node) {
		LineStringSegmentArrayProperty result = new LineStringSegmentArrayProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			List<LineStringSegment> list = new ArrayList<LineStringSegment>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OBJECT)) {
				list.add(createLineStringSegment(n));
			}

			result.setObject(list);
		}

		return result;
	}

	public static Length createLength(Node node) {
		Length result = new Length();

		if (node.hasProperty("value")) {
			result.setValue(Double.parseDouble(node.getProperty("value").toString()));
		}

		if (node.hasProperty("uom")) {
			result.setUom(node.getProperty("uom").toString());
		}

		return result;
	}

	public static ControlPoint createControlPoint(Node node) {
		ControlPoint result = new ControlPoint();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS_LIST)) {
			DirectPositionList obj = createDirectPositionList(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS_LIST));
			result.setPosList(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.GEOMETRIC_POSITION_GROUP)) {
			List<GeometricPositionGroup> list = new ArrayList<GeometricPositionGroup>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.GEOMETRIC_POSITION_GROUP)) {
				list.add(createGeometricPositionGroup(n));
			}

			result.setGeometricPositionGroup(list);
		}

		return result;
	}

	public static GeometricPositionGroup createGeometricPositionGroup(Node node) {
		GeometricPositionGroup result = new GeometricPositionGroup();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POS));
			result.setPos(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POINT_PROPERTY)) {
			PointProperty obj = createSearchPointProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.POINT_PROPERTY));
			result.setPointProperty(obj);
		}

		return result;
	}

	// Curve
	public static MultiCurveProperty createMultiCurveProperty(Node node) {
		MultiCurveProperty result = new MultiCurveProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			MultiCurve obj = createMultiCurve(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static MultiCurve createMultiCurve(Node node) {
		MultiCurve result = new MultiCurve();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CURVE_MEMBER)) {
			List<CurveProperty> list = new ArrayList<CurveProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.CURVE_MEMBER)) {
				list.add(createCurveProperty(n));
			}

			result.setCurveMember(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.CURVE_MEMBERS)) {
			CurveArrayProperty surfaceMembers = createCurveArrayProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.CURVE_MEMBERS));
			result.setCurveMembers(surfaceMembers);
		}

		return result;
	}

	public static CurveArrayProperty createCurveArrayProperty(Node node) {
		CurveArrayProperty result = new CurveArrayProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			List<AbstractCurve> list = new ArrayList<AbstractCurve>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OBJECT)) {
				list.add(createSearchAbstractCurve(n));
			}

			result.setObject(list);
		}

		return result;
	}

	// BoundarySurface
	public static BoundarySurfaceProperty createBoundarySurfaceProperty(Node node) {
		BoundarySurfaceProperty result = new BoundarySurfaceProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractBoundarySurface obj = createSearchAbstractBoundarySurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static CeilingSurface createCeilingSurface(Node node) {
		CeilingSurface result = new CeilingSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static OpeningProperty createOpeningProperty(Node node) {
		OpeningProperty result = new OpeningProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractOpening obj = createSearchAbstractOpening(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static Door createDoor(Node node) {
		Door result = new Door();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION));
			result.setLod3ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION));
			result.setLod4ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static Window createWindow(Node node) {
		Window result = new Window();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION));
			result.setLod3ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION));
			result.setLod4ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static BoundingShape createBoundingShape(Node node) {
		BoundingShape result = new BoundingShape();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.ENVELOPE)) {
			Envelope obj = createEnvelope(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.ENVELOPE));
			result.setEnvelope(obj);
		}

		return result;
	}

	public static Envelope createEnvelope(Node node) {
		Envelope result = new Envelope();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOWER_CORNER)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOWER_CORNER));
			result.setLowerCorner(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.UPPER_CORNER)) {
			DirectPosition obj = createDirectPosition(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.UPPER_CORNER));
			result.setUpperCorner(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORD)) {
			List<Coord> list = new ArrayList<Coord>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.COORD)) {
				list.add(createCoord(n));
			}

			result.setCoord(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.POS)) {
			List<DirectPosition> list = new ArrayList<DirectPosition>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.POS)) {
				list.add(createDirectPosition(n));
			}

			result.setPos(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.COORDINATES)) {
			Coordinates obj = createCoordinates(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.COORDINATES));
			result.setCoordinates(obj);
		}

		return result;
	}

	public static ImplicitRepresentationProperty createImplicitRepresentationProperty(Node node) {
		ImplicitRepresentationProperty result = new ImplicitRepresentationProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			ImplicitGeometry obj = createImplicitGeometry(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static ImplicitGeometry createImplicitGeometry(Node node) {
		ImplicitGeometry result = new ImplicitGeometry();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.TRANSFORMATION_MATRIX)) {
			TransformationMatrix4x4 obj = createTransformationMatrix4x4(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.TRANSFORMATION_MATRIX));
			result.setTransformationMatrix(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.RELATIVE_GEOMETRY)) {
			GeometryProperty<? extends AbstractGeometry> obj = createSearchGeometryProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.RELATIVE_GEOMETRY));
			result.setRelativeGeometry(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.REFERENCE_POINT)) {
			PointProperty obj = createSearchPointProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.REFERENCE_POINT));
			result.setReferencePoint(obj);
		}

		return result;
	}

	public static TransformationMatrix4x4 createTransformationMatrix4x4(Node node) {
		TransformationMatrix4x4 result = new TransformationMatrix4x4();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.MATRIX)) {
			Matrix obj = createMatrix(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.MATRIX));
			result.setMatrix(obj);
		}

		return result;
	}

	public static Matrix createMatrix(Node node) {
		int nrOfCols = 0;
		if (node.hasProperty("nrOfCols")) {
			nrOfCols = Integer.parseInt(node.getProperty("nrOfCols").toString());
		}

		int nrOfRows = 0;
		if (node.hasProperty("nrOfRows")) {
			nrOfRows = Integer.parseInt(node.getProperty("nrOfRows").toString());
		}

		double[][] content = new double[nrOfRows][nrOfCols];

		if (node.hasProperty("matrixContent")) {
			// [[1,2,3],[1,2,3],[1,2,3]]
			String[] matrixContent = node.getProperty("matrixContent").toString().split("],");
			for (int i = 0; i < matrixContent.length; i++) {
				String[] row = matrixContent[i].replaceAll("[|]", "").split(",");
				for (int j = 0; j < row.length; j++) {
					content[i][j] = Double.parseDouble(row[j]);
				}
			}
		}

		Matrix result = new Matrix(content);

		return result;
	}

	public static ClosureSurface createClosureSurface(Node node) {
		ClosureSurface result = new ClosureSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static FloorSurface createFloorSurface(Node node) {
		FloorSurface result = new FloorSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static GroundSurface createGroundSurface(Node node) {
		GroundSurface result = new GroundSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static InteriorWallSurface createInteriorWallSurface(Node node) {
		InteriorWallSurface result = new InteriorWallSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static OuterCeilingSurface createOuterCeilingSurface(Node node) {
		OuterCeilingSurface result = new OuterCeilingSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static OuterFloorSurface createOuterFloorSurface(Node node) {
		OuterFloorSurface result = new OuterFloorSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static RoofSurface createRoofSurface(Node node) {
		RoofSurface result = new RoofSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static WallSurface createWallSurface(Node node) {
		WallSurface result = new WallSurface();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty obj = createMultiSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OPENING)) {
			List<OpeningProperty> list = new ArrayList<OpeningProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OPENING)) {
				list.add(createOpeningProperty(n));
			}

			result.setOpening(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	// Solid
	public static SolidProperty createSolidProperty(Node node) {
		SolidProperty result = new SolidProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			AbstractSolid obj = createSearchAbstractSolid(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static CompositeSolid createCompositeSolid(Node node) {
		CompositeSolid result = new CompositeSolid();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.SOLID_MEMBER)) {
			List<SolidProperty> list = new ArrayList<SolidProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.SOLID_MEMBER)) {
				list.add(createSolidProperty(n));
			}

			result.setSolidMember(list);
		}

		return result;
	}

	public static Solid createSolid(Node node) {
		Solid result = new Solid();

		if (node.hasProperty("srsDimension")) {
			result.setSrsDimension(Integer.parseInt(node.getProperty("srsDimension").toString()));
		}

		if (node.hasProperty("srsName")) {
			result.setSrsName(node.getProperty("srsName").toString());
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.EXTERIOR)) {
			SurfaceProperty obj = createSurfaceProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.EXTERIOR));
			result.setExterior(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.INTERIOR)) {
			List<SurfaceProperty> list = new ArrayList<SurfaceProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.INTERIOR)) {
				list.add(createSurfaceProperty(n));
			}

			result.setInterior(list);
		}

		return result;
	}

	// BuildingInstallation
	public static BuildingInstallationProperty createBuildingInstallationProperty(Node node) {
		BuildingInstallationProperty result = new BuildingInstallationProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			BuildingInstallation obj = createBuildingInstallation(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static BuildingInstallation createBuildingInstallation(Node node) {
		BuildingInstallation result = new BuildingInstallation();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_GEOMETRY)) {
			GeometryProperty<? extends AbstractGeometry> obj = createSearchGeometryProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_GEOMETRY));
			result.setLod2Geometry(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_GEOMETRY)) {
			GeometryProperty<? extends AbstractGeometry> obj = createSearchGeometryProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_GEOMETRY));
			result.setLod3Geometry(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_GEOMETRY)) {
			GeometryProperty<? extends AbstractGeometry> obj = createSearchGeometryProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_GEOMETRY));
			result.setLod4Geometry(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_IMPLICIT_REPRESENTATION));
			result.setLod2ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION));
			result.setLod3ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION)) {
			ImplicitRepresentationProperty obj = createImplicitRepresentationProperty(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION));
			result.setLod4ImplicitRepresentation(obj);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY_SURFACE)) {
			List<BoundarySurfaceProperty> list = new ArrayList<BoundarySurfaceProperty>();

			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.BOUNDED_BY_SURFACE)) {
				list.add(createBoundarySurfaceProperty(n));
			}

			result.setBoundedBySurface(list);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
			BoundingShape obj = createBoundingShape(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.BOUNDED_BY));
			result.setBoundedBy(obj);
		}

		return result;
	}

	public static CompositeCurveProperty createCompositeCurveProperty(Node node) {
		CompositeCurveProperty result = new CompositeCurveProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			CompositeCurve obj = createCompositeCurve(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static CompositeSolidProperty createCompositeSolidProperty(Node node) {
		CompositeSolidProperty result = new CompositeSolidProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			CompositeSolid obj = createCompositeSolid(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static CompositeSurfaceProperty createCompositeSurfaceProperty(Node node) {
		CompositeSurfaceProperty result = new CompositeSurfaceProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			CompositeSurface obj = createCompositeSurface(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	// BuildingPart
	public static BuildingPartProperty createBuildingPartProperty(Node node) {
		BuildingPartProperty result = new BuildingPartProperty();

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OBJECT)) {
			BuildingPart obj = createBuildingPart(GraphUtil.findFirstChildOfNode(node, GMLRelTypes.OBJECT));
			result.setObject(obj);
		}

		return result;
	}

	public static BuildingPart createBuildingPart(Node node) {
		BuildingPart result = new BuildingPart();

		// LoD0
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD0_FOOT_PRINT)) {
			MultiSurfaceProperty lod0FootPrint = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD0_FOOT_PRINT));
			result.setLod0FootPrint(lod0FootPrint);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD0_ROOF_EDGE)) {
			MultiSurfaceProperty lod0RoofEdge = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD0_ROOF_EDGE));
			result.setLod0RoofEdge(lod0RoofEdge);
		}

		// LoD1-4 Solid
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD1_SOLID)) {
			SolidProperty lod1Solid = BoundingBoxCalculator.createSolidProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD1_SOLID));
			result.setLod1Solid(lod1Solid);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_SOLID)) {
			SolidProperty lod2Solid = BoundingBoxCalculator.createSolidProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_SOLID));
			result.setLod2Solid(lod2Solid);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_SOLID)) {
			SolidProperty lod3Solid = BoundingBoxCalculator.createSolidProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_SOLID));
			result.setLod3Solid(lod3Solid);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_SOLID)) {
			SolidProperty lod4Solid = BoundingBoxCalculator.createSolidProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_SOLID));
			result.setLod4Solid(lod4Solid);
		}

		// LoD1-4 MultiSurface
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD1_MULTI_SURFACE)) {
			MultiSurfaceProperty lod1MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD1_MULTI_SURFACE));
			result.setLod1MultiSurface(lod1MultiSurface);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
			MultiSurfaceProperty lod2MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_SURFACE));
			result.setLod2MultiSurface(lod2MultiSurface);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
			MultiSurfaceProperty lod3MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_SURFACE));
			result.setLod3MultiSurface(lod3MultiSurface);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
			MultiSurfaceProperty lod4MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_SURFACE));
			result.setLod4MultiSurface(lod4MultiSurface);
		}

		// LoD2-4 MultiCurve
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_CURVE)) {
			MultiCurveProperty lod2MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD2_MULTI_CURVE));
			result.setLod2MultiCurve(lod2MultiCurve);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_CURVE)) {
			MultiCurveProperty lod3MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD3_MULTI_CURVE));
			result.setLod3MultiCurve(lod3MultiCurve);
		}

		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_CURVE)) {
			MultiCurveProperty lod4MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
					GraphUtil.findFirstChildOfNode(node, GMLRelTypes.LOD4_MULTI_CURVE));
			result.setLod4MultiCurve(lod4MultiCurve);
		}

		// BOUNDED_BY_SURFACE
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY_SURFACE)) {
			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.BOUNDED_BY_SURFACE)) {
				BoundarySurfaceProperty boundarySurface = BoundingBoxCalculator.createBoundarySurfaceProperty(n);
				result.addBoundedBySurface(boundarySurface);
			}
		}

		// OuterBuildingInstallation
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
				BuildingInstallationProperty outerBuildingInstallation = BoundingBoxCalculator.createBuildingInstallationProperty(n);
				result.addOuterBuildingInstallation(outerBuildingInstallation);
			}
		}

		// BuildingPart
		if (node.hasRelationship(Direction.OUTGOING, GMLRelTypes.BUILDING_PART)) {
			for (Node n : GraphUtil.findChildrenOfNode(node, GMLRelTypes.BUILDING_PART)) {
				BuildingPartProperty buildingPart = BoundingBoxCalculator.createBuildingPartProperty(n);
				result.addConsistsOfBuildingPart(buildingPart);
			}
		}

		return result;
	}
}
