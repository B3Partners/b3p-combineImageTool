package nl.b3p.imagetool;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    
    public static List<TileImage> getTilingImages(CombineImageSettings settings) 
            throws MalformedURLException {
        
        List<TileImage> tileImages = new ArrayList();
        
        /* Bbox van verzoek */        
        Bbox requestBbox = null;
        if (settings.getBbox() != null) {            
            Double requestMinX = settings.getBbox().getMinx();
            Double requestMinY = settings.getBbox().getMiny();
            Double requestMaxX = settings.getBbox().getMaxx();
            Double requestMaxY = settings.getBbox().getMaxy();
            
            requestBbox = new Bbox(requestMinX, requestMinY, requestMaxX, requestMaxY);
        }
        
        /* Bbox van service */
        Bbox serviceBbox = null;        
        if (settings.getTilingBbox() != null) {
            String[] bbox = settings.getTilingBbox().split(",");
            
            Double serviceMinX = new Double(bbox[0]);
            Double serviceMinY = new Double(bbox[1]);
            Double serviceMaxX = new Double(bbox[2]);
            Double serviceMaxY = new Double(bbox[3]);
            
            serviceBbox = new Bbox(serviceMinX, serviceMinY, serviceMaxX, serviceMaxY);
        }
        
        /* 1) Berekenen resolutie */        
        Double res = null;    
        if (requestBbox != null) {            
            Integer mapWidth = settings.getWidth();
            
            res = (requestBbox.getMaxX() - requestBbox.getMinX()) / mapWidth;
        }
            
        /* 2) Bekijk de service resoluties voor mogelijk resample tiles. Pak de
         eerstvolgende kleinere service resolutie */
        Double useRes = null;
        if (settings.getTilingResolutions() != null && res != null) {
            String[] resolutions = null;
            if (settings.getTilingResolutions().indexOf(",") > 0) {
                resolutions = settings.getTilingResolutions().split(",");
            }
            
            if (resolutions == null && settings.getTilingResolutions().indexOf(" ") > 0) {
                resolutions = settings.getTilingResolutions().split(" ");
            }
            
            for (int i=0; i < resolutions.length; i++) {
                Double testRes = new Double(resolutions[i]);
                
                if ( ((res - testRes) < 0.0000000001) && ((res-testRes) > -0.0000000001) ) {
                    useRes = testRes;
                    break;
                } else if (res >= testRes) {
                    useRes = testRes;
                    break;
                }
            }
            
            if (useRes == null) {
                useRes = res;
            }
        }
        
        /* Deze later hergebruiken voor berekeing tile positie */        
        Integer mapWidth = null;
        Integer mapHeight = null;
        
        if (settings.getWidth() != null) {
            mapWidth = settings.getWidth();
        }
        
        if (settings.getHeight() != null) {
            mapHeight = settings.getHeight();
        }
        
        /* 3) Berekenen tiles in mapunits */
        Double tileWidthMapUnits = null;
        Double tileHeightMapUnits = null;        
        if (settings.getTilingTileWidth() != null && useRes != null) {
            tileWidthMapUnits = settings.getTilingTileWidth() * useRes;
        }
        if (settings.getTilingTileHeight() != null && useRes != null) {
            tileHeightMapUnits = settings.getTilingTileWidth() * useRes;
        }
        
        /* 4) Berekenen benodigde tiles */
        Integer minTileIndexX = null;
        Integer maxTileIndexX = null;
        Integer minTileIndexY = null;
        Integer maxTileIndexY = null;        
        if (tileWidthMapUnits != null && tileWidthMapUnits > 0 
                && tileHeightMapUnits != null && tileHeightMapUnits > 0) {            
            minTileIndexX = getTilingCoord(serviceBbox.getMinX(), serviceBbox.getMaxX(), tileWidthMapUnits, requestBbox.getMinX());
            maxTileIndexX = getTilingCoord(serviceBbox.getMinX(), serviceBbox.getMaxX(), tileWidthMapUnits, requestBbox.getMaxX());
            minTileIndexY = getTilingCoord(serviceBbox.getMinY(), serviceBbox.getMaxY(), tileHeightMapUnits, requestBbox.getMinY());
            maxTileIndexY = getTilingCoord(serviceBbox.getMinY(), serviceBbox.getMaxY(), tileHeightMapUnits, requestBbox.getMaxY());            
        }
        
        /* 5) Maken tile urls */
        for (int ix = minTileIndexX; ix <= maxTileIndexX; ix++) {
            for (int iy = minTileIndexY; iy <= maxTileIndexY; iy++) {
                double[] bbox = new double[4];
                
                bbox[0] = serviceBbox.getMinX() + (ix * tileWidthMapUnits);
                bbox[1] = serviceBbox.getMinY() + (iy * tileHeightMapUnits);
                bbox[2] = bbox[0] + tileWidthMapUnits;                
                bbox[3] = bbox[1] + tileHeightMapUnits;
                
                Bbox tileBbox = new Bbox(bbox[0], bbox[1], bbox[2], bbox[3]);
                
                TileImage tile = calcTilePosition(mapWidth, mapHeight, tileBbox, requestBbox, ix, iy);               
                 
                String serviceUrl = settings.getTilingServiceUrl();
                
                String sizes = null;
                if (settings.getTilingTileWidth() != null && settings.getTilingTileHeight() != null) {
                    sizes = "&WIDTH=" + settings.getTilingTileWidth() + "&HEIGHT=" + settings.getTilingTileHeight();
                }
                
                String bboxString = "&BBOX=" + tileBbox.getMinX() + "," + tileBbox.getMinY() + "," + tileBbox.getMaxX() + "," + tileBbox.getMaxY();
                
                String newUrl = serviceUrl + sizes + bboxString;
                
                CombineImageUrl url = new CombineImageUrl(); 
                url.setUrl(newUrl);
                url.setRealUrl(new URL(newUrl));
                tile.setCombineImageUrl(url);
                
                tileImages.add(tile);
                
                log.debug("TILE IMAGE REQUEST: " + newUrl);
            }            
        }
        
        /* 6) Berekenen waar tiles geplaatst moeten worden */
        
        return tileImages;
    }
    
    public static TileImage calcTilePosition(Integer mapWidth, Integer mapHeight,
            Bbox tileBbox, Bbox requestBbox, int offsetX, int offsetY) {
        
        TileImage tile = new TileImage();
        
        Double msx = (requestBbox.getMaxX() - requestBbox.getMinX()) / mapWidth;
        Double msy = (requestBbox.getMaxY() - requestBbox.getMinY()) / mapHeight;
        
        Double posX = Math.floor( (tileBbox.getMinX() - requestBbox.getMinX()) / msx );
        Double posY = Math.floor( (requestBbox.getMaxY() - tileBbox.getMaxY()) / msy );
        Double width = Math.floor( (tileBbox.getMaxX() - tileBbox.getMinX()) / msx );
        Double height = Math.floor( (tileBbox.getMaxY() - tileBbox.getMinY()) / msy );
        
        tile.setPosX(posX.intValue() - offsetX);
        tile.setPosY(posY.intValue() + offsetY);
        
        tile.setImageWidth(width.intValue());
        tile.setImageHeight(height.intValue());
        
        tile.setMapWidth(mapWidth);
        tile.setMapHeight(mapHeight);
        
        return tile;
    }
    
    public static int getTilingCoord(Double serviceMin, Double serviceMax,
            Double tileSizeMapUnits, Double coord) {
        
        double epsilon = 0.00000001;        
        Double tileIndex = 0.0;
        
        tileIndex = Math.floor( (coord - serviceMin) / (tileSizeMapUnits + epsilon) );
        
        if (tileIndex < 0)
            tileIndex = 0.0;
        
        Double maxBbox = Math.floor( (serviceMax - serviceMin) / (tileSizeMapUnits + epsilon) );
        
        if (tileIndex > maxBbox)
            tileIndex = maxBbox;
        
        return tileIndex.intValue();   
    }
    
    public static void combineImage(OutputStream out, CombineImageSettings settings, 
            String returnMime, int maxResponseTime, String uname, String pw) throws Exception {
        
        /* Bereken url's voor tiles */
        List<TileImage> tilingImages = new ArrayList();
        if (settings.getTilingServiceUrl() != null) {            
            tilingImages = getTilingImages(settings);
        }
        
        /**herbereken de bbox van de urls en gebruik die urls om het plaatje te maken. Als er geen
         * als er geen urls kunnen/hoeven worden berekend gebruik dan de ingegeven urls
         */        
        List normalUrls = settings.getCalculatedUrls();
        if (normalUrls == null) {
            normalUrls = settings.getUrls();
        }
        
        List urls = new ArrayList();
        if (tilingImages != null && tilingImages.size() > 0) {
            for (TileImage tileImage : tilingImages) {
                urls.add(tileImage.getCombineImageUrl());
            }
        }
        
        if (normalUrls != null && normalUrls.size() > 0) {
            urls.addAll(normalUrls);
        }
        
        if (urls.size() < 1) {
            throw new Exception("Geen verzoeken gevonden om te combineren.");
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
        BufferedImage combinedImages = ImageTool.combineImages(bi, returnMime, alphas, tilingImages);

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
