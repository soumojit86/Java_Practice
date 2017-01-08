package knapsack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

public class Reader {
	int nitems=0, knapsize=0;
	ArrayList<Item> Items= new ArrayList<Item>();
	public Reader(){
		
	}
	public void Data_Reader(){
		
      try {
	    Scanner	scanner = new Scanner(new File("C:/Users/Soumoku/Downloads/Language_Practice/Tutorial Videos/Discrete Optimization/Assignments/knapsack/data/ks_30_0"));
		 String[] f1 = scanner.next().split(" ");
		 nitems=Integer.parseInt(f1[0].trim());
		 String[] f2 = scanner.next().split(" ");
		 knapsize=Integer.parseInt(f2[0].trim());
	    int i=1;
		 while (scanner.hasNext()) {
	            String[] s1 = scanner.next().split(" ");
	            int t1=Integer.parseInt(s1[0].trim());
	            String[] s2 = scanner.next().split(" ");
	            int t2=Integer.parseInt(s2[0].trim());
	          Item itm= new Item(t1,t2);
	          itm.setId(i);
               Items.add(itm);
               i++;
	        }
	        scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      Collections.sort(Items, new Comparator<Item>() {
          @Override public int compare(Item p1, Item p2) {
              return (int)(100000*(p2.ratio - p1.ratio)); // Descending
          }

      });
      System.out.println("Size of Items: "+Items.size());
      System.out.println("Size of Knapsack: "+knapsize);
      System.out.println("Number of Items: "+nitems);
      for(int i=0;i<Items.size();i++){
      	System.out.println(Items.get(i).id+"\t"+Items.get(i).value+"\t"+Items.get(i).weight+"\t"+Items.get(i).ratio);
  	}
	    }
}
