/**
 * Homework01:
 * ---------------------
 * Name: Alex Rodriguez
 * Class: CS-4551
 */

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Scanner;

public class CS4551_Rodriguez{
    public static void main(String[] args){

        HashMap<Integer, int[]> lookUpTable = new HashMap<>();
        String fileName = args[0];
        MImage img = new MImage(fileName);
        img.toString();
        Scanner scanner = new Scanner(System.in);
        int menuOption = 3;
        do{
            printMenu();
            menuOption = scanner.nextInt();

            switch(menuOption){
                case 1:
                    convertToGrayScale(img);
                    break;
                case 2:
                    generateLUT(lookUpTable);
                    printLUT(lookUpTable);
                    MImage indexImg = generateIndexFile(img, lookUpTable);
                    MImage qt8 = new MImage(indexImg.getW(), indexImg.getH());
                    generateQT8Img(img.getName(), indexImg, qt8, lookUpTable);

                    break;
                case 3:
                    System.out.println("Goodbye. . .");
                    break;
                default:
                    System.out.println("Invalid option entered. . .");
            }
        }while(menuOption != 3);
         
    } /* main */

    public static void printMenu(){

        String  menu = "Main Menu----------------------------------\n" +
                        "1. Conversion to Gray-scale image (24bits -> 8bits)\n" +
                        "2. Conversion to 8bit indexed color image using uniform color quantization (24bits -> 8bits)\n" +
                        "3. Quit\n\n" +
                        "Please enter the task number [1 - 3]: ";
        System.out.print(menu);
    
    } /* printMenu */

    public static void convertToGrayScale(MImage img){
        int width = img.getW();
        int height = img.getH();

        MImage grayImg = new MImage(width, height);

        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                int[] rgbValues = new int[3];
                img.getPixel(col, row, rgbValues);
                int r = (int)(rgbValues[0] * 0.299);
                int g = (int)(rgbValues[1] * 0.587);
                int b = (int)(rgbValues[2] * 0.114);
                int gray = r + g + b;
                rgbValues[0] = gray;
                rgbValues[1] = gray;
                rgbValues[2] = gray;
                grayImg.setPixel(col, row, rgbValues);
            }
        }
        grayImg.write2PPM(img.getName() + "_gray.ppm");
    }

    public static void generateLUT(HashMap<Integer, int[]> lut){
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

    public static String addZeros(String binaryStr){
        while(binaryStr.length() < 8){
            binaryStr = "0" + binaryStr;
        }
        return binaryStr;
    } /* addZeros */

    public static int binToDec(String binary){
        int sum = 0;
        int exponent = 0;
        for(int i = binary.length() - 1; i >= 0; i--){
            char c = binary.charAt(i);
            int bit = Integer.parseInt(String.valueOf(c));

            sum += bit * (int)Math.pow(2, exponent++);
        }
        return sum;
    } /* binToDec */

    public static void printLUT(HashMap<Integer, int[]> lut){
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

    public static MImage generateIndexFile(MImage mImg, HashMap<Integer, int[]> lut){
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

        indexImg.write2PPM("ducky-index.ppm");
        return indexImg;
    }   /* generateIndexFile */

    public static int findIndex(int[] rgb){

        int rVal =  (int) Math.floor((rgb[0] / 32));
        String rBinary = verifyBitCount(Integer.toBinaryString(rVal), 3);

        int gVal = (int) Math.floor((rgb[1] / 32));
        String gBinary = verifyBitCount(Integer.toBinaryString(gVal), 3);

        int bVal = (int) Math.floor((rgb[2] / 64));
        String bBinary = verifyBitCount(Integer.toBinaryString(bVal), 2);

        String indexBinary = rBinary + gBinary + bBinary;
        return binToDec(indexBinary);
    } /* findIndex */

    public static String verifyBitCount(String binary, int bitCount){
        if(binary.length() < bitCount){
            while(binary.length() < bitCount){
                binary  = "0" + binary;
            }
            return binary;
        }
        return binary;
    } /* verifyBitCount */

    public static void generateQT8Img(String fileName, MImage indexImg, MImage qt8, HashMap<Integer, int[]> lut){
        for(int row = 0; row < indexImg.getH(); row++){
            for(int col = 0; col < indexImg.getW(); col++){
                int[] indexRGB = new int[3];
                indexImg.getPixel(col, row, indexRGB);
                int[] rgbValues = lut.get(indexRGB[0]);
                for(int i = 0; i < 3; i++){
                    qt8.setPixel(col, row, rgbValues);
                }

            }
        }
        String name = fileName.substring(0, fileName.indexOf("."));
        qt8.write2PPM(name +"-QT8.ppm");
    }

} /* CS4551_Rodriguez */
