package model;

import view.Point3D;

import java.util.*;

public class PointCloud {

    private List<Point3D> attractionPoints;

    public PointCloud() {
        this.attractionPoints = new ArrayList<>();
    }

    public PointCloud(List<Point3D> attractionPoints) {
        this.attractionPoints = attractionPoints;
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

    public void updateWithObstacles(List<Obstacle> obstacles){
        for (Point3D point : attractionPoints) {
            boolean outside = true;
            for (Obstacle obstacle : obstacles) {
                if (obstacle.isInside(point))
                    outside = false;
            }
            if (outside)
                point.setActivated(true);
            else
                point.setActivated(false);
        }
    }

    public void shift(Point3D vector) {
        attractionPoints.forEach(ap -> ap.addTo(vector));
    }
}