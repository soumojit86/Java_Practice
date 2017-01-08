import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * @author Soumojit
 *
 */

public class Reader {
    
    public Reader(){
    	
    }
   String run_id = "559";
  //  String run_id =new String();
    public Reader(String s) {
        this.run_id = s;

    }
    Boolean updated_model = true;
    String workingDirectory="C:/Users/Soumoku/Downloads/Java/PAMM/testdata/";
    int dc_ttl_avail = 0, maxDCsize = 0, maxldrsize = 0, ttl_slstodate = 0, nslackConstraints = 1, ttl_avail_inv = 0, cum_markdown = 0;
    float min_fullprice = 99999;
    ArrayList<String> prdt = new ArrayList<String>();
    ArrayList<String> stores = new ArrayList<String>();
    ArrayList<String> DCs = new ArrayList<String>();
    ArrayList<Float> Ladder_Values =new ArrayList<Float>();
    Map<Integer, String> Period_name_master = new HashMap<Integer, String>();
    Map<Integer, Integer> Period_markdown_flag_master = new HashMap<Integer, Integer>();
    Map<String, Float> cost = new HashMap<String, Float>();
    Map<String, Float> full_price = new HashMap<String, Float>();
    Map<Integer, Float> VariableDemand_Calculator = new HashMap<Integer, Float>();
    Map<Integer, Float> FixedDemand_Calculator = new HashMap<Integer, Float>();
    Map<Integer, Float> trgt_lb = new HashMap<Integer, Float>();
    Map<Integer, Float> trgt_ub = new HashMap<Integer, Float>();
    Map<Integer, String> period = new TreeMap<Integer, String>();
    Map<Integer, Integer> markdown = new LinkedHashMap<Integer, Integer>();
    Map<String, Integer> slstodate = new HashMap<String, Integer>();
    Map<Integer,Integer> periodwisesales = new TreeMap<Integer, Integer>();
    Map<Integer, Integer> period_index = new HashMap<Integer, Integer>();
    Multimap<String, String> storeDC = ArrayListMultimap.create();
    Multimap<Integer,String> periodProduct =ArrayListMultimap.create();
    Table<Integer, Integer, Float> ladder = TreeBasedTable.create();
    Table<String, Integer, Float> price_ub = TreeBasedTable.create();
    Table<String, Integer, Float> price_lb = TreeBasedTable.create();
    Table<String, String, Float> elasticity = TreeBasedTable.create();
    Map<Integer, Integer> dc_avail = new TreeMap<Integer, Integer>();
    Map<String,Integer> prdt_inventory =new HashMap<String, Integer>();
    Map<Integer,Integer> ttl_prdt_inventory =new HashMap<Integer, Integer>();
  
    Map<Integer, Integer> Denominator_Item_Timewise = new HashMap<Integer, Integer>();
    Map<Triplet<Integer, String, String>, Integer> dc_rcpt = new HashMap<Triplet<Integer, String, String>, Integer>();
    Map<Triplet<String, String, String>, Integer> store_beg_inventory = new HashMap<Triplet<String, String, String>, Integer>();
    Map<Quartet<Integer, String, String, String>, Float> bdf_var = new HashMap<Quartet<Integer, String, String, String>, Float>();
    Map<Quartet<Integer, String, String, String>, Float> bdf_fx = new HashMap<Quartet<Integer, String, String, String>, Float>();
    Map<Quartet<Integer, String, String, String>, Float> prc_var = new HashMap<Quartet<Integer, String, String, String>, Float>();
    Map<Quartet<Integer, String, String, String>, Float> prc_fx = new HashMap<Quartet<Integer, String, String, String>, Float>();

    /**
     * Returns an InputStream for the file which is in the same package as
     * TestData.class
     * 
     * @param name
     * @return null or an InputStream
     */
 /*   public InputStream new File(workingDirectory+final String name) {
        InputStream retVal = null;

        if (name != null && !name.isEmpty()) {
            Resource r = new ClassPathResource("/com/gap/plan/testdata/" + name);
            try {
                retVal = r.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return retVal;
    }*/

