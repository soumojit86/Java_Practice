package knapsack;

public class Greedy {
	Reader r;
	public Greedy(Reader r){
		this.r=r;
	}
	
	
 public int[] Selector(){
	  int [] greedy = new int[r.nitems];
	  float wt=(float) 0.0;
	  for(int i=0;i<r.nitems;i++){
		  wt=wt+r.Items.get(i).getWeight();
		  if(wt<r.knapsize) greedy[i]=1;
		  else break;
		  }
	  return greedy;
  }
 public int[] Selector_i(int i){
	 int [] greedy = new int[r.nitems];
	 float wt=r.Items.get(i).getWeight();
	 greedy[i]=1;
	 for(int j=0;j<r.nitems;j++){
		  if(j==i)continue;
		  wt=wt+r.Items.get(j).getWeight();
		  if(wt<r.knapsize) greedy[i]=1;
		  else break;
		  }
	 return greedy;
	 
 }
 public String Array_String_Conv (int [] A){
		StringBuilder builder = new StringBuilder();
		for (int i : A) {
		  builder.append(i);
		}
		String text = builder.toString();
		return text;
	}
}