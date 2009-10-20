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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Stack;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ImageCollector definition:
 */
public class ImageCollector extends Thread {

    private static final Log log = LogFactory.getLog(ImageCollector.class);
    private int maxResponseTime = 10000;
    public static final int NEW = 0;
    public static final int ACTIVE = 1;
    public static final int COMPLETED = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;
    private int status = NEW;
    private String message = null;
    private BufferedImage bufferedImage;
    private static Stack stack = new Stack();
    private String url;
    private Float alpha=null;

    public ImageCollector(String url, int maxResponseTime) {
        this.url = url;
        this.maxResponseTime=maxResponseTime;
        this.setMessage("Still downloading...");
    }
    public ImageCollector(CombineImageUrl ciu, int maxResponseTime) {
        this.url = ciu.getUrl();
        this.alpha= ciu.getAlpha();
        this.maxResponseTime=maxResponseTime;
        this.setMessage("Still downloading...");
    }

    public void processNew() throws InterruptedException {
        status = ACTIVE;
        start();        
    }

    public void processWaiting() throws InterruptedException {
        join(maxResponseTime);
    }

    public void run() {
        if (getUrl()!=null && getUrl().length()>0){
            HttpClient client = new HttpClient();
            HttpMethod method = null;
            method = new GetMethod(getUrl());
            client.getHttpConnectionManager().getParams().setConnectionTimeout(maxResponseTime);
            try {
                int statusCode = client.executeMethod(method);
                if (statusCode != HttpStatus.SC_OK) {
                    throw new Exception("Error connecting to server. HTTP status code: " + statusCode);
                }
                String mime = method.getResponseHeader("Content-Type").getValue();
                setBufferedImage(ImageTool.readImage(method, mime));
                if (alpha!=null && bufferedImage!=null){
                    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, alpha);
                    ((Graphics2D)bufferedImage.getGraphics()).setComposite(ac);
                }
                setMessage("");
                setStatus(COMPLETED);
            } catch (Exception ex) {
                log.error("error callimage collector: ", ex);
                setStatus(ERROR);
            } finally {
                if (method!=null)
                    method.releaseConnection();
            }
        }
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;        
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


}
