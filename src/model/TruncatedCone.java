package model;

import com.jogamp.nativewindow.util.Point;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.vecmath.*;
import view.Point3D;

import java.awt.*;
import java.awt.geom.Point2D;


public class TruncatedCone extends Group {
    public static final int BODY = 0x00;
    public static final int TOP = 0x01;
    public static final int BOT = 0x02;

    float radiusTop, radiusBot, height;
    private int X_DIVISION = 15;

    //fürn deckel trianglefanarray
    public TruncatedCone(float radiusTop, float radiusBot, float height, Appearance appearance, int flags, Point3D[] points) {
        Point3D v01 = null;
        if (points[0] != null) //parent vom parent könnte null sein, dann ist stammwurzel, einfach standard
            v01 = points[1].subtract(points[0]);
        Point3D v12 = points[2].subtract(points[1]);
        Point3D v23 = points[3].subtract(points[2]);

        int N = X_DIVISION + 1;
        int[] stripCounts = {N + 1}; //+1 für dopplung -> kreis schließen
        NormalGenerator ng = new NormalGenerator();


        //top
        Point3f[] coordsTop = new Point3f[N + 1]; //+1 für midde
        coordsTop[0] = new Point3f(0, height, 0);
        for (int i = 1; i < coordsTop.length; i++) {
            double alpha = 2 * Math.PI / (N - 1) * (N - i); //N-i damit faces nach oben
            float xTop = (float) (radiusTop * Math.cos(alpha));
            float zTop = (float) (radiusTop * Math.sin(alpha));
            coordsTop[i] = new Point3f(xTop, height, zTop);
        }

        //top points transformieren
        Point3D drehachseTop = v12.cross(v23);
        if (drehachseTop.vectorLength() != 0) {
            double angleTop = Math.acos(v12.dot(v23)/(v12.vectorLength()*v23.vectorLength()));
            transform(new Point3f(0,height,0), coordsTop, drehachseTop, angleTop);
        }

        if ((flags & TOP) == TOP) {
            GeometryInfo giTop = new GeometryInfo(GeometryInfo.TRIANGLE_FAN_ARRAY);
            giTop.setCoordinates(coordsTop);
            giTop.setStripCounts(stripCounts);
            ng.generateNormals(giTop);
            Shape3D top = new Shape3D(giTop.getGeometryArray(), appearance);
            this.addChild(top);
        }

        //bottom
        Point3f[] coordsBot = new Point3f[N + 1];
        coordsBot[0] = new Point3f(0, 0, 0);
        for (int i = 1; i < coordsBot.length; i++) {
            double alpha = 2 * Math.PI / (N - 1) * i;
            float x = (float) (radiusBot * Math.cos(alpha));
            float z = (float) (radiusBot * Math.sin(alpha));
            coordsBot[i] = new Point3f(x, 0, z);
        }

        //bottom points transformieren TODO
        if (v01 != null) {
            Point3D drehachseBot = v01.cross(v12);
            if (drehachseBot.vectorLength() != 0) {
                double angleBot = Math.acos(v01.dot(v12)/(v01.vectorLength()*v12.vectorLength()));
                transform(new Point3f(0,0,0), coordsBot, drehachseBot, angleBot);
            }
        }


        if ((flags & BOT) == BOT) {
            GeometryInfo giBot = new GeometryInfo(GeometryInfo.TRIANGLE_FAN_ARRAY);
            giBot.setCoordinates(coordsBot);
            giBot.setStripCounts(stripCounts);
            ng.generateNormals(giBot);
            //folgendes war statt den 3 vorherigen
//            TriangleFanArray botDisc = new TriangleFanArray(coordsBot.length, TriangleFanArray.COORDINATES/*|TriangleFanArray.NORMALS | TriangleFanArray.TEXTURE_COORDINATE_3*/, stripCountsTop);
//            botDisc.setCoordinates(0, coordsBot);
//            Shape3D bot = new Shape3D(botDisc);
            Shape3D bot = new Shape3D(giBot.getGeometryArray(), appearance);
//            bot.setAppearance(appearance);
            this.addChild(bot);
        }

        //body
        Point3f[] coordsBody = new Point3f[4 * (N - 1)];
        for (int i = 0; i < coordsBody.length / 4; i++) {
            coordsBody[4 * i] = coordsBot[(i + 1)]; //nochmal +1 weil bei top und bot mittelpunkt vertext dabei ist dabei ist
            coordsBody[4 * i + 1] = coordsTop[N - (i)];
            coordsBody[4 * i + 2] = coordsTop[N - (i + 1)];
            coordsBody[4 * i + 3] = coordsBot[i + 2];
        }
        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        giBody.setCoordinates(coordsBody);
        ng.generateNormals(giBody);
        Shape3D body = new Shape3D(giBody.getGeometryArray(), appearance);
        this.addChild(body);

    }

    private void transform(Point3f pointMid, Point3f[] points, Point3D drehachse, double alpha) {
        //transformiere zu 0
        for (Point3f point3f : points) {
            point3f.setX(point3f.getX()-pointMid.getX());
            point3f.setY(point3f.getY()-pointMid.getY());
            point3f.setZ(point3f.getZ()-pointMid.getZ());
        }
        drehachse.normalize();

        double n1 = drehachse.getX();
        double n2 = drehachse.getY();
        double n3 = drehachse.getZ();
        double cosA = Math.cos(alpha);
        double dings = 1 - cosA;
        double sinA = Math.sin(alpha);

        //reihenweise
        Matrix3d dreh = new Matrix3d(Math.pow(n1, 2) * dings + cosA, n1 * n2 * dings - n3 * sinA, n1 * n3 * dings + n2 * sinA,
                n2 * n1 * dings + n3 * sinA, Math.pow(n2, 2) * dings + cosA, n2 * n3 * dings - n1 * sinA,
                n3 * n1 * dings - n2 * sinA, n3 * n2 * dings + n1 * sinA, Math.pow(n3, 2) * dings + cosA);

        int i;
        if (Double.isNaN(dreh.getM22())) ;
        i = 3;

        for (Point3f orgPoint : points) {
            Point3D point = new Point3D(orgPoint.getX(), orgPoint.getY(), orgPoint.getZ());
            point.matrixVector(dreh);
            orgPoint.set(point.getX(), point.getY(), point.getZ());
        }

        //transformiere zurück
        for (Point3f point3f : points) {
            point3f.setX(point3f.getX()+pointMid.getX());
            point3f.setY(point3f.getY()+pointMid.getY());
            point3f.setZ(point3f.getZ()+pointMid.getZ());
        }
    }

}
