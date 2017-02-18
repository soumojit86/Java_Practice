package knapsack;

/**  
 * @Soumojit
 */

public class Item {
	int id;
	float value, weight,ratio;
    public Item(float x, float y) {
        this.value = x;
        this.weight = y;
        this.ratio= (float)((float)x/(float)y);
    }
    public void setId(int i){this.id=i;}
    public int getId(){ return id;}
    public Float getValue(){ return value; }
    public Float getWeight(){ return weight; }
    public Float getRatio(){ return ratio; }
}
