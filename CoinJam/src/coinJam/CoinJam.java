package coinJam;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CoinJam {
	public CoinJam(){
		
	}
	
	
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/C-large.in"));
		int T=Integer.parseInt(scanner.nextLine());
		int N = 0, J = 0;
		while(scanner.hasNextLine()){
			String[] scan = StringUtils.split(scanner.nextLine());
			 N=Integer.parseInt(scan[0]);
			 J=Integer.parseInt(scan[1]);
		}
		scanner.close();
		FileWriter writer = new FileWriter( "C:/Users/admin/Downloads/C.txt");
		writer.append("Case #"+T+": \n");
		ArrayList <Double> C= new ArrayList<Double>();
		ArrayList <Integer> A= new ArrayList<Integer>();
		ArrayList <Integer> B= new ArrayList<Integer>();
		Set<List<Integer>> strSet = new HashSet<List<Integer>>();
		 
		while(strSet.size()<J){
		for(int i=0;i<(N-1);i++){
			if((i==0)||(i==N-2)) A.add(1);
			else if((i==1)||(i==N-3)) A.add(0);
			else A.add(2);
		}
        for(int i=2;i<N-3;i++){
        	Random rand = new Random();
        	int x = rand.nextInt(10);
        	if((A.get(i-1)!=1)&&(A.get(i+1)!=1)&&(x<=5))
        		A.set(i, 1);
        	else A.set(i, 0);
        }
        for(int i=0;i<N;i++){
        	if((i==0)||(i==N-1)) B.add(1);
			else B.add(A.get(i)+A.get(i-1));
        }
        if(!strSet.contains(B)){
        strSet.add(B);
        for(int i=2;i<=10;i++){
        	double h=0.0;
        	for(int j=(A.size()-1);j>=0;j--){
        	 h=h+Math.pow(i,j)*A.get(A.size()-j-1);
        	}
        	C.add(h);
        }
        String s="";
        for(int i=0;i<B.size();i++)
        	s=s+String.valueOf(B.get(i));
        writer.append(s+"\t");
        DecimalFormat df = new DecimalFormat("#");
        for(int i=0;i<C.size();i++)
        	writer.append(String.valueOf(df.format(C.get(i)))+"\t");
           writer.append("\n");
       }
        
        A.clear();
        B.clear();
        C.clear();
	}
	writer.close();	
	}
   
}
