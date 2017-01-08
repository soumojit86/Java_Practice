package knapsack;

import java.util.ArrayList;

public class Knapsack_BB {
	
	 public static void main(String[] args) {
		 ArrayList<Integer>v=new ArrayList<Integer>();
		 Reader r= new Reader();
		 r.Data_Reader();
		 BB bb = new BB(r);
		 bb.Branch_Bound(v);
		 bb.Write_Results();
		
	 }
  
		 		
}
