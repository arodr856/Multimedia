public class GrayScaleConverter{

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
        grayImg.write2PPM(img.getName().substring(0, img.getName().indexOf(".")) + "-gray.ppm");
    }

}