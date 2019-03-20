package model;

import view.Point3D;

import java.util.*;

public class PointCloud {

    private List<Point3D> attractionPoints;

    public PointCloud() {
        this.attractionPoints = new ArrayList<>();
    }

    public boolean isEmpty() {
        return attractionPoints.isEmpty();
    }

    public List<Point3D> getAttractionPoints() {
        return attractionPoints;
    }

    public void setAttractionPoints(List<Point3D> attractionPoints) {
        this.attractionPoints = attractionPoints;
    }
}