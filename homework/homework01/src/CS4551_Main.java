/*******************************************************
 * CS4551 Multimedia Software Systems @ Author: Elaine Kang
 * 
 * 
 * Template Code - demonstrate how to use MImage class
 *******************************************************/

public class CS4551_Main {
	public static void main(String[] args) {
		// the program expects one commandline argument
		// if there is no commandline argument, exit the program
		if (args.length != 1) {
			usage();
			System.exit(1);
		}

		System.out.println("--Welcome to Multimedia Software System--");

		// Create an Image object with the input PPM file name.
		MImage img = new MImage(args[0]);
		System.out.println(img);

		// Save it into another PPM file.
		img.write2PPM("out.ppm");

		// demonstrate how to read and modify pixels of the MImage instance
		int x = 10, y = 10;
		img.printPixel(x, y);
		int[] rgb = new int[3];
		img.getPixel(x, y, rgb);
		System.out.println("RGB Pixel value at (" + x + "," + y + "): (" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")");
		rgb[0] = 0;
		rgb[1] = 0;
		rgb[2] = 0;
		img.setPixel(x, y, rgb);
		img.getPixel(x, y, rgb);
		System.out.println("RGB Pixel value at (" + x + "," + y + "): (" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")");

		// Save it into another PPM file.
		img.write2PPM("out2.ppm");

		System.out.println("--Good Bye--");
	}

	public static void usage() {
		System.out.println("\nUsage: java CS4551_Main [input_ppm_file]\n");
	}
}