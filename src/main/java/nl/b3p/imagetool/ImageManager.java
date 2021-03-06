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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nl.b3p.commons.services.B3PCredentials;
import nl.b3p.commons.services.HttpClientConfigured;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ImageManager definition:
 *
 */
public class ImageManager {

    private static final Log log = LogFactory.getLog(ImageManager.class);

    private static final int MAX_TREADS = 8;

    private static int instanceCount = 0;

    private final List<ImageCollector> ics = new ArrayList<ImageCollector>();
    private HttpClientConfigured hcc = null;
    private ExecutorService threadPool = null;
    private CompletionService<ImageCollector> pool = null;

    private final int instanceNumber = ++instanceCount;

    protected static final String host = null;
    protected static final int port = -1;

    public ImageManager(List urls, int maxResponseTime) {
        this(urls, maxResponseTime, null, null);
    }

    public ImageManager(List<CombineImageUrl> urls, int maxResponseTime, String uname, String pw) {
        if (urls == null || urls.size() <= 0) {
            throw new IllegalArgumentException();
        }
        log.info(String.format("%s initialized with %d urls, max response time %dms", this.getClass().getName(), urls.size(), maxResponseTime));

        threadPool = Executors.newFixedThreadPool(MAX_TREADS);
        pool = new ExecutorCompletionService<ImageCollector>(threadPool);

        B3PCredentials credentials = new B3PCredentials();
        credentials.setUserName(uname);
        credentials.setPassword(pw);
        //preemptive not possible, varying hosts possible
        credentials.setPreemptive(false);

        hcc = new HttpClientConfigured(credentials, maxResponseTime);

        for (CombineImageUrl ciu : urls) {
            ImageCollector ic = null;
            if (ciu instanceof CombineWmsUrl) {
                ic = new ImageCollector(this, ciu, hcc);
            } else if (ciu instanceof CombineArcIMSUrl) {
                ic = new ArcImsImageCollector(this, ciu, hcc);
            } else if (ciu instanceof CombineArcServerUrl) {
                ic = new ArcServerImageCollector(this, ciu, hcc);
            } else {
                ic = new ImageCollector(this, ciu, hcc);
            }
            ics.add(ic);
        }
    }

    public void process() throws Exception {
        for (ImageCollector ic : ics){
            if (ic.getStatus() == ImageCollector.NEW) {
                pool.submit(ic);
            }
        }
        //wait for all to complete. Wait max 5 min
        for (int i = 0; i < ics.size(); i++) {
            pool.poll(5, TimeUnit.MINUTES).get();
        }
    }
    /**
     * Combine all the images recieved
     * @return a combined image
     * @throws Exception
     */
    public List<ReferencedImage> getCombinedImages() throws Exception {
        ImageCollector ic = null;
        Iterator it = ics.iterator();
        List<ReferencedImage> allImages = new ArrayList<ReferencedImage>();
        while (it.hasNext()) {
            ic = (ImageCollector) it.next();
            int status = ic.getStatus();
            if(status != ImageCollector.COMPLETED) {
                // problem with one of sp's, but we continue with the rest!
            } else {
                ReferencedImage image =new ReferencedImage(ic);
                CombineImageUrl ciu = ic.getCombinedImageUrl();
                image.setAlpha(ciu.getAlpha());
                if (ciu instanceof CombineStaticImageUrl){
                    CombineStaticImageUrl csiu = (CombineStaticImageUrl)ciu;
                    image.setHeight(csiu.getHeight());
                    image.setWidth(csiu.getWidth());
                    image.setX(csiu.getX());
                    image.setY(csiu.getY());
                }
                allImages.add(image);
            }
        }
        if (allImages.size() > 0) {
            return allImages;
        } else {
            return null;
        }

    }

    public void close() {
        if (hcc!=null) {
            hcc.close();
        }
        if (threadPool!=null) {
            threadPool.shutdown();
        }
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }
}
