
public class DCTTransformation{

    private int compressionLevel;
    private String name;

    private MImage copy;
    private MImage resized;
    private double[][][] transformed;

    private double[][] y;
    private double[][] cb;
    private double[][] cr;

    private double[][] sampledCb;
    private double[][] sampledCr;

    private double[][] dctY;
    private double[][] dctCb;
    private double[][] dctCr;

    private double[][] quantizedY;
    private double[][] quantizedCb;
    private double[][] quantizedCr;

    private double[][] tableY = {
        {4, 4, 4, 8, 8, 16, 16, 32},
        {4, 4, 4, 8, 8, 16, 16, 32},
        {4, 4, 8, 8, 16, 16, 32, 32},
        {8, 8, 8, 16, 16, 32, 32, 32},
        {8, 8, 16, 16, 32, 32, 32, 32},
        {16, 16, 16, 32, 32, 32, 32, 32},
        {16, 16, 32, 32, 32, 32, 32, 32},
        {32, 32, 32, 32, 32, 32, 32, 32}
    };

    private double[][] tableCbCr = {
        {8, 8, 8, 16, 32, 32, 32, 32},
        {8, 8, 8, 16, 32, 32, 32, 32},
        {8, 8, 16, 32, 32, 32, 32, 32},
        {16, 16, 32, 32, 32, 32, 32, 32},
        {32, 32, 32, 32, 32, 32, 32, 32},
        {32, 32, 32, 32, 32, 32, 32, 32},
        {32, 32, 32, 32, 32, 32, 32, 32},
        {32, 32, 32, 32, 32, 32, 32, 32}
    };

    public DCTTransformation(MImage original, int compressionLevel){
        this.compressionLevel = compressionLevel;
        this.name = original.getName();
        this.copy = deepCopy(original);
        this.resized = null;
        this.transformed = null;
        this.y = null;
        this.cb = null;
        this.cr = null;
        this.sampledCb = null;
        this.sampledCr = null;

        this.dctY = null;
        this.dctCb = null;
        this.dctCr = null;

        this.quantizedY = null;
        this.quantizedCb = null;
        this.quantizedCr = null;
    }

    private MImage deepCopy(MImage original){
        MImage copy = new MImage(original.getW(), original.getH());
        for(int row = 0; row < original.getH(); row++){
            for(int col = 0; col < original.getW(); col++){
                int[] rgb = new int[3];
                original.getPixel(col, row, rgb);
                copy.setPixel(col, row, rgb);
            }
        }

        return copy;
    }

    /* ENCODING STEPS */

    public void resize(){
        int newH = (this.copy.getH() % 8 == 0) ? this.copy.getH() : (this.copy.getH() + (8 - (this.copy.getH() % 8)));
        int newW = (this.copy.getW() % 8 == 0) ? this.copy.getW() : (this.copy.getW() + (8 - (this.copy.getW() % 8)));
        this.resized = new MImage(newW, newH);
        
        // add the original rgb values from the copy of the original image
        for(int row = 0; row < this.copy.getH(); row++){
            for(int col = 0; col < this.copy.getW(); col++){
                int[] rgb = new int[3];
                this.copy.getPixel(col, row, rgb);
                this.resized.setPixel(col, row, rgb);
            }
        }
        // add black rgb to indexes that are remaining with new image size
        int[] blackRGB = {0, 0, 0};
        for(int row = this.copy.getH(); row < newH; row++){
            for(int col = this.copy.getW(); col < newW; col++){
                this.resized.setPixel(col, row, blackRGB);
            }
        }

    }

