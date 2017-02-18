package knapsack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**  
 * @Soumojit
 */

import java.util.ArrayList;

public class Knapsack_BB {
	static File directory = new File("./");
	static String base= directory.getAbsolutePath();
	
	 public static void main(String[] args) {
		 ArrayList<Integer>v=new ArrayList<Integer>();
		 try{
		 long startTime = System.currentTimeMillis();
		 System.out.println(String.valueOf(startTime));
		 Reader r= new Reader();
		 r.Data_Reader();
		 Knapsack_GA knap = new Knapsack_GA(r);
		 knap.Work();
		 Plot graph = new Plot(knap.mean_fitness_of_generation,"Mean Fitness by Generation");
		 graph.Draw();
		 Console_File(r);
		 double estimatedTime= (double)System.currentTimeMillis()-(double)startTime;
		 System.out.println("Time Taken for running Genetic Algorithm: "+String.valueOf(estimatedTime/1000));
		 File_Console();
		 BB bb = new BB(r);
		 bb.Branch_Bound(v);
		 bb.Write_Results();
		 Console_File(r);
		 estimatedTime= (double)System.currentTimeMillis()-(double)startTime-estimatedTime;
		 System.out.println("Time Taken for running Branch & Bound: "+String.valueOf(estimatedTime/1000));
		 File_Console();
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	 }	
	 
	 public static void Console_File(Reader r){
		 try {
             File file_name = new File(Knapsack_BB.base+"/output/output_"+r.nitems+"_"+r.knapsize+".txt");
             FileOutputStream fos = new FileOutputStream(file_name, true);
             PrintStream ps = new PrintStream(fos);
             System.setOut(ps);
         }
         catch(FileNotFoundException e) {
             System.out.println("Problem with output file");
         }
	 }
	 public static void File_Console(){
		 System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	 }
}
