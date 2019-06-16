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
import java.util.Stack;

class Application {

    private static float treeNodeSize = 0.01f;
    private static float attPointNodeSize = 0.015f;
    private static ViewInterface view;

    private static int step = 5; //every x step is visualized
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
                putBranchesTopDown(tree);

//                putAttractionPoints(cloud);
            }
            Thread.sleep(1000);

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

    // berücksichtigt Dicke
    private static void putBranchesTopDown(Tree tree) {
        KDParentTree branches = tree.getNodes();
        BranchGroup bg = new BranchGroup();

        Iterator<List<KDParentTreeNode>> iterator = branches.iterator();

        float thickness = 0.001f;

        for(KDParentTreeNode branch : branches.getLeaves()){
            bg.addChild(buildCylinder(branch,thickness));
        }
        thickness += 0.0019f;

        List<KDParentTreeNode> layer;
        int i = 0;
        int countBranches = 0;
        while(iterator.hasNext()){
            layer = iterator.next();
            countBranches += layer.size();
            i++;
            for(KDParentTreeNode branch : layer){
                bg.addChild(buildCylinder(branch,thickness));
            }
            thickness += 0.0019f;

        }
        log("      processed " + i + " layers with " + countBranches + " branches");
        view.resetTree();
        bg.setCapability(BranchGroup.ALLOW_DETACH);
        view.addToTree(bg);
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
        KDParentTreeNode node = tree.getNodes().getRoot();
        BranchGroup bg = new BranchGroup();

        if(node==null){
            return;
        }

        Stack<KDParentTreeNode> nodes = new Stack<>();
        nodes.push(node);

        while (!nodes.isEmpty()) {

            KDParentTreeNode tmp = nodes.pop();

            bg.addChild(buildCylinder(tmp,0.01f));

            if (tmp.getLtbChild() != null) {
                nodes.push(tmp.getLtbChild());
            }
            if (tmp.getRbfChild() != null) {
                nodes.push(tmp.getRbfChild());
            }
        }

        view.addToTree(bg);
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

    @Deprecated
    private static void putDummy(){

        final float stemLength = 0.9f;
        final float stemRadius = 0.1f;

        Transform3D tf = new Transform3D();

        tf.setTranslation(new Vector3d(0,stemLength/2,0));

        TransformGroup tg = new TransformGroup(tf);

        Appearance app = new Appearance();

        Color3f objColor = new Color3f(0.8f, 0.2f, 1.0f);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);

        //ambient, emissive, diffuse, specular, shininess
        app.setMaterial(new Material(objColor, black, objColor, black, 80.0f));


        Cylinder cylinder = new Cylinder(stemRadius, stemLength);

        tg.addChild(cylinder);

        TransformGroup tg2 = new TransformGroup();
        Transform3D t = new Transform3D();

        t.setTranslation(new Vector3d(0,1.0,0));
        tg.setTransform(t);

        tg.addChild(new Sphere(0.2f));

        BranchGroup bg = new BranchGroup();
        bg.addChild(tg);
        bg.addChild(tg2);
        view.addToTree(bg);
    }

    /**
     * Test of transformation with a matrix.
     * Transforms y axis to vector of two given points.
     */
    @Deprecated
    private static void test() {

        BranchGroup bg = new BranchGroup();

        Point3D parent = new Point3D(2,5,4);
        Point3D node = new Point3D(3,6,2);

        Point3D vector = node.subtract(parent);


        TransformGroup tg1 = new TransformGroup();
        Transform3D t1 = new Transform3D();

        t1.setTranslation(new Vector3d(parent.getX(),parent.getY(),parent.getZ()));
        tg1.setTransform(t1);

        tg1.addChild(new Sphere(0.04f));
        bg.addChild(tg1);





        TransformGroup tg2 = new TransformGroup();
        Transform3D t2 = new Transform3D();

        t2.setTranslation(new Vector3d(node.getX(),node.getY(),node.getZ()));
        tg2.setTransform(t2);

        tg2.addChild(new Sphere(0.04f));
        bg.addChild(tg2);




        TransformGroup tg = new TransformGroup();
        Transform3D tr = new Transform3D();
//        tr.setTranslation(new Vector3d(0.0,0.1,0.0));

//        Matrix3d matrix = new Matrix3d(-1,1,0,1,1,-2,2,2,2);
        Matrix3d matrix = new Matrix3d(-1/Math.sqrt(2),1/Math.sqrt(6),2/Math.sqrt(12),1/Math.sqrt(2),1/Math.sqrt(6),2/Math.sqrt(12),0/Math.sqrt(2),-2/Math.sqrt(6),2/Math.sqrt(12));

        tr.set(new Matrix3f(matrix),new Vector3d(parent.getX() + 0.5*vector.getX(), parent.getY() + 0.5*vector.getY(), parent.getZ() + 0.5*vector.getZ()),1.0f);


        tg.setTransform(tr);

        Cylinder cy = new Cylinder(0.05f,(float)node.distance(parent));
        tg.addChild(cy);




        bg.addChild(new Cylinder(0.05f,0.08f));

        bg.addChild(tg);
        view.addToNodes(bg);
    }

    private static void log(String s){
        view.log(s);
    }

}
