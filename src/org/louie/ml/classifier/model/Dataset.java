package org.louie.ml.classifier.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Object which contains events and context counts used in training.
 * 
 * @author Younggue Bae
 */
public class Dataset {

	private int numEvents;
	
	/** The integer contexts associated with each unique event. */ 
  protected int[][] contexts;
  
  /** The integer outcome associated with each unique event. */ 
  protected int[] outcomes;
  
  /** The number of times an event occurred in the training data. */
  protected int[] numTimesEventsSeen;
  
  /** The predicate/context names. */
  protected String[] predLabels;
  
  /** The names of the outcomes. */
  protected String[] outcomeLabels;
  
  /** The number of times each predicate occurred. */
  protected int[] predCounts;
  
  /**
   * Returns the array of predicates seen in each event. 
   * @return a 2-D array whose first dimension is the event index and array this refers to contains
   * the contexts for that event. 
   */
  public int[][] getContexts() {
    return contexts;
  }

  /**
   * Returns an array indicating the number of times a particular event was seen.
   * @return an array indexed by the event index indicating the number of times a particular event was seen.
   */
  public int[] getNumTimesEventsSeen() {
    return numTimesEventsSeen;
  }

  /**
   * Returns an array indicating the outcome index for each event.
   * @return an array indicating the outcome index for each event.
   */
  public int[] getOutcomes() {
    return outcomes;
  }

  /**
   * Returns an array of predicate/context names.
   * @return an array of predicate/context names indexed by context index. These indices are the
   * value of the array returned by <code>getContexts</code>.
   */
  public String[] getPredLabels() {
    return predLabels;
  }
  
  /**
   * Returns an array of the count of each predicate in the events.
   * @return an array of the count of each predicate in the events.
   */
  public int[] getPredCounts() {
    return predCounts;
  }

  /**
   * Returns an array of outcome names.
   * @return an array of outcome names indexed by outcome index.
   */
  public String[] getOutcomeLabels() {
    return outcomeLabels;
  }
  
  /**
   * Returns the number of total events indexed.
   * @return The number of total events indexed.
   */
  public int getNumEvents() {
    return numEvents;
  }
  
  /**
   * Returns the values associated with each event context or null if integer values are to be used. 
   * @return the values associated with each event context.
   */
  public float[][] getValues() {
  	return null;
  }
  
  /**
   * Sorts and Remove duplicates with the array of comparable events and return the number of unique events.
   *
   * @param events the List of <code>Event</code> value
   * @param predicateIndex the Map of predicate label and it's index
   * @return The number of unique events in the specified list.
   */
  public int setEvents(LinkedList<Event> events, Map<String, Integer> predicateIndex, boolean sort) {
  	List<ComparableEvent> eventsToCompare = this.index(events, predicateIndex);
    int numUniqueEvents = 1;
    numEvents = eventsToCompare.size();
    if (sort) {
      Collections.sort(eventsToCompare);
      if (numEvents <= 1) {
        return numUniqueEvents; // nothing to do; edge case (see assertion)
      }

      ComparableEvent ce = eventsToCompare.get(0);
      for (int i = 1; i < numEvents; i++) {
        ComparableEvent ce2 = eventsToCompare.get(i);

        if (ce.compareTo(ce2) == 0) { 
          //ce.seen++; // increment the seen count
          eventsToCompare.set(i, null); // kill the duplicate
        }
        else {
          ce = ce2; // a new champion emerges...
          numUniqueEvents++; // increment the # of unique events
        }
      }
    }
    else {
      numUniqueEvents = eventsToCompare.size();
    }
    if (sort) System.out.println("Reduced " + numEvents + " events to " + numUniqueEvents + ".");

    contexts = new int[numUniqueEvents][];
    outcomes = new int[numUniqueEvents];
    numTimesEventsSeen = new int[numUniqueEvents];

    for (int i = 0; i < numEvents; i++) {
      ComparableEvent evt = eventsToCompare.get(i);
      if (null == evt) {
        continue; // this was a dupe, skip over it.
      }
      numTimesEventsSeen[i] = evt.seen;
      outcomes[i] = evt.outcome;
      contexts[i] = evt.predIndexes;
    }
    return numUniqueEvents;
	}
  
  protected List<ComparableEvent> index(LinkedList<Event> events, Map<String, Integer> predicateIndex) {
    Map<String, Integer> omap = new HashMap<String, Integer>();

    int numEvents = events.size();
    int outcomeCount = 0;
    List<ComparableEvent> eventsToCompare = new ArrayList<ComparableEvent>(numEvents);
    List<Integer> indexedContext = new ArrayList<Integer>();

    for (int eventIndex = 0; eventIndex < numEvents; eventIndex++) {
      Event ev = events.removeFirst();
      String[] econtext = ev.getContext();
      ComparableEvent ce;

      int ocID;
      String oc = ev.getOutcome();

      if (omap.containsKey(oc)) {
        ocID = omap.get(oc);
      } else {
        ocID = outcomeCount++;
        omap.put(oc, ocID);
      }

      for (String pred : econtext) {
        if (predicateIndex.containsKey(pred)) {
          indexedContext.add(predicateIndex.get(pred));
        }
      }

      // drop events with no active features
      if (indexedContext.size() > 0) {
        int[] cons = new int[indexedContext.size()];
        for (int ci = 0; ci < cons.length; ci++) {
          cons[ci] = indexedContext.get(ci);
        }
        ce = new ComparableEvent(ocID, cons);
        eventsToCompare.add(ce);
      } else {
        System.err.println("Dropped event " + ev.getOutcome() + ":" + Arrays.asList(ev.getContext()));
      }

      indexedContext.clear();
    }
    this.outcomeLabels = toIndexedStringArray(omap);
    this.predLabels = toIndexedStringArray(predicateIndex);
    return eventsToCompare;
  }
  
  /**
   * Utility method for creating a String[] array from a map whose
   * keys are labels (Strings) to be stored in the array and whose
   * values are the indices (Integers) at which the corresponding
   * labels should be inserted.
   *
   * @param labelToIndexMap a Map of label and it's index value
   * @return a <code>String[]</code> value
   */
  protected static String[] toIndexedStringArray(Map<String, Integer> labelToIndexMap) {
    final String[] array = new String[labelToIndexMap.size()];
    for (String label : labelToIndexMap.keySet()) {
      array[labelToIndexMap.get(label)] = label;
    }
    return array;
  }
  
}
