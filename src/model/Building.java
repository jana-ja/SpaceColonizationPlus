package model;

import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.geometry.Box;
import org.jogamp.vecmath.*;
import view.Point3D;

import java.awt.*;


public class Building implements Obstacle {
    private Point3D corner1, corner2;
    private String name;

    private Point3D centroid;

    private BoundingBox bounds;

    private float maxX, minX, maxZ, minZ, maxY, minY;

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
    public TransformGroup getBox(Texture texture, boolean tex) {

        Appearance appearance = new ShaderAppearance();
        appearance.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
        Color3f gray = new Color3f((float) (1.0 / 255) * Color.darkGray.getRed(), (float) (1.0 / 255) *Color.darkGray.getGreen(), (float) (1.0 / 255) *Color.darkGray.getBlue());
        Material mat = new Material(gray, gray, gray, gray, 1.0f);
//        mat.setDiffuseColor(Color.darkGray.getRed(), Color.darkGray.getGreen(), Color.darkGray.getBlue(), 1);
        appearance.setMaterial(mat);

        appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.2f));

        float xl = maxX - minX;
        float yl = maxY - minY;
        float zl = maxZ - minZ;
        Box obstBox = new Box(xl / 2, yl / 2, zl / 2, appearance); //durch 2 weil die nehmen das wie nen radius?? why

        int[] sides = new int[]{Box.TOP, Box.BOTTOM, Box.LEFT, Box.RIGHT, Box.FRONT, Box.BACK};

        if (tex) {
            for (int side : sides) {
                Appearance appearance1 = new ShaderAppearance();
                appearance1.setTexCoordGeneration(this.generateTexCoord(obstBox.getShape(side)));
                appearance1.setTexture(texture);
                obstBox.setAppearance(side, appearance1);
            }
        }
        Transform3D t = new Transform3D();
        t.setTranslation(new Vector3d(xl / 2 + minX, yl / 2 + minY, zl / 2 + minZ));
        TransformGroup tg = new TransformGroup(t);
        tg.addChild(obstBox);
        return tg;
    }

    private TexCoordGeneration generateTexCoord(Shape3D shape) {
        BoundingBox bb = new BoundingBox(shape.getBounds());
        Point3d lower = new Point3d();
        Point3d upper = new Point3d();
        bb.getLower(lower);
        bb.getUpper(upper);

        double width = upper.x - lower.x;
        double height = upper.y - lower.y;
        double deep = upper.z - lower.z;
        Vector4f planeX = new Vector4f((float) (1.0 / width), 0.0f, 0.0f, (float) (-lower.x / width));
        Vector4f planeY = new Vector4f(0.0f, (float) (1.0 / height), 0.0f, (float) (-lower.y / height));
        Vector4f planeZ = new Vector4f(0.0f, 0.0f, (float) (1.0 / deep), (float) (-lower.z / deep));

        TexCoordGeneration tcg = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2);
        if (width == 0) { // RIGHT, LEFT: YZ
            tcg.setPlaneS(planeZ);
            tcg.setPlaneT(planeY);
        } else if (height == 0) { // TOP, BOTTOM: XZ
            tcg.setPlaneS(planeX);
            tcg.setPlaneT(planeZ);
        } else { // FRONT, BACK: XY
            tcg.setPlaneS(planeX);
            tcg.setPlaneT(planeY);
        }
        return tcg;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInside(Point3D point) {
        return bounds.intersect(new Point3d(point.getX(), point.getY(), point.getZ()));
    }

