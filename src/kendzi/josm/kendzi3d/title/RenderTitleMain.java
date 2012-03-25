

package kendzi.josm.kendzi3d.title;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;
import javax.media.opengl.GLProfile;
import javax.vecmath.Point2d;

import kendzi.jogl.model.render.ModelRender;
import kendzi.josm.kendzi3d.jogl.Kendzi3dTitleGLEventListener;
import kendzi.josm.kendzi3d.jogl.RenderJOSM;
import kendzi.josm.kendzi3d.service.TextureCacheService;
import kendzi.josm.kendzi3d.title.TitleToLatLon.BoundingBox;

import org.apache.log4j.Logger;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.Preferences;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.event.DataChangedEvent;
import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
import org.openstreetmap.josm.io.IllegalDataException;
import org.openstreetmap.josm.io.OsmReader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jogamp.opengl.util.awt.Screenshot;


public class RenderTitleMain {

    /** Log. */
    private static final Logger log = Logger.getLogger(RenderTitleMain.class);

    private static void initJOSMMinimal() {
        Main.pref = new Preferences();
        org.openstreetmap.josm.gui.preferences.map.ProjectionPreference.setProjection();
    }
    private static void initKendzi3dMinimal(Injector injector) {
        // XXX move to module
        //        MetadataCacheService.initMetadataCache(injector);
        //        TextureCacheService.initTextureCache(injector);
        //        FileUrlReciverService.initFileReciver(injector);
        TextureCacheService textureCacheService = injector.getInstance(TextureCacheService.class);
        textureCacheService.setTextureFilter(true);

        ModelRender modelRender = injector.getInstance(ModelRender.class);

        boolean debug = false;
        modelRender.setDebugging(debug);
        modelRender.setDrawEdges(debug);
        modelRender.setDrawNormals(debug);
    }

    public static void main(String[] args) throws IllegalDataException, IOException {

        long startTime = System.currentTimeMillis();

        Map<TitleParm, String> parms = parseArgs(args);

        parseProperties(parms);

        validate(parms);

        generate(parms);

        double renderTime = (System.currentTimeMillis() - startTime) / 1000d;
        log.info("render time: " + renderTime);

    }

    private static void parseProperties(Map<TitleParm, String> parms) throws FileNotFoundException, IOException {

        File propFile = getPropertiesFile(parms);

        Properties prop = new Properties();
        prop.load(new FileInputStream(propFile));

        for (Object o : prop.keySet()) {
            TitleParm tp = getTitleParm((String) o);

            if (tp != null) {

                if (parms.get(tp) == null) {
                    // parms from command line are more importand (exept of location of properties)
                    parms.put(tp, prop.getProperty((String) o));
                }
            }
        }
    }

    private static File getPropertiesFile(Map<TitleParm, String> parms) {
        String prop = parms.get(TitleParm.RENDER_PROPERTIES);
        if (prop == null) {
            prop = "render.properties";
        }

        System.out.println((new File(".")).getAbsoluteFile());

        File f = new File(prop);
        if (!f.exists()) {
            f = new File(parms.get(TitleParm.RES_DIR), prop);
        }
        return f;
    }

    private static void validate(Map<TitleParm, String> parms) {
        // TODO Auto-generated method stub

    }


    private static Map<TitleParm, String> parseArgs(String[] parms) {
        Map<TitleParm, String> ret = new HashMap<TitleParm, String>();

        for (String parm : parms) {
            String[] split = parm.split("=");

            if (split.length>1) {
                TitleParm tp = getTitleParm(split[0]);
                if (tp != null) {
                    ret.put(tp, split[1]);
                }
            }
        }
        return ret;
    }

    private static TitleParm getTitleParm(String string) {
        if (string == null ) {
            return null;
        }
        for (TitleParm tp : TitleParm.values()) {
            if (tp.getParm().toLowerCase().equals(string.toLowerCase())) {
                return tp;
            }
        }
        return null;
    }

