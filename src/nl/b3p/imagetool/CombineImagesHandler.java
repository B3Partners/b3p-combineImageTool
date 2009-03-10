package nl.b3p.imagetool;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 * @author Roy *
 */
public class CombineImagesHandler{
    private static final Log log = LogFactory.getLog(CombineImagesHandler.class);

    private CombineImageSettings settings=null;
    private static String defaultReturnMime="image/png";
    //private int srid=28992;
    private static int defaultMaxResponseTime=30000;

    public static void combineImage(OutputStream out, CombineImageSettings settings) throws Exception{
        combineImage(out,settings,defaultReturnMime,defaultMaxResponseTime);
    }
    public static void combineImage(OutputStream out, CombineImageSettings settings,String returnMime,int maxResponseTime) throws Exception{
             /**herbereken de bbox van de urls en gebruik die urls om het plaatje te maken. Als er geen
             * als er geen urls kunnen/hoeven worden berekend gebruik dan de ingegeven urls
             */
            ArrayList urls =settings.getCalculatedUrls();
            if (urls==null){
                urls=settings.getUrls();
            }

            ArrayList wktGeoms = settings.getWktGeoms();
                        
            //haal de plaatjes van de urls op.
            ImageManager im = new ImageManager(urls,maxResponseTime);
            BufferedImage[] bi=null;
            try{
                im.process();
                bi=im.getCombinedImages();
            }catch(Exception e){
                throw e;
            }
            BufferedImage returnImage=null;
            //combineer de opgehaalde plaatjes en als er een wktGeom is meegegeven teken die dan.
            BufferedImage combinedImages= ImageTool.combineImages(bi,returnMime);
            try{
                if(wktGeoms!=null){
                   returnImage=ImageTool.drawGeometries(combinedImages,wktGeoms, settings);
                }else{
                   returnImage=combinedImages;
                }
            }catch (Exception e){
                log.error("Kan geometrien niet tekenen. Return image zonder alle geometrien: ",e);
                returnImage=combinedImages;
            }            
            try {
                ImageTool.writeImage(returnImage,returnMime, out);
            } catch (Exception ex) {
                log.error(ex);
            }
        }

    
}
