package view;

import model.SunPosition;
import org.jogamp.java3d.Bounds;
import org.jogamp.java3d.BranchGroup;

import java.awt.*;

public interface ViewInterface {
    TextArea text = new TextArea();
    void addToTree(BranchGroup bg);
    void addToNodes(BranchGroup bg);
    void resetTree();
    void resetNodes();
    static void log(String s){ //TODO SHIT?
        text.append(s);
        text.append("\r\n");
    }
    void addMarker(float x, float y, float z);
    void addMarker(float x, float y, float z, Color color);
    void addMarker(float x, float y, float z, Color color, float size);
    void addToScene(BranchGroup bg);

    void setSun(SunPosition sunPos);
    void addLine(Point3D one, Point3D two, Color color);
    void setLine(Point3D one, Point3D two);
    void setSchwerpunkt(Point3D schwerpunkt);

    public Bounds getTreeBounds();
}
