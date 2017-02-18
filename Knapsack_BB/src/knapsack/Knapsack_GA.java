package knapsack;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**  
 * @Soumojit
 */


import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;

public class Knapsack_GA {
    private Reader r;
    private boolean mutation = false;
    private int crossover_count = 0;
    private int clone_count = 0;
    private int number_of_items = 0;
    private int population_size = 0;
    private int maximum_generations = 0;
    private int generation_counter = 1;
    private float knapsack_capacity = 0;
    private float prob_crossover = 0;
    private float prob_mutation = 0;
    private float total_fitness_of_generation = 0;
    private ArrayList<Float> value_of_items = new ArrayList<Float>();
    private ArrayList<Float> weight_of_items = new ArrayList<Float>();
    private ArrayList<Float> fitness = new ArrayList<Float>();
    private ArrayList<Float> best_fitness_of_generation = new ArrayList<Float>();
    ArrayList<Float> mean_fitness_of_generation = new ArrayList<Float>();
    private ArrayList<String> population = new ArrayList<String>();
    private ArrayList<String> breed_population = new ArrayList<String>();
    private ArrayList<String> best_solution_of_generation = new ArrayList<String>();



    /**  
     * Default constructor
     */
    public Knapsack_GA(Reader r) {
         this.r =r;  
    }

  public void Work(){
	  // Get user input
      this.getInput();

      // Make first generation
      this.buildKnapsackProblem();

      // Output summary
      this.showOptimalList();

  }
    /**
     * Controls knapsack problem logic and creates first generation
     */
    public void buildKnapsackProblem() {

        // Generate initial random population (first generation)
        this.makePopulation();

        // Start printing out summary
        System.out.println("\nInitial Generation:");
        System.out.println("===================");
        System.out.println("Population:");
        for(int i = 0; i < this.population_size; i++) {
            System.out.println((i + 1) + " - " + this.population.get(i));
        }

        // Evaluate fitness of initial population members
        this.evalPopulation();

        // Output fitness summary
        System.out.println("\nFitness:");
        for(int i = 0; i < this.population_size; i++) {
            System.out.println((i + 1) + " - " + this.fitness.get(i));
        }

        // Find best solution of generation
        this.best_solution_of_generation.add(this.population.get(this.getBestSolution()));

        // Output best solution of generation
        System.out.println("\nBest solution of initial generation: " + this.best_solution_of_generation.get(0));

        // Find mean solution of generation
	this.mean_fitness_of_generation.add(this.getMeanFitness());

	// Output mean solution of generation
	System.out.println("Mean fitness of initial generation: " + this.mean_fitness_of_generation.get(0));

        // Compute fitness of best solution of generation
        this.best_fitness_of_generation.add(this.evalGene(this.population.get(this.getBestSolution())));

        // Output best fitness of generation
        System.out.println("Fitness score of best solution of initial generation: " + this.best_fitness_of_generation.get(0));

        // If maximum_generations > 1, breed further generations
        if(this.maximum_generations > 1) {
            makeFurtherGenerations();
        }

    }


