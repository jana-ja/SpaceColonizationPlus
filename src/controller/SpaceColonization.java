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
        if (attractionMap.isEmpty()) {
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
            infVec.addTo(vecNorm); //TODO normieren oder nicht?
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
     * @return
     */
    PointCloud generatePointCloud(Tree tree) {
        //würfel um volumen bauen
        List<Point3D> cubeCloud = fillPointCloudCube(tree);

        //schnitt(würfel,volumen) behalten -> für jeden punkt: ist in volumen?
        List<Point3D> cloud = intersectCubeCloud(tree, cubeCloud);

        return new PointCloud(cloud);
    }

    private List<Point3D> fillPointCloudCube(Tree tree) {
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


        //würfel random gleichverteilt füllen
        for (int i = 1; i <= (int) (type.getAttPointsPerHeight() * treeHeight); i++) {
            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z = random.nextFloat() * (zMax - zMin) + zMin;

            cloud.add(new Point3D(x, y, z));
        }

        return cloud;
    }

    private List<Point3D> intersectCubeCloud(Tree tree, List<Point3D> cubeCloud) {

        List<Point3D> cloud = new ArrayList<>();

        Point3D rootCoordinates = tree.getNodes().getRoot().getPoint();
        TreeType type = tree.getType();
        double treeHeight = tree.getHeight();
        double crownHeight = treeHeight * type.getTopPercentage() / 100;
        double treeRadius = type.getWidthPerHeight() * treeHeight / 2;
        double treeTopY = rootCoordinates.getY() + treeHeight;


        for (Point3D point3D : cubeCloud) {
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
}


