import java.util.Scanner;


public class Menu{

    private final String MENU = "Main Menu--------------------------\n" +
                                "1. Block-Based Motion Compensation\n" +
                                "2. Removing Moving Objects\n" + 
                                "3. Quit\n\n" +
                                "Please enter the task number [1-3]: ";
    private final Scanner scanner = new Scanner(System.in);

    public void run(){

        int task = 0; 
        do{
            
            printMenu();
            task = scanner.nextInt();
            handleSelectedTask(task);

        }while(task != 3);

        System.out.println("Exiting. . .");
        scanner.close();
    }

    private void printMenu(){
        System.out.print(MENU);
    }

    private void handleSelectedTask(int task){
        switch(task){
            case 1:
                int[] args = getArgs();
                BlockMotionCompensation bmc = new BlockMotionCompensation(args);
                bmc.motionCompensation();
                break; 
            case 2:

                break;
            case 3:
                
                break;
            default:
                System.out.println("Task number does not exist. Try again.\n");
        }
    }

    private int[] getArgs(){
        int[] args = new int[4];

        System.out.print("Enter n: ");
        args[0] = scanner.nextInt();
        
        System.out.print("Enter p: ");
        args[1] = scanner.nextInt();

        System.out.print("Enter number for reference image: ");
        args[2] = scanner.nextInt();

        System.out.print("Enter number for target image: ");
        args[3] = scanner.nextInt();

        return args;
    }

}