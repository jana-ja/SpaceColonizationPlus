package view;

import java.applet.Applet;
import java.awt.*;

import javax.media.j3d.*;
import javax.vecmath.*;

import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.image.TextureLoader;

public class View extends Applet implements ViewInterface {

    // size of each Canvas3D
    private static final int cWidth = 1024;
    private static final int cHeight = 1024;


    private final int LAND_WIDTH = 12;
    private final float LAND_HEIGHT = 0.0f;
    private final int LAND_LENGTH = 12;
    private final int nTileSize = 2;

    private final float MARKER_NODE_SIZE = 0.02f;

    private final BranchGroup bgTree;
    private final BranchGroup bgNodes;
    private final BranchGroup sceneNodes;


    private final BoundingSphere bounds = new BoundingSphere(new Point3d(0, 0, 0), 100);


    public View(int screenWidth, int screenHeight) {

        //create View1
        Canvas3D birdsEye = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        birdsEye.setSize((int) (0.4 * screenWidth), (int) (0.4 * screenWidth));
        add(birdsEye);

        // create a ViewingPlatform with 2 TransformGroups above the ViewPlatform
        ViewingPlatform vp = new ViewingPlatform(2);

        // create the Viewer and attach to the first canvas
        Viewer viewer = new Viewer(birdsEye);

        // rotate and position the first Viewer above the environment
        Transform3D t3d = new Transform3D();
//        t3d.lookAt(0,0,0);
        t3d.rotX(-Math.PI / 8.0);
        t3d.rotY(Math.toRadians(180));
        t3d.setTranslation(new Vector3d(0, 3, -6));

        MultiTransformGroup mtg = vp.getMultiTransformGroup();
        mtg.getTransformGroup(0).setTransform(t3d);

        // create a SimpleUniverse from the ViewingPlatform and Viewer
        SimpleUniverse u = new SimpleUniverse(vp, viewer);

        //create View2
        Canvas3D firstPerson = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        firstPerson.setSize((int) (0.4 * screenWidth), (int) (0.4 * screenWidth));
        add(firstPerson);


        text.setPreferredSize(new Dimension((int) (0.8 * screenWidth), (int) (0.8 * (screenHeight - (0.4 * screenWidth)))));
        text.setFont(new Font("Lucida Console", Font.PLAIN, 18));
        Panel panel = new Panel();
        panel.add(text);
        add(panel);

        u.getLocale().addBranchGraph(createViewer(firstPerson, 0, 1, -6)); //TODO sinnvolle location vom viewer aus baummaßen berechnen


        // create background with floor and lights
        u.addBranchGraph(createBackground());

        //create SceneGraph for tree
        bgTree = new BranchGroup();
        bgTree.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        bgTree.setCapability(Group.ALLOW_CHILDREN_READ);
        bgTree.setCapability(Group.ALLOW_CHILDREN_WRITE);
        u.addBranchGraph(bgTree);
        bgNodes = new BranchGroup();
        bgNodes.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        bgNodes.setCapability(Group.ALLOW_CHILDREN_READ);
        bgNodes.setCapability(Group.ALLOW_CHILDREN_WRITE);
        u.addBranchGraph(bgNodes);
        sceneNodes = new BranchGroup();
        sceneNodes.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        u.addBranchGraph(sceneNodes);
    }


    @Override
    public void resetTree() {
        this.bgTree.removeAllChildren();
    }

    @Override
    public void addToTree(BranchGroup bg) {
        this.bgTree.addChild(bg);
    }

    @Override
    public void resetNodes() {
        this.bgNodes.removeAllChildren();
    }

    @Override
    public void addToNodes(BranchGroup bg) {
        this.bgNodes.addChild(bg);
    }