    /**
     * Makes further generations beyond first, if necessary
     */
    private void makeFurtherGenerations() {

        // Breeding loops maximum_generation number of times at most
        for(int i = 1; i < this.maximum_generations; i++) {

	    // Check for stopping criterion
	    if((this.maximum_generations > 4) && (i > 4)) {

		// Previous 3 generational fitness values
		float a = this.mean_fitness_of_generation.get(i - 1);
		float b = this.mean_fitness_of_generation.get(i - 2);
		float c = this.mean_fitness_of_generation.get(i - 3);

		// If all are 3 equal, stop
		if(a == b && b == c) {
		    System.out.println("\nStop criterion met");
		    maximum_generations = i;
		    break;
		}
	    }

            // Reset some counters
            this.crossover_count = 0;
            this.clone_count = 0;
            this.mutation = false;
          
            // Breed population
            for(int j = 0; j < this.population_size / 2; j++) {
                this.breedPopulation();
            }
   
            // Clear fitness values of previous generation
            this.fitness.clear();

            // Evaluate fitness of breed population members
            this.evalBreedPopulation();

            // Copy breed_population to population
            for(int k = 0; k < this.population_size; k++) {
                this.population.set(k, this.breed_population.get(k));
            }

            // Output population
            System.out.println("\nGeneration " + (i + 1) + ":");
            if((i + 1) < 10) {
                System.out.println("=============");
            }
            if((i + 1) >= 10) {
                System.out.println("==============");
            }
            if((i + 1) >= 100) {
                System.out.println("===============");
            }
            System.out.println("Population:");
            for(int l = 0; l < this.population_size; l++) {
                System.out.println((l + 1) + " - " + this.population.get(l));
            }

            // Output fitness summary
            System.out.println("\nFitness:");
            for(int m = 0; m < this.population_size; m++) {
               System.out.println((m + 1) + " - " + this.fitness.get(m));
            } 

            // Clear breed_population
            this.breed_population.clear();

            // Find best solution of generation
            this.best_solution_of_generation.add(this.population.get(this.getBestSolution()));

            // Output best solution of generation
            System.out.println("\nBest solution of generation " + (i + 1) + ": " + this.best_solution_of_generation.get(i));

            // Find mean solution of generation
	    this.mean_fitness_of_generation.add(this.getMeanFitness());

	    // Output mean solution of generation
	    System.out.println("Mean fitness of generation: " + this.mean_fitness_of_generation.get(i));

            // Compute fitness of best solution of generation
            this.best_fitness_of_generation.add(this.evalGene(this.population.get(this.getBestSolution())));

            // Output best fitness of generation
            System.out.println("Fitness score of best solution of generation " + (i + 1) + ": " + this.best_fitness_of_generation.get(i));

            // Output crossover/cloning summary
            System.out.println("Crossover occurred " + this.crossover_count + " times");
            System.out.println("Cloning occurred " + this.clone_count + " times");
            if(this.mutation == false) {
                System.out.println("Mutation did not occur");
            }
            if(this.mutation == true) {
                System.out.println("Mutation did occur");
            }
        }
    }

    void stopCriterion() {
      Plot graph = new Plot(mean_fitness_of_generation,"Mean Fitness by Generation");
      graph.Draw();
    }


    /**
     * Output KnapsackProblem summary
     */
    private void showOptimalList() {
    	 try {
             File file_name = new File(Knapsack_BB.base+"/output/output_"+r.nitems+"_"+r.knapsize+".txt");
             if(file_name.exists()) {
                 file_name.delete();
             }
             FileOutputStream fos = new FileOutputStream(file_name, true);
             PrintStream ps = new PrintStream(fos);
             System.setOut(ps);
         }
         catch(FileNotFoundException e) {
             System.out.println("Problem with output file");
         }
        // Output optimal list of items
        System.out.println("\nOptimal list of items to include in knapsack by Genetic Algorithm Approach: ");

        float best_fitness = 0;
        int best_gen = 0;

        // First, find best solution out of generational bests
        for(int z = 0; z < this.maximum_generations - 1; z++) {
            if(this.best_fitness_of_generation.get(z) > best_fitness) {
                best_fitness = this.best_fitness_of_generation.get(z);
                best_gen = z;
            }
        }

        // Then, go through that's generation's best solution and output items
        String optimal_list = this.best_solution_of_generation.get(best_gen);
        float val =(float) 0.0;
        float wt =(float) 0.0;
        for(int y = 0; y < this.number_of_items; y++) {
            if(optimal_list.substring(y, y + 1).equals("1")) {
            	val=val+r.Items.get(y).getValue();
            	wt=wt+r.Items.get(y).getWeight();
              //  System.out.print((y + 1) + " ");
                System.out.print("\t"+r.Items.get(y).getId());
            }
        }
      System.out.println("\nWeight incurred: "+wt);
      System.out.println("Value gained: "+val);
      System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }


