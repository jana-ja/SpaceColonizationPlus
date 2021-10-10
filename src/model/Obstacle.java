package model;

import view.Point3D;

import javax.media.j3d.Texture;
import javax.media.j3d.TransformGroup;

public interface Obstacle {
    String getName();

//    Shape3D getShape3D(Appearance appearance);
    TransformGroup getBox(Texture texture, boolean tex);
    boolean isInside(Point3D point);

    boolean isInShadow(Point3D node, SunPosition sunPos);

    boolean isInShadowPlus(Point3D node, SunPosition sunPos);

    Point3D getCentroid();

    Point3D getCentroidBottom();

    Point3D getVectorFromShadow(Point3D node, SunPosition sunPos);
}
