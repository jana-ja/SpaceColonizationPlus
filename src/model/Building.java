package model;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import controller.SpaceColonization;
import org.apache.commons.math.fraction.Fraction;
import org.apache.commons.math.fraction.FractionConversionException;
import view.Point3D;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import java.util.*;


public class Building implements Obstacle {
    private Point3D corner1, corner2;

    private Point3D centroid;

    private BoundingBox bounds;

//    Point3D[] topCorners; //gegen den uhrzeigersinn

    private float maxX,  minX,  maxZ, minZ, maxY, minY;

    public Building(Point3D corner1, Point3D corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;

        maxX = Math.max(corner1.getX(), corner2.getX());
        minX = Math.min(corner1.getX(), corner2.getX());
        maxZ = Math.max(corner1.getZ(), corner2.getZ());
        minZ = Math.min(corner1.getZ(), corner2.getZ());

        maxY = Math.max(corner1.getY(), corner2.getY());
        minY = Math.min(corner1.getY(), corner2.getY());

        bounds = new BoundingBox(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));


//        topCorners = new Point3D[4];
//        topCorners[0] = new Point3D(maxX, maxY, maxZ); // oben rechts
//        topCorners[1] = new Point3D(minX, maxY, maxZ); // oben links
//        topCorners[2] = new Point3D(minX, maxY, minZ); // unten links
//        topCorners[3] = new Point3D(maxX, maxY, minZ); // unten rechts
    }

    @Override
    public boolean isInside(Point3D point) {
        if (point.getX() > Math.max(corner1.getX(), corner2.getX())
                || point.getX() < Math.min(corner1.getX(), corner2.getX())
                || point.getY() > Math.max(corner1.getY(), corner2.getY())
                || point.getY() < Math.min(corner1.getY(), corner2.getY())
                || point.getZ() > Math.max(corner1.getZ(), corner2.getZ())
                || point.getZ() < Math.min(corner1.getZ(), corner2.getZ()))
            return false;
        return true;
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
        //0 1 2 3 4 - length 5 - ungerade
        //0 1 2 3 - length 4 - gerade
        for (int i = 0; i < array.length/2; i++){
            Point3f temp = array[i];
            array[i] = array[array.length-i-1];
            array[array.length-i-1] = temp;
        }

    }
    @Override
    public Point3D getClosestPoint(Point3D point) {

        //Fall 1: inside
        if(isInside(point))
            return null;
        //Fall 2: drüber oder drunter
        if(point.getY() > Math.max(corner1.getY(), corner2.getY())
        || point.getY() < Math.min(corner1.getY(), corner2.getY()))
            return null;

        //Fall 3: neben oder ecke


        float x;
        float y = point.getY();
        float z;
        //oben
        if(point.getZ() > maxZ){
            z = maxZ;
        }
        //unten
        else if(point.getZ() < minZ){
            z = minZ;
        }
        //mitte Z
        else{
            z = point.getZ();
        }

        //rechts
        if(point.getX() > maxX){
            x = maxX;
        }
        //links
        else if (point.getX() < minX){
            x = minX;
        }
        //mitte X
        else {
            x = point.getX();
        }

        return new Point3D(x,y,z);
    }

    @Override
    public Point3D getClosestShadowVectorPoint(Point3D point) {//vorraussetzung dass
        //TODO null wenn nicht im schatten

        //überprüfung nicht notwendig da in getClosestPoint immer bereits überprüft
//        //Fall 1: inside
//        if(isInside(point))
//            return null;
//        //Fall 2: drüber oder drunter
//        if(point.getY() > Math.max(corner1.getY(), corner2.getY())
//                || point.getY() < Math.min(corner1.getY(), corner2.getY()))
//            return null;


        //north shadow vectors
        //punkt berechnen auf dem vektor mit point.getZ()
        Fraction slope;
        try {
            slope = new Fraction(Math.tan(SpaceColonization.SUN_ANGLE));
        } catch (FractionConversionException e) {
            slope = new Fraction(2,3); //TODO
            e.printStackTrace();
        }

        //sonne scheint von -Z nach Z
        Point3D shadowVector = new Point3D(0, slope.getNumerator(), slope.getDenominator()); //TODO ist gar nicht 0 x
        //TODO sonnenwinkel für x achse überlegen oder andere lösung
        shadowVector.normalize();
        //geradengleichung

        //NE
        Point3D cornerNE = new Point3D(maxX, maxY, maxZ);
        //NW
        Point3D cornerNW = new Point3D(minX, maxY, maxZ);
        //gleichung: punkt + s * vector

        //normale der gerade ist X bei buidling

        //gucken ob punkt von Z Achse her im schatten ist
        float t = (- cornerNE.getY() / shadowVector.getY()); //TODO groundpoint nicht mit y=0 sondern da wo baum anfängt!!!
        Point3D groundPoint = shadowVector.mult(t).add(cornerNE);
        if(point.getZ() <= groundPoint.getZ() && point.getZ() >= cornerNE.getZ()){
            //gucken ob punkt zwischen den schatten vektoren ist X Achse
            //TODO wenn oben vektor nicht mehr trivial mit 0 dann hier anpassen
            if(point.getX() >= minX && point.getX() <= maxX){
                //punkt ist zwischen schattenvektoren NE und NW

                //gucken ob punkt von höhe her mschatten Y Achse
                float s = (point.getZ() - cornerNE.getZ()) / shadowVector.getZ();
                //cornerNE.Z + s * shadowVector.Z = point.Z
                Point3D abovePointOnSV = shadowVector.mult(s);
                Point3D abovePointNE = abovePointOnSV.add(cornerNE);
                Point3D abovePointNW = abovePointOnSV.add(cornerNW);

                Point3D abovePoint = lineIntersection(cornerNE, shadowVector.cross(new Point3D(1,0,0)), point, new Point3D(0,1,0));

                if(/*Math.min(abovePointNE.getY(), abovePointNW.getY())*/ abovePoint.getY() >= point.getY() && point.getY() >= groundPoint.getY()){
                    //ist im schatten
                    //TODO genauer machen bzw ist das richtig?
                    //welche punkt ist am nächsten?
                    //nach osten und wetsen reicht mit verändertem y
                    //nach oben mit mitte zwischen NE x und NW x
                    //nach norden mit normale
                    Point3D pointNE = new Point3D(abovePointNE.getX(), point.getY(), point.getZ()); //TODO geht so nur weil die shatten parallel sind, oben 0 im vektor. sonst z anpassen unf normale zur ebene nehmen
                    double distanceNE = pointNE.distance(point);
                    Point3D pointNW = new Point3D(abovePointNW.getX(), point.getY(), point.getZ());
                    double distanceNW = pointNW.distance(point);
                    //3 punkte für die ebene: abovePointNE, cornerNE, cornerNW
                    //first plane vector is shadowvector
                    Point3D secondPlaneVector = cornerNE.subtract(cornerNW);
                    Point3D pointN = lineIntersection(cornerNE, shadowVector.cross(secondPlaneVector), point, shadowVector.cross(secondPlaneVector));
                    double distanceN = pointN.distance(point);

                    if(distanceNE <= distanceNW && distanceNE <= distanceN)
                        return pointNE;
                    else if(distanceNW <= distanceN)
                        return  pointNW;
                    else
                        return pointN;

                }
            } else if(true /*macht erst sinn wenn oben nicht mehr trivial mit 0, sonnensinkel waagerecht fehlt TODO*/){

            }
        }

        return null;

    }

    @Override
    public Point3D getCentroid() {
        if(centroid == null)
            calculateCentroid();
        return centroid;
    }

    @Override
    public Point3D getDarkestPoint(Point3D node, SunPosition sunPos) {
        if(centroid == null)
            calculateCentroid();
        Point3D ray = sunPos.calculateRayVector();

        //ray mit allen wänden schneiden
        //von den punkten den mit kleinster distance zu node nehmen

        //NS wände sind mit normale (1,0,0)
        Point3D nsNormal = new Point3D(0,0,1);
        //EW wände sind mit normale (0,0,1)
        Point3D ewNormal = new Point3D(1,0,0);

        //punkt für SW wände
        Point3D swPoint = new Point3D(minX, minY, minZ);
        //punkt für ne wände
        Point3D nePoint = new Point3D(maxX, maxY, maxZ);

        List<Point3D> points = new ArrayList<>();
        //schnitt norden
        points.add(zWallIntersection(nePoint, nsNormal, this.centroid, ray));

        //schnitt east
        points.add(xWallIntersection(nePoint, ewNormal, this.centroid, ray));

        //schnitt south
        points.add(zWallIntersection(swPoint, nsNormal, this.centroid, ray));

        //schnitt west
        points.add(xWallIntersection(swPoint, ewNormal, this.centroid, ray));

        points.removeIf(point -> point==null);

        Optional<Point3D> fin = points.stream().min(Comparator.
                comparing(p -> p.distance(node)));

        if(fin.isPresent())
                return fin.get();//TODO was tun mit y??
        else
            return null;
    }

    private void calculateCentroid(){
        //weil quader: (sonst alle punkte addieren und durch anzahl teilen)
        this.centroid = new Point3D((minX+maxX)/2, minY, (minZ+maxZ)/2);
    }

    private Point3D xWallIntersection(Point3D planePoint, Point3D planeNormal, Point3D linePoint, Point3D lineDirection){
        Point3D sec = lineIntersection(planePoint, planeNormal, linePoint, lineDirection);
        if(sec!=null){
            //check x und y
            if(sec.getX() >= minX && sec.getX() <= maxX && sec.getY() >= minY && sec.getY() <= maxY){
                return  sec;
            }
        }
        return null;
    }
    private Point3D zWallIntersection(Point3D planePoint, Point3D planeNormal, Point3D linePoint, Point3D lineDirection){
        Point3D sec = lineIntersection(planePoint, planeNormal, linePoint, lineDirection);
        if(sec!=null){
            //check z und y
            if(sec.getZ() >= minZ && sec.getZ() <= maxZ && sec.getY() >= minY && sec.getY() <= maxY){
                return  sec;
            }
        }
        return null;
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
    public Point3D getVectorFromDarkestPoint(Point3D node, SunPosition sunPos){
        if(getDarkestPoint(node,sunPos)==null){
            int f = 32;
        }
        return node.subtract(getDarkestPoint(node, sunPos));
    }

}