    /**
     * Breeds current population to create a new generation's population
     */
    private void breedPopulation() {

        // 2 genes for breeding
        int gene_1;
        int gene_2;

        // Increase generation_counter
        generation_counter = generation_counter + 1;

        // If population_size is odd #, use elitism to clone best solution of previous generation
        if(population_size % 2 == 1) {
            breed_population.add(best_solution_of_generation.get(generation_counter - 1));
        }

        // Get positions of pair of genes for breeding
        gene_1 = selectGene();
        gene_2 = selectGene();
        
        // Crossover or cloning
        crossoverGenes(gene_1, gene_2);

    }


    /**
     * Performs mutation, if necessary
     */
    private void mutateGene() {
       
        // Decide if mutation is to be used
        float rand_mutation = (float) Math.random();
        if(rand_mutation <= prob_mutation) {

            // If so, perform mutation
            mutation = true;
            String mut_gene;
            String new_mut_gene;
            Random generator = new Random();
            int mut_point = 0;
            float which_gene = (float) (Math.random() * 100);

	    // Mutate gene
            if(which_gene <= 50) {
                mut_gene = breed_population.get(breed_population.size() - 1);
                mut_point = generator.nextInt(number_of_items);
                if(mut_gene.substring(mut_point, mut_point + 1).equals("1")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
                if(mut_gene.substring(mut_point, mut_point + 1).equals("0")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
            }
            if(which_gene > 50) {
                mut_gene = breed_population.get(breed_population.size() - 2);
                mut_point = generator.nextInt(number_of_items);
                if(mut_gene.substring(mut_point, mut_point + 1).equals("1")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "0" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 1, new_mut_gene);
                }
                if(mut_gene.substring(mut_point, mut_point + 1).equals("0")) {
                    new_mut_gene = mut_gene.substring(0, mut_point) + "1" + mut_gene.substring(mut_point);
                    breed_population.set(breed_population.size() - 2, new_mut_gene);
                }
            }           
        }
    }


    /**
     * Selects a gene for breeding
     * @return int - position of gene in population ArrayList to use for breeding
     */
    private int selectGene() {

        // Generate random number between 0 and total_fitness_of_generation
        float rand = (float) (Math.random() * total_fitness_of_generation);
        
        // Use random number to select gene based on fitness level
        for(int i = 0; i < population_size; i++) {
            if(rand <= fitness.get(i)) {
                return i;
            }
            rand = rand - fitness.get(i);
        }

	// Not reachable; default return value
	return 0;
    }


    /**
     * Performs either crossover or cloning
     */
    private void crossoverGenes(int gene_1, int gene_2) {
      
        // Strings to hold new genes
        String new_gene_1;
        String new_gene_2;

        // Decide if crossover is to be used
        float rand_crossover = (float) Math.random();
        if(rand_crossover <= prob_crossover) {
            // Perform crossover
            crossover_count = crossover_count + 1;
            Random generator = new Random(); 
            int cross_point = generator.nextInt(number_of_items) + 1;

            // Cross genes at random spot in strings
            new_gene_1 = population.get(gene_1).substring(0, cross_point) + population.get(gene_2).substring(cross_point);
            new_gene_2 = population.get(gene_2).substring(0, cross_point) + population.get(gene_1).substring(cross_point);

            // Add new genes to breed_population
            breed_population.add(new_gene_1);
            breed_population.add(new_gene_2);
        }
        else {
            // Otherwise, perform cloning
            clone_count = clone_count + 1;
            breed_population.add(population.get(gene_1));
            breed_population.add(population.get(gene_2));
        }

        // Check if mutation is to be performed
        mutateGene();
    }


    /**
     * Gets best solution in population
     * @return int - position of best solution in population
     */
    private int getBestSolution() {
        int best_position = 0;
        float this_fitness = 0;
        float best_fitness = 0;
        for(int i = 0; i < population_size; i++) {
            this_fitness = evalGene(population.get(i));
            if(this_fitness > best_fitness) {
                best_fitness = this_fitness;
                best_position = i;
            }
        }
        return best_position;
    }


    /**
     * Gets mean fitness of generation
     */
    private float getMeanFitness() {
        float total_fitness = 0;
   	float mean_fitness = 0;
        for(int i = 0; i < population_size; i++) {
	    total_fitness = total_fitness + fitness.get(i);
        }
	mean_fitness = total_fitness / population_size;
	return mean_fitness;
    }


    /**
     * Evaluates entire population's fitness, by filling fitness ArrayList
     * with fitness value of each corresponding population member gene
     */
    private void evalPopulation() {       
        total_fitness_of_generation = 0;
        for(int i = 0; i < population_size; i++) {
            float temp_fitness = evalGene(population.get(i));
            fitness.add(temp_fitness);
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness;
        }
    }


    /**
     * Evaluates entire breed_population's fitness, by filling breed_fitness ArrayList
     * with fitness value of each corresponding breed_population member gene
     */
    private void evalBreedPopulation() {
        total_fitness_of_generation = 0;
        for(int i = 0; i < population_size; i++) {
            float temp_fitness = evalGene(breed_population.get(i));
            fitness.add(temp_fitness);
            total_fitness_of_generation = total_fitness_of_generation + temp_fitness;
        }
    }


    /**
     * Evaluates a single gene's fitness, by calculating the total_weight
     * of items selected by the gene
     * @return float - gene's total fitness value
     */
    private float evalGene(String gene) {
        float total_weight = 0;
        float total_value = 0;
        float fitness_value = 0;
        float difference = 0;
        char c = '0';

        // Get total_weight associated with items selected by this gene
        for(int j = 0; j < number_of_items; j ++) {
            c = gene.charAt(j);
            // If chromosome is a '1', add corresponding item position's
            // weight to total weight
            if(c == '1') {
                total_weight = total_weight + weight_of_items.get(j);
                total_value = total_value + value_of_items.get(j);
            }
        }
        // Check if gene's total weight is less than knapsack capacity
        difference = knapsack_capacity - total_weight;
        if(difference >= 0) {
            // This is acceptable; calculate a fitness_value
            // Otherwise, fitness_value remains 0 (default), since knapsack
            // cannot hold all items selected by gene
            // Fitness value is simply total value of acceptable permutation,
            // and for unacceptable permutation is set to '0'
            fitness_value = total_value;
        }
        
        // Return fitness value
        return fitness_value;
    }


    /**  
     * Makes a population by filling population ArrayList with strings of
     * length number_of_items, each element a gene of randomly generated
     * chromosomes (1s and 0s)
     */
    private void makePopulation() {
    	Greedy g= new Greedy(r);
    	population.add(g.Array_String_Conv(g.Selector()));
    	int sum= IntStream.of(g.Selector()).sum();
    	for(int k=sum;k<r.nitems;k++)
        	population.add(g.Array_String_Conv(g.Selector_i(k)));
    	int idx= population.size();
        for(int i = idx; i < population_size; i++) 
        		population.add(makeGene()); 
    }


    /**  
     * Generates a single gene, a random String of 1s and 0s
     * @return String - a randomly generated gene
     */
    private String makeGene() {

    
    	
       // Stringbuilder builds gene, one chromosome (1 or 0) at a time
        StringBuilder gene = new StringBuilder(number_of_items);

        // Each chromosome
        char c;

        // Loop creating gene
        for(int i = 0; i < number_of_items; i++) {
            c = '0';
            float rnd = (float) Math.random(); 
            // If random number is greater than 0.5, chromosome is '1'
            // If random number is less than 0.5, chromosome is '0'
            if(rnd > 0.5) {
                c = '1';
            }
            // Append chromosome to gene
            gene.append(c);
        }
        // Stringbuilder object to string; return
        return gene.toString();
        
    }


    /**  
     * Collects user input to be used as parameters for knapsack problem
     */
    private void getInput() {
    	number_of_items=r.nitems;
    	knapsack_capacity=r.knapsize;
    	for(int i=0;i<r.Items.size();i++){
    		value_of_items.add(r.Items.get(i).getValue());
    		weight_of_items.add(r.Items.get(i).getWeight());
    	}
    	 population_size=2*r.nitems;
    	 maximum_generations=25;
    	 prob_crossover=(float) 0.8;
    	 prob_mutation=(float) 0.5;
      
    }


}
