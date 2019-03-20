package view;

public class Point3D {

    //TODO float geeignet?
    private float x;
    private float y;
    private float z;

    public Point3D(float x, float y, float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }

    public double distance(Point3D point){
        return Math.sqrt(Math.pow((point.getX()-x),2) + Math.pow((point.getY()-y),2) + Math.pow((point.getZ()-z),2));
    }

    public float vectorLength(){
        return (float)(Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2)));
    }
    public Point3D subtract(Point3D sub){
        return new Point3D(x -sub.getX(), y-sub.getY(), z-sub.getZ());
    }

    public double dotProduct(Point3D vector){
        return (x*vector.getX() + y*vector.getY() + z*vector.getZ());
    }

    public Point3D divide(double div){
        return new Point3D((float)(x/div), (float)(y/div), (float)(z/div));
    }

    public Point3D mult(double mul){
        return new Point3D((float)(x*mul), (float)(y*mul), (float)(z*mul));
    }

    public void addTo(Point3D sum){
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

    public String toSTring(){
        return (x + " " + y + " " + z);
    }
}