//    @Override
//    public Shape3D getShape3D(Appearance appearance) {
//        Point3f[] point3fs = new Point3f[8];
//        point3fs[0] = new Point3f(corner1.getX(), corner1.getY(), corner1.getZ());
//        point3fs[1] = new Point3f(corner1.getX(), corner2.getY(), corner1.getZ());
//        point3fs[2] = new Point3f(corner2.getX(), corner2.getY(), corner1.getZ());
//        point3fs[3] = new Point3f(corner2.getX(), corner1.getY(), corner1.getZ());
//        point3fs[4] = new Point3f(corner2.getX(), corner1.getY(), corner2.getZ());
//        point3fs[5] = new Point3f(corner2.getX(), corner2.getY(), corner2.getZ());
//        point3fs[6] = new Point3f(corner1.getX(), corner2.getY(), corner2.getZ());
//        point3fs[7] = new Point3f(corner1.getX(), corner1.getY(), corner2.getZ());
//
//        Point3f[] quadArray = new Point3f[4*6 *2];
//        int j = 0;
//        for(int i = 0 ; i <= 4; i+=4){
//            quadArray[j++] = point3fs[i];
//            quadArray[j++] = point3fs[i+1];
//            quadArray[j++] = point3fs[(i+2)%8];
//            quadArray[j++] = point3fs[(i+3)%8];
//        }
//        for(int i = 2; i <= 6; i +=4){
//            quadArray[j++] = point3fs[i];
//            quadArray[j++] = point3fs[(i+3)%8];
//            quadArray[j++] = point3fs[(i+2)%8];
//            quadArray[j++] = point3fs[i+1];
//        }
//        for(int i = 0; i < 8; i++){
//            quadArray[j++] = point3fs[i];
//        }
//
//        swapArray(point3fs);
//
//        for(int i = 0 ; i <= 4; i+=4){
//            quadArray[j++] = point3fs[i];
//            quadArray[j++] = point3fs[i+1];
//            quadArray[j++] = point3fs[(i+2)%8];
//            quadArray[j++] = point3fs[(i+3)%8];
//        }
//        for(int i = 2; i <= 6; i +=4){
//            quadArray[j++] = point3fs[i];
//            quadArray[j++] = point3fs[(i+3)%8];
//            quadArray[j++] = point3fs[(i+2)%8];
//            quadArray[j++] = point3fs[i+1];
//        }
//        for(int i = 0; i < 8; i++){
//            quadArray[j++] = point3fs[i];
//        }
//
//        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
//        giBody.setCoordinates(quadArray);
//        NormalGenerator ng = new NormalGenerator();
//        ng.generateNormals(giBody);
//        //        shape.setCapability(Shape3D.ALLOW_BOUNDS_READ);
////        shape.setCapability(Shape3D.ALLOW_BOUNDS_WRITE);
////        shape.setBoundsAutoCompute(false);
////        shape.setCollisionBounds(bounds);
//
//        return new Shape3D(giBody.getGeometryArray(), appearance);
//    }

    public static void swapArray(Point3f[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Point3f temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    @Override
    public Point3D getCentroidBottom() {
        if (centroid == null)
            calculateCentroid();
        return new Point3D(centroid.getX(), 0, centroid.getZ());
    }

    @Override
    public Point3D getCentroid() {
        if (centroid == null)
            calculateCentroid();
        return centroid;
    }

    private void calculateCentroid() {
        //weil quader: (sonst alle punkte addieren und durch anzahl teilen)
        this.centroid = new Point3D((minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2);
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
    public boolean isInShadow(Point3D node, SunPosition sunPos) { //TODO auch true wenn strahl erst node und dann building trifft
        Vector3d ray = sunPos.calculateRayVector3d();
        ray.normalize();

        return bounds.intersect(new Point3d(node.getX(), node.getY(), node.getZ()), ray);
    }

    @Override
    public boolean isInShadowPlus(Point3D node, SunPosition sunPos) {
        if (!isInShadow(node, sunPos))
            return false;
        //testen ob ray anderen teil des gebäudes schneidet
        Vector3d ray = sunPos.calculateRayVector3d();
        ray.normalize();
        //TODO MUSS NOCH HER
        return isInShadow(node, sunPos);
    }


    @Override
    public Point3D getVectorFromShadow(Point3D node, SunPosition sunPos) {
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