    public void colorSpaceTransformation(){
        this.transformed = new double[this.resized.getH()][this.resized.getW()][3];
        this.y = new double[this.resized.getH()][this.resized.getW()];
        this.cb = new double[this.resized.getH()][this.resized.getW()];
        this.cr = new double[this.resized.getH()][this.resized.getW()];
        for(int row = 0; row < this.resized.getH(); row++){
            for(int col = 0; col < this.resized.getW(); col++){
                int[] rgb = new int[3];
                this.resized.getPixel(col, row, rgb);
                double[] yCbCr = new double[3];
                yCbCr[0] = ((rgb[0] * 0.299) + (rgb[1] * 0.587) + (rgb[2] * 0.114) - 128);
                yCbCr[1] = ((rgb[0] * -0.1687) + (rgb[1] * -0.3313) + (rgb[2] * 0.5) - 0.5);
                yCbCr[2] = ((rgb[0] * 0.5) + (rgb[1] * -0.4187) + (rgb[2] * -0.0813) - 0.5);
                this.transformed[row][col] = yCbCr;
                this.y[row][col] = (int)Math.round(yCbCr[0]);
                this.cb[row][col] = (int)Math.round(yCbCr[1]);
                this.cr[row][col] = (int)Math.round(yCbCr[2]);
                // this.y[row][col] = yCbCr[0];
                // this.cb[row][col] = yCbCr[1];
                // this.cr[row][col] = yCbCr[2];
            }
        }
    }

    public void subSampling(){

        int newRowSize = ((this.resized.getH() / 2) % 8 == 0) ? (this.resized.getH() / 2) : (this.resized.getH() / 2) + (8 - ((this.resized.getH() / 2) % 8));
        int newColSize = ((this.resized.getW() / 2) % 8 == 0) ? (this.resized.getW() / 2) : (this.resized.getW() / 2) + (8 - ((this.resized.getW() / 2) % 8));
        this.sampledCb = new double[newRowSize][newColSize];
        this.sampledCr = new double[newRowSize][newColSize];

        for(int row = 0, newRow = 0; row < this.resized.getH(); newRow++, row+=2){
            for(int col = 0, newCol = 0; col < this.resized.getW(); newCol++, col+=2){
                double avgCbVal = 0.0;
                double avgCrVal = 0.0;
                for(int innerRow = row; innerRow < (row + 2); innerRow++){
                    for(int innerCol = col; innerCol < (col + 2); innerCol++){
                        avgCbVal += this.cb[innerRow][innerCol];
                        avgCrVal += this.cr[innerRow][innerCol];
                    } // end of innerCol for-loop
                } // end of innerRow for-loop
                avgCbVal /= 4;
                avgCrVal /= 4;
                this.sampledCb[newRow][newCol] = avgCbVal;
                this.sampledCr[newRow][newCol] = avgCrVal;
            } // end of col for-loop
        } // end of row for-loop
    }

