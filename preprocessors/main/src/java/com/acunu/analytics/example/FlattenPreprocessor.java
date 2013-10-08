package com.acunu.analytics.example;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.acunu.analytics.Context;
import com.acunu.analytics.Event;
import com.acunu.analytics.EventReceiver;
import com.acunu.analytics.Preprocessor;

/**
 * A pre-processor which flattens out a hierarchically structured object into a
 * flat one, by joining key names with an underscore ('_').
 * 
 * @author abyde
 */
public class FlattenPreprocessor extends Preprocessor {

	public static final String PARAM_RECEIVER_NAME = "recevier";
	private final EventReceiver receiver;

	public static final String PARAM_SEPARATOR = "separator";
	private final String separator;

	public FlattenPreprocessor(String name, Context context) {
		super(name, context);

		// Use context.getConfig to get the parameters passed to the
		// preprocessor.
		String receiverName = context.getConfig().getString(PARAM_RECEIVER_NAME, null);
		if (receiverName == null)
			throw new IllegalArgumentException("Cannot construct " + this.getClass().getSimpleName()
					+ " without a receiver name -- specify using parameter " + PARAM_RECEIVER_NAME);

		// Use context.getEventReceiver to get a handle on other tables and
		// preprocessors.
		this.receiver = context.getEventReceiverDirectory().lookup(receiverName);

		this.separator = context.getConfig().getString(PARAM_SEPARATOR, "_");
	}

	/**
	 * The key method - submitEvent, takes an {@link Event} object. Here we pass
	 * a modified object on to the specified event receiver.
	 */
	public void submitEvent(Event event) throws IOException {
		receiver.submitEvent(new Event(flatten(event, new Event(), "")));
	}

	protected Map<String, Object> flatten(Map<?, ?> in, Map<String, Object> out, String prefix) {
		for (Entry<?, ?> entry : in.entrySet()) {
			if (entry.getKey() instanceof String) {
				final String key = prefix + (String) entry.getKey();
				final Object value = entry.getValue();

				if (value instanceof Map) {
					flatten((Map<?, ?>) value, out, key + separator);
				} else {
					out.put(key, value);
				}
			}
		}
		return out;
	}
}
