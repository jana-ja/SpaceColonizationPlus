package model;

import org.jogamp.java3d.BoundingBox;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import view.Point3D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Tree {

    public static final int X_DIVISION = 15;

    private final TreeType type;
    private final double height;

    private Point3D lastAvgNode;

    private int count;

    private final KDParentTree nodes;

    public Tree(TreeType type, double height, Point3D root) {
        this.type = type;
        this.height = height;
        this.lastAvgNode = root;

        this.nodes = new KDParentTree(new KDParentTreeNode(root, new double[]{Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE}, null));

    }

    public Point3D getLastAvgNode() {
        return lastAvgNode;
    }

    public Point3D calculateAngle() {
        count = 0;
        Point3D vec = calculateAngleRek(nodes.getRoot());
        vec.divideFrom(count);
        vec.normalize();
        return vec;
    }

    private Point3D calculateAngleRek(KDParentTreeNode node) {
        count++;

        if (node.getTreeChildren().isEmpty())
            return node.parentAngle();
        Point3D childAngle = new Point3D(0, 0, 0);
        node.getTreeChildren().forEach(child -> childAngle.addTo(calculateAngleRek(child)));
        return node.parentAngle().add(childAngle);

    }


    public KDParentTree getNodes() {
        return nodes;
    }

    public TreeType getType() {
        return type;
    }

    public double getHeight() {
        return height;
    }

    public Point3D calculateAvgNode() {
        Point3D avgNode = new Point3D(0, 0, 0);
        this.nodes.getAll().forEach(node -> avgNode.addTo(node.getPoint()));
        avgNode.divideFrom(nodes.getAll().size());
        this.lastAvgNode = avgNode;
        return avgNode;
        //mit gewichtung (thickness?)
    }

    public Point3D calculateSchwerpunkt() {
        Point3D schwerpunkt = new Point3D(0, 0, 0);
        final float[] thickness = {0};
        this.nodes.getAll().forEach(node -> {
            schwerpunkt.addTo(node.getPoint().mult(node.getThickness()));
            thickness[0] += node.getThickness();
        });
        schwerpunkt.divideFrom(nodes.getAll().size());
//        schwerpunkt.divideFrom(thickness[0]);
        return schwerpunkt;
        //mit gewichtung (thickness?)
    }

    public int calculateBranches() {
        return calculateBranchesRek(this.getNodes().getRoot());
    }

    private int calculateBranchesRek(KDParentTreeNode node) {
        int size = node.getTreeChildren().size();
        if (size == 0)
            return 0;

        int sum = node.getTreeChildren().stream().mapToInt(this::calculateBranchesRek).sum();

        sum += (size > 1) ? size - 1 : 0;
//        if (size > 1)
//            sum += size - 1;

        return sum;
    }

    public void calculateDiscs() {
        calculateDiscsRek(nodes.getRoot());
    }

    private void calculateDiscsRek(KDParentTreeNode node) {

        int N = X_DIVISION + 1; // +1 fü dopplung - kreis schließen
        double radius = node.getThickness();
        Point3f[] points = new Point3f[N + 1]; // +1 für midde
        points[0] = new Point3f(0, 0, 0); //midde
        for (int i = 1; i < points.length; i++) {
            double alpha = 2 * Math.PI / (N - 1) * i; //N-i damit faces nach oben
            float x = (float) (radius * Math.cos(alpha));
            float z = (float) (radius * Math.sin(alpha));
            points[i] = new Point3f(x, 0, z);
        }
        Point3f[] pointsTop = points;
        Point3f[] pointsBot = points;

        if (!(node.getTreeChildren().isEmpty() || node.getTreeParent() == null)) {
            //wenn parent und kind(er) hat dann transformieren
            // parent ist bot, node ist top - node ist bot, children sind bot
            Point3D vectParent = node.getTreeParent().getPoint().subtract(node.getPoint());
            Point3D durchschnittChildren = new Point3D(0, 0, 0);
            node.getTreeChildren().forEach(child -> durchschnittChildren.addTo(child.getPoint()));
            Point3D vectChildren = durchschnittChildren.subtract(node.getPoint());

            //drehachse
            Point3D drehachse = vectParent.cross(vectChildren);
//            Application.view.addLine(node.getPoint().add(drehachse), node.getPoint().subtract(drehachse), Color.black);

            //top
            //winkel zwischen den vektoren /2
            double angleTop = -(Math.toRadians(90) - Math.acos(vectParent.dot(vectChildren) / (vectParent.vectorLength() * vectChildren.vectorLength())) / 2);
            pointsTop = copy(points);
            TruncatedCone.transform(new Point3f(0, 0, 0), pointsTop, drehachse, angleTop);

            //bot
            //winkel zwischen den vektoren /2
            double angleBot = Math.toRadians(90) - Math.acos(vectParent.dot(vectChildren) / (vectParent.vectorLength() * vectChildren.vectorLength())) / 2;
            pointsBot = copy(points);
            TruncatedCone.transform(new Point3f(0, 0, 0), pointsBot, drehachse, angleBot);


        }
        node.setPointsTop(pointsTop);
        node.setPointsBot(pointsBot);
        node.getTreeChildren().forEach(this::calculateDiscsRek);
    }

    private Point3f[] copy(Point3f[] points) {
        Point3f[] copy = new Point3f[points.length];
        for (int i = 0; i < points.length; i++) {
            copy[i] = new Point3f(points[i].getX(), points[i].getY(), points[i].getZ());
        }
        return copy;
    }

    public double calculateBoundsPercentDings(List<Obstacle> obstacles) {
        //wie viel prozent sind überhaupt am tag iwann in der sonne
        BoundingBox bounds = calculateBounds();
        Point3d lower = new Point3d();
        bounds.getLower(lower);
        Point3d upper = new Point3d();
        bounds.getUpper(upper);
        double samplingRate = 0.1;
        List<Point3d> points = new ArrayList<>();
        //vorne
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new Point3d(j, i, lower.getZ()));
            }
        }
        //links
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getZ(); j <= upper.getZ(); j += samplingRate) {
                points.add(new Point3d(lower.getX(), i, j));
            }
        }
        //hinten
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new Point3d(j, i, upper.getZ()));
            }
        }
        //rechts
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getZ(); j <= upper.getZ(); j += samplingRate) {
                points.add(new Point3d(upper.getX(), i, j));
            }
        }
        //upper
        for (double i = lower.getZ(); i <= upper.getZ(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new Point3d(j, upper.getY(), i));
            }
        }

        List<SunPosition> sunPositions = SunCalculator.positionsForDay(150, 1.0); //TODO

