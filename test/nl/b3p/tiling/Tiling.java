/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.tiling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import nl.b3p.imagetool.CombineImageSettings;
import nl.b3p.imagetool.CombineImagesHandler;
import nl.b3p.imagetool.OpacityTest;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Roy Braam
 */
public class Tiling {
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
    
    //@Test
    /* Test WMSc
     */
    public void wmscTest() throws Exception {
        testImageSettings(new JSONObject("{"
            + "requests: [{"
            + "     protocol: 'WMSC',"
            + "     url: 'http://geodata.nationaalgeoregister.nl/wmsc?request=GetMap&Service=WMS&Format=image/png&srs=epsg:28992&tiled=true&layers=brtachtergrondkaart',"
            + "     tileWidth: 256,"
            + "     tileHeight: 256,"
            + "     serverExtent: '-285401.92,22598.08,595401.9199999999,903401.9199999999',"
            + "     resolutions: '3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21'"
            + "}],"
            + "bbox: '0,300000,300000,600000',"
            + "width: 1024,"
            + "height: 1024,"
            + "srid: 28992"          
            + "}"), "WMSc_1.png");
    }
    
    @Test
    /* Test WMSc
     */
    public void correctTilesTest() throws Exception {
        testImageSettings(new JSONObject("{"
                + "requests:[{"
                + "     serverExtent:'-285401.920000,22598.080000,595401.920000,903401.920000',"
                + "     protocol:'WMSC',"
                + "     resolutions: '3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21',"
                + "     tileHeight:256,"
                + "     tileWidth:256,"    
                + "     correctTiles: false,"
                + "     url: 'http://www.openbasiskaart.nl/mapcache/?LAYERS=osm-nb&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992'"
                + "}],"
                + "height:3028,"
                + "geometries:[],"
                + "bbox:'150776.556754,444456.23452334,154440.3423423,447484.234523',"
                + "width:3664"
                + "}"), "NotCorrectedTiles.png");
        testImageSettings(new JSONObject("{"
                + "requests:[{"
                + "     serverExtent:'-285401.920000,22598.080000,595401.920000,903401.920000',"
                + "     protocol:'WMSC',"
                + "     resolutions: '3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21',"
                + "     tileHeight:256,"
                + "     tileWidth:256,"
                + "     url: 'http://www.openbasiskaart.nl/mapcache/?LAYERS=osm-nb&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992'"
                + "}],"
                + "height:3028,"
                + "geometries:[],"
                + "bbox:'150776.23452334,444456.23452334,153804.234523,447484.234523',"
                + "width:3028"
                + "}"), "CorrectedTiles.png");
    }
    //@Test
    /* Test WMSc
     */
    public void wmscTest3() throws Exception {
        testImageSettings(new JSONObject("{"
                + "requests:[{"
                + "     serverExtent:'-285401.920000,22598.080000,595401.920000,903401.920000',"
                + "     protocol:'WMSC',"
                + "     resolutions: '3440.64,1720.32,860.16,430.08,215.04,107.52,53.76,26.88,13.44,6.72,3.36,1.68,0.84,0.42,0.21',"
                + "     tileHeight:256,"
                + "     tileWidth:256,"                
                + "     url: 'http://www.openbasiskaart.nl/mapcache/?LAYERS=osm-nb&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&FORMAT=image%2Fjpeg&SRS=EPSG%3A28992'"
                + "}],"
                + "height:7570,"
                + "geometries:[],"
                + "bbox:'150776.556754,444456.23452334,154440.3423423,447484.234523',"
                + "width:9160"
                + "}"), "WMSc_3.png");
    }
    /**
     * Test the imagesettings.
     */
    private void testImageSettings(JSONObject json,String ouputName) throws FileNotFoundException, Exception{
        CombineImageSettings settings = CombineImageSettings.fromJson(json);
        File f = new File(DEST_DIR + "/"+ouputName);
        System.out.println("Writing to: "+f.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(f);
        CombineImagesHandler.combineImage(fos, settings);        
    }
}
