package model;

import org.jogamp.vecmath.Point3f;
import view.Point3D;

import java.util.ArrayList;
import java.util.List;

public class KDParentTreeNode {

    private Point3D point;
    private double[] coords;
    private float thickness;
    private float thicknessHelpSum;
    private Point3f[] pointsTop;
    private Point3f[] pointsBot;



    private KDParentTreeNode treeParent;
    private KDParentTreeNode ltbChild;
    private KDParentTreeNode rbfChild;
    private List<KDParentTreeNode> treeChildren;

    public KDParentTreeNode(Point3D point, double[] coords, KDParentTreeNode parent){
        this.point = point; this.coords = coords; this.treeParent =parent;
        treeChildren = new ArrayList<>();
        this.thicknessHelpSum = 0.0f;
    }

    void addTreeChild(KDParentTreeNode child){
        this.treeChildren.add(child);
    }


    float getThicknessHelpSum() {
        return thicknessHelpSum;
    }

    void resetThicknessHelpSum() {
        this.thicknessHelpSum = 0.0f;
    }

    public List<KDParentTreeNode> getTreeChildren() {
        return treeChildren;
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

    public float getThickness() {
        return thickness;
    }

    void setThickness(float thickness) {
        this.thickness = thickness;
    }

    boolean hasParent(){
        return treeParent !=null;
    }

    public KDParentTreeNode getTreeParent() {
        return treeParent;
    }

    public void setTreeParent(KDParentTreeNode treeParent) {
        this.treeParent = treeParent;
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

    void addToThicknessSum(float thickness) {
        this.thicknessHelpSum+=thickness;
    }

    public Point3D parentAngle() {
        if(treeParent == null)
            return new Point3D(0,0,0);
        else
            return this.point.subtract(treeParent.getPoint());
    }

    public Point3f[] getPointsTop() {
        return pointsTop;
    }

    public void setPointsTop(Point3f[] pointsTop) {
        this.pointsTop = pointsTop;
    }

    public Point3f[] getPointsBot() {
        return pointsBot;
    }

    public void setPointsBot(Point3f[] pointsBot) {
        this.pointsBot = pointsBot;
    }

    public void setTreeChildren(List<KDParentTreeNode> treeChildren){
        this.treeChildren = treeChildren;
    }
}
