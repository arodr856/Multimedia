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
                        
                    break;
                case 2:
                    DCTTransformation dctTrans = new DCTTransformation(this.image);

                    /* encoding */
                    dctTrans.resize();
                    dctTrans.colorSpaceTransformation();
                    dctTrans.subSampling();
                    dctTrans.discreteCosineTransform();
                    dctTrans.inverseDiscreteCosineTransform();
                    dctTrans.quantization();
                    dctTrans.printCb();
                    // dctTrans.printSampledCb();
                    /* encoding */

                    /* decoding */
                    dctTrans.dequantization();
                    dctTrans.inverseDiscreteCosineTransform();
                    dctTrans.superSampling();
                    dctTrans.inverseColorSpaceTransformation();
                    dctTrans.revertSize();
                    
                    /* decoding */
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
                        "1. VQ (Vector Quantization)\n" +
                        "2. DCT-based Coding\n" +
                        "3. Quit\n\n" +
                        "Please enter the task number [1 - 3]: ";
        System.out.print(menu);
    
    } /* printMenu */

} /* ImageConversion */