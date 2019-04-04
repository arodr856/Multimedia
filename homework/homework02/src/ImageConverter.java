import java.util.Scanner;


public class ImageConverter{

    private MImage image;

    public ImageConverter(String fileName){
        this.image = new MImage(fileName);
    } /* ImageConversion(String fileName) */

    public void run(){
        Scanner scanner = new Scanner(System.in);
        int menuOption = 3;
        do{
            printMenu();
            menuOption = scanner.nextInt();

            switch(menuOption){
                case 1:
                    GrayScaleConverter.convertToGrayScale(this.image);
                    break;
                case 2:
                    ColorQuantization.generate8BitUCQ(this.image);
                    break;
                case 3:
                    System.out.println("Goodbye. . .");
                    break;
                default:
                    System.out.println("Invalid option entered. . .");
            }
        }while(menuOption != 3);
        scanner.close();
    } /* run() */

    public static void printMenu(){

        String  menu = "Main Menu----------------------------------\n" +
                        "1. Conversion to Gray-scale image (24bits -> 8bits)\n" +
                        "2. Conversion to 8bit indexed color image using uniform color quantization (24bits -> 8bits)\n" +
                        "3. Quit\n\n" +
                        "Please enter the task number [1 - 3]: ";
        System.out.print(menu);
    
    } /* printMenu */

} /* ImageConversion */