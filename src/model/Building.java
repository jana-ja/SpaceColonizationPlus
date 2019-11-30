package model;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BoundingBox;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import view.Point3D;



public class Building implements Obstacle {
    private Point3D corner1, corner2;
    private String name;

    private Point3D centroid;

    private BoundingBox bounds;

    private float maxX,  minX,  maxZ, minZ, maxY, minY;

    public Building(String name, Point3D corner1, Point3D corner2) {
        this.name = name;
        this.corner1 = corner1;
        this.corner2 = corner2;

        maxX = Math.max(corner1.getX(), corner2.getX());
        minX = Math.min(corner1.getX(), corner2.getX());
        maxZ = Math.max(corner1.getZ(), corner2.getZ());
        minZ = Math.min(corner1.getZ(), corner2.getZ());

        maxY = Math.max(corner1.getY(), corner2.getY());
        minY = Math.min(corner1.getY(), corner2.getY());

        bounds = new BoundingBox(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));
    }
    public Building(Point3D corner1, Point3D corner2) {
        this("", corner1, corner2);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInside(Point3D point) {
        return bounds.intersect(new Point3d(point.getX(), point.getY(), point.getZ()));
    }

    @Override
    public Shape3D getShape3D(Appearance appearance) {
        Point3f[] point3fs = new Point3f[8];
        point3fs[0] = new Point3f(corner1.getX(), corner1.getY(), corner1.getZ());
        point3fs[1] = new Point3f(corner1.getX(), corner2.getY(), corner1.getZ());
        point3fs[2] = new Point3f(corner2.getX(), corner2.getY(), corner1.getZ());
        point3fs[3] = new Point3f(corner2.getX(), corner1.getY(), corner1.getZ());
        point3fs[4] = new Point3f(corner2.getX(), corner1.getY(), corner2.getZ());
        point3fs[5] = new Point3f(corner2.getX(), corner2.getY(), corner2.getZ());
        point3fs[6] = new Point3f(corner1.getX(), corner2.getY(), corner2.getZ());
        point3fs[7] = new Point3f(corner1.getX(), corner1.getY(), corner2.getZ());

        Point3f[] quadArray = new Point3f[4*6 *2];
        int j = 0;
        for(int i = 0 ; i <= 4; i+=4){
            quadArray[j++] = point3fs[i];
            quadArray[j++] = point3fs[i+1];
            quadArray[j++] = point3fs[(i+2)%8];
            quadArray[j++] = point3fs[(i+3)%8];
        }
        for(int i = 2; i <= 6; i +=4){
            quadArray[j++] = point3fs[i];
            quadArray[j++] = point3fs[(i+3)%8];
            quadArray[j++] = point3fs[(i+2)%8];
            quadArray[j++] = point3fs[i+1];
        }
        for(int i = 0; i < 8; i++){
            quadArray[j++] = point3fs[i];
        }

        swapArray(point3fs);

        for(int i = 0 ; i <= 4; i+=4){
            quadArray[j++] = point3fs[i];
            quadArray[j++] = point3fs[i+1];
            quadArray[j++] = point3fs[(i+2)%8];
            quadArray[j++] = point3fs[(i+3)%8];
        }
        for(int i = 2; i <= 6; i +=4){
            quadArray[j++] = point3fs[i];
            quadArray[j++] = point3fs[(i+3)%8];
            quadArray[j++] = point3fs[(i+2)%8];
            quadArray[j++] = point3fs[i+1];
        }
        for(int i = 0; i < 8; i++){
            quadArray[j++] = point3fs[i];
        }

        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        giBody.setCoordinates(quadArray);
        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(giBody);
        Shape3D shape = new Shape3D(giBody.getGeometryArray(), appearance);
//        shape.setCapability(Shape3D.ALLOW_BOUNDS_READ);
//        shape.setCapability(Shape3D.ALLOW_BOUNDS_WRITE);
//        shape.setBoundsAutoCompute(false);
//        shape.setCollisionBounds(bounds);

        return shape;
    }

    private void swapArray(Point3f[] array){
        for (int i = 0; i < array.length/2; i++){
            Point3f temp = array[i];
            array[i] = array[array.length-i-1];
            array[array.length-i-1] = temp;
        }
    }

    @Override
    public Point3D getCentroidBottom() {
        if(centroid == null)
            calculateCentroid();
        return new Point3D(centroid.getX(), 0, centroid.getZ());
    }

    @Override
    public Point3D getCentroid() {
        if(centroid == null)
            calculateCentroid();
        return centroid;
    }

    private void calculateCentroid(){
        //weil quader: (sonst alle punkte addieren und durch anzahl teilen)
        this.centroid = new Point3D((minX+maxX)/2, (minY+maxY)/2, (minZ+maxZ)/2);
    }

    private static Point3D lineIntersection(Point3D planePoint, Point3D planeNormal, Point3D linePoint, Point3D lineDirection) {
        lineDirection.normalize();
        if (planeNormal.dot(lineDirection) == 0) {
            return null; //parallel
        }

        float t = (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(lineDirection);

        lineDirection.multTo(t);

        return linePoint.add(lineDirection);
    }

    @Override
    public boolean isInShadow(Point3D node, SunPosition sunPos){ //TODO auch true wen strahl erst node und dann building trifft
        Vector3d ray = sunPos.calculateRayVector3d();
        ray.normalize();

        return bounds.intersect(new Point3d(node.getX(), node.getY(), node.getZ()), ray);
    }


    @Override
    public Point3D getVectorFromShadow(Point3D node, SunPosition sunPos){
        Point3D ray = sunPos.calculateRayVector();

        //richtung des vektors: senkrecht von gerade(ray durch centroid) durch node

        //dafür hilfsebene (ray, cantroid-node) nehmen
        Point3D vectorCentroidNode = node.subtract(getCentroidBottom());
        //und davon die senkrechte
        Point3D perpendicular1 = ray.cross(vectorCentroidNode);

        //von der ebene (perpendicular, ray) die senkrechte nehmen (die ist senkrecht zu ray und kann so platziert werden dass sie node schneidet)
        Point3D perpendicular2 = perpendicular1.cross(ray);
        perpendicular2.normalize();

        return perpendicular2;

    }

}