    public static void generate(Map<TitleParm, String> parms) throws FileNotFoundException, IllegalDataException {

        String inFile = parms.get(TitleParm.IN_FILE);
        String outDir = parms.get(TitleParm.OUT_DIR);
        boolean dataSetBounds = Boolean.parseBoolean(parms.get(TitleParm.DATA_SET_BOUNDS));
        int zoom = Integer.parseInt(parms.get(TitleParm.ZOOM));
        String resDir = parms.get(TitleParm.RES_DIR);

        int z = zoom;

        Point c = getCenter(z);
        int x = c.x;
        int y = c.y;

        int a = 0;

        initJOSMMinimal();

        Injector injector = Guice.createInjector(new TitleModule(resDir));

        initKendzi3dMinimal(injector);

        FileInputStream in = new FileInputStream(inFile);
        final DataSet dataSet = OsmReader.parseDataSet(in, NullProgressMonitor.INSTANCE);

        if (dataSetBounds) {

            List<Title> titleList = prepareBoundsForDataSet(zoom, dataSet);
            genTitle(titleList, dataSet, outDir, injector);

        } else if (a == 0) {
            genTitle(Arrays.asList(new Title(x, y, z)), dataSet, outDir, injector);
        } else {

            List<Title> titleList = new ArrayList<RenderTitleMain.Title>();
            for (int xi = x - a; xi < x + a; xi++ ) {
                for (int yi = y - a; yi < y + a; yi++ ) {
                    Title t = new Title(xi, yi, z);
                    titleList.add(t);
                }
            }
            genTitle(titleList, dataSet, outDir, injector);
        }
    }
    /**
     * @param zoom
     * @param dataSet
     * @return
     */
    public static List<Title> prepareBoundsForDataSet(int zoom,
            final DataSet dataSet) {
        Bounds bounds  = RenderJOSM.boundsFromDataSet(dataSet);

        LatLon max = bounds.getMax();
        LatLon min = bounds.getMin();

        int xtileMax = TitleToLatLon.lonToTile(max.lon(), zoom);
        int ytileMax = TitleToLatLon.latToTile(max.lat(), zoom);
        int xtileMin = TitleToLatLon.lonToTile(min.lon(), zoom);
        int ytileMin = TitleToLatLon.latToTile(min.lat(), zoom);

        List<Title> titleList = new ArrayList<RenderTitleMain.Title>();
        for (int xi = xtileMin; xi <= xtileMax; xi++ ) {
            for (int yi = ytileMax; yi <= ytileMin; yi++ ) {
                Title t = new Title(xi, yi, zoom);
                titleList.add(t);
            }
        }
        return titleList;
    }


    @Deprecated // only for test!!!
    private static Point getCenter(int z) {

        if (z == 17 ) {
            return new Point(71275, 43212);
        } else if (z == 18 ) {
            return new Point(142551, 86424);
        } else if (z == 19 ) {
            return new Point(285100, 172849);
        } else if (z == 20 ) {
            return new Point(570202, 345699);
        } else if (z == 21 ) {
            return new Point(1140404, 691401);
        } else if (z == 22 ) {
            return new Point(2280807, 1382802);
        }

        return null;
    }

    static class Title {
        int x;
        int y;
        int z;

        public Title(int x, int y, int z) {
            super();
            this.x = x;
            this.y = y;
            this.z = z;
        }
        /**
         * @return the x
         */
        public int getX() {
            return this.x;
        }
        /**
         * @param x the x to set
         */
        public void setX(int x) {
            this.x = x;
        }
        /**
         * @return the y
         */
        public int getY() {
            return this.y;
        }
        /**
         * @param y the y to set
         */
        public void setY(int y) {
            this.y = y;
        }
        /**
         * @return the z
         */
        public int getZ() {
            return this.z;
        }
        /**
         * @param z the z to set
         */
        public void setZ(int z) {
            this.z = z;
        }
    }

