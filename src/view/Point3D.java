package view;

import org.jogamp.vecmath.Matrix3d;

public class Point3D {

    private float x, y, z;
    private boolean activated;

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;

        activated = true;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public Point3D normalized() {
        return this.divide((float) (this.distance(new Point3D(0, 0, 0))));
    }

    public void normalize() {
//        if(this.distance(new Point3D(0,0,0))==0)
//            throw new ArithmeticException();
        this.divideFrom((float) (this.distance(new Point3D(0, 0, 0))));
    }

    public double distance(Point3D point) {
        return Math.sqrt(Math.pow((point.getX() - x), 2) + Math.pow((point.getY() - y), 2) + Math.pow((point.getZ() - z), 2));
    }

    public double horDistance(Point3D point) {
        return Math.sqrt(Math.pow((point.getX() - x), 2) + Math.pow((point.getZ() - z), 2));
    }

    public float vectorLength() {
        return (float) (Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)));
    }

    public Point3D subtract(Point3D sub) {
        return new Point3D(x - sub.getX(), y - sub.getY(), z - sub.getZ());
    }

    public float dot(Point3D vector) {
        return (x * vector.getX() + y * vector.getY() + z * vector.getZ());
    }

    public Point3D divide(double div) {
        return new Point3D((float) (x / div), (float) (y / div), (float) (z / div));
    }

    public void divideFrom(float div) {
        this.x /= div;
        this.y /= div;
        this.z /= div;
    }

    public void multTo(float mul) {
        this.x *= mul;
        this.y *= mul;
        this.z *= mul;
    }

    public Point3D mult(double mul) {
        return new Point3D((float) (x * mul), (float) (y * mul), (float) (z * mul));
    }

    public Point3D add(Point3D sum) {
        return new Point3D(x + sum.getX(), y + sum.getY(), z + sum.getZ());
    }

    public Point3D cross(Point3D dot) {
        return new Point3D(y * dot.getZ() - z * dot.getY(), z * dot.getX() - x * dot.getZ(), x * dot.getY() - y * dot.getX());
    }

    public void addTo(Point3D sum) {
        x += sum.getX();
        y += sum.getY();
        z += sum.getZ();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public String toString() {
        return (x + " " + y + " " + z);
    }

    public String cardinalString() {
        double a = Math.round(x * 1000.0) / 1000.0;
        double b = Math.round(y * 1000.0) / 1000.0;
        double c = Math.round(z * 1000.0) / 1000.0;

        String x = String.valueOf(Math.abs(a));
        String y = String.valueOf(Math.abs(b));
        String z = String.valueOf(Math.abs(c));

        if (a < 0)
            x += "W";
        else
            x += "E";
        if (c < 0)
            z += "N";
        else
            z += "S";
        return (x + "\t" + y + "\t" + z);
    }

    public String shortString() {
        return ((Math.round(x * 100.0) / 100.0) + ", " + (Math.round(y * 100.0) / 100.0) + ", " + (Math.round(z * 100.0) / 100.0));
    }

    public boolean equals(Point3D point) {
        return (this.x == point.getX() && this.y == point.getY() && this.z == point.getZ());
    }

    public Point3D toDegrees() {
        return new Point3D((float) (Math.toDegrees(Math.acos(x))), (float) (Math.toDegrees(Math.acos(y))), (float) Math.toDegrees(Math.acos(z)));
//        Point3D angle = new Point3D(0,0,0);
//
//        return angle;
    }

    public String azimuthDegree() {
//        Point3D runterprojiziert = new Point3D(this.x, 0, this.z);
//        Point3D z = new Point3D(0,0,1);
//        //wenn -x westen, wenn +x osten
//        double ergebnis = Math.toDegrees((Math.acos(z.dot(runterprojiziert) / (z.vectorLength() * runterprojiziert.vectorLength()))));
//        ergebnis = Math.round(ergebnis * 1000.0) / 1000.0;
//        String end = (ergebnis >= 90)? "N" : "S";
//        end += (this.x < 0)? "W" : "E";
//        return (ergebnis) + end;







//        Point3D normal = this.normalized();
//        double azimut = Math.toDegrees(Math.asin(normal.getX() / Math.cos(Math.asin(normal.getY()))));
//        if (Double.isNaN(azimut)) //dann auf int casten damits in range ist für arcsin
//            azimut = Math.toDegrees(Math.asin((int) (normal.getX() / Math.cos(Math.asin(normal.getY())))));
//        azimut = Math.round(azimut * 1000.0) / 1000.0;
        Point3D normal = this.normalized();
        float derenX = -normal.getZ();
        float derenY = normal.getX();
        double azimut;
        azimut = Math.acos(derenX / Math.sqrt(derenX * derenX + derenY * derenY));
        if(Double.isNaN(azimut)) //dann auf int casten damits in range ist für arcsin
            azimut = Math.acos((int)(derenX / Math.sqrt(derenX * derenX + derenY * derenY)));
        if (derenY < 0)
            azimut = 2 * Math.PI - azimut;
        azimut = Math.toDegrees(azimut);
        azimut = Math.round(azimut * 1000.0) / 1000.0;

        String end = (azimut >= 90 && azimut <= 270) ? "S" : "N";
        end += (this.x < 0) ? "W" : "E";
        return (azimut) + end;

    }

    public String azimuthPur() {
//        Point3D runterprojiziert = new Point3D(this.x, 0, this.z);
//        Point3D z = new Point3D(0,0,1);
//        //wenn -x westen, wenn +x osten
//        double ergebnis = Math.toDegrees((Math.acos(z.dot(runterprojiziert) / (z.vectorLength() * runterprojiziert.vectorLength()))));
//        ergebnis = Math.round(ergebnis * 1000.0) / 1000.0;
//        return (ergebnis) + "";

//        Point3D normal = this.normalized();
//        double azimut = Math.toDegrees(Math.asin(normal.getX() / Math.cos(Math.asin(normal.getY()))));
//        if(Double.isNaN(azimut)) //dann auf int casten damits in range ist für arcsin
//            azimut = Math.toDegrees(Math.asin((int)(normal.getX() / Math.cos(Math.asin(normal.getY())))));
//        if(azimut == 0.0)
//            azimut = Math.toDegrees(Math.acos((int)(-normal.getZ()/Math.cos(Math.asin((int)(normal.getY()))))));
//        azimut = Math.round(azimut * 1000.0) / 1000.0;
//        return  String.valueOf(azimut);

        Point3D normal = this.normalized();
        float derenX = -normal.getZ();
        float derenY = normal.getX();
        double azimut;
        azimut = Math.acos(derenX / Math.sqrt(derenX * derenX + derenY * derenY));
        if(Double.isNaN(azimut)) //dann auf int casten damits in range ist für arcsin
            azimut = Math.acos((int)(derenX / Math.sqrt(derenX * derenX + derenY * derenY)));
        if (derenY < 0)
            azimut = 2 * Math.PI - azimut;
        azimut = Math.toDegrees(azimut);
        azimut = Math.round(azimut * 1000.0) / 1000.0;
        return String.valueOf(azimut);
    }

    public String elevationDegree() {
//        Point3D y = new Point3D(this.x,0,this.z);
//        y.normalize();
//        double ergebnis = Math.toDegrees(Math.acos(y.dot(this) / (y.vectorLength() * this.vectorLength())));
//        ergebnis = Math.round(ergebnis * 1000.0) / 1000.0;
//        return String.valueOf(ergebnis);

        Point3D normal = this.normalized();
        double elevation = Math.toDegrees(Math.asin(normal.getY()));
        if (Double.isNaN(elevation))
            elevation = Math.toDegrees(Math.asin((int) normal.getY()));
        elevation = Math.round(elevation * 1000.0) / 1000.0;
        return String.valueOf(elevation);
    }

    public void matrixVector(Matrix3d m) {
        double i1 = m.getM00() * x + m.getM01() * y + m.getM02() * z;
        double i2 = m.getM10() * x + m.getM11() * y + m.getM12() * z;
        double i3 = m.getM20() * x + m.getM21() * y + m.getM22() * z;
        this.x = (float) i1;
        this.y = (float) i2;
        this.z = (float) i3;
    }
}
