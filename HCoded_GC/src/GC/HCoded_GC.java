package GC;
import org.jacop.core.*;
import org.jacop.constraints.*;
import org.jacop.search.*;

public class HCoded_GC {
static HCoded_GC m = new HCoded_GC ();
public static void main (String[] args) {
Store store = new Store(); // define FD store
int size = 4;
// define finite domain variables
IntVar[] v = new IntVar[size];
for (int i=0; i<size; i++)
v[i] = new IntVar(store, "v"+i, 1, size);
// define constraints
store.impose( new XneqY(v[0], v[1]) );
store.impose( new XneqY(v[0], v[2]) );
store.impose( new XneqY(v[1], v[2]) );
store.impose( new XneqY(v[1], v[3]) );
store.impose( new XneqY(v[2], v[3]) );
// search for a solution and print results
Search<IntVar> search = new DepthFirstSearch<IntVar>();
SelectChoicePoint<IntVar> select =
new InputOrderSelect<IntVar>(store, v,
new IndomainMin<IntVar>());
boolean result = search.labeling(store, select);
if ( result )
System.out.println("Solution: " + v[0]+", "+v[1] +", "+
v[2] +", "+v[3]);
else
System.out.println("*** No");
}
}