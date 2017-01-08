package pancake;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Pancakes {

	public static void main(String[] args) throws IOException {
		ArrayList<Character> modon= new ArrayList<Character>();
		int result=0;
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/B-large.in"));
		FileWriter writer = new FileWriter( "C:/Users/admin/Downloads/B.txt");
		int T=Integer.parseInt(scanner.nextLine());
        for(int i=1;i<=T;i++){
        	String scan = scanner.nextLine();
        	char[] array = scan.toCharArray();
        	char temp=array[0];
        	modon.add(temp);
        	  	for(int j=0;j<array.length;j++){
        		if(array[j]==temp)continue;
        		else{
        			temp=array[j];
        			modon.add(temp);
        		}
        	}
        	if(modon.get(modon.size()-1)=='+') result=modon.size()-1;
        	else result=modon.size();
        	modon.clear();
        	writer.append("Case #"+i+": "+result);
        	writer.append("\n");
        }
        scanner.close();
        writer.close();
	}

}
