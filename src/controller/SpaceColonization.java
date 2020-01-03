package controller;

import model.*;
import org.apache.commons.math.ArgumentOutsideDomainException;
import org.apache.commons.math.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math.analysis.polynomials.PolynomialSplineFunction;
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
    private final boolean SHIFT = false;
    private final boolean LIGHT = false;
    private final boolean SHADOW = false;
    private final float FACTOR = 0.5f;

    private boolean bah;


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

            //bias
            Point3D bias = new Point3D(0, 0.0f, 0);
            apVector.addTo(bias);
            apVector.normalize();

            Point3D obstVector;
            Point3D finalVector;
            if (LIGHT || SHADOW) {
                obstVector = calculateShadowDetractionVector(node, obstacles, sunPositions);
                obstVector.multTo(FACTOR);
                finalVector = apVector.add(obstVector);
            } else
                finalVector = apVector;
            finalVector.addTo(new Point3D(0, -0.2f, 0)); //TODO bias
            finalVector.normalize();

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
            shiftVector.setY(shiftVector.getY() * FACTOR * (float) tree.calculateBoundsPercentDings(obstacles) / 100.0f); //TODO iwie sonnenintensität einbsuen
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
                lim = (int) (0.7 * type.getAttPointsPerHeight() * treeHeight); //prozent für rahmen
                break;
//            case UMBRELLA:
            default:
                lim = (int) (type.getAttPointsPerHeight() * treeHeight);
                break;
        }
        //quader random gleichverteilt füllen
        //für umbrella erst rahmen
        for (int i = 1; i <= lim; i++) {

            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z;
            if (Application.TWO_D)
                z = 0;
            else
                z = random.nextFloat() * (zMax - zMin) + zMin;

            Point3D point3D = new Point3D(x, y, z);
            if (buildTreeShape2(tree, point3D, true))
                cloud.add(point3D);
            else i--;
        }

        //für umbrella dann inneres

        bah = true;
        for (int i = lim; i <= (int) (type.getAttPointsPerHeight() * treeHeight); i++) {
            float x = random.nextFloat() * (xMax - xMin) + xMin;
            float y = random.nextFloat() * (yMax - yMin) + yMin;
            float z;
            if (Application.TWO_D)
                z = 0;
            else
                z = random.nextFloat() * (zMax - zMin) + zMin;

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
        double thickness = 0;//TODO abwarten ob das tatsächlich in mehreren fällen gebraucht wird

//        if (!indicator) {
//            topPercentage = 95;
//        }
        Point3D[] points;

        //je nach typ die punkte der form festlegen
        switch (type.getTreeShape()) {
            case UMBRELLA2:
                thickness = 0.15;
                points = new Point3D[]{
                        new Point3D((float) (treeRadius), (float) (treeHeight - crownHeight), 0),
                        new Point3D((float) (treeRadius - 0.25 * treeRadius), (float) (treeHeight - 0.4 * crownHeight), 0),
                        new Point3D(0, (float) treeHeight, 0)};
                break;
            case UMBRELLA:
                thickness = 0.1;
                points = new Point3D[]{
                        new Point3D((float) (treeRadius), (float) (treeHeight - crownHeight), 0),
                        new Point3D((float) (treeRadius - 0.25 * treeRadius), (float) (treeHeight - 0.4 * crownHeight), 0),
                        new Point3D(0, (float) treeHeight, 0)};
                break;
            case CONE:

                points = new Point3D[]{
                        new Point3D((float) treeRadius, (float) (treeHeight - crownHeight), 0),
                        new Point3D((float) (treeRadius * 0.5), (float) (treeHeight - 0.5 * crownHeight), 0),
                        new Point3D(0, (float) treeHeight, 0)};

                break;

            default:
                //round
//                points = new Point3D[]{
//                        new Point3D((float) (treeRadius), (float) (treeHeight - topHeight), 0),
//                        new Point3D((float) (treeRadius - 0.25 * treeRadius), (float) (treeHeight - 0.4 * topHeight), 0),
//                        new Point3D(0, (float) treeHeight, 0)};
                points = new Point3D[]{
                        new Point3D(0, (float) (treeHeight - crownHeight), 0),
                        new Point3D((float) (treeRadius - 0.25 * treeRadius), (float) (treeHeight - 0.8 * crownHeight), 0),
                        new Point3D((float) treeRadius, (float) (treeHeight - 0.5 * crownHeight), 0),
                        new Point3D((float) (treeRadius - 0.25 * treeRadius), (float) (treeHeight - 0.2 * crownHeight), 0),
                        new Point3D(0, (float) treeHeight, 0)};
        }

        {
            double[] x = new double[points.length];
            double[] y = new double[points.length];
            for (int i = 0; i < points.length; i++) {
                x[i] = points[i].getX();
                y[i] = points[i].getY();
            }
            SplineInterpolator interpolator = new SplineInterpolator();
            //x und y getauscht weil ich f(y)=x will
            PolynomialSplineFunction splineFunction = interpolator.interpolate(y, x);
            if (bah) {
                Application.putSpline(splineFunction, points);
                bah = false;
            }
            try {
                xForMaxDistance = (float) splineFunction.value(point3D.getY());
            } catch (ArgumentOutsideDomainException e) {
                xForMaxDistance = 0;
                e.printStackTrace();
            }
        }

        double minDistanceMaxY;
        double minDistanceMinY;

        switch (type.getTreeShape()) {
            case UMBRELLA2:
            case UMBRELLA:
                //alle punkte um thickness % in richtung mittelpunkt der krone achse
                Point3D crownMid = new Point3D(rootCoordinates.getX(), (float) (treeHeight - crownHeight * 0.5), rootCoordinates.getZ());
                for (Point3D point : points) {
                    Point3D midVector = crownMid.subtract(point);
                    float length = midVector.vectorLength();
                    midVector.normalize();
                    midVector.multTo((float) thickness * length);
                    point.addTo(midVector);
                }
                points[0].setY((float) (treeHeight - crownHeight));
                minDistanceMaxY = points[points.length-1].getY();
                minDistanceMinY = points[0].getY();

                //mindistance nehmen
            {
                double[] x = new double[points.length];
                double[] y = new double[points.length];
                for (int i = 0; i < points.length; i++) {
                    x[i] = points[i].getX();
                    y[i] = points[i].getY();
                }
                //wenn über oder unter innerer funktion dann ist eh nicht alos minDistance = 0;
                if (point3D.getY() > minDistanceMaxY || point3D.getY() < minDistanceMinY)
                    xForMinDistance = 0;
                else {
                    SplineInterpolator interpolator = new SplineInterpolator();
                    //x und y getauscht weil ich f(y)=x will
                    PolynomialSplineFunction splineFunction = interpolator.interpolate(y, x);
                    if (bah) {
                        Application.putSpline(splineFunction, points);
                        bah = false;
                    }
                    try {
                        xForMinDistance = (float) splineFunction.value(point3D.getY());
                    } catch (ArgumentOutsideDomainException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }

        double maxDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMaxDistance, point3D.getY());
        double realDistance = point3D.distance(new Point3D(rootCoordinates.getX(), point3D.getY(), rootCoordinates.getZ()));
        double minDistance = distance(rootCoordinates.getX(), point3D.getY(), xForMinDistance, point3D.getY());

            //rahmen
            if ((realDistance <= maxDistance && realDistance >= minDistance)) {
                return true;
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


