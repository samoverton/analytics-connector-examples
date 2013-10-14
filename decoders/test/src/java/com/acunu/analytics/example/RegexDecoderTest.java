package com.acunu.analytics.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.acunu.analytics.Event;
import com.acunu.analytics.conf.SimpleConfig;
import com.acunu.analytics.ingest.DecodeException;

/**
 * Tests for the {@link RegexDecoder} example.
 * 
 * @author tmoreton
 * 
 */
public class RegexDecoderTest {

	private final String SQUID_EVENT = "1366731329.999 0 10.64.46.220 TCP_HIT/200 2966 GET http://XXX.XXXX/path1/path2/path3?param=value - NONE/- application/json useragent";

	private final String SQUID_REGEX = "(\\d+\\.\\d+)\\s+(\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+([A-Z_]+)\\/(\\d+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)\\/(\\S+)\\s+(\\S+)\\s+(\\S+)";

	private final String SQUID_FIELDS = "timestamp,duration,remote_host,cache_code,response_status,bytes,method,uri,,,,content_type,user_agent";

	@Test
	public void testRegexSquid() throws DecodeException {
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("fields", SQUID_FIELDS);
		props.put("regex", SQUID_REGEX);
		RegexDecoder d = new RegexDecoder(new SimpleConfig(props));

		List<Event> l = d.decode(SQUID_EVENT);
		Assert.assertEquals(1, l.size());
		Event e = l.get(0);

		Assert.assertEquals("1366731329.999", e.get("timestamp"));
		Assert.assertEquals("0", e.get("duration"));
		Assert.assertEquals("TCP_HIT", e.get("cache_code"));
		Assert.assertEquals("application/json", e.get("content_type"));
	}

	@Test
	public void testSimple() throws DecodeException {
		final Map<String, Object> props = new HashMap<String, Object>();
		props.put("fields", "msg");
		props.put("regex", "(\\S+)");
		RegexDecoder d = new RegexDecoder(new SimpleConfig(props));

		List<Event> l = d.decode("hello");
		Assert.assertEquals(1, l.size());
		Event e = l.get(0);
		Assert.assertEquals(1, e.size());

		Assert.assertEquals("hello", e.get("msg"));
	}

}
