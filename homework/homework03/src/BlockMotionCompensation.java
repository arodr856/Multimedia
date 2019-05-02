import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class BlockMotionCompensation{

    private int n;
    private int p;
    private int ref;
    private int target;

    private MImage refImg;
    private MImage targetImg;

    private int macroBlockRows;
    private int macroBlockCols;

    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    

    private MacroBlock[][] macroBlocks;
    private MacroBlock[][] replacedBlocks;
    private MacroBlock[][] replacedWithClosest;

    // LOCATION OF VIDEO FRAMES
    // FRAMES_DIRECTORY directory should be in the same directory as the java files
    // example: /root folder -- \FRAMES_DIRECTORY
    //                           \ *.java files 
    private final String FRAMES_DIRECTORY = "IDB"; 

    public BlockMotionCompensation(int n, int p, int ref, int target) {
        
        this.n = n;
        this.p = p;
        this.ref = ref;
        this.target = target;
        
        this.refImg = new MImage(this.FRAMES_DIRECTORY + "/Walk_" + padZeros(this.ref) + ".ppm");
        this.targetImg = new MImage(this.FRAMES_DIRECTORY + "/Walk_" + padZeros(this.target) + ".ppm");

        this.macroBlockRows = this.targetImg.getH() / this.n;
        this.macroBlockCols = this.targetImg.getW() / this.n;

        this.macroBlocks = new MacroBlock[this.macroBlockRows][this.macroBlockCols];
        this.replacedBlocks = new MacroBlock[this.macroBlockRows][this.macroBlockCols];
        this.replacedWithClosest = new MacroBlock[this.macroBlockRows][this.macroBlockCols];
    }

    public BlockMotionCompensation(int[] args){

        this(args[0], args[1], args[2], args[3]);

    }

    private String padZeros(int num) {
        if(num < 10){
            return "00" + num;
        }else if(num < 100){
            return "0" + num;
        }else{
            return "" + num;
        }
    }

    public void motionCompensation(){
        createMacroBlocks();
        // finding best match block for all macro blocks
        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){
                findBestMatchBlock(this.macroBlocks[row][col], this.refImg, this.p);
            }
        }
        computeMotionVectors();
        computeErrorBlocks();
        saveErrorImage();
        writeToTxt();
    }

    private void createMacroBlocks(){
       System.out.println("Creating macro blocks size " + this.macroBlockRows + " x " + this.macroBlockCols + " . . .");
       
       for(int imgRow = 0, blocksRow = 0; imgRow < this.targetImg.getH(); imgRow += this.n, blocksRow++){
            for(int imgCol = 0, blocksCol = 0; imgCol < this.targetImg.getW(); imgCol += this.n, blocksCol++){

                MacroBlock block = new MacroBlock(imgRow, imgCol, this.n, this.targetImg);

                for(int i = imgRow, y = 0; i < (imgRow + this.n); i++, y++){
                    for(int j = imgCol, x = 0; j < (imgCol + this.n); j++, x++){
                        int[] rgb = new int[3];
                        this.targetImg.getPixel(j, i, rgb);
                        block.setPixel(y, x, rgb);
                        block.setGrayValue(y, x, computeGrayValue(rgb));
                    }
                }

                this.macroBlocks[blocksRow][blocksCol] = block;

            }
       }
    }

    private int computeGrayValue(int[] rgb){
        int value = (int) Math.round((0.299 * rgb[0]) + (0.587 * rgb[1]) + (0.114 * rgb[2]));
        // System.out.println(value);
        return value;
    }

    // Create macro block from reference image
    private MacroBlock createRefImgMacroBlock(int row, int col, int n, MImage refImg){
        MacroBlock block = new MacroBlock(row, col, this.n, this.refImg);

        for(int imgRow = row, blockRow = 0; imgRow < (row + n); imgRow++, blockRow++){
            for(int imgCol = col, blockCol = 0; imgCol < (col + n); imgCol++, blockCol++){
                int[] rgb = new int[3];
                refImg.getPixel(imgCol, imgRow, rgb);
                block.setPixel(blockRow, blockCol, rgb);
                block.setGrayValue(blockRow, blockCol, computeGrayValue(rgb));
            }
        }

        return block;
    }

    private void findBestMatchBlock(MacroBlock targetBlock, MImage refImg, int p){
        double minMSD = Double.MAX_VALUE;
        MacroBlock bestMatch = new MacroBlock();

        for(int row = (-1 * p); row <= p; row++){
            for(int col = (-1 * p); col <= p; col++){
                int newRow = targetBlock.getRow() + row;
                int newCol = targetBlock.getCol() + col;
                MacroBlock refBlock = createRefImgMacroBlock(newRow, newCol, n, refImg);
                
                double tempMSD = computeMSD(targetBlock, refBlock);

                if(tempMSD < minMSD){
                    minMSD = tempMSD;
                    bestMatch = refBlock;
                }
            }
        }
        targetBlock.setBestMatchBlock(bestMatch);
        // targetBlock.setMotionVectors(mv);
    }

    private double computeMSD(MacroBlock target, MacroBlock ref){

        double msd = 0.0;
        for(int row = 0; row < this.n; row++){
            for(int col = 0; col < this.n; col++){
                int tGray = target.getGrayValue(row, col);
                int rGray = ref.getGrayValue(row, col);
                msd += Math.pow(tGray - rGray, 2);
            }
        }

        msd /= (this.n * this.n);
        return msd;
    }

    private void computeMotionVectors(){
        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){

                int[] mv = new int[2];
                int targetRow = this.macroBlocks[row][col].getRow();
                int bestRow = this.macroBlocks[row][col].getBestMatchBlock().getRow();

                int targetCol = this.macroBlocks[row][col].getCol();
                int bestCol = this.macroBlocks[row][col].getBestMatchBlock().getCol();

                mv[0] = targetRow - bestRow;
                mv[1] = targetCol - bestCol;

                this.macroBlocks[row][col].setMotionVectors(mv);

            }
        }
    }

    private void computeErrorBlocks(){

        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){

                MacroBlock block = this.macroBlocks[row][col];

                for(int blockRow = 0; blockRow < this.n; blockRow++){
                    for(int blockCol = 0; blockCol < this.n; blockCol++){

                        int targetVal = block.getGrayValue(blockRow, blockCol);
                        int bestVal = block.getBestMatchBlock().getGrayValue(blockRow, blockCol);

                        int result = Math.abs(targetVal - bestVal);
                        if(result > this.max){
                            this.max = result;
                        }
                        if(result < this.min){
                            this.min = result;
                        }
                        // System.out.println(result);
                        block.setErrorBlock(blockRow, blockCol, result);
                    }
                }

            }
        }

    }

    private void saveErrorImage(){

        MImage errorImage = new MImage(this.targetImg.getW(), this.targetImg.getH());
        System.out.println("MIN: " + this.min);
        System.out.println("MAX: " + this.max);

        for(int imgRow = 0, blockRow = 0; imgRow < this.targetImg.getH(); imgRow += this.n, blockRow++){
            for(int imgCol = 0, blockCol = 0; imgCol < this.targetImg.getW(); imgCol += this.n, blockCol++){
                MacroBlock block = this.macroBlocks[blockRow][blockCol];

                for(int errRow = imgRow, row = 0; row < this.n; errRow++, row++){
                    for(int errCol = imgCol, col = 0; col < this.n; errCol++, col++){

                        double error = block.getErrorBlock(row, col) * 1.0;

                        int scaledError = (int) (((error - this.min) / (this.max - this.min)) * 255.0);
                        int[] rgb = {scaledError, scaledError, scaledError};
                        errorImage.setPixel(errCol, errRow, rgb);

                    }
                }

            }
        }
        String fileName = this.targetImg.getName().substring(this.targetImg.getName().indexOf("/") + 1);

        errorImage.write2PPM("err_" + fileName);
    }

    private void writeToTxt(){
        String header = String.format("# Name: Alex Rodriguez\n" +
                                        "# Target image name: %s\n" +
                                        "# Reference image name: %s\n" +
                                        "# Number of target macro blocks: %d x %d (image size is %d x %d)\n\n", this.targetImg.getName(), this.refImg.getName(), this.macroBlocks[0].length, this.macroBlocks.length, this.targetImg.getW(), this.targetImg.getH());  
        StringBuilder builder = new StringBuilder(header);
        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){
                MacroBlock block = this.macroBlocks[row][col];
                int[] mv = block.getMotionVectors();
                String result = "[ " + mv[0] + " , " + mv[1] + " ]\t";
                builder.append(result);
            }
            builder.append("\n");
        }

        System.out.println("\n" + builder.toString());
        PrintWriter writer = null;
        try{
            writer = new PrintWriter("mv.txt", "UTF-8");
            writer.println(builder.toString());

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }finally{
            writer.close();
        }

    }

    private void printMotionVectors(){
        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){
                MacroBlock block = this.macroBlocks[row][col];
                int[] mv = block.getMotionVectors();
                String result = "[ " + mv[0] + " , " + mv[1] + " ]\t";
                System.out.print(result);
            }
            System.out.println();
        }
    }

    public void removeMovingObj(){
        removeObj();
    }

    private void removeObj(){
        createMacroBlocks();
        // finding best match block for all macro blocks
        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){
                findBestMatchBlock(this.macroBlocks[row][col], this.refImg, this.p);
            }
        }
        computeMotionVectors();
        replaceDynamicBlocks();
        replaceWithClosestBlock();
        saveRemovedObjImgs();
    }

    private void replaceDynamicBlocks(){

        MImage fifth = new MImage("IDB/Walk_005.ppm");

        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){


                MacroBlock block = this.macroBlocks[row][col];

                if(block.getMotionVectors()[0] != 0 || block.getMotionVectors()[1] != 0){
                    MacroBlock ref = createRefImgMacroBlock(block.getRow(), block.getCol(),this.n, fifth);
                    this.replacedBlocks[row][col] = ref;
                    
                }else{
                    this.replacedBlocks[row][col] = block;
                } 


            }
        }

    }

    private void replaceWithClosestBlock(){

        for(int row = 0; row < this.macroBlocks.length; row++){
            for(int col = 0; col < this.macroBlocks[row].length; col++){

                MacroBlock block = this.macroBlocks[row][col];
                int y = block.getMotionVectors()[0];
                int x = block.getMotionVectors()[1];
                if(y != 0 || x != 0){

                    int minDistance = Integer.MAX_VALUE;

                    for(int i = 0; i < this.macroBlocks.length; i++){
                        for(int j = 0; j < this.macroBlocks[row].length; j++){
                            MacroBlock tempBlock = this.macroBlocks[i][j];
                            if(tempBlock.getMotionVectors()[0] == 0 && tempBlock.getMotionVectors()[1] == 0){
                                int tempDistance = Math.abs(block.getCol() - tempBlock.getCol()) + Math.abs(block.getRow() - tempBlock.getRow());
                                if(tempDistance < minDistance){
                                    minDistance = tempDistance;
                                    this.replacedWithClosest[row][col] = tempBlock;
                                }
                            }
                        }
                    }

                }else{

                    this.replacedWithClosest[row][col] = block;

                }

            }
        }

    }

    private void saveRemovedObjImgs(){
        MImage removedClosest = new MImage(this.targetImg.getW(), this.targetImg.getH());
        MImage removed2 = new MImage(this.targetImg.getW(), this.targetImg.getH());
        
        System.out.println("MIN: " + this.min);
        System.out.println("MAX: " + this.max);

        for(int imgRow = 0, blockRow = 0; imgRow < this.targetImg.getH(); imgRow += this.n, blockRow++){
            for(int imgCol = 0, blockCol = 0; imgCol < this.targetImg.getW(); imgCol += this.n, blockCol++){
                MacroBlock block = this.replacedBlocks[blockRow][blockCol];
                MacroBlock block2 = this.replacedWithClosest[blockRow][blockCol];
                for(int errRow = imgRow, row = 0; row < this.n; errRow++, row++){
                    for(int errCol = imgCol, col = 0; col < this.n; errCol++, col++){

                        int[] rgb = block.getPixel(row, col);
                        int[] rgb2 = block2.getPixel(row, col);

                        removedClosest.setPixel(errCol, errRow, rgb2);
                        removed2.setPixel(errCol, errRow, rgb);

                    }
                }

            }
        }
        String fileName = this.targetImg.getName().substring(this.targetImg.getName().indexOf("/") + 1);
        removedClosest.write2PPM("obj_remove_1_" + fileName);
        removed2.write2PPM("obj_remove_2_" + fileName);
    }

}