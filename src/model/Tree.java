package model;

import view.Point3D;

public class Tree {

    private final TreeType type;
    private final double height;

    private final KDParentTree nodes;

    public Tree(TreeType type, double height, Point3D root){
        this.type = type;
        this. height = height;

        this.nodes = new KDParentTree(new KDParentTreeNode(root, new double[]{Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE},null));

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