    private BranchGroup createBackground() {

        BranchGroup bgBackground = new BranchGroup();

        addLights(bgBackground);

        // calculate how many vertices we need to store all the "tiles"
        // that compose the QuadArray.
        final int nNumTiles = ((LAND_LENGTH / nTileSize) * 2) * ((LAND_WIDTH / nTileSize) * 2);
        final int nVertexCount = 4 * nNumTiles;
        Point3f[] coordArray = new Point3f[nVertexCount];
        Point2f[] texCoordArray = new Point2f[nVertexCount];

        // create an Appearance and load a texture
        Appearance app = new Appearance();
        Texture tex = new TextureLoader(View.class.getClassLoader().getResource("grass.jpg").getPath(), this).getTexture();
        app.setTexture(tex);

        int nItem = 0;

        // loop over all the tiles in the environment
        for (int x = -LAND_WIDTH; x <= LAND_WIDTH; x += nTileSize) {
            for (int z = -LAND_LENGTH; z <= LAND_LENGTH; z += nTileSize) {

                // if we are not on the last row or column create a "tile"
                // and add to the QuadArray. Use CCW winding and assign texture
                // coordinates.
                if (z < LAND_LENGTH && x < LAND_WIDTH) {
                    coordArray[nItem] = new Point3f(x, LAND_HEIGHT, z);
                    texCoordArray[nItem++] = new Point2f(0, 0);
                    coordArray[nItem] = new Point3f(x, LAND_HEIGHT, z + nTileSize);
                    texCoordArray[nItem++] = new Point2f(1, 0);
                    coordArray[nItem] = new Point3f(x + nTileSize, LAND_HEIGHT, z + nTileSize);
                    texCoordArray[nItem++] = new Point2f(1, 1);
                    coordArray[nItem] = new Point3f(x + nTileSize, LAND_HEIGHT, z);
                    texCoordArray[nItem++] = new Point2f(0, 1);
                }
            }
        }

        // create a GeometryInfo and generate Normal vectors
        // for the QuadArray that was populated.
        GeometryInfo gi = new GeometryInfo(GeometryInfo.QUAD_ARRAY);

        gi.setCoordinates(coordArray);
        gi.setTextureCoordinates(texCoordArray); //TODO deprecated, alternative suchen

        NormalGenerator normalGenerator = new NormalGenerator();
        normalGenerator.generateNormals(gi);

        // wrap the GeometryArray in a Shape3D
        Shape3D shape = new Shape3D(gi.getGeometryArray(), app);

        // add the Shape3D to the parent BranchGroup
        bgBackground.addChild(shape);


        // create a light gray background
        Background back = new Background(new Color3f(rgbToFloat(new int[]{135, 206, 250})));
        back.setApplicationBounds(bounds);
        bgBackground.addChild(back);

        // compile the whole scene
        bgBackground.compile();//TODO wann ist das nötig?


        return bgBackground;
    }

    private ViewingPlatform createViewer(Canvas3D c, double x, double y, double z) {
        // create a Viewer and attach to its canvas
        // a Canvas3D can only be attached to a single Viewer
        Viewer viewer2 = new Viewer(c);

        // create a ViewingPlatform with 1 TransformGroups above the ViewPlatform
        ViewingPlatform vp2 = new ViewingPlatform(1);

        // set the initial position for the Viewer
        Transform3D t3d = new Transform3D();
        t3d.rotY(Math.toRadians(180));
        t3d.setTranslation(new Vector3d(x, y, z));
        vp2.getViewPlatformTransform().setTransform(t3d);

        // set capabilities on the TransformGroup so that the KeyNavigatorBehavior
        // can modify the Viewer's position
        vp2.getViewPlatformTransform().setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        vp2.getViewPlatformTransform().setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

        // attach a navigation behavior to the position of the viewer
        FPKeyNavigatorBehavior key = new FPKeyNavigatorBehavior(vp2.getViewPlatformTransform());
        key.setSchedulingBounds(bounds);
        key.setEnable(true);

        // add the KeyNavigatorBehavior to the ViewingPlatform
        vp2.addChild(key);
        //c.addKeyListener(new FirstPersonCamera(vp2.getViewPlatformTransform()));

        // set the ViewingPlatform for the Viewer
        viewer2.setViewingPlatform(vp2);

        return vp2;
    }

    private BoundingSphere getBoundingSphere() {
        //TODO
        return new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 200.0);
    }

    private void addLights(BranchGroup bg) {

        Color3f dlColor = new Color3f(0.7f, 0.7f, 0.7f);
        Vector3f dir = new Vector3f(0.0f, 1.0f, 1.0f);
        Color3f alColor = new Color3f(0.2f, 0.2f, 0.2f);

        AmbientLight ambLight = new AmbientLight(alColor);
        ambLight.setInfluencingBounds(bounds);
        PointLight pointLight = new PointLight(true, dlColor, new Point3f(0,1.5f,0), new Point3f(5,0,0));
        DirectionalLight dirLight = new DirectionalLight(dlColor, dir);
        dirLight.setInfluencingBounds(bounds);

        // add the lights to the parent BranchGroup
        bg.addChild(ambLight);
        bg.addChild(dirLight);
    }

    private float[] rgbToFloat(int[] rgb) {
        if (rgb.length != 3)
            return new float[]{0, 0, 0};

        float[] floats = new float[3];
        for (int i = 0; i < rgb.length; i++) {
            floats[i] = ((float) (1.0 / 255) * rgb[i]);
        }
        return floats;
    }

    @Override
    public void addMarker(float x, float y, float z) {
        addMarker(x, y, z, new Color3f(Color.RED));
    }

    @Override
    public void addMarker(float x, float y, float z, Color3f color) {
        addMarker(x,y,z,color, MARKER_NODE_SIZE);
    }
    @Override
    public void addMarker(float x, float y, float z, Color3f color, float size) {
        Appearance app = new Appearance();
        BranchGroup bg = new BranchGroup();

        app.setMaterial(new Material(color, color, color, color, 70f));

        TransformGroup tg = new TransformGroup();
        Transform3D t = new Transform3D();

        t.setTranslation(new Vector3d(x, y, z));
        tg.setTransform(t);

        Sphere sphere = new Sphere(size);
        sphere.setAppearance(app);

        tg.addChild(sphere);
        bg.addChild(tg);

        this.sceneNodes.addChild(bg);
    }

    @Override
    public void addToScene(BranchGroup bg){
        this.sceneNodes.addChild(bg);
    }

}



