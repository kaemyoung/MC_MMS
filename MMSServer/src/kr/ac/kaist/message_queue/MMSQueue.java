package kr.ac.kaist.message_queue;

/* -------------------------------------------------------- */
/** 
File name : MMSQueue.java
	Before MSC polls messages, it queues messages for a moment.
Author : Jaehyun Park (jae519@kaist.ac.kr)
	Jin Jung (jungst0001@kaist.ac.kr)
	Jaehee Ha (jaehee.ha@kaist.ac.kr)
Creation Date : 2017-01-24
Version : 0.3.01
*/
/* -------------------------------------------------------- */

import io.netty.handler.codec.http.*;
import kr.ac.kaist.mms_server.MMSConfiguration;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MMSQueue {
	private static final String TAG = "MMSQueue";
	
	public static HashMap<String, String> queue = new HashMap<String, String>();
	
	//public mmsQueue(){
	//}
	
	public static synchronized byte[] getMessage(String mrn) throws Exception{
		if(MMSConfiguration.LOGGING)System.out.println("get queue:" + mrn);
    	if (queue.containsKey(mrn)) {
    		String ret = queue.get(mrn).trim() + "\0";
    		queue.remove(mrn);
    		if(MMSConfiguration.LOGGING)System.out.println("dequeue" + ret);
    		return ret.getBytes(Charset.forName("UTF-8"));
    	} else {
    		throw new Exception("No entry");
    	}
	}
	public static synchronized void putMessage(String mrn, FullHttpRequest req) throws UnsupportedEncodingException{
    	if (queue.containsKey(mrn)){
    		String ret = queue.get(mrn).trim();
    		
    		
//    		TODO: it needs to change right sequence number
    		JSONParser parser = new JSONParser();
    		try {
				JSONObject jsonPayload = (JSONObject) parser.parse(ret);
				JSONArray jsonArray = (JSONArray) jsonPayload.get("payload");
				
				String requestBytes = req.content().toString(Charset.forName("UTF-8")).trim();
				JSONObject jsonNewPayload = (JSONObject) parser.parse(requestBytes);
				JSONArray jsonNewArray = (JSONArray) jsonNewPayload.get("payload");
				
				jsonArray.add(jsonNewArray.get(0));
				JSONObject jsonResult = new JSONObject();
				jsonResult.put("payload", jsonArray);
				String newEntry = jsonResult.toJSONString();
				
				queue.put(mrn, newEntry);
			} catch (ParseException e) {
				if(MMSConfiguration.LOGGING)e.printStackTrace();
			}
    		
    		if(MMSConfiguration.LOGGING)System.out.println("queuing: " + ret);

    	} else {
    		String requestBytes = req.content().toString(Charset.forName("UTF-8")).trim();
    		
    		queue.put(mrn, requestBytes);
    	}
	}
}
