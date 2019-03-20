package controller;

import model.*;
import view.Point3D;

import java.util.*;


class SpaceColonization {


    /**
     * Performs one step of space colonization.
     * Adds nodes to the tree according to influence of attraction points from the point cloud.
     * @param tree
     * @param pointCloud
     */
    void spaceColonize(Tree tree, PointCloud pointCloud){


        if(pointCloud.isEmpty())
            return;

        Map<KDParentTreeNode,List<Point3D>> attractionMap = new HashMap<>();


    //one step

        //ich muss zum baum gehen und sagen "hey hier ist ein attractionpoint, welches ist das nächstgelegene node?"
        //für alle attractionPoints

        //first step: map nodes to their influencing attraction points

        pointCloud.getAttractionPoints().forEach(attractionPoint ->{
            KDParentTreeNode node = tree.getNodes().nearestInRange(attractionPoint, tree.getType().getRadOfInf());

            if (node != null) {
                if(!attractionMap.containsKey(node))
                    attractionMap.put(node, new ArrayList<>());

                attractionMap.get(node).add(attractionPoint);
            }

        });

        //second step: caculate new node for every node in map

        attractionMap.forEach((node, attractionPoints) -> tree.getNodes().insert(calculateNewNode(node,attractionPoints, tree.getType().getNodeDist()),node));


        //third step: remove attraction points that have a node in kill radius distance or less

        pointCloud.getAttractionPoints().removeIf(attractionPoint ->
                tree.getNodes().hasInRange(attractionPoint, tree.getType().getKillRad()));

    }

    /**
     * Returns point of new node given the parent node and all its influencing attraction points.
     * @param node
     * @param attractionPoints
     * @return
     */
    private Point3D calculateNewNode(KDParentTreeNode node, List<Point3D> attractionPoints, double nodeDist) {

        final Point3D infVec = new Point3D(0,0,0);

        attractionPoints.forEach(point -> {
            //get vector from point of node to attraction point
            Point3D vec = point.subtract(node.getPoint());
            //normalize vector
            Point3D vecNorm = vec.divide(vec.distance(new Point3D(0,0,0)));
            //add vector to vector of influence of node
            infVec.addTo(vecNorm);
        });

        //normalize influence vector
        Point3D infVecNorm = infVec.divide(infVec.distance(new Point3D(0,0,0)));

        //multiply with node distance
        Point3D f = infVecNorm.mult(nodeDist);
        //add final vector to point of node
        f.addTo(node.getPoint());
        return f;
    }

    /**
     * Returns a pointcloud fitting the tree type and height.
     * @param type
     * @param treeHeight
     * @return
     */
    PointCloud generatePointCloud(TreeType type, double treeHeight){

        PointCloud pointCloud = new PointCloud();

        List<Point3D> envelope = new ArrayList<>();//generateEnvelope(type);

        switch(type){
            case TREE:
                float xzMin = -(float)(type.getWidthPerHeight() * treeHeight)/2;
                float xzMax = (float)(type.getWidthPerHeight() * treeHeight)/2;

                System.out.println("xzMin "+xzMin);
                System.out.println("xtMax "+xzMax);

                float yMin = (float)(treeHeight - type.getTopPercentage()/100*treeHeight);
                float yMax = (float)treeHeight;

                System.out.println("yMin "+yMin);

                Random random = new Random();


                for(int i = 1; i <= (int)(type.getNodesPerHeight()*treeHeight); i++){
                    float x = random.nextFloat() * (xzMax - xzMin) + xzMin;
                    float y = random.nextFloat() * (yMax - yMin) + yMin;
                    float z = random.nextFloat() * (xzMax - xzMin) + xzMin;

                    envelope.add(new Point3D(x,y,z));
                }
                break;
        }

        pointCloud.setAttractionPoints(envelope);

        return pointCloud;
    }

    private List<Point3D> generateEnvelope(TreeType type){

        switch(type){
            case TREE:


                break;
        }
        return new ArrayList<>();
    }
}
