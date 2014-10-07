/*
 * Copyright (C) 2012 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.imagetool;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.xml.xpath.XPathExpressionException;
import nl.b3p.commons.services.HttpClientConfigured;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.jdom.JDOMException;

/**
 * Class that gets the image in 2 steps. First sumbit the body and then recieve the url from the response.
 * Get the image with the url in the response.
 * @author Roy Braam
 */
public abstract class PrePostImageCollector extends ImageCollector{
    private static final Log log = LogFactory.getLog(PrePostImageCollector.class);
    private String body;
    
    public PrePostImageCollector(CombineImageUrl ciu, HttpClientConfigured client){
        super(ciu, client);
        this.body=ciu.getBody();
    }
    
    @Override
    protected BufferedImage loadImage(String url) throws IOException, Exception {
        String theUrl = url;
        if (this.getBody() != null) {
            HttpPost post = new HttpPost(url);
            HttpResponse response = client.execute(post);
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (statusCode != 200) {
                    throw new Exception("Error connecting to server. HTTP status code: " + statusCode);
                }
                String returnXML = EntityUtils.toString(entity);
                theUrl = getUrlFromXML(returnXML);
                if (theUrl == null && returnXML != null) {
                    throw new Exception("Error getting the correct url. Server returned: \n" + returnXML);
                }
            } finally {
                if (response instanceof CloseableHttpResponse) {
                    ((CloseableHttpResponse) response).close();
                }
            }

        }
        return super.loadImage(theUrl);
    }
    
    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public String getBody() {
        return body;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    //</editor-fold>
    /**
     * Recieve the url from the xml.
     * @param returnXML The xml that is recieved bij doing a post request
     * @return the url.
     * @throws XPathExpressionException
     * @throws JDOMException
     * @throws IOException 
     */
    protected abstract String getUrlFromXML(String returnXML) throws XPathExpressionException, JDOMException, IOException;

}
