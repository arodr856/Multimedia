import java.util.Scanner;


public class BlockMotionCompensation{

    final String MENU = "Main Menu--------------------------\n" +
                                "1. Block-Based Motion Compensation\n" +
                                "2. Removing Moving Objects\n" + 
                                "3. Quit\n\n" +
                                "Please enter the task number [1-3]: ";

    public BlockMotionCompensation(){

    }

    public void run(){
        Scanner scanner = new Scanner(System.in);
        int task = 0; 
        do{
            
            printMenu();
            task = scanner.nextInt();
            handleSelectedTask(task);

        }while(task != 3);

        System.out.println("Exiting. . .");
        scanner.close();
    }

    public void printMenu(){
        System.out.print(MENU);
    }

    public void handleSelectedTask(int task){
        switch(task){
            case 1:
            
                break; 
            case 2:

                break;
            case 3:
                
                break;
            default:
                System.out.println("Task number does not exist. Try again.\n");
        }
    }

}