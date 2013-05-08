package com.elaineou.stupidfuppet;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class FeedReadActivity extends Fragment {
	private static final String TAG = "FeedReadActivity";
    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_DESC = "description";
    static final String KEY_TITL = "title";
    static final String KEY_CATE = "category";
	private Properties FeedReadprops;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	 
	  FeedReadprops = new Properties();
	  FeedReadprops.put("annotators", "tokenize, ssplit");
	  CollectFeed rssfeed = new CollectFeed();
	  rssfeed.execute();
	}
	
	class CollectFeed extends AsyncTask<Void,Void,Void> {
		
		@Override
		protected Void doInBackground(Void... arg0) {
			String text = new String();
			String xml = new String();
			StanfordCoreNLP pipeline = new StanfordCoreNLP(FeedReadprops);
			ArrayList<HashMap<String, String>> feedItems = new ArrayList<HashMap<String, String>>();
			  
			xml = getXmlFromUrl("http://www.thereformedbroker.com/feed/");
			Log.d(TAG,xml);
			Document doc = getDomElement(xml);
	        NodeList nl = doc.getElementsByTagName(KEY_ITEM);
	        // looping through all item nodes <item>
	        for (int i = 0; i < nl.getLength(); i++) {
	            // creating new HashMap
	            HashMap<String, String> map = new HashMap<String, String>();
	            Element e = (Element) nl.item(i);
	            // adding each child node to HashMap key => value
	            map.put(KEY_TITL, getValue(e, KEY_TITL));
	            map.put(KEY_CATE, getValue(e, KEY_CATE));
	            map.put(KEY_DESC, getValue(e, KEY_DESC));

	            
	            // adding HashList to ArrayList
	            feedItems.add(map);
	        }
			/* What should we actually tokenize and store??? */
	        /* test test */
	        for (String value : feedItems.get(0).values()) {
	        	Log.d(TAG,"text = " + value);
	        	text=value;
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
