package com.acunu.analytics.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import com.acunu.analytics.UserAggregate;
import com.acunu.analytics.example.ExponentialMovingAverage.TimeDouble;
import com.acunu.analytics.model.Field;
import com.acunu.util.Serialiser;

/**
 * An aggregate to represent an exponential weighted moving average. The
 * underlying counter is a pair of time and value; the combination of two
 * counters is to scale the older one down by a multiplier depending
 * exponentially on the time difference between the two. The rate of decay is
 * controlled by the parameter 'lambda' passed in via the constructor.
 * 
 * @author abyde
 */
public class ExponentialMovingAverage extends UserAggregate<Double, TimeDouble> {

	/**
	 * A tuple of time and double value.
	 * 
	 * @author abyde
	 */
	public static class TimeDouble {
		public static Serialiser<TimeDouble> ser = new Serialiser<TimeDouble>() {

			@Override
			public TimeDouble fromBytes(ByteBuffer buf) throws IOException {
				long time = buf.getLong();
				double value = buf.getDouble();
				return new TimeDouble(time, value);
			}

			@Override
			public int sizeInBytes(TimeDouble timeValue) {
				return 16;
			}

			@Override
			public ByteBuffer toBytes(TimeDouble timeValue) {
				ByteBuffer buf = ByteBuffer.allocate(16);
				buf.putLong(timeValue.time);
				buf.putDouble(timeValue.value);
				buf.rewind();
				return buf;
			}

			@Override
			public void write(ByteBuffer buf, TimeDouble timeValue) throws IOException {
				buf.putLong(timeValue.time);
				buf.putDouble(timeValue.value);
			}
		};

		public static final TimeDouble ZERO = new TimeDouble(0, 0);

		private long time;
		private double value;

		public TimeDouble(long time, double value) {
			this.time = time;
			this.value = value;
		}

		@Override
		public String toString() {
			return "(" + time + ", " + value + ")";
		}

		public static TimeDouble accumulate(double lambda, TimeDouble older, TimeDouble newer) {
			if (older == null)
				return newer;
			if (newer == null)
				return older;
			long delta = newer.time - older.time;
			if (delta < 0) {
				throw new IllegalArgumentException("destination counter " + older + " is newer than supposedly newer counter " + newer);
			}
			assert delta >= 0;
			double discount = Math.exp(lambda * delta);
			return new TimeDouble(newer.time, newer.value + older.value * discount);
		}

	}

	private final double lambda;
	private final int timeIndex;
	private final int valueIndex;

	/**
	 * Constructor taking a specification of value and time dimensions and a
	 * decay factor.
	 * 
	 * @param fields references to the dimensions in the table.
	 * @param args
	 *            Three arguments, in order,
	 *            <ol>
	 *            <li>Time dimension -- t;</li>
	 *            <li>Value dimension -- x; and</li>
	 *            <li>Lambda decay factor.</li>
	 *            </ol>
	 */
	public ExponentialMovingAverage(List<Field> fields, String[] args) {
		super(fields, args);
		lambda = Double.parseDouble(args[2]);
		timeIndex = lookupField(args[0]);
		valueIndex = lookupField(args[1]);
	}

	@Override
	public TimeDouble counterFromEvent(Comparable[] event) {
		long time = (Long) event[timeIndex];
		double value = (Double) event[valueIndex];
		return new TimeDouble(time, value);
	}

	@Override
	public Double evalCounter(TimeDouble counter) {
		return counter.value;
	}

	@Override
	public Serialiser<TimeDouble> getSerialiser() {
		return TimeDouble.ser;
	}

	@Override
	public Class<Double> rawValueType() {
		return Double.class;
	}

	@Override
	public TimeDouble zero() {
		return TimeDouble.ZERO;
	}

	@Override
	public TimeDouble accumulate(TimeDouble older, TimeDouble newer) {
		return TimeDouble.accumulate(lambda, older, newer);
	}

}
