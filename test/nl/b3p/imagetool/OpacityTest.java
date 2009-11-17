package nl.b3p.imagetool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Matthijs
 */
public class OpacityTest {

    public OpacityTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        File destDir = new File(DEST_DIR);
        if(!destDir.exists()) {
            destDir.mkdir();
        }
    }

    @After
    public void tearDown() {
    }

    private static final String DEST_DIR = "test-output";

    @Test
    /* Test het plakken van de volgende images over elkaar:
     * "0 achtergrond.png": 32-bit RGB+alpha achtergrond luchtfoto
     * "1 overlay transparent.png": 32-bit RGB+alpha overlay polygon (transparant)
     * De "alpha" property van CombineImageUrl wordt hier dus niet gebruikt
     */
    public void opacityTest1() throws Exception {
        CombineImageSettings settings = new CombineImageSettings();
        Class c = this.getClass();
        String pkg = "opacity-test1,2/";
        List urls = Arrays.asList(new CombineImageUrl[] {
            new CombineImageUrl(c.getResource(pkg + "0 achtergrond.png"), null),
            new CombineImageUrl(c.getResource(pkg + "1 overlay transparent.png"), null)
        });
        settings.setUrls(urls);
        FileOutputStream fos = new FileOutputStream(DEST_DIR + "/opacity-test1-result.png");
        CombineImagesHandler.combineImage(fos, settings);
    }

    @Test
    /* Test het plakken van de volgende images over elkaar:
     * "0 achtergrond.png": 32-bit RGB+alpha achtergrond luchtfoto
     * "1 overlay niet transparent.png": 32-bit RGB+alpha overlay polygon, polygoon niet transparant, achtergrond wel
     * De "alpha" property van CombineImageUrl wordt gebruikt om polygoon
     * transparant te maken
     */
    public void opacityTest2() throws Exception {
        CombineImageSettings settings = new CombineImageSettings();
        Class c = this.getClass();
        String pkg = "opacity-test1,2/";
        List urls = Arrays.asList(new CombineImageUrl[] {
            new CombineImageUrl(c.getResource(pkg + "0 achtergrond.png"), null),
            new CombineImageUrl(c.getResource(pkg + "1 overlay niet transparent.png"), 0.5f)
        });
        settings.setUrls(urls);
        FileOutputStream fos = new FileOutputStream(DEST_DIR + "/opacity-test2-result.png");
        CombineImagesHandler.combineImage(fos, settings);
    }

    @Test
    /* Test het plakken van de volgende images over elkaar:
     * "0 topografie.png": 24-bit RGB, tRNS: red = 251 green = 251 blue = 251, achtergrond
     * "4 services.png": 32-bit RGB+alpha overlay niet transparant, achtergrond wel
     * De "alpha" property van CombineImageUrl wordt gebruikt om overlay
     * transparant te maken
     */
    public void opacityTest3() throws Exception {
        CombineImageSettings settings = new CombineImageSettings();
        Class c = this.getClass();
        String pkg = "opacity-test3/";
        List urls = Arrays.asList(new CombineImageUrl[] {
            new CombineImageUrl(c.getResource(pkg + "0 topografie.png"), null),
            new CombineImageUrl(c.getResource(pkg + "1 services.png"), 0.5f)
        });
        settings.setUrls(urls);
        FileOutputStream fos = new FileOutputStream(DEST_DIR + "/opacity-test3-result.png");
        CombineImagesHandler.combineImage(fos, settings);
    }
}