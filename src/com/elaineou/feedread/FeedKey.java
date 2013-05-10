package com.elaineou.feedread;

import java.util.Set;

/* Serves as object key for looking up articles in hashmap 
 * Right now just by tags 
 */

public class FeedKey {
	private Set<String> tags;
	
	public FeedKey(Set<String> t) {
		tags=t;
	}	
	public void addTag(String t){
		tags.add(t);
	}
	public Set<String> getTags(){
		return tags;
	}
	public int hashCode() {
		/* I don't really know what I'm doing */
	    int hash = 1;
	    int i=1;
	    for (String t : tags) {
	    	hash = i * 31 + t.hashCode();
	    	i++;
	    }
	    return hash;
	}
	public boolean equals(Set<String> other) {
	    // Not strictly necessary, but often a good optimization
	    if (tags.containsAll(other) )
	      return true;
	    else
	      return false;
	  }
}
