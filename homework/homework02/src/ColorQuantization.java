import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;;

public class ColorQuantization{

    public static void generate8BitUCQ(MImage image){

        HashMap<Integer, int[]> lookUpTable = new HashMap<>();
        generateLUT(lookUpTable);
        printLUT(lookUpTable);
        MImage indexImg = generateIndexFile(image, lookUpTable);
        MImage qt8 = new MImage(indexImg.getW(), indexImg.getH());
        generate8BitImg(image.getName(), indexImg, qt8, lookUpTable);

    }

    private static void generateLUT(HashMap<Integer, int[]> lut){
        for(int i = 0; i <= 255; i++){
            int[] indexVals = new int[3];
            String binaryIndex = Integer.toBinaryString(i);
            binaryIndex = addZeros(binaryIndex);
            String red = binaryIndex.substring(0, 3);
            String green = binaryIndex.substring(3, 6);
            String blue = binaryIndex.substring(6);

            int rVal = (32 * binToDec(red)) + 16;
            int gVal = (32 * binToDec(green)) + 16;
            int bVal = (64 * binToDec(blue)) + 32;

            indexVals[0] = rVal;
            indexVals[1] = gVal;
            indexVals[2] = bVal;
            
            lut.put(i, indexVals);

        }
    } /* generateLUT */
    
    private static String addZeros(String binaryStr){
        while(binaryStr.length() < 8){
            binaryStr = "0" + binaryStr;
        }
        return binaryStr;
    } /* addZeros */

    private static int binToDec(String binary){
        int sum = 0;
        int exponent = 0;
        for(int i = binary.length() - 1; i >= 0; i--){
            char c = binary.charAt(i);
            int bit = Integer.parseInt(String.valueOf(c));

            sum += bit * (int)Math.pow(2, exponent++);
        }
        return sum;
    } /* binToDec */

    private static void printLUT(HashMap<Integer, int[]> lut){
        Set<Integer> keys = lut.keySet();
        Iterator<Integer> keysIterator = keys.iterator();
        while(keysIterator.hasNext()){
            Integer lutIndex = keysIterator.next();
            int[] lutVals = lut.get(lutIndex);
            System.out.print(lutIndex + ": ");
            for(int i = 0; i < lutVals.length; i++){
                System.out.print(lutVals[i] + " ");
            }
            System.out.println();
        }
    } /* printLUT */

    private static MImage generateIndexFile(MImage mImg, HashMap<Integer, int[]> lut){
        int height = mImg.getH();
        int width = mImg.getW();

        MImage indexImg = new MImage(width, height);

        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){

                int[] rgb = new int[3];
                mImg.getPixel(col, row, rgb);
                int index = findIndex(rgb);

                int[] indexRGB = new int[3];
                indexImg.getPixel(col, row, indexRGB);

                indexRGB[0] = index;
                indexRGB[1] = index;
                indexRGB[2] = index;
                indexImg.setPixel(col, row, indexRGB);
            }
        }

        indexImg.write2PPM(mImg.getName().substring(0, mImg.getName().indexOf(".")) + "-index.ppm");
        return indexImg;
    }   /* generateIndexFile */

    private static int findIndex(int[] rgb){

        int rVal =  (int) Math.floor((rgb[0] / 32));
        String rBinary = verifyBitCount(Integer.toBinaryString(rVal), 3);

        int gVal = (int) Math.floor((rgb[1] / 32));
        String gBinary = verifyBitCount(Integer.toBinaryString(gVal), 3);

        int bVal = (int) Math.floor((rgb[2] / 64));
        String bBinary = verifyBitCount(Integer.toBinaryString(bVal), 2);

        String indexBinary = rBinary + gBinary + bBinary;
        return binToDec(indexBinary);
    } /* findIndex */

    private static String verifyBitCount(String binary, int bitCount){
        if(binary.length() < bitCount){
            while(binary.length() < bitCount){
                binary  = "0" + binary;
            }
            return binary;
        }
        return binary;
    } /* verifyBitCount */

    private static void generate8BitImg(String fileName, MImage indexImg, MImage qt8, HashMap<Integer, int[]> lut){
        for(int row = 0; row < indexImg.getH(); row++){
            for(int col = 0; col < indexImg.getW(); col++){
                int[] indexRGB = new int[3];
                indexImg.getPixel(col, row, indexRGB);
                int[] rgbValues = lut.get(indexRGB[0]);
                qt8.setPixel(col, row, rgbValues);
            }
        }
        String name = fileName.substring(0, fileName.indexOf("."));
        qt8.write2PPM(name + "-QT8.ppm");
    } /* generateQT8Img */

}