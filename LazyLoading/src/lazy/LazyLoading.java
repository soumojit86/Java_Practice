package lazy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class LazyLoading {

	public static void main(String[] args) throws IOException {
		int result=0;
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/Java_Practice/LazyLoading/lazy_loading.txt"));
		FileWriter writer = new FileWriter( "C:/Users/admin/Downloads/Java_Practice/LazyLoading/B.txt");
		int T=Integer.parseInt(scanner.nextLine());
        for(int i=1;i<=T;i++){
        	ArrayList<Integer> modon= new ArrayList<Integer>();
        	int N=Integer.parseInt(scanner.nextLine());
        	for(int j=1;j<=N;j++) 	
        		modon.add(Integer.parseInt(scanner.nextLine()));
        	Collections.sort(modon);
        	result = Loader(modon);
        	result--;
        	System.out.println(modon.size());
        	writer.append("Case #"+i+": "+result);
        	writer.append("\n");
        }
        scanner.close();
        writer.close();
	}
 
 public static	int Loader(ArrayList<Integer> modon){
	if(modon.size()==0) return 0;
	  int last =modon.get(modon.size()-1);
	   modon.remove(modon.size()-1);
		if(last <50){
			int g =Math.min(modon.size(), (int) Math.ceil((double)(50.0/(double)last)))-1;
		for(int y=0;y<g;y++)
			modon.remove(0);			
		}
	 
	 return 1+Loader(modon);
 }
 public static int sum (ArrayList<Integer> list) {
	    int sum = 0;
	    for (int i: list) {
	        sum += i;
	    }
	    return sum;
	}
}