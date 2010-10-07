package nl.b3p.combineimages;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nl.b3p.imagetool.CombineImageSettings;
import nl.b3p.imagetool.CombineImagesHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Roy *
 */
public class CreateImageServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(CreateImageServlet.class);
    private int maxResponseTime = 10000;
    
    //private String returnMime = "image/png";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OutputStream os = response.getOutputStream();
        try {
            //response.setContentType("text/html;charset=UTF-8");
            String imageId=request.getParameter("imageId");
            if (imageId!=null && request.getSession().getAttribute(imageId)!=null){
                CombineImageSettings settings = (CombineImageSettings) request.getSession().getAttribute(imageId);
                response.setContentType(settings.getMimeType());
                response.setDateHeader("Expires", System.currentTimeMillis() + (1000 * 60 * 60 * 24));                
                request.getSession().removeAttribute(imageId);
                CombineImagesHandler.combineImage(response.getOutputStream(), settings,settings.getMimeType(),maxResponseTime);
            }
        } catch (Exception e) {
            log.error("Fout opgetreden: ", e);
            throw new ServletException(e);
        }
        finally{
            os.close();
        }
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (config.getInitParameter("maxResponseTime") != null) {
            try {
                int mrt = Integer.parseInt(config.getInitParameter("maxResponseTime"));
                maxResponseTime = mrt;
            } catch (NumberFormatException nfe) {
                log.error("Fout bij laden init parameter maxResponseTime: Geen nummer", nfe);
            }
        }
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
}
