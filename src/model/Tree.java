package model;

import view.Point3D;

import java.util.List;

public class Tree {

    private final TreeType type;
    private final double height;

    private int count;

    private final KDParentTree nodes;

    public Tree(TreeType type, double height, Point3D root) {
        this.type = type;
        this.height = height;

        this.nodes = new KDParentTree(new KDParentTreeNode(root, new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}, null));

    }

    public Point3D calculateAngle() {
        count = 0;
        Point3D vec = calculateAngleRek(nodes.getRoot());
        vec.divideFrom(count);
        vec.normalize();
        return vec;
    }

    private Point3D calculateAngleRek(KDParentTreeNode node) {
        count++;

        if (node.getTreeChildren().isEmpty())
            return node.parentAngle();
        Point3D childAngle = new Point3D(0, 0, 0);
        node.getTreeChildren().forEach(child -> {
            childAngle.addTo(calculateAngleRek(child));
        });
        return node.parentAngle().add(childAngle);

    }


    public KDParentTree getNodes() {
        return nodes;
    }

    public TreeType getType() {
        return type;
    }

    public double getHeight() {
        return height;
    }

    public Point3D calculateAvgNode(){
        Point3D avgNode = new Point3D(0,0,0);
        this.nodes.getAll().forEach(node -> avgNode.addTo(node.getPoint()));
        avgNode.divideFrom(nodes.getAll().size());
        return avgNode;
        //mit gewichtung (thickness?)
    }

    public Point3D calculateSchwerpunkt(){
        Point3D schwerpunkt = new Point3D(0,0,0);
        final float[] thickness = {0};
        this.nodes.getAll().forEach(node -> {
            schwerpunkt.addTo(node.getPoint().mult(node.getThickness()));
            thickness[0] += node.getThickness();
        });
        schwerpunkt.divideFrom(nodes.getAll().size());
//        schwerpunkt.divideFrom(thickness[0]);
        return schwerpunkt;
        //mit gewichtung (thickness?)
    }


}