package view;

import javax.media.j3d.BranchGroup;

public interface ViewInterface {
    void addToTree(BranchGroup bg);
    void addToNodes(BranchGroup bg);
}
