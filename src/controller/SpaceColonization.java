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

        PointCloud pointCloud = new PointCloud();
        TreeType type = tree.getType();
        double treeHeight = tree.getHeight();
        double crownHeight = treeHeight * type.getTopPercentage() / 100;

        Point3D rootCoordinates = tree.getNodes().getRoot().getPoint();

        double treeWidth = type.getWidthPerHeight() * treeHeight;
        List<Point3D> envelope = new ArrayList<>();//generateEnvelope(type);
        List<Point3D> envelope2 = new ArrayList<>();//generateEnvelope(type);


        Random random = new Random();

        //würfel um volumen bauen
        //würfel gleichverteilt füllen
        //schnitt(würfel,volumen) behalten -> für jeden punkt: ist in volumen?

        //würfel bauen
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

            envelope.add(new Point3D(x, y, z));
        }

        //punkte checken mit funktion
        double maxDistance;
        double minDistance = Double.MIN_VALUE;
        float xForMaxDistance;
        float xForMinDistance = 0;
        double realDistance;
        double thickness = 0.1;
        double treeTopY = rootCoordinates.getY() + treeHeight;

        double fs = rootCoordinates.getY(); //function start, where sin should go positive on y axis
        for (Point3D point3D : envelope) {

            switch (type.getTreeShape()) {
                case UMBRELLA:
                    fs = rootCoordinates.getY() + treeHeight - 2 * type.getTopPercentage() / 100 * treeHeight;
                    xForMaxDistance = (float) ((treeWidth / 2) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());
                    xForMinDistance = (float) ((treeWidth / 2) * (1.0 - 2 * thickness) * Math.sin(((Math.PI) / ((treeTopY - fs) * (1.0 - 2 * thickness))) * (point3D.getY() - (fs + (treeTopY - fs) * thickness)))+ rootCoordinates.getX());

                    break;

                case CONE:
                    // f(x) = (float) ( (crownHeight / (treeWidth/2)) * (point3D.getX() - rootCoordinates.getX()) + treeHeight); //minus verschiebt nach rechts
                    xForMaxDistance = (float) ((point3D.getY() - treeTopY) * treeWidth / 2 / (crownHeight) + rootCoordinates.getX()); //f(y)
                    break;
                default:
                    xForMaxDistance = (float) ((treeWidth / 2) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());


            }


            maxDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMaxDistance, point3D.getY());
            realDistance = point3D.distance(new Point3D(rootCoordinates.getX(), point3D.getY(), rootCoordinates.getZ()));


            switch (type.getTreeShape()) {
                case UMBRELLA: //Kontur
                    minDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMinDistance, point3D.getY());
                    if ((realDistance <= maxDistance && realDistance >= minDistance)) {
                        envelope2.add(point3D); //envelope2 wg concurrent modification
                    }
                    break;

                default:
                    if (realDistance <= maxDistance) {
                        envelope2.add(point3D); //envelope2 wg concurrent modification
                    }

            }


        }


        pointCloud.setAttractionPoints(envelope2);

        return pointCloud;
    }
}


