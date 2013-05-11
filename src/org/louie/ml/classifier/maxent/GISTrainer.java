package org.louie.ml.classifier.maxent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.louie.ml.classifier.model.Dataset;
import org.louie.ml.classifier.model.Event;
import org.louie.ml.classifier.model.MutableContext;
import org.louie.ml.classifier.model.Prior;
import org.louie.ml.classifier.model.UniformPrior;

/**
 * This class implements the algorithm of Generalized Iterative Scaling.
 * 
 * @author Younggue Bae
 */
public class GISTrainer {

	private final boolean printMessages;
	
	public GISTrainer() {
		printMessages = false;
	}
	
	public GISTrainer(boolean printMessages) {
		this.printMessages = printMessages;
	}
	
  /**
   * Train a model using the GIS algorithm.
   *
   * @param iterations  The number of GIS iterations to perform.
   * @param dataset The data set used to compress events in memory.
   * @param prior The prior distribution used to train this model.
   * @return The newly trained model, which can be used immediately or saved
   *         to disk using an opennlp.maxent.io.GISModelWriter object.
   */
  public GISModel trainModel(int iterations, Dataset dataset, Prior prior) {
  	
  	/** Records the array of predicates seen in each event. */
  	int[][] contexts = dataset.getContexts();
  	
  	/** The number of times a predicate occurred in the training data. */
    int[] predicateCounts = dataset.getPredCounts();
    
    /** Records the number of times an event has been seen for each event i, in context[i]. */
    int[] numTimesEventsSeen = dataset.getNumTimesEventsSeen();
    
    /** Number of unique events which occurred in the event set. */
    int numUniqueEvents = contexts.length;
    
    /**
     * Stores the String names of the outcomes. The GIS only tracks outcomes as
     * ints, and so this array is needed to save the model to disk and thereby
     * allow users to know what the outcome was in human understandable terms.
     */
    String[] outcomeLabels = dataset.getOutcomeLabels();
    
    /** List of outcomes for each event i, in context[i]. */
    int[] outcomes = dataset.getOutcomes();
    
    /** Number of outcomes. */
    int numOutcomes = outcomeLabels.length;
    
    /**
     * Stores the String names of the predicates. The GIS only tracks predicates
     * as ints, and so this array is needed to save the model to disk and thereby
     * allow users to know what the outcome was in human understandable terms.
     */
    String[] predLabels = dataset.getPredLabels();
    
    /** This is the prior distribution that the model uses for training. */
    prior.setLabels(outcomeLabels, predLabels);
    
    /** Number of predicates. */
    int numPreds = predLabels.length;
    
    // correction constant
   	double correctionConstant = 0;
  	for (int ei = 0; ei < contexts.length; ei++) {
			if (contexts[ei].length > correctionConstant) {
				correctionConstant = contexts[ei].length;
			}
  	}

    display("\tNumber of Events: " + numUniqueEvents + "\n");
    display("\t    Number of Outcomes: " + numOutcomes + "\n");
    display("\t  Number of Predicates: " + numPreds + "\n");
 
    // set up feature arrays
    float[][] predCount = new float[numPreds][numOutcomes];
    for (int ei = 0; ei < numUniqueEvents; ei++) {
      for (int j = 0; j < contexts[ei].length; j++) {        
         predCount[contexts[ei][j]][outcomes[ei]] += numTimesEventsSeen[ei];
      }
    }
    
    // calculate expectation probabilities by each predicate and outcome
//    double[][] expectation = new double[numPreds][numOutcomes];
//    for (int pi = 0; pi < numPreds; pi++) {
//    	for (int oi = 0; oi < numOutcomes; oi++) {
//    		expectation[pi][oi] = (double) (predCount[pi][oi] / numUniqueEvents);
//    		
//    		display("expectation[pred=" + pi + "][outcome=" + oi +"] = " + expectation[pi][oi] + "\n");
//    	}
//    }
    
    MutableContext[] observedExpects = new MutableContext[numPreds + 1];
    MutableContext[] modelExpects = new MutableContext[numPreds + 1];
    
    for (int pi = 0; pi < numPreds; pi++) {
    	observedExpects[pi] = new MutableContext(new int[numOutcomes], new double[numOutcomes]);
    	modelExpects[pi] = new MutableContext(new int[numOutcomes], new double[numOutcomes]);
    	for (int oi = 0; oi < numOutcomes; oi++) {
    		observedExpects[pi].setParameter(oi, predCount[pi][oi]);
    		modelExpects[pi].setParameter(oi, 0.0);
    	}
    }
    modelExpects[numPreds] = new MutableContext(new int[1], new double[1]);
    
    // a fake "observation" to cover features which are not detected in the data.
    double smoothingObservation = 0;
    float[] smoothing = new float[numUniqueEvents];
    float smoothingSum = 0;
    for (int ei = 0; ei < numUniqueEvents; ei++) {
    	int[] context = contexts[ei];
    	display("value[" + ei + "]: " + context.length + "\n");
    	smoothing[ei] = (float) (correctionConstant - context.length);
    	smoothingSum += smoothing[ei];
    }
    smoothingObservation = (double) (smoothingSum / numUniqueEvents);
    observedExpects[numPreds] = new MutableContext(new int[1], new double[1]);
    observedExpects[numPreds].setParameter(0, smoothingSum);
    display("expectation of smoothing observation: " + smoothingObservation + "\n");
    
    double[][] product = new double[numUniqueEvents][numOutcomes];
    for (int ei = 0; ei < numUniqueEvents; ei++) {
    	int[] context = contexts[ei];
    	int outcomeIndex = outcomes[ei];
    	for (int oi = 0; oi < numOutcomes; oi++) {
       	product[ei][oi] = 1;
      	for (int ci = 0; ci < context.length; ci++) {
      		if (oi == outcomeIndex)
      			product[ei][oi] *= (double) Math.pow(0.5, 1); 
      		else
      			product[ei][oi] *= (double) Math.pow(0.5, 0); 
      	}
      	product[ei][oi] *= (double) Math.pow(0.5, smoothing[ei]);
    	}
    }
    
    double[][] condProb = new double[numUniqueEvents][numOutcomes];
    for (int ei = 0; ei < numUniqueEvents; ei++) {
    	double z = 0;
    	for (int oi = 0; oi < numOutcomes; oi++) {
    		z += product[ei][oi];
    	}
    	for (int oi = 0; oi < numOutcomes; oi++) {
    		condProb[ei][oi] = product[ei][oi] / z;
    		display("P(outcome=" + oi + "|event=" + ei + "): " + condProb[ei][oi] + "\n");
    	}
    }
    
    double loglikelihood = 0;
    for (int ei = 0; ei < numUniqueEvents; ei++) {
    	int outcomeIndex = outcomes[ei];
    	loglikelihood += Math.log(condProb[ei][outcomeIndex]) / Math.log(10);
    }

    display("log likelihood: " + loglikelihood + "\n");
    
    for (int pi = 0; pi < modelExpects.length; pi++) {
    	double[] predExpect = new double[numOutcomes];
    	double smoothPredExpect = 0;
    	
    	for (int oi = 0; oi < numOutcomes; oi++) { 
    		for (int ei = 0; ei < numUniqueEvents; ei++) {
    			int outcomeIndex = outcomes[ei];
    			int[] context = contexts[ei];
    			if (pi < modelExpects.length - 1) {
	    			if (oi == outcomeIndex) {
	    				for (int ci = 0; ci < context.length; ci++) {
	    					if (context[ci] == pi)
	    						predExpect[oi] += condProb[ei][oi] * 1;
	    				}
	    			}
    			}
    			else {
        			smoothPredExpect += smoothing[ei] * condProb[ei][oi];
    			}
      	}
    		if (pi < modelExpects.length - 1) {
    			double tempExpect1 = predExpect[oi] / numUniqueEvents;
    			display("E(fi) <- (pred=" + pi + ",outcome=" + oi + "): " + tempExpect1 + "\n");
    			
    			double tempExpect2 = (observedExpects[pi].getParameters()[oi] / numUniqueEvents) / tempExpect1;
    			if (tempExpect1 == 0)
    				tempExpect2 = 1;
    			display("E~(fi)/E(fi) <- (pred=" + pi + ",outcome=" + oi + "): " + tempExpect2 + "\n");
    			
    			double alpha = Math.pow(tempExpect2, 1/correctionConstant) * 0.5;
    			modelExpects[pi].updateParameter(oi, alpha);
    			display("new alpha <- (pred=" + pi + ",outcome=" + oi + "): " + alpha + "\n");
    		}
    	}
    	
  		
    	if (pi == modelExpects.length - 1) {
    		double tempExpect1 = smoothPredExpect / numUniqueEvents;
  			display("E(pred=" + pi + "): " + tempExpect1 + "\n");
  			
  			double tempExpect2 = (observedExpects[pi].getParameters()[0] / numUniqueEvents) / tempExpect1;
  			if (tempExpect1 == 0)
  				tempExpect2 = 1;
  			display("E~(fi)/E(fi) <- (pred=" + pi + "): " + tempExpect2 + "\n");
  			
  			double alpha = Math.pow(tempExpect2, 1/correctionConstant) * 0.5;
  			modelExpects[pi].updateParameter(0, alpha);
  			display("new alpha <- (pred=" + pi + "): " + alpha + "\n");
  		}   
    }

    return null;
  }
 	
