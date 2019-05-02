
public class MacroBlock{

    private int row;
    private int col;

    private MImage img;

    private int[][][] rgbs;   
    private int[][] grayValues;
    private double[][] errorBlocks;
    private MacroBlock bestMatchBlock;
    private int[] motionVectors; 

    public MacroBlock(){

    }

    public MacroBlock(int row, int col, int n, MImage img){
        this.row = row;
        this.col = col;

        this.img = img; 

        this.rgbs = new int[n][n][3];
        this.grayValues = new int[n][n];
        this.errorBlocks = new double[n][n];
    }

    /* GETTERS AND SETTERS */

    public void setPixel(int row, int col, int[] rgb){
        this.rgbs[row][col] = rgb;
    }

    public int[] getPixel(int row, int col){
        return this.rgbs[row][col];
    }

    public void setErrorBlock(int row, int col, double errVal){
        this.errorBlocks[row][col] = errVal;
    }

    public double getErrorBlock(int row, int col){
        return this.errorBlocks[row][col];
    }

    public void setGrayValue(int row, int col, int value){
        this.grayValues[row][col] = value;
    }

    public int getGrayValue(int row, int col){
        return this.grayValues[row][col];
    }

    public int getRow() {
        return this.row;
    }

    public void setGrayValues(int[][] grayValues) {
        this.grayValues = grayValues;
    }

    public int[] getMotionVectors() {
        return this.motionVectors;
    }

    public void setMotionVectors(int[] motionVectors) {
        this.motionVectors = motionVectors;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return this.col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public MImage getImg() {
        return this.img;
    }

    public void setImg(MImage img) {
        this.img = img;
    }

    public int[][][] getRgbs() {
        return this.rgbs;
    }

    public void setRgbs(int[][][] rgbs) {
        this.rgbs = rgbs;
    }
   
    public MacroBlock getBestMatchBlock() {
        return this.bestMatchBlock;
    }

    public void setBestMatchBlock(MacroBlock bestMatchBlock) {
        this.bestMatchBlock = bestMatchBlock;
    }

    /* GETTERS AND SETTERS */

    
}