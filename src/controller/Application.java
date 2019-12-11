package controller;

import com.google.gson.Gson;
import com.sun.istack.internal.Nullable;
import model.*;
import org.jogamp.java3d.*;
import org.jogamp.java3d.utils.applet.MainFrame;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.image.TextureLoader;
import org.jogamp.vecmath.*;
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

public class Application extends Applet {

    private static final float ATT_POINT_NODE_SIZE = 0.015f;
    private static final double THICKNESS_N = 2.1;
    private static final float INIT_BRANCH_THICKNESS = 0.0025f;
    private static ViewInterface view;

    private static final int STEP = 1; //every x STEP is visualized
    private static final int STEPS = 300; //max number of space colonization iterations
    private static final long DELAY = 0;
    private static final boolean DEBUG = false;
    private static final boolean SAVED = true;
    private static final String NEXTFILE = "exp";
    private static final String SAVEFILE = "t2"; //clud4m, cloud4m, cloud6m, cloud2,
    private static ShaderAppearance branchAppearance;

    private static Appearance obstacleAppearance;


    private static void run() throws InterruptedException {

        Point3D verschiebung = new Point3D(0, 0, 0);

        Tree tree = new Tree(TreeType.PLATANE, 4.0, verschiebung);
//        Tree tree = new Tree(TreeType.KORKHASE, 3.0, verschiebung);

        //obstacles
        List<Obstacle> obstacles = new ArrayList<>();
        //onw building
        Building southBuilding = new Building("south", new Point3D(-1.5f, 0, 2.0f), new Point3D(1.5f, 3, 0.5f));
        obstacles.add(southBuilding);

        //east building
//        Building eastBuilding = new Building("east", new Point3D(1.0f, 0, -3.5f), new Point3D(2.0f, 3.0f, 3.5f));
//        obstacles.add(eastBuilding);

        //west building
        Building westBuilding = new Building("west", new Point3D(-2.0f, 0, -1.5f), new Point3D(-1.0f, 3.0f, 1.5f));
        obstacles.add(westBuilding);

        //north
//        Building northBuilding = new Building("north", new Point3D(-1.5f, 0, -2.0f), new Point3D(1.5f, 3, -1.0f));
//        obstacles.add(northBuilding);

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
            } catch (IOException e) {
                e.printStackTrace();
            }
            cloud.setAttractionPoints(aps);
        } else {
            cloud = colo.generatePointCloud(tree);
            Gson gson = new Gson();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\Users\\JanaJ\\IdeaProjects\\jana.jansen\\" + NEXTFILE + ".txt"));
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

//        cloud.intersectWithObstacles(obstacles);
        cloud.updateWithObstacles(obstacles);

        ViewInterface.log("starting space colonization");
        long start = System.currentTimeMillis();
        int i = 1;

        while (colo.spaceColonize(tree, cloud, obstacles)) {

            ViewInterface.log("   step " + i + "/" + STEPS);
            if (i % STEP == 0) {
//                putBranches(tree);
//                putAttractionPoints(cloud);
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

        Texture loader = new TextureLoader(View.class.getClassLoader().getResource("bark001-color.jpg").getPath(), ((View) view).getComponent(0)).getTexture();
        branchAppearance.setTexture(loader);
        TexCoordGeneration generation = new TexCoordGeneration(TexCoordGeneration.EYE_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2);
        generation.setEnable(true);
        branchAppearance.setTexCoordGeneration(generation);

        putBranches(tree);
//        putAttractionPoints(cloud);


        ViewInterface.log("\n");

        stats("iterations", String.valueOf(i));
        stats("duration", format(duration));
        stats("type", tree.getType().name());
        stats("height", String.valueOf(tree.getHeight()));
        System.out.println("real height: " + tree.getRealHeight());
        if(SAVED){
            stats("file", SAVEFILE);
        }else{
            stats("file", "new");
        }

        colo.stats();
        calculateStats(tree);
        stats("avg#nodes light: ", tree.nodesInLight(obstacles)+"");
        stats("avg light area: ", tree.calculateBoundsPercentDings(obstacles)+"");

        obstacles.forEach(obstacle -> printToStats(obstacle.getName() + ": " + obstacle.getCentroid().cardinalString()));
        stats("null", null);
    }

    private static void initi(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        view = new View((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
        new MainFrame((View) view, (int) (screenSize.getWidth()), (int) (screenSize.getHeight()));

        branchAppearance = new ShaderAppearance();
        branchAppearance.setCapability(ShaderAppearance.ALLOW_SHADER_PROGRAM_WRITE);
        Color3f brown = new Color3f(0.318f, 0.212f, 0.051f);
        Color3f black = new Color3f(0, 0, 0);
        Color3f white = new Color3f(255,255,255);
        Color3f darkbrown = new Color3f(0.106f, 0.0314f, 0.0118f);
        Color3f lightbrown = new Color3f(1/255.0f*222,1/255.0f*184,1/255.0f*135);
//        branchAppearance.setMaterial(new Material(brown, brown, black, black, 70f));
        branchAppearance.setMaterial(new Material(brown, darkbrown, brown, lightbrown, 10f));
//        app.setMaterial(new Material(objColor, black, objColor, white, 80.0f));

//        branchAppearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
//        branchAppearance.setCapability(Appearance.ALLOW_TEXGEN_WRITE);


        obstacleAppearance = new Appearance();
        Color3f gray = new Color3f(Color.gray.getRed(),Color.gray.getGreen(),Color.gray.getBlue());
        obstacleAppearance.setMaterial(new Material(gray, black, gray, white, 110f));

        //coordinates
//        view.addMarker(0, 0, 0, new Color3f(Color.black.getRed(),Color.black.getGreen(),Color.black.getBlue()), 0.02f);
        view.addLine(new Point3D(0,0,0), new Point3D(10,0,0), Color.blue);
//        view.addMarker(0.5f, 0, 0, new Color3f(Color.blue), 0.04f);
        view.addLine(new Point3D(0,0,0), new Point3D(0,10,0), Color.green);
//        view.addMarker(0, 0.5f, 0, new Color3f(Color.green), 0.04f);
        view.addLine(new Point3D(0,0,0), new Point3D(0,0,10), Color.red);
//        view.addMarker(0, 0, 0.5f, new Color3f(Color.red), 0.04f);
    }

    public static void main(String[] args) throws InterruptedException {

//        IOController.analyzeCSV();

        initi();
        run();

        Bounds bounds = view.getTreeBounds();

//        Point3D drehachse = new Point3D(1,2,0.5f);
//        Point3f[] points = new Point3f[]{new Point3f(-1,1,0), new Point3f(0,1,-1), new Point3f(1,1,0), new Point3f(0,1,1)};
//        for (Point3f point : points) {
//            view.addMarker(point.getX(),point.getY(),point.getZ(),new Color3f(1,1,1), 0.05f);
//        }
//        TruncatedCone.transform(new Point3f(0,1,0), points, drehachse, Math.toRadians(40));
//        for (Point3f point : points) {
//            view.addMarker(point.getX(),point.getY(),point.getZ(),new Color3f(0,0,0), 0.05f);
//        }



//        Tree tree = new Tree(TreeType.PLATANE, 1.0f, new Point3D(0,0,-0.1f));
//
//        KDParentTreeNode node1 = tree.getNodes().getRoot();
//        tree.getNodes().insert(new Point3D(0,0.2f,-0.1f), node1);
//        KDParentTreeNode node2 = node1.getTreeChildren().get(0);
//        tree.getNodes().insert(new Point3D(0,0.3f,-0.3f), node2);
//        KDParentTreeNode node3 = node2.getTreeChildren().get(0);
//        tree.getNodes().insert(new Point3D(0,0.5f,-0.3f), node3);
//        KDParentTreeNode node4 = node3.getTreeChildren().get(0);
//        tree.getNodes().insert(new Point3D(0,0.6f,-0.1f), node4);
//        KDParentTreeNode node5 = node4.getTreeChildren().get(0);
//
//        tree.getNodes().calculateThicknesses(0.03f, THICKNESS_N);
//        tree.calculateDiscs();
//
//
//        KDParentTreeNode[] nodes = new KDParentTreeNode[]{node1, node2, node3, node4, node5};
//
//        BranchGroup bg = new BranchGroup();
//
//        TruncatedCone[] cones = new TruncatedCone[4];
//        for (int i = 0; i < cones.length; i++) {
//            cones[i] = new TruncatedCone(nodes[i+1], (float)nodes[i+1].getPoint().distance(nodes[i+1].getParent().getPoint()), branchAppearance, TruncatedCone.BODY | TruncatedCone.TOP | TruncatedCone.BOT);
//        }
//
//        for (int i = 0; i < cones.length; i++) {
//            TransformGroup tg1 = new TransformGroup();
//            Transform3D t1 = transform(nodes[i].getPoint(), nodes[i+1].getPoint());
//            tg1.setTransform(t1);
//            tg1.addChild(cones[i]);
//            bg.addChild(tg1);
//            view.addLine(nodes[i].getPoint(), nodes[i+1].getPoint(), Color.red);
//        }
//
//
//
//        view.addToTree(bg);



//        Point3D angle = new Point3D(-1f,0,1);
//        System.out.println(angle.azimuthDegree());
//        System.out.println(angle.elevationDegree());

    }

    private static void calculateStats(Tree tree) {

        //angle
        Point3D angle = tree.calculateAngle();
        stats("azimuth degr", angle.azimuthDegree());
        stats("elevation degr", angle.elevationDegree());
        Point3D zero = new Point3D(0, 0, 0);
        view.setLine(zero, zero.add(angle.mult(tree.getHeight())));

        //number of nodes
        stats("number of nodes", String.valueOf(tree.getNodes().getAll().size()));

        //number of branches
        stats("number of branches", String.valueOf(tree.calculateBranches()));

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
        Color3f blue = new Color3f(Color.BLUE.getRed(),Color.BLUE.getGreen(),Color.BLUE.getBlue());
        app.setMaterial(new Material(blue, blue, blue, blue, 70f));

        Appearance app2 = new Appearance();
        Color3f darkblue = new Color3f(Color.YELLOW.getRed(),Color.YELLOW.getGreen(),Color.YELLOW.getBlue());
        app2.setMaterial(new Material(darkblue, darkblue, darkblue, darkblue, 70f));

        nodes.forEach(point -> {
            TransformGroup tg = new TransformGroup();
            Transform3D t = new Transform3D();

            t.setTranslation(new Vector3d(point.getX(), point.getY(), point.getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(ATT_POINT_NODE_SIZE);
            if(point.isActivated()){

                sphere.setAppearance(app);
            }
            else {
                sphere.setAppearance(app2);

            }
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
        tree.calculateDiscs();


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
                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY | TruncatedCone.BOT));
            else if (tree.getNodes().getLeaves().contains(tmp))
                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY | TruncatedCone.TOP));
            else
                bg.addChild(buildCylinder(tmp, TruncatedCone.BODY /*| TruncatedCone.TOP | TruncatedCone.BOT*/)); //mit top und bot für mehr beauty


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

        KDParentTreeNode parent = node.getParent();
        if (parent == null)
            return tg;

//        view.addLine(parent.getPoint(), node.getPoint(), Color.red);

        Transform3D t = transform(parent.getPoint(), node.getPoint());

        tg.setTransform(t);

        TruncatedCone branch = new TruncatedCone(node, (float) (node.getPoint().distance(node.getParent().getPoint())), branchAppearance, flags);

        tg.addChild(branch);

        return tg;
    }

    public static void visualizeSun(SunPosition sunPos) {
        view.setSun(sunPos);
    }

    private static Transform3D transform(Point3D parentPoint, Point3D nodePoint){

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

        Point3D yAchse = new Point3D(0,1,0);
        vector.normalize();
        Point3D drehachse = yAchse.cross(vector);
        double alpha = Math.acos(yAchse.dot(vector) / (yAchse.vectorLength() * vector.vectorLength()));
        drehachse.normalize();

        if(alpha == 0){
            t.setTranslation(new Vector3d((parentPoint.getX() /*+ 0.5 * vector.getX()*/), (parentPoint.getY() /*+ 0.5 * vector.getY()*/),  (parentPoint.getZ() /*+ 0.5 * vector.getZ()*/)));
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



        t.set(matrix, new Vector3d((parentPoint.getX() /*+ 0.5 * vector.getX()*/), (parentPoint.getY() /*+ 0.5 * vector.getY()*/),  (parentPoint.getZ() /*+ 0.5 * vector.getZ()*/)), 1.0f);

        return t;
    }
}
