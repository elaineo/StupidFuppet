package com.elaineou.feedread;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.util.Log;

import com.elaineou.utils.MySSLSocketFactory;

public class FeedReader extends AsyncTask<Void,Void,HashMap<FeedKey,FeedArticle>> {
	private static final String TAG = "FeedReadActivity";
    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_DESC = "description";
    static final String KEY_TITL = "title";
    static final String KEY_LINK = "link";
    static final String KEY_CATE = "category";
    static final List<String> rssFeeds = Arrays.asList("http://feeds.venturebeat.com/Venturebeat?format=xml",
    			"http://feeds.feedburner.com/TechCrunch/");
	
	private HashMap<FeedKey, FeedArticle> feedMap;
	
		
	@Override
	protected HashMap<FeedKey,FeedArticle> doInBackground(Void... arg0) {
		String xml = new String();
		feedMap= new HashMap <FeedKey,FeedArticle>();

		/** Loop through a predefined list of feeds **/
		for (String rss: rssFeeds) {
			xml = getXmlFromUrl(rss);
			
			Document doc = getDomElement(xml);
	        NodeList nl = doc.getElementsByTagName(KEY_ITEM);
	        // looping through all item nodes <item>
	        for (int i = 0; i < nl.getLength(); i++) {
	            // create key based on categories
	        	Element e = (Element) nl.item(i);
	        	Set<String> k = new HashSet<String> (getValues(e,KEY_CATE));
	        	Set<String> l = new HashSet<String>();
	        	for (String m:k) {
	        		l.add(m.replaceAll("[!]<>", "").replace("CDATA",""));	        		
	        	}
	        	FeedKey fk = new FeedKey(k);
	        	// creating new FeedArticle
	        	FeedArticle art = new FeedArticle(getValue(e, KEY_TITL),getValue(e, KEY_LINK),
	        			getValue(e, KEY_DESC),k);
	            
	        	feedMap.put(fk,art);
	        }
		}
        return feedMap;
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
    protected void onPostExecute(HashMap<FeedKey, FeedArticle> result) {
        returnFeedMap(result);
    }
    private HashMap<FeedKey, FeedArticle> returnFeedMap(HashMap<FeedKey, FeedArticle> result) {
    	return result;
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
