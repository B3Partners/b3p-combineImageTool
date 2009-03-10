/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.imagetool;

import java.awt.Color;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Roy
 */
public class CombineImageSettings {

    private static final Log log = LogFactory.getLog(CombineImageSettings.class);
    private ArrayList urls = null;
    private ArrayList wktGeoms = null;
    private Bbox bbox = null;
    private Integer srid = null;
    private Integer width = null;
    private Integer height = null;
    private Color wktGeomColor= Color.RED;

    /**
     * @return the wktGeomColor
     */
    public Color getWktGeomColor() {
        return wktGeomColor;
    }

    /**
     * @param wktGeomColor the wktGeomColor to set
     */
    public void setWktGeomColor(Color wktGeomColor) {
        this.wktGeomColor = wktGeomColor;
    }

    public class Bbox {
        //start stepping through the array from the beginning

        private Double minx;
        private Double maxx;
        private Double miny;
        private Double maxy;

        public Bbox(double[] b) {
            setBbox(b);
        }

        public Bbox(Bbox b){
            setBbox(b.toDoubleArray());
        }

        public void setBbox(double[] b){
            if (b == null || b.length != 4) {
                return;
            }
            minx = b[0];
            miny = b[1];
            maxx = b[2];
            maxy = b[3];
        }

        public double getMinx() {
            return minx;
        }

        public void setMinx(double minx) {
            this.minx = minx;
        }

        public double getMaxx() {
            return maxx;
        }

        public void setMaxx(double maxx) {
            this.maxx = maxx;
        }

        public double getMiny() {
            return miny;
        }

        public void setMiny(double miny) {
            this.miny = miny;
        }

        public double getMaxy() {
            return maxy;
        }

        public void setMaxy(double maxy) {
            this.maxy = maxy;
        }

        double[] toDoubleArray() {
            if (minx==null||miny==null||maxx==null||maxy==null){
                return null;
            }
            double[] r = new double[4];
            r[0] = minx;
            r[1] = miny;
            r[2] = maxx;
            r[3] = maxy;
            return r;
        }
        @Override
        public String toString(){
            String returnValue="";
            returnValue+=getMinx();
            returnValue+=",";
            returnValue+=getMiny();
            returnValue+=",";
            returnValue+=getMaxx();
            returnValue+=",";
            returnValue+=getMaxy();
            return returnValue;
        }
    }
    /**
     * Calculate the urls in the combineImageSettings.
     */
    public ArrayList getCalculatedUrls(){
        return getCalculatedUrls(urls);
    }
    /**
     * Geeft de url's terug met daarin de bbox met juist verhoudingen tov de width en height
     * En de juiste Width en Height van de settings
     * Tot nu alleen WMS urls ondersteund
     */
    public ArrayList getCalculatedUrls(ArrayList oldList){
        ArrayList returnValue=new ArrayList();
        if (bbox == null || width == null || height == null) {
            log.info("Not all settings set (width,height and bbox must be set to recalculate). Return original urls");
            return oldList;
        }else if(oldList==null){
            return returnValue;
        }        
        for (int i=0; i < oldList.size(); i++){
            returnValue.add(getCalculatedUrl((String)oldList.get(i)));
        }
        if (returnValue.size()==oldList.size()){
            return returnValue;
        }else{
            return null;
        }
    }
    /**
     * Een enkele url omzetten zodat de bbox, width en height goed worden geset.
     * Tot nu alleen WMS urls ondersteund
     */
    public String getCalculatedUrl(String url){
        if (bbox == null || width == null || height == null) {
            log.error("Not all settings set (width,height and bbox must be set)");
            return null;
        }
        Bbox newBbox= getCalculatedBbox();
        String newurl=new String(url);
        newurl=changeParameter(newurl, "bbox", newBbox.toString());
        newurl=changeParameter(newurl, "width", width.toString());
        newurl=changeParameter(newurl, "height", height.toString());
        return newurl;
    }
    /**
     * Geeft een kloppende bbox terug. Dus kijkt naar de width en height en past
     * de bbox zo aan en returned die zodat je een bbox hebt die klopt met de width en height
     * verhoudingen
     */
    public Bbox getCalculatedBbox(){
        if (bbox == null || width == null || height == null) {
            log.error("Not all settings set (width,height and bbox must be set)");
            return null;
        }
        Bbox newBbox= new Bbox(bbox);
        double bboxXwidth = bbox.maxx - bbox.minx;
        double bboxYheight = bbox.maxy - bbox.miny;

        double unitsPerPixelWidth = bboxXwidth / width;
        double unitsPerPixelHeight = bboxYheight / height;

        if (unitsPerPixelWidth > unitsPerPixelHeight) {
            //verander y waarden van bbox
            double newYHeight2 =(unitsPerPixelWidth*height-bboxYheight)/2;
            newBbox.setMiny(newBbox.getMiny()-newYHeight2);
            newBbox.setMaxy(newBbox.getMaxy()+newYHeight2);
        } else {
            double newXWidth2= (unitsPerPixelHeight*width-bboxXwidth)/2;
            newBbox.setMinx(newBbox.getMinx()-newXWidth2);
            newBbox.setMaxx(newBbox.getMaxx()+newXWidth2);
        }
        return newBbox;
    }

