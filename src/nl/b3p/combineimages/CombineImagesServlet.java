/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.combineimages;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.commons.services.FormUtils;
import nl.b3p.imagetool.CombineImageSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Roy
 */
public class CombineImagesServlet extends HttpServlet {

    private static String WIDTH = "width";
    private static String HEIGHT = "height";
    private static String MIMETYPE = "mimetype";
    private static String BBOX = "bbox";
    private static String htmlTitle = "Plaatje";
    private static String HTMLOUTPUT = "htmloutput";
    private static String htmlOutput = "<html><head><title>[HTMLTITLE]</title></head><body><img src=\"[IMAGESOURCE]\"/></body></html>";
    private static ArrayList extraUrls;
    private static final Log log = LogFactory.getLog(CombineImagesServlet.class);
    private static Random rg = null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            CombineImageSettings settings = getCombineImageSettings(request);
            String imageId = uniqueName("");
            request.getSession().setAttribute(imageId, settings);
            String servletUrl = request.getRequestURL().toString();
            String servletName = this.getServletName().substring(0, this.getServletName().length() - 7);
            servletUrl = servletUrl.substring(0, servletUrl.length() - servletName.length()) + "CreateImage";
            String imageSource = servletUrl + "?imageId=" + imageId;
            String html = htmlOutput;
            if (html.indexOf("[HTMLTITLE]") >= 0) {
                html = html.replaceAll("\\[HTMLTITLE\\]", htmlTitle);
            }
            if (html.indexOf("[IMAGESOURCE]") >= 0) {
                html = html.replaceAll("\\[IMAGESOURCE\\]", imageSource);
            } else {
                html += imageSource;
            }
            out.println(html);
        } catch (Exception e) {
            log.error("Error combining image: ", e);
            out.write(e.getMessage());
        } finally {
            out.close();
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (config.getInitParameter("extraUrls") != null && config.getInitParameter("extraUrls").length() > 0) {
            extraUrls = new ArrayList();
            String[] allUrls = config.getInitParameter("extraUrls").split(";");
            for (int i = 0; i < allUrls.length; i++) {
                extraUrls.add(allUrls[i]);
            }
        }
        if (config.getInitParameter("htmlTitle") != null && config.getInitParameter("htmlTitle").length() > 0) {
            htmlTitle = config.getInitParameter("htmlTitle");
        }
        if (config.getInitParameter("htmlOutput") != null && config.getInitParameter("htmlOutput").length() > 0) {
            htmlOutput = config.getInitParameter("htmlOutput");
        }
        rg = new Random();
    }

    public static String uniqueName(String extension) {
        // Gebruik tijd in milliseconden om gerekend naar een radix van 36.
        // Hierdoor ontstaat een lekker korte code.
        long now = (new Date()).getTime();
        String val1 = Long.toString(now, Character.MAX_RADIX).toUpperCase();
        // random nummer er aanplakken om zeker te zijn van unieke code
        if (rg==null) {
            rg = new Random();
        }
        long rnum = (long) rg.nextInt(1000);
        String val2 = Long.toString(rnum, Character.MAX_RADIX).toUpperCase();
        String thePath = "";
        return val1 + val2 + extension;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public static CombineImageSettings getCombineImageSettings(HttpServletRequest request) throws ServletException {
        String url = FormUtils.nullIfEmpty(request.getParameter("urls"));
        String wkt = FormUtils.nullIfEmpty(request.getParameter("wkts"));
        CombineImageSettings settings = new CombineImageSettings();
        if (url != null) {
            log.debug("Urls: " + url);
            String[] urls = url.split(";");
            settings.setUrls(urls);
            String[] wkts = null;
            if (wkt != null) {
                log.debug("WKT: " + wkt);
                wkts = wkt.split(";");
                settings.setWktGeoms(wkts);
            }
        } else {
            throw new ServletException("Er zijn geen paden naar plaatjes gevonden");
        }
        /**
         * Als width en of height zijn meegegeven vervang dan die in de settings.
         */
        if (request.getParameter(WIDTH) != null && request.getParameter(WIDTH).length() > 0) {
            settings.setWidth(new Integer(request.getParameter(WIDTH)));
        }
        if (request.getParameter(HEIGHT) != null && request.getParameter(HEIGHT).length() > 0) {
            settings.setHeight(new Integer(request.getParameter(HEIGHT)));
        }
        if (request.getParameter(MIMETYPE) != null && request.getParameter(MIMETYPE).length() > 0) {
            settings.setMimeType(request.getParameter(MIMETYPE));
        }
        if (request.getParameter(BBOX) != null && request.getParameter(BBOX).length() > 0) {
            settings.setBbox(request.getParameter(BBOX));
        }
        if (request.getParameter(HTMLOUTPUT) != null && request.getParameter(HTMLOUTPUT).length() > 0) {
            htmlOutput = request.getParameter(HTMLOUTPUT);
        }
        if (extraUrls != null) {
            settings.getUrls().addAll(extraUrls);
        }
        return settings;
    }
}
