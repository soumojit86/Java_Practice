import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.text.DecimalFormat;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

/**
 * @author Soumojit
 *
 */

public class OptimizationEngine {

    private Reader r;

    private final String OPTIMIZATION_SERVER = "gurobi-preprod.phx.gapinc.dev";

    private final String OUT_DIR = "C:/Users/Soumoku/Downloads/Java/PAMM/output";
    
    private DecimalFormat df = new DecimalFormat("#.##%");
    
    static private int header=0;
    
    private int strict = 1;
    private int pass =0;
    Boolean flag = false;

    public OptimizationEngine(Reader r) {
        this.r = r;

    }

    public void Optimizer() throws IOException {
        Map<Integer, Float> TimewiseSales = new HashMap<Integer, Float>();
        Map<Integer, Float> TimewiseFixedDemand =new HashMap<Integer, Float>();
        Map<Integer, Float> TimewiseVariableDemand =new HashMap<Integer, Float>();
        Map<Integer, Float>  Timewiseunitsales =new HashMap<Integer, Float>();
        Map<Integer, Float>  LowerBounds =new HashMap<Integer, Float>();
        Map<Integer, Float>  UpperBounds =new HashMap<Integer, Float>();
    //    String inputfile =new String();
        long startTime = System.currentTimeMillis();
       while(pass < 2){
    	  
        try {
        //	if((flag) && (pass==1)) inputfile =OUT_DIR + "/" +r.run_id+".mst";
        	GRBEnv env = new GRBEnv(OUT_DIR + "/"+r.run_id+"_"+pass+"PAMM.log", OPTIMIZATION_SERVER, -1, "", 0, -1);
        
		      GRBModel model;
		    //  if((flag) && (pass==1)) model = new GRBModel(env,OUT_DIR + "/" +r.run_id+".mst");
		     // else 
		    	  model = new GRBModel(env);
           if(pass ==0) model.getEnv().set(GRB.DoubleParam.TimeLimit,300.0);
           else model.getEnv().set(GRB.DoubleParam.TimeLimit,310.0);
            model.getEnv().set(GRB.IntParam.Presolve, 2);
            model.getEnv().set(GRB.IntParam.PreSparsify, 1);
            model.getEnv().set(GRB.DoubleParam.MarkowitzTol, 1e-4);
            model.getEnv().set(GRB.IntParam.MIPFocus, 1);
            if (pass==0){
          	  flag=true;
          	  model.getEnv().set(GRB.StringParam.ResultFile, OUT_DIR + "/" +r.run_id+".mst");
  		}
            model.set(GRB.StringAttr.ModelName, "PAMM");

            int nperiods = r.period.size();
            int nDCs = r.DCs.size();
            int nstores = r.stores.size();
            int nproducts = r.prdt.size();
            double BigM = 999999.0;
            double bigM = 99999999.0;
            // declaration of demand variable
            GRBVar[][][][] x = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float>LadderMap =r.ladder.row(i);
                if(LadderMap.size()>0){
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                double d=0.0;
                                if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) 
                                     d = r.bdf_var.get(qn) * Math.pow((Collections.min(r.ladder.row(i).values()) / r.prc_var.get(qn)), r.elasticity.row(r.prdt.get(p)).get(r.stores.get(st)));
                                    x[j][p][st][dc] = model.addVar(0.0, d, 0.0, GRB.CONTINUOUS, "X" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                
                                }
                            }
                        }
                    }
                }
            }
           System.out.println("Demand Variables Declared");
           //Total demand variables declared 
           GRBVar[][][][] td = new GRBVar[nperiods][nproducts][nstores][nDCs];
           for (int i : r.period.keySet()) {
               int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
               
               for (int p = 0; p < r.prdt.size(); p++) {
                   for (int st = 0; st < r.stores.size(); st++) {

                       for (int dc = 0; dc < r.DCs.size(); dc++) {
                           Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                           if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                 double d =0.0;
                               if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) 
                                    d = d+ r.bdf_var.get(qn) * Math.pow((Collections.min(r.ladder.row(i).values()) / r.prc_var.get(qn)), r.elasticity.row(r.prdt.get(p)).get(r.stores.get(st)));
                                if(r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0.0)  d=d+r.bdf_fx.get(qn);
                               td[j][p][st][dc] = model.addVar(0.0, d, 0.0, GRB.CONTINUOUS, "X" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                               
                               
                           }
                       }
                   }
               }
           }
          System.out.println("Total Demand Variables Declared");
           
           
                    
           
           // declaration of variable sales
            GRBVar[][][][] zv = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                double d = 0.0;
                                if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) 
                                    d = r.bdf_var.get(qn) * Math.pow(Collections.min(r.ladder.row(i).values()) / r.prc_var.get(qn), r.elasticity.row(r.prdt.get(p)).get(r.stores.get(st)));

                                    zv[j][p][st][dc] = model.addVar(0.0, d, 0.0, GRB.CONTINUOUS, "ZV" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                
                                
                            }
                        }
                    }
                }
            }
           System.out.println("Variable Sales declared");
            // declaration of fixed sales
            GRBVar[][][][] zf = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                          
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                            	double d=0.0;
                                if (r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0.0) 
                                  d=r.bdf_fx.get(qn);
                                    zf[j][p][st][dc] = model.addVar(0.0, d, 0.0, GRB.CONTINUOUS,
                                            "ZF" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                
                            }
                        }
                    }
                }
            }
           System.out.println("Fixed Sales declared"); 
           
           //declaration of penalty for fixed sales violation
           
           GRBVar[]penalty = new GRBVar[nperiods];
           GRBVar[]relax = new GRBVar[nperiods]; 
           for (int i : r.period.keySet()) {
        	   Map<Integer, Float>LadderMap =r.ladder.row(i);
           	if((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
               int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
             penalty[j] = model.addVar(0.0,r.FixedDemand_Calculator.get(i),0.0,GRB.CONTINUOUS,"Penalty"+"_"+i);
             relax[j] = model.addVar(0.0,GRB.INFINITY,0.0,GRB.CONTINUOUS,"Penalty"+"_"+i);
           	}
          
          }
           
           System.out.println("Penalty for fixed sales violation declared");
                 
           
             // Declaration of Total Sales
     GRBVar[][][][] ts = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                           if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                        	   double d=0.0;
                                if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) 
                                	 d = r.bdf_var.get(qn) * Math.pow(Collections.min(r.ladder.row(i).values()) / r.prc_var.get(qn), r.elasticity.row(r.prdt.get(p)).get(r.stores.get(st)));
                                if(r.bdf_fx.containsKey(qn)) d=d+r.bdf_fx.get(qn);
                                    ts[j][p][st][dc] = model.addVar(0.0, d, 0.0, GRB.CONTINUOUS,
                                            "TS" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                
                            }
                        }
                    }
                }
            }
            
               System.out.println("Total Sales Variable declared");       

            // declaration of price
            GRBVar[][] pr = new GRBVar[nperiods][nstores];

            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));

                for (int st = 0; st < r.stores.size(); st++) {

                    if (r.price_lb.column(i).size() > 0 || r.price_ub.column(i).size() > 0)
                        pr[j][st] = model.addVar(r.price_lb.row(r.stores.get(st)).get(i), r.price_ub.row(r.stores.get(st)).get(i), 0.0, GRB.CONTINUOUS,
                                "P" + "_" + r.period.get(i) + "_" + r.stores.get(st));
                }
            }
            System.out.println("Price Variables Declared");
           
            // declaration of store receipts
            GRBVar[][][][] rs = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc)))
                                rs[j][p][st][dc] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS,
                                        "RS" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                        }
                    }
                }
            }
           System.out.println("Store receipts declared");
            // declaration of ending store inventory
            GRBVar[][][][] vs = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc)))
                                vs[j][p][st][dc] = model
                                        .addVar(0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "VS" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                        }
                    }
                }
            }
            System.out.println("Ending Store Inventory declared");
            //declaration of store beginning inventory
            GRBVar[][][][] vi = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc)))
                                vi[j][p][st][dc] = model
                                        .addVar(0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "VI" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                         }
                    }
                }
            }
            
            System.out.println("Beginning Store Inventory declared");
            
            //declaration of minimum variables constraints
            GRBVar[][][][] dtd = new GRBVar[nperiods][nproducts][nstores][nDCs];
            GRBVar[][][][] dvi = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
               
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                           // Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                
                               // if (r.bdf_var.containsKey(qn)) {
                                   	dtd[j][p][st][dc] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DX" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                	dvi[j][p][st][dc] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "DZV" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                
                              //  }
                            }
                        }
                    }
                }
            }

        System.out.println("Binary Variables for comparison declared");
            // declaration of ending DC inventory
            GRBVar[][][] vdc = new GRBVar[nperiods][nproducts][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int dc = 0; dc < nDCs; dc++) {

                        vdc[j][p][dc] = model.addVar(0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "VDC" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.DCs.get(dc));
                    }
                }
            }
            System.out.println("Ending DC inventory declared");
            // declaration of ladder binary variables

            GRBVar[][][] bp = new GRBVar[nperiods][nstores][r.maxldrsize];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                for (int st = 0; st < r.stores.size(); st++) {
                    for (int l = 0; l < LadderMap.size(); l++) {
                        bp[j][st][l] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BP" + "_" + r.period.get(i) + "_" + r.stores.get(st) + "_" + "ldr" + (l + 1));
                    }
                }
            }
           System.out.println("Binary Variables for ladder declared");
            // binary variables for fixed demand
            GRBVar[][][][] bf = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));

                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                if (r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0)
                                    bf[j][p][st][dc] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BF" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));

                            }
                        }
                    }
                }
            }
            System.out.println("Binary variables for fixed demand declared");
            // binary variables for variable demand
            GRBVar[][][][] bv = new GRBVar[nperiods][nproducts][nstores][nDCs];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));

                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0)
                                    bv[j][p][st][dc] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "BV" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                            }
                        }
                    }
                }
            }
            System.out.println("Binary variables for variable demand declared");
            // Auxiliary variables for linearity
            GRBVar[][][][][] y = new GRBVar[nperiods][nproducts][nstores][nDCs][r.maxldrsize];
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {

                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    for (int l = 0; l < LadderMap.size(); l++)
                                        y[j][p][st][dc][l] = model.addVar(0.0, BigM, 0.0, GRB.CONTINUOUS,
                                                "Y" + "_" + r.period.get(i) + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc) + "_" + "ldr" + (l + 1));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Auxillary variables for linearity declared");
            // slack constraints variables defined

            GRBVar[] lo = new GRBVar[r.nslackConstraints];
            GRBVar[] su = new GRBVar[r.nslackConstraints];
            for (int i = 0; i < r.nslackConstraints; i++){
                lo[i] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "L" + "_" + i);
                su[i] = model.addVar(0.0, 1.0, 0.0, GRB.CONTINUOUS, "U" + "_" + i);
            }
           System.out.println("Slack variables for sell through constraints declared");
            model.update();
            System.out.println("Completed Variable declaration");
            
            if(pass == 0){
                GRBLinExpr expr = new GRBLinExpr();
                for (int i = 0; i < r.nslackConstraints; i++){
                          expr.addTerm(1.0 , lo[i]);
                         expr.addTerm(1.0 ,  su[i]);
                                               
                        }
                
                for (int i : r.period.keySet()) {
              	  int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                    Map<Integer, Float> LadderMap = r.ladder.row(i);
                if ((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
              	  if(j==0) expr.addTerm(100.0, penalty[j]);
              	  else expr.addTerm(1.0, penalty[j]);
                }
                }
                               
                  model.setObjective(expr, GRB.MINIMIZE);
                  model.update();
                  }
               
             else{

              // Objective Function Defined
              if (r.nslackConstraints > 1) {
                  GRBLinExpr expr = new GRBLinExpr();
                  for (int i : r.period.keySet()) {
                  	Collection<String> pi=r.periodProduct.get(i);
                      int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                      Map<Integer, Float> LadderMap = r.ladder.row(i);

                      for (int p = 0; p < r.prdt.size(); p++) {
                          for (int dc = 0; dc < r.DCs.size(); dc++) {
                              if (j == (nperiods - 1)){
                              	if(pi.contains(r.prdt.get(p)))
                              	expr.addTerm((-1 * r.cost.get(r.prdt.get(p))), vdc[j][p][dc]);
                              }
                              for (int st = 0; st < r.stores.size(); st++) {
                                  Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                  if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                      if (j == (nperiods - 1)){
                                      	if(pi.contains(r.prdt.get(p)))
                                          expr.addTerm((-1 * r.cost.get(r.prdt.get(p))), vs[j][p][st][dc]);
                                      }
                                      if (LadderMap.size() > 0) {
                                          for (int l = 0; l < LadderMap.size(); l++)
                                              if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn)>0.0)
                                                  expr.addTerm(LadderMap.get(l + 1), y[j][p][st][dc][l]);

                                      }
                                      if (r.prc_fx.containsKey(qn) && (r.bdf_fx.get(qn) > 0.0))
                                          expr.addTerm(r.prc_fx.get(qn), zf[j][p][st][dc]);

                                  }
                              }
                          }
                      }
                      
                  //   if(LowerBounds.size()==0){  
                      if ((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
                      	if(j==0) expr.addTerm(-99999.0, penalty[j]);
                      	else expr.addTerm(-10000.0, penalty[j]);
                      }
                  //    } 
              //    for (int i = 0; i < r.nslackConstraints; i++){
                //      expr.addTerm(-1 *r.ttl_avail_inv * Collections.max(r.ladder.row(r.period_index.get(nperiods - r.nslackConstraints + i)).values()) , lo[i]);
                 //     expr.addTerm(-1 *r.ttl_avail_inv * Collections.max(r.ladder.row(r.period_index.get(nperiods - r.nslackConstraints + i)).values()),  su[i]);
                  //}
                      
                      }
                //  if(LowerBounds.size()==0){  
                      for (int k = 0; k < r.nslackConstraints; k++){
                              expr.addTerm(-1.0*bigM , lo[k]);
                              expr.addTerm(-1.0*bigM ,  su[k]);
                           //   expr.addTerm(-0.50 *r.ttl_avail_inv * Collections.max(r.ladder.row(r.period_index.get(nperiods - r.nslackConstraints + i)).values()),  su[i]);
                      } 
                      //    } 
                  
                  model.setObjective(expr, GRB.MAXIMIZE);

              }

              else {
                  GRBLinExpr expr = new GRBLinExpr();
                  for (int i : r.period.keySet()) {
                  	int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                      Map<Integer, Float> LadderMap = r.ladder.row(i);
                      Collection<String> pi=r.periodProduct.get(i);
                      for (int p = 0; p < r.prdt.size(); p++) {
                      if(pi.contains(r.prdt.get(p))){
                          for (int dc = 0; dc < r.DCs.size(); dc++) {

                              for (int st = 0; st < r.stores.size(); st++) {
                                  Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                  if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                      if (LadderMap.size() > 0) {
                                          for (int l = 0; l < LadderMap.size(); l++)
                                              if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0)
                                                  expr.addTerm(LadderMap.get(l + 1), y[j][p][st][dc][l]);

                                      }
                                      if (r.prc_fx.containsKey(qn) && (r.bdf_fx.get(qn) > 0.0))
                                          expr.addTerm(r.prc_fx.get(qn), zf[j][p][st][dc]);
                                  }
                              }
                          }
                      }
                      }
                    //  if(LowerBounds.size()==0){ 
                      if(((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0))){
                      	if(j==0) expr.addTerm(-99999.0, penalty[j]);
                      	else expr.addTerm(-10000.0, penalty[j]);
                      }
                   //  }
                  }
               //   if(LowerBounds.size()==0){
                  for (int i = 0; i < r.nslackConstraints; i++){
                      expr.addTerm(-1.0*bigM, lo[i]);
                      expr.addTerm(-1.0*bigM, su[i]);
                    //  expr.addTerm(-0.50 *r.ttl_avail_inv * Collections.max(r.ladder.row(r.period_index.get(nperiods - r.nslackConstraints + i)).values()),  su[i]);
                  }  
                //  }
                      model.setObjective(expr, GRB.MAXIMIZE);

              }
              model.update();
             }
              System.out.println("Completed Objective Setup");

            // Linearity Constraints 1
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                    for (int l = 0; l < LadderMap.size(); l++) {
                                        if (r.bdf_var.containsKey(qn) && (r.bdf_var.get(qn) > 0.0)) {
                                            GRBLinExpr li = new GRBLinExpr();
                                            li.addTerm(1.0, y[j][p][st][dc][l]);
                                            li.addTerm(-BigM, bp[j][st][l]);
                                            model.addConstr(li, GRB.LESS_EQUAL, 0.0, "Lin1" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc) + "_" + "ldr" + (l + 1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Linearity Constraints 2
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    for (int l = 0; l < LadderMap.size(); l++) {
                                        if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) {
                                            GRBLinExpr li = new GRBLinExpr();
                                            li.addTerm(1.0, y[j][p][st][dc][l]);
                                            li.addTerm(-1.0, zv[j][p][st][dc]);
                                            model.addConstr(li, GRB.LESS_EQUAL, 0.0, "Lin2" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc) + "_" + "ldr" + (l + 1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Linearity Constraints 3
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) {
                                        for (int l = 0; l < LadderMap.size(); l++) {
                                            GRBLinExpr li = new GRBLinExpr();
                                            li.addTerm(-1.0, y[j][p][st][dc][l]);
                                            li.addTerm(1.0, zv[j][p][st][dc]);
                                            li.addTerm(BigM, bp[j][st][l]);
                                            model.addConstr(li, GRB.LESS_EQUAL, BigM, "Lin3" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc) + "_" + "ldr" + (l + 1));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // DC Inventory Balance Constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int dc = 0; dc < r.DCs.size(); dc++) {
                        double receipt = 0.0;
                        GRBLinExpr l = new GRBLinExpr();
                        l.addTerm(1.0, vdc[j][p][dc]);
                        if (j != 0)
                            l.addTerm(-1.0, vdc[j - 1][p][dc]);
                        for (int st = 0; st < r.stores.size(); st++) {
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc)))
                                l.addTerm(1.0, rs[j][p][st][dc]);
                        }
                        Triplet<Integer, String, String> t = new Triplet<Integer, String, String>(i, r.prdt.get(p), r.DCs.get(dc));
                        if (r.dc_rcpt.get(t) != null)
                            receipt = r.dc_rcpt.get(t);
                        model.addConstr(l, GRB.EQUAL, receipt, "DCInvBal." + "_" + i + "_" + r.prdt.get(p) + "_" + r.DCs.get(dc));
                    }
                }
            }

            System.out.println("DC Inventory Balance Constraints completed");
            // Total Sales Balance Calculation
              for (int i : r.period.keySet()) {
                    int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                               
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                	
                                	 GRBLinExpr l = new GRBLinExpr();
                                     l.addTerm(1.0, ts[j][p][st][dc]);
                                    if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) l.addTerm(-1.0, zv[j][p][st][dc]);
                                    if (r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0.0)  l.addTerm(-1.0, zf[j][p][st][dc]);
                                    
                                   
                                 model.addConstr(l, GRB.EQUAL, 0.0, "TotalSalesConstr." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                	
                                }
                            }
                        }
                    }
                }
                System.out.println("Total Sales Balance Constraints completed");
             // Total Demand Balance Calculation
                for (int i : r.period.keySet()) {
                      int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                      Map<Integer, Float>LadderMap =r.ladder.row(i);
                      
                      for (int p = 0; p < r.prdt.size(); p++) {
                          for (int st = 0; st < r.stores.size(); st++) {
                              for (int dc = 0; dc < r.DCs.size(); dc++) {
                                  Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                 
                                  if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                	  GRBLinExpr l = new GRBLinExpr();
                                      l.addTerm(1.0, td[j][p][st][dc]);
                                if(LadderMap.size()>0){
                                  
                                  	 
                                        if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0) l.addTerm(-1.0, x[j][p][st][dc]);
                                        if (r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0.0) model.addConstr(l, GRB.EQUAL, r.bdf_fx.get(qn), "TotalDemandConstr." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                        else model.addConstr(l, GRB.EQUAL, 0.0, "TotalDemandConstr." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                  	 
                                 
                                }
                               else if(r.bdf_fx.containsKey(qn))
                                  		model.addConstr(l, GRB.EQUAL, r.bdf_fx.get(qn), "TotalDemandConstr." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                               else    model.addConstr(l, GRB.EQUAL, 0.0, "TotalDemandConstr." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                	  
                              }
                          }
                      }
                    }
                 }
                  System.out.println("Total Demand Balance Constraints completed");
                     
            
            // Store Inventory Calculation
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                           
                            double d = 0.0;
                            Triplet<String, String, String> t = new Triplet<String, String, String>(r.prdt.get(p), r.stores.get(st), r.DCs.get(dc));
                            if (r.store_beg_inventory.containsKey(t))
                                d = r.store_beg_inventory.get(t);
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                            	
                                GRBLinExpr l = new GRBLinExpr();
                                l.addTerm(1.0, vs[j][p][st][dc]);
                              
                                l.addTerm(1.0, ts[j][p][st][dc]);
                                l.addTerm(-1.0, rs[j][p][st][dc]);
                                if (j != 0) {
                                    l.addTerm(-1.0, vs[j - 1][p][st][dc]);
                                    model.addConstr(l, GRB.EQUAL, 0.0, "StoreInvBal." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                } else
                                    model.addConstr(l, GRB.EQUAL, d, "StoreInvBal." + "_" + "D0" + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                            
                            }
                        }
                    }
                }
            }
            System.out.println("Store Inventory Balance Constraints completed");
            // Binary ladder constraint
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        GRBLinExpr l = new GRBLinExpr();

                        for (int lm = 0; lm < LadderMap.size(); lm++)
                            l.addTerm(1.0, bp[j][st][lm]);
                        model.addConstr(l, GRB.EQUAL, 1.0, "BLStTPconst" + "_" + i + "_" + r.stores.get(st));
                    }
                }
            }
            System.out.println("Binary Ladder Constraints completed");
            // Price Constraint
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        GRBLinExpr l = new GRBLinExpr();
                        for (int lm = 0; lm < LadderMap.size(); lm++)
                            l.addTerm(LadderMap.get(lm + 1), bp[j][st][lm]);
                        l.addTerm(-1.0, pr[j][st]);
                        model.addConstr(l, GRB.EQUAL, 0.0, "Priceconst" + "_" + i + "_" + r.stores.get(st));
                    }
                }
            }
            System.out.println("Price Constraints completed");

            // Modified Elasticity Constraints
            for (int i : r.period.keySet()) {
            	
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer, Float> LadderMap = r.ladder.row(i);
                if (LadderMap.size() > 0) {
                    for (int p = 0; p < r.prdt.size(); p++) {
                    	
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                    GRBLinExpr li = new GRBLinExpr();
                                    li.addTerm(-1.0, x[j][p][st][dc]);
                                    if (r.bdf_var.containsKey(qn) & r.prc_var.containsKey(qn)) {
                                        
                                        for (int l = 0; l < LadderMap.size(); l++)

                                            li.addTerm(r.bdf_var.get(qn) * Math.pow((LadderMap.get(l + 1) / r.prc_var.get(qn)), r.elasticity.row(r.prdt.get(p)).get(r.stores.get(st))), bp[j][st][l]);

                                    }
                                        model.addConstr(li, GRB.EQUAL, 0.0, "ME" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                    
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Elasticity Constraints completed");
            
          for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {

                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))){
                               
                          if(j!=0)    
                           {
                        	   GRBLinExpr l = new GRBLinExpr();
                        	   l.addTerm(1.0,vi[j][p][st][dc]);
                        	   l.addTerm(-1.0,vs[j-1][p][st][dc]);
                        	   l.addTerm(-1.0,rs[j][p][st][dc]);
                        	   model.addConstr(l,GRB.EQUAL,0.0,"VIConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                           }
                           else{
                               double d=0.0;
                        	   Triplet<String, String, String> t = new Triplet<String, String, String>(r.prdt.get(p), r.stores.get(st), r.DCs.get(dc));
                        	   GRBLinExpr l = new GRBLinExpr();
                        	   l.addTerm(1.0,vi[j][p][st][dc]);
                        	   l.addTerm(-1.0,rs[j][p][st][dc]);
                        	   if(r.store_beg_inventory.containsKey(t)) d = r.store_beg_inventory.get(t);
                        	   model.addConstr(l,GRB.EQUAL,d,"VIConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                           }
                          }
                        }
                    }
                }
            }
            
            System.out.println("Beginning Inventory constraints completed");

            // Sales(variable) bound constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                Map<Integer,Float> LadderMap= r.ladder.row(i);
                if(LadderMap.size()>0){
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                           
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                              
                                	
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(1.0, zv[j][p][st][dc]);
                                    l.addTerm(-1.0, x[j][p][st][dc]);
                                    model.addConstr(l, GRB.LESS_EQUAL, 0.0, "VariableSalesConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                   
                                  
                              
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Sales Variable Bound Constraints completed");
            
           
            
      // flag =false;   
        
            //if(flag){
          //SALES = minimum(Demand, Beginning Inventory at stores)  
         for (int i : r.period.keySet()) {
        	 Collection<String> pi=r.periodProduct.get(i);
         Map<Integer, Float>LadderMap =r.ladder.row(i);
        	//if((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
        		GRBLinExpr l5 =new GRBLinExpr();
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                	if(pi.contains(r.prdt.get(p))){
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                         
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                            	 // if(flag){                    
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(1.0, ts[j][p][st][dc]);
                                    l.addTerm(-1.0, vi[j][p][st][dc]);
                                    model.addConstr(l, GRB.LESS_EQUAL, 0.0, "Minimum Sales Inventory Const.1" + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                   
                                    
                                    GRBLinExpr l1 = new GRBLinExpr();
                                    l1.addTerm(1.0, ts[j][p][st][dc]);
                                    l1.addTerm(-1.0, td[j][p][st][dc]);
                                    model.addConstr(l1, GRB.LESS_EQUAL, 0.0, "Minimum Sales Inventory Const.2" + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));                             
                                   
                                    GRBLinExpr l2 = new GRBLinExpr();
                                    l2.addTerm(1.0, dtd[j][p][st][dc]);
                                    l2.addTerm(1.0, dvi[j][p][st][dc]);
                                    model.addConstr(l2, GRB.EQUAL, 1.0, "Minimum Sales Inventory Const.3" + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));                             
                            	// if(flag){ 
                                     GRBLinExpr l3 = new GRBLinExpr();
                                    l3.addTerm(1.0, ts[j][p][st][dc]);
                                    l3.addTerm(-1.0, td[j][p][st][dc]);
                                    l3.addTerm(-1.0*BigM, dtd[j][p][st][dc]);
                                    model.addConstr(l3, GRB.GREATER_EQUAL, (-1.0*BigM), "Minimum Sales Inventory Const.4" + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));                             
                            	// if(flag){ 
                                    GRBLinExpr l4 = new GRBLinExpr();
                                    l4.addTerm(1.0, ts[j][p][st][dc]);
                                    l4.addTerm(-1.0, vi[j][p][st][dc]);
                                    l4.addTerm(-1.0*BigM, dvi[j][p][st][dc]);
                                    model.addConstr(l4, GRB.GREATER_EQUAL, (-1.0*BigM), "Minimum Sales Inventory Const.5" + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));                          
                            	//  }
                                    
                                    l5.addTerm(1.0,ts[j][p][st][dc]);
                            
                             }
                            }
                        }
                    }
                }
                if((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
                    l5.addTerm(1.0,penalty[j]);
                    l5.addTerm(-1.0, relax[j]);
                    if(flag){
                    if(pass==0) model.addConstr(l5,GRB.EQUAL,r.FixedDemand_Calculator.get(i),"Penalty Constraint"+i);
                    else  model.addConstr(l5,GRB.EQUAL,Timewiseunitsales.get(i),"Penalty Constraint_I"+i);
            	     }
                    else model.addConstr(l5,GRB.EQUAL,r.FixedDemand_Calculator.get(i),"Penalty Constraint"+i);
                   }
            }
            System.out.println("Total Sales = Minimum(Beginning Inventory at Store,Demand) Constraints completed");
       //  }              
            // Fix Demand Slacks Constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                if ((r.bdf_fx.containsKey(qn)) && (r.bdf_fx.get(qn) > 0.0) && (r.bdf_var.containsKey(qn)) && (r.bdf_var.get(qn) > 0.0)) {
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(1.0, zf[j][p][st][dc]);
                                    l.addTerm(-BigM, bf[j][p][st][dc]);
                                    model.addConstr(l, GRB.LESS_EQUAL, 0.0, "FixDemandSlacksConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Demand Slacks Constraints completed");
            // Variable Demand Slacks Constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                if ((r.bdf_fx.containsKey(qn)) && (r.bdf_fx.get(qn) > 0.0) && (r.bdf_var.containsKey(qn)) && (r.bdf_var.get(qn) > 0.0)) {
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(1.0, zv[j][p][st][dc]);
                                    l.addTerm(-1.0*BigM, bv[j][p][st][dc]);
                                    model.addConstr(l, GRB.LESS_EQUAL, 0.0, "VariableDemandSlacksConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Variable Demand Slacks Constraints completed");
            // Fix Demand Dominating Constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                if ((r.bdf_fx.containsKey(qn)) && (r.bdf_fx.get(qn) > 0.0) && (r.bdf_var.containsKey(qn)) && (r.bdf_var.get(qn) > 0.0)) {
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(1.0, bf[j][p][st][dc]);
                                    l.addTerm(-1.0, bv[j][p][st][dc]);
                                    model.addConstr(l, GRB.GREATER_EQUAL, 0.0, "FixedDemandDominatingConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Demand Dominating Constraints completed");
            // Fix Demand Volume Dominating Constraints
            for (int i : r.period.keySet()) {
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                for (int p = 0; p < r.prdt.size(); p++) {
                    for (int st = 0; st < r.stores.size(); st++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                double d = 0.0;
                                if ((r.bdf_fx.containsKey(qn)) && (r.bdf_fx.get(qn) > 0.0) && (r.bdf_var.containsKey(qn)) && (r.bdf_var.get(qn) > 0.0)) {
                                    GRBLinExpr l = new GRBLinExpr();
                                    l.addTerm(BigM, bv[j][p][st][dc]);
                                    l.addTerm(-1.0, zf[j][p][st][dc]);
                                    if (r.bdf_fx.containsKey(qn))
                                        d = r.bdf_fx.get(qn);
                                    model.addConstr(l, GRB.LESS_EQUAL, (BigM - d), "FixedDemandVolumeDominatingConst." + "_" + i + "_" + r.prdt.get(p) + "_" + r.stores.get(st) + "_" + r.DCs.get(dc));
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Fix Demand Volume Dominating Constraints completed");

            // Slack constraints added
        
            for (int i : r.period.keySet()) {
            	Collection<String> pi=r.periodProduct.get(i);
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                if (j >= (r.period.size() - r.nslackConstraints)) {
                    int g = r.markdown.get(i);
                    GRBLinExpr l = new GRBLinExpr();
                    GRBLinExpr l1 =new GRBLinExpr();
                    for (int t = 0; t <= j; t++) {
                        int b = r.period_index.get(t);
                        int c = (r.markdown.get(b) == 0) ? (b - 1) : (nperiods - r.nslackConstraints + r.markdown.get(b));
                        for (int p = 0; p < r.prdt.size(); p++) {
                        	if(pi.contains(r.prdt.get(p))){
                            for (int st = 0; st < r.stores.size(); st++) {
                                for (int dc = 0; dc < r.DCs.size(); dc++) {
                                 
                                    if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                       
                                    	 l.addTerm(1.0, ts[c][p][st][dc]);
                                         l1.addTerm(1.0,ts[c][p][st][dc]);
                                    }
                                }
                            }
                          }
                        }
                    }
                    l.addTerm(r.Denominator_Item_Timewise.get(i), lo[g]);
                    l1.addTerm(-1.0*r.Denominator_Item_Timewise.get(i),su[g]);
                    if(LowerBounds.size() == 0){
                    model.addConstr(l, GRB.GREATER_EQUAL, (r.trgt_lb.get(i) * r.Denominator_Item_Timewise.get(i)) - r.periodwisesales.get(i), "LowerBound Constraints" + i);
                    model.addConstr(l1, GRB.LESS_EQUAL, (r.trgt_ub.get(i) * r.Denominator_Item_Timewise.get(i)) - r.periodwisesales.get(i), "UpperBound Constraints" + i);
                    }
                    else{
                    	System.out.println("Lower Bound for period "+i+": "+LowerBounds.get(i));
                    	System.out.println("Upper Bound for period "+i+": "+UpperBounds.get(i));
                    	model.addConstr(l, GRB.GREATER_EQUAL, (LowerBounds.get(i) * r.Denominator_Item_Timewise.get(i)) - r.periodwisesales.get(i), "LowerBound Constraints" + i);
                        model.addConstr(l1, GRB.LESS_EQUAL, (UpperBounds.get(i) * r.Denominator_Item_Timewise.get(i)) - r.periodwisesales.get(i), "UpperBound Constraints" + i);
                    }
                }
            }
        
            System.out.println("Sell Through Constraints completed");
           
            if(LowerBounds.size()>0){
         	   if(strict == 1){ 
                 for (int i = 0; i < r.nslackConstraints; i++){
                 	GRBLinExpr l1 = new GRBLinExpr();
                 	GRBLinExpr l2 = new GRBLinExpr();
                           l1.addTerm(1.0 , lo[i]);
                          l2.addTerm(1.0 ,  su[i]);
                          model.addConstr(l1, GRB.LESS_EQUAL, 0.0001, "IIPhase"+"LB"+i);
                          model.addConstr(l2, GRB.LESS_EQUAL, 0.0001, "IIPhase"+"UB"+i);                     
                         }
             }
         /*	 for (int i : r.period.keySet()) {
               Map<Integer, Float>LadderMap =r.ladder.row(i);
               	if((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0)){
               		GRBLinExpr l5 =new GRBLinExpr();
                       int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i)); 
                       l5.addTerm(1.0,penalty[j]);
                       model.addConstr(l5, GRB.LESS_EQUAL, 0.05, "IIPhase"+"Penalty"+i);
               	}
         	   }*/
            }
            model.update();
            model.write(OUT_DIR + "/"+r.run_id+"_"+pass+"PAMM_Model.lp");
           
         /*  if((flag) && (pass==1)){
            System.out.println("In reading previous solution block");
          	  model.read(OUT_DIR + "/" +r.run_id+".mst");
          	System.out.println("Completed reading previous solution");
          	model.update(); 
           // int error =GRBread(model, "/tmp/model.mst.bz2");
            }*/
            
            model.optimize();
 // Calculating Time wise Sales Value for Slack calculation
            

            for (int i : r.period.keySet()) {
            	Collection<String> pi =r.periodProduct.get(i);
            	float temp = r.periodwisesales.get(i);
            	float temp1=(float) 0.0;
            	float temp2=(float) 0.0;
            	float temp3= (float) 0.0;
                int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                
                for (int p = 0; p < r.prdt.size(); p++) {
                	if(pi.contains(r.prdt.get(p))){
                    for (int dc = 0; dc < r.DCs.size(); dc++) {

                        for (int st = 0; st < r.stores.size(); st++) {
                        	Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                        	if(r.bdf_fx.containsKey(qn))temp1=temp1+r.bdf_fx.get(qn);
                        	if(r.bdf_fx.containsKey(qn))temp2=temp2+r.bdf_var.get(qn);
                        	
                        	
                            if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))){
                            	temp3 = temp3 + Float.parseFloat(String.valueOf(ts[j][p][st][dc].get(GRB.DoubleAttr.X)));
                            	for(int f=0;f<=j;f++)
                                          temp = temp + Float.parseFloat(String.valueOf(ts[f][p][st][dc].get(GRB.DoubleAttr.X)));
                                        
                            }

                            } 
                        }
                    }
                }
                TimewiseSales.put(i, temp);
                TimewiseFixedDemand.put(i, temp1);
                TimewiseVariableDemand.put(i, temp2);
                Timewiseunitsales.put(i,temp3);
            }
           
            // Printing the objective value
            double obj = model.get(GRB.DoubleAttr.ObjVal);
            if(pass == 0){
            	try{
            	FileWriter writer = new FileWriter(OUT_DIR + "/"+r.run_id+"Phase_I_log.txt");
            	writer.append("The slack constraints summation after first pass would be" +obj);
            	writer.append("\n");
           
            	  	for (int i = 0; i < r.nslackConstraints; i++){
            	  		writer.append("Lower Bound Slack for period "+ r.period_index.get(nperiods-r.nslackConstraints+i)+":"+String.valueOf(lo[i].get(GRB.DoubleAttr.X)));
            	  		writer.append("\n");
            	  		writer.append("Upper Bound Slack for period "+ r.period_index.get(nperiods-r.nslackConstraints+i)+":"+String.valueOf(su[i].get(GRB.DoubleAttr.X)));
            	  		writer.append("\n");
            	  		float lower =r.trgt_lb.get(r.period_index.get(nperiods-r.nslackConstraints+i))-Float.parseFloat(String.valueOf(lo[i].get(GRB.DoubleAttr.X)));
            		float upper= r.trgt_ub.get(r.period_index.get(nperiods-r.nslackConstraints+i))+Float.parseFloat(String.valueOf(su[i].get(GRB.DoubleAttr.X)));
            		
            		LowerBounds.put(r.period_index.get(nperiods-r.nslackConstraints+i),lower);
            		UpperBounds.put(r.period_index.get(nperiods-r.nslackConstraints+i),upper);
            		writer.append("Lower Bound for Period "+r.period_index.get(nperiods-r.nslackConstraints+i)+":"+LowerBounds.get(r.period_index.get(nperiods-r.nslackConstraints+i)));
            		writer.append("\n");
            		writer.append("Upper Bound for Period "+r.period_index.get(nperiods-r.nslackConstraints+i)+":"+UpperBounds.get(r.period_index.get(nperiods-r.nslackConstraints+i)));
            		writer.append("\n");            	  	
            	  
            }
            writer.flush();
            writer.close();
            }catch (GRBException e) {
                    System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
                }
            }
            if(pass == 1){
            for (int i = 0; i < r.nslackConstraints; i++)
             obj = obj + bigM*Float.parseFloat(String.valueOf(lo[i].get(GRB.DoubleAttr.X)))+bigM*Float.parseFloat(String.valueOf(su[i].get(GRB.DoubleAttr.X)));
             //expr.addTerm(-1 *r.ttl_avail_inv * Collections.max(r.ladder.row(r.period_index.get(nperiods - r.nslackConstraints + i)).values()),  su[i]);
             //}
           // if(LowerBounds.size()==0){
           for(int i:r.period.keySet()){
            	 Map<Integer, Float>LadderMap =r.ladder.row(i);
             	if(((LadderMap.size()==0) ||(r.VariableDemand_Calculator.get(i) == 0.0))){
             		  int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
             		  if(j==0) obj = obj + 99999.0*Float.parseFloat(String.valueOf(penalty[j].get(GRB.DoubleAttr.X)));
             		  else obj = obj + 10000.0*Float.parseFloat(String.valueOf(penalty[j].get(GRB.DoubleAttr.X)));
             	}
            }
        //   }	            
            
            double Gap =model.get(GRB.DoubleAttr.MIPGap);
            System.out.println("Obj: " + obj);
            System.out.println("Gap: "+  Gap);

           

            // Printing Slack values

            for (int i = 0; i < r.nslackConstraints; i++) {

                System.out.println("Lower Bound Slack for period " + r.period_index.get(nperiods - r.nslackConstraints + i) + ": " + String.valueOf(lo[i].get(GRB.DoubleAttr.X)));
                System.out.println("Sell through for period  " + r.period_index.get(nperiods - r.nslackConstraints + i) + "\t"
                        + (TimewiseSales.get(r.period_index.get(nperiods - r.nslackConstraints + i)) / r.Denominator_Item_Timewise.get(r.period_index.get(nperiods - r.nslackConstraints + i))));
               System.out.println("Upper Bound Slack for period " + r.period_index.get(nperiods - r.nslackConstraints + i) + ": " + String.valueOf(su[i].get(GRB.DoubleAttr.X)));
            }
            // Printing out Time wise Sales figures
            for (int i : r.period.keySet())
                System.out.println("Time Period: " + i + "Sales to Date: " + TimewiseSales.get(i));

            // Writing Prices
            try {
                FileWriter writer = new FileWriter(OUT_DIR + "/"+r.run_id+"Price.csv");
                String FILE_HEADER = "Time Period,Store,Price";
                writer.append(FILE_HEADER.toString());
                writer.append("\n");
                for (int i : r.period.keySet()) {
                    Map<Integer, Float> LadderMap = r.ladder.row(i);
                    int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                    if (LadderMap.size() > 0) {
                        for (int st = 0; st < r.stores.size(); st++) {
                            writer.append(String.valueOf(i));
                            writer.append(",");
                            writer.append(r.stores.get(st));
                            writer.append(",");
                            writer.append(String.valueOf(pr[j][st].get(GRB.DoubleAttr.X)));
                            writer.append("\n");
                        }
                    }

                }
                writer.flush();
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            // Writing Sales,Demand,StoreReceipts,Ending InventoryStores...
            try {
                FileWriter writer = new FileWriter(OUT_DIR + "/"+r.run_id+"StoreDCvariables.csv");
                String FILE_HEADER = "Time Period,Product,Stores,DC,Fixed Sales,Variable Sales,Total Sales,Demand,Store Receipts,Ending Inventory at Store,Beginning Inventory";
                writer.append(FILE_HEADER.toString());
                writer.append("\n");
                for (int i : r.period.keySet()) {
                	Collection<String> pi = r.periodProduct.get(i);
                    int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                    for (int p = 0; p < r.prdt.size(); p++) {
                    	if(pi.contains(r.prdt.get(p))){
                        for (int st = 0; st < r.stores.size(); st++) {
                            for (int dc = 0; dc < r.DCs.size(); dc++) {
                                Quartet<Integer, String, String, String> qn = new Quartet<Integer, String, String, String>(i, r.prdt.get(p), r.DCs.get(dc), r.stores.get(st));
                                if (r.storeDC.get(r.stores.get(st)).contains(r.DCs.get(dc))) {
                                    writer.append(String.valueOf(i));
                                    writer.append(",");
                                    writer.append(r.prdt.get(p));
                                    writer.append(",");
                                    writer.append(r.stores.get(st));
                                    writer.append(",");
                                    writer.append(r.DCs.get(dc));
                                    writer.append(",");
                                    if (r.bdf_fx.containsKey(qn) && r.bdf_fx.get(qn) > 0.0)
                                        writer.append(String.valueOf(zf[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    else
                                        writer.append((char) 0);
                                    writer.append(",");
                                    if (r.bdf_var.containsKey(qn) && r.bdf_var.get(qn) > 0.0)
                                        writer.append(String.valueOf(zv[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    else
                                        writer.append((char) 0);
                                    writer.append(",");
                                    if ((r.bdf_var.containsKey(qn)) && (r.bdf_fx.containsKey(qn)))
                                        writer.append(String.valueOf(ts[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    else
                                        writer.append((char) 0);
                                    writer.append(",");
                                    if ((r.bdf_var.containsKey(qn)))
                                        writer.append(String.valueOf(td[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    else
                                        writer.append((char) 0.0);
                                    writer.append(",");
                                    writer.append(String.valueOf(rs[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    writer.append(",");
                                    writer.append(String.valueOf(vs[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    writer.append(",");
                                    writer.append(String.valueOf(vi[j][p][st][dc].get(GRB.DoubleAttr.X)));
                                    writer.append("\n");
                                }

                            }
                        }
                    	}
                    	}
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            // Ending DC Inventory Writing
            try {
                FileWriter writer = new FileWriter(OUT_DIR + "/"+r.run_id+"DCInventory.csv");
                String FILE_HEADER = "Time Period,Product,DC,Ending Inventory of DC";
                writer.append(FILE_HEADER.toString());
                writer.append("\n");
                for (int i : r.period.keySet()) {
                    int j = (r.markdown.get(i) == 0) ? (i - 1) : (nperiods - r.nslackConstraints + r.markdown.get(i));
                    for (int p = 0; p < r.prdt.size(); p++) {
                        for (int dc = 0; dc < r.DCs.size(); dc++) {
                            writer.append(String.valueOf(i));
                            writer.append(",");
                            writer.append(r.prdt.get(p));
                            writer.append(",");
                            writer.append(r.DCs.get(dc));
                            writer.append(",");
                            writer.append(String.valueOf(vdc[j][p][dc].get(GRB.DoubleAttr.X)));
                            writer.append(",");
                            writer.append("\n");
                        }

                    }

                }
                writer.flush();
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
            
          // Writing Summary sheet for every run_id  
            long estimatedTime = System.currentTimeMillis() - startTime;
            try {
                FileWriter writer = new FileWriter(OUT_DIR + "/SummarySheet.csv",true);
                String FILE_HEADER = "PGM_GRP_ID_S,	period,	Total Fixed Demand,	Total Var Demand,	Avail Inv,	Sales to Date,	units,	sell_thru,	min_sthru,	max_sthru,	OBJ_VALUE,	Gap, Time(in seconds)";
                if(header ==0){
                writer.append(FILE_HEADER.toString());
                writer.append("\n");
                header++;
                }
                int objective =0;
                for (int i : r.period.keySet()) {
                	Map<Integer, Float> LadderMap = r.ladder.row(i);
                    
                	writer.append(String.valueOf(r.run_id));
                    writer.append(",");
                    writer.append(String.valueOf(i));
                    writer.append(",");
                    writer.append(String.valueOf(TimewiseFixedDemand.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(TimewiseVariableDemand.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(r.Denominator_Item_Timewise.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(TimewiseSales.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(Timewiseunitsales.get(i)));
                    writer.append(",");
                    writer.append(String.valueOf(df.format(TimewiseSales.get(i)/r.Denominator_Item_Timewise.get(i))));
                    writer.append(",");
                    if (LadderMap.size() > 0) {
                    	writer.append(String.valueOf(df.format(r.trgt_lb.get(i))));
                        writer.append(",");
                    }
                    else
                    {
                    	writer.append(" ");
                        writer.append(",");
                    }
                    if (LadderMap.size() > 0) {
                    	writer.append(String.valueOf(df.format(r.trgt_ub.get(i))));
                        writer.append(",");
                    }
                    else
                    {
                    	writer.append(" ");
                        writer.append(",");
                    }
                    
                    if(objective ==0){
                    	writer.append(String.valueOf(obj));
                    	writer.append(",");
                    	writer.append(String.valueOf(df.format(Gap)));
                    	writer.append(",");
                    	writer.append(String.valueOf(estimatedTime/1000));
                    	writer.append(",");
                    	objective++;
                    }
                   writer.append("\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {

                e.printStackTrace();
            } 
              
            }
            
            model.dispose();
            env.dispose();
            
        } catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
        if(pass ==1)System.out.println("The process ended");
        pass++;
        
    }
   }
}