    public ArrayList getUrls() {
        return urls;
    }

    public void setUrls(ArrayList urls) {
        this.urls = urls;
    }

    public ArrayList getWktGeoms() {
        return wktGeoms;
    }

    public void setWktGeoms(ArrayList wktGeoms) {
        this.wktGeoms = wktGeoms;
    }

    public Bbox getBbox() {
        return bbox;
    }

    public void setBbox(double[] bbox) {
        this.bbox = new Bbox(bbox);
    }

    public Integer getSrid() {
        return srid;
    }

    public void setSrid(Integer srid) {
        this.srid = srid;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
    /*Returned een bepaalde parameter uit de url.*/
    private static String getParameter(String url, String key) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.indexOf("?" + key + "=") >= 0 || lowerUrl.indexOf("&" + key + "=") >= 0) {
            int beginIndex = 0;
            int endIndex = lowerUrl.length();
            if (lowerUrl.indexOf("?" + key + "=") >= 0) {
                beginIndex = lowerUrl.indexOf("?" + key + "=") + key.length() + 2;
            } else {
                beginIndex = lowerUrl.indexOf("&" + key + "") + key.length() + 2;
            }
            if (lowerUrl.indexOf("&", beginIndex) > 0) {
                endIndex = lowerUrl.indexOf("&", beginIndex);
            }
            if (beginIndex < endIndex) {
                return url.substring(beginIndex, endIndex);
            }
        }
        return null;
    }
    /**
     * Returned een url met een aangepaste parameter
     * @param url de url die veranderd moet worden
     * @param key de waarde van de parameter key
     * @param newValue de nieuwe waarde van de parameter
     * @return de veranderde url
     *
     */
    private static String changeParameter(String url, String key,String newValue) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.indexOf("?" + key + "=") >= 0 || lowerUrl.indexOf("&" + key + "=") >= 0) {
            int beginIndex = 0;
            int endIndex = lowerUrl.length();
            if (lowerUrl.indexOf("?" + key + "=") >= 0) {
                beginIndex = lowerUrl.indexOf("?" + key + "=") + key.length() + 2;
            } else {
                beginIndex = lowerUrl.indexOf("&" + key + "") + key.length() + 2;
            }
            if (lowerUrl.indexOf("&", beginIndex) > 0) {
                endIndex = lowerUrl.indexOf("&", beginIndex);
            }
            if (beginIndex < endIndex) {
                String newUrl="";
                if (beginIndex>0){
                    newUrl+=url.substring(0,beginIndex);
                }
                newUrl+=newValue;
                if (endIndex < url.length()){
                    newUrl+=url.substring(endIndex,url.length());
                }
                return newUrl;
            }
        }
        return url;
    }
    /**
     * Haalt de bbox op van de eerste de beste bbox in een url
     */
    public Bbox getBboxFromUrls() {
        Bbox bb = null;
        for (int i = 0; i < urls.size() && bb == null; i++) {
            bb = getBboxFromUrl((String) urls.get(i));
        }
        return bb;
    }
    /**
     * Haalt de bbox van de meegegeven url op (of null als die er niet is)
     */
    public Bbox getBboxFromUrl(String url) {
        if (url == null) {
            return null;
        }
        double[] bb = null;
        String stringBbox = getParameter(url, "bbox");
        if (stringBbox != null) {
            if (stringBbox.split(",").length != 4) {
                stringBbox = null;
            } else {
                bb = new double[4];
                try {
                    bb[0] = Double.parseDouble(stringBbox.split(",")[0]);
                    bb[1] = Double.parseDouble(stringBbox.split(",")[1]);
                    bb[2] = Double.parseDouble(stringBbox.split(",")[2]);
                    bb[3] = Double.parseDouble(stringBbox.split(",")[3]);
                } catch (NumberFormatException nfe) {
                    bb = null;
                    log.debug("Geen geldige double waarden in de bbox: " + stringBbox);
                }
            }
        }
        if (bb != null) {
            return new Bbox(bb);
        } else {
            return null;
        }
    }
    /**
     *Haalt de breedte en hoogte uit de eerste url waar hij dat vind
     */
    public Integer[] getWidthAndHeightFromUrls() {
        Integer[] hw = null;
        for (int i = 0; i < urls.size() && hw == null; i++) {
            hw = getWidthAndHeightFromUrl((String) urls.get(i));
        }
        return hw;
    }

    public Integer[] getWidthAndHeightFromUrl(String url) {
        String heightString = getParameter(url, "height");
        String widthString = getParameter(url, "width");
        Integer[] result = null;
        if (heightString != null && widthString != null) {
            try {
                result = new Integer[2];
                result[0] = new Integer(widthString);
                result[1] = new Integer(heightString);
            } catch (NumberFormatException nfe) {
                result = null;
                log.debug("Height en/of Width zijn geen integers: Heigth: " + heightString + "Width: " + widthString);
            }
        }
        return result;
    }    
}