    public void discreteCosineTransform(){
        this.dctY = new double[this.y.length][this.y[0].length];
        this.dctCb = new double[this.sampledCb.length][this.sampledCb[0].length];
        this.dctCr = new double[this.sampledCr.length][this.sampledCr[0].length];

        for(int row = 0; row < this.y.length; row+= 8){
            for(int col = 0; col < this.y[row].length; col+= 8){
            
                for(int u = 0, dctRow = row; u < 8; dctRow++, u++){
                    for(int v = 0, dctCol = col; v < 8; dctCol++, v++){

                        double cu = 1.0; 
                        double cv = 1.0;
                        if(u == 0){
                            cu = 1 / Math.sqrt(2);
                        }
                        if(v == 0){
                            cv = 1.0 / Math.sqrt(2);
                        }
                        // double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                        // double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                    
						double sum = 0.0;

                        for(int i = 0, yRow = row; i < 8; i++, yRow++){
                            for(int j = 0, yCol = col; j < 8; j++, yCol++){
                                // System.out.println("row: " + blockRow + " col: " + blockCol);
                                
                                double calc1 = Math.cos(((2 * i + 1) * u * Math.PI) / 16);
                                double calc2 = Math.cos(((2 * j + 1) * v * Math.PI) / 16);
                                double value = this.y[yRow][yCol];

                                sum += (value * calc1 * calc2);
                            }
                        }

                        sum *= ((cu * cv) / 4);
                        if(sum > Math.pow(2, 10)){
							sum = Math.pow(2, 10);
						}else if(sum < (-Math.pow(2, 10))){
							sum = (-Math.pow(2,10));
                        }
                        this.dctY[dctRow][dctCol] = sum;
                    } // end of u-loop
                } // end of v-loop


            }
        }

        for(int row = 0; row < this.sampledCb.length; row+= 8){
            for(int col = 0; col < this.sampledCb[row].length; col+= 8){
            
                for(int u = 0, dctRow = row; u < 8; dctRow++, u++){
                    for(int v = 0, dctCol = col; v < 8; dctCol++, v++){

                        // double cu = 1.0; 
                        // double cv = 1.0;
                        // if(u == 0){
                        //     cu = 1 / Math.sqrt(2);
                        // }
                        // if(v == 0){
                        //     cv = 1.0 / Math.sqrt(2);
                        // }
                        double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                        double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                    
						double cbSum = 0.0;
                        double crSum = 0.0;

                        for(int i = 0, yRow = row; i < 8; i++, yRow++){
                            for(int j = 0, yCol = col; j < 8; j++, yCol++){
                                // System.out.println("row: " + blockRow + " col: " + blockCol);
                                
                                double calc1 = Math.cos(((2 * i + 1) * u * Math.PI) / 16);
                                double calc2 = Math.cos(((2 * j + 1) * v * Math.PI) / 16);
                                // double value = this.y[yRow][yCol];
                                double cbValue = this.sampledCb[yRow][yCol];
                                double crValue = this.sampledCr[yRow][yCol];

                                // sum += (value * calc1 * calc2);
                                cbSum += (cbValue * calc1 * calc2);
                                crSum += (crValue * calc1 * calc2);
                            }
                        }

                        cbSum *= ((cu * cv) / 4);
                        crSum *= ((cu * cv) / 4);

                        if(cbSum > Math.pow(2, 10)){
							cbSum = Math.pow(2, 10);
						}else if(cbSum < (-Math.pow(2, 10))){
							cbSum = (-Math.pow(2,10));
                        }

                        if(crSum > Math.pow(2, 10)){
							crSum = Math.pow(2, 10);
						}else if(crSum < (-Math.pow(2, 10))){
							crSum = (-Math.pow(2,10));
                        }
                        // this.dctY[dctRow][dctCol] = sum;
                        this.dctCb[dctRow][dctCol] = cbSum;
                        this.dctCr[dctRow][dctCol] = crSum;
                    } // end of u-loop
                } // end of v-loop


            }
        }

    }

    public void quantization(){
        this.quantizedY = new double[this.dctY.length][this.dctY[0].length];
        this.quantizedCb = new double[this.dctCb.length][this.dctCb[0].length];
        this.quantizedCr = new double[this.dctCr.length][this.dctCr[0].length];
        
        for(int row = 0; row < this.dctY.length; row+=8){
            for(int col = 0; col < this.dctY[row].length; col+=8){

                for(int y = 0, rowIndex = row; y < 8; rowIndex++, y++){
                    for(int x = 0, colIndex = col; x < 8; colIndex++, x++){
                        double value = this.dctY[rowIndex][colIndex];
                        
                        double quantizedValue =  Math.round(value / (this.tableY[y][x] * Math.pow(2, this.compressionLevel)));
                        this.quantizedY[rowIndex][colIndex] = quantizedValue;
                    }
                }

            }
        }

        for(int row = 0; row < this.dctCb.length; row+=8){
            for(int col = 0; col < this.dctCb[row].length; col+=8){

                for(int y = 0, indexRow = row; y < 8; indexRow++, y++){
                    for(int x = 0, indexCol = col; x < 8; indexCol++, x++){
                        double cbVal = this.dctCb[indexRow][indexCol];
                        double crVal = this.dctCr[indexRow][indexCol];

                        double newcb = Math.round(cbVal / (tableCbCr[y][x] * Math.pow(2, this.compressionLevel)));
						double newcr = Math.round(crVal / (tableCbCr[y][x] * Math.pow(2, this.compressionLevel)));
                        this.quantizedCb[indexRow][indexCol] = newcb;
                        this.quantizedCr[indexRow][indexCol] = newcr;

                    }
                }

            }
        }

    }

    /* ENCODING STEPS */

    /* DECODING STEPS */

