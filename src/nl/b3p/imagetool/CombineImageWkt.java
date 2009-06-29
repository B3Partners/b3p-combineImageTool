/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.imagetool;

import java.awt.Color;

/**
 *
 * @author Roy
 */
public class CombineImageWkt {
    private String wktGeom="";
    private Color color = null;
    private String label=null;

    public CombineImageWkt(String wktGeomString){
        setWktGeom(wktGeomString);
    }
    public CombineImageWkt(String wktGeomString, String color){
        setWktGeom(wktGeomString);        
        setColor(color);
    }
    /**
     * @return the wktGeom
     */
    public String getWktGeom() {
        return wktGeom;
    }

    /**
     * @param wktGeom the wktGeom to set
     */
    public void setWktGeom(String wktGeom) {
        this.wktGeom = wktGeom;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }
    public void setColor(String hexrgb) {
        if (hexrgb.length()>0)
            this.color = new Color( Integer.parseInt(( hexrgb ), 16) );
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
