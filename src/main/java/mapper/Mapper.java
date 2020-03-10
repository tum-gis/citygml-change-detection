package mapper;

import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.AbstractTexture;
import org.citygml4j.model.citygml.appearance.AbstractTextureParameterization;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.AppearanceModuleComponent;
import org.citygml4j.model.citygml.appearance.AppearanceProperty;
import org.citygml4j.model.citygml.appearance.Color;
import org.citygml4j.model.citygml.appearance.ColorPlusOpacity;
import org.citygml4j.model.citygml.appearance.GeoreferencedTexture;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordGen;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.TextureType;
import org.citygml4j.model.citygml.appearance.WorldToTexture;
import org.citygml4j.model.citygml.appearance.WrapMode;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.bridge.AbstractBridge;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElement;
import org.citygml4j.model.citygml.bridge.BridgeConstructionElementProperty;
import org.citygml4j.model.citygml.bridge.BridgeFurniture;
import org.citygml4j.model.citygml.bridge.BridgeInstallation;
import org.citygml4j.model.citygml.bridge.BridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.BridgeModuleComponent;
import org.citygml4j.model.citygml.bridge.BridgePart;
import org.citygml4j.model.citygml.bridge.BridgePartProperty;
import org.citygml4j.model.citygml.bridge.BridgeRoom;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallation;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.AbstractOpening;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingModuleComponent;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.CeilingSurface;
import org.citygml4j.model.citygml.building.ClosureSurface;
import org.citygml4j.model.citygml.building.Door;
import org.citygml4j.model.citygml.building.FloorSurface;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorFurnitureProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.InteriorWallSurface;
import org.citygml4j.model.citygml.building.OpeningProperty;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.Room;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.building.Window;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityfurniture.CityFurnitureModuleComponent;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupMember;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupModuleComponent;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroupParent;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.AbstractSite;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.citygml.core.CoreModuleComponent;
import org.citygml4j.model.citygml.core.ExternalObject;
import org.citygml4j.model.citygml.core.ExternalReference;
import org.citygml4j.model.citygml.core.GeneralizationRelation;
import org.citygml4j.model.citygml.core.ImplicitGeometry;
import org.citygml4j.model.citygml.core.ImplicitRepresentationProperty;
import org.citygml4j.model.citygml.core.RelativeToTerrain;
import org.citygml4j.model.citygml.core.RelativeToWater;
import org.citygml4j.model.citygml.core.TransformationMatrix2x2;
import org.citygml4j.model.citygml.core.TransformationMatrix3x4;
import org.citygml4j.model.citygml.core.TransformationMatrix4x4;
import org.citygml4j.model.citygml.core.XalAddressProperty;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericAttributeSet;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.generics.GenericsModuleComponent;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.MeasureAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.landuse.LandUseModuleComponent;
import org.citygml4j.model.citygml.relief.AbstractReliefComponent;
import org.citygml4j.model.citygml.relief.BreaklineRelief;
import org.citygml4j.model.citygml.relief.GridProperty;
import org.citygml4j.model.citygml.relief.MassPointRelief;
import org.citygml4j.model.citygml.relief.RasterRelief;
import org.citygml4j.model.citygml.relief.ReliefComponentProperty;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.relief.ReliefModuleComponent;
import org.citygml4j.model.citygml.relief.TINRelief;
import org.citygml4j.model.citygml.relief.TinProperty;
import org.citygml4j.model.citygml.texturedsurface.TexturedSurfaceModuleComponent;
import org.citygml4j.model.citygml.texturedsurface._AbstractAppearance;
import org.citygml4j.model.citygml.texturedsurface._AppearanceProperty;
import org.citygml4j.model.citygml.texturedsurface._Color;
import org.citygml4j.model.citygml.texturedsurface._Material;
import org.citygml4j.model.citygml.texturedsurface._SimpleTexture;
import org.citygml4j.model.citygml.texturedsurface._TextureType;
import org.citygml4j.model.citygml.texturedsurface._TexturedSurface;
import org.citygml4j.model.citygml.transportation.AbstractTransportationObject;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.Railway;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.Square;
import org.citygml4j.model.citygml.transportation.Track;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.transportation.TransportationModuleComponent;
import org.citygml4j.model.citygml.tunnel.AbstractTunnel;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallation;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorHollowSpaceProperty;
import org.citygml4j.model.citygml.tunnel.Tunnel;
import org.citygml4j.model.citygml.tunnel.TunnelFurniture;
import org.citygml4j.model.citygml.tunnel.TunnelInstallation;
import org.citygml4j.model.citygml.tunnel.TunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelModuleComponent;
import org.citygml4j.model.citygml.tunnel.TunnelPart;
import org.citygml4j.model.citygml.tunnel.TunnelPartProperty;
import org.citygml4j.model.citygml.vegetation.AbstractVegetationObject;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.vegetation.VegetationModuleComponent;
import org.citygml4j.model.citygml.waterbody.AbstractWaterBoundarySurface;
import org.citygml4j.model.citygml.waterbody.AbstractWaterObject;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.citygml.waterbody.WaterBodyModuleComponent;
import org.citygml4j.model.citygml.waterbody.WaterClosureSurface;
import org.citygml4j.model.citygml.waterbody.WaterGroundSurface;
import org.citygml4j.model.citygml.waterbody.WaterSurface;
import org.citygml4j.model.common.association.Associable;
import org.citygml4j.model.common.base.ModelClassEnum;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.GML;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.base.ArrayAssociation;
import org.citygml4j.model.gml.base.AssociationAttributeGroup;
import org.citygml4j.model.gml.base.AssociationByRep;
import org.citygml4j.model.gml.base.AssociationByRepOrRef;
import org.citygml4j.model.gml.base.MetaData;
import org.citygml4j.model.gml.base.MetaDataProperty;
import org.citygml4j.model.gml.base.StandardObjectProperties;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.BooleanOrNull;
import org.citygml4j.model.gml.basicTypes.BooleanOrNullList;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.CodeOrNullList;
import org.citygml4j.model.gml.basicTypes.Coordinates;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.DoubleOrNullList;
import org.citygml4j.model.gml.basicTypes.IntegerOrNull;
import org.citygml4j.model.gml.basicTypes.IntegerOrNullList;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.citygml4j.model.gml.basicTypes.NameOrNull;
import org.citygml4j.model.gml.basicTypes.Null;
import org.citygml4j.model.gml.coverage.AbstractCoverage;
import org.citygml4j.model.gml.coverage.AbstractDiscreteCoverage;
import org.citygml4j.model.gml.coverage.CoverageFunction;
import org.citygml4j.model.gml.coverage.DataBlock;
import org.citygml4j.model.gml.coverage.DomainSet;
import org.citygml4j.model.gml.coverage.File;
import org.citygml4j.model.gml.coverage.FileValueModel;
import org.citygml4j.model.gml.coverage.GridFunction;
import org.citygml4j.model.gml.coverage.IncrementOrder;
import org.citygml4j.model.gml.coverage.IndexMap;
import org.citygml4j.model.gml.coverage.RangeParameters;
import org.citygml4j.model.gml.coverage.RangeSet;
import org.citygml4j.model.gml.coverage.RectifiedGridCoverage;
import org.citygml4j.model.gml.coverage.RectifiedGridDomain;
import org.citygml4j.model.gml.coverage.SequenceRule;
import org.citygml4j.model.gml.coverage.SequenceRuleNames;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.feature.AbstractFeatureCollection;
import org.citygml4j.model.gml.feature.BoundingShape;
import org.citygml4j.model.gml.feature.FeatureArrayProperty;
import org.citygml4j.model.gml.feature.FeatureMember;
import org.citygml4j.model.gml.feature.FeatureProperty;
import org.citygml4j.model.gml.feature.LocationProperty;
import org.citygml4j.model.gml.feature.PriorityLocationProperty;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryArrayProperty;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.InlineGeometryProperty;
import org.citygml4j.model.gml.geometry.SRSInformationGroup;
import org.citygml4j.model.gml.geometry.SRSReferenceGroup;
import org.citygml4j.model.gml.geometry.aggregates.AbstractGeometricAggregate;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurve;
import org.citygml4j.model.gml.geometry.aggregates.MultiCurveProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiGeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiLineString;
import org.citygml4j.model.gml.geometry.aggregates.MultiLineStringProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPoint;
import org.citygml4j.model.gml.geometry.aggregates.MultiPointProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiPolygonProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurve;
import org.citygml4j.model.gml.geometry.complexes.CompositeCurveProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSolidProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplex;
import org.citygml4j.model.gml.geometry.complexes.GeometricComplexProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurve;
import org.citygml4j.model.gml.geometry.primitives.AbstractCurveSegment;
import org.citygml4j.model.gml.geometry.primitives.AbstractGeometricPrimitive;
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
import org.citygml4j.model.gml.geometry.primitives.GeometricPrimitiveProperty;
import org.citygml4j.model.gml.geometry.primitives.InnerBoundaryIs;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.LineStringProperty;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegment;
import org.citygml4j.model.gml.geometry.primitives.LineStringSegmentArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.LinearRingProperty;
import org.citygml4j.model.gml.geometry.primitives.OrientableCurve;
import org.citygml4j.model.gml.geometry.primitives.OrientableSurface;
import org.citygml4j.model.gml.geometry.primitives.OuterBoundaryIs;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.PointRep;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.PolygonProperty;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRep;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRepOrCoord;
import org.citygml4j.model.gml.geometry.primitives.Rectangle;
import org.citygml4j.model.gml.geometry.primitives.Ring;
import org.citygml4j.model.gml.geometry.primitives.Sign;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.Surface;
import org.citygml4j.model.gml.geometry.primitives.SurfaceArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceInterpolation;
import org.citygml4j.model.gml.geometry.primitives.SurfacePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Tin;
import org.citygml4j.model.gml.geometry.primitives.Triangle;
import org.citygml4j.model.gml.geometry.primitives.TrianglePatchArrayProperty;
import org.citygml4j.model.gml.geometry.primitives.TriangulatedSurface;
import org.citygml4j.model.gml.geometry.primitives.Vector;
import org.citygml4j.model.gml.grids.Grid;
import org.citygml4j.model.gml.grids.GridEnvelope;
import org.citygml4j.model.gml.grids.GridLimits;
import org.citygml4j.model.gml.grids.RectifiedGrid;
import org.citygml4j.model.gml.measures.Length;
import org.citygml4j.model.gml.measures.Speed;
import org.citygml4j.model.gml.valueObjects.CategoryExtent;
import org.citygml4j.model.gml.valueObjects.CompositeValue;
import org.citygml4j.model.gml.valueObjects.CountExtent;
import org.citygml4j.model.gml.valueObjects.GenericValueObject;
import org.citygml4j.model.gml.valueObjects.QuantityExtent;
import org.citygml4j.model.gml.valueObjects.ScalarValue;
import org.citygml4j.model.gml.valueObjects.ScalarValueList;
import org.citygml4j.model.gml.valueObjects.Value;
import org.citygml4j.model.gml.valueObjects.ValueArray;
import org.citygml4j.model.gml.valueObjects.ValueArrayProperty;
import org.citygml4j.model.gml.valueObjects.ValueExtent;
import org.citygml4j.model.gml.valueObjects.ValueObject;
import org.citygml4j.model.gml.valueObjects.ValueProperty;
import org.citygml4j.model.gml.xlink.XLinkActuate;
import org.citygml4j.model.gml.xlink.XLinkShow;
import org.citygml4j.model.gml.xlink.XLinkType;
import org.citygml4j.model.module.AbstractModule;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.citygml.AbstractCityGMLModule;
import org.citygml4j.model.module.citygml.AppearanceModule;
import org.citygml4j.model.module.citygml.BridgeModule;
import org.citygml4j.model.module.citygml.BuildingModule;
import org.citygml4j.model.module.citygml.CityFurnitureModule;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityObjectGroupModule;
import org.citygml4j.model.module.citygml.CoreModule;
import org.citygml4j.model.module.citygml.GenericsModule;
import org.citygml4j.model.module.citygml.LandUseModule;
import org.citygml4j.model.module.citygml.ReliefModule;
import org.citygml4j.model.module.citygml.TexturedSurfaceModule;
import org.citygml4j.model.module.citygml.TransportationModule;
import org.citygml4j.model.module.citygml.TunnelModule;
import org.citygml4j.model.module.citygml.VegetationModule;
import org.citygml4j.model.module.citygml.WaterBodyModule;
import org.citygml4j.model.module.gml.AbstractGMLModule;
import org.citygml4j.model.module.gml.GMLCoreModule;
import org.citygml4j.model.module.gml.XLinkModule;
import org.citygml4j.model.module.xal.AbstractXALModule;
import org.citygml4j.model.module.xal.XALCoreModule;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.AddressIdentifier;
import org.citygml4j.model.xal.AddressLatitude;
import org.citygml4j.model.xal.AddressLatitudeDirection;
import org.citygml4j.model.xal.AddressLine;
import org.citygml4j.model.xal.AddressLines;
import org.citygml4j.model.xal.AddressLongitude;
import org.citygml4j.model.xal.AddressLongitudeDirection;
import org.citygml4j.model.xal.AdministrativeArea;
import org.citygml4j.model.xal.AdministrativeAreaName;
import org.citygml4j.model.xal.Barcode;
import org.citygml4j.model.xal.BuildingName;
import org.citygml4j.model.xal.Country;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.CountryNameCode;
import org.citygml4j.model.xal.Department;
import org.citygml4j.model.xal.DepartmentName;
import org.citygml4j.model.xal.DependentLocality;
import org.citygml4j.model.xal.DependentLocalityName;
import org.citygml4j.model.xal.DependentLocalityNumber;
import org.citygml4j.model.xal.DependentThoroughfare;
import org.citygml4j.model.xal.EndorsementLineCode;
import org.citygml4j.model.xal.Firm;
import org.citygml4j.model.xal.FirmName;
import org.citygml4j.model.xal.GrPostal;
import org.citygml4j.model.xal.KeyLineCode;
import org.citygml4j.model.xal.LargeMailUser;
import org.citygml4j.model.xal.LargeMailUserIdentifier;
import org.citygml4j.model.xal.LargeMailUserName;
import org.citygml4j.model.xal.Locality;
import org.citygml4j.model.xal.LocalityName;
import org.citygml4j.model.xal.MailStop;
import org.citygml4j.model.xal.MailStopName;
import org.citygml4j.model.xal.MailStopNumber;
import org.citygml4j.model.xal.PostBox;
import org.citygml4j.model.xal.PostBoxNumber;
import org.citygml4j.model.xal.PostBoxNumberExtension;
import org.citygml4j.model.xal.PostBoxNumberPrefix;
import org.citygml4j.model.xal.PostBoxNumberSuffix;
import org.citygml4j.model.xal.PostOffice;
import org.citygml4j.model.xal.PostOfficeName;
import org.citygml4j.model.xal.PostOfficeNumber;
import org.citygml4j.model.xal.PostTown;
import org.citygml4j.model.xal.PostTownName;
import org.citygml4j.model.xal.PostTownSuffix;
import org.citygml4j.model.xal.PostalCode;
import org.citygml4j.model.xal.PostalCodeNumber;
import org.citygml4j.model.xal.PostalCodeNumberExtension;
import org.citygml4j.model.xal.PostalRoute;
import org.citygml4j.model.xal.PostalRouteName;
import org.citygml4j.model.xal.PostalRouteNumber;
import org.citygml4j.model.xal.PostalServiceElements;
import org.citygml4j.model.xal.Premise;
import org.citygml4j.model.xal.PremiseLocation;
import org.citygml4j.model.xal.PremiseName;
import org.citygml4j.model.xal.PremiseNumber;
import org.citygml4j.model.xal.PremiseNumberPrefix;
import org.citygml4j.model.xal.PremiseNumberRange;
import org.citygml4j.model.xal.PremiseNumberRangeFrom;
import org.citygml4j.model.xal.PremiseNumberRangeTo;
import org.citygml4j.model.xal.PremiseNumberSuffix;
import org.citygml4j.model.xal.SortingCode;
import org.citygml4j.model.xal.SubAdministrativeArea;
import org.citygml4j.model.xal.SubAdministrativeAreaName;
import org.citygml4j.model.xal.SubPremise;
import org.citygml4j.model.xal.SubPremiseLocation;
import org.citygml4j.model.xal.SubPremiseName;
import org.citygml4j.model.xal.SubPremiseNumber;
import org.citygml4j.model.xal.SubPremiseNumberPrefix;
import org.citygml4j.model.xal.SubPremiseNumberSuffix;
import org.citygml4j.model.xal.SupplementaryPostalServiceData;
import org.citygml4j.model.xal.Thoroughfare;
import org.citygml4j.model.xal.ThoroughfareLeadingType;
import org.citygml4j.model.xal.ThoroughfareName;
import org.citygml4j.model.xal.ThoroughfareNumber;
import org.citygml4j.model.xal.ThoroughfareNumberFrom;
import org.citygml4j.model.xal.ThoroughfareNumberFromContent;
import org.citygml4j.model.xal.ThoroughfareNumberOrRange;
import org.citygml4j.model.xal.ThoroughfareNumberPrefix;
import org.citygml4j.model.xal.ThoroughfareNumberRange;
import org.citygml4j.model.xal.ThoroughfareNumberSuffix;
import org.citygml4j.model.xal.ThoroughfareNumberTo;
import org.citygml4j.model.xal.ThoroughfareNumberToContent;
import org.citygml4j.model.xal.ThoroughfarePostDirection;
import org.citygml4j.model.xal.ThoroughfarePreDirection;
import org.citygml4j.model.xal.ThoroughfareTrailingType;
import org.citygml4j.model.xal.XAL;
import org.citygml4j.model.xal.XALClass;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.MissingADESchemaException;
import org.citygml4j.xml.io.reader.UnmarshalException;
import org.citygml4j.xml.io.reader.XMLChunk;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.rtree.RTreeIndex;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.w3c.dom.Element;

