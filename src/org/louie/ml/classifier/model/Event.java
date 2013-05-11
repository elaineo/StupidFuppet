package org.louie.ml.classifier.model;

/**
 * The finite training or test sample of events. This includes contextual
 * predicates and an outcome(hand-tagged class label).
 * 
 * @author Younggue Bae
 */
public class Event {

	private String outcome;
	private String[] context;
	private float[] values;

	public Event(String outcome, String[] context) {
		this(outcome, context, null);
	}

	public Event(String outcome, String[] context, float[] values) {
		this.outcome = outcome;
		this.context = context;
		this.values = values;
	}

	public String getOutcome() {
		return outcome;
	}

	public String[] getContext() {
		return context;
	}

	public float[] getValues() {
		return values;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(outcome).append(" [");
		if (context.length > 0) {
			sb.append(context[0]);
			if (values != null) {
				sb.append("=").append(values[0]);
			}
		}
		for (int i = 1; i < context.length; i++) {
			sb.append(" ").append(context[i]);
			if (values != null) {
				sb.append("=").append(values[i]);
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
