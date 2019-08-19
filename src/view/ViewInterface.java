package view;

import javax.media.j3d.BranchGroup;
import java.awt.*;

public interface ViewInterface {
    TextArea text = new TextArea();
    void addToTree(BranchGroup bg);
    void addToNodes(BranchGroup bg);
    void resetTree();
    void resetNodes();
    static void log(String s){
        text.append(s);
        text.append("\r\n");
    }
}
