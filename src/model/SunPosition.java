package model;

public class SunPosition {
    private double azimuthRadians;
    private double altitudeRadians; //von Norden im Uhrzeigersinn

    public SunPosition(double azimuthDegree, double altitudeDegree) {
        this.azimuthRadians = Math.toRadians(azimuthDegree);
        this.altitudeRadians = Math.toRadians(altitudeDegree);
    }

    public double getAzimuthRadians() {
        return azimuthRadians;
    }

    public double getAltitudeRadians() {
        return altitudeRadians;
    }
}