    public static void genTitle(List<Title> pTitleList, DataSet dataSet, String outDir, Injector injector) {

        GLContext context = null;
        GLPbuffer buf = null;

        int width = 256;
        int height = 256;

        int bufw = 0;
        int bufh = 0;

        System.out.println("is set debug for GraphicsConfiguration: " + com.jogamp.opengl.impl.Debug.debug("GraphicsConfiguration"));

        final GLProfile gl2Profile = GLProfile.get(GLProfile.GL2);

        if (buf == null || bufw != width || bufh != height){
            if (buf != null) {   // clean the old buffer
                context.destroy();   //context is type GLContext
                buf.destroy();    // buf is type GLPbuffer
            }
            //          GLDawableFactory


            GLDrawableFactory factory = GLDrawableFactory.getFactory(gl2Profile );
            //            context = factory.createExternalGLContext();


            //            GLDrawableFactory factory = GLDrawableFactory.getFactory(null);
            GLCapabilities glCap = new GLCapabilities(gl2Profile);
            // Without line below, there is an error on Windows.
            glCap.setDoubleBuffered(false);

            GLCapabilities capabilities = glCap;
            //capabilities.seto
            capabilities.setDepthBits(16);

            capabilities.setSampleBuffers(true);

            capabilities.setNumSamples(2);

            capabilities.setAlphaBits(1);
            //            capabilities.setAccumAlphaBits(8);

            System.out.println("GLCapabilities: " + capabilities);

            //makes a new buffer
            buf = factory.createGLPbuffer(null, glCap, null, width, height, null);
            //save size for later use in getting image
            bufw = width;
            bufh = height;
            //required for drawing to the buffer
            //            context =  buf.createContext(null);
            context = buf.getContext();

        }




        //        Kendzi3dTitleGLEventListener ff = new Kendzi3dTitleGLEventListener();
        Kendzi3dTitleGLEventListener ff = injector.getInstance(Kendzi3dTitleGLEventListener.class);


        ff.getRenderJosm().processDatasetEvent(new DataChangedEvent(dataSet));

        //Disable the using of OpenGL or at least by way of a Pbuffer
        context.makeCurrent();


        //        GL2 gl = buf.getGL().getGL2();

        ff.init(buf);

        for (Title t : pTitleList) {
            generateTitle(t.getX(), t.getY(), t.getZ(), buf, bufw, bufh, ff
                    ,width, height, outDir);
        }

        context.release();
        if (buf != null) {
            context.destroy();
            buf.destroy();
        }


    }

    /**
     * @param x
     * @param y
     * @param z
     * @param buf
     * @param bufw
     * @param bufh
     * @param ff
     * @param width
     * @param height
     */
    public static void generateTitle(int x, int y, int z, GLPbuffer buf,
            int bufw, int bufh, Kendzi3dTitleGLEventListener ff, int width, int height,
            String outDir) {
        BoundingBox camraBoundingBox = TitleToLatLon.tile2boundingBox(x, y, z);

        LatLon leftTop = new LatLon(camraBoundingBox.north, camraBoundingBox.west);
        LatLon rightBottom = new LatLon(camraBoundingBox.south, camraBoundingBox.east);


        Point2d leftTopPoint = ff.getRenderJosm().getPerspective().calcPoint(Main.getProjection().latlon2eastNorth(leftTop));
        Point2d rightBottomPoint = ff.getRenderJosm().getPerspective().calcPoint(Main.getProjection().latlon2eastNorth(rightBottom));

        Point2d center = new Point2d(leftTopPoint);
        center.add(rightBottomPoint);
        center.scale(0.5);




        ff.setCamraCenter(center);
        ff.setLeftTopPoint(leftTopPoint);
        ff.setRightBottomPoint(rightBottomPoint);

        ff.reshape(buf, 0, 0, width, height);
        ff.display(buf);
        ff.dispose(buf);

        //        context.makeCurrent();
        BufferedImage img = Screenshot.readToBufferedImage(bufw,bufh, true);
        //        context.release();
        //        g.drawImage(img,0,0,null);

        String fileName = outDir + "/" + z + "/" + x + "/" + y + ".png";

        try {

            File outFile = new File(fileName);

            File parentDir = outFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            ImageIO.write(img, "png", outFile);
            log.info("Generaded: " + fileName);
        } catch (IOException e) {
            log.error("Error writing to file: " + fileName, e);
        }
    }
}
