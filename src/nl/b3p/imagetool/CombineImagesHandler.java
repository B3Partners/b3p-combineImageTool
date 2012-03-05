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
    
    public static List<TileImage> getTilingUrls(CombineImageSettings settings) 
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
        
        /* 1) Berekenen resolutie */        
        Double res = null;        
        Bbox serviceBbox = null;        
        if (settings.getTilingBbox() != null) {
            String[] bbox = settings.getTilingBbox().split(",");
            
            Double serviceMinX = new Double(bbox[0]);
            Double serviceMinY = new Double(bbox[1]);
            Double serviceMaxX = new Double(bbox[2]);
            Double serviceMaxY = new Double(bbox[3]);
            
            serviceBbox = new Bbox(serviceMinX, serviceMinY, serviceMaxX, serviceMaxY);
            
            Integer mapWidth = settings.getWidth();
            
            res = (serviceMaxX - serviceMinX) / mapWidth;
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
        Integer tileWidth = null;
        Integer tileHeight = null;
        
        /* 3) Berekenen tiles in mapunits */
        Long tileWidthMapUnits = null;
        Long tileHeightMapUnits = null;        
        if (settings.getTilingTileWidth() != null && useRes != null) {
            tileWidthMapUnits = Math.round(settings.getTilingTileWidth() * useRes);
            tileWidth = settings.getTilingTileWidth();
        }
        if (settings.getTilingTileHeight() != null && useRes != null) {
            tileHeightMapUnits = Math.round(settings.getTilingTileWidth() * useRes);
            tileHeight = settings.getTilingTileWidth();
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
                bbox[1] = bbox[0] + tileWidthMapUnits;
                bbox[2] = serviceBbox.getMinY() + (iy * tileHeightMapUnits);
                bbox[3] = bbox[2] + tileHeightMapUnits;
                
                Bbox tileBbox = new Bbox(bbox[0], bbox[1], bbox[2], bbox[3]);
                
                TileImage tile = calcTilePosition(tileWidth, tileHeight, tileBbox, requestBbox);
                
                // http://localhost:8084/kaartenbalie/services/e84fd9f44587683512f2c00ff36d243f?
                // &SERVICE=WMS&VERSION=1.1.1&LAYERS=geoweblimprod_Luchtfoto&STYLES=
                // &FORMAT=image/jpeg&SRS=EPSG:28992&SERVICE=WMS&REQUEST=GetMap
                // &NAME=fmc49_49&WIDTH=256&HEIGHT=256&BBOX=210050.24,366662.08,223812.8,380424.64
               
                CombineImageUrl url = new CombineImageUrl();  
                String serviceUrl = settings.getTilingServiceUrl();
                String bboxString = "&" + bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3];
                String newUrl = serviceUrl + bboxString;
                url.setUrl(newUrl);
                url.setRealUrl(new URL(newUrl));
                tile.setCombineImageUrl(url);
                
                tileImages.add(tile);
                
                log.debug("TILE BBOX: " + bboxString);
                log.debug("TILE URL: " + newUrl);
            }            
        }
        
        /* 6) Berekenen waar tiles geplaatst moeten worden */
        
        return tileImages;
    }
    
    public static TileImage calcTilePosition(Integer tileWidth, Integer tileHeight,
            Bbox tileBbox, Bbox requestBbox) {
        
        TileImage tile = new TileImage();
        
        Double msx = (requestBbox.getMaxX() - requestBbox.getMinX()) / tileWidth;
        Double msy = (requestBbox.getMaxY() - requestBbox.getMinY()) / tileHeight;
        
        Double posX = (tileBbox.getMinX() - requestBbox.getMinX()) / msx;
        Double posY = (requestBbox.getMaxY() - tileBbox.getMaxY()) / msy;
        Long width = Math.round( (tileBbox.getMaxX() - tileBbox.getMinX()) / msx);
        Long height = Math.round( (tileBbox.getMaxY() - tileBbox.getMinY()) / msy);
        
        tile.setPosX(posX.intValue());
        tile.setPosY(posY.intValue());
        tile.setImageWidth(width.intValue());
        tile.setImageHeight(height.intValue());
        
        return tile;
    }
    
    public static int getTilingCoord(Double serviceMin, Double serviceMax,
            Long tileSizeMapUnits, Double coord) {
        
        int epsilon = 5;        
        Double tileIndex = 0.0;
        
        tileIndex = Math.floor( (coord - serviceMin) / (tileSizeMapUnits + epsilon) );
        
        if (tileIndex < 0)
            tileIndex = 0.0;
        
        Double maxBboxX = Math.floor( (serviceMax - serviceMin) / (tileSizeMapUnits + epsilon) );
        
        if (tileIndex > maxBboxX)
            tileIndex = maxBboxX;
        
        return tileIndex.intValue();   
    }
    
    public static void combineImage(OutputStream out, CombineImageSettings settings, 
            String returnMime, int maxResponseTime, String uname, String pw) throws Exception {
        
        /* Bereken url's voor tiles */
        List<TileImage> tilingImages = new ArrayList();
        if (settings.getTilingServiceUrl() != null) {            
            tilingImages = getTilingUrls(settings);
        }
        
        /**herbereken de bbox van de urls en gebruik die urls om het plaatje te maken. Als er geen
         * als er geen urls kunnen/hoeven worden berekend gebruik dan de ingegeven urls
         */        
        List normalUrls = settings.getCalculatedUrls();
        if (normalUrls == null) {
            normalUrls = settings.getUrls();
        }
        
        List urls = new ArrayList();        
        if (normalUrls != null && normalUrls.size() > 0) {
            urls.addAll(normalUrls);
        }            
        
        if (tilingImages != null && tilingImages.size() > 0) {
            for (TileImage tileImage : tilingImages) {
                urls.add(tileImage.getCombineImageUrl());
            }
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