//        AtomicInteger count = new AtomicInteger();
//        AtomicBoolean stop = new AtomicBoolean(false);
////        points.forEach(point -> {
////            if(obstacles.stream().anyMatch(obst -> sunPositions.stream().anyMatch(pos -> (!obst.isInShadowPlus(new Point3D((float) point.getX(), (float) point.getY(), (float) point.getZ()), pos)))))
////                count.getAndIncrement();
////        });
//        System.out.println("count :" + count + ". all: " + points.size());
//        double result = count.get() * 100.0 / points.size();
//        System.out.println(result);

        AtomicReference<Double> avg = new AtomicReference<>(0.0);
        AtomicInteger tmp = new AtomicInteger();
        AtomicBoolean inSun = new AtomicBoolean();
        AtomicReference<Double> gesamt = new AtomicReference<>(0.0);
        sunPositions.forEach(sunPos -> {
            points.forEach(point -> {
                inSun.set(true);
                obstacles.forEach(obst -> {
                    if (obst.isInShadow(new Point3D((float) point.getX(), (float) point.getY(), (float) point.getZ()), sunPos))
                        inSun.set(false);
                });
                if (inSun.get())
                    tmp.getAndIncrement();
            });
            avg.set(tmp.doubleValue() / this.getNodes().getAll().size() * 100);
//            System.out.println(avg.get() + "%");
            gesamt.updateAndGet(v -> new Double((double) (v + avg.get())));
            tmp.set(0);

        });
        gesamt.set(gesamt.get() / sunPositions.size());
        System.out.println(gesamt + "% gesamt");
        return gesamt.get();
//        return result;
    }

    public double nodesInLight(List<Obstacle> obstacles) {
        List<SunPosition> sunPositions = SunCalculator.positionsForDay(160, 1.0); //TODO welcher tag?

        AtomicReference<Double> avg = new AtomicReference<>(0.0);
        AtomicInteger tmp = new AtomicInteger();
        AtomicBoolean inSun = new AtomicBoolean();
        AtomicReference<Double> gesamt = new AtomicReference<>(0.0);
        sunPositions.forEach(sunPos -> {
            this.getNodes().getAll().forEach(node -> {
                inSun.set(true);
                obstacles.forEach(obst -> {
                    if (obst.isInShadow(node.getPoint(), sunPos))
                        inSun.set(false);
                });
                if (inSun.get())
                    tmp.getAndIncrement();
            });
            avg.set(tmp.doubleValue() / this.getNodes().getAll().size() * 100);
            System.out.println(avg.get() + "%");
            gesamt.updateAndGet(v -> new Double((double) (v + avg.get())));
            tmp.set(0);

        });
        gesamt.set(gesamt.get() / sunPositions.size());
        System.out.println(gesamt + "% gesamt");
        return gesamt.get();
    }

    private BoundingBox calculateBounds() {
        Point3d upper = new Point3d(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        Point3d lower = new Point3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        this.getNodes().getAll().forEach(node -> {
            if (node.getPoint().getX() > upper.getX())
                upper.setX(node.getPoint().getX());
            if (node.getPoint().getY() > upper.getY())
                upper.setY(node.getPoint().getY());
            if (node.getPoint().getZ() > upper.getZ())
                upper.setZ(node.getPoint().getZ());
            if (node.getPoint().getX() < lower.getX())
                lower.setX(node.getPoint().getX());
            if (node.getPoint().getY() < lower.getY())
                lower.setY(node.getPoint().getY());
            if (node.getPoint().getZ() < lower.getZ())
                lower.setZ(node.getPoint().getZ());
        });

        return new BoundingBox(lower, upper);
    }

    public double getRealHeight(){
        List<Point3D> points = new ArrayList<>();
        this.getNodes().getAll().forEach(node -> points.add(node.getPoint()));
        Point3D p =  points.stream().max(Comparator.comparing(Point3D::getY)).orElseThrow(NoSuchElementException::new);
        return p.getY();
    }

}