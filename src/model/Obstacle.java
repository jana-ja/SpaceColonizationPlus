package model;

import org.jogamp.java3d.Shape3D;
import view.Point3D;

import org.jogamp.java3d.Appearance;

public interface Obstacle {
    String getName();

    Shape3D getShape3D(Appearance appearance);

    boolean isInside(Point3D point);

    boolean isInShadow(Point3D node, SunPosition sunPos);

    boolean isInShadowPlus(Point3D node, SunPosition sunPos);

    Point3D getCentroid();

    Point3D getCentroidBottom();

    Point3D getVectorFromShadow(Point3D node, SunPosition sunPos);
}
