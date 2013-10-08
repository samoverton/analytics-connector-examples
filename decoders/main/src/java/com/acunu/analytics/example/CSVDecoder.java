package com.acunu.analytics.example;

import java.io.Reader;
import java.util.List;

import org.mozilla.javascript.edu.emory.mathcs.backport.java.util.Arrays;

import com.acunu.analytics.Event;
import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.ingest.AbstractDecoder;
import com.acunu.analytics.ingest.DecodeException;

/**
 * A CSV decoder for Acunu Analytics.
 * 
 * @author tmoreton
 * 
 */
public class CSVDecoder extends AbstractDecoder {

	/** Name of the property that might be specified to provide the field order */
	public static final String FIELD_ORDER = "field_order";

	public CSVDecoder() {
		super();
	}

	/**
	 * Called whenever a decoder is instantiated in the context of the flow.
	 * 
	 * @param properties
	 *            a read-only reference to the flow properties.
	 */
	public CSVDecoder(ConfigProperties properties) {
		super(properties);
	}

	/**
	 * Return the list of values for Content-Type that this Decoder could be
	 * invoked for, and possibly decode a message. It is not a contract that
	 * they have to be able to decode those messages (it could return
	 * DecodeException), or that this decoder will be called (another
	 * registering the same type may be invoked instead).
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getRegisterableContentTypes() {
		return Arrays.asList(new String[] { "text/csv" });
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

		final Reader r = getStringReader(rawEvent);
		
		return null;
	}

}
