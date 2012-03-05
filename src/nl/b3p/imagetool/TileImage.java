package nl.b3p.imagetool;

/**
 *
 * @author Boy de Wit
 */
public class TileImage {
    
    private int posX;
    private int posY;
    private int imageWidth;
    private int imageHeight;
    
    private CombineImageUrl combineImageUrl;

    public TileImage() {
    }

    public TileImage(int posX, int posY, int imageWidth, int imageheight) {
        this.posX = posX;
        this.posY = posY;
        this.imageWidth = imageWidth;
        this.imageHeight = imageheight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public CombineImageUrl getCombineImageUrl() {
        return combineImageUrl;
    }

    public void setCombineImageUrl(CombineImageUrl combineImageUrl) {
        this.combineImageUrl = combineImageUrl;
    }
}
