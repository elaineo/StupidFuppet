package com.elaineou.feedread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.louie.ml.lexrank.DocumentSummarizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.elaineou.utils.MySSLSocketFactory;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class FeedReadActivity extends Fragment {
	private static final String TAG = "FeedReadActivity";
    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_DESC = "description";
    static final String KEY_TITL = "title";
    static final String KEY_LINK = "link";
    static final String KEY_CATE = "category";
	private Properties FeedReadprops;
	
	private HashMap<FeedKey, FeedArticle> feedMap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	 
	  FeedReadprops = new Properties();
	  FeedReadprops.put("annotators", "tokenize, ssplit");
	  CollectFeed rssfeed = new CollectFeed();
	  rssfeed.execute();
	}
	
	class CollectFeed extends AsyncTask<Void,Void,Void> {
		
		private String summarize_article(String text){
			Properties props = new Properties();
		    props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

			String buf = "";
		    Annotation document = new Annotation(text);
		    
		    // run all Annotators on this text
		    pipeline.annotate(document);
		    
		    // these are all the sentences in this document
		    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		    
		    List<CoreMap> selected_sentences = new ArrayList<CoreMap>();
		    Set<String> seen_persons = new HashSet<String>();
		    for(CoreMap sentence: sentences) {
		        // traversing the words in the current sentence
		        // a CoreLabel is a CoreMap with additional token-specific methods
		    	boolean pick_sentence = false;
		        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		          // this is the text of the token
		          String word = token.get(TextAnnotation.class);
		          // this is the POS tag of the token
		          String pos = token.get(PartOfSpeechAnnotation.class);
		          // this is the NER label of the token
		          String ner = token.get(NamedEntityTagAnnotation.class);
//		          System.out.println(word +' '+ ner);
		          if (ner.equals("PERSON")){
	//	        	  System.out.println(ner);
		        	  int i = seen_persons.size();
		        	  seen_persons.add(word);
		        	  if (seen_persons.size() > i){
		        		  pick_sentence = true;
		        	  }
		          }
		        }
		        if (pick_sentence){ 
	      	  		selected_sentences.add(sentence);
		        }
		    }
		    // assemble sentence
		    for(CoreMap sentence: selected_sentences) {
		    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		    		String word = token.get(TextAnnotation.class);
		    		buf += word + ' ';
		    	}
		    }

			return buf;
		}
		private String summarize_multidocument(String text){
			String buf = "";
			// Make a list of sentences
			Properties props = new Properties();
		    props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		    Annotation document = new Annotation(text);
		    pipeline.annotate(document);
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		    
		    List<CoreMap> selected_sentences = new ArrayList<CoreMap>();
		    Set<String> seen_persons = new HashSet<String>();
		    List<String> core_sentences = new ArrayList<String>();
		    
		    for(CoreMap sentence: sentences) {
		    	String s = "";
		        for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
			          String word = token.get(TextAnnotation.class);
			          s += word;
		        }
		        core_sentences.add(s);
		    }

			// Summarize
			DocumentSummarizer summarizer = new DocumentSummarizer(core_sentences);
			List<String> results = summarizer.summarize();
			for (String s : results) {
				buf += s;
			}
			return buf;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			String text = new String();
			String xml = new String();
			StanfordCoreNLP pipeline = new StanfordCoreNLP(FeedReadprops);
			feedMap= new HashMap <FeedKey,FeedArticle>();

			/** TODO: Loop through a predefined list of feeds **/
			xml = getXmlFromUrl("http://feeds.feedburner.com/techcrunch/startups?format=xml");

			Document doc = getDomElement(xml);
	        NodeList nl = doc.getElementsByTagName(KEY_ITEM);
	        // looping through all item nodes <item>
	        for (int i = 0; i < nl.getLength(); i++) {
	            // create key based on categories
	        	Element e = (Element) nl.item(i);
	        	Set<String> k = new HashSet<String> (getValues(e,KEY_CATE));
	        	FeedKey fk = new FeedKey(k);
	        	// creating new FeedArticle
	        	FeedArticle art = new FeedArticle(getValue(e, KEY_TITL),getValue(e, KEY_LINK),
	        			getValue(e, KEY_DESC),k);
	            
	        	feedMap.put(fk,art);
	        }
	        
			/* What should we actually tokenize and store??? */
	        /* test test */
	        Iterator it = feedMap.entrySet().iterator();
	        text="";
	        while (it.hasNext()) {
	            Map.Entry pairs = (Map.Entry)it.next();
	            FeedArticle fadebug = (FeedArticle) pairs.getValue();
	            Log.d(TAG, pairs.getKey() + " = " + fadebug);
	            	
	            //blah blah delete this later
	            text = fadebug.getTitle();
	            it.remove(); // avoids a ConcurrentModificationException
	        }

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
		          Log.d(TAG,word);
		        }
		    }
			return null;
		}
		
		public String toString(Document doc) {
		    try {
		        StringWriter sw = new StringWriter();
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer transformer = tf.newTransformer();
		        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		        transformer.transform(new DOMSource(doc), new StreamResult(sw));
		        return sw.toString();
		    } catch (Exception ex) {
		        throw new RuntimeException("Error converting to String", ex);
		    }
		}
	}
	
	 private HttpClient sslClient(HttpClient client) {
	    try {
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", ssf, 443));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	public String getXmlFromUrl(String url) {
        String xml = null;
 
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
 
            HttpResponse httpResponse = sslClient(httpClient).execute(httpGet);
      	    StatusLine status = httpResponse.getStatusLine();
    	    if (status.getStatusCode() != 200) {
    	        Log.d(TAG, "HTTP error, invalid server status code: " + httpResponse.getStatusLine());  
    	    }
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
 
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // return XML
        return xml;
    }
	public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is);  
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        return doc;
    }
	public String getValue(Element item, String str) {      
	    NodeList n = item.getElementsByTagName(str);        
	    return this.getElementValue(n.item(0));
	}
	public ArrayList<String> getValues(Element item, String str) {      
		ArrayList<String> vals = new ArrayList<String>();
		NodeList n = item.getElementsByTagName(str);
		for (int j=0; j<n.getLength();j++) {
			vals.add(getElementValue(n.item(j)));
		}
		return vals;
	} 
	public final String getElementValue( Node elem ) {
         Node child;
         if( elem != null){
             if (elem.hasChildNodes()){
                 for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                     if( child.getNodeType() == Node.TEXT_NODE  ){
                         return child.getNodeValue();
                     }
                 }
             }
         }
         return "";
	  } 
}
