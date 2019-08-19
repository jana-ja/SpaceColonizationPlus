package controller;

import model.*;
import view.Point3D;
import view.ViewInterface;

import java.util.*;

import static java.awt.geom.Point2D.distance;


class SpaceColonization {


    /**
     * Performs one step of space colonization.
     * Adds nodes to the tree according to influence of attraction points from the point cloud.
     *
     * @param tree
     * @param pointCloud
     */
    boolean spaceColonize(Tree tree, PointCloud pointCloud) {


        if (pointCloud.isEmpty())
            return false;

        Map<KDParentTreeNode, List<Point3D>> attractionMap = new HashMap<>();


        //one step

        //ich muss zum baum gehen und sagen "hey hier ist ein attractionpoint, welches ist das nächstgelegene node?"
        //für alle attractionPoints

        //first step: map nodes to their influencing attraction points

        pointCloud.getAttractionPoints().forEach(attractionPoint -> {
            KDParentTreeNode node = tree.getNodes().nearestInRange(attractionPoint, tree.getType().getRadOfInf());

            if (node != null) {
                if (!attractionMap.containsKey(node))
                    attractionMap.put(node, new ArrayList<>());

                attractionMap.get(node).add(attractionPoint);
            }

        });

        //if attractionMap is empty -> space colonization is finished
        if(attractionMap.isEmpty()){
            System.out.println("abbruch");
            return false;
        }

        ViewInterface.log("      mapped nodes: " + attractionMap.size());

        //second step: calculate new node for every node in map

        attractionMap.forEach((node, attractionPoints) -> {
            Point3D newPoint = calculateNewNode(node, attractionPoints, tree.getType().getNodeDist());
            //testen ob newNode.point = point von nem kind von node
            boolean isNew = true;
            for (KDParentTreeNode child : node.getTreeChildren()) {
                if(child.getPoint().equals(newPoint)) {
                    isNew = false;
                    pointCloud.getAttractionPoints().removeAll(attractionPoints);
                    ViewInterface.log("   unlimited growing problem prevented");
                    break;
                }
            }
            if(isNew) tree.getNodes().insert(newPoint, node);
        });


        //third step: remove attraction points that have a node in kill radius distance or less
        //TODO anders machen? removeIf sache überprüfen.
        tree.getNodes().getAll().forEach( node ->
            pointCloud.getAttractionPoints().removeIf(attractionPoint ->
                attractionPoint.distance(node.getPoint()) < tree.getType().getKillRad()
            ));
//        pointCloud.getAttractionPoints().removeIf(attractionPoint ->
//                tree.getNodes().hasInRange(attractionPoint, tree.getType().getKillRad()));

        return true;
    }

    /**
     * Returns point of new node given the parent node and all its influencing attraction points.
     *
     * @param node
     * @param attractionPoints
     * @return
     */
    private Point3D calculateNewNode(KDParentTreeNode node, List<Point3D> attractionPoints, double nodeDist) {

        final Point3D infVec = new Point3D(0, 0, 0);

        attractionPoints.forEach(point -> {
            //get vector from point of node to attraction point
            Point3D vec = point.subtract(node.getPoint());
            //normalize vector
            Point3D vecNorm = vec.divide(vec.distance(new Point3D(0, 0, 0))); //gibt den betrag, deswegen zu (0,0,0)
            //add vector to vector of influence of node
            infVec.addTo(vecNorm);
        });

        //normalize influence vector
        Point3D infVecNorm = infVec.divide(infVec.distance(new Point3D(0, 0, 0)));

        //multiply with node distance
        Point3D f = infVecNorm.mult(nodeDist);
        //add final vector to point of node
        f.addTo(node.getPoint());
        return f;
    }

    /**
     * Returns a pointcloud fitting the tree type and height.
     *
     * @param type
     * @param treeHeight
     * @return
     */
    PointCloud generatePointCloud(TreeType type, double treeHeight) {

        PointCloud pointCloud = new PointCloud();

        double treeWidth = type.getWidthPerHeight() * treeHeight;
        List<Point3D> envelope = new ArrayList<>();//generateEnvelope(type);
        List<Point3D> envelope2 = new ArrayList<>();//generateEnvelope(type);


        Random random = new Random();

        //würfel um volumen bauen
        //würfel gleichverteilt füllen
        //schnitt(würfel,volumen) behalten -> für jeden punkt: ist in volumen?

        //würfel bauen
        float xzMin = -(float)treeWidth/2;
        float xzMax = (float)treeWidth/2;

        float yMin = (float)(treeHeight - type.getTopPercentage()/100*treeHeight);
        float yMax = (float)treeHeight;


        for(int i = 1; i <= (int)(type.getAttPointsPerHeight()*treeHeight); i++){
            float x = random.nextFloat() * (xzMax - xzMin) + xzMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z = random.nextFloat() * (xzMax - xzMin) + xzMin;

            envelope.add(new Point3D(x,y,z));
        }


        //punkte checken mit funktion
        switch (type) {
            case TREE:

                double maxDistance;
                float xForDistance;
                double realDistance;
                for (Point3D point3D : envelope) {

                    //Formel
                    xForDistance = (float)((treeWidth/2) * Math.sin(((2 * Math.PI) / (treeHeight*2)) * point3D.getY()));

                    maxDistance = distance(0.0f, point3D.getY(), xForDistance, point3D.getY());
                    realDistance = point3D.distance(new Point3D(0.0f, point3D.getY(), 0.0f));
                    if(realDistance <= maxDistance ){
                        envelope2.add(point3D);
                    }
                }
                break;

        }
        pointCloud.setAttractionPoints(envelope2);

        return pointCloud;
    }
}
