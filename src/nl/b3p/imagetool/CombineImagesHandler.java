package nl.b3p.imagetool;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Roy *
 */
public class CombineImagesHandler {

    private static final Log log = LogFactory.getLog(CombineImagesHandler.class);
    private static String defaultReturnMime = "image/png";
    private static int defaultMaxResponseTime = 30000;

    public static void combineImage(OutputStream out, CombineImageSettings settings) throws Exception {
        combineImage(out, settings, defaultReturnMime, defaultMaxResponseTime);
    }

    public static void combineImage(OutputStream out, CombineImageSettings settings, String returnMime, int maxResponseTime) throws Exception {
        combineImage(out, settings, returnMime, maxResponseTime, null, null);
    }

    public static void combineImage(OutputStream out, CombineImageSettings settings, 
            String returnMime, int maxResponseTime, String uname, String pw) throws Exception {
        /**herbereken de bbox van de urls en gebruik die urls om het plaatje te maken. Als er geen
         * als er geen urls kunnen/hoeven worden berekend gebruik dan de ingegeven urls
         */
        List urls = settings.getCalculatedUrls();
        if (urls == null) {
            urls = settings.getUrls();
        }
        if (urls == null || urls.size() == 0) {
            throw new Exception("No image urls found!");
        }

        //haal de plaatjes van de urls op.
        ImageManager im = new ImageManager(urls, maxResponseTime, uname, pw);
        BufferedImage[] bi = null;
        try {
            im.process();
            bi = im.getCombinedImages();
        } catch (Exception e) {
            throw e;
        }

        Float[] alphas = null;

        for (int i = 0; i < urls.size(); i++) {
            CombineImageUrl ciu = (CombineImageUrl) urls.get(i);
            if (ciu.getAlpha() != null) {
                if (alphas == null) {
                    alphas = new Float[urls.size()];
                }
                alphas[i] = ciu.getAlpha();
            }
        }

        BufferedImage returnImage = null;
        //combineer de opgehaalde plaatjes en als er een wktGeom is meegegeven teken die dan.
        BufferedImage combinedImages = ImageTool.combineImages(bi, returnMime, alphas);

        try {
            if (settings.getWktGeoms() != null) {
                returnImage = ImageTool.drawGeometries(combinedImages, settings);
            } else {
                returnImage = combinedImages;
            }
        } catch (Exception e) {
            log.error("Kan geometrien niet tekenen. Return image zonder alle geometrien: ", e);
            returnImage = combinedImages;
        }
        try {
            ImageTool.writeImage(returnImage, returnMime, out);
        } catch (Exception ex) {
            log.error(ex);
        }
    }
}