    public void Period_Master() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"period.csv"));
        scanner.nextLine(); // read the first line and throw it away

        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");

            Period_name_master.put(Integer.parseInt(s[0].trim()), s[1].trim());
            Period_markdown_flag_master.put(Integer.parseInt(s[0].trim()), Integer.parseInt(s[2].trim()));
        }
        scanner.close();
    }

    public void Product() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"main_table.csv"));
        scanner.nextLine(); // read the first line and throw it away

        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (run_id.equals(s[0])) 
                prdt.add(s[2]);
        }

        // checking if reading happened correctly
        for (int i = 0; i < prdt.size(); i++)
            System.out.println(prdt.get(i));
        System.out.println("Number of Products in analysis:" + prdt.size());

        scanner.close();
    }

    public void Cost() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"cost.csv"));

        for (String p : prdt)
            cost.put(p, (float) 0.0);
        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");

            if (prdt.contains(s[0]))
                cost.put(s[0], Float.valueOf(s[1].trim()).floatValue());
        }

        // checking if costs are properly read
        for (int i = 0; i < prdt.size(); i++)
            System.out.println(prdt.get(i) + '\t' + cost.get(prdt.get(i)));
        System.out.println("Number of products with costs read:" + cost.size());

        scanner.close();
    }

    public void FullPrice() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"full_price.csv"));

        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (prdt.contains(s[0])) {
                float f = Float.parseFloat(s[1].trim());
                full_price.put(s[0], f);
                if (f < min_fullprice)
                    min_fullprice = f;
            }

        }

        // checking if full prices are properly read
        for (int i = 0; i < prdt.size(); i++)
            System.out.println(prdt.get(i) + '\t' + full_price.get(prdt.get(i)));
        System.out.println("Number of products with prices read:" + full_price.size());
        System.out.println("Minimum Full Price among products:" + min_fullprice);

        scanner.close();
    }

    public void SellThroughRates() throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(workingDirectory+"sellThroughRates.csv"));
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");
            if (run_id.equals(s[1])) {
                trgt_lb.put(Integer.parseInt(s[0].trim()), Float.parseFloat(s[2].trim()) - Float.parseFloat(s[3].trim()));
                trgt_ub.put(Integer.parseInt(s[0].trim()), Float.parseFloat(s[2].trim()) + Float.parseFloat(s[4].trim()));
            }
        }
        // check if targets have been read correctly
        for (Map.Entry<Integer, Float> entry : trgt_lb.entrySet())
            System.out.println("period=" + entry.getKey() + ", trgt_lb=" + entry.getValue());

        for (Map.Entry<Integer, Float> entry : trgt_ub.entrySet())
            System.out.println("period=" + entry.getKey() + ", trgt_ub=" + entry.getValue());

        scanner.close();
    }

    public void Stores() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"stores.csv"));
        scanner.nextLine(); // read the first line and throw it away

        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (run_id.equals(s[1]))
                stores.add(s[0]);
        }

       

        scanner.close();
    }

    public void Ladder() throws FileNotFoundException {

     	Scanner scanner = new Scanner(new File(workingDirectory+"ldr.csv"));
        scanner.nextLine();
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");

            if (min_fullprice == Float.parseFloat(s[0].trim()))
            	
            	ladder.put(Integer.parseInt(s[2].trim()), Integer.parseInt(s[1].trim()), Float.parseFloat(s[3].trim()));
            
        }

        System.out.println("\n---[Find all ladder Values]-----");

        for (int i : period.keySet()) {
            System.out.println("Period Name:" + i);
            Map<Integer, Float> ldr = ladder.row(i);
            if (maxldrsize < ldr.size())
                maxldrsize = ldr.size();
            for (Map.Entry<Integer, Float> l : ldr.entrySet())
                System.out.println("Ladder Name: " + l.getKey() + ", Price: " + l.getValue());
        }

        scanner.close();
        System.out.println("Maximum Number of steps in any ladder:" + maxldrsize);
    }

    public void DC() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"dc_inventory.csv"));
        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (prdt.contains(s[1].trim())) {
                if (!DCs.contains(s[2].trim())) 
                    DCs.add(s[2].trim());
                            
                
                Triplet<Integer, String, String> t = new Triplet<Integer, String, String>(Integer.parseInt(s[0].trim()), s[1].trim(), s[2].trim());
                
                dc_rcpt.put(t, Integer.parseInt(s[3].trim()));

             
                dc_ttl_avail = dc_ttl_avail + Integer.parseInt(s[3]);
            }

        }
        scanner.close();

        // checking the proper read happening
        

        for (int g : period.keySet()) {
            for (String d : prdt) {
                for (String h : DCs) {
                    Triplet<Integer, String, String> t = new Triplet<Integer, String, String>(g, d, h);
                    if (dc_rcpt.get(t) != null)
                        System.out.println(g + "\t" + d + "\t" + h + "\t" + dc_rcpt.get(t));
                }
            }
        }
        System.out.println("Total Receipts of products:" + dc_ttl_avail);
        System.out.println("Productwise Availability in DCs");
       
    }

    public void Sales() throws FileNotFoundException {

        Scanner scanner = new Scanner(new File(workingDirectory+"sales.csv"));
        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (prdt.contains(s[0].trim())) {
            	int temp=0;
            	if(slstodate.containsKey(s[0].trim()))
            		 temp= slstodate.get(s[0].trim());
            	slstodate.put(s[0].trim(), Integer.parseInt(s[3].trim())+temp) ;
            	
                ttl_slstodate = ttl_slstodate + Integer.parseInt(s[3].trim());
            }
        }
        
        for(Integer i:period.keySet()){
        	Collection<String> pi =periodProduct.get(i);
        	int temp = 0;
        	for(String p:prdt)
        		if(pi.contains(p)) temp=temp+slstodate.get(p);
        	periodwisesales.put(i, temp);
        }
        
        
        System.out.println("\nValues of sales to date periodwise wise after iterating over it : ");
        for (Integer key : periodwisesales.keySet()) {
            System.out.println(key + ":\t" + periodwisesales.get(key));
        }
        
        System.out.println("Total sales to date:" + ttl_slstodate);
        ttl_avail_inv = ttl_slstodate + dc_ttl_avail;
        System.out.println("Total Avaialble Inventory comprising of DC receipts and Total Sales:" + ttl_avail_inv);
        scanner.close();

    }

    public void Demand() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"demand.csv"));
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");
            if (prdt.contains(s[1].trim())) {
            	
            	 if (!DCs.contains(s[3].trim())) 
                     DCs.add(s[3].trim());
            	 
            	 if (!stores.contains(s[2].trim())) 
                     stores.add(s[2].trim());

                Quartet<Integer, String, String, String> q = new Quartet<Integer, String, String, String>(Integer.parseInt(s[0].trim()), s[1].trim(), s[3].trim(), s[2].trim());
                bdf_var.put(q, Float.parseFloat(s[4].trim()));
                bdf_fx.put(q, Float.parseFloat(s[5].trim()));
                prc_fx.put(q, Float.parseFloat(s[7].trim()));
                prc_var.put(q, min_fullprice);

                if (!storeDC.containsEntry(s[2].trim(), s[3].trim()))
                    storeDC.put(s[2].trim(), s[3].trim());

                if(!periodProduct.containsEntry(Integer.parseInt(s[0].trim()), s[1].trim()))
                	periodProduct.put(Integer.parseInt(s[0].trim()), s[1].trim());
            
            }
        }
        scanner.close();

        
     // checking if reading happened correctly
        System.out.println("Writing out the DCs in the system");
        for (String dc : DCs)
            System.out.println(dc);
        System.out.println("Number of DCs in the system:" + DCs.size());
        
        System.out.println("Writing out the stores in the system");
        for (int i = 0; i < stores.size(); i++)
            System.out.println(stores.get(i));
        System.out.println("Number of Stores in analysis:" + stores.size());
        
 
 System.out.println("prininting bdf_var");
        for (int i : period.keySet()) {
            for (String p : prdt) {
                for (String dc : DCs) {
                    for (String st : stores) {
                        Quartet<Integer, String, String, String> ql = new Quartet<Integer, String, String, String>(i, p, dc, st);
                        if (bdf_var.get(ql) != null)
                            System.out.println(i + "\t" + p + "\t" + dc + "\t" + st + "\t" + bdf_var.get(ql));
                    }
                }
            }
        }
        System.out.println("prininting bdf_fx");
        for (int i : period.keySet()) {
            for (String p : prdt) {
                for (String dc : DCs) {
                    for (String st : stores) {
                        Quartet<Integer, String, String, String> qm = new Quartet<Integer, String, String, String>(i, p, dc, st);
                        if (bdf_fx.get(qm) != null)
                            System.out.println(i + "\t" + p + "\t" + dc + "\t" + st + "\t" + bdf_fx.get(qm));
                    }
                }
            }
        }

        System.out.println("prininting prc_var");
        for (int i : period.keySet()) {
            for (String p : prdt) {
                for (String dc : DCs) {
                    for (String st : stores) {
                        Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, p, dc, st);
                        if (prc_var.get(qn) != null)
                            System.out.println(i + "\t" + p + "\t" + dc + "\t" + st + "\t" + prc_var.get(qn));
                    }
                }
            }
        }

        System.out.println("prininting prc_fx");
        for (int i : period.keySet()) {
            for (String p : prdt) {
                for (String dc : DCs) {
                    for (String st : stores) {
                        Quartet<Integer, String, String, String> qo = new Quartet<Integer, String, String, String>(i, p, dc, st);
                        if (prc_fx.get(qo) != null)
                            System.out.println(i + "\t" + p + "\t" + dc + "\t" + st + "\t" + prc_fx.get(qo));
                    }
                }
            }
        }

        System.out.println("Fetching Stores and corresponding [Multiple] DCs: \n");

        // get all the set of key
        Set<String> keys = storeDC.keySet();
        // iterate through the key set and display key and values
        for (String key : keys) {
            System.out.println("Store = " + key);
            System.out.println("DCs = " + storeDC.get(key) + "\n");
            if (maxDCsize < storeDC.get(key).size())
                maxDCsize = storeDC.get(key).size();
        }

        System.out.println("Number of key value pairs in StoreDC:" + storeDC.size());
        System.out.println("Maximum number of DCs attached to a store:" + maxDCsize);
   
        
        
        System.out.println("Fetching Periods and corresponding [Multiple] Products: \n");

        // get all the set of key
        Set<Integer> periods = periodProduct.keySet();
        // iterate through the key set and display key and values
        for (Integer p : periods) {
            System.out.println("Period = " + p);
            System.out.println("Products = " + periodProduct.get(p) + "\n");
            
        }

    
    }

    public void Elasticity() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"elasticity.csv"));
        while (scanner.hasNext()) {
            String[] s = scanner.next().split(",");
            if (prdt.contains(s[1]))
                elasticity.put(s[1].trim(), s[2].trim(), Float.parseFloat(s[3].trim()));

        }

        scanner.close();
    }

    public void Period() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"period_product.csv"));
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");
            if (prdt.contains(s[0])) {
                if (!period.containsKey(Integer.parseInt(s[1].trim()))) {
                    period.put(Integer.parseInt(s[1].trim()), Period_name_master.get(Integer.parseInt(s[1].trim())));

                    markdown.put(Integer.parseInt(s[1].trim()), cum_markdown + Period_markdown_flag_master.get(Integer.parseInt(s[1].trim())));

                }
                for (String ss : stores) {
                    price_ub.put(ss, Integer.parseInt(s[1].trim()), Float.parseFloat(s[3].trim()));
                    price_lb.put(ss, Integer.parseInt(s[1].trim()), Float.parseFloat(s[2].trim()));

                }
            }
        }
        nslackConstraints = nslackConstraints + Collections.max(markdown.values());
        int idx=0;
        for (int i : period.keySet()) {
            period_index.put(idx, i);
            idx++;
        }

               
        System.out.println("\nValues of map after iterating over it : ");
        for (Integer key : period.keySet()) {
            System.out.println(key + ":\t" + period.get(key));
        }

        System.out.println("\nSize of Upper Bound Table: " + price_ub.size());
        System.out.println("\nSize of Lower Bound Table: " + price_lb.size());

        System.out.println("\nMarkdown Table:");
        for (int i : period.keySet())
            System.out.println("Period Name:" + i + "  Cumulative Markdown Value:  " + markdown.get(i));
     
        scanner.close();
        System.out.println("Number of slack constraints to be built:  " + nslackConstraints);
    }

    public void Inventory() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"Inventory.csv"));
        int d = 0;
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");
            if (prdt.contains(s[0])) {
                Triplet<String, String, String> q = new Triplet<String, String, String>(s[0].trim(), s[1].trim(), s[3].trim());
                store_beg_inventory.put(q, Integer.parseInt(s[2].trim()));
                int temp =0;
                if(prdt_inventory.containsKey(s[0].trim())) temp=prdt_inventory.get(s[0].trim());
                prdt_inventory.put(s[0].trim(), temp+Integer.parseInt(s[2].trim()));
            }

        }
        for (float value : store_beg_inventory.values()) {
            d += value;
        }
        scanner.close();
        System.out.println("\nValues of product wise inventory after iterating over it : ");
        for (String key : prdt_inventory.keySet()) {
            System.out.println(key + ":\t" + prdt_inventory.get(key));
        }

        
        ttl_avail_inv = ttl_avail_inv + d;
        System.out.println("Initial total Store Inventory: " + d);
        System.out.println("Total Inventory: " + ttl_avail_inv);
    }
    
    public void ProductwiseInventory() throws FileNotFoundException {
    	
    	
    	for(int i=0;i<period.size();i++) {
    		int temp=0;
    		Collection<String> str = periodProduct.get(period_index.get(i));
    		for(String p:prdt){
    			if(str.contains(p)){
    				for(int j=0;j<=i;j++){
    					for(String dc:DCs){
    						 Triplet<Integer, String, String> t = new Triplet<Integer, String, String>(period_index.get(j),p, dc);
    					if(dc_rcpt.containsKey(t)) temp=temp+dc_rcpt.get(t);
    					
    					}
    				}
    			}
    		}
    		dc_avail.put(period_index.get(i), temp);
    	}
    	System.out.println("Periodwise DCs availability");
    	 for (Integer d : period.keySet()) {
             System.out.println(d + "\t" + dc_avail.get(d));
           
       }  	
    	
    	
    	for(Integer i:period.keySet()){
    		Collection<String> pi = periodProduct.get(i);
    		int temp=0;
    		for(String p:prdt)
    			if(pi.contains(p)) temp=temp+ prdt_inventory.get(p);
    		ttl_prdt_inventory.put(i, temp);
    	}
    	System.out.println("Periodwise Product Inventory");
    	for (Integer key : ttl_prdt_inventory.keySet()) 
            System.out.println(key + ":\t" + ttl_prdt_inventory.get(key));
    			
    	for(Integer i:period.keySet())
    		Denominator_Item_Timewise.put(i,ttl_prdt_inventory.get(i)+periodwisesales.get(i)+dc_avail.get(i));
    		
    	
    	 System.out.println("\nValues of Timewise Sales after iterating over it : ");
         for (Integer key : periodwisesales.keySet()) 
             System.out.println(key + ":\t" + periodwisesales.get(key));
             
        System.out.println("\nValues of Total Inventory  available period wise after iterating over it : ");
         for (Integer key : Denominator_Item_Timewise.keySet()) 
                 System.out.println(key + ":\t" + Denominator_Item_Timewise.get(key));
    }
    
    public void Ladder_List_Extraction() throws FileNotFoundException {
    	Scanner scanner = new Scanner(new File(workingDirectory+"ldr.csv"));
        scanner.nextLine();
        while (scanner.hasNext()) {

            while (scanner.hasNext()) {
                String[] s = scanner.next().split(",");
                if (!Ladder_Values.contains(Float.parseFloat(s[0].trim()))) 
                    Ladder_Values.add(Float.parseFloat(s[0].trim()));
            }
            Float[] array = Ladder_Values.toArray(new Float[Ladder_Values.size()]);
            java.util.Arrays.sort(array);
            
            int index = java.util.Arrays.binarySearch(array, min_fullprice);
            if(index<0){
            	float min = Float.MAX_VALUE;
                float closest = min_fullprice;

                for (float v : array) {
                    final float diff = Math.abs(v - min_fullprice);

                    if (diff < min) {
                        min = diff;
                        closest = v;
                    }
                }

                min_fullprice=closest;
            }
            System.out.println("Minimum Full Price is:"+ min_fullprice);
            
            
        }
        scanner.close();
    }
    
    public void VariableDemandCalculator() throws FileNotFoundException{
    	
    	  for (int i : period.keySet()) {
          	Collection<String> pi =periodProduct.get(i);
          	
          	float temp1=(float) 0.0;
          	float temp2=(float) 0.0;     
              for (int p = 0; p < prdt.size(); p++) {
              	if(pi.contains(prdt.get(p))){
                  for (int dc = 0; dc < DCs.size(); dc++) {

                      for (int st = 0; st < stores.size(); st++) {
                      	Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, prdt.get(p), DCs.get(dc), stores.get(st));
                      	
                      	if(bdf_var.containsKey(qn))temp1=temp1+bdf_var.get(qn);
                      	if(bdf_fx.containsKey(qn))temp2=temp2+bdf_fx.get(qn);           	           
                                      
                          }

                         } 
                      }
                  }
                        
              VariableDemand_Calculator.put(i, temp1);
              FixedDemand_Calculator.put(i,temp2);
    	  }  
    	  
    	  for(int i:VariableDemand_Calculator.keySet())
    		  System.out.println("Time Period:\t"+i+"\t"+"Variable Demand:\t"+VariableDemand_Calculator.get(i));
    
    	  for(int i:FixedDemand_Calculator.keySet())
    		  System.out.println("Time Period:\t"+i+"\t"+"Fixed Demand:\t"+FixedDemand_Calculator.get(i));
    
    }
    public void Period_Prc_Bound() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(workingDirectory+"period_prc_bound.csv"));
        while (scanner.hasNext()) {

            String[] s = scanner.next().split(",");
            if (run_id.equals(s[0])) {
            	 if (!period.containsKey(Integer.parseInt(s[1].trim()))) {
                     period.put(Integer.parseInt(s[1].trim()), Period_name_master.get(Integer.parseInt(s[1].trim())));

                     markdown.put(Integer.parseInt(s[1].trim()), cum_markdown + Period_markdown_flag_master.get(Integer.parseInt(s[1].trim())));

                 }
                for (String ss : stores) {
                    price_ub.put(ss, Integer.parseInt(s[1].trim()), Float.parseFloat(s[3].trim()));
                    price_lb.put(ss, Integer.parseInt(s[1].trim()), Float.parseFloat(s[2].trim()));

                }
            }
        
        }
        nslackConstraints = nslackConstraints + Collections.max(markdown.values());
        int idx=0;
        for (int i : period.keySet()) {
            period_index.put(idx, i);
            idx++;
        }
        System.out.println("\nValues of map after iterating over it : ");
        for (Integer key : period.keySet()) {
            System.out.println(key + ":\t" + period.get(key));
        }

      

        System.out.println("\nMarkdown Table:");
        for (int i : period.keySet())
            System.out.println("Period Name:" + i + "  Cumulative Markdown Value:  " + markdown.get(i));
     
        
        System.out.println("Number of slack constraints to be built:  " + nslackConstraints);
        System.out.println("\n---[Find all price_ub Values]-----");

        for (int i : period.keySet()) {
            System.out.println("Period Name:" + i);
            Map<String, Float> pu = price_ub.column(i);
            for (Map.Entry<String, Float> p : pu.entrySet())
                System.out.println("Store Name: " + p.getKey() + ", Price: " + p.getValue());
        }

        System.out.println("\n---[Find all price_lb Values]-----");
        for (int i : period.keySet()) {
            System.out.println("Period Name:" + i);
            Map<String, Float> pl = price_lb.column(i);
            for (Map.Entry<String, Float> p : pl.entrySet())
                System.out.println("Store Name: " + p.getKey() + ", Price: " + p.getValue());
        }
        scanner.close();
        System.out.println("\nSize of Upper Bound Table: " + price_ub.size());
        System.out.println("\nSize of Lower Bound Table: " + price_lb.size());
        
    }
    public void DataReader() throws FileNotFoundException {
        Period_Master();
        Product();
        Cost();
        FullPrice();
        Stores();
       // Period();
        Period_Prc_Bound();
        SellThroughRates();
        Ladder_List_Extraction();
        DC();
        Demand();
        VariableDemandCalculator();
        Ladder();
        Sales();
        Elasticity();
        Inventory();
        ProductwiseInventory();
        
    }

}
