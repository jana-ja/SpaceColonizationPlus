package model;

import org.jogamp.vecmath.*;

public class Triangle {

    Point3d p1;
    Point3d p2;
    Point3d p3;

    Vector3d planeNormal;

    public Triangle (Point3d p1, Point3d p2, Point3d p3){
        this.p1=p1;
        this.p2=p2;
        this.p3=p3;

        calculatePlaneNormal();
    }

    public Triangle(Point3d[] point3ds){
        if(point3ds.length!=3)
            throw new IllegalArgumentException("whuaaaa");
        p1 = point3ds[0];
        p2 = point3ds[1];
        p3 = point3ds[2];

        calculatePlaneNormal();

    }

    private void calculatePlaneNormal(){
        Vector3d one = new Vector3d();
        one.sub(p1,p2);
        Vector3d two = new Vector3d();
        two.sub(p2,p3);
        Vector3d planeNormal = new Vector3d();
        planeNormal.cross(one,two);

        this.planeNormal = planeNormal;
    }

    public Point3d centroid(){
        Point3d point = new Point3d(p1.getX(),p1.getY(),p1.getZ());
        point.add(p2);
        point.add(p3);
        point.scale(1.0/3.0);
        return point;
    }

    public boolean intersect (Point3d point, Vector3d ray){
        calculatePlaneNormal();
        //line intersection mit der ebene des dreiecke kriegen
        Point3d intersection = lineIntersection(p1, planeNormal, point, ray);

        //liegt intersection punkt im dreieck?
        if(intersection != null){
            return isInside(intersection);
        }
        else return false; //TODO parallele gelten grad auch als angeleuchtet, aber ist null wenn ray in falsche richtung schneidet, deshalb false

    }

    boolean isInside(Point3d point){
        Vector3d u = new Vector3d();
        u.sub(p2,p1);
        Vector3d v = new Vector3d();
        v.sub(p3,p1);

        Vector3d n = new Vector3d();
        n.cross(u,v);

        Vector3d w = new Vector3d();
        w.sub(point,p1);

        Vector3d uw = new Vector3d();
        uw.cross(u,w);
        Vector3d wv = new Vector3d();
        wv.cross(w,v);

        double gamma = uw.dot(n) / n.lengthSquared();
        double beta = wv.dot(n) / n.lengthSquared();
        double alpha = 1-gamma-beta;

//        Vector3d bla = new Vector3d();
//        double area = bla.length()/2;

//        Vector3d pa = new Vector3d();
//        pa.sub(point, p1);
//        Vector3d pb = new Vector3d();
//        pb.sub(point, p2);
//        Vector3d pc = new Vector3d();
//        pc.sub(point, p3);
//
//        Vector3d boahA = new Vector3d();
//        boahA.cross(pb,pc);
//        Vector3d boahB = new Vector3d();
//        boahB.cross(pc,pa);
//        double alpha = boahA.length()/(2*area);
//        double beta = boahB.length()/(2*area);
//        double gamma = 1-alpha-beta;

        //alle drei müssen zwishcen 0 und 1 sein
        if(alpha < 0 || alpha > 1 || beta < 0 || beta > 1 || gamma < 0 || gamma > 1) {
//            System.out.println("false");
            return false;
        }
        else{
//            System.out.println("true");
            return true;

        }
    }

    private static Point3d lineIntersection(Point3d planePoint, Vector3d planeNormal, Point3d linePoint, Vector3d lineDirection) {
        lineDirection.normalize();
        planeNormal.normalize();

        if (planeNormal.dot(lineDirection) == 0) {
            return null; //parallel
        }

        Vector3d planePointVector = new Vector3d(planePoint);
        Vector3d linePointVector = new Vector3d(linePoint);
        //strahl = linepoint + t * linedirection
        //ebene planenormal und planepoint
        //t = planepoint -
        double t = (planeNormal.dot(planePointVector) - planeNormal.dot(linePointVector)) / planeNormal.dot(lineDirection);

        if(t <= 0)
            return null;
        lineDirection.scale(t);
        Point3d point = new Point3d();
        point.add(linePoint,lineDirection);
        return point;


    }

    public double area(){
        Vector3d ab = new Vector3d();
        ab.sub(p1,p2);
        Vector3d ac = new Vector3d();
        ac.sub(p1,p3);

        Vector3d bla = new Vector3d();
        bla.cross(ab,ac);
        return bla.length()/2.0;
    }
}
