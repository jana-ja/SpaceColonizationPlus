package controller;

import model.*;
import view.Point3D;
import view.ViewInterface;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.geom.Point2D.distance;


class SpaceColonization {

    private final int beginDay = 60;
    private final int endDay = 300;
    private int currentDay = beginDay;
    private boolean marker;
    private final boolean SUN = false;
    private final int SUN_DELAY = 30;
    private final boolean SHIFT = true;
    private final boolean LIGHT = true;
    private final boolean SHADOW = true;
    private final float FACTOR = 0.5f;


    public void stats() {
        Application.stats("days", beginDay + "-" + endDay);
        if (SHIFT)
            Application.stats("shift", "+");
        else
            Application.stats("shift", "-");
        if (LIGHT)
            Application.stats("light", "+");
        else
            Application.stats("light", "-");
        if (SHADOW)
            Application.stats("shadow", "+");
        else
            Application.stats("shadow", "-");
        Application.stats("factor", String.valueOf(FACTOR));
    }

    /**
     * Performs one step of space colonization.
     * Adds nodes to the tree according to influence of attraction points from the point cloud.
     *
     * @param tree
     * @param pointCloud
     */
    boolean spaceColonize(Tree tree, PointCloud pointCloud, List<Obstacle> obstacles) {


        marker = true;
        if (pointCloud.isEmpty())
            return false;

        Point3D lastAvgNode;
        if (SHIFT)
            lastAvgNode = tree.getLastAvgNode();

        Map<KDParentTreeNode, List<Point3D>> attractionMap = new HashMap<>();

        //first step: map nodes to their influencing attraction points

        pointCloud.getAttractionPoints().forEach(attractionPoint -> {
            if (attractionPoint.isActivated()) {
                KDParentTreeNode node = tree.getNodes().nearestInRange(attractionPoint, tree.getType().getRadOfInf());

                if (node != null) {
                    if (!attractionMap.containsKey(node))
                        attractionMap.put(node, new ArrayList<>());

                    attractionMap.get(node).add(attractionPoint);
                }
            }
        });

        //if attractionMap is empty -> space colonization is finished
        if (attractionMap.isEmpty()) {
            System.out.println("abbruch");
            return false;
        }

        ViewInterface.log("      mapped nodes: " + attractionMap.size());

        //second step: calculate new node for every node in map

//        currentDay = 100;
        List<SunPosition> sunPositions = SunCalculator.positionsForDay(currentDay, 1.0);
        ViewInterface.log("\t day: " + currentDay + ", " + sunPositions.size() + "  hours");
        if (currentDay == endDay)
            currentDay = beginDay;
        else
            currentDay++;


        attractionMap.forEach((node, attractionPoints) -> {
            //attraction vector
            Point3D apVector = calculateInfluenceVector(node, attractionPoints);

            Point3D obstVector;
            Point3D finalVector;
            if (LIGHT || SHADOW) {
                obstVector = calculateShadowDetractionVector(node, obstacles, sunPositions);
                obstVector.multTo(FACTOR);
                finalVector = apVector.add(obstVector);
            } else
                finalVector = apVector;

            //norm final vector
            finalVector.normalize();

            Point3D newPoint = finalVector.mult(tree.getType().getNodeDist());

            //vector zu node point addieren
            newPoint.addTo(node.getPoint());
            //testen ob newNode.point = point von nem kind von node
            boolean isNew = true;
            if (attractionPoints.size() == 2) {
                for (KDParentTreeNode child : node.getTreeChildren()) {

                    if (child.getPoint().distance(newPoint) < 0.001) {
                        isNew = false;
                        pointCloud.getAttractionPoints().removeAll(attractionPoints);
                        ViewInterface.log("   unlimited growing problem prevented"); //TODO schwierig mit wandernder sonne. so funktioniert das dann nicht
                        break;
                    }
                }
            }

            if (isNew) {
                AtomicBoolean legitim = new AtomicBoolean(true);
                obstacles.forEach(obstacle -> {
                    if (obstacle.isInside(newPoint)) legitim.set(false);
                });
                if (legitim.get())
                    tree.getNodes().insert(newPoint, node);
                else
                    System.err.println("NE REIN GEWACHSEN"); //TODO unlimitetd nicht-growing
            }
        });


        //third step: remove attraction points that have a node in kill radius distance or less
        //TODO debug anders machen? removeIf sache überprüfen.
        tree.getNodes().getAll().forEach(node ->
                pointCloud.getAttractionPoints().removeIf(attractionPoint ->
                        attractionPoint.distance(node.getPoint()) < tree.getType().getKillRad()
                ));
//        pointCloud.getAttractionPoints().removeIf(attractionPoint ->
//                tree.getNodes().hasInRange(attractionPoint, tree.getType().getKillRad()));

        if (SHIFT) {
            Point3D avgNode = tree.calculateAvgNode();
            Point3D shiftVector = avgNode.subtract(lastAvgNode);
            shiftVector.setY(0);
            pointCloud.shift(shiftVector);

            pointCloud.updateWithObstacles(obstacles);
        }

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

    private Point3D calculateShadowDetractionVector(KDParentTreeNode node, List<Obstacle> obstacles, List<SunPosition> sunPositions) {
        Point3D shadowVector = new Point3D(0, 0, 0);
        sunPositions.forEach(sunPos -> {
            if (marker && SUN) {

                Application.visualizeSun(sunPos);
                try {
                    Thread.sleep(SUN_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            AtomicBoolean light = new AtomicBoolean(true);
            obstacles.forEach(obstacle -> {
                double shadowFactor = Math.toDegrees(sunPos.getElevationRadians()) / 100; //TODO guten faktor finden, iwas was intensität wiederspiegelt

                if (obstacle.isInShadow(node.getPoint(), sunPos)) {
                    if (SHADOW)
                        shadowVector.addTo(obstacle.getVectorFromShadow(node.getPoint(), sunPos).mult(shadowFactor));
                    light.set(false);
                }
            });
            if (light.get()) {
                //ist im licht
                if (LIGHT) {
                    double lightFactor = Math.toDegrees(sunPos.getElevationRadians()) / 100; //TODO faktor finden, vllt der lichtintensität weiderspiegelt (elevation winkel oder so, der ist höchstens 61.94)
                    shadowVector.addTo(sunPos.calculateRayVector().mult(lightFactor));
                }
            }

        });
//        System.out.println("\t new day");
        marker = false;
        if (shadowVector.vectorLength() != 0)
            shadowVector.normalize();
        return shadowVector;
    }

    /**
     * Returns a pointcloud fitting the tree type and height.
     *
     * @return
     */
    PointCloud generatePointCloud(Tree tree/*, List<Obstacle> obstacles*/) {
        //würfel um volumen bauen
        List<Point3D> cloud = fillPointCloudCuboid(tree);

        //schnitt(würfel,volumen) behalten -> für jeden punkt: ist in volumen?
//        cloud = buildTreeShape(tree, cloud);
        //jetzt in filldingens drin

        //schnitt cloud obstacles
//        cloud = intersectWithObstacles(obstacles, cloud);
        //jetzt separat auszuführen damit die cloud gespeichert werden kann
        //wird in pointcloud verschoben

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

        int lim;
        switch (tree.getType().getTreeShape()) {
            case UMBRELLA2:
            case UMBRELLA:
                lim = (int) (0.55 * type.getAttPointsPerHeight() * treeHeight); //prozent für rahmen
                break;
            default:
                lim = (int) (type.getAttPointsPerHeight() * treeHeight);
                break;
        }
        //quader random gleichverteilt füllen
        //für umbrella erst rahmen
        for (int i = 1; i <= lim; i++) {
            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z = random.nextFloat() * (zMax - zMin) + zMin;

            Point3D point3D = new Point3D(x, y, z);
            if (buildTreeShape2(tree, point3D, true))
                cloud.add(point3D);
            else i--;
        }

        //für umbrella dann inneres
        crownHeight = treeHeight * 95 / 100;
        yMin = rootCoordinates.getY() + (float) (treeHeight - crownHeight);

        for (int i = lim; i <= (int) (type.getAttPointsPerHeight() * treeHeight); i++) {
            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z = random.nextFloat() * (zMax - zMin) + zMin;

            Point3D point3D = new Point3D(x, y, z);
            if (buildTreeShape2(tree, point3D, false))
                cloud.add(point3D);
            else i--;
        }


        return cloud;
    }

    private boolean buildTreeShape2(Tree tree, Point3D point3D, boolean indicator) {

        Point3D rootCoordinates = tree.getNodes().getRoot().getPoint();
        TreeType type = tree.getType();
        double treeHeight = tree.getHeight();
        double crownHeight = treeHeight * type.getTopPercentage() / 100;
        double treeRadius = type.getWidthPerHeight() * treeHeight / 2;
        double treeTopY = rootCoordinates.getY() + treeHeight;
        double topPercentage = type.getTopPercentage();



        float xForMaxDistance;
        float xForMinDistance = rootCoordinates.getX();
        double fs; //function start, where sin should go positive on y axis
        double thickness;//TODO abwarten ob das tatsächlich in mehreren fällen gebraucht wird

//        if (!indicator) {
//            topPercentage = 95;
//        }
        switch (type.getTreeShape()) {
            case UMBRELLA2:
                thickness = 0.15;
                fs = rootCoordinates.getY() + treeHeight - 2 * topPercentage / 100 * treeHeight;
                xForMaxDistance = (float) ((treeRadius) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());
                xForMinDistance = (float) ((treeRadius) * (1.0 - 2 * thickness) * Math.sin(((Math.PI) / ((treeTopY - fs) * (1.0 - 2 * thickness))) * (point3D.getY() - (fs + (treeTopY - fs) * thickness))) + rootCoordinates.getX());
                break;
            case UMBRELLA:
                thickness = 0.1;
                fs = rootCoordinates.getY() + treeHeight - 2 * topPercentage / 100 * treeHeight;
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
            case UMBRELLA2:
            case UMBRELLA:
                double minDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMinDistance, point3D.getY());
                if (indicator) {
                    //rahmen
                    if(xForMinDistance < 0) //TODO shitty lösung für problem oben sin zu viel punkte ne
                        break;
                    if ((realDistance <= maxDistance && realDistance >= minDistance)) {
                        return true;
                    }
                    break;
                }
                else {
                    if(xForMinDistance < 0) //TODO shitty lösung für problem oben sin zu viel punkte ne
                        break;
                    if (realDistance <= minDistance) {
                        return true;
                    }
                }
            default:
                //normaler schnitt
                if (realDistance <= maxDistance) {
                    return true;
                }
        }
        return false;
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
                case UMBRELLA2:
                    thickness = 0.15;
                    fs = rootCoordinates.getY() + treeHeight - 2 * type.getTopPercentage() / 100 * treeHeight;
                    xForMaxDistance = (float) ((treeRadius) * Math.sin(Math.PI / (treeTopY - fs) * (point3D.getY() - fs)) + rootCoordinates.getX());
                    xForMinDistance = (float) ((treeRadius) * (1.0 - 2 * thickness) * Math.sin(((Math.PI) / ((treeTopY - fs) * (1.0 - 2 * thickness))) * (point3D.getY() - (fs + (treeTopY - fs) * thickness))) + rootCoordinates.getX());
                    break;
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


