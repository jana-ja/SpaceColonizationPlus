package model;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

import javax.media.j3d.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import java.awt.*;

public class TruncatedCone extends Group {
    public static final int BODY = 0x00;
    public static final int TOP = 0x01;
    public static final int BOT = 0x02;

    float radiusTop, radiusBot, height;
    int X_DIVISION = 15;

    //fürn deckel trianglefanarray
    public TruncatedCone(float radiusTop, float radiusBot, float height, Appearance appearance, int flags) {

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
        Point3f[] coordsBody = new Point3f[4 * (N-1)];
        for (int i = 0; i < coordsBody.length/4 ; i++) {
            coordsBody[4*i] = coordsBot[(i +1)]; //nochmal +1 weil bei top und bot mittelpunkt vertext dabei ist dabei ist
            coordsBody[4*i+1] = coordsTop[N - (i )];
            coordsBody[4*i+2] = coordsTop[N - (i+1)];
            coordsBody[4*i+3] = coordsBot[i+2];
        }
        GeometryInfo giBody = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        giBody.setCoordinates(coordsBody);
        ng.generateNormals(giBody);
        Shape3D body = new Shape3D(giBody.getGeometryArray(), appearance);
        this.addChild(body);

    }

}
