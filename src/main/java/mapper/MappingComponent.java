package mapper;

import java.util.HashMap;
import java.util.List;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.citygml.CityGML;
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
import org.citygml4j.model.citygml.bridge.BridgeConstructionElementProperty;
import org.citygml4j.model.citygml.bridge.BridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.BridgePartProperty;
import org.citygml4j.model.citygml.bridge.IntBridgeInstallationProperty;
import org.citygml4j.model.citygml.bridge.InteriorBridgeRoomProperty;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.BuildingFurniture;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.building.BuildingInstallationProperty;
import org.citygml4j.model.citygml.building.BuildingModuleComponent;
import org.citygml4j.model.citygml.building.BuildingPart;
import org.citygml4j.model.citygml.building.BuildingPartProperty;
import org.citygml4j.model.citygml.building.IntBuildingInstallation;
import org.citygml4j.model.citygml.building.IntBuildingInstallationProperty;
import org.citygml4j.model.citygml.building.InteriorRoomProperty;
import org.citygml4j.model.citygml.building.Room;
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
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.citygml.tunnel.HollowSpace;
import org.citygml4j.model.citygml.tunnel.IntTunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.InteriorHollowSpaceProperty;
import org.citygml4j.model.citygml.tunnel.TunnelInstallationProperty;
import org.citygml4j.model.citygml.tunnel.TunnelPartProperty;
import org.citygml4j.model.citygml.waterbody.BoundedByWaterSurfaceProperty;
import org.citygml4j.model.common.association.Associable;
import org.citygml4j.model.common.base.ModelObject;
import org.citygml4j.model.common.child.Child;
import org.citygml4j.model.gml.GML;
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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.w3c.dom.Element;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public interface MappingComponent {
	/*
	 * Auxiliary functions
	 */
	public Node createNodeSearchHierarchy(Module content, Node parent, RelationshipType relType);

	public Node createNodeSearchHierarchy(ModelObject content, Node parent, RelationshipType relType);

	public <T> Node createNode(List<T> content, Node parent, RelationshipType relType);

	public <T> Node createNode(T[] content, Node parent, RelationshipType relType);

	public Node createNode(HashMap<String, Object> content, Node parent, RelationshipType relType);

	public Node createNode(Element content, Node parent, RelationshipType relType);

	/*
	 * Module functions
	 */
	// CityGMLModule
	public Node createNode(CityGMLModule content, Node parent, RelationshipType relType);

	// AbstractModule
	public Node createNode(AbstractModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule
	public Node createNode(AbstractCityGMLModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > AppearanceModule
	public Node createNode(AppearanceModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > BridgeModule
	public Node createNode(BridgeModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > BuildingModule
	public Node createNode(BuildingModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > CityFurnitureModule
	public Node createNode(CityFurnitureModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > CityObjectGroupModule
	public Node createNode(CityObjectGroupModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > CoreModule
	public Node createNode(CoreModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > GenericsModule
	public Node createNode(GenericsModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > LandUseModule
	public Node createNode(LandUseModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > ReliefModule
	public Node createNode(ReliefModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > TexturedSurfaceModule
	public Node createNode(TexturedSurfaceModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > TransportationModule
	public Node createNode(TransportationModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > TunnelModule
	public Node createNode(TunnelModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > VegetationModule
	public Node createNode(VegetationModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractCityGMLModule > WaterBodyModule
	public Node createNode(WaterBodyModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractGMLModule
	public Node createNode(AbstractGMLModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractGMLModule > GMLCoreModule
	public Node createNode(GMLCoreModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractGMLModule > XLinkModule
	public Node createNode(XLinkModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractXALModule
	public Node createNode(AbstractXALModule content, Node parent, RelationshipType relType);

	// AbstractModule > AbstractXALModule > XALCoreModule
	public Node createNode(XALCoreModule content, Node parent, RelationshipType relType);

	/*
	 * ModelObject functions
	 */
	// ModelObject
	public Node createNode(ModelObject content, Node parent, RelationshipType relType);

	// ModelObject > Associable
	public Node createNode(Associable content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractCurveSegment
	public Node createNode(AbstractCurveSegment content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractCurveSegment > LineStringSegment
	public Node createNode(LineStringSegment content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML
	public Node createNode(AbstractGML content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > _AbstractAppearance
	public Node createNode(_AbstractAppearance content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > _AbstractAppearance > _Material
	public Node createNode(_Material content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > _AbstractAppearance > _SimpleTexture
	public Node createNode(_SimpleTexture content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature
	public Node createNode(AbstractFeature content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject
	public Node createNode(AbstractCityObject content, Node parent, RelationshipType relType);

	// TODO AbstractBoundarySurface of Tunnel and Bridge

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface
	public Node createNode(org.citygml4j.model.citygml.building.AbstractBoundarySurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > CeilingSurface
	public Node createNode(org.citygml4j.model.citygml.building.CeilingSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > ClosureSurface
	public Node createNode(org.citygml4j.model.citygml.building.ClosureSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > FloorSurface
	public Node createNode(org.citygml4j.model.citygml.building.FloorSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > GroundSurface
	public Node createNode(org.citygml4j.model.citygml.building.GroundSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > InteriorWallSurface
	public Node createNode(org.citygml4j.model.citygml.building.InteriorWallSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > OuterCeilingSurface
	public Node createNode(org.citygml4j.model.citygml.building.OuterCeilingSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > OuterFloorSurface
	public Node createNode(org.citygml4j.model.citygml.building.OuterFloorSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > RoofSurface
	public Node createNode(org.citygml4j.model.citygml.building.RoofSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractBoundarySurface > WallSurface
	public Node createNode(org.citygml4j.model.citygml.building.WallSurface content, Node parent, RelationshipType relType);

	// TODO AbstractOpening of Tunnel and Bridge

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractOpening
	public Node createNode(org.citygml4j.model.citygml.building.AbstractOpening content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractOpening > Door
	public Node createNode(org.citygml4j.model.citygml.building.Door content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractOpening > Window
	public Node createNode(org.citygml4j.model.citygml.building.Window content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractReliefComponent
	public Node createNode(AbstractReliefComponent content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractReliefComponent > BreaklineRelief
	public Node createNode(BreaklineRelief content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractReliefComponent > MassPointRelief
	public Node createNode(MassPointRelief content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractReliefComponent > RasterRelief
	public Node createNode(RasterRelief content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractReliefComponent > TINRelief
	public Node createNode(TINRelief content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite
	public Node createNode(AbstractSite content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBridge
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBridge > Bridge
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBridge > BridgePart
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBuilding
	public Node createNode(AbstractBuilding content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBuilding > Building
	public Node createNode(Building content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractBuilding > BuildingPart
	public Node createNode(BuildingPart content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractTunnel
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractTunnel > Tunnel
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractSite > AbstractTunnel > TunnelPart
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > AuxiliaryTrafficArea
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TrafficArea
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TransportationComplex
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TransportationComplex > Railway
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TransportationComplex > Road
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TransportationComplex > Square
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractTransportationObject > TransportationComplex > Track
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractVegetationObject
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractVegetationObject > PlantCover
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractVegetationObject > SolitaryVegetationObject
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterBoundarySurface
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterBoundarySurface > WaterClosureSurface
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterBoundarySurface > WaterGroundSurface
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterBoundarySurface > WaterSurface
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterObject
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > AbstractWaterObject > WaterBody
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BridgeConstructionElement
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BridgeFurniture
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BridgeInstallation
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BridgeRoom
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BuildingFurniture
	public Node createNode(BuildingFurniture content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > BuildingInstallation
	public Node createNode(BuildingInstallation content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > CityFurniture
	public Node createNode(CityFurniture content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > CityObjectGroup
	public Node createNode(CityObjectGroup content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > GenericCityObject
	public Node createNode(GenericCityObject content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > HollowSpace
	public Node createNode(HollowSpace content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > IntBridgeInstallation
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > IntBuildingInstallation
	public Node createNode(IntBuildingInstallation content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > IntTunnelInstallation
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > LandUse
	public Node createNode(LandUse content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > ReliefFeature
	public Node createNode(ReliefFeature content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > Room
	public Node createNode(Room content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > TunnelFurniture
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCityObject > TunnelInstallation
	// TODO

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCoverage
	public Node createNode(AbstractCoverage content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCoverage > AbstractDiscreteCoverage
	public Node createNode(AbstractDiscreteCoverage content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractCoverage > AbstractDiscreteCoverage > RectifiedGridCoverage
	public Node createNode(RectifiedGridCoverage content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractFeatureCollection
	public Node createNode(AbstractFeatureCollection content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractFeatureCollection > CityModel
	public Node createNode(CityModel content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractSurfaceData
	public Node createNode(AbstractSurfaceData content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractSurfaceData > AbstractTexture
	public Node createNode(AbstractTexture content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractSurfaceData > AbstractTexture > GeoreferencedTexture
	public Node createNode(GeoreferencedTexture content, Node parent, RelationshipType relType);

	public Node createNode(Matrix content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractSurfaceData > AbstractTexture > ParameterizedTexture
	public Node createNode(ParameterizedTexture content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > AbstractSurfaceData > X3DMaterial
	public Node createNode(X3DMaterial content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > Address
	public Node createNode(org.citygml4j.model.citygml.core.Address content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractFeature > Appearance
	public Node createNode(Appearance content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry
	public Node createNode(AbstractGeometry content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate
	public Node createNode(AbstractGeometricAggregate content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiCurve
	public Node createNode(MultiCurve content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiGeometry
	public Node createNode(MultiGeometry content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiLineString
	public Node createNode(MultiLineString content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiPoint
	public Node createNode(MultiPoint content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiPolygon
	public Node createNode(MultiPolygon content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiSolid
	public Node createNode(MultiSolid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricAggregate > MultiSurface
	public Node createNode(MultiSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive
	public Node createNode(AbstractGeometricPrimitive content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractCurve
	public Node createNode(AbstractCurve content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractCurve > CompositeCurve
	public Node createNode(CompositeCurve content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractCurve > Curve
	public Node createNode(org.citygml4j.model.gml.geometry.primitives.Curve content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractCurve > LineString
	public Node createNode(LineString content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractCurve > OrientableCurve
	public Node createNode(OrientableCurve content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSolid
	public Node createNode(AbstractSolid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSolid > CompositeSolid
	public Node createNode(CompositeSolid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSolid > Solid
	public Node createNode(Solid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface
	public Node createNode(AbstractSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > CompositeSurface
	public Node createNode(CompositeSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > OrientableSurface
	public Node createNode(OrientableSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > OrientableSurface > _TexturedSurface
	public Node createNode(_TexturedSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > Polygon
	public Node createNode(Polygon content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > Surface
	public Node createNode(Surface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > Surface > TriangulatedSurface
	public Node createNode(TriangulatedSurface content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > AbstractSurface > Surface > TriangulatedSurface > Tin
	public Node createNode(Tin content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractGeometricPrimitive > Point
	public Node createNode(Point content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractRing
	public Node createNode(AbstractRing content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractRing > LinearRing
	public Node createNode(LinearRing content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > AbstractRing > Ring
	public Node createNode(Ring content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > GeometricComplex
	public Node createNode(GeometricComplex content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > Grid
	public Node createNode(Grid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractGeometry > Grid > RectifiedGrid
	public Node createNode(RectifiedGrid content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractTextureParameterization
	public Node createNode(AbstractTextureParameterization content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractTextureParameterization > TexCoordGen
	public Node createNode(TexCoordGen content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > AbstractTextureParameterization > TexCoordList
	public Node createNode(TexCoordList content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > CompositeValue
	public Node createNode(CompositeValue content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > CompositeValue > ValueArray
	public Node createNode(ValueArray content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractGML > ImplicitGeometry
	public Node createNode(ImplicitGeometry content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractSurfacePatch
	public Node createNode(AbstractSurfacePatch content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractSurfacePatch > Rectangle
	public Node createNode(Rectangle content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AbstractSurfacePatch > Triangle
	public Node createNode(Triangle content, Node parent, RelationshipType relType);

	// ModelObject > Associable > AddressDetails
	public Node createNode(AddressDetails content, Node parent, RelationshipType relType);

	// ModelObject > Associable > GenericValueObject
	public Node createNode(GenericValueObject content, Node parent, RelationshipType relType);

	// ModelObject > Associable > MetaData
	public Node createNode(MetaData content, Node parent, RelationshipType relType);

	// ModelObject > Associable > Value
	public Node createNode(Value content, Node parent, RelationshipType relType);

	// ModelObject > Associable > ValueObject
	public Node createNode(ValueObject content, Node parent, RelationshipType relType);

	// ModelObject > Child
	public Node createNode(Child content, Node parent, RelationshipType relType);

	// ModelObject > Child > _Color
	public Node createNode(_Color content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute
	public Node createNode(AbstractGenericAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > DateAttribute
	public Node createNode(DateAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > DoubleAttribute
	public Node createNode(DoubleAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > GenericAttributeSet
	public Node createNode(GenericAttributeSet content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > IntAttribute
	public Node createNode(IntAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > MeasureAttribute
	public Node createNode(MeasureAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > StringAttribute
	public Node createNode(StringAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > AbstractGenericAttribute > UriAttribute
	public Node createNode(UriAttribute content, Node parent, RelationshipType relType);

	// ModelObject > Child > Address
	public Node createNode(org.citygml4j.model.xal.Address content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressIdentifier
	public Node createNode(AddressIdentifier content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLatitude
	public Node createNode(AddressLatitude content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLatitudeDirection
	public Node createNode(AddressLatitudeDirection content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLine
	public Node createNode(AddressLine content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLines
	public Node createNode(AddressLines content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLongitude
	public Node createNode(AddressLongitude content, Node parent, RelationshipType relType);

	// ModelObject > Child > AddressLongitudeDirection
	public Node createNode(AddressLongitudeDirection content, Node parent, RelationshipType relType);

	// ModelObject > Child > ADEComponent
	public Node createNode(ADEComponent content, Node parent, RelationshipType relType);

	// ModelObject > Child > AdministrativeArea
	public Node createNode(AdministrativeArea content, Node parent, RelationshipType relType);

	// ModelObject > Child > AdministrativeAreaName
	public Node createNode(AdministrativeAreaName content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child>
	public <T extends Associable & Child> Node createNode(ArrayAssociation<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > CurveSegmentArrayProperty
	public Node createNode(CurveSegmentArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > FeatureArrayProperty
	public Node createNode(FeatureArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > GeometryArrayProperty<T extends AbstractGeometry>
	public <T extends AbstractGeometry> Node createNode(GeometryArrayProperty<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > GeometryArrayProperty<T extends AbstractGeometry> > CurveArrayProperty
	public Node createNode(CurveArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > GeometryArrayProperty<T extends AbstractGeometry> > PointArrayProperty
	public Node createNode(PointArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > GeometryArrayProperty<T extends AbstractGeometry> > SolidArrayProperty
	public Node createNode(SolidArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > GeometryArrayProperty<T extends AbstractGeometry> > SurfaceArrayProperty
	public Node createNode(SurfaceArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > LineStringSegmentArrayProperty
	public Node createNode(LineStringSegmentArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > SurfacePatchArrayProperty
	public Node createNode(SurfacePatchArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > SurfacePatchArrayProperty > TrianglePatchArrayProperty
	public Node createNode(TrianglePatchArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > ArrayAssociation<T extends Associable & Child> > ValueArrayProperty
	public Node createNode(ValueArrayProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child>
	public <T extends Associable & Child> Node createNode(AssociationByRep<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child>
	public <T extends Associable & Child> Node createNode(AssociationByRepOrRef<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> _AppearanceProperty
	public Node createNode(_AppearanceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature>
	public <T extends AbstractFeature> Node createNode(FeatureProperty<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > AddressProperty
	public Node createNode(AddressProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > AppearanceProperty
	public Node createNode(AppearanceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > AppearanceProperty > AppearanceMember
	public Node createNode(AppearanceMember content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > AuxiliaryTrafficAreaProperty
	public Node createNode(AuxiliaryTrafficAreaProperty content, Node parent, RelationshipType relType);

	// TODO BoundarySurfaceProperty for Tunnel and Bridge

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BoundarySurfaceProperty
	public Node createNode(org.citygml4j.model.citygml.building.BoundarySurfaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BoundedByWaterSurfaceProperty
	public Node createNode(BoundedByWaterSurfaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BridgeConstructionElementProperty
	public Node createNode(BridgeConstructionElementProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BridgeInstallationProperty
	public Node createNode(BridgeInstallationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BridgePartProperty
	public Node createNode(BridgePartProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BuildingInstallationProperty
	public Node createNode(BuildingInstallationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > BuildingPartProperty
	public Node createNode(BuildingPartProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > CityObjectGroupMember
	public Node createNode(CityObjectGroupMember content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > CityObjectGroupParent
	public Node createNode(CityObjectGroupParent content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > CityObjectMember
	public Node createNode(CityObjectMember content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > FeatureMember
	public Node createNode(FeatureMember content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > GeneralizationRelation
	public Node createNode(GeneralizationRelation content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > GridProperty
	public Node createNode(GridProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > IntBridgeInstallationProperty
	public Node createNode(IntBridgeInstallationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > IntBuildingInstallationProperty
	public Node createNode(IntBuildingInstallationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > InteriorBridgeRoomProperty
	public Node createNode(InteriorBridgeRoomProperty content, Node parent, RelationshipType relType);

	// TODO InteriorBridgeRoomProperty for Tunnel and Bridge

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > InteriorFurnitureProperty
	public Node createNode(org.citygml4j.model.citygml.building.InteriorFurnitureProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > InteriorHollowSpaceProperty
	public Node createNode(InteriorHollowSpaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > InteriorRoomProperty
	public Node createNode(InteriorRoomProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > IntTunnelInstallationProperty
	public Node createNode(IntTunnelInstallationProperty content, Node parent, RelationshipType relType);

	// TODO OpeningProperty for Tunnel and Bridge

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > OpeningProperty
	public Node createNode(org.citygml4j.model.citygml.building.OpeningProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > ReliefComponentProperty
	public Node createNode(ReliefComponentProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > SurfaceDataProperty
	public Node createNode(SurfaceDataProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > TrafficAreaProperty
	public Node createNode(TrafficAreaProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > TunnelInstallationProperty
	public Node createNode(TunnelInstallationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> FeatureProperty<T extends AbstractFeature> > TunnelPartProperty
	public Node createNode(TunnelPartProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry>
	public <T extends AbstractGeometry> Node createNode(GeometryProperty<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > CompositeCurveProperty
	public Node createNode(CompositeCurveProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > CompositeSolidProperty
	public Node createNode(CompositeSolidProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > CompositeSurfaceProperty
	public Node createNode(CompositeSurfaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > CurveProperty
	public Node createNode(CurveProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > DomainSet<T extends AbstractGeometry>
	public <T extends AbstractGeometry> Node createNode(DomainSet<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > DomainSet<T extends AbstractGeometry> > RectifiedGridDomain
	public Node createNode(RectifiedGridDomain content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > GeometricComplexProperty
	public Node createNode(GeometricComplexProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > GeometricPrimitiveProperty
	public Node createNode(GeometricPrimitiveProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > LineStringProperty
	public Node createNode(LineStringProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > LocationProperty
	public Node createNode(LocationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > LocationProperty > PriorityLocationProperty
	public Node createNode(PriorityLocationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiCurveProperty
	public Node createNode(MultiCurveProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiGeometryProperty
	public Node createNode(MultiGeometryProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiLineStringProperty
	public Node createNode(MultiLineStringProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiPointProperty
	public Node createNode(MultiPointProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiPolygonProperty
	public Node createNode(MultiPolygonProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiSolidProperty
	public Node createNode(MultiSolidProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > MultiSurfaceProperty
	public Node createNode(MultiSurfaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > PointProperty
	public Node createNode(PointProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > PointProperty > PointRep
	public Node createNode(PointRep content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > PolygonProperty
	public Node createNode(PolygonProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > SolidProperty
	public Node createNode(SolidProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > SurfaceProperty
	public Node createNode(SurfaceProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> GeometryProperty<T extends AbstractGeometry> > TinProperty
	public Node createNode(TinProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> ImplicitRepresentationProperty
	public Node createNode(ImplicitRepresentationProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> MetaDataProperty
	public Node createNode(MetaDataProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> RangeParameters
	public Node createNode(RangeParameters content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> TextureAssociation
	public Node createNode(TextureAssociation content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > AssociationByRepOrRef<T extends Associable & Child> ValueProperty
	public Node createNode(ValueProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry>
	public <T extends AbstractGeometry> Node createNode(InlineGeometryProperty<T> content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > AbstractRingProperty
	public Node createNode(AbstractRingProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > AbstractRingProperty > Exterior
	public Node createNode(Exterior content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > AbstractRingProperty > InnerBoundaryIs
	public Node createNode(InnerBoundaryIs content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > AbstractRingProperty > Interior
	public Node createNode(Interior content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > AbstractRingProperty > OuterBoundaryIs
	public Node createNode(OuterBoundaryIs content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > InlineGeometryProperty<T extends AbstractGeometry> > LinearRingProperty
	public Node createNode(LinearRingProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > AssociationByRep<T extends Associable & Child> > XalAddressProperty
	public Node createNode(XalAddressProperty content, Node parent, RelationshipType relType);

	// ModelObject > Child > Barcode
	public Node createNode(Barcode content, Node parent, RelationshipType relType);

	// ModelObject > Child > BooleanOrNull
	public Node createNode(BooleanOrNull content, Node parent, RelationshipType relType);

	// ModelObject > Child > BooleanOrNullList
	public Node createNode(BooleanOrNullList content, Node parent, RelationshipType relType);

	// ModelObject > Child > BoundingShape
	public Node createNode(BoundingShape content, Node parent, RelationshipType relType);

	// ModelObject > Child > BuildingName
	public Node createNode(BuildingName content, Node parent, RelationshipType relType);

	// ModelObject > Child > Code
	public Node createNode(Code content, Node parent, RelationshipType relType);

	// ModelObject > Child > CodeOrNullList
	public Node createNode(CodeOrNullList content, Node parent, RelationshipType relType);

	// ModelObject > Child > CodeOrNullList > CategoryExtent
	public Node createNode(CategoryExtent content, Node parent, RelationshipType relType);

	// ModelObject > Child > Color
	public Node createNode(Color content, Node parent, RelationshipType relType);

	// ModelObject > Child > ColorPlusOpacity
	public Node createNode(ColorPlusOpacity content, Node parent, RelationshipType relType);

	// ModelObject > Child > ControlPoint
	public Node createNode(ControlPoint content, Node parent, RelationshipType relType);

	// ModelObject > Child > Coord
	public Node createNode(Coord content, Node parent, RelationshipType relType);

	// ModelObject > Child > Coordinates
	public Node createNode(Coordinates content, Node parent, RelationshipType relType);

	// ModelObject > Child > Country
	public Node createNode(Country content, Node parent, RelationshipType relType);

	// ModelObject > Child > CountryName
	public Node createNode(CountryName content, Node parent, RelationshipType relType);

	// ModelObject > Child > CountryNameCode
	public Node createNode(CountryNameCode content, Node parent, RelationshipType relType);

	// ModelObject > Child > CoverageFunction
	public Node createNode(CoverageFunction content, Node parent, RelationshipType relType);

	// ModelObject > Child > DataBlock
	public Node createNode(DataBlock content, Node parent, RelationshipType relType);

	// ModelObject > Child > Department
	public Node createNode(Department content, Node parent, RelationshipType relType);

	// ModelObject > Child > DepartmentName
	public Node createNode(DepartmentName content, Node parent, RelationshipType relType);

	// ModelObject > Child > DependentLocality
	public Node createNode(DependentLocality content, Node parent, RelationshipType relType);

	// ModelObject > Child > DependentLocalityName
	public Node createNode(DependentLocalityName content, Node parent, RelationshipType relType);

	// ModelObject > Child > DependentLocalityNumber
	public Node createNode(DependentLocalityNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > DependentThoroughfare
	public Node createNode(DependentThoroughfare content, Node parent, RelationshipType relType);

	// ModelObject > Child > DirectPosition
	public Node createNode(DirectPosition content, Node parent, RelationshipType relType);

	// ModelObject > Child > DirectPositionList
	public Node createNode(DirectPositionList content, Node parent, RelationshipType relType);

	// ModelObject > Child > DoubleOrNull
	public Node createNode(DoubleOrNull content, Node parent, RelationshipType relType);

	// ModelObject > Child > DoubleOrNullList
	public Node createNode(DoubleOrNullList content, Node parent, RelationshipType relType);

	// ModelObject > Child > DoubleOrNullList > MeasureOrNullList
	public Node createNode(MeasureOrNullList content, Node parent, RelationshipType relType);

	// ModelObject > Child > DoubleOrNullList > MeasureOrNullList > QuantityExtent
	public Node createNode(QuantityExtent content, Node parent, RelationshipType relType);

	// ModelObject > Child > EndorsementLineCode
	public Node createNode(EndorsementLineCode content, Node parent, RelationshipType relType);

	// ModelObject > Child > Envelope
	public Node createNode(Envelope content, Node parent, RelationshipType relType);

	// ModelObject > Child > ExternalObject
	public Node createNode(ExternalObject content, Node parent, RelationshipType relType);

	// ModelObject > Child > ExternalReference
	public Node createNode(ExternalReference content, Node parent, RelationshipType relType);

	// ModelObject > Child > File
	public Node createNode(File content, Node parent, RelationshipType relType);

	// ModelObject > Child > Firm
	public Node createNode(Firm content, Node parent, RelationshipType relType);

	// ModelObject > Child > FirmName
	public Node createNode(FirmName content, Node parent, RelationshipType relType);

	// ModelObject > Child > GeometricPositionGroup
	public Node createNode(GeometricPositionGroup content, Node parent, RelationshipType relType);

	// ModelObject > Child > GridEnvelope
	public Node createNode(GridEnvelope content, Node parent, RelationshipType relType);

	// ModelObject > Child > GridFunction
	public Node createNode(GridFunction content, Node parent, RelationshipType relType);

	// ModelObject > Child > GridFunction > IndexMap
	public Node createNode(IndexMap content, Node parent, RelationshipType relType);

	// ModelObject > Child > GridLimits
	public Node createNode(GridLimits content, Node parent, RelationshipType relType);

	// ModelObject > Child > IntegerOrNull
	public Node createNode(IntegerOrNull content, Node parent, RelationshipType relType);

	// ModelObject > Child > IntegerOrNullList
	public Node createNode(IntegerOrNullList content, Node parent, RelationshipType relType);

	// ModelObject > Child > IntegerOrNullList > CountExtent
	public Node createNode(CountExtent content, Node parent, RelationshipType relType);

	// ModelObject > Child > KeyLineCode
	public Node createNode(KeyLineCode content, Node parent, RelationshipType relType);

	// ModelObject > Child > LargeMailUser
	public Node createNode(LargeMailUser content, Node parent, RelationshipType relType);

	// ModelObject > Child > LargeMailUserIdentifier
	public Node createNode(LargeMailUserIdentifier content, Node parent, RelationshipType relType);

	// ModelObject > Child > LargeMailUserName
	public Node createNode(LargeMailUserName content, Node parent, RelationshipType relType);

	// ModelObject > Child > Locality
	public Node createNode(Locality content, Node parent, RelationshipType relType);

	// ModelObject > Child > LocalityName
	public Node createNode(LocalityName content, Node parent, RelationshipType relType);

	// ModelObject > Child > MailStop
	public Node createNode(MailStop content, Node parent, RelationshipType relType);

	// ModelObject > Child > MailStopName
	public Node createNode(MailStopName content, Node parent, RelationshipType relType);

	// ModelObject > Child > MailStopNumber
	public Node createNode(MailStopNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > Measure
	public Node createNode(Measure content, Node parent, RelationshipType relType);

	// ModelObject > Child > Measure > Length
	public Node createNode(Length content, Node parent, RelationshipType relType);

	// ModelObject > Child > Measure > Speed
	public Node createNode(Speed content, Node parent, RelationshipType relType);

	// ModelObject > Child > NameOrNull
	public Node createNode(NameOrNull content, Node parent, RelationshipType relType);

	// ModelObject > Child > Null
	public Node createNode(Null content, Node parent, RelationshipType relType);

	// ModelObject > Child > PosOrPointPropertyOrPointRep
	public Node createNode(PosOrPointPropertyOrPointRep content, Node parent, RelationshipType relType);

	// ModelObject > Child > PosOrPointPropertyOrPointRepOrCoord
	public Node createNode(PosOrPointPropertyOrPointRepOrCoord content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalCode
	public Node createNode(PostalCode content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalCodeNumber
	public Node createNode(PostalCodeNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalCodeNumberExtension
	public Node createNode(PostalCodeNumberExtension content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalRoute
	public Node createNode(PostalRoute content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalRouteName
	public Node createNode(PostalRouteName content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalRouteNumber
	public Node createNode(PostalRouteNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostalServiceElements
	public Node createNode(PostalServiceElements content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostBox
	public Node createNode(PostBox content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostBoxNumber
	public Node createNode(PostBoxNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostBoxNumberExtension
	public Node createNode(PostBoxNumberExtension content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostBoxNumberPrefix
	public Node createNode(PostBoxNumberPrefix content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostBoxNumberSuffix
	public Node createNode(PostBoxNumberSuffix content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostOffice
	public Node createNode(PostOffice content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostOfficeName
	public Node createNode(PostOfficeName content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostOfficeNumber
	public Node createNode(PostOfficeNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostTown
	public Node createNode(PostTown content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostTownName
	public Node createNode(PostTownName content, Node parent, RelationshipType relType);

	// ModelObject > Child > PostTownSuffix
	public Node createNode(PostTownSuffix content, Node parent, RelationshipType relType);

	// ModelObject > Child > Premise
	public Node createNode(Premise content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseLocation
	public Node createNode(PremiseLocation content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseName
	public Node createNode(PremiseName content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumber
	public Node createNode(PremiseNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumberPrefix
	public Node createNode(PremiseNumberPrefix content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumberRange
	public Node createNode(PremiseNumberRange content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumberRangeFrom
	public Node createNode(PremiseNumberRangeFrom content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumberRangeTo
	public Node createNode(PremiseNumberRangeTo content, Node parent, RelationshipType relType);

	// ModelObject > Child > PremiseNumberSuffix
	public Node createNode(PremiseNumberSuffix content, Node parent, RelationshipType relType);

	// ModelObject > Child > RangeSet
	public Node createNode(RangeSet content, Node parent, RelationshipType relType);

	// ModelObject > Child > ScalarValue
	public Node createNode(ScalarValue content, Node parent, RelationshipType relType);

	// ModelObject > Child > ScalarValueList
	public Node createNode(ScalarValueList content, Node parent, RelationshipType relType);

	// ModelObject > Child > SequenceRule
	public Node createNode(SequenceRule content, Node parent, RelationshipType relType);

	// ModelObject > Child > SortingCode
	public Node createNode(SortingCode content, Node parent, RelationshipType relType);

	// ModelObject > Child > StringOrRef
	public Node createNode(StringOrRef content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubAdministrativeArea
	public Node createNode(SubAdministrativeArea content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubAdministrativeAreaName
	public Node createNode(SubAdministrativeAreaName content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremise
	public Node createNode(SubPremise content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremiseLocation
	public Node createNode(SubPremiseLocation content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremiseName
	public Node createNode(SubPremiseName content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremiseNumber
	public Node createNode(SubPremiseNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremiseNumberPrefix
	public Node createNode(SubPremiseNumberPrefix content, Node parent, RelationshipType relType);

	// ModelObject > Child > SubPremiseNumberSuffix
	public Node createNode(SubPremiseNumberSuffix content, Node parent, RelationshipType relType);

	// ModelObject > Child > SupplementaryPostalServiceData
	public Node createNode(SupplementaryPostalServiceData content, Node parent, RelationshipType relType);

	// ModelObject > Child > TextureCoordinates
	public Node createNode(TextureCoordinates content, Node parent, RelationshipType relType);

	// ModelObject > Child > Thoroughfare
	public Node createNode(Thoroughfare content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareLeadingType
	public Node createNode(ThoroughfareLeadingType content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareName
	public Node createNode(ThoroughfareName content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumber
	public Node createNode(ThoroughfareNumber content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberFrom
	public Node createNode(ThoroughfareNumberFrom content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberFromContent
	public Node createNode(ThoroughfareNumberFromContent content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberOrRange
	public Node createNode(ThoroughfareNumberOrRange content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberPrefix
	public Node createNode(ThoroughfareNumberPrefix content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberRange
	public Node createNode(ThoroughfareNumberRange content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberSuffix
	public Node createNode(ThoroughfareNumberSuffix content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberTo
	public Node createNode(ThoroughfareNumberTo content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareNumberToContent
	public Node createNode(ThoroughfareNumberToContent content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfarePostDirection
	public Node createNode(ThoroughfarePostDirection content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfarePreDirection
	public Node createNode(ThoroughfarePreDirection content, Node parent, RelationshipType relType);

	// ModelObject > Child > ThoroughfareTrailingType
	public Node createNode(ThoroughfareTrailingType content, Node parent, RelationshipType relType);

	// ModelObject > Child > TransformationMatrix2x2
	public Node createNode(TransformationMatrix2x2 content, Node parent, RelationshipType relType);

	// ModelObject > Child > TransformationMatrix3x4
	public Node createNode(TransformationMatrix3x4 content, Node parent, RelationshipType relType);

	// ModelObject > Child > TransformationMatrix3x4 > WorldToTexture
	public Node createNode(WorldToTexture content, Node parent, RelationshipType relType);

	// ModelObject > Child > TransformationMatrix4x4
	public Node createNode(TransformationMatrix4x4 content, Node parent, RelationshipType relType);

	// ModelObject > Child > ValueExtent
	public Node createNode(ValueExtent content, Node parent, RelationshipType relType);

	// ModelObject > Child > Vector
	public Node createNode(Vector content, Node parent, RelationshipType relType);

	// ModelObject > CityGML
	public Node createNode(CityGML content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > AppearanceModuleComponent
	public Node createNode(AppearanceModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > AppearanceModuleComponent > TextureType
	public Node createNode(TextureType content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > AppearanceModuleComponent > WrapMode
	public Node createNode(WrapMode content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > BridgeModuleComponent
	// TODO

	// ModelObject > CityGML > BuildingModuleComponent
	public Node createNode(BuildingModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > CityFurnitureModuleComponent
	public Node createNode(CityFurnitureModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > CityObjectGroupModuleComponent
	public Node createNode(CityObjectGroupModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > CoreModuleComponent
	public Node createNode(CoreModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > CoreModuleComponent > RelativeToTerrain
	public Node createNode(RelativeToTerrain content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > CoreModuleComponent > RelativeToWater
	public Node createNode(RelativeToWater content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > GenericsModuleComponent
	public Node createNode(GenericsModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > LandUseModuleComponent
	public Node createNode(LandUseModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > ReliefModuleComponent
	public Node createNode(ReliefModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > TexturedSurfaceModuleComponent
	public Node createNode(TexturedSurfaceModuleComponent content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > TexturedSurfaceModuleComponent > _TextureType
	public Node createNode(_TextureType content, Node parent, RelationshipType relType);

	// ModelObject > CityGML > TransportationModuleComponent
	// TODO

	// ModelObject > CityGML > TunnelModuleComponent
	// TODO

	// ModelObject > CityGML > VegetationModuleComponent
	// TODO

	// ModelObject > CityGML > WaterBodyModuleComponent
	// TODO

	// ModelObject > GML
	public Node createNode(GML content, Node parent, RelationshipType relType);

	// ModelObject > GML > CurveInterpolation
	public Node createNode(CurveInterpolation content, Node parent, RelationshipType relType);

	// ModelObject > GML > FileValueModel
	public Node createNode(FileValueModel content, Node parent, RelationshipType relType);

	// ModelObject > GML > IncrementOrder
	public Node createNode(IncrementOrder content, Node parent, RelationshipType relType);

	// ModelObject > GML > SequenceRuleNames
	public Node createNode(SequenceRuleNames content, Node parent, RelationshipType relType);

	// ModelObject > GML > Sign
	public Node createNode(Sign content, Node parent, RelationshipType relType);

	// ModelObject > GML > SurfaceInterpolation
	public Node createNode(SurfaceInterpolation content, Node parent, RelationshipType relType);

	// ModelObject > GML > XLinkActuate
	public Node createNode(XLinkActuate content, Node parent, RelationshipType relType);

	// ModelObject > GML > XLinkShow
	public Node createNode(XLinkShow content, Node parent, RelationshipType relType);

	// ModelObject > GML > XLinkType
	public Node createNode(XLinkType content, Node parent, RelationshipType relType);

	// ModelObject > GML > AssociationAttributeGroup
	public Node createNode(AssociationAttributeGroup content, Node parent, RelationshipType relType);

	// ModelObject > GML > SRSInformationGroup
	public Node createNode(SRSInformationGroup content, Node parent, RelationshipType relType);

	// ModelObject > GML > SRSInformationGroup > SRSReferenceGroup
	public Node createNode(SRSReferenceGroup content, Node parent, RelationshipType relType);

	// ModelObject > GML > StandardObjectProperties
	public Node createNode(StandardObjectProperties content, Node parent, RelationshipType relType);

	// ModelObject > XAL
	public Node createNode(XAL content, Node parent, RelationshipType relType);

	// ModelObject > XAL > GrPostal
	public Node createNode(GrPostal content, Node parent, RelationshipType relType);

}
