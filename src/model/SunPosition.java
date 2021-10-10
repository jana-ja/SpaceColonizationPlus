package model;

import org.jogamp.vecmath.Vector3d;
import view.Point3D;



public class SunPosition {
    private double azimuthRadians;
    private double elevationRadians; //von Norden im Uhrzeigersinn

//    public SunPosition(double azimuthDegree, double altitudeDegree) {
//        this.azimuthRadians = Math.toRadians(azimuthDegree);
//        this.elevationRadians = Math.toRadians(altitudeDegree);
//    }

    public SunPosition(double azimuthRadians, double elevationRadians) {
        this.azimuthRadians = azimuthRadians;
        this.elevationRadians = elevationRadians;
    }

    public double getAzimuthRadians() {
        return azimuthRadians;
    }

    public double getElevationRadians() {
        return elevationRadians;
    }

    public Point3D calculateRayVector(){
        float x = (float)((Math.sin(azimuthRadians) * Math.cos(elevationRadians)));
        float y = (float)((Math.sin(elevationRadians)));
        float z = (float)(-(Math.cos(azimuthRadians) * Math.cos(elevationRadians)));
        Point3D ray = new Point3D(x,y,z);
        ray.normalize();
        return ray;
    }
    public Vector3d calculateRayVector3d(){
        double x = (Math.sin(azimuthRadians) * Math.cos(elevationRadians));
        double y = (Math.sin(elevationRadians));
        double z = -(Math.cos(azimuthRadians) * Math.cos(elevationRadians));
        return new Vector3d(x, y, z);
    }
}
