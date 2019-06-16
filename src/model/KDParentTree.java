package model;

import view.Point3D;

import java.util.*;

public class KDParentTree implements Iterable<List<KDParentTreeNode>> {

    private KDParentTreeNode root;

    private List<KDParentTreeNode> leaves;


    KDParentTree(KDParentTreeNode root){
     this.root = root;
     this.leaves = new ArrayList<>();
    }

    public KDParentTreeNode getRoot() {
        return root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public boolean contains(Point3D point) {
        if (point == null) throw new java.lang.NullPointerException(
                "Error: tried to check if contains null Point3D ");
        return contains(root, point, Level.X);
    }

    public List<KDParentTreeNode> getLeaves(){
        return leaves;
    }

    public KDParentTreeNode nearestInRange(Point3D center, double radius){
        if (center == null) throw new java.lang.NullPointerException(
                "called range() with a null Point3D");


        if (root == null) return null;

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {

            // Examine the next Node
            KDParentTreeNode tmp = nodes.pop();

            // Add contained points to our points stack
            if (inRange(center, radius, tmp.getPoint())) return tmp;

            Point3D topleft = new Point3D((float)(center.getX()-radius), (float)(center.getY()-radius), (float)(center.getZ()-radius));
            if (tmp.getLtbChild() != null && intersectsWith(topleft, 2*radius, tmp.getLtbChild().getCoords())) {
                nodes.push(tmp.getLtbChild());
            }
            if (tmp.getRbfChild() != null && intersectsWith(topleft, 2*radius, tmp.getRbfChild().getCoords())) {
                nodes.push(tmp.getRbfChild());
            }
        }
        return null;
    }

    public boolean hasInRange(Point3D center, double radius){
        if (center == null) throw new java.lang.NullPointerException(
                "called range() with a null Point3D");


        if (root == null) return false;

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {

            // Examine the next Node
            KDParentTreeNode tmp = nodes.pop();

            // Add contained points to our points stack
            if (inRange(center, radius, tmp.getPoint())) return true;

            Point3D topleft = new Point3D((float)(center.getX()-radius), (float)(center.getY()-radius), (float)(center.getZ()-radius));
            if (tmp.getLtbChild() != null && intersectsWith(topleft, 2*radius, tmp.getLtbChild().getCoords())) {
                nodes.push(tmp.getLtbChild());
            }
            if (tmp.getRbfChild() != null && intersectsWith(topleft, 2*radius, tmp.getRbfChild().getCoords())) {
                nodes.push(tmp.getRbfChild());
            }
        }
        return false;
    }

    public Iterable<KDParentTreeNode> range(Point3D center, double radius) {
        if (center == null) throw new java.lang.NullPointerException(
                "called range() with a null Point3D");

        Stack<KDParentTreeNode> points = new Stack<>();


        if (root == null) return points;

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(root);
        while (!nodes.isEmpty()) {

            // Examine the next Node
            KDParentTreeNode tmp = nodes.pop();

            // Add contained points to our points stack
            if (inRange(center, radius, tmp.getPoint())) points.push(tmp);

            Point3D topleft = new Point3D((float)(center.getX()-radius), (float)(center.getY()-radius), (float)(center.getZ()-radius));
            if (tmp.getLtbChild() != null && intersectsWith(topleft, 2*radius, tmp.getLtbChild().getCoords())) {
                nodes.push(tmp.getLtbChild());
            }
            if (tmp.getRbfChild() != null && intersectsWith(topleft, 2*radius, tmp.getRbfChild().getCoords())) {
                nodes.push(tmp.getRbfChild());
            }
        }
        return points;
    }

    public void insert(Point3D point, KDParentTreeNode parent) {
        if(point==null) throw new java.lang.NullPointerException("Error: tried to insert null Point3D to PointCloud");
        else root = insert(root, point, parent, Level.X, new double[] {0, 0, 0, 1, 1, 1});
    }

    private KDParentTreeNode insert(KDParentTreeNode node, Point3D point, KDParentTreeNode parent, Level lvl, double[] coords) {
        if (node == null) {
            KDParentTreeNode newNode = new KDParentTreeNode(point, coords, parent);
            this.leaves.add(newNode);
            this.leaves.removeIf(parentNode -> parentNode.equals(newNode.getParent()));
            return newNode;
        }

        double cmp = comparePoints(point, node, lvl); //1.-2.

        if(node.getPoint().equals(point)){
            //TODO point existiert bereits
        }
        // left
        else if (cmp < 0 && lvl==Level.X){
            coords[3] = node.getPoint().getX(); // lessen x_max
            node.setLtbChild( insert(node.getLtbChild(), point, parent, Level.Y, coords));
//            node.getLtbChild().setParent(parent);
        }

        // right
        else if (cmp >= 0 && lvl==Level.X){
            coords[0] = node.getPoint().getX(); // increase x_min
            node.setRbfChild(insert(node.getRbfChild(), point, parent, lvl.next(), coords));
//            node.getRbfChild().setParent(parent);
        }

        // top
        else if (cmp < 0 && lvl==Level.Y){
            coords[4] = node.getPoint().getY(); // lessen y_max
            node.setLtbChild(insert(node.getLtbChild(), point, parent, lvl.next(), coords));
//            node.getLtbChild().setParent(parent);
        }

        // bottom
        else if (cmp >= 0 && lvl==Level.Y){
            coords[1] = node.getPoint().getY(); // increase y_min
            node.setRbfChild(insert(node.getRbfChild(), point, parent, lvl.next(), coords));
//            node.getRbfChild().setParent(parent);
        }

        //back
        else if (cmp < 0 && lvl==Level.Z){
            coords[5] = node.getPoint().getZ(); //lessen y_max
            node.setLtbChild(insert(node.getLtbChild(), point, parent, lvl.next(), coords));
//            node.getLtbChild().setParent(parent);
        }

        //front
        else if (cmp >= 0 && lvl==Level.Z){
            coords[2] = node.getPoint().getZ(); //increase y_min
            node.setRbfChild(insert(node.getRbfChild(), point, parent, lvl.next(), coords));
//            node.getRbfChild().setParent(parent);
        }

        return node;
    }

    private boolean contains(KDParentTreeNode node, Point3D point, Level lvl) {

        if (node == null) return false;

        if (node.getPoint().equals(point)) return true;

        double cmp = comparePoints(point, node, lvl);

        // smaller path
        if (cmp < 0) return contains(node.getLtbChild(), point, lvl.next());

            // bigger path
        else return contains(node.getRbfChild(), point, lvl.next());
    }

    //checks if sphere defined by center and radius intersect with the cuboid defined by coords
    private boolean intersectsWith(Point3D topleft, double diameter, double[] coords) {
        //check if sphere is anywhere inside rect
        if(topleft.getX() >= coords[0] && topleft.getX() <= coords[3])
            return true;
        if(topleft.getX()+diameter >= coords[0] && topleft.getX()+diameter <= coords[3])
            return true;
        if(topleft.getY() >= coords[1] && topleft.getY() <= coords[4])
            return true;
        if(topleft.getY()+diameter >= coords[1] && topleft.getY()+diameter <= coords[4])
            return true;
        if(topleft.getZ() >= coords[2] && topleft.getZ() <= coords[5])
            return true;
        if(topleft.getZ()+diameter >= coords[2] && topleft.getZ()+diameter <= coords[5])
            return true;

        //check if rect is anywhere inside sphere
        if(coords[0] >= topleft.getX() && coords[0] <= topleft.getX()+diameter)
            return true;
        if(coords[3] >= topleft.getX() && coords[3] <= topleft.getX()+diameter)
            return true;
        if(coords[1] >= topleft.getY() && coords[1] <= topleft.getY()+diameter)
            return true;
        if(coords[4] >= topleft.getY() && coords[4] <= topleft.getY()+diameter)
            return true;
        if(coords[2] >= topleft.getZ() && coords[2] <= topleft.getZ()+diameter)
            return true;

        return coords[5] >= topleft.getZ() && coords[5] <= topleft.getZ() + diameter;
    }

    private boolean inRange(Point3D center, double radius, Point3D point) {
        return center.distance(point)<= radius;
    }

    private double comparePoints(Point3D point, KDParentTreeNode node, Level lvl) {
        switch(lvl) {
            case X:
                return point.getX()-node.getPoint().getX();
            case Y:
                return point.getY()-node.getPoint().getY();
            case Z:
                return point.getZ()-node.getPoint().getZ();
            default:
                return -1;

        }
    }

    @Override
    public Iterator<List<KDParentTreeNode>> iterator() {
        return new TreeIterator(leaves);
    }

    private static final class TreeIterator implements Iterator<List<KDParentTreeNode>>{

        private List<KDParentTreeNode> current;

        public TreeIterator(List<KDParentTreeNode> leaves){
            this.current = leaves;
        }

        @Override
        public boolean hasNext() {
            for(KDParentTreeNode node : current){
                if(node.hasParent()){
                    return true;
                }
            }
            return false;
        }

        @Override
        public List<KDParentTreeNode> next() {
            if(!this.hasNext()){
                throw new NoSuchElementException();
            }
            List<KDParentTreeNode> next = new ArrayList<>();
            current.forEach(node -> {if(node.hasParent()) next.add(node.getParent());});
            current = next;
            return next;
        }
    }
    private enum Level{
        X, Y, Z;

        Level next(){
            switch(this){
                case X: return Y;
                case Y: return Z;
                case Z: return X;
                default: return null;
            }
        }
    }


}
