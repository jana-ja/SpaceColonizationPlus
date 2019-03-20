package model;

import view.Point3D;

import java.util.List;

public class Tree {

    private TreeType type;
    private double height;

    private KDParentTree nodes;

    //stammkorrdinate //TODO bei der pointcloud

    public Tree(TreeType type, double height){
        this.type = type;
        this. height = height;

        this.nodes = new KDParentTree(new KDParentTreeNode(new Point3D(0,0,0), new double[]{Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE},null));

        generateStem();
    }

    //TODO dummy
    private void generateStem(){
        //5 inserten
        KDParentTreeNode tmp = nodes.getRoot();
        for(int i = 1; i<6 ; i++){
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