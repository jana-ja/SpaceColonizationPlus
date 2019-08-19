package model;

import view.Point3D;

public class Tree {

    private final TreeType type;
    private final double height;

    private final KDParentTree nodes;

    //TODO stammkoordinate //TODO bei der pointcloud

    public Tree(TreeType type, double height){
        this.type = type;
        this. height = height;

        this.nodes = new KDParentTree(new KDParentTreeNode(new Point3D(0,0,0), new double[]{Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE},null));

//        generateStem();
    }

    @Deprecated
    private void generateStem(){

        //idee: attraction radius hoch setzen und nach und nach runter
        double stemHeight = height - type.getTopPercentage()/100 * height;
        int count = (int)(stemHeight/0.1);
        //5 inserten
        KDParentTreeNode tmp = nodes.getRoot();
        for(int i = 1; i<count+1 ; i++){
            nodes.insert(new Point3D(0,i*0.1f,0), tmp);
            tmp = tmp.getRbfChild();
        }
    }

    public KDParentTree getNodes(){
        return nodes;
    }

    public TreeType getType() {
        return type;
    }

    public double getHeight() {
        return height;
    }
}