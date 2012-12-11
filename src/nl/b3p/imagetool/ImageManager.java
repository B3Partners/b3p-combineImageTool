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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ImageManager definition:
 *
 */
public class ImageManager {

    private final Log log = LogFactory.getLog(this.getClass());
    private List ics = new ArrayList();
    private int maxResponseTime = 10000;
    private int maxThreads = 4;

    public ImageManager(List urls, int maxResponseTime) {
        this(urls, maxResponseTime, null, null);
    }

    public ImageManager(List urls, int maxResponseTime, String uname, String pw) {
        this.maxResponseTime = maxResponseTime;
        if (urls == null || urls.size() <= 0) {
            return;
        }
        for (int i = 0; i < urls.size(); i++) {
            Object o = urls.get(i);
            ImageCollector ic = null;
            if (o instanceof String) {
                ic = new ImageCollector((String) o, maxResponseTime);
            } else if (o instanceof CombineImageUrl) {
                ic = new ImageCollector((CombineImageUrl) o, maxResponseTime, uname, pw);
            }
            ics.add(ic);
        }
    }

    public void process() throws Exception {
        log.debug("IMAGE LIST SIZE: " + ics.size());
        
        process(0, maxThreads);
    }

    List<ImageCollector> activeThreads = new ArrayList();
    
    public void process(Integer startIndex, Integer endIndex) throws Exception { 
        if (endIndex >= ics.size()) {
            endIndex = ics.size() -1;
        }
        
        boolean active = false;
        if (activeThreads.size() > 0) {
            for (ImageCollector ic : activeThreads) {
                if (ic.getStatus() == ImageCollector.ACTIVE) {
                    active = true;
                }
            }
            
            if (active) {
                process(startIndex, endIndex);
            } else {
                activeThreads.clear();
            }
        }
        
        // Hier worden de threads gestart
        ImageCollector ic;
        for (int i = startIndex; i <= endIndex; i++) {
            if (i <= ics.size() - 1) {
                ic = (ImageCollector) ics.get(i);
                if (ic.getStatus() == ImageCollector.NEW) {
                    ic.processNew();
                }
            }
        }

        // Hier wordt op de threads gewacht tot ze klaar zijn.
        for (int i = startIndex; i <= endIndex; i++) {
            ic = (ImageCollector) ics.get(i);
            if (ic.getStatus() == ImageCollector.ACTIVE) {//if (ic.isAlive()) { /
                activeThreads.add(ic);
                
                log.debug("ADDED NEW THREAD: " + ic.getId());
                log.debug("START INDEX: " + startIndex);
                log.debug("END INDEX: " + endIndex);
                
                ic.processWaiting();
            }
        }
        
        if (endIndex < ics.size() -1) {
            int idx;
            
            if (endIndex + maxThreads >= ics.size()) {
                idx = ics.size()-1;
            } else {
                idx = endIndex + maxThreads;
            }
            
            process(endIndex, idx);
        }
    }

    public BufferedImage[] getCombinedImages() throws Exception {
        ImageCollector ic = null;
        Iterator it = ics.iterator();
        List allImages = new ArrayList();
        while (it.hasNext()) {
            ic = (ImageCollector) it.next();
            int status = ic.getStatus();
            if (status == ImageCollector.ERROR || ic.getBufferedImage() == null) {
                log.error(ic.getMessage() + " (Status: " + status + ")");
            } else if (status != ImageCollector.COMPLETED) {
                // problem with one of sp's, but we continue with the rest!
                log.error(ic.getMessage() + " (Status: " + status + ")");
            } else {
                allImages.add(ic.getBufferedImage());
            }
        }
        if (allImages.size() > 0) {
            BufferedImage[] bis = new BufferedImage[allImages.size()];
            for (int i = 0; i < allImages.size(); i++) {
                bis[i] = (BufferedImage) allImages.get(i);
            }
            return bis;
        } else {
            return null;
        }

    }
}
