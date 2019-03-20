package model;

import view.Point3D;

public class KDParentTreeNode {

    private Point3D point;
    private double[] coords;

    private KDParentTreeNode parent;
    private KDParentTreeNode ltbChild;
    private KDParentTreeNode rbfChild;

    KDParentTreeNode(Point3D point, double[] coords, KDParentTreeNode parent){
        this.point = point; this.coords = coords; this.parent=parent;
    }


    public Point3D getPoint() {
        return point;
    }

    public void setPoint(Point3D point) {
        this.point = point;
    }

    double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

    public KDParentTreeNode getParent() {
        return parent;
    }

    public void setParent(KDParentTreeNode parent) {
        this.parent = parent;
    }

    public KDParentTreeNode getLtbChild() {
        return ltbChild;
    }

    void setLtbChild(KDParentTreeNode ltbChild) {
        this.ltbChild = ltbChild;
    }

    public KDParentTreeNode getRbfChild() {
        return rbfChild;
    }

    void setRbfChild(KDParentTreeNode rbfChild) {
        this.rbfChild = rbfChild;
    }
}
