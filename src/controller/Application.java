package controller;

import com.google.gson.Gson;
import model.*;
//import org.apache.commons.math3.exception.ArgumentOutsideDomainException;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
//import org.jogamp.java3d.utils.applet.MainFrame;
//import org.jogamp.java3d.utils.geometry.GeometryInfo;
//import org.jogamp.java3d.utils.geometry.NormalGenerator;
//import org.jogamp.java3d.utils.geometry.Sphere;
//import org.jogamp.java3d.utils.image.TextureLoader;
import javax.media.j3d.*;
import javax.vecmath.*;
//import org.jogamp.vecmath.*;


//import org.scijava.java3d.*;
import toxi.geom.LineStrip3D;
import toxi.geom.Spline3D;//Spline3D;
import toxi.geom.Vec3D;
//import com.sun.istack.internal.Nullable;

import view.Point3D;
import view.View;
import view.ViewInterface;

import java.applet.Applet;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class Application extends Applet {

    private static final float ATT_POINT_NODE_SIZE = 0.015f;
    private static final double THICKNESS_N = 2.1;//2.1;
    private static final float INIT_BRANCH_THICKNESS = 0.0025f;//0.0025f;
    private static ViewInterface view;

    private static final double DECIMATE = 0.08;
    private static final int STEP = 1; //every x STEP is visualized
    private static final int STEPS = 900; //max number of space colonization iterations
    private static final long DELAY = 00;
    private static final boolean DEBUG = false;
    private static final boolean SAVED = false;
    private static final String NEXTFILE = "expo";
    private static final String SAVEFILE = "exp"; //clud4m, cloud4m, cloud6m, cloud2, t2, ske (1.0), ver (4.0), postpr, popr2d, busch, sonne1, eval, eval2, eval3
    private static ShaderAppearance branchAppearance;
    public static final boolean TWO_D = false;

    public static final boolean PHOTO_MODE = true;
    private static Appearance obstacleAppearance;

    private static double[] factors;
    private static List<Obstacle> obstacles;
    private static Tree treelo;
    private static SpaceColonization colo;
    private static PointCloud pointCloud;
    private static List<TruncatedCone> branches;

    private static void run(double[] factors) throws InterruptedException {

        Thread.sleep(10000);

//        Thread.sleep(10000);
//        putAttractionPoints(cloud);

        ViewInterface.log("starting space colonization");
        long start = System.currentTimeMillis();


        colo.setInitPointcloudSize(pointCloud.getAttractionPoints().size());

        for (double factor : factors) {


            int i = 1;
            //ganze view resetten

            //baum für diese runde
            Tree dieserTree = copyTree();

            PointCloud cloud = copyPointCloud();


            //eine runde
            colo.startSpaceColonization();
            while (colo.spaceColonize(dieserTree, cloud, obstacles, factor)) {

                ViewInterface.log("   step " + i + "/" + STEPS);
                if (i % STEP == 0) {
//                putBranches(tree);
                putAttractionPoints(cloud);
                putSkeleton(dieserTree);
                }
                Thread.sleep(DELAY);

                if (i >= STEPS)
                    break;

                i++;
            }

            long stop = System.currentTimeMillis();
            double duration = (double) (stop - start) / 1000;
            ViewInterface.log("finished in " + duration + "seconds");


            postprocessing(dieserTree);
            putBranches(dieserTree);


            //stats
            {
                ViewInterface.log("\n");

                stats("iterations", String.valueOf(i));
                stats("duration", format(duration));
                stats("type", dieserTree.getType().name());
                stats("height", String.valueOf(dieserTree.getHeight()));
                System.out.println("real height: " + dieserTree.getRealHeight());
                if (SAVED) {
                    stats("file", SAVEFILE);
                } else {
                    stats("file", "new");
                }

                evaluierung(SAVEFILE);
                evaluierung(String.valueOf(dieserTree.getType()));
                evaluierung(String.valueOf(factor));

                colo.stats();
                calculateStats(dieserTree);
//                stats("avg#nodes light: ", dieserTree.nodesInLight(obstacles, branches) + "");
                evaluierung(dieserTree.nodesInLight(obstacles, branches) + "");
//                stats("avg light area: ", dieserTree.calculateBoundsPercentDings(obstacles) + "");
                evaluierung(dieserTree.areaTest(obstacles, view) + "");
                evaluierung(String.valueOf(dieserTree.getRealHeight()));
                obstacles.forEach(obstacle -> {
                    printToStats(obstacle.getName() + ": " + obstacle.getCentroid().cardinalString());
                    evaluierung(obstacle.getName() + ": " + obstacle.getCentroid().cardinalString());
                });
                stats("null", null);
                evaluierung(null);
            }

        }


    }

    private static Tree copyTree() {
        return new Tree(treelo.getType(), treelo.getHeight(), treelo.getNodes().getRoot().getPoint());
    }

    private static PointCloud copyPointCloud() {
        List<Point3D> points = new ArrayList<>();

        pointCloud.getAttractionPoints().forEach(ap -> points.add(new Point3D(ap.getX(), ap.getY(), ap.getZ())));

        PointCloud cloud = new PointCloud(points);
        cloud.setFunction(pointCloud.getFunction());
        return cloud;
    }

    private static void putShadow(Point3D pu1, Point3D pu2, SunPosition sp) {
        Appearance appearance = new ShaderAppearance();
        appearance.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
        Color3f gray = new Color3f((float) (1.0 / 255) * Color.darkGray.getRed(), (float) (1.0 / 255) * Color.darkGray.getGreen(), (float) (1.0 / 255) * Color.darkGray.getBlue());
        Material mat = new Material(gray, gray, gray, gray, 1.0f);
        appearance.setMaterial(mat);
        appearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.FASTEST, 0.8f));

        Point3D ray = sp.calculateRayVector();

        Group shadow = new Group();

        Point3f base1 = new Point3f(pu1.getX(), 0, pu1.getZ());
        Point3f base2 = new Point3f(pu2.getX(), 0, pu2.getZ());
        Point3f top1 = new Point3f(pu1.getX(), pu1.getY(), pu1.getZ());
        Point3f top2 = new Point3f(pu2.getX(), pu2.getY(), pu2.getZ());
        //top1Y + rayY * s = 0 <=> s * rayY = -top1Y <=> s = -top1Y / rayY
        float s = -top1.getY() / ray.getY();
        ray.multTo(s);
        //top + ray*s = ground
        Point3f ground1 = new Point3f();
        ground1.add(top1, new Point3f(ray.getX(), ray.getY(), ray.getZ()));
        Point3f ground2 = new Point3f();
        ground2.add(top2, new Point3f(ray.getX(), ray.getY(), ray.getZ()));

        NormalGenerator ng = new NormalGenerator();

        //viereck
        Point3f[] quadArray = new Point3f[]{top1, ground1, ground2, top2};
        //TODO
