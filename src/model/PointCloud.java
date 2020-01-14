package model;

import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
import view.Point3D;

import java.util.*;

public class PointCloud {

    private PolynomialSplineFunction function;
    private List<Point3D> attractionPoints;

    public PointCloud() {
        this.attractionPoints = new ArrayList<>();
    }

    public PointCloud(List<Point3D> attractionPoints) {
        this.attractionPoints = attractionPoints;
    }

    public PolynomialSplineFunction getFunction() {
        return function;
    }

    public void setFunction(PolynomialSplineFunction function) {
        this.function = function;
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

    public void shift2(Point3D lastAvgNode, Point3D avgNode) {
        Point3D shiftVector = avgNode.subtract(lastAvgNode);

        attractionPoints.forEach(ap -> {
            try {
                double radius = function.value(ap.getY()); //radius an der höhe

                //wenn abstand zu lastAvg < radius && abstan dzu avg > radius dann muss der rüber
                if(lastAvgNode.horDistance(ap) < radius && avgNode.horDistance(ap) > radius){
                    //rüber
                    //erstmal punktsymmetrisch und dann schieben
                    Point3D apZUlastAvg = lastAvgNode.subtract(ap);
                    apZUlastAvg.setY(0);
                    ap.addTo(apZUlastAvg.mult(2));
                    ap.addTo(shiftVector);

                }
            } catch (ArgumentOutsideDomainException e) {
                e.printStackTrace();
            }
        });


    }
}