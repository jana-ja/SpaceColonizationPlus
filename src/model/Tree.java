package model;

import org.jogamp.java3d.BoundingBox;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import quickhull3d.QuickHull3D;
import view.Point3D;
import view.View;
import view.ViewInterface;

import java.awt.*;
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

    public static int zahl = 0;


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
            // parent ist bot, node ist top - node ist bot, children sind top
            Point3D vectParent = node.getTreeParent().getPoint().subtract(node.getPoint());
            vectParent.normalize();
            Point3D durchschnittChildren = new Point3D(0, 0, 0);
            node.getTreeChildren().forEach(child -> durchschnittChildren.addTo(child.getPoint()));
            Point3D vectChildren = durchschnittChildren.subtract(node.getPoint());
            vectChildren.normalize();

            //drehachse
            Point3D drehachse = vectParent.cross(vectChildren);
//            Application.view.addLine(node.getPoint().add(drehachse), node.getPoint().subtract(drehachse), Color.black);

            //top
            //winkel zwischen den vektoren /2
            double angleTop = -(Math.toRadians(90) - Math.acos(vectParent.dot(vectChildren) / (vectParent.vectorLength() * vectChildren.vectorLength())) / 2);
            if(Double.isNaN(angleTop))
                angleTop = 0;
            pointsTop = copy(points);
            TruncatedCone.transform(new Point3f(0, 0, 0), pointsTop, drehachse, angleTop);

            //bot
            //winkel zwischen den vektoren /2
            double angleBot = Math.toRadians(90) - Math.acos(vectParent.dot(vectChildren) / (vectParent.vectorLength() * vectChildren.vectorLength())) / 2;
            if(Double.isNaN(angleBot))
                angleBot = 0;
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

    public double calculateBoundsPercentDings(List<Obstacle> obstacles, ViewInterface view) {
        //TODO guck ich ob auf schattenseite von sich selbst liegt?
        //wie viel prozent sind durchschnittlich übern tag in der sonne
        BoundingBox bounds = calculateBounds();
        org.jogamp.vecmath.Point3d lower = new org.jogamp.vecmath.Point3d();
        bounds.getLower(lower);
        org.jogamp.vecmath.Point3d upper = new org.jogamp.vecmath.Point3d();
        bounds.getUpper(upper);
        double samplingRate = 0.1;
        List<org.jogamp.vecmath.Point3d> points = new ArrayList<>();
        //vorne
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new org.jogamp.vecmath.Point3d(j, i, lower.getZ()));
            }
        }
        //links
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getZ(); j <= upper.getZ(); j += samplingRate) {
                points.add(new org.jogamp.vecmath.Point3d(lower.getX(), i, j));
            }
        }
        //hinten
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new org.jogamp.vecmath.Point3d(j, i, upper.getZ()));
            }
        }
        //rechts
        for (double i = lower.getY(); i <= upper.getY(); i += samplingRate) {
            for (double j = lower.getZ(); j <= upper.getZ(); j += samplingRate) {
                points.add(new org.jogamp.vecmath.Point3d(upper.getX(), i, j));
            }
        }
        //upper
        for (double i = lower.getZ(); i <= upper.getZ(); i += samplingRate) {
            for (double j = lower.getX(); j <= upper.getX(); j += samplingRate) {
                points.add(new org.jogamp.vecmath.Point3d(j, upper.getY(), i));
            }
        }
        List<SunPosition> sunPositions = SunCalculator.positionsForDay(150, 1.0); //TODO
//        List<SunPosition> sunPositions = new ArrayList<>();// SunCalculator.positionsForDay(150, 1.0); //TODO
//        sunPositions.add(new SunPosition(Math.toRadians(180), Math.toRadians(50)));
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
                //testen ob auf schattenseite
                //gerade ray von punkt aus und schnitttest
                Vector3d rayMinus = sunPos.calculateRayVector3d();
                rayMinus.normalize();
//                rayMinus.scale(-1);
                rayMinus.scale(0.01);
                org.jogamp.vecmath.Point3d testPoint = new org.jogamp.vecmath.Point3d(point);
                testPoint.add(rayMinus);
                rayMinus.scale(100);
                if(bounds.intersect(testPoint, rayMinus)){
                    inSun.set(false);
                }

                //testen ob im schatten von hindernis
                obstacles.forEach(obst -> {
                    if (obst.isInShadow(new Point3D((float) point.getX(), (float) point.getY(), (float) point.getZ()), sunPos))
                        inSun.set(false);
                });
                view.addLine(new Point3D(0,0,0), sunPos.calculateRayVector(),Color.CYAN);
                if (inSun.get()) //{
                    tmp.getAndIncrement();
