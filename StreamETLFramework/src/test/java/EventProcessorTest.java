import static org.junit.Assert.assertTrue;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Strings;

import jn.main.EventProcessorImpl;

public class EventProcessorTest {

	EventProcessorImpl processor;


	@Test
	public void trafficProcessor() {
		String[] str = new String[] {
				//"<14>Aug  7 14:23:34 US-DUR-DMZ-FW01.corp.example.com 1,2018/08/07 14:23:34,007901000218,TRAFFIC,drop,0,2018/08/07 14:23:34,106.51.140.93,216.240.29.168,0.0.0.0,0.0.0.0,interzone-default,,,not-applicable,vsys1,Untrust,nDMZ,ethernet1/22,,Log_Forward,2018/08/07 14:23:34,0,1,52065,445,0,0,0x0,tcp,deny,66,66,0,1,2018/08/07 14:23:34,0,any,0,41157862927,0x8000000000000000,India,United States,0,1,0,policy-deny,0,0,0,0,,US-DUR-DMZ-FW01,from-policy,,,0,,0,,N/A",
				"<14>Aug  7 18:30:59 US-FW02.corp.example.com 1,2018/08/07 18:30:58,0011C102101,TRAFFIC,drop,0,2018/08/07 18:30:58,10.253.58.86,10.102.69.196,0.0.0.0,0.0.0.0,interzone-default,,,not-applicable,vsys1,IT Labs,Trust,ethernet1/13.1021,,Log_Forward,2018/08/07 18:30:58,0,1,63520,514,0,0,0x0,udp,deny,186,186,0,1,2018/08/07 18:30:59,0,any,0,1159886926,0x8000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,1,0,policy-deny,0,0,0,0,,US-FW02,from-policy,,,0,,0,,N/A" };

		processor = new EventProcessorImpl();
		for (String string : str) {
			try {
				JSONObject obj = processor.parsePanOSEvent(string,"localhost");
				JSONObject expected = new JSONObject(
						"{\"sequence_number\": \"1159886926\", \"dport\": \"514\", \"repeat_count\": \"1\", \"app_name\": \"not-applicable\", \"pkts_received\": \"0\", \"src_nat\": \"0.0.0.0\", \"duser\": \"\", \"sport\": \"63520\", \"src_country\": \"10.0.0.0-10.255.255.255\", \"dzone\": \"Trust\", \"log_action\": \"Log_Forward\", \"ert\": \"2018/08/07 18:30:58\", \"in_iface\": \"ethernet1/13.1021\", \"protocol\": \"udp\", \"packets\": \"1\", \"dst_ip\": \"10.102.69.196\", \"dst_nat\": \"0.0.0.0\", \"rawLog\": \"<14>Aug  7 18:30:59 US-FW02.corp.example.com 1,2018/08/07 18:30:58,0011C102101,TRAFFIC,drop,0,2018/08/07 18:30:58,10.253.58.86,10.102.69.196,0.0.0.0,0.0.0.0,interzone-default,,,not-applicable,vsys1,IT Labs,Trust,ethernet1/13.1021,,Log_Forward,2018/08/07 18:30:58,0,1,63520,514,0,0,0x0,udp,deny,186,186,0,1,2018/08/07 18:30:59,0,any,0,1159886926,0x8000000000000000,10.0.0.0-10.255.255.255,10.0.0.0-10.255.255.255,0,1,0,policy-deny,0,0,0,0,,US-FW02,from-policy,,,0,,0,,N/A\", \"bytes_received\": \"0\", \"session_time\": \"2018/08/07 18:30:59\", \"flags\": \"0x0\", \"dvc_sn\": \"0011C102101\", \"bytes\": \"186\", \"bytes_sent\": \"186\", \"at\": \"Aug  7 18:30:59\", \"name\": \"PanOS: TRAFFIC\", \"evt_type\": \"TRAFFIC\", \"szone\": \"IT Labs\", \"message\": \"drop\", \"sev\": \"\", \"outcome\": \"deny\", \"deviceVendor\": \"PaloAlto\", \"out_iface\": \"\", \"session_elapsed_time\": \"0\", \"pkts_sent\": \"1\", \"dst_country\": \"10.0.0.0-10.255.255.255\", \"dnat_port\": \"0\", \"et\": \"2018/08/07 18:30:58\", \"src_user\": \"\", \"deviceProduct\": \"PanOS\", \"session_end_reason\": \"policy-deny\", \"sid\": \"0\", \"src_ip\": \"10.253.58.86\", \"vsys\": \"vsys1\", \"action_source\": \"from-policy\", \"ahost\": \"localhost\", \"fw_rule\": \"interzone-default\", \"snat_port\": \"0\", \"dvchost\": \"US-FW02\", \"act\": \"deny\", \"action_flag\": \"0x8000000000000000\"}");

				assertTrue(match("at", expected, obj));
				assertTrue(match("ert", expected, obj));
				assertTrue(match("dvc_sn", expected, obj));
				assertTrue(match("evt_type", expected, obj));
				assertTrue(match("name", expected, obj));
				assertTrue(match("message", expected, obj));
				assertTrue(match("et", expected, obj));
				assertTrue(match("src_ip", expected, obj));
				assertTrue(match("dst_ip", expected, obj));
				assertTrue(match("src_nat", expected, obj));
				assertTrue(match("dst_nat", expected, obj));
				assertTrue(match("fw_rule", expected, obj));
				assertTrue(match("src_user", expected, obj));
				assertTrue(match("duser", expected, obj));
				assertTrue(match("app_name", expected, obj));
				assertTrue(match("vsys", expected, obj));
				assertTrue(match("szone", expected, obj));
				assertTrue(match("dzone", expected, obj));
				assertTrue(match("in_iface", expected, obj));
				assertTrue(match("out_iface", expected, obj));
				assertTrue(match("log_action", expected, obj));
				assertTrue(match("sid", expected, obj));
				assertTrue(match("repeat_count", expected, obj));
				assertTrue(match("sport", expected, obj));
				assertTrue(match("dport", expected, obj));
				assertTrue(match("snat_port", expected, obj));
				assertTrue(match("dnat_port", expected, obj));
				assertTrue(match("flags", expected, obj));
				assertTrue(match("protocol", expected, obj));
				assertTrue(match("act", expected, obj));
				assertTrue(match("bytes", expected, obj));
				assertTrue(match("bytes_sent", expected, obj));
				assertTrue(match("bytes_received", expected, obj));
				assertTrue(match("packets", expected, obj));
				assertTrue(match("session_time", expected, obj));
				assertTrue(match("session_elapsed_time", expected, obj));
				assertTrue(match("sequence_number", expected, obj));
				assertTrue(match("action_flag", expected, obj));
				assertTrue(match("src_country", expected, obj));
				assertTrue(match("dst_country", expected, obj));
				assertTrue(match("pkts_sent", expected, obj));
				assertTrue(match("pkts_received", expected, obj));
				assertTrue(match("session_end_reason", expected, obj));
				assertTrue(match("dvchost", expected, obj));
				assertTrue(match("action_source", expected, obj));
				assertTrue(match("deviceVendor", expected, obj));
				assertTrue(match("deviceProduct", expected, obj));
				assertTrue(match("outcome", expected, obj));
				assertTrue(match("sev", expected, obj));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	public void threatProcessor() {
		String[] str = new String[] {
				"<14>Aug  7 14:42:03 US-DMZ-FW01.corp.example.com 1,2018/08/07 14:42:03,007901000218,THREAT,url,0,2018/08/07 14:42:03,10.100.60.36,125.56.201.105,216.240.31.145,125.56.201.105,7-replica,,,web-browsing,vsys1,DMZ,Untrust,ethernet1/21.2303,ethernet1/22,Log_Forward,2018/08/07 14:42:03,68794816,1,40107,80,40107,80,0x40b000,tcp,alert,\"detectportal.firefox.com/success.txt\",(9999),computer-and-internet-info,informational,client-to-server,582341859,0x8000000000000000,10.0.0.0-10.255.255.255,United States,0,text/plain,0,,,9,Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0,,,,,,,0,0,0,0,0,,US-DUR-DMZ-FW01,,,,get,0,,0,,N/A,unknown,AppThreat-0-0,0x0" };

		processor = new EventProcessorImpl();
		for (String string : str) {
			try {
				JSONObject result = processor.parsePanOSEvent(string,"localhost");
				System.out.println(result.toString());
				JSONObject expected = new JSONObject();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean match(String string, JSONObject expected, JSONObject obj) {
		String exp = null;
		String actual = null;
		exp = expected.opt(string).toString();
		actual = obj.opt(string) == null ? "" : obj.opt(string).toString();
		if ((Strings.isNullOrEmpty(exp.trim()) && Strings.isNullOrEmpty(actual)) || exp.trim().equals(actual)) {
			System.out.println(string + " property matching");
			return true;
		} else {
			System.out.println(string + " property not matching. Expected: " + exp + "  , Return: " + actual);
		}
		return false;
	}
}
