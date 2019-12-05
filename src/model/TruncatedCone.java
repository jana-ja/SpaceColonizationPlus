package model;

import controller.Application;
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
    private int X_DIVISION = /*1*/5;

    //fürn deckel trianglefanarray
    public TruncatedCone(float radiusTop, float radiusBot, float height, Appearance appearance, int flags, Point3D[] points) {
        Point3D v01 = null;
        if (points[0] != null) //parent vom parent könnte null sein, dann ist stammwurzel, einfach standard
            v01 = points[1].subtract(points[0]);
        Point3D v12 = points[2].subtract(points[1]);
        Point3D v23 = null;
        if (points[3].vectorLength()!=0)
            v23 = points[3].subtract(points[2]);

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
        if (v23 != null) {
            Point3D drehachseTop = v12.cross(v23);
            if (drehachseTop.vectorLength() != 0) {
                double angleTop = Math.acos(v12.dot(v23) / (v12.vectorLength() * v23.vectorLength())) / 2;
                transform(new Point3f(0, height, 0), coordsTop, drehachseTop, angleTop);
            }
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
                double angleBot = -Math.acos(v01.dot(v12) / (v01.vectorLength() * v12.vectorLength())) / 2;
                transform(new Point3f(0, 0, 0), coordsBot, drehachseBot, angleBot);
            }
        }
        if (Application.l >= 1) {
            System.out.print("top: ");
            for (Point3f point3f : coordsTop) {
                System.out.print("new Point3f(" + point3f.getX() + "f, " + point3f.getY() + "f, " + point3f.getZ() + "f), ");
            }
            System.out.print("\n");
        }
        if (Application.l >= 2) {
            System.out.print("bot: ");
            for (Point3f point3f : coordsBot) {
                System.out.print("new Point3f(" + point3f.getX() + "f, " + point3f.getY() + "f, " + point3f.getZ() + "f), ");
            }
            System.out.print("\n");
//            coordsBot = new Point3f[]{new Point3f(0.0f, 0.2f, 0.0f), new Point3f(0.05f, 0.2f, -1.0417469E-17f), new Point3f(0.045677274f, 0.2106917f, -0.017299544f), new Point3f(0.03345653f, 0.21953472f, -0.031607836f), new Point3f(0.01545085f, 0.22500001f, -0.040450852f), new Point3f(-0.005226423f, 0.22614256f, -0.042299543f), new Point3f(-0.025f, 0.22276482f, -0.03683426f), new Point3f(-0.04045085f, 0.21545085f, -0.025f), new Point3f(-0.04890738f, 0.20546529f, -0.008843012f), new Point3f(-0.04890738f, 0.19453472f, 0.008843012f), new Point3f(-0.04045085f, 0.18454915f, 0.025f), new Point3f(-0.025f, 0.17723519f, 0.03683426f), new Point3f(-0.005226423f, 0.17385745f, 0.042299543f), new Point3f(0.01545085f, 0.175f, 0.040450852f), new Point3f(0.03345653f, 0.18046528f, 0.031607836f), new Point3f(0.045677274f, 0.1893083f, 0.017299544f), new Point3f(0.05f, 0.2f, 0.0f), new Point3f(0.0f, 0.22360681f, 0.0f), new Point3f(0.025f, 0.22360681f, -6.0441678E-18f), new Point3f(0.022838637f, 0.22197801f, -0.010037116f), new Point3f(0.016728265f, 0.22063084f, -0.018338723f), new Point3f(0.007725425f, 0.21979825f, -0.0234694f), new Point3f(-0.0026132115f, 0.21962419f, -0.024542002f), new Point3f(-0.0125f, 0.22013876f, -0.02137107f), new Point3f(-0.020225424f, 0.221253f, -0.014504886f), new Point3f(-0.02445369f, 0.22277422f, -0.0051306756f), new Point3f(-0.02445369f, 0.2244394f, 0.0051306756f), new Point3f(-0.020225424f, 0.22596063f, 0.014504886f), new Point3f(-0.0125f, 0.22707486f, 0.02137107f), new Point3f(-0.0026132115f, 0.22758943f, 0.024542002f), new Point3f(0.007725425f, 0.22741537f, 0.0234694f), new Point3f(0.016728265f, 0.22658278f, 0.018338723f), new Point3f(0.022838637f, 0.22523561f, 0.010037116f), new Point3f(0.025f, 0.22360681f, 0.0f), new Point3f(0.0f, 0.07071067f, 0.0f), new Point3f(0.02f, 0.07071067f, -4.525704E-18f), new Point3f(0.018270908f, 0.067597635f, -0.007515513f), new Point3f(0.013382612f, 0.06502288f, -0.013731525f), new Point3f(0.00618034f, 0.06343159f, -0.017573232f), new Point3f(-0.0020905691f, 0.06309892f, -0.018376367f), new Point3f(-0.01f, 0.06408239f, -0.016002063f), new Point3f(-0.01618034f, 0.066211954f, -0.010860855f), new Point3f(-0.019562952f, 0.06911938f, -0.003841707f), new Point3f(-0.019562952f, 0.072301954f, 0.003841707f), new Point3f(-0.01618034f, 0.07520938f, 0.010860855f), new Point3f(-0.01f, 0.07733894f, 0.016002063f), new Point3f(-0.0020905691f, 0.07832241f, 0.018376367f), new Point3f(0.00618034f, 0.07798974f, 0.017573232f), new Point3f(0.013382612f, 0.076398455f, 0.013731525f), new Point3f(0.018270908f, 0.0738237f, 0.007515513f), new Point3f(0.02f, 0.07071067f, 0.0f)};
//            Building.swapArray(coordsBot);
//            Building.swapArray(coordsTop);
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

        if(Application.l%2 ==0)
        Building.swapArray(coordsBody);

        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        giBody.setCoordinates(coordsBody);
        ng.generateNormals(giBody);
        Shape3D body = new Shape3D(giBody.getGeometryArray(), appearance);
        this.addChild(body);

        Application.l++;

    }

    public static void transform(Point3f pointMid, Point3f[] points, Point3D drehachse, double alpha) {
        //transformiere zu 0
        for (Point3f point3f : points) {
            point3f.setX(point3f.getX() - pointMid.getX());
            point3f.setY(point3f.getY() - pointMid.getY());
            point3f.setZ(point3f.getZ() - pointMid.getZ());
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
            point3f.setX(point3f.getX() + pointMid.getX());
            point3f.setY(point3f.getY() + pointMid.getY());
            point3f.setZ(point3f.getZ() + pointMid.getZ());
        }
    }

}