    public void dequantization(){

        for(int row = 0; row < this.quantizedY.length; row+=8){
            for(int col = 0; col < this.quantizedY[row].length; col+=8){

                for(int y = 0, indexRow = row; y < 8; indexRow++, y++){
                    for(int x = 0, indexCol = col; x < 8; indexCol++, x++){
                        double yVal = this.quantizedY[indexRow][indexCol];

                        double newYVal = yVal * (this.tableY[y][x] * Math.pow(2, this.compressionLevel));
                        this.dctY[indexRow][indexCol] = newYVal;
                    }
                }

            }
        }

        for(int row = 0; row < this.quantizedCb.length; row+=8){
            for(int col = 0; col < this.quantizedCb[row].length; col+=8){


                for(int y = 0, indexRow = row; y < 8; indexRow++, y++){
                    for(int x = 0, indexCol = col; x < 8; indexCol++, x++){
                        double cbVal = this.quantizedCb[indexRow][indexCol];
                        double crVal = this.quantizedCr[indexRow][indexCol];

                        double newCb = cbVal * (this.tableCbCr[y][x] * Math.pow(2, this.compressionLevel));
                        double newCr = crVal * (this.tableCbCr[y][x] * Math.pow(2, this.compressionLevel));

                        this.dctCb[indexRow][indexCol] = newCb;
                        this.dctCr[indexRow][indexCol] = newCr;
                    }
                }


            }
        }

    }

    public void inverseDiscreteCosineTransform(){
        for(int row = 0; row < this.dctY.length; row+=8){
            for(int col = 0; col < this.dctY[row].length; col+=8){

                for(int i = 0, yRow = row; i < 8; yRow++, i++){
                    for(int j = 0, yCol = col; j < 8; yCol++, j++){

                        double sum = 0.0;

                        for(int u = 0, dctRow = row; u < 8; dctRow++, u++){
                            for(int v = 0, dctCol = col; v < 8; dctCol++, v++){
                                double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                                double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;

                                double calc1 = Math.cos((2 * i + 1) * u * Math.PI / 16);
                                double calc2 = Math.cos((2 * j + 1) * v * Math.PI / 16);
                                double dctVal = this.dctY[dctRow][dctCol];
                                sum += (cu * cv * calc1 * calc2 * dctVal);
                            }
                        }

                        sum /= 4;
                        this.y[yRow][yCol] = sum;
                    }
                }
            }
        }

        for(int row = 0; row < this.sampledCb.length; row+=8){
            for(int col = 0; col < this.sampledCb[row].length; col+=8){

                for(int i = 0, yRow = row; i < 8; yRow++, i++){
                    for(int j = 0, yCol = col; j < 8; yCol++, j++){

                        // double sum = 0.0;
                        double cbSum = 0.0;
                        double crSum = 0.0;

                        for(int u = 0, dctRow = row; u < 8; dctRow++, u++){
                            for(int v = 0, dctCol = col; v < 8; dctCol++, v++){
                                double cu = (u == 0) ? (1.0 / Math.sqrt(2)) : 1.0;
                                double cv = (v == 0) ? (1.0 / Math.sqrt(2)) : 1.0;

                                double calc1 = Math.cos(((2 * i + 1) * u * Math.PI )/ 16);
                                double calc2 = Math.cos(((2 * j + 1) * v * Math.PI) / 16);

                                // double dctVal = this.dctY[dctRow][dctCol];
                                double dctCb = this.dctCb[dctRow][dctCol];
                                double dctCr = this.dctCr[dctRow][dctCol];
                                // sum += (cu * cv * calc1 * calc2 * dctVal);
                                cbSum += (cu * cv * dctCb  * calc1 * calc2);
                                crSum += (cu * cv * dctCr  * calc1 * calc2);
                            }
                        }

                        // sum /= 4;
                        cbSum /= 4;
                        crSum /= 4;
                        // this.y[yRow][yCol] = sum;
                        this.sampledCb[yRow][yCol] = cbSum;
                        this.sampledCr[yRow][yCol] = crSum;
                    }
                }
            }
        }

        
    }

