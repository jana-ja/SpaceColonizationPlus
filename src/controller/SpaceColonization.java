package controller;

import model.*;
import view.Point3D;
import view.ViewInterface;

import java.util.*;

import static java.awt.geom.Point2D.distance;


public class SpaceColonization {

    public static final double SUN_ANGLE = 40;

    /**
     * Performs one step of space colonization.
     * Adds nodes to the tree according to influence of attraction points from the point cloud.
     *
     * @param tree
     * @param pointCloud
     */
    boolean spaceColonize(Tree tree, PointCloud pointCloud, List<Obstacle> obstacles) {


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
        if (attractionMap.isEmpty()) {
            System.out.println("abbruch");
            return false;
        }

        ViewInterface.log("      mapped nodes: " + attractionMap.size());

        //second step: calculate new node for every node in map

        attractionMap.forEach((node, attractionPoints) -> {
            //attraction vector
            Point3D apVector = calculateInfluenceVector(node, attractionPoints);

            //liste mit closest points kriegen für obstacles
//            List<Point3D> obstacleAPs = new ArrayList<>();
//            obstacles.forEach(obstacle -> {
//                if(obstacle.getClosestPoint(node.getPoint())!=null) {
//                    if(obstacle.getClosestShadowVectorPoint(node.getPoint()) != null)
//                      obstacleAPs.add(obstacle.getClosestShadowVectorPoint(node.getPoint()));
//                }
//            });

            //detraction vector
//            Point3D obstVector = calculateLightAttractionVector(node, obstacles);
            Point3D obstVector = calculateShadowDetractrionVector(node, obstacles);


            Point3D finalVector = apVector.add(obstVector);
//            Point3D finalVector = apVector;

            //norm final vector
            finalVector.normalize();

            Point3D newPoint = finalVector.mult(tree.getType().getNodeDist());

            //vector zu node point addieren
            newPoint.addTo(node.getPoint());
            //testen ob newNode.point = point von nem kind von node
            boolean isNew = true;
            for (KDParentTreeNode child : node.getTreeChildren()) {
                if (child.getPoint().equals(newPoint)) {
                    isNew = false;
                    pointCloud.getAttractionPoints().removeAll(attractionPoints);
                    ViewInterface.log("   unlimited growing problem prevented");
                    break;
                }
            }
            if (isNew) tree.getNodes().insert(newPoint, node);
        });


        //third step: remove attraction points that have a node in kill radius distance or less
        //TODO debug anders machen? removeIf sache überprüfen.
        tree.getNodes().getAll().forEach(node ->
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
     * @param influencePoints
     * @return
     */
    private Point3D calculateInfluenceVector(KDParentTreeNode node, List<Point3D> influencePoints) {

        final Point3D inflVec = new Point3D(0, 0, 0);

        influencePoints.forEach(influencePoint -> {
            //get vector from node to attraction point
            Point3D ipVec = influencePoint.subtract(node.getPoint());

            //normalize vector
            ipVec.normalize();

            //add vector to vector of influence of node
            inflVec.addTo(ipVec);


        });

        //normalize attraction vector
        inflVec.normalize();

//        //multiply with node distance
//        Point3D f = attrVecNorm.mult(nodeDist);
//        //add final vector to point of node
//        f.addTo(node.getPoint());
        return inflVec;
    }

    private Point3D calculateShadowDetractrionVector(KDParentTreeNode node, List<Obstacle> obstacles){
        final Point3D inflVec = new Point3D(0, 0, 0);

        obstacles.forEach(obstacle -> {

                if(obstacle.getClosestShadowVectorPoint(node.getPoint()) != null) { //dann ist im schatten
                    Point3D darkestPoint = obstacle.getDarkestPoint();

                    //get vector from darkest point to node
                    Point3D dpVec = node.getPoint().subtract(darkestPoint);

                    float factor;
                    int maxPercent = 100; //bestimmt intensität der schattenflucht
                    factor = 100 - (float)(maxPercent / darkestPoint.distance(obstacle.intersectDPVecShadow(dpVec)) * darkestPoint.distance(node.getPoint()));

                    dpVec.normalize();
                    dpVec.multTo(factor/100);

                    //add vector to vector of influence of node
                    inflVec.addTo(dpVec);
                }

        });

        //normalize attraction vector
//            inflVec.normalize();

        //TODO obstacles sind grad nicht normalisiert, gute lösung finden mit dem faktor undso

        return inflVec;
    }
    private Point3D calculateLightAttractionVector(KDParentTreeNode node, List<Obstacle> obstacles) {

        final Point3D inflVec = new Point3D(0, 0, 0);
        final Point3D[] influencePoint = new Point3D[1];
        obstacles.forEach(obstacle -> {
            if(obstacle.getClosestPoint(node.getPoint())!=null) {
                if(obstacle.getClosestShadowVectorPoint(node.getPoint()) != null) {
                    influencePoint[0] = obstacle.getClosestShadowVectorPoint(node.getPoint());

                    //get vector from node to influence point
                    Point3D ipVec = influencePoint[0].subtract(node.getPoint());

                    float factor;
                    int maxPercent = 100; //bestimmt intensität der schattenflucht
                    factor = (float) (maxPercent / influencePoint[0].distance(obstacle.getClosestPoint(influencePoint[0])) * influencePoint[0].distance(node.getPoint()));

                    ipVec.normalize();
                    ipVec.multTo(factor/100 );

                    //add vector to vector of influence of node
                    inflVec.addTo(ipVec);
                }
            }
        });

        //normalize attraction vector
//            inflVec.normalize();

        //TODO obstacles sind grad nicht normalisiert, gute lösung finden mit dem faktor undso

        return inflVec;
    }

    /**
     * Returns a pointcloud fitting the tree type and height.
     *
     * @return
     */
    PointCloud generatePointCloud(Tree tree, List<Obstacle> obstacles) {
        //würfel um volumen bauen
        List<Point3D> cloud = fillPointCloudCuboid(tree);

        //schnitt(würfel,volumen) behalten -> für jeden punkt: ist in volumen?
        cloud = buildTreeShape(tree, cloud);

        //schnitt cloud obstacles
        cloud = intersectWithObstacles(obstacles, cloud);

        return new PointCloud(cloud);
    }



    private List<Point3D> fillPointCloudCuboid(Tree tree) {
        List<Point3D> cloud = new ArrayList<>();
        Random random = new Random();

        Point3D rootCoordinates = tree.getNodes().getRoot().getPoint();
        TreeType type = tree.getType();
        double treeHeight = tree.getHeight();
        double crownHeight = treeHeight * type.getTopPercentage() / 100;
        double treeWidth = type.getWidthPerHeight() * treeHeight;


        float xMin = rootCoordinates.getX() - (float) treeWidth / 2;
        float xMax = rootCoordinates.getX() + (float) treeWidth / 2;

        float zMin = rootCoordinates.getZ() - (float) treeWidth / 2;
        float zMax = rootCoordinates.getZ() + (float) treeWidth / 2;

        float yMin = rootCoordinates.getY() + (float) (treeHeight - crownHeight);
        float yMax = rootCoordinates.getY() + (float) treeHeight;


        //quader random gleichverteilt füllen
        for (int i = 1; i <= (int) (type.getAttPointsPerHeight() * treeHeight); i++) {
            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z = random.nextFloat() * (zMax - zMin) + zMin;

            cloud.add(new Point3D(x, y, z));
        }

        return cloud;
    }

    private List<Point3D> buildTreeShape(Tree tree, List<Point3D> cuboidCloud) {

        List<Point3D> cloud = new ArrayList<>();

        Point3D rootCoordinates = tree.getNodes().getRoot().getPoint();
        TreeType type = tree.getType();
        double treeHeight = tree.getHeight();
        double crownHeight = treeHeight * type.getTopPercentage() / 100;
        double treeRadius = type.getWidthPerHeight() * treeHeight / 2;
        double treeTopY = rootCoordinates.getY() + treeHeight;


        for (Point3D point3D : cuboidCloud) {
            float xForMaxDistance;
            float xForMinDistance = rootCoordinates.getX();
            double fs; //function start, where sin should go positive on y axis
            double thickness;//TODO abwarten ob das tatsächlich in mehreren fällen gebraucht wird

            switch (type.getTreeShape()) {
                case UMBRELLA:
                    thickness = 0.1;
                    fs = rootCoordinates.getY() + treeHeight - 2 * type.getTopPercentage() / 100 * treeHeight;
                    xForMaxDistance = (float) ((treeRadius) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());
                    xForMinDistance = (float) ((treeRadius) * (1.0 - 2 * thickness) * Math.sin(((Math.PI) / ((treeTopY - fs) * (1.0 - 2 * thickness))) * (point3D.getY() - (fs + (treeTopY - fs) * thickness))) + rootCoordinates.getX());
                    break;
                case CONE:
                    // f(x) = (float) ( (crownHeight / (treeWidth/2)) * (point3D.getX() - rootCoordinates.getX()) + treeHeight); //minus verschiebt nach rechts
                    xForMaxDistance = (float) ((point3D.getY() - treeTopY) * treeRadius / (crownHeight) + rootCoordinates.getX()); //f(y)
                    break;
                default:
                    //round
                    fs = rootCoordinates.getY();
                    xForMaxDistance = (float) ((treeRadius) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());
            }


            double maxDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMaxDistance, point3D.getY());
            double realDistance = point3D.distance(new Point3D(rootCoordinates.getX(), point3D.getY(), rootCoordinates.getZ()));

            switch (type.getTreeShape()) {
                case UMBRELLA:
                    //rahmen
                    double minDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMinDistance, point3D.getY());
                    if ((realDistance <= maxDistance && realDistance >= minDistance)) {
                        cloud.add(point3D);
                    }
                    break;
                default:
                    //normaler schnitt
                    if (realDistance <= maxDistance) {
                        cloud.add(point3D);
                    }
            }
        }
        return cloud;
    }

    private List<Point3D> intersectWithObstacles(List<Obstacle> obstacles, List<Point3D> cloud){

        List<Point3D> cloud2 = new ArrayList<>();

        for (Point3D point : cloud) {
            boolean yeah = true;
            for (Obstacle obstacle : obstacles) {
                if(obstacle.isInside(point))
                    yeah = false;
            }
            if(yeah)
                cloud2.add(point);
        }



        return cloud2;
    }
}


