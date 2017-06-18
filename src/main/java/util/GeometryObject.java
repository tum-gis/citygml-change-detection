package util;

import java.awt.geom.Area;
import java.util.ArrayList;

/**
 * Suggestions, bug reports, etc. please contact: son.nguyen@tum.de
 *
 */
public class GeometryObject {
	private Area area;
	private ArrayList<String[]> curve;
	private String[] point;

	public GeometryObject() {
		this.area = null;
		this.curve = null;
		this.point = null;
	}

	public GeometryObject(Object obj) {
		if (obj instanceof Area) {
			this.area = (Area) obj;
		} else if (obj instanceof ArrayList<?>) {
			this.curve = (ArrayList<String[]>) obj;
		} else if (obj instanceof String[]) {
			this.point = (String[]) obj;
		}
	}

	public GeometryObject(GeometryObject obj) {
		if (obj.area != null) {
			this.area = obj.area;
			return;
		}

		if (obj.curve != null) {
			this.curve = obj.curve;
			return;
		}

		if (obj.point != null) {
			this.point = obj.point;
			return;
		}
	}

	public void add(GeometryObject obj) {
		if (obj.area != null) {
			this.area.add(obj.area);
			return;
		}

		if (obj.curve != null) {
			this.curve.addAll(obj.curve);
			return;
		}

		if (obj.point != null) {
			this.curve = new ArrayList<String[]>();
			this.curve.add(this.point);
			this.curve.add(obj.point);
			this.point = null;
			return;
		}
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public ArrayList<String[]> getCurve() {
		return curve;
	}

	public void setCurve(ArrayList<String[]> curve) {
		this.curve = curve;
	}

	public String[] getPoint() {
		return point;
	}

	public void setPoint(String[] point) {
		this.point = point;
	}
}
