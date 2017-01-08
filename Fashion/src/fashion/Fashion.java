package fashion;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class Fashion {
	public static void main(String[] args) throws IOException {
		Scanner scanner = new Scanner(new File("C:/Users/admin/Downloads/C-small-attempt0.in"));
		int T=Integer.parseInt(scanner.nextLine());
		
		int J = 0, P = 0, S=0, N=0;
		while(scanner.hasNextLine()){
			String[] scan = StringUtils.split(scanner.nextLine());
			 J=Integer.parseInt(scan[0]);
			 P=Integer.parseInt(scan[1]);
			 S=Integer.parseInt(scan[2]);
			 N=Integer.parseInt(scan[3]);
		}
	}
}
