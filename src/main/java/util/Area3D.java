package util;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class Area3D extends Area {
	private double[] normalVector = null;
	private double distanceToOrigin = Double.NaN;

	public Area3D() {
		super();
	}

	public Area3D(Path2D path) {
		super(path);
	}

	public double[] getNormalVector() {
		return normalVector;
	}

	public void setNormalVector(double[] normalVector) {
		this.normalVector = normalVector;
	}

	public double getDistanceToOrigin() {
		return distanceToOrigin;
	}

	public void setDistanceToOrigin(double distanceToOrigin) {
		this.distanceToOrigin = distanceToOrigin;
	}

	public void add(Area3D area) {
		super.add(area);
		if (area != null) {
			if ((area.getNormalVector() != null)
					&& ((this.getNormalVector() == null) || (this.getNormalVector().length == 0))) {
				this.setNormalVector(area.getNormalVector());
			}
			if ((area.getDistanceToOrigin() != Double.NaN) 
					&& (Double.isNaN(this.getDistanceToOrigin()))) {
				this.setDistanceToOrigin(area.getDistanceToOrigin());
			}
		}
	}
}