//        Building.swapArray(quadArray);
        GeometryInfo vier = new GeometryInfo(GeometryInfo.QUAD_ARRAY);
        vier.setCoordinates(quadArray);
        ng.generateNormals(vier);
        shadow.addChild(new Shape3D(vier.getGeometryArray(), appearance));

        //dreieck1
        Point3f[] triangleArray1 = new Point3f[]{top1, base1, ground1};
        //TODO
//        Building.swapArray(triangleArray1);
        GeometryInfo drei1 = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        drei1.setCoordinates(triangleArray1);
        ng.generateNormals(drei1);
        shadow.addChild(new Shape3D(drei1.getGeometryArray(), appearance));

        //dreeick2
        Point3f[] triangleArray2 = new Point3f[]{top2, ground2, base2};
        //TODO
//        Building.swapArray(triangleArray2);
        GeometryInfo drei2 = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        drei2.setCoordinates(triangleArray2);
        ng.generateNormals(drei2);
        shadow.addChild(new Shape3D(drei2.getGeometryArray(), appearance));

        BranchGroup bg = new BranchGroup();
        bg.addChild(shadow);
        view.addToScene(bg);
    }

    private static void initi() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        view = new View((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
        new MainFrame((View) view, (int) (screenSize.getWidth()), (int) (screenSize.getHeight()));

        branchAppearance = new ShaderAppearance();
        branchAppearance.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
        Color3f brown = new Color3f(0.318f, 0.212f, 0.051f);
        Color3f black = new Color3f(0, 0, 0);
        Color3f white = new Color3f(255, 255, 255);
        Color3f darkbrown = new Color3f(0.106f, 0.0314f, 0.0118f);
        Color3f lightbrown = new Color3f(1 / 255.0f * 222, 1 / 255.0f * 184, 1 / 255.0f * 135);
        branchAppearance.setMaterial(new Material(brown, darkbrown, brown, lightbrown, 10f));
        Texture loader = new TextureLoader(View.class.getClassLoader().getResource("bark001-color.jpg").getPath(), ((View) view).getComponent(0)).getTexture();
        branchAppearance.setTexture(loader);
        TexCoordGeneration generation = new TexCoordGeneration(TexCoordGeneration.EYE_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2);
        generation.setEnable(true);
        branchAppearance.setTexCoordGeneration(generation);

////        branchAppearance.setMaterial(new Material(brown, brown, black, black, 70f));

        obstacleAppearance = new ShaderAppearance();
        obstacleAppearance.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
//        Texture loader = new TextureLoader(View.class.getClassLoader().getResource("mauer.jpg").getPath(), ((View) view).getComponent(0)).getTexture();
//        obstacleAppearance.setTexture(loader);
//        TexCoordGeneration generation = new TexCoordGeneration(TexCoordGeneration.EYE_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_3);
//        generation.setEnable(true);
//        obstacleAppearance.setTexCoordGeneration(generation);

        Color3f gray = new Color3f((float) (1.0 / 255) * Color.gray.getRed(), (float) (1.0 / 255) * Color.gray.getGreen(), (float) (1.0 / 255) * Color.gray.getBlue());
        obstacleAppearance.setMaterial(new Material(gray, gray, gray, gray, 10f));

        //coordinates
//        view.addMarker(0, 0, 0, new Color3f(Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue()), 0.02f);
        if (!PHOTO_MODE) {
        view.addLine(new Point3D(0, 0, 0), new Point3D(10, 0, 0), Color.blue);
        for (int i = 1; i <= 10; i++) {
            view.addLine(new Point3D(i, 0, -0.05f), new Point3D(i, 0, 0.05f), Color.blue);
        }
//        view.addLine(new Point3D(0, 0, 0), new Point3D(0, 0, -10), Color.blue);
        view.addLine(new Point3D(0, 0, 0), new Point3D(0, 10, 0), new Color(0,0.8f,0));
        for (int i = 1; i <= 10; i++) {
            view.addLine(new Point3D(0, i, -0.05f), new Point3D(0, i, 0.05f), new Color(0,0.8f,0));
        }
        view.addLine(new Point3D(0, 0, 0), new Point3D(0, 0, 10), Color.red);
        for (int i = 1; i <= 10; i++) {
            view.addLine(new Point3D(-0.05f, 0, i), new Point3D(0.05f, 0, i), Color.red);
        }
//        view.addLine(new Point3D(0, 0, 0), new Point3D(10, 0, 0), Color.red);

        }

////        //metermaß
//        int hoehe = 5;
//        float xPos = -1.5f;
//        view.addLine(new Point3D(xPos, 0, 0), new Point3D(xPos, hoehe, 0), Color.black);
//        for (int i = 0; i <= hoehe; i++) {
//            view.addLine(new Point3D(xPos - 0.05f, i, 0), new Point3D(xPos + 0.05f, i, 0), Color.black);
//        }
    }

    private static void createScene() {

        //tree
        Point3D verschiebung = new Point3D(0, 0, 0);

        treelo = new Tree(TreeType.BSP1, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.VERTEILUNG1, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.VERTEILUNG2, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.MENGE1, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.MENGE2, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.POSTPRO, 3.0, verschiebung);

//        treelo = new Tree(TreeType.BUSCH, 3.4, verschiebung);

//        treelo = new Tree(TreeType.SONNE1, 4, verschiebung);
//        Tree tree = new Tree(TreeType.HINDERNISSE1, 5, verschiebung);

//        treelo = new Tree(TreeType.BSP5, 2.0, verschiebung);
//        treelo = new Tree(TreeType.EVAL, 4, verschiebung);
//        treelo = new Tree(TreeType.EVAL, 3, verschiebung);
//        treelo = new Tree(TreeType.EVAL, 2, verschiebung);
//        treelo = new Tree(TreeType.EVAL, 7.5, verschiebung);

//        treelo = new Tree(TreeType.ERSTBILD, 3.0, verschiebung);
//        Tree tree = new Tree(TreeType.PLATANE, 2.0, verschiebung);
//        treelo = new Tree(TreeType.KORKHASE, 3.0, verschiebung);
//        treelo = new Tree(TreeType.LANGER, 6.0, verschiebung);
//        treelo = new Tree(TreeType.HAENGER, 4.0, verschiebung);
//        treelo = new Tree(TreeType.TREE, 4.0, verschiebung);

        //obstacles
        obstacles = new ArrayList<>();
//        //onw building
//        Building southBuilding = new Building("south", new Point3D(-1.5f, 0, 2.0f), new Point3D(1.5f, 3, 1.0f));
//        obstacles.add(southBuilding);

//        view.addMarker(-1.5f, 0, 2.0f, Color.BLACK, 0.02f);
//        view.addMarker(1.5f, 3, 1.0f, Color.BLACK, 0.02f);
//        view.addLine(new Point3D(-1.5f, 0, 2.0f), new Point3D(1.5f, 0, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 3, 1.0f), new Point3D(-1.5f, 3, 1.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 3, 2.0f), new Point3D(-1.5f, 3, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 0, 1.0f), new Point3D(-1.5f, 0, 1.0f), Color.BLACK);
//
//        view.addLine(new Point3D(-1.5f, 0, 2.0f), new Point3D(-1.5f, 3, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 3, 1.0f), new Point3D(1.5f, 0, 1.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 3, 2.0f), new Point3D(1.5f, 0, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(-1.5f, 3, 1.0f), new Point3D(-1.5f, 0, 1.0f), Color.BLACK);
//
//        view.addLine(new Point3D(-1.5f, 0, 2.0f), new Point3D(-1.5f, 0, 1.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 3, 1.0f), new Point3D(1.5f, 3, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(-1.5f, 3, 1.0f), new Point3D(-1.5f, 3, 2.0f), Color.BLACK);
//        view.addLine(new Point3D(1.5f, 0, 1.0f), new Point3D(1.5f, 0, 2.0f), Color.BLACK);
//
//
//        view.addLine(new Point3D(2f,0,3), new Point3D(2f,0,-30), Color.red);
//        view.addLine(new Point3D(2f,0,3), new Point3D(-10.5f,0,3), Color.blue);
//        view.addLine(new Point3D(2f,0,3), new Point3D(2f,10,3), new Color(0,0.8f,0));

//
        //east building
//        Building eastBuilding = new Building("east", new Point3D(1.5f, 0, -2f), new Point3D(2.0f, 4.0f, 2f));
//        obstacles.add(eastBuilding);
//
//        //west building
//        Building westBuilding = new Building("west", new Point3D(-2.0f, 0, -1.5f), new Point3D(-1.0f, 3.0f, 1.5f));
//        obstacles.add(westBuilding);
//
//        //north
//        Building northBuilding = new Building("north", new Point3D(-1.5f, 0, -2.0f), new Point3D(1.5f, 3, -1.0f));
//        obstacles.add(northBuilding);
//
//        Building strangeBuilding = new Building("strange", new Point3D(-2.0f, 1, -1.5f), new Point3D(-0.2f, 1.5f, 1.5f));
//        obstacles.add(strangeBuilding);

        //TODO für erste bilder mit sonne
//        Building bild1 = new Building("bild1", new Point3D(1.5f, 0, -2f), new Point3D(3.0f, 4.0f, 1f));
//        obstacles.add(bild1);
//        Building bild2 = new Building("bild2", new Point3D(-1.5f, 0, 3.0f), new Point3D(0.5f, 4, 2.0f));
//        obstacles.add(bild2);
//        Building sonne1 = new Building("sonne1", new Point3D(2.0f, 0, -2f), new Point3D(4.0f, 4.0f, 2f));
//        obstacles.add(sonne1);


//        SunPosition sp = new SunPosition(Math.toRadians(90), Math.toRadians(50));
//
//        Point3D pu1 = new Point3D(2f, 4, -2);
//        Point3D pu2 = new Point3D(2f, 4, 2);
//            putShadow(pu1, pu2, sp);


//        List<SunPosition> sunPositions = SunCalculator.positionsForDay(126, 1.0); //TODO


//        Point3D  vekko = new Point3D(0,0,0);
//        Point3D  vekko2 = new Point3D(0,0,0);
//        //durchschnitt höhenwinkel berechnen wie der bei shadow detraction vector sich ergibt
//        sunPositions.forEach( sunPosition -> {
//            vekko.addTo(sunPosition.calculateRayVector().mult(Math.toDegrees(sunPosition.getElevationRadians())/100));
//            vekko2.addTo(sunPosition.calculateRayVector().mult(Math.toDegrees(sunPosition.getElevationRadians())));
//        });
//        System.out.println(vekko.azimuthPur());
//        System.out.println(vekko2.azimuthPur());
//        vekko.normalize();
//        vekko2.normalize();
//        System.out.println(vekko.elevationDegree());
//        System.out.println(vekko2.elevationDegree());

//        SunPosition sp = SunCalculator.midde(126);//sunPositions.size()/2 - 5);//new SunPosition(Math.toRadians(180), Math.toRadians(50));
////        view.setSun(sp);
////        Point3D pu1 = new Point3D(3f, 6, -2);
////        Point3D pu2 = new Point3D(3f, 6, 2);
//        Point3D pu1 = new Point3D(-3f, 8, 3);
//        Point3D pu2 = new Point3D(3f, 8, 3);
////        for(int l = 4; l < sunPositions.size()-8; l++){
////            putShadow(pu1, pu2, sunPositions.get(l));
////        }
////        sunPositions.forEach(sunPos -> putShadow(pu1, pu2, sunPos));
////        putShadow(pu1, pu2, sp);

        //view.addLine(pu, pu.add(sp.calculateRayVector().mult(-5)), Color.black);
        //eval dynamische szene
        Building bild1 = new Building("eval", new Point3D(3f, 0, -2f), new Point3D(6f, 6.0f, 2f));
////        obstacles.add(bild1);
        Building bild2 = new Building("eval2", new Point3D(-3, 0, 3), new Point3D(3, 8, 6));
//        obstacles.add(bild2);
//        //TODO BAUM 5m
//        Building bild1 = new Building("bild1", new Point3D(0.5f, 0, -2f), new Point3D(2.0f, 4.0f, 1f));
//        obstacles.add(bild1);
//        Building bild2 = new Building("bild2", new Point3D(-1.5f, 0, 2.0f), new Point3D(0.5f, 4, 1.0f));
//        obstacles.add(bild2);
//        Building bild3 = new Building("bild3", new Point3D(-1.5f, 0, 2.0f), new Point3D(1.5f, 4, 1.0f));
//        obstacles.add(bild3);
//        //TODO BAUM 4m
//        Building bild3 = new Building("bild3", new Point3D(0.5f, 0, -1f), new Point3D(2.0f, 2.0f, 1f));
//        obstacles.add(bild3);
//        Building bild4 = new Building("bild4", new Point3D(-0.5f, 0, -1f), new Point3D(-2.0f, 2.0f, 1f));
//        obstacles.add(bild4);


        putObstacles(obstacles);


        //cloud
        ViewInterface.log("generating point cloud");

        PointCloud cloud = new PointCloud();

        ViewInterface.log("fertig");

        colo = new SpaceColonization();

        if (SAVED) {
            List<Point3D> aps = new ArrayList<>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader("D:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\" + SAVEFILE + ".txt"));
                String st;
                Gson gson = new Gson();
                while ((st = reader.readLine()) != null)
                    aps.add(gson.fromJson(st, Point3D.class));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cloud.setAttractionPoints(aps);
        } else {
            cloud = colo.generatePointCloud(treelo);
            Gson gson = new Gson();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\" + NEXTFILE + ".txt"));
                cloud.getAttractionPoints().forEach(ap -> {
                    String json = gson.toJson(ap);
                    try {
                        writer.write(json);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        cloud.updateWithObstacles(obstacles);
        pointCloud = cloud;
        putAttractionPoints(pointCloud);

    }

    public static void main(String[] args) throws InterruptedException {

        branches = new ArrayList<>();
//        IOController.analyzeCSV();

        factors = new double[args.length];
        for (int i = 0; i < args.length; i++) {
            factors[i] = Double.parseDouble(args[i]);
        }


        initi();
        createScene();
//        TreeType type = treelo.getType();
//        double treeHeight = treelo.getHeight();
//        double crownHeight = treeHeight * type.getTopPercentage() / 100;
//        double treeRadius = type.getWidthPerHeight() * treeHeight / 2;
//        Point3D[] points;
//        points = new Point3D[]{
//                new Point3D((float) (0.2 * treeRadius), (float) (treeHeight - crownHeight), 0),
//                new Point3D((float) treeRadius, (float) (treeHeight - 0.3 * crownHeight), 0),
//                new Point3D(0, (float) treeHeight, 0)};
//        SplineInterpolator interpolator = new SplineInterpolator();
//        double[] x = new double[points.length];
//        double[] y = new double[points.length];
//        for (int i = 0; i < points.length; i++) {
//            x[i] = points[i].getX();
//            y[i] = points[i].getY();
//        }
//        PolynomialSplineFunction splineFunction = interpolator.interpolate(y, x);
//            Application.putSpline(splineFunction, points);

        run(factors);

//        //shit testen triangle intersection
//        Triangle triangle = new Triangle(new Point3d(1,0,0), new Point3d(1,1,0), new Point3d(1,0,1));
//
//        System.out.println("ein test");
//        List<Point3d> points = new ArrayList<>();
//        points.add(new Point3d(0,0.1,0.1)); //drin
//        points.add(new Point3d(0,1.5,0.1)); //drüber
//        points.add(new Point3d(-1,0.1,0.1)); //drin -x
//        points.add(new Point3d(0,-0.2,0.1)); //drunter
//        points.add(new Point3d(0,0.1,1.5)); //zu z
//        points.add(new Point3d(2,0.1,0.1)); //zu +x
//
//
//        Vector3d vec = new Vector3d(1,0,0);
//        points.forEach(point3d -> {
//            System.out.println(triangle.intersect(point3d,vec));
//        });
//
//        int k = 3;
    }

    private static void calculateStats(Tree tree) {

        //angle
        Point3D angle = tree.calculateAngle();
        if (!PHOTO_MODE)
            view.addLine(new Point3D(0, 0, 0), angle.mult(9), Color.orange);
        stats("azimuth degr", angle.azimuthDegree());
        stats("elevation degr", angle.elevationDegree());
        evaluierung(angle.azimuthPur());
        evaluierung(angle.elevationDegree());
        Point3D zero = new Point3D(0, 0, 0);
        if (!PHOTO_MODE) {
            view.setLine(zero, zero.add(angle.mult(tree.getHeight())));
            angle.setY(0);
            angle.normalize();
            view.addLine(zero, zero.add(angle.mult(tree.getHeight())), Color.black);
        }

        //number of nodes
        stats("number of nodes", String.valueOf(tree.getNodes().getAll().size()));
        evaluierung(String.valueOf(tree.getNodes().getAll().size()));

        //number of branches
        stats("number of branches", String.valueOf(tree.calculateBranches()));
        evaluierung(String.valueOf(tree.calculateBranches()));

        //avgNode
        Point3D avgNode = tree.calculateAvgNode();
        stats("average Node", avgNode.cardinalString());
        evaluierung(avgNode.shortString());
        evaluierung(avgNode.azimuthPur());
        evaluierung(String.valueOf(new Point3D(avgNode.getX(),0,avgNode.getZ()).vectorLength()));
        if (!PHOTO_MODE)
            view.setSchwerpunkt(avgNode);
        avgNode.normalize();
//        ViewInterface.log("avg normalized " + avgNode.toString());
        avgNode.multTo(3f);
//        view.addMarker(avgNode.getX(), avgNode.getY(), avgNode.getZ(), new Color3f(Color.green), 0.05f);

        //avgNode gewichtet
        Point3D schwerpunkt = tree.calculateSchwerpunkt();
//        ViewInterface.log(schwerpunkt.toString());
        schwerpunkt.normalize();
//        ViewInterface.log("schwerpunkt normalized " + schwerpunkt.toString());
        schwerpunkt.multTo(3f);
//        view.addMarker(schwerpunkt.getX(), schwerpunkt.getY(), schwerpunkt.getZ(), new Color3f(Color.blue), 0.05f);


    }

    public static void stats(String description, String value) {
        printToStats(value);
        ViewInterface.log(description + ": " + value);
    }

    public static void evaluierung(String string) {//@Nullable
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\eval_test.txt", true));

            if (string == null)
                writer.newLine();
            else
                writer.write(string + ",");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printToStats(String string) {//@Nullable
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\stats.txt", true));

            if (string == null)
                writer.newLine();
            else
                writer.write(string + "\t\t");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String format(double duration) {
        return (Math.round(duration / 60) + "m" + Math.round(duration % 60) + "s");
    }

    private static void putObstacles(List<Obstacle> obstacles) {
        BranchGroup bg = new BranchGroup();

        obstacles.forEach(obst -> {
//                obstacle -> bg.addChild(obstacle.getShape3D(obstacleAppearance));
            Texture texture = new TextureLoader(view.View.class.getClassLoader().getResource("mauer.jpg").getPath(), ((View) view).getComponent(0)).getTexture();

            bg.addChild(obst.getBox(texture, false));
        });
        view.addToScene(bg);
    }

    /**
     * Creates Sphere for every attraction point of the pointcloud and passes them to the view.
     *
     * @param cloud
     */
    private static void putAttractionPoints(PointCloud cloud) {

        List<Point3D> nodes = cloud.getAttractionPoints();
        BranchGroup bg = new BranchGroup();

        Color3f white = new Color3f(0.7f, 0.7f, 0.7f);

        Appearance app = new Appearance();
        Color3f blue = new Color3f(Color.BLUE.getRed(), Color.BLUE.getGreen(), Color.BLUE.getBlue());
        app.setMaterial(new Material(blue, blue, white, blue, 70f));

        Appearance app2 = new Appearance();
        Color3f red = new Color3f(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue());
        app2.setMaterial(new Material(red, red, white, red, 70f));

        nodes.forEach(point -> {
            TransformGroup tg = new TransformGroup();
            Transform3D t = new Transform3D();

            t.setTranslation(new Vector3d(point.getX(), point.getY(), point.getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(ATT_POINT_NODE_SIZE);
            if (point.isActivated()) {

                sphere.setAppearance(app);
                tg.addChild(sphere);
            } else {
                sphere.setAppearance(app2);
                tg.addChild(sphere);

            }
            bg.addChild(tg);
        });

        view.resetNodes();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToNodes(bg);
    }

    /**
     * Creates branches for the tree and passes them to the view.
     *
     * @param tree
     */
    private static void putBranches(Tree tree) {

        tree.getNodes().calculateThicknesses(INIT_BRANCH_THICKNESS, THICKNESS_N);
        tree.calculateDiscs();


        KDParentTreeNode root = tree.getNodes().getRoot();
        BranchGroup bg = new BranchGroup();

        if (root == null) {
            return;
        }

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(root);

        int nodeCounter = 1;

        while (!nodes.isEmpty()) {

            KDParentTreeNode tmp = nodes.pop();

//            if (tmp.equals(tree.getNodes().getRoot()))
//                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY | TruncatedCone.BOT));
//            else if (tree.getNodes().getLeaves().contains(tmp))
//                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY | TruncatedCone.TOP));
//            else {
            if (PHOTO_MODE)
                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY | TruncatedCone.TOP | TruncatedCone.BOT)); //mit top und bot für mehr beauty
            else
                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY));
//            }

            tmp.getTreeChildren().forEach(nodes::push);
            nodeCounter += tmp.getTreeChildren().size();
//            if (tmp.getLtbChild() != null) {
//                nodes.push(tmp.getLtbChild());
//                nodeCounter++;
//            }
//            if (tmp.getRbfChild() != null) {
//                nodes.push(tmp.getRbfChild());
//                nodeCounter++;
//
//            }
//            System.out.println(nodeCounter);
        }
        System.out.println("hä");
        ViewInterface.log("      processed " + nodeCounter + " nodes");
        view.resetTree();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToTree(bg);

//        log("      displayed " + nodeCounter + " nodes");

    }

    /**
     * Returns Transformgroup containing one cylinder which represents the branch between node and its parent.
     *
     * @param node
     * @return
     */
    private static TransformGroup buildCylinder(KDParentTreeNode node, int flags) {


        Appearance debugAppearance;
        if (DEBUG) {
            Random random = new Random();
            Color3f black = new Color3f(0, 0, 0);

            debugAppearance = new Appearance();
            Color3f rand = new Color3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            debugAppearance.setMaterial(new Material(rand, rand, rand, black, 70f));
        }

        TransformGroup tg = new TransformGroup();

        KDParentTreeNode parent = node.getTreeParent();
        if (parent == null)
            return tg;

//        view.addLine(parent.getPoint(), node.getPoint(), Color.red);

        Transform3D t = transform(parent.getPoint(), node.getPoint());

        tg.setTransform(t);

        TruncatedCone branch = new TruncatedCone(node, (float) (node.getPoint().distance(node.getTreeParent().getPoint())), branchAppearance, flags);

        branch.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE);
        branch.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        branch.setBoundsAutoCompute(true);
        branches.add(branch);
        tg.addChild(branch);

        return tg;
    }

    public static void visualizeSun(SunPosition sunPos) {
        view.setSun(sunPos);
    }

    private static Transform3D transform(Point3D parentPoint, Point3D nodePoint) {

        Point3D vector = nodePoint.subtract(parentPoint);
        Transform3D t = new Transform3D();

        if (nodePoint.toString().equals(parentPoint.toString())) {
            System.out.println("parent and node were equal");
            return t;
        }

//        Point3D newYAxis = new Point3D(nodePoint.getX() - parentPoint.getX(), nodePoint.getY() - parentPoint.getY(), nodePoint.getZ() - parentPoint.getZ());
//
//
//        //calculate new X and Z vector (orthogonal)
//        Point3D newXAxis;
//        Point3D newZAxis;
//        //check if newY is parallel to one of the old axis
//        if (newYAxis.getX() == 0 && newYAxis.getY() == 0) {
//            //Y axis is now Z axis
//            newXAxis = new Point3D(0, 1.0f, 0);
//            newZAxis = new Point3D(1.0f, 0, 0);
//        } else if (newYAxis.getY() == 0 && newYAxis.getZ() == 0) {
//            //Y axis is now X axis
//            newXAxis = new Point3D(0, 0, 1.0f);
//            newZAxis = new Point3D(0, 1.0f, 0);
//        } else if (newYAxis.getZ() == 0 && newYAxis.getX() == 0) {
//            //Y axis is still Y axis
//            newXAxis = new Point3D(1.0f, 0, 0);
//            newZAxis = new Point3D(0, 0, 1.0f);
//        } else {
//            //höchstens eine 0 im vektor der neuen y Achse
//            newXAxis = new Point3D(-newYAxis.getY(), newYAxis.getX(), 0);
//            newZAxis = new Point3D(newYAxis.getY() * newXAxis.getZ() - newYAxis.getZ() * newXAxis.getY(),
//                    newYAxis.getZ() * newXAxis.getX() - newYAxis.getX() * newXAxis.getZ(),
//                    newYAxis.getX() * newXAxis.getY() - newYAxis.getY() * newXAxis.getX());
//        }
//
//
//        //build matrix
//
//        Matrix3f matrix = new Matrix3f(newXAxis.getX() / newXAxis.vectorLength(), newYAxis.getX() / newYAxis.vectorLength(), newZAxis.getX() / newZAxis.vectorLength(),
//                                        newXAxis.getY() / newXAxis.vectorLength(), newYAxis.getY() / newYAxis.vectorLength(), newZAxis.getY() / newZAxis.vectorLength(),
//                                        newXAxis.getZ() / newXAxis.vectorLength(), newYAxis.getZ() / newYAxis.vectorLength(), newZAxis.getZ() / newZAxis.vectorLength());
//

        Point3D yAchse = new Point3D(0, 1, 0);
        vector.normalize();
        Point3D drehachse = yAchse.cross(vector);
        double alpha = Math.acos(yAchse.dot(vector) / (yAchse.vectorLength() * vector.vectorLength()));
        drehachse.normalize();

        if (alpha == 0) {
            t.setTranslation(new Vector3d((parentPoint.getX() /*+ 0.5 * vector.getX()*/), (parentPoint.getY() /*+ 0.5 * vector.getY()*/), (parentPoint.getZ() /*+ 0.5 * vector.getZ()*/)));
            return t;
        }
        double n1 = drehachse.getX();
        double n2 = drehachse.getY();
        double n3 = drehachse.getZ();
        double cosA = Math.cos(alpha);
        double dings = 1 - cosA;
        double sinA = Math.sin(alpha);

        //reihenweise
        Matrix3d matrix = new Matrix3d(Math.pow(n1, 2) * dings + cosA, n1 * n2 * dings - n3 * sinA, n1 * n3 * dings + n2 * sinA,
                n2 * n1 * dings + n3 * sinA, Math.pow(n2, 2) * dings + cosA, n2 * n3 * dings - n1 * sinA,
                n3 * n1 * dings - n2 * sinA, n3 * n2 * dings + n1 * sinA, Math.pow(n3, 2) * dings + cosA);


        t.set(matrix, new Vector3d((parentPoint.getX() /*+ 0.5 * vector.getX()*/), (parentPoint.getY() /*+ 0.5 * vector.getY()*/), (parentPoint.getZ() /*+ 0.5 * vector.getZ()*/)), 1.0f);

        return t;
    }

    private static void putSkeleton(Tree tree) {
        view.resetTree();

        KDParentTreeNode root = tree.getNodes().getRoot();
        Appearance app = new Appearance();
        BranchGroup bg = new BranchGroup();
        Color3f color = new Color3f(0, 0, 0);
        app.setMaterial(new Material(color, color, color, color, 70f));
        ColoringAttributes att = new ColoringAttributes();
        att.setColor(color);
        app.setColoringAttributes(att);

        if (root == null) {
            return;
        }


        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(root);

        while (!nodes.empty()) {
            KDParentTreeNode node = nodes.pop();
            //node
            TransformGroup tg = new TransformGroup();
            Transform3D t = new Transform3D();

            t.setTranslation(new Vector3d(node.getPoint().getX(), node.getPoint().getY(), node.getPoint().getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(0.007f);
            sphere.setAppearance(app);

            tg.addChild(sphere);
            bg.addChild(tg);


            KDParentTreeNode parent = node.getTreeParent();
            if (parent != null) {
                LineArray lineX = new LineArray(2, LineArray.COORDINATES);
                lineX.setCoordinate(0, new Point3f(node.getPoint().getX(), node.getPoint().getY(), node.getPoint().getZ()));
                lineX.setCoordinate(1, new Point3f(parent.getPoint().getX(), parent.getPoint().getY(), parent.getPoint().getZ()));
                bg.addChild(new Shape3D(lineX, app));
            }
//            node.getTreeChildren().forEach(child -> {
//                LineArray lineX = new LineArray(2, LineArray.COORDINATES);
//                lineX.setCoordinate(0, new Point3f(node.getPoint().getX(), node.getPoint().getY(), node.getPoint().getZ()));
//                lineX.setCoordinate(1, new Point3f(child.getPoint().getX(), child.getPoint().getY(), child.getPoint().getZ()));
//                bg.addChild(new Shape3D(lineX, app));
//            });
            //segment

            node.getTreeChildren().forEach(nodes::push);
        }
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToTree(bg);

    }

    private static void postprocessing(Tree tree) {
        decimate(tree, tree.getNodes().getRoot(), 0);
        reduceAngles(tree.getNodes().getRoot());
        curveSubdivision(tree, tree.getNodes().getRoot(), new ArrayList<>());

    }

    private static void decimate(Tree tree, KDParentTreeNode node, double akku) {
        int children = node.getTreeChildren().size();
        if (children == 0) // spitze
            return;

        if (children == 1) { //inner knoten vom ast
            KDParentTreeNode childs = node.getTreeChildren().get(0);
            //node.getPoint().distance(node.getTreeParent().getPoint()); //müsste eig immer d sein
            //wenn < decimate dann kommt der weg und seinen parent mit seinem kind verbinden
            if (akku < DECIMATE && akku != 0) { //wenn akku = 0 dann ist es ein startpunnkt eines astes
                //node entfernen bis decimate erreicht ist
                tree.getNodes().getAll().remove(node); //TODO macht kd tree kaputt
                node.getTreeParent().getTreeChildren().remove(node);
                node.getTreeChildren().forEach(child -> {
                    node.getTreeParent().getTreeChildren().add(child);
                    child.setTreeParent(node.getTreeParent());
                });
                //für kind aufrufen
                decimate(tree, childs, akku + tree.getType().getNodeDist());
            } else {
                //node behalten ab decimate
                //für kind aufrufen
                decimate(tree, childs, 0 + tree.getType().getNodeDist());
            }
        }

        if (children > 1) { //nächster branchingpoint, hier beginnt neuer ast
            List<KDParentTreeNode> nodes = new ArrayList<>();
            node.getTreeChildren().forEach(child -> nodes.add(child));
            for (KDParentTreeNode child : nodes) {
                decimate(tree, child, 0 + tree.getType().getNodeDist());
            }
        }

    }

    private static void reduceAngles(KDParentTreeNode node) {
        List<KDParentTreeNode> children = node.getTreeChildren();
        children.forEach(child -> reduceAngles(child));
        if (node.getTreeParent() == null)
            return;

        Point3D point = node.getPoint();
        Point3D parentVec = node.getTreeParent().getPoint().subtract(point); //von punkt zu parent
        node.setPoint(point.add(parentVec.divide(2))); //punkt halben weg zu parent bewegen

    }

    private static void curveSubdivision(Tree tree, KDParentTreeNode node, List<KDParentTreeNode> ast) {
        ast.add(node); //node zu ast

        int children = node.getTreeChildren().size();

        if (children == 0 || children > 1) {// spitze
            //ast ist jetzt vollständig.
            if (ast.size() == 2) {
                if (ast.get(0).getTreeParent() != null)
                    ast.add(0, ast.get(0).getTreeParent()); //zusatz für subdivision
                //wenn ast zu kurz zum subdividen mit splines dann parent von vorher einhängen
                //problem: parent soll nicht verändert werden und auch nicht subdivided dazwischen


                //die haben die die struktur verändert, getDecimatedVertices war früher von Spline3D und ist jetzt von LineStrip3D, müsste aber trz noch die vertices auf dem spline zurückgeben!
                //ich mache also astPoints zu nem LineStrip3D (ist da ne liste und kein array)
//                Vec3D[] astPoints = new Vec3D[ast.size()];
//                for (int i = 0; i < astPoints.length; i++) {
//                    astPoints[i] = new Vec3D(ast.get(i).getPoint().getX(), ast.get(i).getPoint().getY(), ast.get(i).getPoint().getZ());
//                }

                LineStrip3D astLineStrip = new LineStrip3D();
                for (int i = 0; i < ast.size(); i++){
                    astLineStrip.add(new Vec3D(ast.get(i).getPoint().getX(), ast.get(i).getPoint().getY(), ast.get(i).getPoint().getZ()));
                }

//                Spline3D spline3D = new Spline3D(astPoints);
                try {
//                    List<Vec3D> subdivided = spline3D.getDecimatedVertices((float) tree.getType().getNodeDist(),true);
                    List<Vec3D> subdivided = astLineStrip.getDecimatedVertices((float) tree.getType().getNodeDist(),true);


                    List<Point3D> subbi = new ArrayList<>();
                    AtomicBoolean da = new AtomicBoolean(false);
                    //nur die in subbi kopieren ab first, dann sidn alle zusätzlichen weg
                    subdivided.forEach(punkt -> {
                        Point3D punkto = new Point3D(punkt.getComponent(0), punkt.getComponent(1), punkt.getComponent(2));
//                    if(equal(punkt, test))//equal geht nicht weil ich decimatedvertices benutze
                        if (punkto.distance(ast.get(0).getPoint()) > ast.get(1).getPoint().distance(ast.get(0).getPoint())) // wenn abstand zu hilfsknoten größer ist als abstand von first zu hilfsknoten
                            da.set(true);
                        if (da.get())
                            subbi.add(punkto);
                    });

                    //zusatz entfernen
                    ast.remove(0);


                    //subdivided hat jetzt subdividedte punkte zwischen beginn und ende des astes
                    //also alle inneren knoten abhängen und löschen

                    KDParentTreeNode first = ast.get(0);
                    KDParentTreeNode last = ast.get(ast.size() - 1);

                    //bei first abhängen
                    first.getTreeChildren().remove(ast.get(1));
                    //bei last abhängen
                    last.setTreeParent(null); //TODO überflüssig

                    //alle inneren entfernen
                    ast.remove(0);
                    ast.remove(ast.size() - 1);
                    tree.getNodes().getAll().removeAll(ast);

                    //zwischen dem ersten und dem letzten die knoten aus subdivided einhängen
                    KDParentTreeNode nextParent = first;
                    for (int i = 1; i < subbi.size() - 1; i++) {
                        KDParentTreeNode tmp = new KDParentTreeNode(subbi.get(i), new double[]{}, nextParent);//hat parent
                        nextParent.getTreeChildren().add(tmp);//ist kind von parent
                        tree.getNodes().getAll().add(tmp);//zum baum
                        nextParent = tmp;//next
                    }
                    //unten wieder anhängen an last
                    nextParent.getTreeChildren().add(last);
                    last.setTreeParent(nextParent);

                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("ArrayIndexOutOfBounds bei curve subdivision");
                }
            } else if (ast.size() >= 3) {
                //hier durch änderung in toxiclibs die gleichen änderungen wie in dem if fall hier drüber
//                Vec3D[] astPoints = new Vec3D[ast.size()];
//                for (int i = 0; i < astPoints.length; i++) {
//                    astPoints[i] = new Vec3D(ast.get(i).getPoint().getX(), ast.get(i).getPoint().getY(), ast.get(i).getPoint().getZ());
//                }
                LineStrip3D astLineStrip = new LineStrip3D();
                for (int i = 0; i < ast.size(); i++){
                    astLineStrip.add(new Vec3D(ast.get(i).getPoint().getX(), ast.get(i).getPoint().getY(), ast.get(i).getPoint().getZ()));
                }

//                Spline3D spline3D = new Spline3D(astPoints);
//                List<Vec3D> subdivided = spline3D.getDecimatedVertices((float) tree.getType().getNodeDist(), true);
                List<Vec3D> subdivided = astLineStrip.getDecimatedVertices((float) tree.getType().getNodeDist(), true);
                List<Point3D> subbi = new ArrayList<>();
                subdivided.forEach(punkt -> subbi.add(new Point3D(punkt.getComponent(0), punkt.getComponent(1), punkt.getComponent(2))));

                //subdivided hat jetzt subdividedte punkte zwischen beginn und ende des astes
                //also alle inneren knoten abhängen und löschen

                KDParentTreeNode first = ast.get(0);
                KDParentTreeNode last = ast.get(ast.size() - 1);

                //bei first abhängen
                first.getTreeChildren().remove(ast.get(1));
                //bei last abhängen
                last.setTreeParent(null); //TODO überflüssig

                //alle inneren entfernen
                ast.remove(0);
                ast.remove(ast.size() - 1);
                tree.getNodes().getAll().removeAll(ast);

                //zwischen dem ersten und dem letzten die knoten aus subdivided einhängen
                KDParentTreeNode nextParent = first;
                for (int i = 1; i < subbi.size() - 1; i++) {
                    KDParentTreeNode tmp = new KDParentTreeNode(subbi.get(i), new double[]{}, nextParent);//hat parent
                    nextParent.getTreeChildren().add(tmp);//ist kind von parent
                    tree.getNodes().getAll().add(tmp);//zum baum
                    nextParent = tmp;//next
                }
                //unten wieder anhängen an last
                nextParent.getTreeChildren().add(last);
                last.setTreeParent(nextParent);
            }
            //wenn der kindknoten hat muss ich für jeden kindknoten curve subdividen aber aktueller node muss schon mti drin sein
            List<KDParentTreeNode> nextAst = new ArrayList<>();
            nextAst.add(node);

            List<KDParentTreeNode> childrenList = new ArrayList<>();
            node.getTreeChildren().forEach(child -> childrenList.add(child));
            childrenList.forEach(child -> {
                List<KDParentTreeNode> nextAstfinal = new ArrayList<>();
                nextAstfinal.add(node);
                curveSubdivision(tree, child, nextAstfinal);
            });
        }

        if (children == 1) { //inner knoten vom ast
            KDParentTreeNode child = node.getTreeChildren().get(0);
            curveSubdivision(tree, child, ast);
        }
    }

    public static void putSpline(PolynomialSplineFunction function, Point3D[] points) {
        for (Point3D point : points) {
            view.addMarker(point.getX(), point.getY(), point.getZ());
        }
        double d = 0.01;
        double minY = points[0].getY();
        double maxY = points[points.length - 1].getY();

        System.out.println(maxY);
        Point3D one = points[0];
        for (double i = minY; i <= maxY; i += d) {
//            try {
                Point3D two = new Point3D((float) function.value(i), (float) i, 0);
                view.addLine(one, two, Color.black);
                one = two;
//            } catch (ArgumentOutsideDomainException e) {
//                e.printStackTrace();
//            }

        }
    }
}
