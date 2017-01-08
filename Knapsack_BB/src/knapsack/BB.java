package knapsack;

import java.util.ArrayList;

public class BB {
 Reader r;
 static ArrayList<Integer> final_choice=new ArrayList<Integer>();
 static float solution=(float) -0.1;
  public BB(Reader r){
	  this.r=r;
  }
  public void Branch_Bound(ArrayList<Integer> v){
	  if(Weight_Cal(v) > r.knapsize){
		  System.out.println("Weight exceeded--infeasible solution");
		  return;
	  }
	  else if(LR_Bound(v) <= solution){
		  System.out.println("Not profitable--infeasible solution");
		  return;
	  }
	  else if((v.size()==r.nitems)&&(Weight_Cal(v) <=r.knapsize)){
		  solution= Value_Cal(v);
		  final_choice=v;
		  System.out.println("Feasible Solution found");
		  return;
	  }
	  else {
		  ArrayList<Integer> t1= new ArrayList<Integer>();
		  t1.addAll(v);
		  ArrayList<Integer> t2= new ArrayList<Integer>();
		  t2.addAll(v);
		  t1.add(0);
		  t2.add(1);
		  Branch_Bound(t1);
		  Branch_Bound(t2);
		  return;
		  }
  }
  public float LR_Bound (ArrayList<Integer> v){
	  float wt=r.knapsize-Weight_Cal(v);
	  float ubound=Value_Cal(v);
	    for(int i=v.size();i<r.Items.size();i++){
			  if(wt >= r.Items.get(i).getWeight()){
				  wt=wt-r.Items.get(i).getWeight();
				  ubound=ubound+r.Items.get(i).getValue();
			  }
			  else{
				  ubound =ubound + ((wt/((float)r.Items.get(i).getWeight()))*(float)(r.Items.get(i).getValue()));
				  wt=(float) 0.0;
				 }
			  if(wt==(float)0.0) break;
		  }
	  return ubound;
  }
  
 public float Weight_Cal(ArrayList<Integer> v){
	 float weight=(float) 0.0;
	 for(int i=0;i<v.size();i++){
		 if(v.get(i)!=0) weight=weight+ (float)r.Items.get(i).getWeight();
	 }
	return weight;
 }
 public float Value_Cal(ArrayList<Integer> v){
	 float val=(float) 0.0;
	 for(int i=0;i<v.size();i++){
		 if(v.get(i)!=0) val=val+ (float)r.Items.get(i).getValue();
	 }
	return val;
 }
 
 public void Write_Results(){
	System.out.println("Knapsack content: "+solution); 
	for(int i=0;i<final_choice.size();i++)
		System.out.print(final_choice.get(i)+"\t");
	}
}