package model;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.vecmath.*;
import view.Point3D;


public class TruncatedCone extends Group {
    public static final int BODY = 0x00;
    public static final int TOP = 0x01;
    public static final int BOT = 0x02;


    //für node und parent
    public TruncatedCone(KDParentTreeNode node, float height, Appearance appearance, int flags){


        int N = Tree.X_DIVISION + 1;
        int[] stripCounts = {N + 1}; //+1 für dopplung -> kreis schließen
        NormalGenerator ng = new NormalGenerator();


        //top
        Point3f[] coordsTop = new Point3f[node.getPointsTop().length];
        Point3f addi = new Point3f(0,height,0);
        //top punkte nach oben verschieben
        for (int i = 0; i < coordsTop.length; i++) {
            coordsTop[i] = new Point3f();
            coordsTop[i].add(node.getPointsTop()[i], addi);
        }
        Building.swapArray(coordsTop);


        if ((flags & TOP) == TOP) {
            GeometryInfo giTop = new GeometryInfo(GeometryInfo.TRIANGLE_FAN_ARRAY);
            giTop.setCoordinates(coordsTop);
            giTop.setStripCounts(stripCounts);
            ng.generateNormals(giTop);
            Shape3D top = new Shape3D(giTop.getGeometryArray(), appearance);
            this.addChild(top);
        }
        Building.swapArray(coordsTop);

        //bottom
        Point3f[] coordsBot = node.getTreeParent().getPointsBot();
//        Building.swapArray(coordsBot);

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
            coordsBody[4 * i] = coordsBot[i+1]; //nochmal +1 weil bei top und bot mittelpunkt vertext dabei ist dabei ist
            coordsBody[4 * i + 1] = coordsTop[i+1];
            coordsBody[4 * i + 2] = coordsTop[i+2];
            coordsBody[4 * i + 3] = coordsBot[i+2];
        }

//        Building.swapArray(coordsBody);

        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        giBody.setCoordinates(coordsBody);
        ng.generateNormals(giBody);
        Shape3D body = new Shape3D(giBody.getGeometryArray(), appearance);
        this.addChild(body);

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
