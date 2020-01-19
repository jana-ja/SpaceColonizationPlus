package model;

import static model.TreeShape.*;

public enum TreeType {
    // radOfInf, nodeDist, killRad, topPercentage, attPointsPerHeight, widthPerHeight
    BSP1(2.0,0.02, 2*0.03,80.0,400,0.6, ROUND),//rund spine pointcloud bild
    BSP2(2.0,0.02, 2*0.03,80.0,400,0.6, CONE), //cone spline pointcloud bild
    VERTEILUNG1(3.0,0.02, 2*0.04,50.0,200,1.0, FINGERHUT), //gleichmäßig vs mehr am rand bild: gleichmäßig //TODO: hier muss ich punkte von umbrella benutzen
    VERTEILUNG2(3.0,0.02, 2*0.04,50.0,200,1.0, TreeShape.UMBRELLA2), //gleichmäßig vs mehr am rand bild: rand
    BSP5(2.0,0.04, 2*0.03,80.0,100,0.6, FINGERHUT),//skelett zeigen
    MENGE1(2.0,0.04, 2*0.03,80.0,100,0.6, FINGERHUT),//menge
    MENGE2(2.0,0.04, 2*0.03,80.0,300,0.6, FINGERHUT),//menge
    POSTPRO(2.0,0.02, 2*0.03,80.0,80,0.6, ROUND),//bild bei postprocessing an dem alle sachen gezeigt werden. mit savefile postpr
    ERSTBILD(3.0,0.02, 2*0.03,80.0,200,1.0, FINGERHUT),
    HINDERNISSE1(1.5,0.04, 2*0.03,80.0,200,0.6, FINGERHUT),
    KORKHASE(0.36,0.02, 0.1,80.0,800,1.0, UMBRELLA2),
    BIRKE(2.0,0.01, 15*0.01,80.0,1000,0.4, ROUND),
//    TREE(0.5,0.05, 2*0.05,70.0,1100,1.0), //killRad = 2*nodeDist
    TREE(3.0,0.02, 2*0.03,90.0,600,0.6, CONE),
    PLATANE(7.0,0.02, 2*0.03,80.0,400,0.6, ROUND),
    SONNE1(2.0,0.02, 2*0.05,80.0,300,0.6, ROUND),
    BUSCH(7.0,0.02, 2*0.03,100.0,400,1, V),
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
