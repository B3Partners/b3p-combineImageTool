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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import nl.b3p.commons.services.HttpClientConfigured;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

/**
 * ImageCollector definition:
 */
public class ImageCollector implements Callable<ImageCollector> {

    private static final Log log = LogFactory.getLog(ImageCollector.class);

    private static int instanceCount = 0;
    private final int instanceNumber = ++instanceCount;
    protected final String preLog;

    public static final int NEW = 0;
    public static final int ACTIVE = 1;
    public static final int COMPLETED = 2;
    public static final int WARNING = 3;
    public static final int ERROR = 4;

    private final ImageManager manager;
    private int status = NEW;
    private BufferedImage bufferedImage;
    private CombineImageUrl combinedImageUrl = null;
    protected HttpClientConfigured client = null;
    private File tempFile;
    private String mime;
    private boolean imageLoaded = false;

    public ImageCollector(ImageManager manager, CombineImageUrl ciu, HttpClientConfigured client) {
        this.manager = manager;
        this.combinedImageUrl=ciu;
        this.client = client;

        this.preLog = "im #" + manager.getInstanceNumber() +  ",ic #" + instanceNumber + " " + this.getClass().getName() + ": ";
    }

    public ImageCollector call() throws Exception {
        status = ACTIVE;
        try {
            log.info(preLog + "starting download from URL " + combinedImageUrl);
            downloadImage(combinedImageUrl.getUrl());

            status = COMPLETED;
        } catch (Exception ex) {
            log.error(String.format(preLog + "exception downloading image from URL %s: %s: %s",
                    combinedImageUrl.toString(),
                    ex.getClass().getName(),
                    ex.getMessage()
            ));
            log.debug("Full stacktrace for exception downloading image", ex);
            status = ERROR;
        }
        return this;
    }

    protected void downloadImage(String url) throws Exception {

        FileOutputStream out = null;
        HttpResponse response = null;
        try {
            HttpGet request = new HttpGet(url);
            response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new Exception("HTTP status code: " + statusCode);
            }
            HttpEntity entity = response.getEntity();
            Header header = entity.getContentType();
            mime = null;
            if (header == null || header.getValue().isEmpty()) {
                mime = "image/png";
            } else {
                mime = header.getValue();
            }

            tempFile = File.createTempFile("imagetool-im-" + manager.getInstanceNumber() + "-ic-" + instanceNumber, null);
            tempFile.deleteOnExit();
            out = new FileOutputStream(tempFile);

            entity.writeTo(out);
            log.info(String.format(preLog + "succesfully downloaded, stored %d bytes in temporary file %s",
                    tempFile.length(),
                    tempFile.getAbsolutePath()));
        } catch(Exception e) {
            if(tempFile != null) {
                try {
                    log.debug(preLog + "exception writing downloaded image to temp file, deleting " + tempFile);
                    tempFile.delete();
                } catch(Exception e2) {
                }
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(out);
            client.close(response);
        }
    }

    private void loadImage() throws Exception {
        imageLoaded = true;
        FileInputStream fis = null;
        try {
            log.info(preLog + "reading image from temporary file " + tempFile);
            fis = new FileInputStream(tempFile);
            bufferedImage = ImageTool.readImage(fis, mime);
            //bufferedImage = ImageIO.read(tempFile);
        } finally {
            IOUtils.closeQuietly(fis);
            if(!tempFile.delete()) {
                log.error(preLog + "loadImage(): could not delete temporary file " + tempFile.getAbsolutePath());
            } else {
                log.debug(preLog + "loadImage(): deleted temporary file");
                tempFile = null;
            }
        }
    }

    /**
     * Call this after clearing all references retrieved from getBufferedImage() if
     * you still keep referencing this ImageCollector for some reason.
     */
    public void dispose() {
        bufferedImage = null;
        if(tempFile != null) {
            if(!tempFile.delete()) {
                log.error(preLog + "dispose(): could not delete temporary file " + tempFile.getAbsolutePath());
            } else {
                log.debug(preLog + "dispose(): deleted temporary file");
            }
        }
    }

    /**
     * Caller MUST DIRECTLY CALL dispose() after use.
     * @return the downloaded and decoded image or null if the status is not COMPLETED,
     *  he method threw an exception when called before, or dispose() has been called
     * @throws java.io.IOException if an exception occurs reading the downloaded image
     */
    public BufferedImage getBufferedImage() throws Exception {
        if(status != COMPLETED) {
            return null;
        }
        if(!imageLoaded) {
            loadImage();
        }
        return bufferedImage;
    }

    public int getStatus() {
        return status;
    }

    public CombineImageUrl getCombinedImageUrl() {
        return combinedImageUrl;
    }

    public ImageManager getManager() {
        return manager;
    }
}
