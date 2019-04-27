import java.util.ArrayList;
import java.util.Random;

public class VectorQuantization {

    private MImage originalImage;
    private String name;

    private ArrayList<Integer>[][] inputVectors;
    private int[][] centroids;
    private int[][] centroidIndexes;

    public VectorQuantization(){
        
    }

    public VectorQuantization(MImage originalImage){
        this.originalImage = deepCopy(originalImage);
        this.name = originalImage.getName();
        this.centroids = new int[256][12];
    }

    public void generateInputVectors(){
        this.inputVectors = this.generateInputVectors(this.originalImage);
    }

    private ArrayList<Integer>[][] generateInputVectors(MImage image){

        ArrayList<Integer>[][] inputVectors = new ArrayList[image.getH() / 2][image.getW() / 2];

        for(int row = 0, inputRow = 0; row < image.getH(); row+=2, inputRow++){
            for(int col = 0, inputCol = 0; col < image.getW(); col+= 2, inputCol++){

                ArrayList<Integer> vector = new ArrayList<>();
                for(int imageRow = row; imageRow < (row + 2); imageRow++){
                    for(int imageCol = col; imageCol < (col + 2); imageCol++){
                        int[] rgb = new int[3];
                        image.getPixel(imageCol, imageRow, rgb);
                        vector.add(rgb[0]);
                        vector.add(rgb[1]);
                        vector.add(rgb[2]);
                    }
                }

                inputVectors[inputRow][inputCol] = vector;
            }
        }

        return inputVectors;
    }

    public void assignRandomValuesToCentroids(){
        boolean[][] used = new boolean[this.originalImage.getH() / 2][this.originalImage.getW() / 2];
        Random rand = new Random();
        for(int row = 0; row < this.centroids.length; row++){
            

            int randRow = rand.nextInt(this.inputVectors.length);
            int randCol = rand.nextInt(this.inputVectors[randRow].length);

            while(used[randRow][randCol] == true){
                randRow = rand.nextInt(this.inputVectors.length);
                randCol = rand.nextInt(this.inputVectors[randRow].length);
            }

            // System.out.println("Row: " + randRow + " -----> Col: " + randCol);

            used[randRow][randCol] = true;
            ArrayList<Integer> vector = this.inputVectors[randRow][randCol];
            for(int i = 0; i < vector.size(); i++){
                this.centroids[row][i] = vector.get(i);
            }
        }
    }

    public void run(){

        this.inputVectors = generateInputVectors(this.originalImage);
        assignRandomValuesToCentroids();
        kMeansClustering();
        writeImageIndex();
        decode();

    }
    
    public void decode(){
        MImage output = new MImage(this.originalImage.getW(), this.originalImage.getH());

        for(int row = 0, indexRow = 0; row < this.originalImage.getH(); row+=2, indexRow++){
            for(int col = 0, indexCol = 0; col < this.originalImage.getW(); col+=2, indexCol++){

                int index = this.centroidIndexes[indexRow][indexCol];
                int[] vector = this.centroids[index];

                for(int outRow = row; outRow < (row + 2); outRow++){
                    for(int outCol = col; outCol < (col + 2); outCol++){

                        for(int i = 0; i < vector.length; i+=3){
                            int[] rgb = {vector[i], vector[i + 1], vector[i + 2]};
                            output.setPixel(outCol, outRow, rgb);
                        }


                    }
                }

            }
        }
        String fileName = this.name.substring(0, this.name.indexOf(".")) + "_" + "VQ.ppm";
        output.write2PPM(fileName);

    }

    public void kMeansClustering(){
        this.centroidIndexes = new int[this.inputVectors.length][this.inputVectors[0].length];

        int count = 0;
        int max =  100;


        while(count < max){
            
            for(int row = 0; row < this.inputVectors.length; row++){
                for(int col = 0; col < this.inputVectors[row].length; col++){
                    
                    ArrayList<Integer> vector = this.inputVectors[row][col];

                    this.centroidIndexes[row][col] = findNearestCentroid(vector);
                }

            }
            this.centroids = updateCentroids();
            count++;
        }


    }

    public int[][] updateCentroids(){

        int[][] centroids = new int[256][12];
        int[] count = new int[256];

        for(int row = 0; row < this.centroidIndexes.length; row++){
            for(int col = 0; col < this.centroidIndexes[row].length; col++){

                int index = this.centroidIndexes[row][col];
                for(int i = 0; i < 12; i++){
                    centroids[index][i] += this.inputVectors[row][col].get(i);
                }
                count[index]++;
            }   
        }

        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 12; j++){
                if(count[i] != 0){
                    centroids[i][j] /= count[i];
                }
            }
        }

        return centroids;
    }

    public int findNearestCentroid(ArrayList<Integer> list){

        double currentMin = euclidianDistance(list, this.centroids[0]);
        int minIndex = 0;

        for(int i = 1; i < this.centroids.length; i++){
            double possibleMin = euclidianDistance(list, this.centroids[i]);
            if(possibleMin < currentMin){
                currentMin = possibleMin;
                minIndex = i;
            }

        }

        return minIndex;
    }

    public double euclidianDistance(ArrayList<Integer> input, int[] centroid){
        double sum = 0.0;
        for (int i = 0; i < 12; i++){
            double result = input.get(i) - centroid[i];
            // sum += result * result;
            sum += (Math.pow(result, 2));
        }
        return Math.sqrt(sum);
    }

    public void writeImageIndex(){
        MImage indexImage = new MImage(this.centroidIndexes[0].length, this.centroidIndexes.length);
        for(int row = 0; row < this.centroidIndexes.length; row++){
            for(int col = 0; col < this.centroidIndexes[row].length; col++){
                int index = this.centroidIndexes[row][col];
                int[] rgb = {index, index, index};
                indexImage.setPixel(col, row, rgb);
            }
        }
        String fileName = this.name.substring(0, this.name.indexOf(".")) + "_" + "Quantized.ppm";
        indexImage.write2PPM(fileName);
    }

    public void printCentroids(){
        for(int row = 0; row < this.centroids.length; row++){
            // System.out.println(this.centroids[row].length);
            System.out.print(row + ": ");
            for(int col = 0; col < this.centroids[row].length; col++){
                System.out.print(this.centroids[row][col] + " ");
            }
            System.out.println();
        }
    }

    public void printIndexes(){
        for(int row = 0; row < this.centroidIndexes.length; row++){
            for(int col = 0; col < this.centroidIndexes[row].length; col++){
                System.out.print(this.centroidIndexes[row][col] + " ");
            }
            System.out.println();
        }
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

    public void printInputVectors(){
        for(int row = 0; row < this.inputVectors.length; row++){
            for(int col = 0; col < this.inputVectors[row].length; col++){
                ArrayList<Integer> list = inputVectors[row][col];
                for(int input = 0; input < list.size(); input++){
                    System.out.print(list.get(input) + " ");
                }
                System.out.println();
            }   
        }
        System.out.println("original image width: " + this.originalImage.getW() + " height: " + this.originalImage.getH());
        System.out.println("input vectors image width: " + this.inputVectors[0].length + " height: " + this.inputVectors.length);
        int[] rgb = new int[3];
        this.originalImage.getPixel(349, 279, rgb);
        for(int x : rgb){
            System.out.print(x + " ");
        }
        System.out.println();
    }

}