package zombies;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class Zombie {

	public static void main(String[] args) throws IOException {
	/*	double a= Math.pow(20,20);
		String aStr = Double.toString(a);
		System.out.println("Value of a: "+aStr);
		System.out.println("Maximum value of integer: "+Double.toString(Double.MAX_VALUE));*/
		DecimalFormat df = new DecimalFormat("0.000000");
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/Java_Practice/Zombie/fighting_the_zombie.txt"));
		FileWriter writer = new FileWriter( "C:/Users/admin/Downloads/Java_Practice/Zombie/B.txt");
		int T=Integer.parseInt(scanner.nextLine());
        for(int i=1;i<=T;i++){
        	double result= 0.0;
        	ArrayList<Double> Prob_Values =new ArrayList<Double>();
        	String[] scan = StringUtils.split(scanner.nextLine());
        	int H=Integer.parseInt(scan[0].trim());
        //	System.out.println("H="+H);
        	int S=Integer.parseInt(scan[1].trim());
        //	System.out.println("S="+S);
        	String[] spell = StringUtils.split(scanner.nextLine());
        	for(int j=0;j<S;j++){
        	//	System.out.println(spell[j].trim());
        		 String[] data = (spell[j].trim()).split("d|[+]|-");
        		 int[] myIntArray=new int[3];
        		 if(data.length ==2)  myIntArray = new int[]{Integer.parseInt(data[0].trim()),Integer.parseInt(data[1].trim()),0};
        		 else if((spell[j].trim()).contains("-")) myIntArray = new int[]{Integer.parseInt(data[0].trim()),Integer.parseInt(data[1].trim()),(-1)*(Integer.parseInt(data[2].trim()))};
        		 else myIntArray = new int[]{Integer.parseInt(data[0].trim()),Integer.parseInt(data[1].trim()),Integer.parseInt(data[2].trim())};
        	     //   System.out.println(Arrays.toString(myIntArray));
        		 if((H-myIntArray[2]) <= myIntArray[0]) Prob_Values.add(1.0);
        		 else if((H-myIntArray[2]) > (myIntArray[0]*myIntArray[1])) Prob_Values.add(0.0);
        		 else if ((H-myIntArray[2]) == (myIntArray[0]*myIntArray[1])) Prob_Values.add((1/Math.pow(myIntArray[1], myIntArray[0])));
        		 else{
        	        Map<Integer, Double> initial = new HashMap<Integer, Double>(); 
        	        Map<Integer, Double> finish = new HashMap<Integer, Double>();
        	        for(int k=1;k<=myIntArray[1];k++) initial.put(k,1.0);
        	        if(myIntArray[0]==1) finish.putAll(initial);
        	        else{
        	          for(int l=2;l<=myIntArray[0];l++){
        	        	   for(int k=1;k<=myIntArray[1];k++){
        	        		         for(int g: initial.keySet()){
        	        		        	 int key =g+k;
        	        		        	   if(!(finish.containsKey(key))) finish.put(key, initial.get(g));
        	        		        	   else{
        	        		        		   double o=finish.get(key);
        	        		        		   finish.put(key, (o+initial.get(g)));
        	        		        	   }
        	        		        	    }
        	        		              }
        	        	//   System.out.println("Iteration number: "+l);
        	        	   initial.clear();
        	        	   initial.putAll(finish);
        	        	/*   for (Map.Entry<Integer, Double> entry : initial.entrySet())
        	                   System.out.println("key=" + entry.getKey() + ", value=" + entry.getValue());
        	        	       int d=0;
        	        	   for (double value : initial.values()) {
        	                   d += value;
        	               }*/
        	        	   finish.clear();
        	        	//   System.out.println("Size of values: "+d);
        	        	//   System.out.println("Size of Finish: "+finish.size());
        	        	   }
        	            }  
        	        double su=0.0, tot=0.0;
        	      for(int key:initial.keySet()){ 
        	    	  tot=tot+initial.get(key);
        	    	  
        	    	  if(key < H-myIntArray[2]) 
        	    		  su=su+initial.get(key);
        	   }
        	   //   System.out.println("Total"+tot);
        	   //   System.out.println("Su"+su);
        	      double b =((tot-su)/tot);
        	  //    System.out.printf("%.6f", b);
        	  //    System.out.println("\n");
        	      Prob_Values.add(b);
        	 }
        	}
        	result =Collections.max(Prob_Values);
           	writer.append("Case #"+i+": "+String.valueOf(df.format(result)));
        	writer.append("\n");
        }
        scanner.close();
        writer.close();
	}

}
