package view;

import java.text.DecimalFormat;

public class Point3D {

    private float x, y, z;

    public Point3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void normalize() {
//        if(this.distance(new Point3D(0,0,0))==0)
//            throw new ArithmeticException();
        this.divideFrom((float) (this.distance(new Point3D(0, 0, 0))));
    }

    public double distance(Point3D point) {
        return Math.sqrt(Math.pow((point.getX() - x), 2) + Math.pow((point.getY() - y), 2) + Math.pow((point.getZ() - z), 2));
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

    public Point3D cross(Point3D dot){
        return new Point3D(y*dot.getZ() - z*dot.getY(), z*dot.getX() - x*dot.getZ(), x*dot.getY() - y*dot.getX());
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

    public String cardinalString(){
        double a = Math.round(x * 1000.0) / 1000.0;
        double b = Math.round(y * 1000.0) / 1000.0;
        double c = Math.round(z * 1000.0) / 1000.0;

        String x = String.valueOf(Math.abs(a));
        String y = String.valueOf(Math.abs(b));
        String z = String.valueOf(Math.abs(c));

        if(a < 0)
            x += "W";
        else
            x += "E";
        if(c < 0)
            z += "N";
        else
            z += "S";
        return (x + "\t" + y + "\t" +z);
    }

    public String shortString(){
        return ((double) (Math.round(x * 100.0) / 100.0) + " " + (double) (Math.round(y * 100.0) / 100.0) + " " + (double) (Math.round(z * 100.0) / 100.0));
    }

    public boolean equals(Point3D point) {
        return (this.x == point.getX() && this.y == point.getY() && this.z == point.getZ());
    }

    public Point3D toDegrees(){
        return new Point3D((float)(Math.toDegrees(Math.acos(x))), (float)(Math.toDegrees(Math.acos(y))), (float)Math.toDegrees(Math.acos(z)));
//        Point3D angle = new Point3D(0,0,0);
//
//        return angle;
    }

    public double azimuthDegree(){
        return 0;
    }

    public double elevationDegree(){
        Point3D y = new Point3D(1,0,0);
        return Math.toDegrees(y.dot(this) / (y.vectorLength() * this.vectorLength()));
    }
}
