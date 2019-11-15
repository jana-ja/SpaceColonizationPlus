package controller;

import com.google.gson.Gson;
import com.sun.istack.internal.Nullable;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.Sphere;
import model.*;
import view.Point3D;
import view.View;
import view.ViewInterface;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.applet.Applet;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class Application extends Applet {

    private static final float ATT_POINT_NODE_SIZE = 0.015f;
    private static final double THICKNESS_N = 2.1;
    private static final float INIT_BRANCH_THICKNESS = 0.0025f;
    private static ViewInterface view;

    private static final int STEP = 1; //every x STEP is visualized
    private static final int STEPS = 250; //number of space colonization iterations
    private static final long DELAY = 0;
    private static final boolean DEBUG = false;
    private static final boolean SAVED = true;
    private static final String SAVEFILE = "cloud4m"; //cloud4m, cloud6m
    private static Appearance branchAppearance;
    private static Appearance obstacleAppearance;


    private static void run() throws InterruptedException {

        Point3D verschiebung = new Point3D(0, 0, 0);

        Tree tree = new Tree(TreeType.PLATANE, 4.0, verschiebung);


        //obstacles
        List<Obstacle> obstacles = new ArrayList<>();
        //onw building
        Building southBuilding = new Building("south", new Point3D(-1.5f, 0, 2.0f), new Point3D(1.5f, 3, 1.0f));
        obstacles.add(southBuilding);

        //east building
        Building eastBuilding = new Building("east", new Point3D(0.5f, 0, -1.5f), new Point3D(2.0f, 2.0f, 1.5f));
        obstacles.add(eastBuilding);

        //west building
        Building westBuilding = new Building("west", new Point3D(-2.0f, 0, -1.5f), new Point3D(-1.0f, 2.0f, 1.5f));
//        obstacles.add(westBuilding);

//        Building strangeBuilding = new Building("strange", new Point3D(-2.0f, 1, -1.5f), new Point3D(-0.2f, 1.5f, 1.5f));
//        obstacles.add(strangeBuilding);

        putObstacles(obstacles);

        SpaceColonization colo = new SpaceColonization();
        ViewInterface.log("generating point cloud");

        PointCloud cloud = new PointCloud();

        if (SAVED) {
            List<Point3D> aps = new ArrayList<>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader("E:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\" + SAVEFILE + ".txt"));
                String st;
                Gson gson = new Gson();
                while ((st = reader.readLine()) != null)
                    aps.add(gson.fromJson(st, Point3D.class));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cloud.setAttractionPoints(aps);
        } else {
            cloud = colo.generatePointCloud(tree);
            Gson gson = new Gson();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\cloud2.txt"));
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

        cloud.intersectWithObstacles(obstacles);

        ViewInterface.log("starting space colonization");
        long start = System.currentTimeMillis();
        int i = 1;

        while (colo.spaceColonize(tree, cloud, obstacles)) {

            ViewInterface.log("   step " + i + "/" + STEPS);
            if (i % STEP == 0) {
                putBranches(tree);
                putAttractionPoints(cloud);
            }
            Thread.sleep(DELAY);

//            calculateStats(tree);

            if (i >= STEPS)
                break;

            i++;
        }

        long stop = System.currentTimeMillis();
        double duration = (double) (stop - start) / 1000;
        ViewInterface.log("finished in " + duration + "seconds");

//        Texture loader = new TextureLoader(View.class.getClassLoader().getResource("bark001-color.jpg").getPath(), ((View) view).getComponent(0)).getTexture();
//        branchAppearance.setTexture(loader);
//        TexCoordGeneration generation = new TexCoordGeneration(TexCoordGeneration.EYE_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2);
//        generation.setEnable(true);
//        branchAppearance.setTexCoordGeneration(generation);
//
//        putBranches(tree);
//        putAttractionPoints(cloud);


        ViewInterface.log("\n");

        stats("iterations", String.valueOf(i));
        stats("duration", String.valueOf(format(duration)));
        stats("type", tree.getType().name());
        stats("height", String.valueOf(tree.getHeight()));
        if(SAVED){
            stats("file", SAVEFILE);
        }else{
            stats("file", "new");
        }

        colo.stats();
        calculateStats(tree);

        obstacles.forEach(obstacle -> printToStats(obstacle.getName() + ": " + obstacle.getCentroid().cardinalString()));
        stats("null", null);
    }

    public static void main(String[] args) throws InterruptedException {

//        IOController.analyzeCSV();


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        view = new View((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
        new MainFrame((View) view, (int) (screenSize.getWidth()), (int) (screenSize.getHeight()));

        branchAppearance = new Appearance();
        Color3f brown = new Color3f(0.318f, 0.212f, 0.051f);
        Color3f black = new Color3f(0, 0, 0);
        branchAppearance.setMaterial(new Material(brown, brown, black, black, 70f));
//        branchAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
//        branchAppearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);


        obstacleAppearance = new Appearance();
        Color3f white = new Color3f(Color.WHITE);
        obstacleAppearance.setMaterial(new Material(white, black, white, white, 110f));

        //coordinates
        view.addMarker(0, 0, 0, new Color3f(Color.black), 0.02f);
        view.addLine(new Point3D(0,0,0), new Point3D(10,0,0), Color.blue);
//        view.addMarker(0.5f, 0, 0, new Color3f(Color.blue), 0.04f);
        view.addLine(new Point3D(0,0,0), new Point3D(0,10,0), Color.green);
//        view.addMarker(0, 0.5f, 0, new Color3f(Color.green), 0.04f);
        view.addLine(new Point3D(0,0,0), new Point3D(0,0,10), Color.red);
//        view.addMarker(0, 0, 0.5f, new Color3f(Color.red), 0.04f);


        run();


    }

    private static void calculateStats(Tree tree) {
        //angle
        Point3D angle = tree.calculateAngle();
//        ViewInterface.log(angle.toString());
//        ViewInterface.log(angle.toDegrees().toString());
        Point3D zero = new Point3D(0, 0, 0);
        view.setLine(zero, zero.add(angle.mult(tree.getHeight())));

        //number of nodes
        stats("number of nodes", String.valueOf(tree.getNodes().getAll().size()));

        //number of branches
        stats(" ", " ");

        //avgNode
        Point3D avgNode = tree.calculateAvgNode();
        stats("average Node", avgNode.cardinalString());
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

    public static void stats(String description, String value){
        printToStats(value);
        ViewInterface.log(description + ": " + value);
    }

    private static void printToStats(@Nullable String string) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\stats.txt", true));

            if(string == null)
                writer.newLine();
            else
                writer.write(string + "\t\t");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String format(double duration) {
        return (Math.round(duration/60) + "m" + Math.round(duration%60) +"s");
    }

    private static void putObstacles(List<Obstacle> obstacles) {
        BranchGroup bg = new BranchGroup();

        obstacles.forEach(obstacle -> bg.addChild(obstacle.getShape3D(obstacleAppearance)));
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

        Appearance app = new Appearance();
        Color3f green = new Color3f(Color.BLUE);
        app.setMaterial(new Material(green, green, green, green, 70f));

        nodes.forEach(point -> {
            TransformGroup tg = new TransformGroup();
            Transform3D t = new Transform3D();

            t.setTranslation(new Vector3d(point.getX(), point.getY(), point.getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(ATT_POINT_NODE_SIZE);
            sphere.setAppearance(app);

            tg.addChild(sphere);
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


        KDParentTreeNode node = tree.getNodes().getRoot();
        BranchGroup bg = new BranchGroup();

        if (node == null) {
            return;
        }

        Stack<KDParentTreeNode> nodes = new Stack<>();

        nodes.push(node);

        int nodeCounter = 1;

        while (!nodes.isEmpty()) {

            KDParentTreeNode tmp = nodes.pop();

            if (tmp.equals(tree.getNodes().getRoot()))
                bg.addChild(buildCylinder(tmp, tmp.getThickness(), tmp.getThickness(), TruncatedCone.BODY | TruncatedCone.BOT));
            else if (tree.getNodes().getLeaves().contains(tmp))
                bg.addChild(buildCylinder(tmp, tmp.getThickness(), tmp.getParent().getThickness(), TruncatedCone.BODY | TruncatedCone.TOP));
            else
                bg.addChild(buildCylinder(tmp, tmp.getThickness(), tmp.getParent().getThickness(), TruncatedCone.BODY));


            if (tmp.getLtbChild() != null) {
                nodes.push(tmp.getLtbChild());
                nodeCounter++;
            }
            if (tmp.getRbfChild() != null) {
                nodes.push(tmp.getRbfChild());
                nodeCounter++;

            }
        }
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
    private static TransformGroup buildCylinder(KDParentTreeNode node, float thicknessTop, float thicknessBot, int flags) {

        Appearance debugAppearance;
        if (DEBUG) {
            Random random = new Random();
            Color3f black = new Color3f(0, 0, 0);

            debugAppearance = new Appearance();
            Color3f rand = new Color3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            debugAppearance.setMaterial(new Material(rand, rand, rand, black, 70f));
        }
        TransformGroup tg = new TransformGroup();

        KDParentTreeNode parent = node.getParent();
        if (parent == null)
            return tg;


        Point3D parentPoint = parent.getPoint();
        Point3D nodePoint = node.getPoint();

        Point3D vector = nodePoint.subtract(parentPoint);

        Transform3D t = new Transform3D();

        if (nodePoint.toString().equals(parentPoint.toString())) {
            System.out.println("parent and node were equal");
            return tg;
        }

        Point3D newYAxis = new Point3D(nodePoint.getX() - parentPoint.getX(), nodePoint.getY() - parentPoint.getY(), nodePoint.getZ() - parentPoint.getZ());


        //calculate new X and Z vector (orthogonal)
        Point3D newXAxis;
        Point3D newZAxis;
        //check if newY is parallel to one of the old axis
        if (newYAxis.getX() == 0 && newYAxis.getY() == 0) {
            //Y axis is now Z axis
            newXAxis = new Point3D(0, 1.0f, 0);
            newZAxis = new Point3D(1.0f, 0, 0);
        } else if (newYAxis.getY() == 0 && newYAxis.getZ() == 0) {
            //Y axis is now X axis
            newXAxis = new Point3D(0, 0, 1.0f);
            newZAxis = new Point3D(0, 1.0f, 0);
        } else if (newYAxis.getZ() == 0 && newYAxis.getX() == 0) {
            //Y axis is still Y axis
            newXAxis = new Point3D(1.0f, 0, 0);
            newZAxis = new Point3D(0, 0, 1.0f);
        } else {
            //höchstens eine 0 im vektor der neuen y Achse
            newXAxis = new Point3D(-newYAxis.getY(), newYAxis.getX(), 0);
            newZAxis = new Point3D(newYAxis.getY() * newXAxis.getZ() - newYAxis.getZ() * newXAxis.getY(),
                    newYAxis.getZ() * newXAxis.getX() - newYAxis.getX() * newXAxis.getZ(),
                    newYAxis.getX() * newXAxis.getY() - newYAxis.getY() * newXAxis.getX());
        }


        //build matrix

        Matrix3f matrix = new Matrix3f(newXAxis.getX() / newXAxis.vectorLength(), newYAxis.getX() / newYAxis.vectorLength(), newZAxis.getX() / newZAxis.vectorLength(),
                newXAxis.getY() / newXAxis.vectorLength(), newYAxis.getY() / newYAxis.vectorLength(), newZAxis.getY() / newZAxis.vectorLength(),
                newXAxis.getZ() / newXAxis.vectorLength(), newYAxis.getZ() / newYAxis.vectorLength(), newZAxis.getZ() / newZAxis.vectorLength());
        t.set(matrix, new Vector3f((float) (node.getParent().getPoint().getX() + 0.5 * vector.getX()), (float) (node.getParent().getPoint().getY() + 0.5 * vector.getY()), (float) (node.getParent().getPoint().getZ() + 0.5 * vector.getZ())), 1.0f);


        tg.setTransform(t);

        TruncatedCone branch = new TruncatedCone(thicknessTop, thicknessBot, (float) (node.getPoint().distance(node.getParent().getPoint())), branchAppearance, flags);
//        if (DEBUG)
//            branch.setAppearance(debugAppearance);
//        else
//            branch.setAppearance(branchAppearance);

        tg.addChild(branch);

        return tg;
    }

    public static void visualizeSun(SunPosition sunPos) {
        view.setSun(sunPos);
    }
}
