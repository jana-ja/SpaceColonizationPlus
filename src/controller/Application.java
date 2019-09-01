package controller;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import model.*;
import view.Point3D;
import view.View;
import view.ViewInterface;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.applet.Applet;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class Application extends Applet {

    private static final float ATT_POINT_NODE_SIZE = 0.015f;
    private static final double THICKNESS_N = 2.1;
    private static final float INIT_BRANCH_THICKNESS = 0.0025f;
    private static ViewInterface view;

    private static final int STEP = 1; //every x STEP is visualized
    private static final int STEPS = 400; //number of space colonization iterations
    private static final long DELAY = 0;
    private static final boolean DEBUG = false;
    private static Appearance branchAppearance;


    public static void main(String[] args) throws InterruptedException {

//        IOController.analyzeCSV();


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        view = new View((int) (screenSize.getWidth()), (int) (screenSize.getHeight()));
        new MainFrame((View) view, (int) (screenSize.getWidth()), (int) (screenSize.getHeight()));

        branchAppearance = new Appearance();
        Color3f brown = new Color3f(0.318f, 0.212f, 0.051f);
        Color3f black = new Color3f(0, 0, 0);
//        branchAppearance.setMaterial(new Material(brown, brown, brown, black, 70f));

        Texture loader = new TextureLoader( View.class.getClassLoader().getResource("bark004-color.jpg").getPath(), ((View) view).getComponent(0)  ).getTexture( );
        branchAppearance.setTexture(loader);
        TexCoordGeneration generation = new TexCoordGeneration(TexCoordGeneration.EYE_LINEAR, TexCoordGeneration.TEXTURE_COORDINATE_2);
        generation.setEnable(true);
        branchAppearance.setTexCoordGeneration(generation);

        Point3D verschiebung = new Point3D(0,0,0);

        Tree tree = new Tree(TreeType.PLATANE, 4.0, verschiebung);

        SpaceColonization colo = new SpaceColonization();
        ViewInterface.log("generating point cloud");

        PointCloud cloud = colo.generatePointCloud(tree);

//        TruncatedCone test = new TruncatedCone(0.1f, 1.0f, 1f, branchAppearance);
//        BranchGroup bg = new BranchGroup();
//        bg.addChild(test);
//        view.addToNodes(bg);

        ViewInterface.log("starting space colonization");
        int i = 1;

        while(colo.spaceColonize(tree,cloud)){

            ViewInterface.log("   step " + i + "/" + STEPS);
            if(i% STEP ==0) {
                putBranches(tree);
                putAttractionPoints(cloud);
            }
            Thread.sleep(DELAY);

            if(i >= STEPS)
                break;

            i++;

        }
        ViewInterface.log("finished");

//        putBranches(tree);
//        putAttractionPoints(cloud);




//        float xForMaxDistance;
//        float xOhneVerschiebung;
//        double treeWidth = tree.getType().getWidthPerHeight() * tree.getHeight();
//        System.out.println(treeWidth);
//        double thickness = 0.1;
//        float xForMinDistance;
//        double treeHeight = tree.getHeight();
//        TreeType type = tree.getType();
//        double crownHeight = treeHeight * type.getTopPercentage() / 100;
//
//
//        double treeTopY = verschiebung.getY() + treeHeight;
//        double fs = verschiebung.getY() + treeHeight - 2 * type.getTopPercentage() / 100 * treeHeight;
//        double fsOhneVerschiebung = treeHeight - 2 * type.getTopPercentage() / 100 * treeHeight;
//
//        view.addMarker(verschiebung.getX(), verschiebung.getY() + (float) (tree.getHeight() - tree.getType().getTopPercentage()/100*tree.getHeight()), verschiebung.getZ());
//        view.addMarker(verschiebung.getX(), verschiebung.getY() + (float)tree.getHeight(), verschiebung.getZ());
//        view.addMarker(verschiebung.getX(), (float) fs, verschiebung.getZ());

//        for (double i = fs; i <= verschiebung.getY() + tree.getHeight(); i += 0.2) {
//
//            für cone
//            xForMaxDistance = (float) ((i - treeTopY) * treeWidth / 2 / (crownHeight) + verschiebung.getX());
//            view.addMarker( xForMaxDistance,(float)i, 0.0f, new Color3f(Color.MAGENTA));
//
//
//            für round
//            xForMaxDistance = (float) ((treeWidth / 2) * Math.sin(Math.PI / (treeTopY - fs) * (i - fs)) + verschiebung.getX());
//            view.addMarker(xForMaxDistance, (float)i, 0.0f, new Color3f(Color.MAGENTA));
//
//            xForMinDistance = (float) ((treeWidth / 2) * (1.0 - 2 * thickness) * Math.sin(((Math.PI) / ((treeTopY - fs) * (1.0 - 2 * thickness))) * (i - (fs + (treeTopY - fs) * thickness)))+ verschiebung.getX());
//            view.addMarker(xForMinDistance, (float)i, 0.0f, new Color3f(Color.CYAN));
//        }
//
//        for (double i = fsOhneVerschiebung; i <=  + tree.getHeight(); i += 0.2) {
//            xOhneVerschiebung = (float) ((treeWidth / 2) * Math.sin(Math.PI / (treeHeight - fsOhneVerschiebung) * (i - fsOhneVerschiebung)));
//            view.addMarker(xOhneVerschiebung, (float)i, 0.0f, new Color3f(Color.CYAN));
//        }
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

            if(tmp.equals( tree.getNodes().getRoot()))
                bg.addChild(buildCylinder(tmp, tmp.getThickness(), tmp.getThickness(), TruncatedCone.BODY | TruncatedCone.BOT));
            else if(tree.getNodes().getLeaves().contains(tmp))
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


}
