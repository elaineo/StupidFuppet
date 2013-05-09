package com.elaineou.utils;

import java.util.Set;

public class FeedArticle {
	private String title;
	private String link;
	private String descr;
	private Set<String> tags; /* includes categories and keys */
	
	public FeedArticle(String t, String l, String d, Set<String> ts) {
		title=t;
		link=l;
		descr=d;
		tags=ts;
	}
	public String getTitle() {
		return title;
	}
	public String getLink() {
		return link;
	}
	public String getDescr() {
		return descr;
	}
	public Set<String> getTags() {
		return tags;
	}
	public void addTag(String t){
		tags.add(t);
	}
}
