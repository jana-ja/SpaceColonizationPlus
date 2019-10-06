package view;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Color3f;
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
    void addMarker(float x, float y, float z, Color3f color);
    void addToScene(BranchGroup bg);
}
