/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.b3p.imagetool;
/**
 *
 * @author meine
 */
public class CombineTMSUrl extends CombineTileImageUrl {

    public CombineTMSUrl(CombineTileImageUrl ctiu) {
        super(ctiu);
    }

    public CombineTMSUrl() {
        super();
    }

    @Override
    protected String createUrl(ImageBbox imageBbox, Bbox tileBbox, int indexX, int indexY, int zoomlevel) {
        String requestUrl = this.url + "/" + zoomlevel + "/" + indexX + "/" + indexY + "." + this.extension;
        return requestUrl;
    }

    /**
     * Get the index number X of the tile on coordinate 'xCoord'.
     * @param xCoord The x coord.
     * @param res The resolution for which the tile should be calculated
     * @param max If it is the max, increase the index with one more, to prevent missing tiles
     * @return The index of the tile specified at the coordinate/resolution pair
     * @see coremodel.service.tiling.factory.TileFactoryInterface#getTileIndexX
     */
    @Override
    public Integer getTileIndexX(Double xCoord, Double res, boolean max) {
        Double tileSpanX = res * getTileWidth();
        
        Double tileIndexX = (xCoord - this.getServiceBbox().getMinx()) / (tileSpanX + epsilon);
        if(max){
            tileIndexX = Math.ceil(tileIndexX);
        }else{
            tileIndexX = Math.floor(tileIndexX);
        }
        
        if (tileIndexX < 0) {
            tileIndexX = 0.0;
        }
        Double maxBboxX = Math.floor((this.getServiceBbox().getMaxx() - this.getServiceBbox().getMinx()) / (tileSpanX + epsilon));
        if (tileIndexX > maxBboxX) {
            tileIndexX = maxBboxX;
        }
        return tileIndexX.intValue();
    }

    /**
     * Get the index number Y of the tile on coordinate 'yCoord' o
     * @param xCoord The x coord.
     * @param res The resolution for which the tile should be calculated
     * @param max If it is the max, increase the index with one more, to prevent missing tiles
     * @return The index of the tile specified at the coordinate/resolution pair
     */
    @Override
    public Integer getTileIndexY(double yCoord, Double res, boolean max) {
        Double tileSpanY = res * getTileHeight();
        Double tileIndexY = (yCoord - this.getServiceBbox().getMiny()) / (tileSpanY + epsilon);
        if(max){
            tileIndexY = Math.ceil(tileIndexY);
        }else{
            tileIndexY = Math.floor(tileIndexY);
        }
        if (tileIndexY < 0) {
            tileIndexY = 0.0;
        }
        Double maxBboxY = Math.floor((this.getServiceBbox().getMaxy() - this.getServiceBbox().getMiny()) / (tileSpanY + epsilon));
        if (tileIndexY > maxBboxY) {
            tileIndexY = maxBboxY;
        }
        
        return tileIndexY.intValue();
    }

    @Override
    protected int getTileDirectionX() {
        return 1;
    }

    @Override
    protected int getTileDirectionY() {
        return -1;
    }
}
