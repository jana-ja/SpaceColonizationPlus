package model;

public enum TreeType {
    // radOfInf, nodeDist, killRad, topPercentage, nodesPerHeight, widthPerHeight
    TREE(0.5,0.2, 2*0.2,70.0,400,1.0), //killRad = 2*nodeDist
    TREE2(0.51,0.21, 2*0.21,70.1,301,1.1);

    //max neighbors - ????

    private final double radOfInf;
    private final double nodeDist;
    private final double killRad;

    private final double topPercentage;
    private final int nodesPerHeight;
    private final double widthPerHeight;

    TreeType(double radOfInf, double nodeDist, double killRad, double topPercentage, int nodesPerHeight, double widthPerHeight){
        this.radOfInf = radOfInf;
        this.nodeDist = nodeDist;
        this.killRad = killRad;

        this.topPercentage = topPercentage;
        this.nodesPerHeight = nodesPerHeight;
        this.widthPerHeight = widthPerHeight;
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

    public double getNodesPerHeight() {
        return nodesPerHeight;
    }

    public double getWidthPerHeight() {
        return widthPerHeight;
    }
}
