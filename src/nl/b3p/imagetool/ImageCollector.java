/*
 * B3P Kaartenbalie is a OGC WMS/WFS proxy that adds functionality
 * for authentication/authorization, pricing and usage reporting.
 *
 * Copyright 2006, 2007, 2008 B3Partners BV
 * 
 * This file is part of B3P Kaartenbalie.
 * 
 * B3P Kaartenbalie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * B3P Kaartenbalie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with B3P Kaartenbalie.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.imagetool;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import nl.b3p.commons.services.HttpClientConfigured;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * ImageCollector definition:
 */
public class ImageCollector implements Callable<ImageCollector> {

    private static final Log log = LogFactory.getLog(ImageCollector.class);
    public static final int NEW = 0;
    public static final int ACTIVE = 1;
    public static final int COMPLETED = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;
    private int status = NEW;
    private String message = null;
    private BufferedImage bufferedImage;
    private CombineImageUrl combinedImageUrl = null;
    protected HttpClientConfigured client = null;

    public ImageCollector(CombineImageUrl ciu, HttpClientConfigured client) {
        this.combinedImageUrl=ciu;
        this.client = client;
        this.setMessage("Still downloading...");
    }

    public ImageCollector call() throws Exception {        
        status = ACTIVE;
        if ((getUrl() == null || getUrl().length() == 0) && getRealUrl() == null) {
            return this;
        }

        try {
            if (getRealUrl() != null) {
                setBufferedImage(ImageIO.read(getRealUrl()));
            } else {
                setBufferedImage(loadImage(getUrl()));                
            }
            setMessage("");
            setStatus(COMPLETED);
        } catch (Exception ex) {
            log.warn("error callimage collector: ", ex);
            setStatus(ERROR);
        } 
        return this;
    }
    /**
     * Load the image with a http-get
     * @param url The url to the image
     * @param user username
     * @param pass password
     * @return The image
     * @throws IOException
     * @throws Exception 
     */
    protected BufferedImage loadImage(String url) throws IOException, Exception {
        HttpGet request = new HttpGet(url);
        HttpResponse response = client.execute(request);
        try {            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception("Error connecting to server. HTTP status code: " + statusCode);
            }
            HttpEntity entity = response.getEntity();
            Header header = entity.getContentType();
            String mime = null;
            if (header == null || header.getValue().isEmpty()) {
                mime = "image/png";
            } else {
                mime = header.getValue();      
            }                  
            
            return ImageTool.readImage(entity.getContent(), mime);
            
        }finally{
            if (response instanceof CloseableHttpResponse) {
                ((CloseableHttpResponse)response).close();
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and setters">
    /**
     * @return the url
     */
    public String getUrl() {
        if (combinedImageUrl==null)
            return null;
        return getCombinedImageUrl().getUrl();
    }
    
    public URL getRealUrl(){ 
        if (combinedImageUrl==null)
            return null;
        return getCombinedImageUrl().getRealUrl();
    }
    
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public CombineImageUrl getCombinedImageUrl() {
        return combinedImageUrl;
    }

    public void setCombineImageUrl(CombineImageUrl ciu) {
        this.combinedImageUrl = ciu;
    }
    //</editor-fold>
}
