package jn.main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import jn.util.EventProcessor;

public class EventProcessorImpl extends EventProcessor {

	final Pattern panos_traffic_re = Pattern.compile(
			"(?mi)\\<\\d+\\>?([\\w\\d\\s\\/\\:]+)\\s+(\\S+)\\s+(\\d+)\\,([\\d\\s\\/\\:]+)\\,([\\d\\w]+)\\,(TRAFFIC)\\,(\\w+)\\,(\\d+)\\,([\\d\\s\\/\\:]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,(\\S+)\\,(\\w+)?\\,(\\w+)?\\,(\\S+)?\\,(\\S+)?\\,([a-zA-Z0-9_ ]*)\\,([a-zA-Z0-9_ ]*)\\,([\\w\\.\\/\\-]+)\\,(\\S+)?\\,(\\S+)?\\,([\\w\\/\\s\\:]+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(0[xX][0-9a-fA-F]+)\\,(\\w+)\\,([\\w\\-]+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,([\\w\\/\\s\\:]+)\\,(\\d+)\\,(\\w+)\\,(\\d+)\\,(\\d+)\\,(0[xX][0-9a-fA-F]+)\\,([\\d\\.\\-]+)\\,([\\d\\.\\-]+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,([\\w\\d\\-\\.\\_]+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)?\\,([\\w\\d\\-\\_]+)\\,([\\w\\d\\-\\_]+)\\,([\\w\\d]+)?\\,([\\w\\d]+)?\\,([\\w\\d]+)?\\,([\\w\\d]+)?\\,([\\w\\d]+)?\\,([\\w\\d]+)?\\,([\\w\\d\\/]+)?");
	final Pattern panos_threat_re = Pattern.compile(
			"(?mi)\\<\\d+\\>?([\\w\\d\\s\\/\\:]+)\\s+(\\S+)\\s+(\\d+)\\,([\\d\\s\\/\\:]+)\\,([\\d\\w]+)\\,(THREAT)\\,([\\w\\s\\d\\-\\_]+)\\,(\\d+)\\,([\\w\\d\\s\\/\\:]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,([\\d\\.]+)\\,([\\d\\w\\-\\_]+)\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\w\\d\\.\\-\\_\\/]+)?\\,([\\w\\d\\.\\-\\_\\/]+)?\\,([\\w\\d\\.\\-\\_]+)?\\,([\\d\\:\\s\\/]+)?\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(\\d+)\\,(0[xX][0-9a-fA-F]+)\\,(\\w+)\\,([\\w\\-]+)\\,\\\"(.+)\\\"\\,\\((\\d+)\\)\\,([\\w\\-\\_]+)\\,([\\d\\w\\-\\_]+)\\,([\\d\\w\\-\\_]+)\\,(\\d+)\\,(0[xX][0-9a-fA-F]+)\\,([\\w\\s\\d\\.\\-]+)\\,([\\w\\s\\d\\.\\-]+)\\,([\\w\\s\\d\\.\\-]+)?\\,([\\w\\s\\d\\.\\-\\/]+)?\\,(\\d+)?\\,([\\d\\w]+)?\\,([\\d\\w\\.\\-\\_]+)?\\,(\\d+)?\\,(.+?[/\\s][\\d.]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_\\/]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?\\,([\\w\\d\\-\\_]+)?");

	boolean isPanOSEvent(String event) {
		return event.contains(",TRAFFIC,") || event.contains(",THREAT,");
	}

	/**
	 * Method process and enrich the input data.
	 * 
	 * @param message: Input Kafka Message
	 * @return enriched message
	 */
	public String process(String event) {
		JSONObject result = null;
		JSONObject receivedObject = null;
		try {
			receivedObject = new JSONObject(event);
			String message = receivedObject.getString("data");
			if (isPanOSEvent(message)) {
				System.out.println("Panos message detected:" + message);
				result = parsePanOSEvent(message, receivedObject.getJSONArray("source").getString(0));
				System.out.println("Output: "+result.toString());
			} else {
				result.put("ErrMessage", event);
				System.out.println("Non-Panos message: " + event);
			}

		} catch (Exception e) {
			System.err.println(event + "\n" + e.toString());
			try {
				result.put("ErrMessage", event);
			} catch (JSONException e1) {
				
				e1.printStackTrace();
			}
		}

		return result == null ? null : result.toString();
	}
	
	public boolean isErrorMessage(String str) {
		return false;
		
	}

	public JSONObject parsePanOSEvent(String message, String host) throws JSONException {

		JSONObject object = new JSONObject();
		if (message.contains(",TRAFFIC,")) {
			parsePanOSTrafficEvent(object, message, host);
		} else if (message.contains(",THREAT,")) {
			parsePanOSThreatEvent(object, message, host);
		}
		return object;
	}

