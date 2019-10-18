package model;

import view.Point3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.Shape3D;

public interface Obstacle {

    boolean isInside(Point3D point);
    Shape3D getShape3D(Appearance appearance);
    Point3D getClosestPoint(Point3D point);
    Point3D getClosestShadowVectorPoint(Point3D point);
    boolean isInShadow(Point3D node, SunPosition sunPos);

    Point3D getDarkestPoint();
    Point3D getVectorFromDarkestPoint(Point3D node);

    Point3D intersectDPVecShadow(Point3D dpVec);
}
