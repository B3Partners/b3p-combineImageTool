package nl.b3p.imagetool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * B3partners B.V. http://www.b3partners.nl
 * @author Roy
 * Created on 20-okt-2009, 10:30:38
 */
public class CombineImageUrl {
    private static final Log log = LogFactory.getLog(CombineImageSettings.class);
    private String url=null;
    private Float alpha =null;

    public CombineImageUrl(){}
    public CombineImageUrl(String url, Float alpha){
        setUrl(url);
        setAlpha(alpha);
    }
    public CombineImageUrl(String u){
        int alphaIndex=u.indexOf("#");
        Float al=null;
        String realUrl=u;
        if (alphaIndex > 0){
            realUrl=u.substring(0, alphaIndex);
            try{
                al=new Float(u.substring(alphaIndex+1,u.length()));
            }catch(Exception e){
                log.error("Fout bij parsen van Alpha: ",e);
                al=null;
            }
        }
        setUrl(realUrl);
        setAlpha(al);
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

    /**
     * @return the alpha
     */
    public Float getAlpha() {
        return alpha;
    }

    /**
     * @param alpha the alpha to set
     */
    public void setAlpha(Float alpha) {
        if (alpha==null){
            this.alpha=null;
        }else if (alpha > 1){
            alpha= new Float("1");
        }else if (alpha < 0){
            alpha= new Float("0");
        }
        this.alpha = alpha;
    }
}
