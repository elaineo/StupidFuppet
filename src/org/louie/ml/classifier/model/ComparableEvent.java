package org.louie.ml.classifier.model;

import java.util.Arrays;

/**
 * A maxent event representation which we can use to sort based on the
 * predicates indexes contained in the events.
 * 
 * @author Younggue Bae
 */
public class ComparableEvent implements Comparable<ComparableEvent> {
	public int outcome;
  public int[] predIndexes;
  public int seen = 1; // the number of times this event has been seen

  public float[] values;

  public ComparableEvent(int oc, int[] pids, float[] values) {
    this.outcome = oc;
    if (values == null) {
      Arrays.sort(pids);
    } else {
      sort(pids, values);
    }
    this.values = values; // needs to be sorted like pids
    this.predIndexes = pids;
  }

  public ComparableEvent(int oc, int[] pids) {
    this(oc, pids, null);
  }

  public int compareTo(ComparableEvent ce) {
    
    if (outcome < ce.outcome)
      return -1;
    else if (outcome > ce.outcome)
      return 1;

    int smallerLength = (predIndexes.length > ce.predIndexes.length ? ce.predIndexes.length : predIndexes.length);

    for (int i = 0; i < smallerLength; i++) {
      if (predIndexes[i] < ce.predIndexes[i])
        return -1;
      else if (predIndexes[i] > ce.predIndexes[i])
        return 1;
      if (values != null && ce.values != null) {
        if (values[i] < ce.values[i])
          return -1;
        else if (values[i] > ce.values[i])
          return 1;
      } else if (values != null) {
        if (values[i] < 1)
          return -1;
        else if (values[i] > 1)
          return 1;
      } else if (ce.values != null) {
        if (1 < ce.values[i])
          return -1;
        else if (1 > ce.values[i])
          return 1;
      }
    }

    if (predIndexes.length < ce.predIndexes.length)
      return -1;
    else if (predIndexes.length > ce.predIndexes.length)
      return 1;

    return 0;
  }

  public String toString() {
    StringBuilder s = new StringBuilder().append(outcome).append(":");
    for (int i = 0; i < predIndexes.length; i++) {
      s.append(" ").append(predIndexes[i]);
      if (values != null) {
        s.append("=").append(values[i]);
      }
    }
    return s.toString();
  }

  private void sort(int[] pids, float[] values) {
    for (int mi = 0; mi < pids.length; mi++) {
      int min = mi;
      for (int pi = mi + 1; pi < pids.length; pi++) {
        if (pids[min] > pids[pi]) {
          min = pi;
        }
      }
      int pid = pids[mi];
      pids[mi] = pids[min];
      pids[min] = pid;
      float val = values[mi];
      values[mi] = values[min];
      values[min] = val;
    }
  }
}