	private void parsePanOSThreatEvent(JSONObject json, String message, String host) throws JSONException {
		Matcher match = panos_threat_re.matcher(message);
		if (match.find()) {
			String msg = match.group(7);
			if (msg.toLowerCase().equals("url")) {
				json.put("urlname", match.group(34));
				json.put("ucat", match.group(36));
				json.put("content_type", match.group(44));
				json.put("url_idx", match.group(45));
				json.put("user_agent", match.group(49));
				json.put("xff", match.group(51));
				json.put("referer", match.group(52));
				json.put("http_method", match.group(66));
			} else if (msg.toLowerCase().equals("file") || msg.toLowerCase().equals("virus")
					|| msg.toLowerCase().equals("virus")) {
				json.put("filename", match.group(33));
				json.put("fcat", match.group(35));
				json.put("filedigest", match.group(45));
				json.put("wildfireCloud", match.group(46));
				json.put("filetype", match.group(49));
				json.put("wsender", match.group(52));
				json.put("wsubject", match.group(53));
				json.put("wrecipient", match.group(54));
				json.put("wreportid", match.group(55));
			}
			json.put("at", match.group(1));
			json.put("ert", match.group(4));
			json.put("dvc_sn", match.group(5));
			json.put("evt_type", match.group(6));
			json.put("name", "PanOS, " + match.group(6));
			json.put("message", msg);
			json.put("et", match.group(9));
			json.put("src_ip", match.group(10));
			json.put("dst_ip", match.group(11));
			json.put("src_nat", match.group(12));
			json.put("dst_nat", match.group(13));
			json.put("fw_rule", match.group(14));
			json.put("src_user", match.group(15));
			json.put("duser", match.group(16));
			json.put("app_name", match.group(17));
			json.put("vsys", match.group(18));
			json.put("szone", match.group(19));
			json.put("dzone", match.group(20));
			json.put("in_iface", match.group(21));
			json.put("out_iface", match.group(22));
			json.put("log_action", match.group(23));
			json.put("sid", match.group(25));
			json.put("repeat_count", match.group(26));
			json.put("sport", match.group(27));
			json.put("dport", match.group(28));
			json.put("snat_port", match.group(29));
			json.put("dnat_port", match.group(30));
			json.put("flags", match.group(31));
			json.put("protocol", match.group(32));
			json.put("act", match.group(33));
			json.put("tid", match.group(35));
			json.put("direction", match.group(38));
			json.put("sequence_number", match.group(39));
			json.put("action_flag", match.group(40));
			json.put("src_country", match.group(41));
			json.put("dst_country", match.group(42));
			json.put("dvchost", match.group(62));
			json.put("thr_category", match.group(73));
			json.put("deviceVendor", "PaloAlto");
			json.put("deviceProduct", "PanOS");
			json.put("outcome", match.group(33));
			json.put("sev", match.group(37));
			json.put("ahost", host);
			json.put("rawLog", message);
		} else {
			System.out.println("Fail to parse message...");
		}
	}

	private void parsePanOSTrafficEvent(JSONObject json, String message, String host) throws JSONException {
		System.out.println("Parsing Traffic Event");
		Matcher match = panos_traffic_re.matcher(message);
		if (match.find()) {
			System.out.println("Found the Match...");
			json.put("at", match.group(1));
			json.put("ert", match.group(4));
			json.put("dvc_sn", match.group(5));
			json.put("evt_type", match.group(6));
			json.put("name", "PanOS: " + match.group(6));
			json.put("message", match.group(7));
			json.put("et", match.group(9));
			json.put("src_ip", match.group(10));
			json.put("dst_ip", match.group(11));
			json.put("src_nat", match.group(12));
			json.put("dst_nat", match.group(13));
			json.put("fw_rule", match.group(14));
			json.put("src_user", match.group(15));
			json.put("duser", match.group(16));
			json.put("app_name", match.group(17));
			json.put("vsys", match.group(18));
			json.put("szone", match.group(19));
			json.put("dzone", match.group(20));
			json.put("in_iface", match.group(21));
			json.put("out_iface", match.group(22));
			json.put("log_action", match.group(23));
			json.put("sid", match.group(25));
			json.put("repeat_count", match.group(26));
			json.put("sport", match.group(27));
			json.put("dport", match.group(28));
			json.put("snat_port", match.group(29));
			json.put("dnat_port", match.group(30));
			json.put("flags", match.group(31));
			json.put("protocol", match.group(32));
			json.put("act", match.group(33));
			json.put("bytes", match.group(34));
			json.put("bytes_sent", match.group(35));
			json.put("bytes_received", match.group(36));
			json.put("packets", match.group(37));
			json.put("session_time", match.group(38));
			json.put("session_elapsed_time", match.group(39));
			json.put("sequence_number", match.group(42));
			json.put("action_flag", match.group(43));
			json.put("src_country", match.group(44));
			json.put("dst_country", match.group(45));
			json.put("pkts_sent", match.group(47));
			json.put("pkts_received", match.group(48));
			json.put("session_end_reason", match.group(49));
			json.put("dvchost", match.group(55));
			json.put("action_source", match.group(56));
			json.put("deviceVendor", "PaloAlto");
			json.put("deviceProduct", "PanOS");
			json.put("outcome", match.group(33));
			json.put("sev", "");
			json.put("ahost", host);
			json.put("rawLog", message);
			System.out.println("result: " + json.toString());
		} else {
			System.out.println("Fail to parse message.");
		}
	}

	public static void main(String args[]) {

		String[] str = new String[] {"{\"source\": [\"127.0.0.1\", \"1234\"], \"received_time\": \"timestamp here\", \"data\": \"<14>Aug  7 18:30:59 US-FW02.corp.example.com 1,2018/08/07 18:30:58,0011C102101,TRAFFIC,drop,0,2018/08/07 18:30:58,10.253.58.86,10.102.69.196,0.0.0.0,0.0.0.0,interzone-default,,,not-applicable,vsys1,IT Labs,Trust,ethernet1/13.1021,,Log_Forward,2018/08/07 18:30:58,0,1,63520,514,0,0,0x0,udp,deny,186,186,0,1,2018/08/07 18:30:59,0,any,0,1159886926,0x8000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,1,0,policy-deny,0,0,0,0,,US-FW02,from-policy,,,0,,0,,N/A\"}" };

		EventProcessorImpl processor = new EventProcessorImpl();
		for (String string : str) {
			// String obj=processor.process(string);
				System.out.println(processor.process(string));
		
		}

	}
}
