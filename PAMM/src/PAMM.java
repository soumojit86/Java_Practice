import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class PAMM {
	public static void main(String[] args){
		ArrayList<String> runs = new ArrayList<String>();
		try {
			Scanner scanner = new Scanner(new File("C:/Users/Soumoku/Downloads/Java/PAMM/testdata/runs2.csv"));
			scanner.nextLine();
			
			while (scanner.hasNext())
	        {
	        	String[] s = scanner.next().split(",");
	        	if(!runs.contains(s[0].trim()))
	        	runs.add(s[0].trim());
	        	
	        }
	         scanner.close();
		
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
          
		for(String stri: runs){	
	        try {
	           Reader r = new Reader(stri);
	        	//Reader r =new Reader();
	            r.DataReader();
	            OptimizationEngine o = new OptimizationEngine(r);
	            o.Optimizer();
	        } catch (Exception e) {
	            System.out.println("Error code: " + e.getMessage());
	           // Assert.fail(e.getMessage());
	          }
	    }
	}
}