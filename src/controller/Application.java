package controller;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import model.*;
import view.Point3D;
import view.View;
import view.ViewInterface;

import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class Application {

    private static float treeNodeSize = 0.01f;
    private static float attPointNodeSize = 0.015f;
    private static ViewInterface view;

    private static int step = 1; //every x step is visualized
    private static int steps = 50; //number of space colonization iterations
    private static Appearance branchAppearance;

    public static void main(String[] args) throws InterruptedException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        view = new View((int)(screenSize.getWidth()),(int)(screenSize.getHeight()));
        new MainFrame( (View)view, (int) (screenSize.getWidth()), (int) (screenSize.getHeight()) );

        branchAppearance = new Appearance();
        Color3f brown = new Color3f(0.318f, 0.212f, 0.051f);
        Color3f black = new Color3f(0, 0, 0);
        branchAppearance.setMaterial(new Material(brown,brown,brown,black,70f));


        Tree tree = new Tree(TreeType.TREE, 5.0);

        SpaceColonization colo = new SpaceColonization();
        log("generating point cloud");
        PointCloud cloud = colo.generatePointCloud(tree.getType(),tree.getHeight());

        log("starting space colonization");
        int i = 1;
        while(colo.spaceColonize(tree,cloud)){

            log("   step " + i + "/" + steps);
            if(i%step==0) {
                putBranches(tree);
//                putAttractionPoints(cloud);
            }
            Thread.sleep(10);

            if(i >= steps)
                break;

            i++;

        }
        log("finished");

//        putDummy();
//        putNodes(tree);
//        putBranches(tree);
//        putBranchesTopDown(tree);
//        putAttractionPoints(cloud);

//        test();
    }

    @Deprecated
    private static void putBranchesTopDown(Tree tree) throws InterruptedException {
        KDParentTree branches = tree.getNodes();
        BranchGroup bg = new BranchGroup();
        bg.setCapability(BranchGroup.ALLOW_DETACH);//TODO weg nach dem debug

        Iterator<List<KDParentTreeNode>> iterator = branches.iterator();

        float thickness = 0.001f;

        view.resetTree();//TODO weg nach debug

        for(KDParentTreeNode branch : branches.getLeaves()){
            bg.addChild(buildCylinder(branch,thickness));
        }
        view.addToTree(bg);//TODO weg nach debug
        Thread.sleep(1000);//TODO weg nach debug

        thickness += 0.0019f;

        List<KDParentTreeNode> layer;
        int i = 0;
        int countBranches = 0;

        Random random = new Random();
        Color3f black = new Color3f(0, 0, 0);

        while(iterator.hasNext()){
            bg = new BranchGroup();//TODO weg nach debug
            bg.setCapability(BranchGroup.ALLOW_DETACH);//TODO weg nach dem debug

            Appearance debugAppearance = new Appearance();
            Color3f rand = new Color3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
            debugAppearance.setMaterial(new Material(rand,rand,rand,black,70f));

            layer = iterator.next();
            countBranches += layer.size();
            i++;
            for(KDParentTreeNode branch : layer){
                bg.addChild(debugBuildCylinder(branch,thickness,debugAppearance));
            }
            thickness += 0.0019f;
            view.addToTree(bg);//TODO weg nach debug
            Thread.sleep(1000);//TODO weg nach debug






        }
//        log("      processed " + i + " layers with " + countBranches + " branches");
//        view.resetTree();
//        bg.setCapability(BranchGroup.ALLOW_DETACH);
//        view.addToTree(bg);
//        log("      displayed " + i + " layers with " + countBranches + " branches");
    }

    /**
     * Creates Sphere for every attraction point of the pointcloud and passes them to the view.
     * @param cloud
     */
    private static void putAttractionPoints(PointCloud cloud){

        List<Point3D> nodes = cloud.getAttractionPoints();
        BranchGroup bg = new BranchGroup();

        Appearance app = new Appearance();
        Color3f green = new Color3f(Color.BLUE);
        app.setMaterial(new Material(green,green,green,green,70f));

        nodes.forEach(point -> {
            TransformGroup tg = new TransformGroup();
            Transform3D t = new Transform3D();

            t.setTranslation(new Vector3d(point.getX(),point.getY(),point.getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(attPointNodeSize);
            sphere.setAppearance(app);

            tg.addChild(sphere);
            bg.addChild(tg);
        });

        view.resetNodes();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToNodes(bg);
    }

    /**
     * Creates Sphere for every node of the tree and passes them to the view.
     * @param tree
     */
    private static void putNodes(Tree tree){

        KDParentTreeNode node = tree.getNodes().getRoot();
        BranchGroup bg = new BranchGroup();

        if(node==null){
            return;
        }

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(node);

        TransformGroup tg;
        Transform3D t;

        Appearance app = new Appearance();
        Color3f green = new Color3f(0.149f, 0.376f, 0.075f);
        app.setMaterial(new Material(green,green,green,green,70f));

        while (!nodes.isEmpty()) {

            KDParentTreeNode tmp = nodes.pop();

            //view
            tg = new TransformGroup();
            t = new Transform3D();

            t.setTranslation(new Vector3d(tmp.getPoint().getX(),tmp.getPoint().getY(),tmp.getPoint().getZ()));
            tg.setTransform(t);

            Sphere sphere = new Sphere(treeNodeSize);
            sphere.setAppearance(app);

            tg.addChild(sphere);
            bg.addChild(tg);


            if (tmp.getLtbChild() != null) {
                nodes.push(tmp.getLtbChild());
            }
            if (tmp.getRbfChild() != null) {
                nodes.push(tmp.getRbfChild());
            }
        }

        view.addToNodes(bg);

    }

    /**
     * Creates branches for the tree and passes them to the view.
     * @param tree
     */
    private static void putBranches(Tree tree){

        tree.getNodes().calculateThicknesses(0.0025f, 2.5); //TODO parameter


        KDParentTreeNode node = tree.getNodes().getRoot();
        BranchGroup bg = new BranchGroup();

        if(node==null){
            return;
        }

        Stack<KDParentTreeNode> nodes = new Stack<>();

        nodes.push(node);

        int nodeCounter = 1;

        while (!nodes.isEmpty()) {

            KDParentTreeNode tmp = nodes.pop();

            bg.addChild(buildCylinder(tmp,tmp.getThickness()));

            if (tmp.getLtbChild() != null) {
                nodes.push(tmp.getLtbChild());
                nodeCounter++;
            }
            if (tmp.getRbfChild() != null) {
                nodes.push(tmp.getRbfChild());
                nodeCounter++;

            }
        }
        log("      processed " + nodeCounter + " nodes");

        view.resetTree();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToTree(bg);

//        log("      displayed " + nodeCounter + " nodes");

    }

    private static TransformGroup debugBuildCylinder(KDParentTreeNode node, float thickness, Appearance appearance) {

        TransformGroup tg = new TransformGroup();

        KDParentTreeNode parent = node.getParent();
        if(parent==null)
            return tg;


        Point3D parentPoint = parent.getPoint();
        Point3D nodePoint = node.getPoint();

        Point3D vector = nodePoint.subtract(parentPoint);

        Transform3D t = new Transform3D();

        if(nodePoint.toString().equals(parentPoint.toString())) {
            System.out.println("parent and node were equal");
            return tg;
        }
//        System.out.println("node" + nodePoint.toSTring());
//        System.out.println("parent" + parentPoint.toSTring());

        Point3D newYAxis = new Point3D(nodePoint.getX() - parentPoint.getX(), nodePoint.getY() - parentPoint.getY(), nodePoint.getZ() - parentPoint.getZ());

        //calculate new X and Z vector (orthogonal)
        Point3D newXAxis;
        Point3D newZAxis;
        //check if newY is parallel to one of the old axis
        if(newYAxis.getX()==0 && newYAxis.getY()==0){
            //Y axis is now Z axis
            newXAxis = new Point3D(0,1.0f,0);
            newZAxis = new Point3D(1.0f,0,0);
        }else if(newYAxis.getY()==0 && newYAxis.getZ()==0){
            //Y axis is now X axis
            newXAxis = new Point3D(0,0,1.0f);
            newZAxis = new Point3D(0,1.0f,0);
        }else if(newYAxis.getZ()==0 && newYAxis.getX()==0){
            //Y axis is still Y axis
            newXAxis = new Point3D(1.0f,0,0);
            newZAxis = new Point3D(0,0,1.0f);
        }else{
            //höchstens eine 0 im vektor der neuen y Achse
            newXAxis = new Point3D(-newYAxis.getY(), newYAxis.getX(), 0);
            newZAxis = new Point3D(newYAxis.getY()*newXAxis.getZ() - newYAxis.getZ()*newXAxis.getY(),
                    newYAxis.getZ()*newXAxis.getX() - newYAxis.getX()*newXAxis.getZ(),
                    newYAxis.getX()*newXAxis.getY() -newYAxis.getY()*newXAxis.getX());
        }

        //build matrix

        Matrix3f matrix = new Matrix3f(newXAxis.getX()/newXAxis.vectorLength(), newYAxis.getX()/newYAxis.vectorLength(), newZAxis.getX()/newZAxis.vectorLength(),
                newXAxis.getY()/newXAxis.vectorLength(), newYAxis.getY()/newYAxis.vectorLength(), newZAxis.getY()/newZAxis.vectorLength(),
                newXAxis.getZ()/newXAxis.vectorLength(), newYAxis.getZ()/newYAxis.vectorLength(), newZAxis.getZ()/newZAxis.vectorLength());
        t.set(matrix, new Vector3f((float)(node.getParent().getPoint().getX() + 0.5*vector.getX()), (float)(node.getParent().getPoint().getY() + 0.5*vector.getY()), (float)(node.getParent().getPoint().getZ() + 0.5*vector.getZ())),1.0f);

//        System.out.println("new y axis" + newYAxis.toSTring());
//        System.out.println(matrix.toString());

        tg.setTransform(t);

        Cylinder branch = new Cylinder(thickness,(float)(node.getPoint().distance(node.getParent().getPoint())));
        branch.setAppearance(appearance);

        tg.addChild(branch);

        return tg;
    }

    /**
     * Returns Transformgroup containing one cylinder which represents the branch between node and its parent.
     *
     * @param node
     * @return
     */
    private static TransformGroup buildCylinder(KDParentTreeNode node, float thickness) {

        TransformGroup tg = new TransformGroup();

        KDParentTreeNode parent = node.getParent();
        if(parent==null)
            return tg;


        Point3D parentPoint = parent.getPoint();
        Point3D nodePoint = node.getPoint();

        Point3D vector = nodePoint.subtract(parentPoint);

        Transform3D t = new Transform3D();

        if(nodePoint.toString().equals(parentPoint.toString())) {
            System.out.println("parent and node were equal");
            return tg;
        }
//        System.out.println("node" + nodePoint.toSTring());
//        System.out.println("parent" + parentPoint.toSTring());

        Point3D newYAxis = new Point3D(nodePoint.getX() - parentPoint.getX(), nodePoint.getY() - parentPoint.getY(), nodePoint.getZ() - parentPoint.getZ());

        //calculate new X and Z vector (orthogonal)
        Point3D newXAxis;
        Point3D newZAxis;
        //check if newY is parallel to one of the old axis
        if(newYAxis.getX()==0 && newYAxis.getY()==0){
            //Y axis is now Z axis
            newXAxis = new Point3D(0,1.0f,0);
            newZAxis = new Point3D(1.0f,0,0);
        }else if(newYAxis.getY()==0 && newYAxis.getZ()==0){
            //Y axis is now X axis
            newXAxis = new Point3D(0,0,1.0f);
            newZAxis = new Point3D(0,1.0f,0);
        }else if(newYAxis.getZ()==0 && newYAxis.getX()==0){
            //Y axis is still Y axis
            newXAxis = new Point3D(1.0f,0,0);
            newZAxis = new Point3D(0,0,1.0f);
        }else{
            //höchstens eine 0 im vektor der neuen y Achse
            newXAxis = new Point3D(-newYAxis.getY(), newYAxis.getX(), 0);
            newZAxis = new Point3D(newYAxis.getY()*newXAxis.getZ() - newYAxis.getZ()*newXAxis.getY(),
                    newYAxis.getZ()*newXAxis.getX() - newYAxis.getX()*newXAxis.getZ(),
                    newYAxis.getX()*newXAxis.getY() -newYAxis.getY()*newXAxis.getX());
        }

        //build matrix

        Matrix3f matrix = new Matrix3f(newXAxis.getX()/newXAxis.vectorLength(), newYAxis.getX()/newYAxis.vectorLength(), newZAxis.getX()/newZAxis.vectorLength(),
                newXAxis.getY()/newXAxis.vectorLength(), newYAxis.getY()/newYAxis.vectorLength(), newZAxis.getY()/newZAxis.vectorLength(),
                newXAxis.getZ()/newXAxis.vectorLength(), newYAxis.getZ()/newYAxis.vectorLength(), newZAxis.getZ()/newZAxis.vectorLength());
        t.set(matrix, new Vector3f((float)(node.getParent().getPoint().getX() + 0.5*vector.getX()), (float)(node.getParent().getPoint().getY() + 0.5*vector.getY()), (float)(node.getParent().getPoint().getZ() + 0.5*vector.getZ())),1.0f);

//        System.out.println("new y axis" + newYAxis.toSTring());
//        System.out.println(matrix.toString());

        tg.setTransform(t);

        Cylinder branch = new Cylinder(thickness,(float)(node.getPoint().distance(node.getParent().getPoint())));
        branch.setAppearance(branchAppearance);

        tg.addChild(branch);

        return tg;
    }

    private static void log(String s){
        view.log(s);
    }

}
