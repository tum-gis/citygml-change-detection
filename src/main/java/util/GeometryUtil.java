package util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.citygml4j.geometry.Matrix;
import org.citygml4j.model.gml.basicTypes.Coordinates;
import org.citygml4j.model.gml.geometry.primitives.Coord;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.geometry.primitives.LineString;
import org.citygml4j.model.gml.geometry.primitives.Point;
import org.citygml4j.model.gml.geometry.primitives.PointProperty;
import org.citygml4j.model.gml.geometry.primitives.PosOrPointPropertyOrPointRepOrCoord;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class GeometryUtil {
	/*
	 * TRANSFORMATION FUNCTIONS
	 */
	// convert strings into a matrix, each column of which is a point
	public static double[][] convertFromStringTo3dPoints(String pointsString, int dimension, String delimiter, Logger logger, boolean suppressLogger) {
		String[] tmp = pointsString.split(delimiter);

		if (tmp.length % dimension != 0) {
			if (!suppressLogger) {
				logger.warning("WARNING: List of points " + pointsString + "  must have length factor of " + dimension + ". Ignoring this geometry ...");
			}
			return null;
		}

		double[][] result = new double[dimension][tmp.length / dimension];
		for (int i = 0; i < tmp.length; i++) {
			result[i % dimension][i / dimension] = Double.parseDouble(tmp[i]);
		}

		// print("CONVERT STRING", result);
		return result;
	}

	// translate all points to the same left most (0,0,0)
	public static double[][] translate3dPointsToOxy(double[][] points, double[] offset) {
		double[][] result = new double[points.length][points[0].length];
		for (int i = 0; i < points.length; i++) {
			for (int j = 0; j < points[i].length; j++) {
				result[i][j] = points[i][j] - offset[i];
			}
		}

		// print("TRANSLATE BY " + Arrays.toString(offset), result);
		return result;
	}

	// get point from a matrix with index
	public static double[] getPoint(double[][] points, int index) {
		double[] result = new double[points.length];
		for (int i = 0; i < points.length; i++) {
			result[i] = points[i][index];
		}

		return result;
	}

	// calculate cross product of 2 vectors
	public static double[] cross(double[] v1, double[] v2) {
		return new double[] { v1[1] * v2[2] - v1[2] * v2[1],
				v2[0] * v1[2] - v2[2] * v1[0],
				v1[0] * v2[1] - v1[1] * v2[0] };
	}

	// calculate dot product of 2 vectors
	public static double dot(double[] v1, double[] v2) {
		double result = 0;
		for (int i = 0; i < v1.length; i++) {
			result += v1[i] * v2[i];
		}

		return result;
	}

	// check if a given vector is zero
	public static boolean isZero(double[] v) {
		for (int i = 0; i < v.length; i++) {
			if (v[i] != 0)
				return false;
		}

		return true;
	}

	// normalize a given vector
	public static double[] normalize(double[] v) {
		double[] result = new double[v.length];

		double length = length(v);
		for (int i = 0; i < v.length; i++) {
			result[i] = v[i] / length;
		}

		// System.out.println("NORM OF " + Arrays.toString(v) + " = " + Arrays.toString(result));
		return result;
	}

	// calculate length of a given vector
	public static double length(double[] v) {
		return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
	}

	// calculate distance of two given vectors
	public static double distance(double[] v1, double[] v2) {
		return Math.sqrt((v1[0] - v2[0]) * (v1[0] - v2[0])
				+ (v1[1] - v2[1]) * (v1[1] - v2[1])
				+ (v1[2] - v2[2]) * (v1[2] - v2[2]));
	}

	// calculate the unit normal vector of current plane
	public static double[] findUnitNormalOf3dPlane(double[][] points, Logger logger, boolean suppressLogger) {
		if (points.length < 2) {
			if (!suppressLogger) {
				logger.warning("WARNING: List " + Arrays.toString(points) + " must have at least 3 points. Ignoring this geometry ...");
			}
			return null;
		}

		// Note: avoid collinear points!
		double[] v1 = { points[0][0] - points[0][1],
				points[1][0] - points[1][1],
				points[2][0] - points[2][1] };
		for (int i = 2; i < points[0].length; i++) {
			double[] v2 = { points[0][0] - points[0][i],
					points[1][0] - points[1][i],
					points[2][0] - points[2][i] };
			double[] normal = cross(v1, v2);
			if (!isZero(normal)) {
				// System.out.println("UNIT NORMAL = " + Arrays.toString(normalize(normal)));
				return normalize(normal);
			}
		}

		if (!suppressLogger) {
			String tmp = "[ ";
			for (int i = 0; i < points[0].length; i++) {
				for (int j = 0; j < points.length; j++) {
					tmp += points[j][i] + " ";
				}
			}
			tmp += "]";
			logger.warning("WARNING: All points " + tmp + " in list are collinear.");
		}

		return null;
	}

	// rotate 3D points on a plane to 2D points parallel to Oxy
	public static double[][] rotate3dPointsToOxy(double[][] points, Logger logger, boolean suppressLogger) {

		// unit normal of original plane with 3D points
		double[] unitNormalFrom = findUnitNormalOf3dPlane(points, logger, suppressLogger);
		if (unitNormalFrom == null || unitNormalFrom.length == 0) {
			return null;
		}

		// unit normal of desired plane with 2D points (here Oxy)
		double[] unitNormalTo = { 0, 0, 1 };

		// cosine of rotation angle between two normals
		double c = dot(unitNormalFrom, unitNormalTo) / (length(unitNormalFrom) * length(unitNormalTo));

		// rotation axis which is a unit cross product of two normals
		double[] cross = cross(unitNormalFrom, unitNormalTo);
		if (isZero(cross)) {
			return points;
		}
		double[] rotationAxis = normalize(cross);

		double s = Math.sqrt(1 - c * c);
		double C = 1 - c;
		double x = rotationAxis[0];
		double y = rotationAxis[1];
		double z = rotationAxis[2];

		double[][] rotation = {
				{ x * x * C + c, x * y * C - z * s, x * z * C + y * s },
				{ y * x * C + z * s, y * y * C + c, y * z * C - x * s },
				{ z * x * C - y * s, z * y * C + x * s, z * z * C + c } };

		// print("ROTATION MATRIX", rotation);

		return (new Matrix(rotation)).times(new Matrix(points)).getArray();
	}

	// transform 3D points on a plane to 2D points parallel to Oxy
	public static double[][] transform3dPointsTo2d(String pointsString, int dimension, String delimiter, Logger logger, boolean suppressLogger) {
		double[][] points = convertFromStringTo3dPoints(pointsString, dimension, delimiter, logger, suppressLogger);

		if (points == null || points.length == 0) {
			return null;
		}

		if (dimension == 2) {
			return points;
		}

		// find the nearest point to Oxy

		// String output = "... transforming coplanar 3D points to 2D ...";
		// output += toString("\n" + String.format("%-22s", "") + "-----------------------------------------------BEFORE (3D)-----------------------------------------------", points);

		double min = Double.MAX_VALUE;
		double[] offset = null;
		for (int i = 0; i < points.length; i++) {
			double[] tmpV = getPoint(points, i);
			double tmpLength = length(tmpV);
			if (min > tmpLength) {
				min = tmpLength;
				offset = tmpV;
			}
		}

		// translate and rotate all points to Oxy
		// !!! DO NOT TRANSLATE BEFORE ROTATING (WHICH LEADS TO POINTS' WRONG POSITIONS) !!!
		// double[][] tmp = rotate3dPointsToOxy(translate3dPointsToOxy(points, offset));
		double[][] tmp = rotate3dPointsToOxy(points, logger, suppressLogger);
		if (tmp == null || tmp.length == 0) {
			return null;
		}

		// omit the z coordinate
		double[][] result = new double[tmp.length - 1][tmp[0].length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				result[i][j] = tmp[i][j];
			}
		}

		// output += toString("\n" + String.format("%-22s", "") + "-----------------------------------------------AFTER (2D)------------------------------------------------", result);
		// logger.info(output);

		return result;
	}

	public static String toString(String title, double[][] points) {
		String tmp = title;
		for (int i = 0; i < points.length; i++) {
			tmp += "\n" + String.format("%-22s", "");
			for (int j = 0; j < points[i].length; j++) {
				tmp += String.format("%20.10f", points[i][j]);
			}
		}

		return tmp;
	}

	/*
	 * SHAPE FUNCTIONS
	 */
	/**
	 * 
	 * @param pointsString
	 * @param dimension
	 * @param delimiter
	 *            default value is ";"
	 * @return
	 */
	public static Area createArea(String pointsString, int dimension, String delimiter, Logger logger, boolean suppressLogger) {
		// transform 3D to 2D points
		double[][] points = transform3dPointsTo2d(pointsString, dimension, delimiter, logger, suppressLogger);

		if (points == null || points.length == 0) {
			return new Area();
		}

		// create a path shape restricted by given 2D points
		Path2D path = new Path2D.Double();
		path.moveTo(points[0][0], points[1][0]);
		for (int i = 1; i < points[0].length; i++) {
			path.lineTo(points[0][i], points[1][i]);
		}
		path.closePath(); // If the path is already closed then this method has no effect.

		Area result = new Area(path);
		return result;
	}

	public static String toString(Area area, String prefix) {
		String tmp = prefix;
		PathIterator iterator = area.getPathIterator(null);
		float[] floats = new float[6];
		while (!iterator.isDone()) {
			int type = iterator.currentSegment(floats);
			float x = floats[0];
			float y = floats[1];
			if (type != PathIterator.SEG_CLOSE) {
				tmp += "\n" + String.format("%-30s", "") + "point x = " + x + ", y = " + y;
			}
			iterator.next();
		}

		return tmp;
	}

	// calculate overlapping volume of 3D bounding shapes (boxes) with given lower and upper points
	public static double calcSharedVolOfBoxes(String[] lowerCornerA, String[] upperCornerA,
			String[] lowerCornerB, String[] upperCornerB) {
		double[] lA = { Double.parseDouble(lowerCornerA[0]), Double.parseDouble(lowerCornerA[1]), Double.parseDouble(lowerCornerA[2]) };
		double[] uA = { Double.parseDouble(upperCornerA[0]), Double.parseDouble(upperCornerA[1]), Double.parseDouble(upperCornerA[2]) };
		double[] lB = { Double.parseDouble(lowerCornerB[0]), Double.parseDouble(lowerCornerB[1]), Double.parseDouble(lowerCornerB[2]) };
		double[] uB = { Double.parseDouble(upperCornerB[0]), Double.parseDouble(upperCornerB[1]), Double.parseDouble(upperCornerB[2]) };

		return calcSharedVolOfBoxes(lA, uA, lB, uB);
	}

	public static double calcSharedVolOfBoxes(double[] lowerCornerA, double[] upperCornerA,
			double[] lowerCornerB, double[] upperCornerB) {
		double diffX = Math.max(Math.min(upperCornerA[0], upperCornerB[0]) - Math.max(lowerCornerA[0], lowerCornerB[0]), 0);
		double diffY = Math.max(Math.min(upperCornerA[1], upperCornerB[1]) - Math.max(lowerCornerA[1], lowerCornerB[1]), 0);
		double diffZ = Math.max(Math.min(upperCornerA[2], upperCornerB[2]) - Math.max(lowerCornerA[2], lowerCornerB[2]), 0);

		if (Math.abs(diffZ) <= SETTINGS.ERR_TOLERANCE) {
			// 2D
			return diffX * diffY;
		}

		return diffX * diffY * diffZ;
	}

	public static double calcBoxVol(String[] lowerCorner, String[] upperCorner) {
		double[] l = { Double.parseDouble(lowerCorner[0]), Double.parseDouble(lowerCorner[1]), Double.parseDouble(lowerCorner[2]) };
		double[] u = { Double.parseDouble(upperCorner[0]), Double.parseDouble(upperCorner[1]), Double.parseDouble(upperCorner[2]) };

		return calcBoxVol(l, u);
	}

	public static double calcBoxVol(double[] lowerCorner, double[] upperCorner) {
		double diffX = upperCorner[0] - lowerCorner[0];
		double diffY = upperCorner[1] - lowerCorner[1];
		double diffZ = upperCorner[2] - lowerCorner[2];

		if (Math.abs(diffZ) <= SETTINGS.ERR_TOLERANCE) {
			// 2D
			return diffX * diffY;
		}

		return diffX * diffY * diffZ;
	}

	/*
	 * Envelope lower/upper corner
	 */
	public static double[][] getLowerUpperCorner(Envelope envelope, Logger logger) {
		double[] lowerCorner = new double[3];
		double[] upperCorner = new double[3];

		if (envelope.isSetLowerCorner()) {
			for (int i = 0; i < envelope.getLowerCorner().getValue().size(); i++) {
				lowerCorner[i] = envelope.getLowerCorner().getValue().get(i);
			}

			for (int i = 0; i < envelope.getUpperCorner().getValue().size(); i++) {
				upperCorner[i] = envelope.getUpperCorner().getValue().get(i);
			}
		} else if (envelope.isSetCoord()) {
			Coord lowerCoord = envelope.getCoord().get(0);
			lowerCorner = new double[] { lowerCoord.getX(), lowerCoord.getY(), lowerCoord.getZ() };

			Coord upperCoord = envelope.getCoord().get(1);
			upperCorner = new double[] { upperCoord.getX(), upperCoord.getY(), upperCoord.getZ() };

			if (lowerCorner[0] > upperCorner[0]) {
				for (int i = 0; i < upperCorner.length; i++) {
					double tmp = lowerCorner[i];
					lowerCorner[i] = upperCorner[i];
					upperCorner[i] = tmp;
				}
			}
		} else if (envelope.isSetPos()) {
			DirectPosition lowerPos = envelope.getPos().get(0);
			for (int i = 0; i < lowerPos.getValue().size(); i++) {
				lowerCorner[i] = lowerPos.getValue().get(i);
			}

			DirectPosition upperPos = envelope.getPos().get(1);
			for (int i = 0; i < upperPos.getValue().size(); i++) {
				upperCorner[i] = upperPos.getValue().get(i);
			}

			if (lowerCorner[0] > upperCorner[0]) {
				for (int i = 0; i < upperCorner.length; i++) {
					double tmp = lowerCorner[i];
					lowerCorner[i] = upperCorner[i];
					upperCorner[i] = tmp;
				}
			}
		} else if (envelope.isSetCoordinates()) {
			Coordinates coordinates = envelope.getCoordinates();

			String[] points = coordinates.getValue().split(coordinates.getTs());
			String[] lowerString = points[0].split(coordinates.getCs());
			for (int i = 0; i < lowerString.length; i++) {
				lowerCorner[i] = Double.parseDouble(lowerString[i]);
			}

			String[] upperString = points[1].split(coordinates.getCs());
			for (int i = 0; i < upperString.length; i++) {
				upperCorner[i] = Double.parseDouble(upperString[i]);
			}

			if (lowerCorner[0] > upperCorner[0]) {
				for (int i = 0; i < upperCorner.length; i++) {
					double tmp = lowerCorner[i];
					lowerCorner[i] = upperCorner[i];
					upperCorner[i] = tmp;
				}
			}
		} else {
			logger.warning("WARNING: EMPTY ELEMENT ENVELOPE IN CITY MODEL! NO TILES CREATED.");
			return null;
		}

		return new double[][] { lowerCorner, upperCorner };
	}

	public static double[] getDoubleArray(Point point) {
		double[] result = new double[3];

		if (point.isSetPos()) {
			return getDoubleArray(point.getPos());
		}

		if (point.isSetCoordinates()) {
			// ts must not exist in a point
			return getDoubleArray(point.getCoordinates())[0];
		}

		if (point.isSetCoord()) {
			return getDoubleArray(point.getCoord());
		}

		return null;
	}

	public static double[] getDoubleArray(DirectPosition point) {
		double[] result = new double[3];

		int i = 0;
		for (Double d : point.getValue()) {
			result[i++] = d;
		}

		return result;
	}

	public static double[][] getDoubleArray(Coordinates coordinates) {
		double[][] result;

		String[] points = coordinates.getValue().split(coordinates.getTs());
		result = new double[points.length][3];

		for (int i = 0; i < points.length; i++) {
			String[] values = points[i].split(coordinates.getCs());

			for (int j = 0; j < values.length; j++) {
				result[i][j] = Double.parseDouble(values[j]);
			}
		}

		return result;
	}

	public static double[][] getDoubleArray(DirectPositionList coordinates) {

		int srsDimension = 3;
		int count = coordinates.getValue().size() / srsDimension;

		if (coordinates.isSetSrsDimension() && coordinates.getSrsDimension() != 3) {
			srsDimension = coordinates.getSrsDimension();
		}

		if (coordinates.isSetCount() && coordinates.getCount() != count) {
			count = coordinates.getCount();
		}

		double[][] result = new double[count][srsDimension];

		for (int i = 0; i < coordinates.getValue().size(); i++) {
			result[i / srsDimension][i % srsDimension] = coordinates.getValue().get(i);
		}

		return result;
	}

	public static double[] getDoubleArray(PosOrPointPropertyOrPointRepOrCoord point) {
		double[] result = new double[3];

		if (point.isSetPos()) {
			return getDoubleArray(point.getPos());
		}

		if (point.isSetPointProperty()) {
			return getDoubleArray(point.getPointProperty());
		}

		if (point.isSetPointRep()) {
			return getDoubleArray(point.getPointRep());
		}

		if (point.isSetCoord()) {
			return getDoubleArray(point.getCoord());
		}

		return result;
	}

	public static double[] getDoubleArray(PointProperty point) {
		return getDoubleArray(point.getObject());
	}

	public static double[] getDoubleArray(Coord coord) {
		double[] result = new double[3];

		result[0] = coord.getX();
		result[1] = coord.getY();
		if (coord.isSetZ()) {
			result[2] = coord.getZ();
		}

		return result;
	}

	public static ArrayList<double[]> getDoubleArray(LineString lineString) {
		ArrayList<double[]> result = new ArrayList<double[]>();

		if (lineString.isSetPosOrPointPropertyOrPointRepOrCoord()) {
			for (PosOrPointPropertyOrPointRepOrCoord point : lineString.getPosOrPointPropertyOrPointRepOrCoord()) {
				result.add(getDoubleArray(point));
			}
		}

		if (lineString.isSetPosList()) {
			for (double[] point : getDoubleArray(lineString.getPosList())) {
				result.add(point);
			}
		}

		if (lineString.isSetCoordinates()) {
			for (double[] point : getDoubleArray(lineString.getCoordinates())) {
				result.add(point);
			}
		}

		return result;
	}

	/*
	 * Fuzzy/Error tolerance comparison
	 */
	public static boolean fuzzyEquals(Area area1, Area area2) {
		ArrayList<double[]> points1 = getDoubleArray(area1);
		ArrayList<double[]> points2 = getDoubleArray(area2);

		if (!areaContainsPoints(area1, points2)) {
			return false;
		}

		if (!areaContainsPoints(area2, points1)) {
			return false;
		}

		return true;
	}

	public static ArrayList<double[]> getDoubleArray(Area area) {
		ArrayList<double[]> result = new ArrayList<double[]>();

		PathIterator iterator = area.getPathIterator(null);
		double[] coords = new double[6];

		while (!iterator.isDone()) {
			int type = iterator.currentSegment(coords);

			// 2D points
			double[] point = new double[] {
					coords[0],
					coords[1]
			};

			if (type != PathIterator.SEG_CLOSE) {
				result.add(point);
			}

			iterator.next();
		}

		return result;
	}

	public static boolean areaContainsPoints(Area area, ArrayList<double[]> points) {
		for (double[] point : points) {
			if (!area.contains(point[0], point[1])
					&& !area.contains(point[0] + SETTINGS.ERR_TOLERANCE, point[1])
					&& !area.contains(point[0] - SETTINGS.ERR_TOLERANCE, point[1])
					&& !area.contains(point[0], point[1] + SETTINGS.ERR_TOLERANCE)
					&& !area.contains(point[0], point[1] - SETTINGS.ERR_TOLERANCE)
					&& !area.contains(point[0] + SETTINGS.ERR_TOLERANCE, point[1] + SETTINGS.ERR_TOLERANCE)
					&& !area.contains(point[0] - SETTINGS.ERR_TOLERANCE, point[1] - SETTINGS.ERR_TOLERANCE)
					&& !area.contains(point[0] + SETTINGS.ERR_TOLERANCE, point[1] - SETTINGS.ERR_TOLERANCE)
					&& !area.contains(point[0] - SETTINGS.ERR_TOLERANCE, point[1] + SETTINGS.ERR_TOLERANCE)) {
				return false;
			}
		}

		return true;
	}

	public static boolean fuzzyEquals(double[] point1, double[] point2) {
		if (point1.length != point2.length) {
			return false;
		}

		for (int i = 0; i < point1.length; i++) {
			if (Math.abs(point1[i] - point2[i]) > SETTINGS.ERR_TOLERANCE) {
				return false;
			}
		}

		return true;
	}

	public static boolean fuzzyEquals(ArrayList<double[]> points1, ArrayList<double[]> points2) {
		if (points1.size() != points2.size()) {
			return false;
		}

		for (int i = 0; i < points1.size(); i++) {
			double[] point1 = points1.get(i);
			double[] point2 = points2.get(i);

			if (point1.length != point2.length) {
				return false;
			}

			for (int j = 0; j < point1.length; j++) {
				if (Math.abs(point1[j] - point2[j]) > SETTINGS.ERR_TOLERANCE) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean fuzzyEquals(double[][] points1, double[][] points2) {
		if (points1.length != points2.length) {
			return false;
		}

		for (int i = 0; i < points1.length; i++) {
			double[] point1 = points1[i];
			double[] point2 = points2[i];

			if (point1.length != point2.length) {
				return false;
			}

			for (int j = 0; j < point1.length; j++) {
				if (Math.abs(point1[j] - point2[j]) > SETTINGS.ERR_TOLERANCE) {
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * TESTS
	 */
	public static void main(String[] args) {
		String pointsString = "0.0;0.0;0.0;"
				+ "6.0;0.0;0.0;"
				+ "6.0;0.0;5.0;"
				+ "3.0;0.0;11.0;"
				+ "0.0;0.0;5.0;"
				+ "0.0;0.0;0.0";
		String delimiter = ";";

		// print("BEFORE", convertFromStringTo3dPoints(pointsString, 3, ";"));
		// print("AFTER", transform3dPointsTo2d(pointsString, 3, ";"));
	}
}