  private void display(String s) {
    if (printMessages)
      System.out.print(s);
  }
  
	public static void main(String[] args) throws Exception {
		LinkedList<Event> events = new LinkedList<Event>();
		
		String[] contexts1 = { "w1", "w2", "w3" };
		Event event1 = new Event("A", contexts1);
		
		String[] contexts2 = { "w2", "w3", "w5" };
		Event event2 = new Event("A", contexts2);
		
		String[] contexts3 = { "w1", "w2" };
		Event event3 = new Event("A", contexts3);
		
		String[] contexts4 = { "w4", "w5" };
		Event event4 = new Event("B", contexts4);
		
		String[] contexts5 = { "w2", "w4", "w6" };
		Event event5 = new Event("B", contexts5);
		
		String[] contexts6 = { "w5", "w6" };
		Event event6 = new Event("B", contexts6);
		
		String[] contexts7 = { "w5", "w6" };
		Event event7 = new Event("B", contexts7);
		
		events.add(event1);
		events.add(event2);
		events.add(event3);
		events.add(event4);
		events.add(event5);
		events.add(event6);
		//events.add(event7);
		
		Map<String, Integer> predicateIndex = new HashMap<String, Integer>();
		predicateIndex.put("w1", 0);
		predicateIndex.put("w2", 1);
		predicateIndex.put("w3", 2);
		predicateIndex.put("w4", 3);
		predicateIndex.put("w5", 4);
		predicateIndex.put("w6", 5);
		
		Dataset dataset = new Dataset();
		dataset.setEvents(events, predicateIndex, false);
		
		System.out.println("The labels of outcomes == " + Arrays.asList(dataset.getOutcomeLabels()));
		System.out.println("The labels of context predicates == " + Arrays.asList(dataset.getPredLabels()));
		
		GISTrainer trainer = new GISTrainer(true);
		trainer.trainModel(15, dataset, new UniformPrior());
	}
	
}
