package pie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class ProgressPie {

	public static void main(String[] args) throws IOException {
		
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/Java_Practice/ProgressPie/progress_pie.txt"));
		FileWriter writer = new FileWriter( "C:/Users/admin/Downloads/Java_Practice/ProgressPie/B.txt");
		int T=Integer.parseInt(scanner.nextLine());
        for(int i=1;i<=T;i++){
        	 String result;
        	 String[] scan = StringUtils.split(scanner.nextLine());
			 int P=Integer.parseInt(scan[0]);
			 int X=Integer.parseInt(scan[1]);
			 int Y=Integer.parseInt(scan[2]);
			 double r = Math.toDegrees(Math.atan2(Y-50, X-50));
			 double p = (r<0.0)?(r+360)/3.6:r/3.6;
			 double d = Math.pow((Math.pow(X-50,2)+Math.pow(Y-50, 2)),0.5);
			 result = ((p <P) && (d <50.0))?"black":"white";
			// System.out.println("Math.atan2(" + X + "," + Y + ")=" + p+"---"+d);
			/*System.out.println("Value of P:"+P);
			System.out.println("Value of X:"+X);
			System.out.println("Value of Y:"+Y);*/
        	writer.append("Case #"+i+": "+result);
        	writer.append("\n");
        }
        scanner.close();
        writer.close();

	}

}