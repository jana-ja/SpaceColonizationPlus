package model;

public enum TreeType {
    // radOfInf, nodeDist, killRad, topPercentage, attPointsPerHeight, widthPerHeight
//    TREE(0.5,0.05, 2*0.05,70.0,1100,1.0), //killRad = 2*nodeDist
    TREE(3.0,0.02, 2*0.03,80.0,900,0.6), //TODO sicherheitskopie
    TREE2(0.51,0.21, 2*0.21,70.1,301,1.1);

    //max neighbors - ????

    private final double radOfInf;
    private final double nodeDist;
    private final double killRad;

    private final double topPercentage;
    private final int attPointsPerHeight;
    private final double widthPerHeight;

    TreeType(double radOfInf, double nodeDist, double killRad, double topPercentage, int nodesPerHeight, double widthPerHeight){
        this.radOfInf = radOfInf;
        this.nodeDist = nodeDist;
        this.killRad = killRad;

        this.topPercentage = topPercentage;
        this.attPointsPerHeight = nodesPerHeight;
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

    public double getAttPointsPerHeight() {
        return attPointsPerHeight;
    }

    public double getWidthPerHeight() {
        return widthPerHeight;
    }
}