import mapper.EnumClasses.GMLRelTypes;
import matcher.Matcher.TmpRelTypes;
import util.GeometryUtil;
import util.GraphUtil;
import util.ProducerConsumerUtil.XMLChunkConsumer;
import util.ProducerConsumerUtil.XMLChunkProducer;
import util.SETTINGS;
import util.SETTINGS.MatchingStrategies;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class Mapper implements MappingComponent {
	// Internal auxiliary properties while mapping
	// -> should be ignored while matching properties
	public enum InternalMappingProperties {
		BOUNDING_SHAPE_CREATED("boundingShapeCreated"),;

		private final String text;

		private InternalMappingProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	/*
	 * Tile node labels
	 */
	public enum TileNodes implements ModelClassEnum {
		ROOT_OLD_INNER_TILE,
		ROOT_NEW_INNER_TILE,
		INNER_TILE,

		ROOT_OLD_VERTICAL_BORDER,
		ROOT_NEW_VERTICAL_BORDER,
		VERTICAL_BORDER,

		ROOT_OLD_HORIZONTAL_BORDER,
		ROOT_NEW_HORIZONTAL_BORDER,
		HORIZONTAL_BORDER,

		ROOT_OLD_CROSS_BORDER,
		ROOT_NEW_CROSS_BORDER,
		CROSS_BORDER,
	}

	public enum TileOrBorderRootNodeProperties {
		SIZE_X("size_x"),
		SIZE_Y("size_y"),;

		private final String text;

		private TileOrBorderRootNodeProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum InnerTileNodeProperties {
		TILE_ID("id"),
		INDEX_X("index_x"),
		INDEX_Y("index_y"),
		LOWER_VALUE_X("lower_value_x"),
		LOWER_VALUE_Y("lower_value_y");

		private final String text;

		private InnerTileNodeProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum VerticalBorderNodeProperties {
		BORDER_ID("id"),
		INDEX_X("index_x"),
		INDEX_Y("index_y"),
		LOWER_VALUE_Y("lower_value_y"),
		UPPER_VALUE_Y("upper_value_y");

		private final String text;

		private VerticalBorderNodeProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum HorizontalBorderNodeProperties {
		BORDER_ID("id"),
		INDEX_X("index_x"),
		INDEX_Y("index_y"),
		LOWER_VALUE_X("lower_value_x"),
		UPPER_VALUE_X("upper_value_x");

		private final String text;

		private HorizontalBorderNodeProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum CrossBorderNodeProperties {
		BORDER_ID("id"),
		INDEX_X("index_x"),
		INDEX_Y("index_y"),
		VALUE_X("value_x"),
		VALUE_Y("value_y");

		private final String text;

		private CrossBorderNodeProperties(final String text) {
			this.text = text;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	// for matching in tiles
	private Node tileRootNode;

	// for matching using an RTree
	SpatialDatabaseService spatialDb;
	EditableLayer buildingLayer;

	private boolean enableLogger = true;
	private boolean enableParent = true;

	private GraphDatabaseService graphDb;
	private Logger logger;
	private boolean isOld;

	private ArrayList<ArrayList<Node>> tiles;

	private boolean cityBoundedByMissing = false;
	private double cityEnvelopeLowerX = Double.MAX_VALUE;
	private double cityEnvelopeLowerY = Double.MAX_VALUE;
	private double cityEnvelopeUpperX = Double.MIN_VALUE;
	private double cityEnvelopeUpperY = Double.MIN_VALUE;
	private double cityGridStartX; // eg. if cityEnvelopeLowerX = 5 and SETTINGS.TILE_UNIT_X = 2 then cityGridStartX = 4
	private double cityGridStartY;
	private int cityEnvelopeSizeX = 0;
	private int cityEnvelopeSizeY = 0;

	// private static HashMap<String, Long> stats = new HashMap<String, Long>(); // statistics

	private ConcurrentHashMap<String, ArrayList<Node>> hrefIndex; // a href can be referenced multiple times
	private ConcurrentHashMap<String, Node> idIndex;

	private IndexManager indexManager; // indices for faster querying
	private Index<Node> hrefApiIndex;
	private Index<Node> idApiIndex;
	// private HashSet<String> hrefValues;

	private long countTrans = 0;
	private long countFeatures = 0;
	private Transaction mapperTx;
	private ExecutorService service;

	public Mapper(GraphDatabaseService graphDb, Logger logger, boolean isOld) {
		this.graphDb = graphDb;
		this.logger = logger;
		this.isOld = isOld;
		logger.info("\n-------------------------------------------------"
				+ "\nINITIALIZING MAPPING COMPONENT FOR " + (isOld ? "OLD " : "NEW ") + "CITY MODEL"
				+ "\n-------------------------------------------------");

		if (SETTINGS.ENABLE_INDICES) {
			logger.info("Created indices on IDs and hrefs");
			indexManager = graphDb.index();
			hrefApiIndex = indexManager.forNodes("hrefs");
			idApiIndex = indexManager.forNodes("ids");
			// hrefValues = new HashSet<String>();
		} else {
			hrefIndex = new ConcurrentHashMap<String, ArrayList<Node>>();
			idIndex = new ConcurrentHashMap<String, Node>();
		}
	}

	public boolean isOld() {
		return isOld;
	}

	public void setOld(boolean isOld) {
		this.isOld = isOld;
	}

	public boolean isEnableLogger() {
		return enableLogger;
	}

	public void setEnableLogger(boolean enableLogger) {
		this.enableLogger = enableLogger;
	}

	public boolean isEnableParent() {
		return enableParent;
	}

	public void setEnableParent(boolean enableParent) {
		this.enableParent = enableParent;
	}

	public long getCountFeatures() {
		return countFeatures;
	}

	public Transaction getMapperTx() {
		return mapperTx;
	}

	public void setMapperTx(Transaction mapperTx) {
		this.mapperTx = mapperTx;
	}

	public Node createNodeWithLabel(ModelClassEnum label) {
		return createNodeWithLabel(label + "");
	}

	private Node createNodeWithLabel(String label) {
		Node node = graphDb.createNode();
		node.addLabel(Label.label(label));
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "... processing " + label + " ...");
		}

		// statistics
		// if (stats.containsKey(label)) {
		// Long oldValue = stats.get(label);
		// stats.replace(label, oldValue, oldValue + 1);
		// } else {
		// stats.put(label, new Long(1));
		// }

		return node;
	}

	// public static HashMap<String, Long> getStats() {
	// return stats;
	// }

	/*
	 * 
	 */
	public void mapperInit(CityGMLReader reader, Node mapperRootNode) throws CityGMLReadException, InterruptedException {
		if (SETTINGS.ENABLE_MULTI_THREADED_MAPPING) {
			mapperInitMultiThreaded(reader, mapperRootNode);
		} else {
			mapperInitSingleThreaded(reader, mapperRootNode);
		}
	}

	public void mapperInitMultiThreaded(CityGMLReader reader, Node mapperRootNode) throws CityGMLReadException, InterruptedException {
		// create a fixed thread pool
		int nThreads = Runtime.getRuntime().availableProcessors() * 2;
		logger.info("... setting up thread pool with " + nThreads + " threads ...");
		logger.info("INFO: p<i>-t<j> means pool<i>-thread<j>, example: p2t1 means pool2-thread1");
		service = Executors.newFixedThreadPool(nThreads);

		countFeatures = 0;

		/*
		 * Classic approach
		 */
		// while (reader.hasNext()) {
		// countFeatures++;
		//
		// final XMLChunk chunk = reader.nextChunk();
		// final long countFeaturesInner = countFeatures;
		//
		// service.execute(new Runnable() {
		// @Override
		// public void run() {
		// try (Transaction tx = graphDb.beginTx()) {
		//
		// CityGML cityGml = chunk.unmarshal();
		//
		// // if (cityGml.getCityGMLClass().equals(CityGMLClass.BUILDING)) {
		// // logger.info("Found BUILDING " + ((Building) cityGml).getId());
		// // } else if (logger.isLoggable(Level.FINE)) {
		// // logger.log(Level.FINE, "Found " + cityGml.getCityGMLClass());
		// // }
		//
		// if (cityGml.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
		// createNodeSearchHierarchy(cityGml, mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
		// } else {
		// createNodeSearchHierarchy(cityGml, null, GMLRelTypes.HREF_FEATURE);
		// }
		//
		// if (countFeaturesInner % SETTINGS.NR_OF_COMMIT_FEATURES == 0) {
		// logger.info("Found features: " + countFeaturesInner);
		// }
		// tx.success();
		// } catch (UnmarshalException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (MissingADESchemaException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// });
		// }

		/*
		 * GraphAware framework approach
		 */
		// BatchTransactionExecutor batchExecutor = new IterableInputBatchTransactionExecutor<>(
		// graphDb,
		// SETTINGS.NR_OF_COMMIT_FEATURES,
		// new TransactionalInput<>(graphDb, SETTINGS.NR_OF_COMMIT_FEATURES, new TransactionCallback<Iterable<XMLChunk>>() {
		// @Override
		// public Iterable<XMLChunk> doInTransaction(GraphDatabaseService database) throws Exception {
		// List<XMLChunk> chunks = new ArrayList<XMLChunk>();
		//
		// while (reader.hasNext()) {
		// chunks.add(reader.nextChunk());
		// }
		//
		// return chunks;
		// }
		// }),
		// new UnitOfWork<XMLChunk>() {
		// @Override
		// public void execute(GraphDatabaseService database, XMLChunk chunk, int batchNumber, int stepNumber) {
		// try {
		// CityGML cityGml = chunk.unmarshal();
		//
		// if (cityGml.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
		// createNodeSearchHierarchy(cityGml, mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
		// } else {
		// createNodeSearchHierarchy(cityGml, null, GMLRelTypes.HREF_FEATURE);
		// }
		//
		// if (stepNumber % batchNumber == 0) {
		// logger.info("Found features: " + stepNumber);
		// }
		// } catch (UnmarshalException | MissingADESchemaException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// });
		//
		// BatchTransactionExecutor multiThreadedExecutor = new MultiThreadedBatchTransactionExecutor((IterableInputBatchTransactionExecutor<?>) batchExecutor, 8);
		// multiThreadedExecutor.execute();

		/*
		 * Load chunks in a buffer approach
		 */
		// while (reader.hasNext()) {
		// ArrayList<XMLChunk> chunks = new ArrayList<XMLChunk>(SETTINGS.NR_OF_COMMIT_FEATURES);
		//
		// // until batch is full or no more features left
		// for (int i = 0; i < SETTINGS.NR_OF_COMMIT_FEATURES && reader.hasNext(); i++) {
		// countFeatures++;
		// chunks.add(reader.nextChunk());
		// }
		//
		// final ArrayList<XMLChunk> innerChunks = chunks;
		// final long innerCount = countFeatures;
		// final Node mapperRootNodeInner = mapperRootNode;
		//
		// service.execute(new Runnable() {
		// @Override
		// public void run() {
		// try (Transaction tx = graphDb.beginTx()) {
		// for (XMLChunk chunk : innerChunks) {
		// CityGML cityGml = chunk.unmarshal();
		//
		// // if (cityGml.getCityGMLClass().equals(CityGMLClass.BUILDING)) {
		// // logger.info("Found BUILDING " + ((Building) cityGml).getId());
		// // } else if (logger.isLoggable(Level.FINE)) {
		// // logger.log(Level.FINE, "Found " + cityGml.getCityGMLClass());
		// // }
		//
		// if (cityGml.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
		// createNodeSearchHierarchy(cityGml, mapperRootNodeInner, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
		// } else {
		// createNodeSearchHierarchy(cityGml, null, GMLRelTypes.HREF_FEATURE);
		// }
		// }
		//
		// logger.info("Features found: " + innerCount);
		// tx.success();
		//
		// } catch (UnmarshalException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (MissingADESchemaException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// });
		// }

		/*
		 * Producer-consumer approach
		 */
		XMLChunkConsumer.resetCounter();

		// the poison pill approach only reliably works in unbounded blocking queues
		BlockingQueue<XMLChunk> queue = new LinkedBlockingQueue<XMLChunk>(3 * SETTINGS.NR_OF_PRODUCERS * SETTINGS.CONSUMERS_PRO_PRODUCER * SETTINGS.NR_OF_COMMIT_FEATURES);

		for (int i = 0; i < SETTINGS.NR_OF_PRODUCERS; i++) {
			Thread producer = new Thread(new XMLChunkProducer(reader, queue));
			service.execute(producer);

			for (int j = 0; j < SETTINGS.CONSUMERS_PRO_PRODUCER; j++) {
				Thread consumer = new Thread(new XMLChunkConsumer(queue, graphDb, mapperRootNode, this, isOld, logger));
				service.execute(consumer);
			}
		}

		// wait for all threads to finish
		// logger.info("... shutting down threadpool ...");
		service.shutdown();
		service.awaitTermination(SETTINGS.THREAD_TIME_OUT, TimeUnit.SECONDS);
	}

	public void mapperInitSingleThreaded(CityGMLReader reader, Node mapperRootNode) throws CityGMLReadException, InterruptedException {
		countFeatures = 0;

		mapperTx = graphDb.beginTx();

		try {
			while (reader.hasNext()) {

				countFeatures++;

				if (countFeatures % SETTINGS.NR_OF_COMMIT_FEATURES == 0) {
					logger.info((SETTINGS.SPLIT_PER_COLLECTION_MEMBER ? "Buildings" : "Features") + " found: " + countFeatures);
					mapperTx.success();
					mapperTx.close();
					mapperTx = graphDb.beginTx();
				}

				// whereas the nextFeature() method of a CityGML reader completely unmarshals the
				// XML chunk to an instance of the citygml4j object model and optionally validates
				// it before returning, the nextChunk() method returns faster but only provides a
				// set of SAX events.
				final XMLChunk chunk = reader.nextChunk();

				CityGML cityGml;

				try {
					cityGml = chunk.unmarshal();

					// if (cityGml.getCityGMLClass().equals(CityGMLClass.BUILDING)) {
					// logger.info("Found BUILDING " + ((Building) cityGml).getId());
					// } else if (logger.isLoggable(Level.FINE)) {
					// logger.log(Level.FINE, "Found " + cityGml.getCityGMLClass());
					// }

					if (cityGml.getCityGMLClass().equals(CityGMLClass.CITY_MODEL)) {
						createNodeSearchHierarchy(cityGml, mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
					} else {
						createNodeSearchHierarchy(cityGml, null, GMLRelTypes.HREF_FEATURE);
					}

				} catch (UnmarshalException | MissingADESchemaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			logger.info((SETTINGS.SPLIT_PER_COLLECTION_MEMBER ? "Buildings" : "Features") + " found: " + countFeatures);
			mapperTx.success();
		} finally {
			mapperTx.close();
		}

	}

	/*
	 * Post processing
	 */
	public void postProcessing(Node mapperRootNode, RelationshipType relType) throws InterruptedException {
		logger.info("Post processing city model");

		mapperTx = graphDb.beginTx();
		try {
			countTrans = 0;
			resolveXLinks();

			mapperTx.success();
		} finally {
			mapperTx.close();
		}

		// mapperTx = graphDb.beginTx();
		// try {
		// countTrans = 0;
		// cleanUpRoot(mapperRootNode, relType);
		//
		// mapperTx.success();
		// } finally {
		// mapperTx.close();
		// }

		// if city envelope is missing
		// -> first iterate through buildings, then create tiles
		// else create tiles then assign buildings while iterating
		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
			mapperTx = graphDb.beginTx();
			try {
				countTrans = 0;
				examineCityBoundingShape(mapperRootNode);

				if (!cityBoundedByMissing) {
					createTiles(mapperRootNode);
				}

				mapperTx.success();
			} finally {
				mapperTx.close();
			}
		} else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
			mapperTx = graphDb.beginTx();
			try {
				createRTreeLayer();

				mapperTx.success();
			} finally {
				mapperTx.close();
			}
		}

		// also assign buildings to tiles if city envelope is available, else after
		// OR also assign buildings to an RTree
		mapperTx = graphDb.beginTx();
		try {
			countTrans = 0;
			if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
				logger.info("Assigning buildings to their respective tiles ...");
			} else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
				logger.info("Storing buildings' locations in an RTree for spatial indexing ...");
			}

			calcBoundingShapes(mapperRootNode);

			if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
				// save image of RTree
				DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_");
				String dateString = dateFormat.format(new Date());

				String tmpLog = "Exporting RTree signatures as images ...\n";
				String imageName = SETTINGS.RTREE_IMAGE_LOCATION + dateString + (isOld ? "old" : "new") + "_city_model_M" + SETTINGS.MAX_RTREE_NODE_REFERENCES + ".png";
				
				// redirect System.out.print/ln to logger
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				PrintStream old = System.out;
				System.setOut(ps);
				GraphUtil.exportRTreeImage(buildingLayer, graphDb, imageName, logger);
				System.out.flush();
				System.setOut(old);
				String[] tmpLines = baos.toString().split("\n");
				for (int i = 0; i < tmpLines.length; i++) {
					tmpLog += String.format("%20s", "") + tmpLines[i];
					if (i != tmpLines.length - 1) {
						tmpLog += "\n";
					}
				}

				logger.info(tmpLog);
			}

			mapperTx.success();
		} finally {
			mapperTx.close();
		}

		if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES) && cityBoundedByMissing) {
			mapperTx = graphDb.beginTx();
			try {
				countTrans = 0;

				logger.info("Calculated city envelope: [" + cityEnvelopeLowerX + ", " + cityEnvelopeLowerY + " -> " + cityEnvelopeUpperX + ", " + cityEnvelopeUpperY + "]");

				createTiles(mapperRootNode);
				assignBuildingsToTiles(mapperRootNode);

				mapperTx.success();
			} finally {
				mapperTx.close();
			}
		}
	}

	private void resolveXLinks() throws InterruptedException {
		logger.info("Resolving XLINKs ...");

		// if (SETTINGS.ENABLE_INDICES) {
		// for (String hrefValue : hrefValues) {
		// IndexHits<Node> hrefNodes = hrefApiIndex.get("href", hrefValue);
		//
		// for (Node hrefNode : hrefNodes) {
		// countTrans++;
		//
		// if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
		// // logger.info("PROCESS TRANSACTIONS: " + countTrans);
		// mapperTx.success();
		// mapperTx.close();
		// mapperTx = graphDb.beginTx();
		// }
		//
		// String id = hrefValue.replace("#", "");
		//
		// if (logger.isLoggable(Level.FINE)) {
		// logger.log(Level.FINE, "... resolving XLINK object, ID = " + id + " ...");
		// }
		//
		// Node realNode = GraphUtil.findNodeById(idApiIndex, id, logger);
		//
		// if (realNode != null) {
		// hrefNode.removeProperty("href");
		// hrefNode.createRelationshipTo(realNode, GMLRelTypes.OBJECT);
		// }
		//
		// // remove processed node
		// hrefApiIndex.remove(hrefNode);
		// }
		//
		// hrefNodes.close();
		// }
		//
		// // remove indices for each city model
		// hrefApiIndex.delete();
		// idApiIndex.delete();
		// hrefValues.clear();
		// }

		if (SETTINGS.ENABLE_INDICES) {
			IndexHits<Node> hrefNodes = hrefApiIndex.query("*:*");

			for (Node hrefNode : hrefNodes) {
				countTrans++;

				if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
					// logger.info("PROCESS TRANSACTIONS: " + countTrans);
					mapperTx.success();
					mapperTx.close();
					mapperTx = graphDb.beginTx();
				}

				String id = hrefNode.getProperty("href").toString().replace("#", "");

				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "... resolving XLINK object, ID = " + id + " ...");
				}

				Node realNode = GraphUtil.findNodeById(idApiIndex, id, logger);

				if (realNode != null) {
					hrefNode.removeProperty("href");
					hrefNode.createRelationshipTo(realNode, GMLRelTypes.OBJECT);
				}

				// remove processed node
				hrefApiIndex.remove(hrefNode);
			}

			hrefNodes.close();

			// remove indices for each city model
			hrefApiIndex.delete();
			idApiIndex.delete();
		} else {
			Iterator<Entry<String, ArrayList<Node>>> it = hrefIndex.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ArrayList<Node>> pair = (Map.Entry<String, ArrayList<Node>>) it.next();

				for (Node hrefNode : pair.getValue()) {
					countTrans++;

					if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
						// logger.info("PROCESS TRANSACTIONS: " + countTrans);
						mapperTx.success();
						mapperTx.close();
						mapperTx = graphDb.beginTx();
					}

					String id = pair.getKey().replace("#", "");

					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "... resolving XLINK object, ID = " + id + " ...");
					}

					Node realNode = GraphUtil.findNodeById(idIndex, id, logger);

					if (realNode != null) {
						hrefNode.removeProperty("href");
						hrefNode.createRelationshipTo(realNode, GMLRelTypes.OBJECT);
					}
				}

				it.remove(); // avoids a ConcurrentModificationException
			}

			// remove indices for each city model
			hrefIndex.clear();
			idIndex.clear();
		}
	}

	// due to reading CityGML files in chunks, separate features are marked with hrefs
	// the tester simply links ROOT_MAPPER to those feature nodes with the relationship OBJECT
	// the post-processing function should then delete these OBJECT relationships
	// with the only exception is that CityModel will have an OLD__CITY_MODEL/NEW_CITY_MODEL relationship
	private void cleanUpRoot(Node mapperRootNode, RelationshipType relType) throws InterruptedException {
		logger.info("Cleaning up temporary data ...");

		Iterable<Relationship> rels;
		for (Relationship rel : mapperRootNode.getRelationships(Direction.OUTGOING, GMLRelTypes.OBJECT)) {

			countTrans++;

			if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
				// logger.info("PROCESSED FEATURES: " + countTrans);
				mapperTx.success();
				mapperTx.close();
				mapperTx = graphDb.beginTx();
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "... deleting temporary data ...");
			}

			Node otherNode = rel.getOtherNode(mapperRootNode);
			rel.delete();

			if (otherNode.hasLabel(Label.label(CityGMLClass.CITY_MODEL + ""))) {
				mapperRootNode.createRelationshipTo(otherNode, relType);
			}
		}
	}

	private void examineCityBoundingShape(Node mapperRootNode) {
		// get envelop from the old/new city model
		Node boundingShape = GraphUtil.findFirstChildOfNode(
				GraphUtil.findFirstChildOfNode(mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL), GMLRelTypes.BOUNDED_BY);

		if (boundingShape == null) {
			cityBoundedByMissing = true;
			logger.warning("WARNING: CITY ENVELOPE NOT FOUND. AUTOMATIC CALCULATION OF NEW ENVELOPE IN PROGRESS ...");
			return;
		}

		Node envelopeNode = GraphUtil.findFirstChildOfNode(boundingShape, GMLRelTypes.ENVELOPE);

		if (envelopeNode == null) {
			cityBoundedByMissing = true;
			logger.warning("WARNING: CITY ENVELOPE NOT FOUND. AUTOMATIC CALCULATION OF NEW ENVELOPE IN PROGRESS ...");
			return;
		}

		cityBoundedByMissing = false;

		Envelope envelope = BoundingBoxCalculator.createEnvelope(envelopeNode);

		// get lower and upper corner from envelope
		double[][] lowerUpperCorner = GeometryUtil.getLowerUpperCorner(envelope, logger);
		double[] lowerCorner = lowerUpperCorner[0];
		double[] upperCorner = lowerUpperCorner[1];

		cityEnvelopeLowerX = lowerCorner[0];
		cityEnvelopeLowerY = lowerCorner[1];
		cityEnvelopeUpperX = upperCorner[0];
		cityEnvelopeUpperY = upperCorner[1];
	}

	/**
	 * Create a 2D array of tiles, each tile is a 2D defined by its lower X and Y.
	 * 
	 * @param mapperRootNode
	 */
	private void createTiles(Node mapperRootNode) {
		logger.info("Dividing city model into tiles ...");

		// set grid's start values
		// tiles must be in the same grid between two city models so that they can be synced together
		// also make a "buffer" boundary around the current grid -> 2 more columns and 2 more rows
		cityGridStartY = (((int) (cityEnvelopeLowerY / SETTINGS.TILE_UNIT_Y)) - (cityEnvelopeLowerY < 0 ? 1 : 0)) * SETTINGS.TILE_UNIT_Y - SETTINGS.TILE_UNIT_Y;
		cityGridStartX = (((int) (cityEnvelopeLowerX / SETTINGS.TILE_UNIT_X)) - (cityEnvelopeLowerX < 0 ? 1 : 0)) * SETTINGS.TILE_UNIT_X - SETTINGS.TILE_UNIT_X;

		// create (inner) tiles and borders
		tileRootNode = createNodeWithLabel((isOld ? TileNodes.ROOT_OLD_INNER_TILE : TileNodes.ROOT_NEW_INNER_TILE));
		tiles = new ArrayList<ArrayList<Node>>();

		// tile index
		int x = 0;
		int y = 0;

		double curY = cityGridStartY;
		while (curY < cityEnvelopeUpperY + SETTINGS.TILE_UNIT_Y) {

			// init each row
			tiles.add(new ArrayList<Node>());

			x = 0;

			// tiles must be in the same grid between two city models so that they can be synced together
			double curX = cityGridStartX;
			while (curX < cityEnvelopeUpperX + SETTINGS.TILE_UNIT_X) {

				countTrans++;

				if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
					// logger.info("Processed buildings: " + countTrans);
					mapperTx.success();
					mapperTx.close();
					mapperTx = graphDb.beginTx();
				}

				// create tile nodes
				Node tile = createNodeWithLabel(TileNodes.INNER_TILE);
				tile.setProperty(InnerTileNodeProperties.TILE_ID.toString(), "tile_" + y + "_" + x);
				tile.setProperty(InnerTileNodeProperties.INDEX_X.toString(), x);
				tile.setProperty(InnerTileNodeProperties.INDEX_Y.toString(), y);
				tile.setProperty(InnerTileNodeProperties.LOWER_VALUE_X.toString(), curX);
				tile.setProperty(InnerTileNodeProperties.LOWER_VALUE_Y.toString(), curY);
				tileRootNode.createRelationshipTo(tile, TmpRelTypes.CONSISTS_OF);
				tiles.get(y).add(tile);

				x++;
				curX += SETTINGS.TILE_UNIT_X;
			}

			y++;
			curY += SETTINGS.TILE_UNIT_Y;
		}

		// nr of tiles is 1 smaller than nr of cross borders
		cityEnvelopeSizeX = x;
		cityEnvelopeSizeY = y;

		// create (inner) tile and border root nodes
		tileRootNode.setProperty(TileOrBorderRootNodeProperties.SIZE_X.toString(), x);
		tileRootNode.setProperty(TileOrBorderRootNodeProperties.SIZE_Y.toString(), y);
	}

	private void createRTreeLayer() {
		spatialDb = new SpatialDatabaseService(graphDb);
		buildingLayer = (EditableLayer) spatialDb.getOrCreateEditableLayer((isOld ? "old" : "new") + "BuildingLayer");

		// set config to this layer
		Map<String, Object> config = new HashMap<String, Object>();
		config.put(RTreeIndex.KEY_MAX_NODE_REFERENCES.toString(), SETTINGS.MAX_RTREE_NODE_REFERENCES);
		buildingLayer.getIndex().configure(config);
	}

	// should only be called when city envelope is missing
	private void assignBuildingsToTiles(Node mapperRootNode) {
		Node cityModel = GraphUtil.findFirstChildOfNode(mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
		for (Node buildingNode : GraphUtil.findBuildings(cityModel)) {

			countTrans++;

			if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
				mapperTx.success();
				mapperTx.close();
				mapperTx = graphDb.beginTx();
			}

			Envelope envelope = BoundingBoxCalculator.createBoundingShape(GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.BOUNDED_BY)).getEnvelope();

			double[][] lowerUpperCorner = GeometryUtil.getLowerUpperCorner(envelope, logger);
			double[] lowerCorner = lowerUpperCorner[0];
			double[] upperCorner = lowerUpperCorner[1];

			assignBuildingsWithEnvelopeToTiles(lowerCorner, upperCorner, buildingNode);
		}
	}

	private void assignBuildingsWithEnvelopeToTiles(double[] lowerCorner, double[] upperCorner, Node buildingNode) {
		int indLowerY = (int) ((lowerCorner[1] - cityGridStartY) / SETTINGS.TILE_UNIT_Y);
		int indLowerX = (int) ((lowerCorner[0] - cityGridStartX) / SETTINGS.TILE_UNIT_X);

		// UNIT TILE X AND Y OF CITY MODEL MUST BE BIG ENOUGH SO THAT NO BUILDING IS LARGER THAN A TILE ITSELF!
		double deltaLeftX = lowerCorner[0] - (cityGridStartX + indLowerX * SETTINGS.TILE_UNIT_X);
		boolean reachesLeftVerticalBorder = deltaLeftX <= SETTINGS.TILE_BORDER_DISTANCE;

		double deltaRightX = (cityGridStartX + (indLowerX + 1) * SETTINGS.TILE_UNIT_X) - lowerCorner[0];
		boolean reachesRightVerticalBorder = deltaRightX <= SETTINGS.TILE_BORDER_DISTANCE;

		double deltaDownY = lowerCorner[1] - (cityGridStartY + indLowerY * SETTINGS.TILE_UNIT_Y);
		boolean reachesDownHorzizontalBorder = deltaDownY <= SETTINGS.TILE_BORDER_DISTANCE;

		double deltaUpY = (cityGridStartY + (indLowerY + 1) * SETTINGS.TILE_UNIT_Y) - lowerCorner[1];
		boolean reachesUpHorizontalBorder = deltaUpY <= SETTINGS.TILE_BORDER_DISTANCE;

		ArrayList<Node> curTileOrBorder = new ArrayList<Node>(4);

		if (reachesLeftVerticalBorder) {
			if (reachesRightVerticalBorder) {
				if (reachesDownHorzizontalBorder) {
					if (reachesUpHorizontalBorder) {
						// left right down up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX + 1));
					} else {
						// left right down !up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
					}
				} else {
					if (reachesUpHorizontalBorder) {
						// left right !down up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX + 1));
					} else {
						// left right !down !up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
					}
				}
			} else {
				if (reachesDownHorzizontalBorder) {
					if (reachesUpHorizontalBorder) {
						// left !right down up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
					} else {
						// left !right down !up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX - 1));
					}
				} else {
					if (reachesUpHorizontalBorder) {
						// left !right !down up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX - 1));
					} else {
						// left !right !down !up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX - 1));
					}
				}
			}
		} else {
			if (reachesRightVerticalBorder) {
				if (reachesDownHorzizontalBorder) {
					if (reachesUpHorizontalBorder) {
						// !left right down up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX + 1));
					} else {
						// !left right down !up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX + 1));
					}
				} else {
					if (reachesUpHorizontalBorder) {
						// !left right !down up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX + 1));
					} else {
						// !left right !down !up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX + 1));
					}
				}
			} else {
				if (reachesDownHorzizontalBorder) {
					if (reachesUpHorizontalBorder) {
						// !left !right down up: should not happen if tile is big enough
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
					} else {
						// !left !right down !up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY - 1).get(indLowerX));
					}
				} else {
					if (reachesUpHorizontalBorder) {
						// !left !right !down up
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
						curTileOrBorder.add(tiles.get(indLowerY + 1).get(indLowerX));
					} else {
						// !left !right !down !up: building is right in tile
						curTileOrBorder.add(tiles.get(indLowerY).get(indLowerX));
					}
				}
			}
		}

		for (Node node : curTileOrBorder) {
			node.createRelationshipTo(buildingNode, TmpRelTypes.TILE_CONSISTS_OF);
		}
	}

	/**
	 * If the input file was read feature by feature, buildings can not have their bounding shapes calculated while mapping.
	 * 
	 * Hence, this function calculates bounding shapes (if not available) for all buildings after they are mapped.
	 * 
	 * @param mapperRootNode
	 * @throws InterruptedException
	 */
	private void calcBoundingShapes(Node mapperRootNode) throws InterruptedException {
		logger.info("Calculating bounding shapes of buildings ...");

		// get buildings from the old/new city model
		Node cityModel = GraphUtil.findFirstChildOfNode(mapperRootNode, isOld ? GMLRelTypes.OLD_CITY_MODEL : GMLRelTypes.NEW_CITY_MODEL);
		for (Node buildingNode : GraphUtil.findBuildings(cityModel)) {

			countTrans++;

			if (countTrans % SETTINGS.NR_OF_COMMMIT_TRANS == 0) {
				// logger.info("Processed buildings: " + countTrans);
				mapperTx.success();
				mapperTx.close();
				mapperTx = graphDb.beginTx();
			}

			attachBoundingShapeToBuildingOrBuildingPart(buildingNode, new Building());
		}
	}

	private void attachBoundingShapeToBuildingOrBuildingPart(Node buildingNode, AbstractBuilding building) {
		if (GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.BOUNDED_BY) != null) {
			buildingNode.setProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "false");

			// BuildingPart
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.BUILDING_PART)) {
				for (Node n : GraphUtil.findChildrenOfNode(buildingNode, GMLRelTypes.BUILDING_PART)) {
					// also attach bounding shapes to building parts
					attachBoundingShapeToBuildingOrBuildingPart(GraphUtil.findFirstChildOfNode(n, GMLRelTypes.OBJECT), new BuildingPart());
				}
			}
		} else {
			buildingNode.setProperty(InternalMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "true");

			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "... calculating bounding shape of "
						+ buildingNode.getLabels().iterator().next().toString() + " "
						+ buildingNode.getProperty("id").toString() + " ...");
			}

			// LoD0
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD0_FOOT_PRINT)) {
				MultiSurfaceProperty lod0FootPrint = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD0_FOOT_PRINT));
				building.setLod0FootPrint(lod0FootPrint);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD0_ROOF_EDGE)) {
				MultiSurfaceProperty lod0RoofEdge = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD0_ROOF_EDGE));
				building.setLod0RoofEdge(lod0RoofEdge);
			}

			// LoD1-4 Solid
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD1_SOLID)) {
				SolidProperty lod1Solid = BoundingBoxCalculator.createSolidProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD1_SOLID));
				building.setLod1Solid(lod1Solid);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_SOLID)) {
				SolidProperty lod2Solid = BoundingBoxCalculator.createSolidProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD2_SOLID));
				building.setLod2Solid(lod2Solid);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_SOLID)) {
				SolidProperty lod3Solid = BoundingBoxCalculator.createSolidProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD3_SOLID));
				building.setLod3Solid(lod3Solid);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_SOLID)) {
				SolidProperty lod4Solid = BoundingBoxCalculator.createSolidProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD4_SOLID));
				building.setLod4Solid(lod4Solid);
			}

			// LoD1-4 MultiSurface
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD1_MULTI_SURFACE)) {
				MultiSurfaceProperty lod1MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD1_MULTI_SURFACE));
				building.setLod1MultiSurface(lod1MultiSurface);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_SURFACE)) {
				MultiSurfaceProperty lod2MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD2_MULTI_SURFACE));
				building.setLod2MultiSurface(lod2MultiSurface);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_SURFACE)) {
				MultiSurfaceProperty lod3MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD3_MULTI_SURFACE));
				building.setLod3MultiSurface(lod3MultiSurface);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_SURFACE)) {
				MultiSurfaceProperty lod4MultiSurface = BoundingBoxCalculator.createMultiSurfaceProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD4_MULTI_SURFACE));
				building.setLod4MultiSurface(lod4MultiSurface);
			}

			// LoD2-4 MultiCurve
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD2_MULTI_CURVE)) {
				MultiCurveProperty lod2MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD2_MULTI_CURVE));
				building.setLod2MultiCurve(lod2MultiCurve);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD3_MULTI_CURVE)) {
				MultiCurveProperty lod3MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD3_MULTI_CURVE));
				building.setLod3MultiCurve(lod3MultiCurve);
			}

			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.LOD4_MULTI_CURVE)) {
				MultiCurveProperty lod4MultiCurve = BoundingBoxCalculator.createMultiCurveProperty(
						GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.LOD4_MULTI_CURVE));
				building.setLod4MultiCurve(lod4MultiCurve);
			}

			// BOUNDED_BY_SURFACE
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY_SURFACE)) {
				for (Node n : GraphUtil.findChildrenOfNode(buildingNode, GMLRelTypes.BOUNDED_BY_SURFACE)) {
					BoundarySurfaceProperty boundarySurface = BoundingBoxCalculator.createBoundarySurfaceProperty(n);
					building.addBoundedBySurface(boundarySurface);
				}
			}

			// OuterBuildingInstallation
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
				for (Node n : GraphUtil.findChildrenOfNode(buildingNode, GMLRelTypes.OUTER_BUILDING_INSTALLATION)) {
					BuildingInstallationProperty outerBuildingInstallation = BoundingBoxCalculator.createBuildingInstallationProperty(n);
					building.addOuterBuildingInstallation(outerBuildingInstallation);
				}
			}

			// BuildingPart
			if (buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.BUILDING_PART)) {
				for (Node n : GraphUtil.findChildrenOfNode(buildingNode, GMLRelTypes.BUILDING_PART)) {
					BuildingPartProperty buildingPart = BoundingBoxCalculator.createBuildingPartProperty(n);
					building.addConsistsOfBuildingPart(buildingPart);

					// also attach bounding shapes to building parts
					attachBoundingShapeToBuildingOrBuildingPart(GraphUtil.findFirstChildOfNode(n, GMLRelTypes.OBJECT), new BuildingPart());
				}
			}

			if (!buildingNode.hasRelationship(Direction.OUTGOING, GMLRelTypes.BOUNDED_BY)) {
				createNode(building.calcBoundedBy(true), buildingNode, GMLRelTypes.BOUNDED_BY);
			}
		}

		if (building instanceof Building) {// exclude BuildingPart

			Envelope envelope = BoundingBoxCalculator.createBoundingShape(GraphUtil.findFirstChildOfNode(buildingNode, GMLRelTypes.BOUNDED_BY)).getEnvelope();

			double[][] lowerUpperCorner = GeometryUtil.getLowerUpperCorner(envelope, logger);
			double[] lowerCorner = lowerUpperCorner[0];
			double[] upperCorner = lowerUpperCorner[1];

			if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.TILES)) {
				// if city envelope is missing, find lower corners while iterating over buildings
				if (cityBoundedByMissing) {
					if (lowerCorner[0] < cityEnvelopeLowerX) {
						cityEnvelopeLowerX = lowerCorner[0];
					}

					if (lowerCorner[1] < cityEnvelopeLowerY) {
						cityEnvelopeLowerY = lowerCorner[1];
					}

					if (upperCorner[0] > cityEnvelopeUpperX) {
						cityEnvelopeUpperX = upperCorner[0];
					}

					if (upperCorner[1] > cityEnvelopeUpperY) {
						cityEnvelopeUpperY = upperCorner[1];
					}
				} else {
					assignBuildingsWithEnvelopeToTiles(lowerCorner, upperCorner, buildingNode);
				}
			} else if (SETTINGS.MATCHING_STRATEGY.equals(MatchingStrategies.RTREE)) {
				com.vividsolutions.jts.geom.Coordinate lowerCoordinate = new com.vividsolutions.jts.geom.Coordinate(lowerCorner[0], lowerCorner[1]);
				com.vividsolutions.jts.geom.Coordinate upperCoordinate = new com.vividsolutions.jts.geom.Coordinate(upperCorner[0], upperCorner[1]);

				com.vividsolutions.jts.geom.Envelope bbox = new com.vividsolutions.jts.geom.Envelope(lowerCoordinate, upperCoordinate);

				// add the bounding box of this building to the RTree
				Node geomNode = buildingLayer.add(buildingLayer.getGeometryFactory().toGeometry(bbox)).getGeomNode();
				// link the building node to this RTree node to retrieve data later
				geomNode.createRelationshipTo(buildingNode, TmpRelTypes.RTREE_DATA);
			}
		}
	}

	/*
	 * Auxiliary functions
	 */
	@Override
	public Node createNodeSearchHierarchy(Module content, Node parent, RelationshipType relType) {
		if (content instanceof AbstractModule) {
			if (content instanceof AbstractCityGMLModule) {
				if (content instanceof AppearanceModule)
					return createNode((AppearanceModule) content, parent, relType);
				if (content instanceof BridgeModule)
					return createNode((BridgeModule) content, parent, relType);
				if (content instanceof BuildingModule)
					return createNode((BuildingModule) content, parent, relType);
				if (content instanceof CityFurnitureModule)
					return createNode((CityFurnitureModule) content, parent, relType);
				if (content instanceof CityObjectGroupModule)
					return createNode((CityObjectGroupModule) content, parent, relType);
				if (content instanceof CoreModule)
					return createNode((CoreModule) content, parent, relType);
				if (content instanceof GenericsModule)
					return createNode((GenericsModule) content, parent, relType);
				if (content instanceof LandUseModule)
					return createNode((LandUseModule) content, parent, relType);
				if (content instanceof ReliefModule)
					return createNode((ReliefModule) content, parent, relType);
				if (content instanceof TexturedSurfaceModule)
					return createNode((TexturedSurfaceModule) content, parent, relType);
				if (content instanceof TransportationModule)
					return createNode((TransportationModule) content, parent, relType);
				if (content instanceof TunnelModule)
					return createNode((TunnelModule) content, parent, relType);
				if (content instanceof VegetationModule)
					return createNode((VegetationModule) content, parent, relType);
				if (content instanceof WaterBodyModule)
					return createNode((WaterBodyModule) content, parent, relType);
				return createNode((AbstractCityGMLModule) content, parent, relType);
			}

			if (content instanceof AbstractGMLModule) {
				if (content instanceof GMLCoreModule)
					return createNode((GMLCoreModule) content, parent, relType);
				if (content instanceof XLinkModule)
					return createNode((XLinkModule) content, parent, relType);
				return createNode((AbstractGMLModule) content, parent, relType);
			}

			if (content instanceof AbstractXALModule) {
				if (content instanceof XALCoreModule)
					return createNode((XALCoreModule) content, parent, relType);
				return createNode((AbstractXALModule) content, parent, relType);
			}

			return createNode((AbstractModule) content, parent, relType);
		}

		return null;
	}

	@Override
	public Node createNodeSearchHierarchy(ModelObject content, Node parent, RelationshipType relType) {
		if (content instanceof Associable) {
			if (content instanceof AbstractCurveSegment) {
				if (content instanceof LineStringSegment)
					return createNode((LineStringSegment) content, parent, relType);
				return createNode((AbstractCurveSegment) content, parent, relType);
			}

			if (content instanceof AbstractGML) {
				if (content instanceof _AbstractAppearance) {
					if (content instanceof _Material)
						return createNode((_Material) content, parent, relType);
					if (content instanceof _SimpleTexture)
						return createNode((_SimpleTexture) content, parent, relType);
					return createNode((_AbstractAppearance) content, parent, relType);
				}

				if (content instanceof AbstractFeature) {
					if (content instanceof AbstractCityObject) {
						if (content instanceof AbstractBoundarySurface) {
							if (content instanceof CeilingSurface)
								return createNode((CeilingSurface) content, parent, relType);
							if (content instanceof ClosureSurface)
								return createNode((ClosureSurface) content, parent, relType);
							if (content instanceof FloorSurface)
								return createNode((FloorSurface) content, parent, relType);
							if (content instanceof GroundSurface)
								return createNode((GroundSurface) content, parent, relType);
							if (content instanceof InteriorWallSurface)
								return createNode((InteriorWallSurface) content, parent, relType);
							if (content instanceof OuterCeilingSurface)
								return createNode((OuterCeilingSurface) content, parent, relType);
							if (content instanceof OuterFloorSurface)
								return createNode((OuterFloorSurface) content, parent, relType);
							if (content instanceof RoofSurface)
								return createNode((RoofSurface) content, parent, relType);
							if (content instanceof WallSurface)
								return createNode((WallSurface) content, parent, relType);
							return createNode((AbstractBoundarySurface) content, parent, relType);
						}

						if (content instanceof AbstractOpening) {
							if (content instanceof Door)
								return createNode((Door) content, parent, relType);
							if (content instanceof Window)
								return createNode((Window) content, parent, relType);
							return createNode((AbstractOpening) content, parent, relType);
						}

						if (content instanceof AbstractReliefComponent) {
							if (content instanceof BreaklineRelief)
								return createNode((BreaklineRelief) content, parent, relType);
							if (content instanceof MassPointRelief)
								return createNode((MassPointRelief) content, parent, relType);
							if (content instanceof RasterRelief)
								return createNode((RasterRelief) content, parent, relType);
							if (content instanceof TINRelief)
								return createNode((TINRelief) content, parent, relType);
							return createNode((AbstractReliefComponent) content, parent, relType);
						}

						if (content instanceof AbstractSite) {
							if (content instanceof AbstractBridge) {
								if (content instanceof Bridge)
									return createNode((Bridge) content, parent, relType);
								if (content instanceof BridgePart)
									return createNode((BridgePart) content, parent, relType);
								return createNode((AbstractBridge) content, parent, relType);
							}

							if (content instanceof AbstractBuilding) {
								if (content instanceof Building)
									return createNode((Building) content, parent, relType);
								if (content instanceof BuildingPart)
									return createNode((BuildingPart) content, parent, relType);
								return createNode((AbstractBuilding) content, parent, relType);
							}

							if (content instanceof AbstractTunnel) {
								if (content instanceof Tunnel)
									return createNode((Tunnel) content, parent, relType);
								if (content instanceof TunnelPart)
									return createNode((TunnelPart) content, parent, relType);
								return createNode((AbstractTunnel) content, parent, relType);
							}

							return createNode((AbstractSite) content, parent, relType);
						}

						if (content instanceof AbstractTransportationObject) {
							if (content instanceof AuxiliaryTrafficArea)
								return createNode((AuxiliaryTrafficArea) content, parent, relType);
							if (content instanceof TrafficArea)
								return createNode((TrafficArea) content, parent, relType);
							if (content instanceof TransportationComplex) {
								if (content instanceof Railway)
									return createNode((Railway) content, parent, relType);
								if (content instanceof Road)
									return createNode((Road) content, parent, relType);
								if (content instanceof Square)
									return createNode((Square) content, parent, relType);
								if (content instanceof Track)
									return createNode((Track) content, parent, relType);
								return createNode((TransportationComplex) content, parent, relType);
							}
							return createNode((AbstractTransportationObject) content, parent, relType);
						}

						if (content instanceof AbstractVegetationObject) {
							if (content instanceof PlantCover)
								return createNode((PlantCover) content, parent, relType);
							if (content instanceof SolitaryVegetationObject)
								return createNode((SolitaryVegetationObject) content, parent, relType);
							return createNode((AbstractVegetationObject) content, parent, relType);
						}

						if (content instanceof AbstractWaterBoundarySurface) {
							if (content instanceof WaterClosureSurface)
								return createNode((WaterClosureSurface) content, parent, relType);
							if (content instanceof WaterGroundSurface)
								return createNode((WaterGroundSurface) content, parent, relType);
							if (content instanceof WaterSurface)
								return createNode((WaterSurface) content, parent, relType);
							return createNode((AbstractWaterBoundarySurface) content, parent, relType);
						}

						if (content instanceof AbstractWaterObject) {
							if (content instanceof WaterBody)
								return createNode((WaterBody) content, parent, relType);
							return createNode((AbstractWaterObject) content, parent, relType);
						}

						if (content instanceof BridgeConstructionElement)
							return createNode((BridgeConstructionElement) content, parent, relType);

						if (content instanceof BridgeFurniture)
							return createNode((BridgeFurniture) content, parent, relType);

						if (content instanceof BridgeInstallation)
							return createNode((BridgeInstallation) content, parent, relType);

						if (content instanceof BridgeRoom)
							return createNode((BridgeRoom) content, parent, relType);

						if (content instanceof BuildingFurniture)
							return createNode((BuildingFurniture) content, parent, relType);

						if (content instanceof BuildingInstallation)
							return createNode((BuildingInstallation) content, parent, relType);

						if (content instanceof CityFurniture)
							return createNode((CityFurniture) content, parent, relType);

						if (content instanceof CityObjectGroup)
							return createNode((CityObjectGroup) content, parent, relType);

						if (content instanceof GenericCityObject)
							return createNode((GenericCityObject) content, parent, relType);

						if (content instanceof HollowSpace)
							return createNode((HollowSpace) content, parent, relType);

						if (content instanceof IntBridgeInstallation)
							return createNode((IntBridgeInstallation) content, parent, relType);

						if (content instanceof IntBuildingInstallation)
							return createNode((IntBuildingInstallation) content, parent, relType);

						if (content instanceof IntTunnelInstallation)
							return createNode((IntTunnelInstallation) content, parent, relType);

						if (content instanceof LandUse)
							return createNode((LandUse) content, parent, relType);

						if (content instanceof ReliefFeature)
							return createNode((ReliefFeature) content, parent, relType);

						if (content instanceof Room)
							return createNode((Room) content, parent, relType);

						if (content instanceof TunnelFurniture)
							return createNode((TunnelFurniture) content, parent, relType);

						if (content instanceof TunnelInstallation)
							return createNode((TunnelInstallation) content, parent, relType);

						return createNode((AbstractCityObject) content, parent, relType);
					}

					if (content instanceof AbstractCoverage) {
						if (content instanceof AbstractDiscreteCoverage) {
							if (content instanceof RectifiedGridCoverage)
								return createNode((RectifiedGridCoverage) content, parent, relType);
							return createNode((AbstractDiscreteCoverage) content, parent, relType);
						}
						return createNode((AbstractCoverage) content, parent, relType);
					}

					if (content instanceof AbstractFeatureCollection) {
						if (content instanceof CityModel)
							return createNode((CityModel) content, parent, relType);
						return createNode((AbstractFeatureCollection) content, parent, relType);
					}

					if (content instanceof AbstractSurfaceData) {
						if (content instanceof AbstractTexture) {
							if (content instanceof GeoreferencedTexture)
								return createNode((GeoreferencedTexture) content, parent, relType);
							if (content instanceof ParameterizedTexture)
								return createNode((ParameterizedTexture) content, parent, relType);
							return createNode((AbstractTexture) content, parent, relType);
						}

						if (content instanceof X3DMaterial)
							return createNode((X3DMaterial) content, parent, relType);

						return createNode((AbstractSurfaceData) content, parent, relType);
					}

					if (content instanceof org.citygml4j.model.citygml.core.Address)
						return createNode((org.citygml4j.model.citygml.core.Address) content, parent, relType);

					if (content instanceof Appearance)
						return createNode((Appearance) content, parent, relType);

					return createNode((AbstractFeature) content, parent, relType);
				}

				if (content instanceof AbstractGeometry) {
					if (content instanceof AbstractGeometricAggregate) {
						if (content instanceof MultiCurve)
							return createNode((MultiCurve) content, parent, relType);
						if (content instanceof MultiGeometry)
							return createNode((MultiGeometry) content, parent, relType);
						if (content instanceof MultiLineString)
							return createNode((MultiLineString) content, parent, relType);
						if (content instanceof MultiPoint)
							return createNode((MultiPoint) content, parent, relType);
						if (content instanceof MultiPolygon)
							return createNode((MultiPolygon) content, parent, relType);
						if (content instanceof MultiSolid)
							return createNode((MultiSolid) content, parent, relType);
						if (content instanceof MultiSurface)
							return createNode((MultiSurface) content, parent, relType);
						return createNode((AbstractGeometricAggregate) content, parent, relType);
					}

					if (content instanceof AbstractGeometricPrimitive) {
						if (content instanceof AbstractCurve) {
							if (content instanceof CompositeCurve)
								return createNode((CompositeCurve) content, parent, relType);
							if (content instanceof Curve)
								return createNode((Curve) content, parent, relType);
							if (content instanceof LineString)
								return createNode((LineString) content, parent, relType);
							if (content instanceof OrientableCurve)
								return createNode((OrientableCurve) content, parent, relType);
							return createNode((AbstractCurve) content, parent, relType);
						}

						if (content instanceof AbstractSolid) {
							if (content instanceof CompositeSolid)
								return createNode((CompositeSolid) content, parent, relType);
							if (content instanceof Solid)
								return createNode((Solid) content, parent, relType);
							return createNode((AbstractSolid) content, parent, relType);
						}

						if (content instanceof AbstractSurface) {
							if (content instanceof CompositeSurface)
								return createNode((CompositeSurface) content, parent, relType);

							if (content instanceof OrientableSurface) {
								if (content instanceof _TexturedSurface)
									return createNode((_TexturedSurface) content, parent, relType);
								return createNode((OrientableSurface) content, parent, relType);
							}

							if (content instanceof Polygon)
								return createNode((Polygon) content, parent, relType);

							if (content instanceof Surface) {
								if (content instanceof TriangulatedSurface) {
									if (content instanceof Tin)
										return createNode((Tin) content, parent, relType);
									return createNode((TriangulatedSurface) content, parent, relType);
								}
								return createNode((Surface) content, parent, relType);
							}

							return createNode((AbstractSurface) content, parent, relType);
						}

						if (content instanceof Point)
							return createNode((Point) content, parent, relType);

						return createNode((AbstractGeometricPrimitive) content, parent, relType);
					}

					if (content instanceof AbstractRing) {
						if (content instanceof LinearRing)
							return createNode((LinearRing) content, parent, relType);
						if (content instanceof Ring)
							return createNode((Ring) content, parent, relType);
						return createNode((AbstractRing) content, parent, relType);
					}

					if (content instanceof GeometricComplex)
						return createNode((GeometricComplex) content, parent, relType);

					if (content instanceof Grid) {
						if (content instanceof RectifiedGrid)
							return createNode((RectifiedGrid) content, parent, relType);
						return createNode((Grid) content, parent, relType);
					}

					return createNode((AbstractGeometry) content, parent, relType);
				}

				if (content instanceof AbstractTextureParameterization) {
					if (content instanceof TexCoordGen)
						return createNode((TexCoordGen) content, parent, relType);
					if (content instanceof TexCoordList)
						return createNode((TexCoordList) content, parent, relType);
					return createNode((AbstractTextureParameterization) content, parent, relType);
				}

				if (content instanceof CompositeValue) {
					if (content instanceof ValueArray)
						return createNode((ValueArray) content, parent, relType);
					return createNode((CompositeValue) content, parent, relType);
				}

				if (content instanceof ImplicitGeometry)
					return createNode((ImplicitGeometry) content, parent, relType);

				return createNode((AbstractGML) content, parent, relType);
			}

			if (content instanceof AbstractSurfacePatch) {
				if (content instanceof Rectangle)
					return createNode((Rectangle) content, parent, relType);
				if (content instanceof Triangle)
					return createNode((Triangle) content, parent, relType);
				return createNode((AbstractSurfacePatch) content, parent, relType);
			}

			if (content instanceof AddressDetails)
				return createNode((AddressDetails) content, parent, relType);

			if (content instanceof GenericValueObject)
				return createNode((GenericValueObject) content, parent, relType);

			if (content instanceof MetaData)
				return createNode((MetaData) content, parent, relType);

			if (content instanceof Value)
				return createNode((Value) content, parent, relType);

			if (content instanceof ValueObject)
				return createNode((ValueObject) content, parent, relType);

			return createNode((Associable) content, parent, relType);
		}

		if (content instanceof Child) {
			if (content instanceof _Color)
				return createNode((_Color) content, parent, relType);

			if (content instanceof AbstractGenericAttribute) {
				if (content instanceof DateAttribute)
					return createNode((DateAttribute) content, parent, relType);

				if (content instanceof DoubleAttribute)
					return createNode((DoubleAttribute) content, parent, relType);

				if (content instanceof GenericAttributeSet)
					return createNode((GenericAttributeSet) content, parent, relType);

				if (content instanceof IntAttribute)
					return createNode((IntAttribute) content, parent, relType);

				if (content instanceof MeasureAttribute)
					return createNode((MeasureAttribute) content, parent, relType);

				if (content instanceof StringAttribute)
					return createNode((StringAttribute) content, parent, relType);

				if (content instanceof UriAttribute)
					return createNode((UriAttribute) content, parent, relType);

				return createNode((AbstractGenericAttribute) content, parent, relType);
			}

			if (content instanceof org.citygml4j.model.xal.Address)
				return createNode((org.citygml4j.model.xal.Address) content, parent, relType);

			if (content instanceof AddressIdentifier)
				return createNode((AddressIdentifier) content, parent, relType);

			if (content instanceof AddressLatitude)
				return createNode((AddressLatitude) content, parent, relType);

			if (content instanceof AddressLatitudeDirection)
				return createNode((AddressLatitudeDirection) content, parent, relType);

			if (content instanceof AddressLine)
				return createNode((AddressLine) content, parent, relType);

			if (content instanceof AddressLines)
				return createNode((AddressLines) content, parent, relType);

			if (content instanceof AddressLongitude)
				return createNode((AddressLongitude) content, parent, relType);

			if (content instanceof AddressLongitudeDirection)
				return createNode((AddressLongitudeDirection) content, parent, relType);

			if (content instanceof ADEComponent)
				return createNode((ADEComponent) content, parent, relType);

			if (content instanceof AdministrativeArea)
				return createNode((AdministrativeArea) content, parent, relType);

			if (content instanceof ArrayAssociation) {
				if (content instanceof CurveSegmentArrayProperty)
					return createNode((CurveSegmentArrayProperty) content, parent, relType);

				if (content instanceof FeatureArrayProperty)
					return createNode((FeatureArrayProperty) content, parent, relType);

				if (content instanceof GeometryArrayProperty) {
					if (content instanceof CurveArrayProperty)
						return createNode((CurveArrayProperty) content, parent, relType);

					if (content instanceof PointArrayProperty)
						return createNode((PointArrayProperty) content, parent, relType);

					if (content instanceof SolidArrayProperty)
						return createNode((SolidArrayProperty) content, parent, relType);

					if (content instanceof SurfaceArrayProperty)
						return createNode((SurfaceArrayProperty) content, parent, relType);

					return createNode((GeometryArrayProperty) content, parent, relType);
				}

				if (content instanceof LineStringSegmentArrayProperty)
					return createNode((LineStringSegmentArrayProperty) content, parent, relType);

				if (content instanceof SurfacePatchArrayProperty) {
					if (content instanceof TrianglePatchArrayProperty)
						return createNode((TrianglePatchArrayProperty) content, parent, relType);

					return createNode((SurfacePatchArrayProperty) content, parent, relType);
				}

				if (content instanceof ValueArrayProperty)
					return createNode((ValueArrayProperty) content, parent, relType);

				return createNode((ArrayAssociation) content, parent, relType);
			}

			if (content instanceof AssociationByRep) {
				if (content instanceof AssociationByRepOrRef) {
					if (content instanceof _AppearanceProperty)
						return createNode((_AppearanceProperty) content, parent, relType);

					if (content instanceof FeatureProperty) {
						if (content instanceof AddressProperty)
							return createNode((AddressProperty) content, parent, relType);

						if (content instanceof AppearanceProperty) {
							if (content instanceof AppearanceMember)
								return createNode((AppearanceMember) content, parent, relType);
							return createNode((AppearanceProperty) content, parent, relType);
						}

						if (content instanceof AuxiliaryTrafficAreaProperty)
							return createNode((AuxiliaryTrafficAreaProperty) content, parent, relType);

						if (content instanceof BoundarySurfaceProperty)
							return createNode((BoundarySurfaceProperty) content, parent, relType);

						if (content instanceof org.citygml4j.model.citygml.tunnel.BoundarySurfaceProperty)
							// TODO
							return null;

						if (content instanceof org.citygml4j.model.citygml.bridge.BoundarySurfaceProperty)
							// TODO
							return null;

						if (content instanceof BoundedByWaterSurfaceProperty)
							return createNode((BoundedByWaterSurfaceProperty) content, parent, relType);

						if (content instanceof BridgeConstructionElementProperty)
							return createNode((BridgeConstructionElementProperty) content, parent, relType);

						if (content instanceof BridgeInstallationProperty)
							return createNode((BridgeInstallationProperty) content, parent, relType);

						if (content instanceof BridgePartProperty)
							return createNode((BridgePartProperty) content, parent, relType);

						if (content instanceof BuildingInstallationProperty)
							return createNode((BuildingInstallationProperty) content, parent, relType);

						if (content instanceof BuildingPartProperty)
							return createNode((BuildingPartProperty) content, parent, relType);

						if (content instanceof CityObjectGroupMember)
							return createNode((CityObjectGroupMember) content, parent, relType);

						if (content instanceof CityObjectGroupParent)
							return createNode((CityObjectGroupParent) content, parent, relType);

						if (content instanceof CityObjectMember)
							return createNode((CityObjectMember) content, parent, relType);

						if (content instanceof FeatureMember)
							return createNode((FeatureMember) content, parent, relType);

						if (content instanceof GeneralizationRelation)
							return createNode((GeneralizationRelation) content, parent, relType);

						if (content instanceof GridProperty)
							return createNode((GridProperty) content, parent, relType);

						if (content instanceof IntBridgeInstallationProperty)
							return createNode((IntBridgeInstallationProperty) content, parent, relType);

						if (content instanceof IntBuildingInstallationProperty)
							return createNode((IntBuildingInstallationProperty) content, parent, relType);

						if (content instanceof InteriorBridgeRoomProperty)
							return createNode((InteriorBridgeRoomProperty) content, parent, relType);

						if (content instanceof InteriorFurnitureProperty)
							return createNode((InteriorFurnitureProperty) content, parent, relType);

						if (content instanceof org.citygml4j.model.citygml.tunnel.InteriorFurnitureProperty)
							// TODO
							return null;

						if (content instanceof org.citygml4j.model.citygml.bridge.InteriorFurnitureProperty)
							// TODO
							return null;

						if (content instanceof InteriorHollowSpaceProperty)
							return createNode((InteriorHollowSpaceProperty) content, parent, relType);

						if (content instanceof InteriorRoomProperty)
							return createNode((InteriorRoomProperty) content, parent, relType);

						if (content instanceof IntTunnelInstallationProperty)
							return createNode((IntTunnelInstallationProperty) content, parent, relType);

						if (content instanceof OpeningProperty)
							return createNode((OpeningProperty) content, parent, relType);

						if (content instanceof org.citygml4j.model.citygml.tunnel.OpeningProperty)
							// TODO
							return null;

						if (content instanceof org.citygml4j.model.citygml.bridge.OpeningProperty)
							// TODO
							return null;

						if (content instanceof ReliefComponentProperty)
							return createNode((ReliefComponentProperty) content, parent, relType);

						if (content instanceof SurfaceDataProperty)
							return createNode((SurfaceDataProperty) content, parent, relType);

						if (content instanceof TrafficAreaProperty)
							return createNode((TrafficAreaProperty) content, parent, relType);

						if (content instanceof TunnelInstallationProperty)
							return createNode((TunnelInstallationProperty) content, parent, relType);

						if (content instanceof TunnelPartProperty)
							return createNode((TunnelPartProperty) content, parent, relType);

						return createNode((FeatureProperty) content, parent, relType);
					}

					if (content instanceof GeometryProperty) {
						if (content instanceof CompositeCurveProperty)
							return createNode((CompositeCurveProperty) content, parent, relType);

						if (content instanceof CompositeSolidProperty)
							return createNode((CompositeSolidProperty) content, parent, relType);

						if (content instanceof CompositeSurfaceProperty)
							return createNode((CompositeSurfaceProperty) content, parent, relType);

						if (content instanceof CurveProperty)
							return createNode((CurveProperty) content, parent, relType);

						if (content instanceof DomainSet) {
							if (content instanceof RectifiedGridDomain)
								return createNode((RectifiedGridDomain) content, parent, relType);
							return createNode((DomainSet) content, parent, relType);
						}

						if (content instanceof GeometricComplexProperty)
							return createNode((GeometricComplexProperty) content, parent, relType);

						if (content instanceof GeometricPrimitiveProperty)
							return createNode((GeometricPrimitiveProperty) content, parent, relType);

						if (content instanceof LineStringProperty)
							return createNode((LineStringProperty) content, parent, relType);

						if (content instanceof LocationProperty) {
							if (content instanceof PriorityLocationProperty)
								return createNode((PriorityLocationProperty) content, parent, relType);
							return createNode((LocationProperty) content, parent, relType);
						}

						if (content instanceof MultiCurveProperty)
							return createNode((MultiCurveProperty) content, parent, relType);

						if (content instanceof MultiGeometryProperty)
							return createNode((MultiGeometryProperty) content, parent, relType);

						if (content instanceof MultiLineStringProperty)
							return createNode((MultiLineStringProperty) content, parent, relType);

						if (content instanceof MultiPointProperty)
							return createNode((MultiPointProperty) content, parent, relType);

						if (content instanceof MultiPolygonProperty)
							return createNode((MultiPolygonProperty) content, parent, relType);

						if (content instanceof MultiSolidProperty)
							return createNode((MultiSolidProperty) content, parent, relType);

						if (content instanceof MultiSurfaceProperty)
							return createNode((MultiSurfaceProperty) content, parent, relType);

						if (content instanceof PointProperty) {
							if (content instanceof PointRep)
								return createNode((PointRep) content, parent, relType);
							return createNode((PointProperty) content, parent, relType);
						}

						if (content instanceof PolygonProperty)
							return createNode((PolygonProperty) content, parent, relType);

						if (content instanceof SolidProperty)
							return createNode((SolidProperty) content, parent, relType);

						if (content instanceof SurfaceProperty)
							return createNode((SurfaceProperty) content, parent, relType);

						if (content instanceof TinProperty)
							return createNode((TinProperty) content, parent, relType);

						return createNode((GeometryProperty) content, parent, relType);
					}

					if (content instanceof ImplicitRepresentationProperty)
						return createNode((ImplicitRepresentationProperty) content, parent, relType);

					if (content instanceof MetaDataProperty)
						return createNode((MetaDataProperty) content, parent, relType);

					if (content instanceof RangeParameters)
						return createNode((RangeParameters) content, parent, relType);

					if (content instanceof TextureAssociation)
						return createNode((TextureAssociation) content, parent, relType);

					if (content instanceof ValueProperty)
						return createNode((ValueProperty) content, parent, relType);

					return createNode((AssociationByRepOrRef) content, parent, relType);
				}

				if (content instanceof InlineGeometryProperty) {
					if (content instanceof AbstractRingProperty) {
						if (content instanceof Exterior)
							return createNode((Exterior) content, parent, relType);

						if (content instanceof InnerBoundaryIs)
							return createNode((InnerBoundaryIs) content, parent, relType);

						if (content instanceof Interior)
							return createNode((Interior) content, parent, relType);

						if (content instanceof OuterBoundaryIs)
							return createNode((OuterBoundaryIs) content, parent, relType);

						return createNode((AbstractRingProperty) content, parent, relType);
					}

					if (content instanceof LinearRingProperty)
						return createNode((LinearRingProperty) content, parent, relType);

					return createNode((InlineGeometryProperty) content, parent, relType);
				}

				if (content instanceof XalAddressProperty)
					return createNode((XalAddressProperty) content, parent, relType);

				return createNode((AssociationByRep) content, parent, relType);
			}

			if (content instanceof Barcode)
				return createNode((Barcode) content, parent, relType);

			if (content instanceof BooleanOrNull)
				return createNode((BooleanOrNull) content, parent, relType);

			if (content instanceof BooleanOrNullList)
				return createNode((BooleanOrNullList) content, parent, relType);

			if (content instanceof BoundingShape)
				return createNode((BoundingShape) content, parent, relType);

			if (content instanceof BuildingName)
				return createNode((BuildingName) content, parent, relType);

			if (content instanceof Code)
				return createNode((Code) content, parent, relType);

			if (content instanceof CodeOrNullList) {
				if (content instanceof CategoryExtent)
					return createNode((CategoryExtent) content, parent, relType);
				return createNode((CodeOrNullList) content, parent, relType);
			}

			if (content instanceof Color)
				return createNode((Color) content, parent, relType);

			if (content instanceof ColorPlusOpacity)
				return createNode((ColorPlusOpacity) content, parent, relType);

			if (content instanceof ControlPoint)
				return createNode((ControlPoint) content, parent, relType);

			if (content instanceof Coord)
				return createNode((Coord) content, parent, relType);

			if (content instanceof Coordinates)
				return createNode((Coordinates) content, parent, relType);

			if (content instanceof Country)
				return createNode((Country) content, parent, relType);

			if (content instanceof CountryName)
				return createNode((CountryName) content, parent, relType);

			if (content instanceof CountryNameCode)
				return createNode((CountryNameCode) content, parent, relType);

			if (content instanceof CoverageFunction)
				return createNode((CoverageFunction) content, parent, relType);

			if (content instanceof DataBlock)
				return createNode((DataBlock) content, parent, relType);

			if (content instanceof Department)
				return createNode((Department) content, parent, relType);

			if (content instanceof DepartmentName)
				return createNode((DepartmentName) content, parent, relType);

			if (content instanceof DependentLocality)
				return createNode((DependentLocality) content, parent, relType);

			if (content instanceof DependentLocalityName)
				return createNode((DependentLocalityName) content, parent, relType);

			if (content instanceof DependentLocalityNumber)
				return createNode((DependentLocalityNumber) content, parent, relType);

			if (content instanceof DependentThoroughfare)
				return createNode((DependentThoroughfare) content, parent, relType);

			if (content instanceof DirectPosition)
				return createNode((DirectPosition) content, parent, relType);

			if (content instanceof DirectPositionList)
				return createNode((DirectPositionList) content, parent, relType);

			if (content instanceof DoubleOrNull)
				return createNode((DoubleOrNull) content, parent, relType);

			if (content instanceof DoubleOrNullList) {
				if (content instanceof MeasureOrNullList) {
					if (content instanceof QuantityExtent)
						return createNode((QuantityExtent) content, parent, relType);
					return createNode((MeasureOrNullList) content, parent, relType);
				}
				return createNode((DoubleOrNullList) content, parent, relType);
			}

			if (content instanceof EndorsementLineCode)
				return createNode((EndorsementLineCode) content, parent, relType);

			if (content instanceof Envelope)
				return createNode((Envelope) content, parent, relType);

			if (content instanceof ExternalObject)
				return createNode((ExternalObject) content, parent, relType);

			if (content instanceof ExternalReference)
				return createNode((ExternalReference) content, parent, relType);

			if (content instanceof File)
				return createNode((File) content, parent, relType);

			if (content instanceof Firm)
				return createNode((Firm) content, parent, relType);

			if (content instanceof FirmName)
				return createNode((FirmName) content, parent, relType);

			if (content instanceof GeometricPositionGroup)
				return createNode((GeometricPositionGroup) content, parent, relType);

			if (content instanceof GridEnvelope)
				return createNode((GridEnvelope) content, parent, relType);

			if (content instanceof GridFunction) {
				if (content instanceof IndexMap)
					return createNode((IndexMap) content, parent, relType);
				return createNode((GridFunction) content, parent, relType);
			}

			if (content instanceof GridLimits)
				return createNode((GridLimits) content, parent, relType);

			if (content instanceof IntegerOrNull)
				return createNode((IntegerOrNull) content, parent, relType);

			if (content instanceof IntegerOrNullList) {
				if (content instanceof CountExtent)
					return createNode((CountExtent) content, parent, relType);
				return createNode((IntegerOrNullList) content, parent, relType);
			}

			if (content instanceof KeyLineCode)
				return createNode((KeyLineCode) content, parent, relType);

			if (content instanceof LargeMailUser)
				return createNode((LargeMailUser) content, parent, relType);

			if (content instanceof LargeMailUserIdentifier)
				return createNode((LargeMailUserIdentifier) content, parent, relType);

			if (content instanceof LargeMailUserName)
				return createNode((LargeMailUserName) content, parent, relType);

			if (content instanceof Locality)
				return createNode((Locality) content, parent, relType);

			if (content instanceof LocalityName)
				return createNode((LocalityName) content, parent, relType);

			if (content instanceof MailStop)
				return createNode((MailStop) content, parent, relType);

			if (content instanceof MailStopName)
				return createNode((MailStopName) content, parent, relType);

			if (content instanceof MailStopNumber)
				return createNode((MailStopNumber) content, parent, relType);

			if (content instanceof Measure) {
				if (content instanceof Length)
					return createNode((Length) content, parent, relType);
				if (content instanceof Speed)
					return createNode((Speed) content, parent, relType);
				return createNode((Measure) content, parent, relType);
			}

			if (content instanceof NameOrNull)
				return createNode((NameOrNull) content, parent, relType);

			if (content instanceof Null)
				return createNode((Null) content, parent, relType);

			if (content instanceof PosOrPointPropertyOrPointRep)
				return createNode((PosOrPointPropertyOrPointRep) content, parent, relType);

			if (content instanceof PosOrPointPropertyOrPointRepOrCoord)
				return createNode((PosOrPointPropertyOrPointRepOrCoord) content, parent, relType);

			if (content instanceof PostalCode)
				return createNode((PostalCode) content, parent, relType);

			if (content instanceof PostalCodeNumber)
				return createNode((PostalCodeNumber) content, parent, relType);

			if (content instanceof PostalCodeNumberExtension)
				return createNode((PostalCodeNumberExtension) content, parent, relType);

			if (content instanceof PostalRoute)
				return createNode((PostalRoute) content, parent, relType);

			if (content instanceof PostalRouteName)
				return createNode((PostalRouteName) content, parent, relType);

			if (content instanceof PostalRouteNumber)
				return createNode((PostalRouteNumber) content, parent, relType);

			if (content instanceof PostalServiceElements)
				return createNode((PostalServiceElements) content, parent, relType);

			if (content instanceof PostBox)
				return createNode((PostBox) content, parent, relType);

			if (content instanceof PostBoxNumber)
				return createNode((PostBoxNumber) content, parent, relType);

			if (content instanceof PostBoxNumberExtension)
				return createNode((PostBoxNumberExtension) content, parent, relType);

			if (content instanceof PostBoxNumberPrefix)
				return createNode((PostBoxNumberPrefix) content, parent, relType);

			if (content instanceof PostBoxNumberSuffix)
				return createNode((PostBoxNumberSuffix) content, parent, relType);

			if (content instanceof PostOffice)
				return createNode((PostOffice) content, parent, relType);

			if (content instanceof PostOfficeName)
				return createNode((PostOfficeName) content, parent, relType);

			if (content instanceof PostOfficeNumber)
				return createNode((PostOfficeNumber) content, parent, relType);

			if (content instanceof PostTown)
				return createNode((PostTown) content, parent, relType);

			if (content instanceof PostTownName)
				return createNode((PostTownName) content, parent, relType);

			if (content instanceof PostTownSuffix)
				return createNode((PostTownSuffix) content, parent, relType);

			if (content instanceof Premise)
				return createNode((Premise) content, parent, relType);

			if (content instanceof PremiseLocation)
				return createNode((PremiseLocation) content, parent, relType);

			if (content instanceof PremiseName)
				return createNode((PremiseName) content, parent, relType);

			if (content instanceof PremiseNumber)
				return createNode((PremiseNumber) content, parent, relType);

			if (content instanceof PremiseNumberPrefix)
				return createNode((PremiseNumberPrefix) content, parent, relType);

			if (content instanceof PremiseNumberRange)
				return createNode((PremiseNumberRange) content, parent, relType);

			if (content instanceof PremiseNumberRangeFrom)
				return createNode((PremiseNumberRangeFrom) content, parent, relType);

			if (content instanceof PremiseNumberRangeTo)
				return createNode((PremiseNumberRangeTo) content, parent, relType);

			if (content instanceof PremiseNumberSuffix)
				return createNode((PremiseNumberSuffix) content, parent, relType);

			if (content instanceof RangeSet)
				return createNode((RangeSet) content, parent, relType);

			if (content instanceof ScalarValue)
				return createNode((ScalarValue) content, parent, relType);

			if (content instanceof ScalarValueList)
				return createNode((ScalarValueList) content, parent, relType);

			if (content instanceof SequenceRule)
				return createNode((SequenceRule) content, parent, relType);

			if (content instanceof SortingCode)
				return createNode((SortingCode) content, parent, relType);

			if (content instanceof StringOrRef)
				return createNode((StringOrRef) content, parent, relType);

			if (content instanceof SubAdministrativeArea)
				return createNode((SubAdministrativeArea) content, parent, relType);

			if (content instanceof SubAdministrativeAreaName)
				return createNode((SubAdministrativeAreaName) content, parent, relType);

			if (content instanceof SubPremise)
				return createNode((SubPremise) content, parent, relType);

			if (content instanceof SubPremiseLocation)
				return createNode((SubPremiseLocation) content, parent, relType);

			if (content instanceof SubPremiseName)
				return createNode((SubPremiseName) content, parent, relType);

			if (content instanceof SubPremiseNumber)
				return createNode((SubPremiseNumber) content, parent, relType);

			if (content instanceof SubPremiseNumberPrefix)
				return createNode((SubPremiseNumberPrefix) content, parent, relType);

			if (content instanceof SubPremiseNumberSuffix)
				return createNode((SubPremiseNumberSuffix) content, parent, relType);

			if (content instanceof SupplementaryPostalServiceData)
				return createNode((SupplementaryPostalServiceData) content, parent, relType);

			if (content instanceof TextureCoordinates)
				return createNode((TextureCoordinates) content, parent, relType);

			if (content instanceof Thoroughfare)
				return createNode((Thoroughfare) content, parent, relType);

			if (content instanceof ThoroughfareLeadingType)
				return createNode((ThoroughfareLeadingType) content, parent, relType);

			if (content instanceof ThoroughfareName)
				return createNode((ThoroughfareName) content, parent, relType);

			if (content instanceof ThoroughfareNumber)
				return createNode((ThoroughfareNumber) content, parent, relType);

			if (content instanceof ThoroughfareNumberFrom)
				return createNode((ThoroughfareNumberFrom) content, parent, relType);

			if (content instanceof ThoroughfareNumberFromContent)
				return createNode((ThoroughfareNumberFromContent) content, parent, relType);

			if (content instanceof ThoroughfareNumberOrRange)
				return createNode((ThoroughfareNumberOrRange) content, parent, relType);

			if (content instanceof ThoroughfareNumberPrefix)
				return createNode((ThoroughfareNumberPrefix) content, parent, relType);

			if (content instanceof ThoroughfareNumberRange)
				return createNode((ThoroughfareNumberRange) content, parent, relType);

			if (content instanceof ThoroughfareNumberSuffix)
				return createNode((ThoroughfareNumberSuffix) content, parent, relType);

			if (content instanceof ThoroughfareNumberTo)
				return createNode((ThoroughfareNumberTo) content, parent, relType);

			if (content instanceof ThoroughfareNumberToContent)
				return createNode((ThoroughfareNumberToContent) content, parent, relType);

			if (content instanceof ThoroughfarePostDirection)
				return createNode((ThoroughfarePostDirection) content, parent, relType);

			if (content instanceof ThoroughfarePreDirection)
				return createNode((ThoroughfarePreDirection) content, parent, relType);

			if (content instanceof ThoroughfareTrailingType)
				return createNode((ThoroughfareTrailingType) content, parent, relType);

			if (content instanceof TransformationMatrix2x2)
				return createNode((TransformationMatrix2x2) content, parent, relType);

			if (content instanceof TransformationMatrix3x4) {
				if (content instanceof WorldToTexture)
					return createNode((WorldToTexture) content, parent, relType);
				return createNode((TransformationMatrix3x4) content, parent, relType);
			}

			if (content instanceof TransformationMatrix4x4)
				return createNode((TransformationMatrix4x4) content, parent, relType);

			if (content instanceof ValueExtent)
				return createNode((ValueExtent) content, parent, relType);

			if (content instanceof Vector)
				return createNode((Vector) content, parent, relType);

			return createNode((Child) content, parent, relType);
		}

		if (content instanceof CityGML) {
			if (content instanceof AppearanceModuleComponent) {
				if (content instanceof TextureType)
					return createNode((TextureType) content, parent, relType);

				if (content instanceof WrapMode)
					return createNode((WrapMode) content, parent, relType);

				return createNode((AppearanceModuleComponent) content, parent, relType);
			}

			if (content instanceof BridgeModuleComponent)
				return createNode((BridgeModuleComponent) content, parent, relType);

			if (content instanceof BuildingModuleComponent)
				return createNode((BuildingModuleComponent) content, parent, relType);

			if (content instanceof CityFurnitureModuleComponent)
				return createNode((CityFurnitureModuleComponent) content, parent, relType);

			if (content instanceof CityObjectGroupModuleComponent)
				return createNode((CityObjectGroupModuleComponent) content, parent, relType);

			if (content instanceof CoreModuleComponent) {
				if (content instanceof RelativeToTerrain)
					return createNode((RelativeToTerrain) content, parent, relType);

				if (content instanceof RelativeToWater)
					return createNode((RelativeToWater) content, parent, relType);

				return createNode((CoreModuleComponent) content, parent, relType);
			}

			if (content instanceof GenericsModuleComponent)
				return createNode((GenericsModuleComponent) content, parent, relType);

			if (content instanceof LandUseModuleComponent)
				return createNode((LandUseModuleComponent) content, parent, relType);

			if (content instanceof ReliefModuleComponent)
				return createNode((ReliefModuleComponent) content, parent, relType);

			if (content instanceof TexturedSurfaceModuleComponent) {
				if (content instanceof _TextureType)
					return createNode((_TextureType) content, parent, relType);
				return createNode((TexturedSurfaceModuleComponent) content, parent, relType);
			}

			if (content instanceof TransportationModuleComponent)
				return createNode((TransportationModuleComponent) content, parent, relType);

			if (content instanceof TunnelModuleComponent)
				return createNode((TunnelModuleComponent) content, parent, relType);

			if (content instanceof VegetationModuleComponent)
				return createNode((VegetationModuleComponent) content, parent, relType);

			if (content instanceof WaterBodyModuleComponent)
				return createNode((WaterBodyModuleComponent) content, parent, relType);

			return createNode((CityGML) content, parent, relType);
		}

		if (content instanceof GML) {
			if (content instanceof CurveInterpolation)
				return createNode((CurveInterpolation) content, parent, relType);

			if (content instanceof FileValueModel)
				return createNode((FileValueModel) content, parent, relType);

			if (content instanceof IncrementOrder)
				return createNode((IncrementOrder) content, parent, relType);

			if (content instanceof SequenceRuleNames)
				return createNode((SequenceRuleNames) content, parent, relType);

			if (content instanceof Sign)
				return createNode((Sign) content, parent, relType);

			if (content instanceof SurfaceInterpolation)
				return createNode((SurfaceInterpolation) content, parent, relType);

			if (content instanceof XLinkActuate)
				return createNode((XLinkActuate) content, parent, relType);

			if (content instanceof XLinkShow)
				return createNode((XLinkShow) content, parent, relType);

			if (content instanceof XLinkType)
				return createNode((XLinkType) content, parent, relType);

			if (content instanceof AssociationAttributeGroup)
				return createNode((AssociationAttributeGroup) content, parent, relType);

			if (content instanceof SRSInformationGroup) {
				if (content instanceof SRSReferenceGroup)
					return createNode((SRSReferenceGroup) content, parent, relType);
				return createNode((SRSInformationGroup) content, parent, relType);
			}

			if (content instanceof StandardObjectProperties)
				return createNode((StandardObjectProperties) content, parent, relType);

			return createNode((GML) content, parent, relType);
		}

		if (content instanceof XAL) {
			if (content instanceof GrPostal)
				return createNode((GrPostal) content, parent, relType);
			return createNode((XAL) content, parent, relType);
		}

		return null;
	}

	@Override
	public <T> Node createNode(List<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		StringBuilder listContents = new StringBuilder();
		for (T obj : content) {
			if (obj == null) {
				continue;
			}

			if (obj instanceof Integer || obj instanceof Double || obj instanceof String) {
				listContents.append((listContents.length() > 0 ? ";" : "") + obj.toString());
			} else if (obj instanceof Module) {
				createNodeSearchHierarchy((Module) obj, parent, relType);
			} else if (obj instanceof ModelObject) {
				createNodeSearchHierarchy((ModelObject) obj, parent, relType);
			}
		}

		if (listContents.length() > 0) {
			parent.setProperty(relType + "", listContents.toString());
		}

		return parent;
	}

	@Override
	public <T> Node createNode(T[] content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		return createNode(Arrays.asList(content), parent, relType);
	}

	@Override
	public Node createNode(HashMap<String, Object> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = createNodeWithLabel("HASH_MAP");

		Iterator it = content.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			targetNode.setProperty(pair.getKey().toString(), pair.getValue().toString());
		}

		if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
			parent.createRelationshipTo(targetNode, relType);
		}
		return targetNode;
	}

	@Override
	public Node createNode(Element content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Module functions
	 */
	@Override
	public Node createNode(CityGMLModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITYGML_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		return targetNode;
	}

	@Override
	public Node createNode(AbstractModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ABSTRACT_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("type", content.getType() + "");

		targetNode.setProperty("version", content.getVersion() + "");

		targetNode.setProperty("namespaceURI", content.getNamespaceURI() + "");

		targetNode.setProperty("namespacePrefix", content.getNamespacePrefix() + "");

		targetNode.setProperty("schemaLocation", content.getSchemaLocation() + "");

		createNode(content.getDependencies(), targetNode, GMLRelTypes.DEPENDENCIES);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractCityGMLModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ABSTRACT_CITYGML_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AppearanceModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("APPEARANCE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("Appearance", content.getFeatureElementClass("Appearance").getSimpleName());
		elementMap.put("ParameterizedTexture", content.getFeatureElementClass("ParameterizedTexture").getSimpleName());
		elementMap.put("GeoreferencedTexture", content.getFeatureElementClass("GeoreferencedTexture").getSimpleName());
		elementMap.put("X3DMaterial", content.getFeatureElementClass("X3DMaterial").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		targetNode.setProperty("propertySet", "appearance;appearanceMember;surfaceDataMember");

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BridgeModule content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createNode(BuildingModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("BUILDING_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("Building", content.getFeatureElementClass("Building").getSimpleName());
		elementMap.put("BuildingPart", content.getFeatureElementClass("BuildingPart").getSimpleName());
		elementMap.put("Room", content.getFeatureElementClass("Room").getSimpleName());
		elementMap.put("IntBuildingInstallation", content.getFeatureElementClass("IntBuildingInstallation").getSimpleName());
		elementMap.put("BuildingInstallation", content.getFeatureElementClass("BuildingInstallation").getSimpleName());
		elementMap.put("BuildingFurniture", content.getFeatureElementClass("BuildingFurniture").getSimpleName());
		elementMap.put("InteriorWallSurface", content.getFeatureElementClass("InteriorWallSurface").getSimpleName());
		elementMap.put("RoofSurface", content.getFeatureElementClass("RoofSurface").getSimpleName());
		elementMap.put("ClosureSurface", content.getFeatureElementClass("ClosureSurface").getSimpleName());
		elementMap.put("WallSurface", content.getFeatureElementClass("WallSurface").getSimpleName());
		elementMap.put("FloorSurface", content.getFeatureElementClass("FloorSurface").getSimpleName());
		elementMap.put("CeilingSurface", content.getFeatureElementClass("CeilingSurface").getSimpleName());
		elementMap.put("GroundSurface", content.getFeatureElementClass("GroundSurface").getSimpleName());
		elementMap.put("Window", content.getFeatureElementClass("Window").getSimpleName());
		elementMap.put("Door", content.getFeatureElementClass("Door").getSimpleName());

		// only in v2_0_0
		elementMap.put("OuterFloorSurface", content.getFeatureElementClass("OuterFloorSurface") == null ? "null" : content.getFeatureElementClass("OuterFloorSurface").getSimpleName());
		elementMap.put("OuterCeilingSurface", content.getFeatureElementClass("OuterCeilingSurface") == null ? "null" : content.getFeatureElementClass("OuterCeilingSurface").getSimpleName());

		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		targetNode.setProperty("propertySet", "address;boundedBy;opening;outerBuildingInstallation;interiorBuildingInstallation;interiorRoom;consistsOfBuildingPart;interiorFurniture;roomInstallation");

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityFurnitureModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITY_FURNITURE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("CityFurniture", content.getFeatureElementClass("CityFurniture").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectGroupModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITY_OBJECT_GROUP_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("CityObjectGroup", content.getFeatureElementClass("CityObjectGroup").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		targetNode.setProperty("propertySet", "groupMember;parent");

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CoreModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CORE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("CityModel", content.getFeatureElementClass("CityModel").getSimpleName());
		elementMap.put("Address", content.getFeatureElementClass("Address").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		targetNode.setProperty("propertySet", "cityObjectMember;generalizesTo");

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GenericsModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("GENERICS_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("GenericCityObject", content.getFeatureElementClass("GenericCityObject").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LandUseModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("LAND_USE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("LandUse", content.getFeatureElementClass("LandUse").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ReliefModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("RELIEF_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		HashMap<String, Object> elementMap = new HashMap<String, Object>();
		elementMap.put("MassPointRelief", content.getFeatureElementClass("MassPointRelief").getSimpleName());
		elementMap.put("ReliefFeature", content.getFeatureElementClass("ReliefFeature").getSimpleName());
		elementMap.put("BreaklineRelief", content.getFeatureElementClass("BreaklineRelief").getSimpleName());
		elementMap.put("TINRelief", content.getFeatureElementClass("TINRelief").getSimpleName());
		elementMap.put("RasterRelief", content.getFeatureElementClass("RasterRelief").getSimpleName());
		createNode(elementMap, targetNode, GMLRelTypes.ELEMENT_MAP);

		targetNode.setProperty("propertySet", "grid;reliefComponent");

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TexturedSurfaceModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("TEXTURED_SURFACE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractCityGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TransportationModule content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createNode(TunnelModule content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createNode(VegetationModule content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createNode(WaterBodyModule content, Node parent, RelationshipType relType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node createNode(AbstractGMLModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ABSTRACT_GML_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		return createNode((AbstractModule) content, targetNode, null);
	}

	@Override
	public Node createNode(GMLCoreModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("GML_CORE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(XLinkModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("XLINK_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGMLModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractXALModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ABSTRACT_XAL_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractModule) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(XALCoreModule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("XAL_CORE_MODULE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractModule) content, targetNode, null);

		return targetNode;
	}

	/*
	 * GML functions
	 */
	@Override
	public Node createNode(ModelObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("MODEL_OBJECT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		return targetNode;
	}

	@Override
	public Node createNode(Associable content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ASSOCIABLE");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ModelObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractCurveSegment content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_CURVE_SEGMENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetNumDerivativesAtStart())
			targetNode.setProperty("numDerivativesAtStart", content.getNumDerivativesAtStart());

		if (content.isSetNumDerivativesAtEnd())
			targetNode.setProperty("numDerivativesAtEnd", content.getNumDerivativesAtEnd());

		if (content.isSetNumDerivativeInterior())
			targetNode.setProperty("numDerivativeInterior", content.getNumDerivativeInterior());

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(LineStringSegment content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINE_STRING_SEGMENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPosOrPointPropertyOrPointRep(), targetNode, GMLRelTypes.CONTROL_POINTS);

		createNode(content.getPosList(), targetNode, GMLRelTypes.POS_LIST);

		createNode(content.getCoordinates(), targetNode, GMLRelTypes.COORDINATES);

		createNode(content.getInterpolation(), targetNode, GMLRelTypes.INTERPOLATION);

		createNode((AbstractCurveSegment) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractGML content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_GML);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetId()) {
			String id = content.getId();

			targetNode.setProperty("id", id);

			// create/add index on IDs
			if (SETTINGS.ENABLE_INDICES) {
				idApiIndex.putIfAbsent(targetNode, "id", id);
			} else {
				Node tmp = idIndex.putIfAbsent(id, targetNode);

				// if (tmp != null) {
				// logger.warning("WARNING: MULTIPLE ELEMENTS EXIST WITH ID = " + id + ". ID IGNORED...");
				// }
			}
		}

		createNode(content.getDescription(), targetNode, GMLRelTypes.DESCRIPTION);

		createNode(content.getName(), targetNode, GMLRelTypes.NAME);

		createNode(content.getMetaDataProperty(), targetNode, GMLRelTypes.META_DATA_PROPERTY);

		// TODO localProperties

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(_AbstractAppearance content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._ABSTRACT_APPEARANCE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(_Material content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._MATERIAL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetShininess())
			targetNode.setProperty("shininess", content.getShininess() + "");

		if (content.isSetTransparency())
			targetNode.setProperty("transparency", content.getTransparency() + "");

		if (content.isSetAmbientIntensity())
			targetNode.setProperty("ambientIntensity", content.getAmbientIntensity() + "");

		createNode(content.getDiffuseColor(), targetNode, GMLRelTypes.DIFFUSE_COLOR);

		createNode(content.getEmissiveColor(), targetNode, GMLRelTypes.EMISSIVE_COLOR);

		createNode(content.getSpecularColor(), targetNode, GMLRelTypes.SPECULAR_COLOR);

		createNode((_AbstractAppearance) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(_SimpleTexture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._SIMPLE_TEXTURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetTextureMap())
			targetNode.setProperty("textureMap", content.getTextureMap() + "");

		createNode(content.getTextureCoordinates(), targetNode, GMLRelTypes.TEXTURE_COORDINATES);

		createNode(content.getTextureType(), targetNode, GMLRelTypes._TEXTURE_TYPE);

		if (content.isSetRepeat())
			targetNode.setProperty("repeat", content.getRepeat() + "");

		createNode((_AbstractAppearance) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractFeature content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_FEATURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getBoundedBy(), targetNode, GMLRelTypes.BOUNDED_BY);

		createNode(content.getLocation(), targetNode, GMLRelTypes.LOCATION);

		createNode(content.getGenericADEComponent(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractCityObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_CITY_OBJECT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		if (content.isSetCreationDate())
			targetNode.setProperty("creationDate", formatter.format(content.getCreationDate().getTime()) + "");

		if (content.isSetTerminationDate())
			targetNode.setProperty("terminationDate", formatter.format(content.getTerminationDate().getTime()) + "");

		createNode(content.getExternalReference(), targetNode, GMLRelTypes.EXTERNAL_REFERENCE);

		createNode(content.getGenericAttribute(), targetNode, GMLRelTypes.GENERIC_ATTRIBUTE);

		createNode(content.getGeneralizesTo(), targetNode, GMLRelTypes.GENERALIZES_TO);

		// <xs:enumeration value="entirelyAboveTerrain"/>
		// <xs:enumeration value="substantiallyAboveTerrain"/>
		// <xs:enumeration value="substantiallyAboveAndBelowTerrain"/>
		// <xs:enumeration value="substantiallyBelowTerrain"/>
		// <xs:enumeration value="entirelyBelowTerrain"/>
		createNode(content.getRelativeToTerrain(), targetNode, GMLRelTypes.RELATIVE_TO_TERRAIN);

		// <xs:enumeration value="entirelyAboveWaterSurface"/>
		// <xs:enumeration value="substantiallyAboveWaterSurface"/>
		// <xs:enumeration value="substantiallyAboveAndBelowWaterSurface"/>
		// <xs:enumeration value="substantiallyBelowWaterSurface"/>
		// <xs:enumeration value="entirelyBelowWaterSurface"/>
		// <xs:enumeration value="temporarilyAboveAndBelowWaterSurface"/>
		createNode(content.getRelativeToWater(), targetNode, GMLRelTypes.RELATIVE_TO_WATER);

		createNode(content.getAppearance(), targetNode, GMLRelTypes.APPEARANCE);

		createNode(content.getGenericApplicationPropertyOfCityObject(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractFeature) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractBoundarySurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_BUILDING_BOUNDARY_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLod2MultiSurface(), targetNode, GMLRelTypes.LOD2_MULTI_SURFACE);

		createNode(content.getLod3MultiSurface(), targetNode, GMLRelTypes.LOD3_MULTI_SURFACE);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getOpening(), targetNode, GMLRelTypes.OPENING);

		createNode(content.getGenericApplicationPropertyOfBoundarySurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CeilingSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_CEILING_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfCeilingSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ClosureSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_CLOSURE_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfClosureSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(FloorSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_FLOOR_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfFloorSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GroundSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_GROUND_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfGroundSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InteriorWallSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INTERIOR_BUILDING_WALL_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfInteriorWallSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OuterCeilingSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.OUTER_BUILDING_CEILING_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfOuterCeilingSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OuterFloorSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.OUTER_BUILDING_FLOOR_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfOuterFloorSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RoofSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_ROOF_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfRoofSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(WallSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_WALL_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfWallSurface(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBoundarySurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractOpening content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_BUILDING_OPENING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLod3MultiSurface(), targetNode, GMLRelTypes.LOD3_MULTI_SURFACE);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getLod3ImplicitRepresentation(), targetNode, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode(content.getGenericApplicationPropertyOfOpening(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Door content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_DOOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddress(), targetNode, GMLRelTypes.ADDRESS);

		createNode(content.getGenericApplicationPropertyOfDoor(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractOpening) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Window content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_WINDOW);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfWindow(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractOpening) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractReliefComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_RELIEF_COMPONENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetLod())
			targetNode.setProperty("lod", content.getLod() + "");

		createNode(content.getExtent(), targetNode, GMLRelTypes.EXTENT);

		createNode(content.getGenericApplicationPropertyOfReliefComponent(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BreaklineRelief content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BREAKLINE_RELIEF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getRidgeOrValleyLines(), targetNode, GMLRelTypes.RIDGE_OR_VALLEY_LINES);

		createNode(content.getBreaklines(), targetNode, GMLRelTypes.BREAK_LINES);

		createNode(content.getGenericApplicationPropertyOfBreaklineRelief(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractReliefComponent) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MassPointRelief content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.MASSPOINT_RELIEF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getReliefPoints(), targetNode, GMLRelTypes.RELIEF_POINTS);

		createNode(content.getGenericApplicationPropertyOfMassPointRelief(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractReliefComponent) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RasterRelief content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.RASTER_RELIEF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGrid(), targetNode, GMLRelTypes.GRID);

		createNode(content.getGenericApplicationPropertyOfRasterRelief(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractReliefComponent) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TINRelief content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TIN_RELIEF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getTin(), targetNode, GMLRelTypes.TIN);

		createNode(content.getGenericApplicationPropertyOfTinRelief(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractReliefComponent) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractSite content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_SITE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericApplicationPropertyOfSite(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractBuilding content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_BUILDING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

		if (content.isSetYearOfConstruction())
			targetNode.setProperty("yearOfConstruction", formatter.format(content.getYearOfConstruction().getTime()) + "");

		if (content.isSetYearOfDemolition())
			targetNode.setProperty("yearOfDemolition", formatter.format(content.getYearOfDemolition().getTime()) + "");

		createNode(content.getRoofType(), targetNode, GMLRelTypes.ROOF_TYPE);

		createNode(content.getMeasuredHeight(), targetNode, GMLRelTypes.MEASURED_HEIGHT);

		if (content.isSetStoreysAboveGround())
			targetNode.setProperty("storeysAboveGround", content.getStoreysAboveGround());

		if (content.isSetStoreysBelowGround())
			targetNode.setProperty("storeysBelowGround", content.getStoreysBelowGround());

		createNodeSearchHierarchy(content.getStoreyHeightsAboveGround(), targetNode, GMLRelTypes.STOREY_HEIGHTS_ABOVE_GROUND);

		createNodeSearchHierarchy(content.getStoreyHeightsBelowGround(), targetNode, GMLRelTypes.STOREY_HEIGHTS_BELOW_GROUND);

		createNode(content.getLod1Solid(), targetNode, GMLRelTypes.LOD1_SOLID);

		createNode(content.getLod2Solid(), targetNode, GMLRelTypes.LOD2_SOLID);

		createNode(content.getLod3Solid(), targetNode, GMLRelTypes.LOD3_SOLID);

		createNode(content.getLod4Solid(), targetNode, GMLRelTypes.LOD4_SOLID);

		createNode(content.getLod1TerrainIntersection(), targetNode, GMLRelTypes.LOD1_TERRAIN_INTERSECTION);

		createNode(content.getLod2TerrainIntersection(), targetNode, GMLRelTypes.LOD2_TERRAIN_INTERSECTION);

		createNode(content.getLod3TerrainIntersection(), targetNode, GMLRelTypes.LOD3_TERRAIN_INTERSECTION);

		createNode(content.getLod4TerrainIntersection(), targetNode, GMLRelTypes.LOD4_TERRAIN_INTERSECTION);

		createNode(content.getLod2MultiCurve(), targetNode, GMLRelTypes.LOD2_MULTI_CURVE);

		createNode(content.getLod3MultiCurve(), targetNode, GMLRelTypes.LOD3_MULTI_CURVE);

		createNode(content.getLod4MultiCurve(), targetNode, GMLRelTypes.LOD4_MULTI_CURVE);

		createNode(content.getLod0FootPrint(), targetNode, GMLRelTypes.LOD0_FOOT_PRINT);

		createNode(content.getLod0RoofEdge(), targetNode, GMLRelTypes.LOD0_ROOF_EDGE);

		createNode(content.getLod1MultiSurface(), targetNode, GMLRelTypes.LOD1_MULTI_SURFACE);

		createNode(content.getLod2MultiSurface(), targetNode, GMLRelTypes.LOD2_MULTI_SURFACE);

		createNode(content.getLod3MultiSurface(), targetNode, GMLRelTypes.LOD3_MULTI_SURFACE);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getOuterBuildingInstallation(), targetNode, GMLRelTypes.OUTER_BUILDING_INSTALLATION);

		createNode(content.getInteriorBuildingInstallation(), targetNode, GMLRelTypes.INTERIOR_BUILDING_INSTALLATION);

		createNode(content.getBoundedBySurface(), targetNode, GMLRelTypes.BOUNDED_BY_SURFACE);

		createNode(content.getConsistsOfBuildingPart(), targetNode, GMLRelTypes.BUILDING_PART);

		createNode(content.getInteriorRoom(), targetNode, GMLRelTypes.INTERIOR_ROOM);

		createNode(content.getAddress(), targetNode, GMLRelTypes.ADDRESS);

		createNode(content.getGenericApplicationPropertyOfAbstractBuilding(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractSite) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Building content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// DOES NOT WORK IN CHUNK READING MODE (DUE TO XLINKS)
		// BOUNDING SHAPE FOR BUILDING/BUILDING_PART GEOMETRIC COMPARISON
		// if (content.isSetBoundedBy()) {
		// createNode(content.getBoundedBy(), targetNode, GMLRelTypes.BOUNDED_BY);
		// parent.setProperty(InteralMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "false");
		// } else {
		// createNode(content.calcBoundedBy(false), targetNode, GMLRelTypes.BOUNDED_BY);
		// parent.setProperty(InteralMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "true");
		// }

		createNode(content.getGenericApplicationPropertyOfBuilding(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBuilding) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingPart content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_PART);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// DOES NOT WORK IN CHUNK READING MODE (DUE TO XLINKS)
		// BOUNDING SHAPE FOR BUILDING/BUILDING_PART GEOMETRIC COMPARISON
		// if (content.isSetBoundedBy()) {
		// createNode(content.getBoundedBy(), targetNode, GMLRelTypes.BOUNDED_BY);
		// parent.setProperty(InteralMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "false");
		// } else {
		// createNode(content.calcBoundedBy(false), targetNode, GMLRelTypes.BOUNDED_BY);
		// parent.setProperty(InteralMappingProperties.BOUNDING_SHAPE_CREATED.toString(), "true");
		// }

		createNode(content.getGenericApplicationPropertyOfBuildingPart(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractBuilding) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingFurniture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_FURNITURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNodeSearchHierarchy(content.getLod4Geometry(), targetNode, GMLRelTypes.LOD4_GEOMETRY);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		createNode(content.getGenericApplicationPropertyOfBuildingFurniture(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingInstallation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_INSTALLATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNodeSearchHierarchy(content.getLod2Geometry(), targetNode, GMLRelTypes.LOD2_GEOMETRY);

		createNodeSearchHierarchy(content.getLod3Geometry(), targetNode, GMLRelTypes.LOD3_GEOMETRY);

		createNodeSearchHierarchy(content.getLod4Geometry(), targetNode, GMLRelTypes.LOD4_GEOMETRY);

		createNode(content.getLod2ImplicitRepresentation(), targetNode, GMLRelTypes.LOD2_IMPLICIT_REPRESENTATION);

		createNode(content.getLod3ImplicitRepresentation(), targetNode, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		createNode(content.getBoundedBySurface(), targetNode, GMLRelTypes.BOUNDED_BY_SURFACE);

		createNode(content.getGenericApplicationPropertyOfBuildingInstallation(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityFurniture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_FURNITURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNodeSearchHierarchy(content.getLod1Geometry(), targetNode, GMLRelTypes.LOD1_GEOMETRY);

		createNodeSearchHierarchy(content.getLod2Geometry(), targetNode, GMLRelTypes.LOD2_GEOMETRY);

		createNodeSearchHierarchy(content.getLod3Geometry(), targetNode, GMLRelTypes.LOD3_GEOMETRY);

		createNodeSearchHierarchy(content.getLod4Geometry(), targetNode, GMLRelTypes.LOD4_GEOMETRY);

		createNode(content.getLod1TerrainIntersection(), targetNode, GMLRelTypes.LOD1_TERRAIN_INTERSECTION);

		createNode(content.getLod2TerrainIntersection(), targetNode, GMLRelTypes.LOD2_TERRAIN_INTERSECTION);

		createNode(content.getLod3TerrainIntersection(), targetNode, GMLRelTypes.LOD3_TERRAIN_INTERSECTION);

		createNode(content.getLod4TerrainIntersection(), targetNode, GMLRelTypes.LOD4_TERRAIN_INTERSECTION);

		createNode(content.getLod1ImplicitRepresentation(), targetNode, GMLRelTypes.LOD1_IMPLICIT_REPRESENTATION);

		createNode(content.getLod2ImplicitRepresentation(), targetNode, GMLRelTypes.LOD2_IMPLICIT_REPRESENTATION);

		createNode(content.getLod3ImplicitRepresentation(), targetNode, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		createNode(content.getGenericApplicationPropertyOfCityFurniture(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectGroup content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_OBJECT_GROUP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNode(content.getGroupMember(), targetNode, GMLRelTypes.GROUP_MEMBER);

		createNode(content.getGroupParent(), targetNode, GMLRelTypes.GROUP_PARENT);

		createNodeSearchHierarchy(content.getGeometry(), targetNode, GMLRelTypes.GEOMETRY);

		createNode(content.getGenericApplicationPropertyOfCityObjectGroup(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GenericCityObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.GENERIC_CITY_OBJECT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNodeSearchHierarchy(content.getLod0Geometry(), targetNode, GMLRelTypes.LOD0_GEOMETRY);

		createNodeSearchHierarchy(content.getLod1Geometry(), targetNode, GMLRelTypes.LOD1_GEOMETRY);

		createNodeSearchHierarchy(content.getLod2Geometry(), targetNode, GMLRelTypes.LOD2_GEOMETRY);

		createNodeSearchHierarchy(content.getLod3Geometry(), targetNode, GMLRelTypes.LOD3_GEOMETRY);

		createNodeSearchHierarchy(content.getLod4Geometry(), targetNode, GMLRelTypes.LOD4_GEOMETRY);

		createNode(content.getLod0TerrainIntersection(), targetNode, GMLRelTypes.LOD0_TERRAIN_INTERSECTION);

		createNode(content.getLod1TerrainIntersection(), targetNode, GMLRelTypes.LOD1_TERRAIN_INTERSECTION);

		createNode(content.getLod2TerrainIntersection(), targetNode, GMLRelTypes.LOD2_TERRAIN_INTERSECTION);

		createNode(content.getLod3TerrainIntersection(), targetNode, GMLRelTypes.LOD3_TERRAIN_INTERSECTION);

		createNode(content.getLod4TerrainIntersection(), targetNode, GMLRelTypes.LOD4_TERRAIN_INTERSECTION);

		createNode(content.getLod0ImplicitRepresentation(), targetNode, GMLRelTypes.LOD0_IMPLICIT_REPRESENTATION);

		createNode(content.getLod1ImplicitRepresentation(), targetNode, GMLRelTypes.LOD1_IMPLICIT_REPRESENTATION);

		createNode(content.getLod2ImplicitRepresentation(), targetNode, GMLRelTypes.LOD2_IMPLICIT_REPRESENTATION);

		createNode(content.getLod3ImplicitRepresentation(), targetNode, GMLRelTypes.LOD3_IMPLICIT_REPRESENTATION);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(HollowSpace content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.HOLLOW_SPACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNode(content.getBoundedBySurface(), targetNode, GMLRelTypes.BOUNDED_BY_SURFACE);

		createNode(content.getInteriorFurniture(), targetNode, GMLRelTypes.INTERIOR_FURNITURE);

		createNode(content.getHollowSpaceInstallation(), targetNode, GMLRelTypes.HOLLOW_SPACE_INSTALLATION);

		createNode(content.getLod4Solid(), targetNode, GMLRelTypes.LOD4_SOLID);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getGenericApplicationPropertyOfHollowSpace(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(IntBuildingInstallation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INT_BUILDING_INSTALLATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNodeSearchHierarchy(content.getLod4Geometry(), targetNode, GMLRelTypes.LOD4_GEOMETRY);

		createNode(content.getLod4ImplicitRepresentation(), targetNode, GMLRelTypes.LOD4_IMPLICIT_REPRESENTATION);

		createNode(content.getBoundedBySurface(), targetNode, GMLRelTypes.BOUNDED_BY_SURFACE);

		createNode(content.getGenericApplicationPropertyOfIntBuildingInstallation(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LandUse content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.LAND_USE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNode(content.getLod0MultiSurface(), targetNode, GMLRelTypes.LOD0_MULTI_SURFACE);

		createNode(content.getLod1MultiSurface(), targetNode, GMLRelTypes.LOD1_MULTI_SURFACE);

		createNode(content.getLod2MultiSurface(), targetNode, GMLRelTypes.LOD2_MULTI_SURFACE);

		createNode(content.getLod3MultiSurface(), targetNode, GMLRelTypes.LOD3_MULTI_SURFACE);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getGenericApplicationPropertyOfLandUse(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ReliefFeature content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.RELIEF_FEATURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetLod())
			targetNode.setProperty("lod", content.getLod() + "");

		createNode(content.getReliefComponent(), targetNode, GMLRelTypes.RELIEF_COMPONENT);

		createNode(content.getGenericApplicationPropertyOfReliefFeature(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Room content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_ROOM);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getClazz(), targetNode, GMLRelTypes.CLAZZ);

		createNode(content.getFunction(), targetNode, GMLRelTypes.FUNCTION);

		createNode(content.getUsage(), targetNode, GMLRelTypes.USAGE);

		createNode(content.getLod4Solid(), targetNode, GMLRelTypes.LOD4_SOLID);

		createNode(content.getLod4MultiSurface(), targetNode, GMLRelTypes.LOD4_MULTI_SURFACE);

		createNode(content.getBoundedBySurface(), targetNode, GMLRelTypes.BOUNDED_BY_SURFACE);

		createNode(content.getInteriorFurniture(), targetNode, GMLRelTypes.INTERIOR_FURNITURE);

		createNode(content.getRoomInstallation(), targetNode, GMLRelTypes.ROOM_INSTALLATION);

		createNode(content.getGenericApplicationPropertyOfRoom(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractCityObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractCoverage content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_COVERAGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getRangeSet(), targetNode, GMLRelTypes.RANGE_SET);

		if (content.isSetDimension())
			targetNode.setProperty("dimension", content.getDimension());

		createNode((AbstractFeature) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractDiscreteCoverage content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_DISCRETE_COVERAGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCoverageFunction(), targetNode, GMLRelTypes.COVERAGE_FUNCTION);

		createNode((AbstractCoverage) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RectifiedGridCoverage content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RECTIFIED_GRID_COVERAGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getRectifiedGridDomain(), targetNode, GMLRelTypes.RECTIFIED_GRID_DOMAIN);

		createNode((AbstractDiscreteCoverage) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractFeatureCollection content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_FEATURE_COLLECTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getFeatureMember(), targetNode, GMLRelTypes.FEATURE_MEMBER);

		createNode(content.getFeatureMembers(), targetNode, GMLRelTypes.FEATURE_MEMBERS);

		createNode((AbstractFeature) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityModel content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_MODEL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCityObjectMember(), targetNode, GMLRelTypes.CITY_OBJECT_MEMBER);

		createNode(content.getAppearanceMember(), targetNode, GMLRelTypes.APPERANCE_MEMBER);

		createNode(content.getGenericApplicationPropertyOfCityModel(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractFeatureCollection) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractSurfaceData content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_SURFACE_DATA);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetIsFront())
			targetNode.setProperty("isFront", content.getIsFront() + "");

		createNode(content.getGenericApplicationPropertyOfSurfaceData(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractFeature) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractTexture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_TEXTURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetImageURI())
			targetNode.setProperty("imageURI", content.getImageURI() + "");

		createNode(content.getMimeType(), targetNode, GMLRelTypes.MIME_TYPE);

		createNode(content.getTextureType(), targetNode, GMLRelTypes.TEXTURE_TYPE);

		createNode(content.getWrapMode(), targetNode, GMLRelTypes.WRAP_MODE);

		createNode(content.getBorderColor(), targetNode, GMLRelTypes.BORDER_COLOR);

		createNode(content.getGenericApplicationPropertyOfTexture(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractSurfaceData) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GeoreferencedTexture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.GEOREFERENCED_TEXTURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetPreferWorldFile())
			targetNode.setProperty("preferWorldFile", content.getPreferWorldFile() + "");

		createNodeSearchHierarchy(content.getReferencePoint(), targetNode, GMLRelTypes.REFERENCE_POINT);

		createNode(content.getOrientation(), targetNode, GMLRelTypes.ORIENTATION);

		createNode(content.getTarget(), targetNode, GMLRelTypes.TARGET);

		createNode(content.getGenericApplicationPropertyOfGeoreferencedTexture(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractTexture) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Matrix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("MATRIX");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("nrOfCols", content.getColumnDimension() + "");

		targetNode.setProperty("nrOfRows", content.getRowDimension() + "");

		String str = "[";
		for (int i = 0; i < content.getRowDimension(); i++) {
			str += "[";
			for (int j = 0; j < content.getColumnDimension(); j++) {
				str += content.getArray()[i][j] + ",";
			}
			str = str.substring(0, str.length());
			str += "],";
		}
		str = str.substring(0, str.length());
		str += "]";

		targetNode.setProperty("matrixContent", str);

		return targetNode;
	}

	@Override
	public Node createNode(ParameterizedTexture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.PARAMETERIZED_TEXTURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getTarget(), targetNode, GMLRelTypes.TARGET);

		createNode(content.getGenericApplicationPropertyOfParameterizedTexture(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractTexture) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(X3DMaterial content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.X3D_MATERIAL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetAmbientIntensity())
			targetNode.setProperty("ambientIntensity", content.getAmbientIntensity() + "");

		createNode(content.getDiffuseColor(), targetNode, GMLRelTypes.DIFFUSE_COLOR);

		createNode(content.getEmissiveColor(), targetNode, GMLRelTypes.EMISSIVE_COLOR);

		createNode(content.getSpecularColor(), targetNode, GMLRelTypes.SPECULAR_COLOR);

		if (content.isSetShininess())
			targetNode.setProperty("shininess", content.getShininess() + "");

		if (content.isSetTransparency())
			targetNode.setProperty("transparency", content.getShininess() + "");

		if (content.isSetIsSmooth())
			targetNode.setProperty("isSmooth", content.getIsSmooth() + "");

		createNode(content.getTarget(), targetNode, GMLRelTypes.TARGET);

		createNode(content.getGenericApplicationPropertyOfX3DMaterial(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractSurfaceData) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(org.citygml4j.model.citygml.core.Address content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ADDRESS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getXalAddress(), targetNode, GMLRelTypes.XAL_ADDRESS);

		createNode(content.getMultiPoint(), targetNode, GMLRelTypes.MULTI_POINT);

		createNode(content.getGenericApplicationPropertyOfAddress(), targetNode, GMLRelTypes.ADE);

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((CoreModuleComponent) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Appearance content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.APPEARANCE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetTheme())
			targetNode.setProperty("theme", content.getTheme());

		createNode(content.getSurfaceDataMember(), targetNode, GMLRelTypes.SURFACE_DATA_MEMBER);

		createNode(content.getGenericApplicationPropertyOfAppearance(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractFeature) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractGeometry content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_GEOMETRY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetGid())
			targetNode.setProperty("gid", content.getGid() + "");

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractGeometricAggregate content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_GEOMETRIC_AGGREGATE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometry) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiCurve content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_CURVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCurveMember(), targetNode, GMLRelTypes.CURVE_MEMBER);

		createNode(content.getCurveMembers(), targetNode, GMLRelTypes.CURVE_MEMBERS);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiGeometry content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_GEOMETRY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGeometryMember(), targetNode, GMLRelTypes.GEOMETRY_MEMBER);

		createNodeSearchHierarchy(content.getGeometryMembers(), targetNode, GMLRelTypes.GEOMETRY_MEMBERS);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiLineString content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_LINE_STRING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLineStringMember(), targetNode, GMLRelTypes.LINE_STRING_MEMBER);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiPoint content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_POINT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPointMember(), targetNode, GMLRelTypes.POINT_MEMBER);

		createNode(content.getPointMembers(), targetNode, GMLRelTypes.POINT_MEMBERS);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiPolygon content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_POLYGON);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPolygonMember(), targetNode, GMLRelTypes.POLYGON_MEMBER);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiSolid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_SOLID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSolidMember(), targetNode, GMLRelTypes.SOLID_MEMBER);

		createNode(content.getSolidMembers(), targetNode, GMLRelTypes.SOLID_MEMBERS);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSurfaceMember(), targetNode, GMLRelTypes.SURFACE_MEMBER);

		createNode(content.getSurfaceMembers(), targetNode, GMLRelTypes.SURFACE_MEMBERS);

		createNode((AbstractGeometricAggregate) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractGeometricPrimitive content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_GEOMETRIC_PRIMITIVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometry) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractCurve content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_CURVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometricPrimitive) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeCurve content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_CURVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCurveMember(), targetNode, GMLRelTypes.CURVE_MEMBER);

		createNode((AbstractCurve) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Curve content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CURVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSegments(), targetNode, GMLRelTypes.SEGMENTS);

		createNode((AbstractCurve) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LineString content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINE_STRING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPosOrPointPropertyOrPointRepOrCoord(), targetNode, GMLRelTypes.CONTROL_POINTS);

		createNode(content.getPosList(), targetNode, GMLRelTypes.POS_LIST);

		createNode(content.getCoordinates(), targetNode, GMLRelTypes.COORDINATES);

		createNode((AbstractCurve) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OrientableCurve content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ORIENTABLE_CURVE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getBaseCurve(), targetNode, GMLRelTypes.BASE_CURVE);

		createNode(content.getOrientation(), targetNode, GMLRelTypes.ORIENTATION);

		createNode((AbstractCurve) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractSolid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_SOLID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometricPrimitive) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeSolid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_SOLID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSolidMember(), targetNode, GMLRelTypes.SOLID_MEMBER);

		createNode((AbstractSolid) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Solid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SOLID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getExterior(), targetNode, GMLRelTypes.EXTERIOR);

		createNode(content.getInterior(), targetNode, GMLRelTypes.INTERIOR);

		createNode((AbstractSolid) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometricPrimitive) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSurfaceMember(), targetNode, GMLRelTypes.SURFACE_MEMBER);

		createNode((AbstractSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OrientableSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ORIENTABLE_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getBaseSurface(), targetNode, GMLRelTypes.BASE_SURFACE);

		createNode(content.getOrientation(), targetNode, GMLRelTypes.ORIENTATION);

		createNode((AbstractSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(_TexturedSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass._TEXTURED_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAppearance(), targetNode, GMLRelTypes.APPEARANCE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((OrientableSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Polygon content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POLYGON);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getExterior(), targetNode, GMLRelTypes.EXTERIOR);

		createNode(content.getInterior(), targetNode, GMLRelTypes.INTERIOR);

		createNode((AbstractSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Surface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getPatches(), targetNode, GMLRelTypes.PATCHES);

		createNode((AbstractSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TriangulatedSurface content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.TRIANGULATED_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((Surface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Tin content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.TIN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getStopLines(), targetNode, GMLRelTypes.STOP_LINES);

		createNode(content.getBreakLines(), targetNode, GMLRelTypes.BREAK_LINES);

		createNode(content.getMaxLength(), targetNode, GMLRelTypes.MAX_LENGTH);

		createNode(content.getControlPoint(), targetNode, GMLRelTypes.CONTROL_POINT);

		createNode((TriangulatedSurface) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Point content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POINT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPos(), targetNode, GMLRelTypes.POS);

		createNode(content.getCoordinates(), targetNode, GMLRelTypes.COORDINATES);

		createNode(content.getCoord(), targetNode, GMLRelTypes.COORD);

		createNode((AbstractGeometricPrimitive) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractRing content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_RING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractGeometry) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LinearRing content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINEAR_RING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPosOrPointPropertyOrPointRep(), targetNode, GMLRelTypes.CONTROL_POINTS);

		createNode(content.getPosList(), targetNode, GMLRelTypes.POS_LIST);

		createNode(content.getCoordinates(), targetNode, GMLRelTypes.COORDINATES);

		createNode(content.getCoord(), targetNode, GMLRelTypes.COORD);

		createNode((AbstractRing) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Ring content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RING);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCurveMember(), targetNode, GMLRelTypes.CURVE_MEMBER);

		createNode((AbstractRing) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GeometricComplex content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRIC_COMPLEX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getElement(), targetNode, GMLRelTypes.ELEMENT);

		createNode((AbstractGeometry) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Grid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GRID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLimits(), targetNode, GMLRelTypes.LIMITS);

		createNode(content.getAxisName(), targetNode, GMLRelTypes.AXIS_NAME);

		if (content.isSetDimension())
			targetNode.setProperty("dimension", content.getDimension() + "");

		createNode((AbstractGeometry) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RectifiedGrid content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RECTIFIED_GRID);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getOrigin(), targetNode, GMLRelTypes.ORIGIN);

		createNode(content.getOffsetVector(), targetNode, GMLRelTypes.OFFSET_VECTOR);

		createNode((Grid) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractTextureParameterization content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_TEXTURE_PARAMETERIZATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericADEComponent(), targetNode, GMLRelTypes.GENERIC_ADE_COMPONENT);

		createNode(content.getGenericApplicationPropertyOfTextureParameterization(), targetNode, GMLRelTypes.ADE);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TexCoordGen content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TEX_COORD_GEN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getWorldToTexture(), targetNode, GMLRelTypes.WORLD_TO_TEXTURE);

		createNode(content.getGenericApplicationPropertyOfTexCoordGen(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractTextureParameterization) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TexCoordList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TEX_COORD_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getTextureCoordinates(), targetNode, GMLRelTypes.TEXTURE_COORDINATES);

		createNode(content.getGenericApplicationPropertyOfTexCoordList(), targetNode, GMLRelTypes.ADE);

		createNode((AbstractTextureParameterization) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeValue content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_VALUE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValueComponent(), targetNode, GMLRelTypes.VALUE_COMPONENT);

		createNode(content.getValueComponents(), targetNode, GMLRelTypes.VALUE_COMPONENTS);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ValueArray content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE_ARRAY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetCodeSpace())
			targetNode.setProperty("codeSpace", content.getCodeSpace() + "");

		if (content.isSetUom())
			targetNode.setProperty("uom", content.getUom() + "");

		createNode((CompositeValue) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ImplicitGeometry content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.IMPLICIT_GEOMETRY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getMimeType(), targetNode, GMLRelTypes.MIME_TYPE);

		createNode(content.getTransformationMatrix(), targetNode, GMLRelTypes.TRANSFORMATION_MATRIX);

		if (content.isSetLibraryObject())
			targetNode.setProperty("libraryObject", content.getLibraryObject() + "");

		createNodeSearchHierarchy(content.getRelativeGMLGeometry(), targetNode, GMLRelTypes.RELATIVE_GEOMETRY);

		createNodeSearchHierarchy(content.getReferencePoint(), targetNode, GMLRelTypes.REFERENCE_POINT);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AbstractGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractSurfacePatch content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_SURFACE_PATCH);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		return targetNode;
	}

	@Override
	public Node createNode(Rectangle content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RECTANGLE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getExterior(), targetNode, GMLRelTypes.EXTERIOR);

		createNode(content.getInterpolation(), targetNode, GMLRelTypes.INTERPOLATION);

		// TODO parent

		createNode((AbstractSurfacePatch) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Triangle content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.TRIANGLE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getExterior(), targetNode, GMLRelTypes.EXTERIOR);

		createNode(content.getInterpolation(), targetNode, GMLRelTypes.INTERPOLATION);

		// TODO parent

		createNode((AbstractSurfacePatch) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AddressDetails content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_DETAILS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPostalServiceElements(), targetNode, GMLRelTypes.POSTAL_SERVICE_ELEMENTS);

		createNode(content.getAddress(), targetNode, GMLRelTypes.ADDRESS);

		createNode(content.getAddressLines(), targetNode, GMLRelTypes.ADDRESS_LINES);

		createNode(content.getCountry(), targetNode, GMLRelTypes.COUNTRY);

		createNode(content.getAdministrativeArea(), targetNode, GMLRelTypes.ADMINISTRATIVE_AREA);

		createNode(content.getLocality(), targetNode, GMLRelTypes.LOCALITY);

		createNode(content.getThoroughfare(), targetNode, GMLRelTypes.THOROUGHFARE);

		if (content.isSetAddressType())
			targetNode.setProperty("addressType", content.getAddressType() + "");

		if (content.isSetCurrentStatus())
			targetNode.setProperty("currentStatus", content.getCurrentStatus() + "");

		if (content.isSetValidToDate())
			targetNode.setProperty("validFromDate", content.getValidFromDate() + "");

		if (content.isSetValidToDate())
			targetNode.setProperty("validToDate", content.getValidToDate() + "");

		if (content.isSetUsage())
			targetNode.setProperty("usage", content.getUsage() + "");

		if (content.isSetAddressDetailsKey())
			targetNode.setProperty("addressDetailsKey", content.getAddressDetailsKey() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(GenericValueObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GENERIC_VALUE_OBJECT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// TODO content

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(MetaData content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.META_DATA);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// TODO content

		// TODO parent

		return targetNode;

	}

	@Override
	public Node createNode(Value content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValueObject(), targetNode, GMLRelTypes.VALUE_OBJECT);

		createNodeSearchHierarchy(content.getGeometry(), targetNode, GMLRelTypes.GEOMETRY);

		createNode(content.getGenericValueObject(), targetNode, GMLRelTypes.GENEREIC_VALUE_OBJECT);

		createNode(content.getNull(), targetNode, GMLRelTypes._NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ValueObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE_OBJECT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getScalarValue(), targetNode, GMLRelTypes.SCALAR_VALUE);

		createNode(content.getScalarValueList(), targetNode, GMLRelTypes.SCALAR_VALUE_LIST);

		createNode(content.getValueExtent(), targetNode, GMLRelTypes.VALUE_EXTENT);

		createNodeSearchHierarchy(content.getCompositeValue(), targetNode, GMLRelTypes.COMPOSITE_VALUE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Child content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CHILD");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ModelObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(_Color content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._COLOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("red", content.getRed() + "");

		targetNode.setProperty("green", content.getGreen() + "");

		targetNode.setProperty("blue", content.getBlue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AbstractGenericAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ABSTRACT_GENERIC_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetName())
			targetNode.setProperty("name", content.getName());

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DateAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.DATE_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

		if (content.isSetValue())
			targetNode.setProperty("value", formatter.format(content.getValue().getTime()) + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(DoubleAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.DOUBLE_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GenericAttributeSet content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.GENERIC_ATTRIBUTE_SET);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericAttribute(), targetNode, GMLRelTypes.GENERIC_ATTRIBUTE);

		if (content.isSetCodeSpace())
			targetNode.setProperty("codeSpace", content.getCodeSpace() + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(IntAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INT_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MeasureAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.MEASURE_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getValue(), targetNode, GMLRelTypes.VALUE);

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(StringAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.STRING_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(UriAttribute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.URI_ATTRIBUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		createNode((AbstractGenericAttribute) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(org.citygml4j.model.xal.Address content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressIdentifier content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_IDENTIFIER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetIdentifierType())
			targetNode.setProperty("identifierType", content.getIdentifierType() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLatitude content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LATITUDE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLatitudeDirection content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LATITUDE_DIRECTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLine content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LINE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLines content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LINES);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLongitude content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LONGITUDE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AddressLongitudeDirection content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADDRESS_LONGITUDE_DIRECTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ADEComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ADE_COMPONENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// TODO content

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AdministrativeArea content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADMINISTRATIVE_AREA);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getAdministrativeAreaName(), targetNode, GMLRelTypes.ADMINISTRATIVE_AREA_NAME);

		createNode(content.getSubAdministrativeArea(), targetNode, GMLRelTypes.SUB_ADMINISTRATIVE_AREA);

		createNode(content.getLocality(), targetNode, GMLRelTypes.LOCALITY);

		createNode(content.getPostOffice(), targetNode, GMLRelTypes.POST_OFFICE);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetUsageType())
			targetNode.setProperty("usageType", content.getUsageType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(AdministrativeAreaName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ADMINISTRATIVE_AREA_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public <T extends Associable & Child> Node createNode(ArrayAssociation<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ARRAY_ASSOCIATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getObject(), targetNode, GMLRelTypes.OBJECT);

		// TODO localProperties

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CurveSegmentArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CURVE_SEGMENT_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ArrayAssociation<AbstractCurveSegment>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(FeatureArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.FEATURE_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericADEComponent(), targetNode, GMLRelTypes.GENERIC_ADE_COMPONENT);

		createNode((ArrayAssociation<AbstractFeature>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends AbstractGeometry> Node createNode(GeometryArrayProperty<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRY_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ArrayAssociation<T>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CurveArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CURVE_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryArrayProperty<AbstractCurve>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(PointArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POINT_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryArrayProperty<Point>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SolidArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SOLID_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryArrayProperty<AbstractSolid>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SurfaceArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SURFACE_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryArrayProperty<AbstractSurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LineStringSegmentArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINE_STRING_SEGMENT_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ArrayAssociation<LineStringSegment>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SurfacePatchArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SURFACE_PATCH_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ArrayAssociation<AbstractSurfacePatch>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TrianglePatchArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.TRIANGLE_PATCH_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((SurfacePatchArrayProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ValueArrayProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE_ARRAY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ArrayAssociation<Value>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends Associable & Child> Node createNode(AssociationByRep<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ASSOCIATION_BY_REP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNodeSearchHierarchy(content.getObject(), targetNode, GMLRelTypes.OBJECT);

		// TODO localProperties

		// TODO parent

		return targetNode;
	}

	@Override
	public <T extends Associable & Child> Node createNode(AssociationByRepOrRef<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ASSOCIATION_BY_REP_OR_REF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getActuate(), parent, GMLRelTypes.ACTUATE);

		if (content.isSetArcrole())
			parent.setProperty("arcrole", content.getArcrole() + "");

		if (content.isSetHref()) {
			String href = content.getHref();

			parent.setProperty("href", href);

			// remember this node for post processing (to link to referenced object)
			if (SETTINGS.ENABLE_INDICES) {
				hrefApiIndex.add(targetNode, "href", href);
				// hrefValues.add(href);
			} else {
				ArrayList<Node> nodes = hrefIndex.get(href);
				if (nodes == null || nodes.isEmpty()) {
					nodes = new ArrayList<Node>();
				}
				nodes.add(targetNode);
				hrefIndex.put(href, nodes);
			}
		}

		if (content.isSetRemoteSchema())
			parent.setProperty("remoteSchema", content.getRemoteSchema() + "");

		if (content.isSetRole())
			parent.setProperty("role", content.getRole() + "");

		createNode(content.getShow(), parent, GMLRelTypes.SHOW);

		if (content.isSetTitle())
			parent.setProperty("title", content.getTitle() + "");

		// createNode(content.getType(), parent, GMLRelTypes.TYPE);

		createNode((AssociationByRep<T>) content, parent, null);

		return targetNode;
	}

	@Override
	public Node createNode(_AppearanceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._APPEARANCE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetOrientation())
			targetNode.setProperty("orientation", content.getOrientation() + "");

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AssociationByRepOrRef<_AbstractAppearance>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends AbstractFeature> Node createNode(FeatureProperty<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.FEATURE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGenericADEComponent(), targetNode, GMLRelTypes.GENERIC_ADE_COMPONENT);

		createNode((AssociationByRepOrRef<T>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AddressProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.ADDRESS_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<org.citygml4j.model.citygml.core.Address>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AppearanceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.APPEARANCE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<Appearance>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AppearanceMember content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.APPEARANCE_MEMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AppearanceProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AuxiliaryTrafficAreaProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.AUXILIARY_TRAFFIC_AREA_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AuxiliaryTrafficArea>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BoundarySurfaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_BOUNDARY_SURFACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractBoundarySurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BoundedByWaterSurfaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BOUNDED_BY_WATER_SURFACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractWaterBoundarySurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BridgeConstructionElementProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BRIDGE_CONSTRUCTION_ELEMENT_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BridgeConstructionElement>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BridgeInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BRIDGE_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BridgeInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BridgePartProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BRIDGE_PART_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BridgePart>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BuildingInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingPartProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_PART_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BuildingPart>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectGroupMember content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_OBJECT_GROUP_MEMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetGroupRole())
			targetNode.setProperty("groupRole", content.getGroupRole() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractCityObject>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectGroupParent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_OBJECT_GROUP_PARENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractCityObject>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectMember content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.CITY_OBJECT_MEMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractCityObject>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(FeatureMember content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.FEATURE_MEMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((FeatureProperty<AbstractFeature>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GeneralizationRelation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.GENERALIZATION_RELATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractCityObject>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GridProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.GRID_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<RectifiedGridCoverage>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(IntBridgeInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INT_BRIDGE_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<IntBridgeInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(IntBuildingInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INT_BUILDING_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<IntBuildingInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InteriorBridgeRoomProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INTERIOR_BRIDGE_ROOM_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BridgeRoom>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InteriorFurnitureProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INTERIOR_BUILDING_FURNITURE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<BuildingFurniture>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InteriorHollowSpaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INTERIOR_HOLLOW_SPACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<HollowSpace>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InteriorRoomProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INTERIOR_ROOM_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<Room>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(IntTunnelInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.INT_TUNNEL_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<IntTunnelInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OpeningProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.BUILDING_OPENING_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractOpening>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ReliefComponentProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.RELIEF_COMPONENT_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractReliefComponent>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SurfaceDataProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.SURFACE_DATA_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<AbstractSurfaceData>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TrafficAreaProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TRAFFIC_AREA_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<TrafficArea>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TunnelInstallationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TUNNEL_INSTALLATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<TunnelInstallation>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TunnelPartProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TUNNEL_PART_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((FeatureProperty<TunnelPart>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends AbstractGeometry> Node createNode(GeometryProperty<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AssociationByRepOrRef<T>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeCurveProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_CURVE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<CompositeCurve>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeSolidProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_SOLID_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<CompositeSolid>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CompositeSurfaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COMPOSITE_SURFACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<CompositeSurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CurveProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CURVE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractCurve>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends AbstractGeometry> Node createNode(DomainSet<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DOMAIN_SET);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<T>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RectifiedGridDomain content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RECTIFIED_GRID_DOMAIN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((DomainSet<RectifiedGrid>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GeometricComplexProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRIC_COMPLEX_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractGeometry>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GeometricPrimitiveProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRIC_PRIMITIVE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractGeometricPrimitive>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LineStringProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINE_STRING_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<LineString>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LocationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LOCATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLocationKeyWord(), targetNode, GMLRelTypes.LOCATION_KEYWORD);

		createNode(content.getLocationString(), targetNode, GMLRelTypes.LOCATION_STRING);

		createNode(content.getNull(), targetNode, GMLRelTypes._NULL);

		createNode((GeometryProperty<AbstractGeometry>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(PriorityLocationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.PRIORITY_LOCATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetPriority())
			targetNode.setProperty("priority", content.getPriority() + "");

		createNode((LocationProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiCurveProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_CURVE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiCurve>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiGeometryProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_GEOMETRY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractGeometricAggregate>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiLineStringProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_LINE_STRING_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiLineString>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiPointProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_POINT_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiPoint>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiPolygonProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_POLYGON_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiPolygon>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiSolidProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_SOLID_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiSolid>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MultiSurfaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MULTI_SURFACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<MultiSurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(PointProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POINT_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<Point>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(PointRep content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POINT_REP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((PointProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(PolygonProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POLYGON_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<Polygon>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SolidProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SOLID_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractSolid>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SurfaceProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SURFACE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GeometryProperty<AbstractSurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TinProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TIN_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((GeometryProperty<TriangulatedSurface>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ImplicitRepresentationProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.IMPLICIT_REPRESENTATION_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AssociationByRepOrRef<ImplicitGeometry>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(MetaDataProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.META_DATA_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetAbout())
			targetNode.setProperty("about", content.getAbout() + "");

		createNode((AssociationByRepOrRef<MetaData>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RangeParameters content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RANGE_PARAMETERS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AssociationByRepOrRef<ValueObject>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TextureAssociation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass._TEXTURED_SURFACE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetUri())
			targetNode.setProperty("uri", content.getUri());

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AssociationByRepOrRef<AbstractTextureParameterization>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ValueProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AssociationByRepOrRef<Value>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public <T extends AbstractGeometry> Node createNode(InlineGeometryProperty<T> content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INLINE_GEOMETRY_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AssociationByRep<T>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AbstractRingProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ABSTRACT_RING_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((InlineGeometryProperty<AbstractRing>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Exterior content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.EXTERIOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractRingProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(InnerBoundaryIs content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INNER_BOUNDARY_IS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractRingProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Interior content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INTERIOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractRingProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(OuterBoundaryIs content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.OUTER_BOUNDARY_IS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((AbstractRingProperty) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LinearRingProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LINEAR_RING_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((InlineGeometryProperty<LinearRing>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(XalAddressProperty content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.XAL_ADDRESS_PROPERTY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((AssociationByRep<AddressDetails>) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Barcode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.BARCODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(BooleanOrNull content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.BOOLEAN_OR_NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetBoolean())
			targetNode.setProperty("_boolean", content.getBoolean() + "");

		if (content.isSetNull())
			targetNode.setProperty("_null", content.getNull() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(BooleanOrNullList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.BOOLEAN_OR_NULL_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getBooleanOrNull(), targetNode, GMLRelTypes.BOOLEAN_OR_NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(BoundingShape content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.BOUNDING_SHAPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getEnvelope(), targetNode, GMLRelTypes.ENVELOPE);

		createNode(content.getNull(), targetNode, GMLRelTypes._NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(BuildingName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.BUILDING_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetTypeOccurrence())
			targetNode.setProperty("typeOccurrence", content.getTypeOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Code content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		if (content.isSetCodeSpace())
			targetNode.setProperty("codeSpace", content.getCodeSpace() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CodeOrNullList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CODE_OR_NULL_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getNameOrNull(), targetNode, GMLRelTypes.NAME_OR_NULL);

		if (content.isSetCodeSpace())
			targetNode.setProperty("codeSpace", content.getCodeSpace() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CategoryExtent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CATEGORY_EXTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CodeOrNullList) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(Color content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.COLOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("red", content.getRed() + "");

		targetNode.setProperty("green", content.getGreen() + "");

		targetNode.setProperty("blue", content.getBlue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ColorPlusOpacity content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.COLOR_PLUS_OPACITY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getColor(), targetNode, GMLRelTypes.COLOR);

		targetNode.setProperty("opacity", content.getOpacity() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ControlPoint content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CONTROL_POINT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPosList(), targetNode, GMLRelTypes.POS_LIST);

		createNode(content.getGeometricPositionGroup(), targetNode, GMLRelTypes.GEOMETRIC_POSITION_GROUP);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Coord content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COORD);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("x", content.getX() + "");

		targetNode.setProperty("y", content.getY() + "");

		targetNode.setProperty("z", content.getZ() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Coordinates content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COORDINATES);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		targetNode.setProperty("decimal", content.getDecimal() + "");

		targetNode.setProperty("cs", content.getCs() + "");

		targetNode.setProperty("ts", content.getTs() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Country content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.COUNTRY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getCountryNameCode(), targetNode, GMLRelTypes.COUNTRY_NAME_CODE);

		createNode(content.getCountryName(), targetNode, GMLRelTypes.COUNTRY_NAME);

		createNode(content.getAdministrativeArea(), targetNode, GMLRelTypes.ADMINISTRATIVE_AREA);

		createNode(content.getLocality(), targetNode, GMLRelTypes.LOCALITY);

		createNode(content.getThoroughfare(), targetNode, GMLRelTypes.THOROUGHFARE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CountryName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.COUNTRY_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CountryNameCode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.COUNTRY_NAME_CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetScheme())
			targetNode.setProperty("scheme", content.getScheme() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CoverageFunction content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COVERAGE_FUNCTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getMappingRule(), targetNode, GMLRelTypes.MAPPING_RULE);

		createNodeSearchHierarchy(content.getGridFunction(), targetNode, GMLRelTypes.GRID_FUNCTION);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DataBlock content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DATA_BLOCK);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getRangeParameters(), targetNode, GMLRelTypes.RANGE_PARAMETERS);

		createNode(content.getTupleList(), targetNode, GMLRelTypes.TUPLE_LIST);

		createNodeSearchHierarchy(content.getDoubleOrNullTupleList(), targetNode, GMLRelTypes.DOUBLE_OR_NULL_TUPLE_LIST);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Department content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPARTMENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getDepartmentName(), targetNode, GMLRelTypes.DEPARTMENT_NAME);

		createNode(content.getMailStop(), targetNode, GMLRelTypes.MAIL_STOP);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DepartmentName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPARTMENT_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DependentLocality content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPENDENT_LOCALITY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getDependentLocalityName(), targetNode, GMLRelTypes.DEPENDENT_LOCALITY_NAME);

		createNode(content.getDependentLocalityNumber(), targetNode, GMLRelTypes.DEPENDENT_LOCALITY_NUMBER);

		createNode(content.getPostBox(), targetNode, GMLRelTypes.POST_BOX);

		createNode(content.getLargeMailUser(), targetNode, GMLRelTypes.LARGE_MAIL_USER);

		createNode(content.getPostOffice(), targetNode, GMLRelTypes.POST_OFFICE);

		createNode(content.getPostalRoute(), targetNode, GMLRelTypes.POSTAL_ROUTE);

		createNode(content.getThoroughfare(), targetNode, GMLRelTypes.THOROUGHFARE);

		createNode(content.getPremise(), targetNode, GMLRelTypes.PREMISE);

		createNode(content.getDependentLocality(), targetNode, GMLRelTypes.DEPENDENT_LOCALITY);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetUsageType())
			targetNode.setProperty("usageType", content.getUsageType() + "");

		if (content.isSetConnector())
			targetNode.setProperty("connector", content.getConnector() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DependentLocalityName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPENDENT_LOCALITY_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DependentLocalityNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPENDENT_LOCALITY_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNameNumberOccurrence())
			targetNode.setProperty("nameNumberOccurrence", content.getNameNumberOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DependentThoroughfare content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.DEPENDENT_THOROUGHFARE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getThoroughfareName(), targetNode, GMLRelTypes.THOROUGHFARE_NAME);

		createNode(content.getThoroughfarePreDirection(), targetNode, GMLRelTypes.THOROUGHFARE_PRE_DIRECTION);

		createNode(content.getThoroughfarePostDirection(), targetNode, GMLRelTypes.THOROUGHFARE_POST_DIRECTION);

		createNode(content.getThoroughfareLeadingType(), targetNode, GMLRelTypes.THOROUGHFARE_LEADING_TYPE);

		createNode(content.getThoroughfareTrailingType(), targetNode, GMLRelTypes.THOROUGHFARE_TRAILING_TYPE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DirectPosition content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DIRECT_POSITION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValue(), targetNode, GMLRelTypes.VALUE);

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DirectPositionList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DIRECT_POSITION_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValue(), targetNode, GMLRelTypes.VALUE);

		if (content.isSetCount())
			targetNode.setProperty("count", content.getCount() + "");

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DoubleOrNull content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DOUBLE_OR_NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetDouble())
			targetNode.setProperty("_double", content.getDouble() + "");

		createNode(content.getNull(), targetNode, GMLRelTypes._NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(DoubleOrNullList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.DOUBLE_OR_NULL_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getDoubleOrNull(), targetNode, GMLRelTypes.DOUBLE_OR_NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(MeasureOrNullList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MEASURE_OR_NULL_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetUom())
			targetNode.setProperty("uom", content.getUom());

		createNode((DoubleOrNullList) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(QuantityExtent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.QUANTITY_EXTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((MeasureOrNullList) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(EndorsementLineCode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.ENDORSEMENT_LINE_CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Envelope content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.ENVELOPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLowerCorner(), targetNode, GMLRelTypes.LOWER_CORNER);

		createNode(content.getUpperCorner(), targetNode, GMLRelTypes.UPPER_CORNER);

		createNode(content.getCoord(), targetNode, GMLRelTypes.COORD);

		createNode(content.getPos(), targetNode, GMLRelTypes.POS);

		createNode(content.getCoordinates(), targetNode, GMLRelTypes.COORDINATES);

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ExternalObject content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.EXTERNAL_OBJECT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetName())
			targetNode.setProperty("name", content.getName() + "");

		if (content.isSetUri())
			targetNode.setProperty("uri", content.getUri() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ExternalReference content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.EXTERNAL_REFERENCE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetInformationSystem())
			targetNode.setProperty("informationSystem", content.getInformationSystem() + "");

		createNode(content.getExternalObject(), targetNode, GMLRelTypes.EXTERNAL_OBJECT);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(File content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.FILE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getRangeParameters(), targetNode, GMLRelTypes.RANGE_PARAMETERS);

		if (content.isSetFileName())
			targetNode.setProperty("fileName", content.getFileName() + "");

		createNode(content.getFileStructure(), targetNode, GMLRelTypes.FILE_STRUCTURE);

		if (content.isSetMimeType())
			targetNode.setProperty("mimeType", content.getMimeType() + "");

		if (content.isSetCompression())
			targetNode.setProperty("compression", content.getCompression() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Firm content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.FIRM);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getFirmName(), targetNode, GMLRelTypes.FIRM_NAME);

		createNode(content.getDepartment(), targetNode, GMLRelTypes.DEPARTMENT);

		createNode(content.getMailStop(), targetNode, GMLRelTypes.MAIL_STOP);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(FirmName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.FIRM_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(GeometricPositionGroup content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GEOMETRIC_POSITION_GROUP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPos(), targetNode, GMLRelTypes.POS);

		createNodeSearchHierarchy(content.getPointProperty(), targetNode, GMLRelTypes.POINT_PROPERTY);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(GridEnvelope content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GRID_ENVELOPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLow(), targetNode, GMLRelTypes.LOW);

		createNode(content.getHigh(), targetNode, GMLRelTypes.HIGH);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(GridFunction content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GRID_FUNCTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getSequenceRule(), targetNode, GMLRelTypes.SEQUENCE_RULE);

		createNode(content.getStartPoint(), targetNode, GMLRelTypes.START_POINT);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(IndexMap content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INDEX_MAP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getLookUpTable(), targetNode, GMLRelTypes.LOOKUP_TABLE);

		createNode((GridFunction) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GridLimits content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.GRID_LIMITS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getGridEnvelope(), targetNode, GMLRelTypes.GRID_ENVELOPE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(IntegerOrNull content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INTEGER_OR_NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetInteger())
			targetNode.setProperty("_integer", content.getInteger() + "");

		createNode(content.getNull(), targetNode, GMLRelTypes._NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(IntegerOrNullList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INTEGER_OR_NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getIntegerOrNull(), targetNode, GMLRelTypes.INTEGER_OR_NULL);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CountExtent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.COUNT_EXTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((IntegerOrNullList) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(KeyLineCode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.KEY_LINE_CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(LargeMailUser content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.LARGE_MAIL_USER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getLargeMailUserName(), targetNode, GMLRelTypes.LARGE_MAIL_USER_NAME);

		createNode(content.getBuildingName(), targetNode, GMLRelTypes.BUILDING_NAME);

		createNode(content.getLargeMailUserIdentifier(), targetNode, GMLRelTypes.LARGE_MAIL_USER_IDENTIFIER);

		createNode(content.getDepartment(), targetNode, GMLRelTypes.DEPARTMENT);

		createNode(content.getPostBox(), targetNode, GMLRelTypes.POST_BOX);

		createNode(content.getThoroughfare(), targetNode, GMLRelTypes.THOROUGHFARE);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(LargeMailUserIdentifier content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.LARGE_MAIL_USER_IDENTIFIER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(LargeMailUserName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.LARGE_MAIL_USER_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Locality content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.LOCALITY);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getLocalityName(), targetNode, GMLRelTypes.LOCALITY_NAME);

		createNode(content.getPostBox(), targetNode, GMLRelTypes.POST_BOX);

		createNode(content.getLargeMailUser(), targetNode, GMLRelTypes.LARGE_MAIL_USER);

		createNode(content.getPostOffice(), targetNode, GMLRelTypes.POST_OFFICE);

		createNode(content.getPostalRoute(), targetNode, GMLRelTypes.POSTAL_ROUTE);

		createNode(content.getThoroughfare(), targetNode, GMLRelTypes.THOROUGHFARE);

		createNode(content.getPremise(), targetNode, GMLRelTypes.PREMISE);

		createNode(content.getDependentLocality(), targetNode, GMLRelTypes.DEPENDENT_LOCALITY);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetUsageType())
			targetNode.setProperty("usageType", content.getUsageType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(LocalityName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.LOCALITY_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(MailStop content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.MAIL_STOP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getMailStopName(), targetNode, GMLRelTypes.MAIL_STOP_NAME);

		createNode(content.getMailStopNumber(), targetNode, GMLRelTypes.MAIL_STOP_NUMBER);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(MailStopName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.MAIL_STOP_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(MailStopNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.MAIL_STOP_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNameNumberSeparator())
			targetNode.setProperty("nameNumberSeparator", content.getNameNumberSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Measure content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.MEASURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		if (content.isSetUom())
			targetNode.setProperty("uom", content.getUom() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Length content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.LENGTH);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((Measure) content, targetNode, null);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Speed content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SPEED);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((Measure) content, targetNode, null);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(NameOrNull content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.NAME_OR_NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetName())
			targetNode.setProperty("name", content.getName() + "");

		if (content.isSetNull())
			targetNode.setProperty("_null", content.getNull() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Null content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.NULL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PosOrPointPropertyOrPointRep content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POS_OR_POINT_PROPERTY_OR_POINT_REP);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPos(), targetNode, GMLRelTypes.POS);

		createNodeSearchHierarchy(content.getPointProperty(), targetNode, GMLRelTypes.POINT_PROPERTY);

		createNode(content.getPointRep(), targetNode, GMLRelTypes.POINT_REP);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PosOrPointPropertyOrPointRepOrCoord content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.POS_OR_POINT_PROPERTY_OR_POINT_REP_OR_COORD);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPos(), targetNode, GMLRelTypes.POS);

		createNodeSearchHierarchy(content.getPointProperty(), targetNode, GMLRelTypes.POINT_PROPERTY);

		createNode(content.getPointRep(), targetNode, GMLRelTypes.POINT_REP);

		createNode(content.getCoord(), targetNode, GMLRelTypes.COORD);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalCode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPostalCodeNumber(), targetNode, GMLRelTypes.POSTAL_CODE_NUMBER);

		createNode(content.getPostalCodeNumberExtension(), targetNode, GMLRelTypes.POSTAL_CODE_NUMBER_EXTENSION);

		createNode(content.getPostTown(), targetNode, GMLRelTypes.POST_TOWN);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalCodeNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_CODE_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalCodeNumberExtension content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_CODE_NUMBER_EXTENSION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberExtensionSeparator())
			targetNode.setProperty("numberExtensionSeparator", content.getNumberExtensionSeparator() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalRoute content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_ROUTE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPostalRouteName(), targetNode, GMLRelTypes.POSTAL_ROUTE_NAME);

		createNode(content.getPostalRouteNumber(), targetNode, GMLRelTypes.POSTAL_ROUTE_NUMBER);

		createNode(content.getPostBox(), targetNode, GMLRelTypes.POST_BOX);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalRouteName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_ROUTE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalRouteNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_ROUTE_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostalServiceElements content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_SERVICE_ELEMENTS);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressIdentifier(), targetNode, GMLRelTypes.ADDRESS_INDETIFIER);

		createNode(content.getEndorsementLineCode(), targetNode, GMLRelTypes.ENDORSEMENT_LINE_CODE);

		createNode(content.getKeyLineCode(), targetNode, GMLRelTypes.KEY_LINE_CODE);

		createNode(content.getBarcode(), targetNode, GMLRelTypes.BARCODE);

		createNode(content.getSortingCode(), targetNode, GMLRelTypes.SORTING_CODE);

		createNode(content.getAddressLatitude(), targetNode, GMLRelTypes.ADDRESS_LATITUDE);

		createNode(content.getAddressLatitudeDirection(), targetNode, GMLRelTypes.ADDRESS_LATITUDE_DIRECTION);

		createNode(content.getAddressLongitude(), targetNode, GMLRelTypes.ADDRESS_LONGITUDE);

		createNode(content.getAddressLongitudeDirection(), targetNode, GMLRelTypes.ADDRESS_LONGITUDE_DIRECTION);

		createNode(content.getSupplementaryPostalServiceData(), targetNode, GMLRelTypes.SUPPLEMENTARY_POSTAL_SERVICE_DATA);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostBox content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_BOX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPostBoxNumber(), targetNode, GMLRelTypes.POST_BOX_NUMBER);

		createNode(content.getPostBoxNumberPrefix(), targetNode, GMLRelTypes.POST_BOX_NUMBER_PREFIX);

		createNode(content.getPostBoxNumberSuffix(), targetNode, GMLRelTypes.POST_BOX_NUMBER_SUFFIX);

		createNode(content.getPostBoxNumberExtension(), targetNode, GMLRelTypes.POST_BOX_NUMBER_EXTENSION);

		createNode(content.getFirm(), targetNode, GMLRelTypes.FIRM);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostBoxNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_BOX_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostBoxNumberExtension content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_BOX_NUMBER_EXTENSION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberExtensionSeparator())
			targetNode.setProperty("numberExtensionSeparator", content.getNumberExtensionSeparator() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostBoxNumberPrefix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_BOX_NUMBER_PREFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberPrefixSeparator())
			targetNode.setProperty("numberPrefixSeparator", content.getNumberPrefixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostBoxNumberSuffix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_BOX_NUMBER_SUFFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberSuffixSeparator())
			targetNode.setProperty("numberSuffixSeparator", content.getNumberSuffixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostOffice content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_OFFICE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPostOfficeName(), targetNode, GMLRelTypes.POST_OFFICE_NAME);

		createNode(content.getPostOfficeNumber(), targetNode, GMLRelTypes.POST_OFFICE_NUMBER);

		createNode(content.getPostalRoute(), targetNode, GMLRelTypes.POSTAL_ROUTE);

		createNode(content.getPostBox(), targetNode, GMLRelTypes.POST_BOX);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostOfficeName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_OFFICE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostOfficeNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POSTAL_ROUTE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetIndicatorOccurrence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurrence() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostTown content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_TOWN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPostTownName(), targetNode, GMLRelTypes.POST_TOWN_NAME);

		createNode(content.getPostTownSuffix(), targetNode, GMLRelTypes.POST_TOWN_SUFFIX);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostTownName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_TOWN_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PostTownSuffix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.POST_TOWN_SUFFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Premise content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPremiseName(), targetNode, GMLRelTypes.PREMISE_NAME);

		createNode(content.getPremiseNumber(), targetNode, GMLRelTypes.PREMISE_NUMBER);

		createNode(content.getPremiseNumberPrefix(), targetNode, GMLRelTypes.PREMISE_NUMBER_PREFIX);

		createNode(content.getPremiseNumberSuffix(), targetNode, GMLRelTypes.PREMISE_NUMBER_SUFFIX);

		createNode(content.getBuildingName(), targetNode, GMLRelTypes.BUILDING_NAME);

		createNode(content.getSubPremise(), targetNode, GMLRelTypes.SUB_PREMISE);

		createNode(content.getPremiseLocation(), targetNode, GMLRelTypes.PREMISE_LOCATION);

		createNode(content.getPremiseNumberRange(), targetNode, GMLRelTypes.PREMISE_NUMBER_RANGE);

		createNode(content.getFirm(), targetNode, GMLRelTypes.FIRM);

		createNode(content.getMailStop(), targetNode, GMLRelTypes.MAIL_STOP);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		createNode(content.getPremise(), targetNode, GMLRelTypes.PREMISE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetPremiseDependency())
			targetNode.setProperty("premiseDependency", content.getPremiseDependency() + "");

		if (content.isSetPremiseDependencyType())
			targetNode.setProperty("premiseDependencyType", content.getPremiseDependencyType() + "");

		if (content.isSetPremiseThoroughfareConnector())
			targetNode.setProperty("premiseThoroughfareConnector", content.getPremiseThoroughfareConnector() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseLocation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_LOCATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetTypeOccurrence())
			targetNode.setProperty("typeOccurrence", content.getTypeOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberType())
			targetNode.setProperty("numberType", content.getNumberType() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetIndicatorOccurrence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurrence() + "");

		if (content.isSetNumberTypeOccurrence())
			targetNode.setProperty("numberTypeOccurrence", content.getNumberTypeOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumberPrefix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER_PREFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberPrefixSeparator())
			targetNode.setProperty("numberPrefixSeparator", content.getNumberPrefixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumberRange content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER_RANGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getPremiseNumberRangeFrom(), targetNode, GMLRelTypes.PREMISE_NUMBER_RANGE_FROM);

		createNode(content.getPremiseNumberRangeTo(), targetNode, GMLRelTypes.PREMISE_NUMBER_RANGE_TO);

		if (content.isSetRangeType())
			targetNode.setProperty("rangeType", content.getRangeType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetSeparator())
			targetNode.setProperty("separator", content.getSeparator() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetIndicatorOccurence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurence() + "");

		if (content.isSetNumberRangeOccurence())
			targetNode.setProperty("numberRangeOccurrence", content.getNumberRangeOccurence() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumberRangeFrom content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER_RANGE_FROM);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPremiseNumberPrefix(), targetNode, GMLRelTypes.PREMISE_NUMBER_PREFIX);

		createNode(content.getPremiseNumber(), targetNode, GMLRelTypes.PREMISE_NUMBER);

		createNode(content.getPremiseNumberSuffix(), targetNode, GMLRelTypes.PREMISE_NUMBER_SUFFIX);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumberRangeTo content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER_RANGE_TO);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getPremiseNumberPrefix(), targetNode, GMLRelTypes.PREMISE_NUMBER_PREFIX);

		createNode(content.getPremiseNumber(), targetNode, GMLRelTypes.PREMISE_NUMBER);

		createNode(content.getPremiseNumberSuffix(), targetNode, GMLRelTypes.PREMISE_NUMBER_SUFFIX);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(PremiseNumberSuffix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.PREMISE_NUMBER_SUFFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberSuffixSeparator())
			targetNode.setProperty("numberSuffixSeparator", content.getNumberSuffixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(RangeSet content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.RANGE_SET);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValueArray(), targetNode, GMLRelTypes.VALUE_ARRAY);

		createNode(content.getScalarValueList(), targetNode, GMLRelTypes.SCALAR_VALUE_LIST);

		createNode(content.getDataBlock(), targetNode, GMLRelTypes.DATA_BLOCK);

		createNode(content.getFile(), targetNode, GMLRelTypes.FILE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ScalarValue content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SCALAR_VALUE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetBoolean())
			targetNode.setProperty("_boolean", content.getBoolean() + "");

		createNode(content.getCategory(), targetNode, GMLRelTypes.CATEGORY);

		createNodeSearchHierarchy(content.getQuantity(), targetNode, GMLRelTypes.QUANTITY);

		if (content.isSetCount())
			targetNode.setProperty("count", content.getCount() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ScalarValueList content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SCALAR_VALUE_LIST);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getBooleanList(), targetNode, GMLRelTypes.BOOLEAN_LIST);

		createNodeSearchHierarchy(content.getCategoryList(), targetNode, GMLRelTypes.CATEGORY_LIST);

		createNodeSearchHierarchy(content.getQuantityList(), targetNode, GMLRelTypes.QUANTITY_LIST);

		createNodeSearchHierarchy(content.getCountList(), targetNode, GMLRelTypes.COUNT_LIST);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SequenceRule content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SEQUENCE_RULE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValue(), targetNode, GMLRelTypes.VALUE);

		createNode(content.getOrder(), targetNode, GMLRelTypes.ORDER);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SortingCode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SORTING_CODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(StringOrRef content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.STRING_OR_REF);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetValue())
			targetNode.setProperty("value", content.getValue());

		createNode(content.getActuate(), targetNode, GMLRelTypes.ACTUATE);

		if (content.isSetArcrole())
			targetNode.setProperty("arcrole", content.getArcrole());

		if (content.isSetHref()) {
			String href = content.getHref();

			targetNode.setProperty("href", href);

			// remember this node for post processing (to link to referenced object)
			if (SETTINGS.ENABLE_INDICES) {
				hrefApiIndex.add(targetNode, "href", href);
				// hrefValues.add(href);
			} else {
				ArrayList<Node> nodes = hrefIndex.get(href);
				if (nodes == null || nodes.isEmpty()) {
					nodes = new ArrayList<Node>();
				}
				nodes.add(targetNode);
				hrefIndex.put(href, nodes);
			}
		}

		if (content.isSetRemoteSchema())
			targetNode.setProperty("remoteSchema", content.getRemoteSchema());

		if (content.isSetRole())
			targetNode.setProperty("role", content.getRole());

		createNode(content.getShow(), targetNode, GMLRelTypes.SHOW);

		if (content.isSetTitle())
			targetNode.setProperty("title", content.getTitle());

		// createNode(content.getType(), targetNode, GMLRelTypes.TYPE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubAdministrativeArea content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_ADMINISTRATIVE_AREA);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getSubAdministrativeAreaName(), targetNode, GMLRelTypes.SUB_ADMINISTRATIVE_AREA_NAME);

		createNode(content.getLocality(), targetNode, GMLRelTypes.LOCALITY);

		createNode(content.getPostOffice(), targetNode, GMLRelTypes.POST_OFFICE);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetUsageType())
			targetNode.setProperty("usageType", content.getUsageType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubAdministrativeAreaName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_ADMINISTRATIVE_AREA_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremise content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getSubPremiseName(), targetNode, GMLRelTypes.SUB_PREMISE_NAME);

		createNode(content.getSubPremiseNumber(), targetNode, GMLRelTypes.SUB_PREMISE_NUMBER);

		createNode(content.getSubPremiseNumberPrefix(), targetNode, GMLRelTypes.SUB_PREMISE_NUMBER_PREFIX);

		createNode(content.getSubPremiseNumberSuffix(), targetNode, GMLRelTypes.SUB_PREMISE_NUMBER_SUFFIX);

		createNode(content.getBuildingName(), targetNode, GMLRelTypes.BUILDING_NAME);

		createNode(content.getSubPremiseLocation(), targetNode, GMLRelTypes.SUB_PREMISE_LOCATION);

		createNode(content.getFirm(), targetNode, GMLRelTypes.FIRM);

		createNode(content.getMailStop(), targetNode, GMLRelTypes.MAIL_STOP);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		createNode(content.getSubPremise(), targetNode, GMLRelTypes.SUB_PREMISE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremiseLocation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE_LOCATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremiseName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetTypeOccurrence())
			targetNode.setProperty("typeOccurrence", content.getTypeOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremiseNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetIndicatorOccurrence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurrence() + "");

		if (content.isSetPremiseNumberSeparator())
			targetNode.setProperty("premiseNumberSeparator", content.getPremiseNumberSeparator() + "");

		if (content.isSetNumberTypeOccurrence())
			targetNode.setProperty("numberTypeOccurrence", content.getNumberTypeOccurrence() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremiseNumberPrefix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE_NUMBER_PREFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberPrefixSeparator())
			targetNode.setProperty("numberPrefixSeparator", content.getNumberPrefixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SubPremiseNumberSuffix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUB_PREMISE_NUMBER_SUFFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberSuffixSeparator())
			targetNode.setProperty("numberSufixSeparator", content.getNumberSuffixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(SupplementaryPostalServiceData content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.SUPPLEMENTARY_POSTAL_SERVICE_DATA);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(TextureCoordinates content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TEXTURE_COORDINATES);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValue(), targetNode, GMLRelTypes.VALUE);

		if (content.isSetRing())
			targetNode.setProperty("ring", content.getRing() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Thoroughfare content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getThoroughfareNumberOrThoroughfareNumberRange(), targetNode, GMLRelTypes.NUMBER_OR_RANGE);

		createNode(content.getThoroughfareNumberPrefix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_PREFIX);

		createNode(content.getThoroughfareNumberSuffix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_SUFFIX);

		createNode(content.getThoroughfareName(), targetNode, GMLRelTypes.THOROUGHFARE_NAME);

		createNode(content.getThoroughfarePreDirection(), targetNode, GMLRelTypes.THOROUGHFARE_PRE_DIRECTION);

		createNode(content.getThoroughfarePostDirection(), targetNode, GMLRelTypes.THOROUGHFARE_POST_DIRECTION);

		createNode(content.getThoroughfareLeadingType(), targetNode, GMLRelTypes.THOROUGHFARE_LEADING_TYPE);

		createNode(content.getThoroughfareTrailingType(), targetNode, GMLRelTypes.THOROUGHFARE_TRAILING_TYPE);

		createNode(content.getDependentThoroughfare(), targetNode, GMLRelTypes.DEPENDENT_THOROUGHFARE);

		createNode(content.getDependentLocality(), targetNode, GMLRelTypes.DEPENDENT_LOCALITY);

		createNode(content.getPremise(), targetNode, GMLRelTypes.PREMISE);

		createNode(content.getFirm(), targetNode, GMLRelTypes.FIRM);

		createNode(content.getPostalCode(), targetNode, GMLRelTypes.POSTAL_CODE);

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetDependentThoroughfares())
			targetNode.setProperty("dependentThoroughfares", content.getDependentThoroughfares() + "");

		if (content.isSetDependentThoroughfaresIndicator())
			targetNode.setProperty("dependentThoroughfaresIndicator", content.getDependentThoroughfaresIndicator() + "");

		if (content.isSetDependentThoroughfaresConnector())
			targetNode.setProperty("dependentThoroughfaresConnector", content.getDependentThoroughfaresConnector() + "");

		if (content.isSetDependentThoroughfaresType())
			targetNode.setProperty("dependentThoroughfaresType", content.getDependentThoroughfaresType() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareLeadingType content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_LEADING_TYPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareName content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NAME);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumber content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetNumberType())
			targetNode.setProperty("numberType", content.getNumberType() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetIndicatorOccurrence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurrence() + "");

		if (content.isSetNumberOccurrence())
			targetNode.setProperty("numberOccurrence", content.getNumberOccurrence() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberFrom content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_FROM);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getContent(), targetNode, GMLRelTypes.CONTENT);

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberFromContent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_FROM_CONTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getThoroughfareNumber(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER);

		createNode(content.getThoroughfareNumberPrefix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_PREFIX);

		createNode(content.getThoroughfareNumberSuffix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_SUFFIX);

		if (content.isSetString())
			targetNode.setProperty("string", content.getString() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberOrRange content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_OR_RANGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getThoroughfareNumber(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER);

		createNode(content.getThoroughfareNumberRange(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_RANGE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberPrefix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_PREFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberPrefixSeparator())
			targetNode.setProperty("numberPrefixSeparator", content.getNumberPrefixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberRange content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_RANGE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getThoroughfareNumberFrom(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_FROM);

		createNode(content.getThoroughfareNumberTo(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_TO);

		if (content.isSetRangeType())
			targetNode.setProperty("rangeType", content.getRangeType() + "");

		if (content.isSetIndicator())
			targetNode.setProperty("indicator", content.getIndicator() + "");

		if (content.isSetSeparator())
			targetNode.setProperty("separator", content.getSeparator() + "");

		if (content.isSetIndicatorOccurrence())
			targetNode.setProperty("indicatorOccurrence", content.getIndicatorOccurrence() + "");

		if (content.isSetNumberRangeOccurrence())
			targetNode.setProperty("numberRangeOccurrence", content.getNumberRangeOccurrence() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberSuffix content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_SUFFIX);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetNumberSuffixSeparator())
			targetNode.setProperty("numberSuffixSeparator", content.getNumberSuffixSeparator() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberTo content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_TO);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getContent(), targetNode, GMLRelTypes.CONTENT);

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareNumberToContent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_NUMBER_TO_CONTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getAddressLine(), targetNode, GMLRelTypes.ADDRESS_LINE);

		createNode(content.getThoroughfareNumber(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER);

		createNode(content.getThoroughfareNumberPrefix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_PREFIX);

		createNode(content.getThoroughfareNumberSuffix(), targetNode, GMLRelTypes.THOROUGHFARE_NUMBER_SUFFIX);

		if (content.isSetString())
			targetNode.setProperty("string", content.getString() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfarePostDirection content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_POST_DIRECTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfarePreDirection content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_PRE_DIRECTION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ThoroughfareTrailingType content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(XALClass.THOROUGHFARE_TRAILING_TYPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetContent())
			targetNode.setProperty("content", content.getContent() + "");

		if (content.isSetType())
			targetNode.setProperty("type", content.getType() + "");

		if (content.isSetCode())
			targetNode.setProperty("code", content.getCode() + "");

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(TransformationMatrix2x2 content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TRANSFORMATION_MATRIX_2X2);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getMatrix(), targetNode, GMLRelTypes.MATRIX);

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(TransformationMatrix3x4 content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TRANSFORMATION_MATRIX_3X4);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getMatrix(), targetNode, GMLRelTypes.MATRIX);

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(WorldToTexture content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.WORLD_TO_TEXTURE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		createNode((TransformationMatrix3x4) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TransformationMatrix4x4 content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TRANSFORMATION_MATRIX_4X4);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getMatrix(), targetNode, GMLRelTypes.MATRIX);

		// createNodeSearchHierarchy(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(ValueExtent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VALUE_EXTENT);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getCategoryExtent(), targetNode, GMLRelTypes.CATEGORY_EXTENT);

		createNode(content.getQuantityExtent(), targetNode, GMLRelTypes.QUANTITY_EXTENT);

		createNode(content.getCountExtent(), targetNode, GMLRelTypes.COUNT_EXTENT);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(Vector content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.VECTOR);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode(content.getValue(), targetNode, GMLRelTypes.VALUE);

		if (content.isSetSrsDimension())
			targetNode.setProperty("srsDimension", content.getSrsDimension() + "");

		if (content.isSetSrsName())
			targetNode.setProperty("srsName", content.getSrsName() + "");

		createNode(content.getAxisLabels(), targetNode, GMLRelTypes.AXIS_LABELS);

		createNode(content.getUomLabels(), targetNode, GMLRelTypes.UOM_LABELS);

		// TODO parent

		return targetNode;
	}

	@Override
	public Node createNode(CityGML content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITY_GML");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ModelObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(AppearanceModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("APPEARANCE_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TextureType content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.TEXTURE_TYPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		return targetNode;
	}

	@Override
	public Node createNode(WrapMode content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.WRAP_MODE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		return targetNode;
	}

	@Override
	public Node createNode(BuildingModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("BUILDING_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityFurnitureModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITY_FURNITURE_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CityObjectGroupModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CITY_OBJECT_GROUP_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CoreModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("CORE_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(RelativeToTerrain content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.RELATIVE_TO_TERRAIN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		return targetNode;
	}

	@Override
	public Node createNode(RelativeToWater content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass.RELATIVE_TO_WATER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		return targetNode;
	}

	@Override
	public Node createNode(GenericsModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("GENERICS_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(LandUseModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("LANDUSE_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(ReliefModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("RELIEF_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(TexturedSurfaceModuleComponent content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("TEXTURED_SURFACE_MODULE_COMPONENT");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((CityGML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(_TextureType content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(CityGMLClass._TEXTURE_TYPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		// createNode(content.getCityGMLModule(), targetNode, GMLRelTypes.MODULE);

		return targetNode;
	}

	@Override
	public Node createNode(GML content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("GML");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ModelObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(CurveInterpolation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.CURVE_INTERPOLATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(FileValueModel content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.FILE_VALUE_MODEL);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(IncrementOrder content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.INCREMENT_ORDER);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(SequenceRuleNames content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SEQUENCE_RULE_NAMES);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(Sign content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SIGN);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(SurfaceInterpolation content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.SURFACE_INTERPOLATION);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(XLinkActuate content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.XLINK_ACTUATE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(XLinkShow content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.XLINK_SHOW);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(XLinkType content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel(GMLClass.XLINK_TYPE);
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		targetNode.setProperty("value", content.getValue() + "");

		return targetNode;
	}

	@Override
	public Node createNode(AssociationAttributeGroup content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("ASSOCIATION_ATTRIBUTE_GROUP");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SRSInformationGroup content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("SRS_INFORMATION_GROUP");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(SRSReferenceGroup content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("SRS_REFERENCE_GROUP");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(StandardObjectProperties content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("STANDARD_OBJECT_PROPERTIES");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((GML) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(XAL content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("XAL");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((ModelObject) content, targetNode, null);

		return targetNode;
	}

	@Override
	public Node createNode(GrPostal content, Node parent, RelationshipType relType) {
		if (content == null)
			return null;

		Node targetNode = null;
		if (relType == null)
			targetNode = parent;
		else {
			targetNode = createNodeWithLabel("GrPostal");
			if (!relType.equals(GMLRelTypes.HREF_FEATURE)) {
				parent.createRelationshipTo(targetNode, relType);
			}
		}

		createNode((XAL) content, targetNode, null);

		return targetNode;
	}

}
