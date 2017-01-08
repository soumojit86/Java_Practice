package knapsack;

public class Item {
	int id, value, weight;
	float ratio;
    public Item(int x, int y) {
        this.value = x;
        this.weight = y;
        this.ratio= (float)((float)x/(float)y);
    }
    public void setId(int i){this.id=i;}
    public Integer getValue(){ return value; }
    public Integer getWeight(){ return weight; }
    public Float getRatio(){ return ratio; }
}