//                    view.addMarker((float)point.getX(), (float)point.getY(),(float)point.getZ(), Color.BLUE, 0.01f);
//                }else{
//                    view.addMarker((float)point.getX(), (float)point.getY(),(float)point.getZ(), Color.RED, 0.01f);
//                }
            });
            avg.set(tmp.doubleValue() / points.size() * 100);
            System.out.println(sunPos.calculateRayVector().azimuthDegree());
            System.out.println(avg.get() + "%");
            gesamt.updateAndGet(v -> new Double((double) (v + avg.get())));
            tmp.set(0);

        });
        gesamt.set(gesamt.get() / sunPositions.size());
        System.out.println(gesamt + "% area gesamt");
        return gesamt.get();
//        return result;
    }

    public double nodesInLight(List<Obstacle> obstacles, List<TruncatedCone> branches) {
        System.out.println("NODES");
        List<SunPosition> sunPositions = SunCalculator.positionsForDay(150, 1.0); //TODO

//        List<SunPosition> sunPositions = new ArrayList<>();// SunCalculator.positionsForDay(160, 1.0); //TODO welcher tag?
//        sunPositions.add(new SunPosition(Math.toRadians(180), Math.toRadians(50)));

        AtomicReference<Double> avg = new AtomicReference<>(0.0);
        AtomicInteger tmp = new AtomicInteger();
        AtomicBoolean inSun = new AtomicBoolean();
        AtomicReference<Double> gesamt = new AtomicReference<>(0.0);
        sunPositions.forEach(sunPos -> {
            this.getNodes().getAll().forEach(node -> {
                //test eigener schatten
                //test mit boundingbox aller truncated cones, komm ich da überhaupt noch ran?
//                Vector3d ray = sunPos.calculateRayVector3d();
//                ray.normalize();
//                ray.scale(0.01);
//                Point3d testPoint = new Point3d(node.getPoint().getX(), node.getPoint().getY(), node.getPoint().getZ());
//                testPoint.add(ray);
//                ray.scale(100);
//                branches.forEach(branch -> {
//                    if(branch.getBounds().intersect(testPoint,ray)){
//                        inSun.set(false);
//                    }
//                });


                //test schatten obstacles
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
        System.out.println(gesamt + "% nodes gesamt");
        return gesamt.get();
    }

    private BoundingBox calculateBounds() {
        org.jogamp.vecmath.Point3d upper = new org.jogamp.vecmath.Point3d(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
        org.jogamp.vecmath.Point3d lower = new org.jogamp.vecmath.Point3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
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

    public QuickHull3D convexHull(){

        //TODO vllt stamm weg

        List<KDParentTreeNode> nodes = this.getNodes().getAll();
//        KDParentTreeNode akt = this.getNodes().getRoot();
//        while(akt.getTreeChildren().size()==1){
//            nodes.remove(akt);
//            akt = akt.getTreeChildren().get(0);
//        }
        quickhull3d.Point3d[] points = new quickhull3d.Point3d[nodes.size()];
        for (int i = 0; i < points.length; i++) {
            Point3D point = this.getNodes().getAll().get(i).getPoint();
            points[i] = new quickhull3d.Point3d(point.getX(), point.getY(), point.getZ());
        }
        return new QuickHull3D(points);

    }

    public double areaTest(List<Obstacle> obstacles, ViewInterface view){

        QuickHull3D hull = this.convexHull();// QuickHull3D(points);
        hull.triangulate();

        List<Shape3D> hullShapes = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        List<Point3d> points = new ArrayList<>();
        int[][] faces = hull.getFaces();
        quickhull3d.Point3d[] vertices = hull.getVertices();
        for (int i1 = 0; i1 < faces.length; i1++) {
            GeometryInfo giFace = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
            int[] stripcount = new int[]{faces[i1].length};
            giFace.setStripCounts(stripcount);
            org.jogamp.vecmath.Point3d[] diePoints = new org.jogamp.vecmath.Point3d[faces[i1].length];
            double bigX = 0.0, bigY =0.0 ,bigZ = 0.0;
            for (int i2 = 0; i2 < faces[i1].length; i2++) {

                quickhull3d.Point3d vertex = vertices[faces[i1][i2]];
                diePoints[i2] = new org.jogamp.vecmath.Point3d(vertex.x, vertex.y, vertex.z);
                bigX+=vertex.x;
                bigY+=vertex.y;
                bigZ+=vertex.z;
            }
            Triangle tri = new Triangle(diePoints);
            triangles.add(tri);
            //TODO die area punkte
            //punkte mehrmals hinzufügen abhängig von fläche des dreiecks
            if(tri.area()*100>1)
                System.out.println("area: " + tri.area()*100);

            for(int i = 0; i <= tri.area()*100; i++){
                points.add(new Point3d(tri.centroid()));

            }

            giFace.setCoordinates(diePoints);
            hullShapes.add(new Shape3D(giFace.getGeometryArray()));

//                for (int i2 = 0; i2 < faces[i1].length-1; i2++) {
//                    //hier sind die indizes aller vertizes drin die zum face i1 gehören
//                    quickhull3d.Point3d vertex1 = vertices[faces[i1][i2]];
//                    Point3D point1 = new Point3D((float)vertex1.x, (float)vertex1.y, (float)vertex1.z);
//                    quickhull3d.Point3d vertex2 = vertices[faces[i1][i2+1]];
//                    Point3D point2 = new Point3D((float)vertex2.x, (float)vertex2.y, (float)vertex2.z);
//                    view.addLine(point1,point2,Color.BLACK);
//                }
        }

        //TODO die area punkte zeigen
//        points.forEach(point -> {
//            view.addMarker((float)point.getX(), (float)point.getY(), (float)point.getZ(), Color.RED, 0.01f);
//        });

        //TODO bullshit
//        this.getNodes().getAll().forEach(node -> {
//            points.add(new Point3d(node.getPoint().getX(),node.getPoint().getY(), node.getPoint().getZ()));
//        });
        //jetzt liste von dreiecken shape3ds
        //für jeden mittelpunkt gucken: schnitt mit hindernissen, schnitt mit anderen dreiecken


        List<SunPosition> sunPositions = SunCalculator.positionsForDay(150, 1.0); //TODO
//        List<SunPosition> sunPositions = new ArrayList<>();
//        sunPositions.add(new SunPosition(Math.toRadians(180), Math.toRadians(50)));

        AtomicReference<Double> avg = new AtomicReference<>(0.0);
        AtomicInteger tmp = new AtomicInteger();
        AtomicBoolean inSun = new AtomicBoolean();
        AtomicReference<Double> gesamt = new AtomicReference<>(0.0);
        sunPositions.forEach(sunPos -> {
            points.forEach(point -> {
                inSun.set(true);

                //testen ob im schatten von hindernis
                obstacles.forEach(obst -> {
                    if (obst.isInShadow(new Point3D((float) point.getX(), (float) point.getY(), (float) point.getZ()), sunPos))
                        inSun.set(false);
                });

                if(inSun.get()) {
                    //testen ob auf schattenseite
                    //gerade ray von punkt aus und schnitttest
                    Vector3d rayMinus = sunPos.calculateRayVector3d();
                    rayMinus.normalize();
//                rayMinus.scale(-1);
                    rayMinus.scale(0.1);
                    Point3d testPoint = new Point3d(point.getX(),point.getY(),point.getZ());
                    testPoint.add(rayMinus);
                    rayMinus.scale(1.0/0.1);
                    //für alle triangles
//                    Point3D point3D = new Point3D((float)testPoint.getX(), (float)testPoint.getY(), (float)testPoint.getZ());
//                    Point3D vektori = point3D.add(new Point3D((float)rayMinus.getX(), (float)rayMinus.getY(), (float)rayMinus.getZ()));
//                    view.addLine(point3D, vektori, Color.ORANGE);
                    triangles.forEach(triangle -> {
                        if(triangle.intersect(testPoint, rayMinus)){
                            inSun.set(false);
                        }
                    });
                    //für alle shapes
//                    hullShapes.forEach(shape -> {//boudning box die scheiße ist, bracuhe glaube ich intersect und dafür scene graph path
//                        if (shape.getBounds().intersect(testPoint, rayMinus))
//                            inSun.set(false);
//                    });
                }
                if (inSun.get())
                    tmp.getAndIncrement();
            });
            avg.set(tmp.doubleValue() / (points.size() + zahl) * 100);
            zahl = 0;
            System.out.println(sunPos.calculateRayVector().azimuthDegree());
            System.out.println(avg.get() + "% hull");
            gesamt.updateAndGet(v -> new Double((double) (v + avg.get())));
            tmp.set(0);

        });
        gesamt.set(gesamt.get() / sunPositions.size());
        System.out.println(gesamt + "% area hull gesamt");
        return gesamt.get();





    }


}