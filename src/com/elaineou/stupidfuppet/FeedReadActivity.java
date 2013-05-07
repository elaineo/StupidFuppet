package com.elaineou.stupidfuppet;

import java.util.List;
import java.util.Properties;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class FeedReadActivity extends Fragment {
	private Properties FeedReadprops;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	 
	  FeedReadprops = new Properties();
	  FeedReadprops.put("annotators", "cleanxml, tokenize, ssplit");
	  StanfordCoreNLP pipeline = new StanfordCoreNLP(FeedReadprops);
	  
	  
	    // test
	    String text = "Fruit flies like a banana.";

	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);

	    // run all Annotators on this text
	    pipeline.annotate(document);
	    
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);

	    for(CoreMap sentence: sentences) {
	        // traversing the words in the current sentence
	        // a CoreLabel is a CoreMap with additional token-specific methods
	        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	          // this is the text of the token
	          String word = token.get(TextAnnotation.class);
	        }
	    }
	}
}