    public void superSampling(){

        for(int row = 0, r = 0; row < this.cb.length; r++, row+=2){
            for(int col = 0, c = 0; col < this.cb[row].length; c++, col+=2){

                double avgcb = this.sampledCb[r][c];
                double avgcr = this.sampledCr[r][c];

                for(int y = row; y < (row + 2); y++){
                    for(int x = col; x < (col + 2); x++){
                        this.cb[row][col] = avgcb;
                        this.cr[row][col] = avgcr;
                        // this.cb[y][x] = avgcb;
                        // this.cr[y][x] = avgcr;
                    }
                }

            }
        }

    }

    public void inverseColorSpaceTransformation(){
        for(int row = 0; row < this.resized.getH(); row++){
            for(int col = 0; col < this.resized.getW(); col++){
                double y = this.y[row][col] + 128;
                double cb = this.cb[row][col] + 0.5;
                double cr = this.cr[row][col] + 0.5;

                int[] rgb = new int[3];
                rgb[0] = (int) Math.round((1.0 * y) + (0 * cb) + (1.402 * cr));
                rgb[1] = (int) Math.round((1.0 * y) + (- 0.3441 * cb) + (- 0.7141 * cr));
                rgb[2] = (int) Math.round((1.0 * y) + (1.772 * cb) + (0 * cr));

                for(int i = 0; i < 3; i++){
					if(rgb[i] < 0){
						rgb[i] = 0;
					}else if (rgb[i] > 255){
						rgb[i] = 255;
					}
                }
                this.resized.setPixel(col, row, rgb);

            }
        }
    }

    public void revertSize(){
        for(int row = 0; row < this.copy.getH(); row++){
            for(int col = 0; col < this.copy.getW(); col++){
                int[] rgb = new int[3];
                this.resized.getPixel(col, row, rgb);
                this.copy.setPixel(col, row, rgb);
            }
        }
        System.out.println(this.name.substring(0, this.name.indexOf(".")));
        String fileName = this.name.substring(0, this.name.indexOf(".")) + "_" + this.compressionLevel + ".ppm";
        this.copy.write2PPM(fileName);
    }

    /* DECODING STEPS */

    public void printY(){
        System.out.println("Height: " + this.y.length);
        System.out.println("Width: " + this.y[0].length);
        print2DArray(this.y);
    }

    public void printDCTY(){
        System.out.println("Height: " + this.dctY.length);
        System.out.println("Width: " + this.dctY[0].length);
        print2DArray(this.dctY);
    }

    public void printQuantY(){
        this.print2DArray(this.quantizedY);
    }

    public MImage getResized(){
        return this.resized;
    }

    public void printResizedPixels(){
        print(this.resized);
    }

    public void printOriginalPixels(){
        print(this.copy);
    }

    public void printTransformed(){
        for(int row = 0; row < this.transformed.length; row++){
            for(int col = 0; col < this.transformed[row].length; col++){
                System.out.print("Values at: row: " + row + " col: " + col + ":\t");
                for(int vals = 0; vals < this.transformed[row][col].length; vals++){
                    System.out.print(this.transformed[row][col][vals] + "\t");
                }
                System.out.println("\n");
            }
        }
    }

    public void printSampledCb(){
        System.out.println("Height: " + this.sampledCb.length);
        System.out.println("Width: " + this.sampledCb[0].length);
        print2DArray(this.sampledCb);
    }

    public void printCb(){
        System.out.println("Height: " + this.cb.length);
        System.out.println("Width: " + this.cb[0].length);
        print2DArray(this.cb);
    }

    private void print2DArray(double[][] array){
        for(int row = 0; row < array.length; row++){
            for(int col = 0; col < array[row].length; col++){
                System.out.println("value at row: " + row + " col: " + col + ": ---> " + array[row][col]);
            }
            System.out.println("\n\n");
        }
    }

    private void print(MImage image){
        for(int row = 0; row < image.getH(); row++){
            for(int col = 0; col < image.getW(); col++){
                image.printPixel(col, row);
            }
            System.out.println("\n");
        }
    }

}