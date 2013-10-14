package com.acunu.analytics.example;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.acunu.analytics.Event;
import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.ingest.AbstractDecoder;
import com.acunu.analytics.ingest.DecodeException;

/**
 * A simple regular expression decoder for converting string events into
 * structured events. It takes a regular expression {@link Pattern} and a
 * comma-separated string of names with which to associate each group matched by
 * the regex, in order.
 * 
 * @author tmoreton
 * 
 */
public class RegexDecoder extends AbstractDecoder {

	/**
	 * Name of the property that specifies the regex pattern
	 **/
	public static final String FIELD_PATTERN = "regex";

	/**
	 * Name of the property that specifies in order, which groups map to which
	 * fields
	 */
	public static final String FIELD_GROUP_NAMES = "fields";

	/**
	 * The regex against which to match events
	 */
	protected Pattern pattern;

	/**
	 * The names of the fields to associate with groups matched by the regex
	 */
	protected String[] groupNames;

	public RegexDecoder() {
		throw new IllegalArgumentException("Missing required parameter '" + FIELD_PATTERN + "'");
	}

	/**
	 * Called whenever a decoder is instantiated in the context of the flow.
	 * 
	 * @param properties
	 *            a read-only reference to the flow properties.
	 */
	public RegexDecoder(ConfigProperties properties) {
		super(properties);
		final String regex = properties.getString(FIELD_PATTERN);
		if (regex == null)
			throw new IllegalArgumentException("Missing required parameter '" + FIELD_PATTERN + "'");
		this.pattern = Pattern.compile(regex);

		final String groups = properties.getString(FIELD_GROUP_NAMES);
		if (groups == null || groups.trim().equals(""))
			throw new IllegalArgumentException("Missing required parameter '" + FIELD_GROUP_NAMES + "'");
		this.groupNames = groups.split("\\s*,\\s*");
	}

	/**
	 * Return the list of values for Content-Type that this Decoder could be
	 * invoked for, and possibly decode a message. It is not a contract that
	 * they have to be able to decode those messages (it could return
	 * DecodeException), or that this decoder will be called (another
	 * registering the same type may be invoked instead).
	 */
	@Override
	public List<String> getRegisterableContentTypes() {
		return Arrays.asList(new String[] { "text/plain" });
	}

	/**
	 * Decode a raw event. Here, we just use the first line or the "field_order"
	 * property as a key to the field ordering in the rest of the message.
	 * 
	 * @return one or more Events, in the order they should be ingested.
	 * @throws DecodeException
	 *             when the raw event is invalid.
	 */
	@Override
	public List<Event> decode(Object rawEvent) throws DecodeException {

		// Try and get a string.
		final String s = getString(rawEvent);

		// Get a matcher for this string
		final Matcher m = this.pattern.matcher(s);

		if (!m.find())
			throw new DecodeException("No regex match");

		// Create a map-like event by adding each supplied group name with the
		// corresponding group from the regex match (start at 1 since group 0 is
		// the whole match). You can skip a group with an empty name.

		final Event e = new Event();
		for (int i = 0; i < m.groupCount() && i < this.groupNames.length; i++)
			if (!"".equals(this.groupNames[i]))
				e.put(this.groupNames[i], m.group(i + 1));

		return Collections.singletonList(e);
	}

	/**
	 * Helper method to get a String over the raw event.
	 * 
	 * @throws DecodeException
	 *             If the raw event isn't a String, byte[] or char[].
	 */
	protected String getString(Object rawEvent) throws DecodeException {
		if (rawEvent instanceof String) {
			return (String) rawEvent;
		} else if (rawEvent instanceof byte[]) {
			return new String((byte[]) rawEvent);
		} else if (rawEvent instanceof char[]) {
			return new String((char[]) rawEvent);
		} else {
			throw new DecodeException("Cannot decode a " + rawEvent.getClass().getCanonicalName());
		}
	}
}
