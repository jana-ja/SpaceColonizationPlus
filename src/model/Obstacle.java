package model;

import view.Point3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

public interface Obstacle {
    String getName();
    Shape3D getShape3D(Appearance appearance);

    boolean isInside(Point3D point);
    boolean isInShadow(Point3D node, SunPosition sunPos);

    Point3D getCentroid();
    Point3D getCentroidBottom();
    Point3D getVectorFromShadow(Point3D node, SunPosition sunPos);
}
