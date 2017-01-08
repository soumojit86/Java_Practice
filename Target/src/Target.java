import java.util.Arrays;
import java.util.List;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class Target {
  public static void main(String[] args) {
    try {
      GRBEnv    env   = new GRBEnv("target.log");
      GRBModel  model = new GRBModel(env);
      int nproducts=5, nstorages=6;
      double[] sales_acc = {1.5,0.5,1.0,1.0,0.5,1.5};
      double[] profit_margin={1.1,1.5,5.0,5.0,1.0};
      double[] minimum_order={100.0,50.0,100.0,100.0,40.0};
      double[] maximum_order={1000.0,300.0,1000.0,1000.0,1000.0};
      List<Integer> Ref_Str = Arrays.asList(0,2,4);
      List<Integer> Nor_Str = Arrays.asList(1,3,5);
      List<Integer> Ref_Prd = Arrays.asList(1,4);
      List<Integer> Nor_Prd = Arrays.asList(0,2,3);
      double[] maximum_capacity={100.0,1000.0,100.0,1000.0,100.0,1000.0};
      // Create variables
      GRBVar[][]av = new GRBVar[nproducts][nstorages];
      for (int i=0;i<nproducts;i++) {
           for (int j=0;j<nstorages;j++) 
        	   av[i][j] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "AV" + "_" + i+"_" + j);
    
          }
      GRBVar[][]sa = new GRBVar[nproducts][nstorages];
      for (int i=0;i<nproducts;i++) {
          for (int j=0;j<nstorages;j++) 
       	   sa[i][j] = model.addVar(0.0, GRB.INFINITY, 0.0, GRB.CONTINUOUS, "SA" + "_" + i+"_" + j);
   
         }
      // Integrate new variables

      model.update();

      // Set objective: maximize Expected Sales

      GRBLinExpr expr = new GRBLinExpr();
      for (int i=0;i<nproducts;i++) {
          for (int j=0;j<nstorages;j++) {
             expr.addTerm((profit_margin[i]*sales_acc[j]), sa[i][j]); 
          }
      }
      model.setObjective(expr, GRB.MAXIMIZE);
    model.update();
      // Add constraint: Sales is less than availability over all storage facilities
      for (int i=0;i<nproducts;i++) {
          for (int j=0;j<nstorages;j++) {
                     expr = new GRBLinExpr();
                    expr.addTerm(1.0, sa[i][j]); expr.addTerm(-1.0, av[i][j]); 
                  model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "c0"+"_"+i+"_"+j);
          }
      }
      // Add constraint: maximum and minimum availability
      for (int i=0;i<nproducts;i++) {
                 expr = new GRBLinExpr();
               for (int j=0;j<nstorages;j++)
             expr.addTerm(1.0, av[i][j]); 
            model.addConstr(expr, GRB.GREATER_EQUAL, minimum_order[i], "c2_min"+"_"+i);
           model.addConstr(expr, GRB.LESS_EQUAL, maximum_order[i], "c2_max"+"_"+i);
      }
      
   // Add constraint: Freezing facilities
      for (int i=0;i<nproducts;i++) {
          for (int j=0;j<nstorages;j++) {
        	  if((Ref_Prd.contains(i))&&(Nor_Str.contains(j))){
                     expr = new GRBLinExpr();
                    expr.addTerm(1.0, av[i][j]);
                  model.addConstr(expr, GRB.EQUAL, 0.0, "c3"+"_"+i+"_"+j);
        	  }
        	  else if((Nor_Prd.contains(i))&&(Ref_Str.contains(j))){
        		  expr = new GRBLinExpr();
                  expr.addTerm(1.0, av[i][j]);
                model.addConstr(expr, GRB.EQUAL, 0.0, "c3"+"_"+i+"_"+j);
        	  }
          }
      }
      
      // Add constraint: maximum capacity
      for (int j=0;j<nstorages;j++) {
                 expr = new GRBLinExpr();
               for (int i=0;i<nproducts;i++)
                  expr.addTerm(1.0, av[i][j]); 
              model.addConstr(expr, GRB.LESS_EQUAL, maximum_capacity[j], "c4_max"+"_"+j);
      }
      model.update();
      // Optimize model
     
      model.optimize();
      for (int i=0;i<nproducts;i++) {
          for (int j=0;j<nstorages;j++) {
      System.out.println("Availability"+"_"+"Product "+i+ "Storage "+j+":"+av[i][j].get(GRB.DoubleAttr.X));
      System.out.println("Sales"+"_"+"Product "+i+ "Storage "+j+":"+sa[i][j].get(GRB.DoubleAttr.X));
          }
      }

      System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

      // Dispose of model and environment

      model.dispose();
      env.dispose();

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " +
                         e.getMessage());
    }
  }
}
