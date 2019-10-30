package model;

import static model.TreeShape.*;

public enum TreeType {
    // radOfInf, nodeDist, killRad, topPercentage, attPointsPerHeight, widthPerHeight
//    TREE(0.5,0.05, 2*0.05,70.0,1100,1.0), //killRad = 2*nodeDist
    TREE(3.0,0.02, 2*0.03,90.0,600,0.6, CONE),
    PLATANE(3.0,0.02, 2*0.03,80.0,600,0.6, ROUND),
    UMBRELLA(4.0,0.02, 2*0.03,50.0,700,1.4, TreeShape.UMBRELLA);//TODO redofInf mit treehight

    //max neighbors - ????

    private final double radOfInf;
    private final double nodeDist;
    private final double killRad;

    private final double topPercentage;
    private final int attPointsPerHeight;
    private final double widthPerHeight;

    private final TreeShape treeShape;

    TreeType(double radOfInf, double nodeDist, double killRad, double topPercentage, int nodesPerHeight, double widthPerHeight, TreeShape treeShape){
        this.radOfInf = radOfInf;
        this.nodeDist = nodeDist;
        this.killRad = killRad;

        this.topPercentage = topPercentage;
        this.attPointsPerHeight = nodesPerHeight;
        this.widthPerHeight = widthPerHeight;

        this.treeShape = treeShape;
    }

    public double getRadOfInf() {
        return radOfInf;
    }

    public double getNodeDist() {
        return nodeDist;
    }

    public double getKillRad() {
        return killRad;
    }

    public double getTopPercentage() {
        return topPercentage;
    }

    public double getAttPointsPerHeight() {
        return attPointsPerHeight;
    }

    public double getWidthPerHeight() {
        return widthPerHeight;
    }

    public TreeShape getTreeShape(){
        return treeShape;
    }
}
