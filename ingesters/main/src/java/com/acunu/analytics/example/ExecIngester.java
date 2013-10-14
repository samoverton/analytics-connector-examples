package com.acunu.analytics.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.Context;
import com.acunu.analytics.Flow;
import com.acunu.analytics.ingest.AbstractIngester;
import com.acunu.analytics.ingest.FlowSource;
import com.acunu.analytics.ingest.IngestException;

/**
 * An example ingester for ingesting the output of commands into Acunu
 * Analytics. Each flow executes a command, attaches to the stdout of the
 * process, and treats lines of text received as events.
 * 
 * @author tmoreton
 * 
 */
public class ExecIngester extends AbstractIngester {

	/**
	 * Initialize and start-up the Ingester.
	 */
	public ExecIngester(String name, Context context) throws IngestException {
		super(name, context);
	}

	/**
	 * Build a new {@link FlowSource} for the given {@link Flow}.
	 */
	@Override
	protected FlowSource<? extends AbstractIngester> createFlowSource(Flow flow) throws IngestException {
		return new ExecFlowSource(this, flow);
	}

	/**
	 * A {@link FlowSource} associated with a single exec'd process.
	 */
	public static class ExecFlowSource extends FlowSource<ExecIngester> {

		private Logger logger = LoggerFactory.getLogger(ExecFlowSource.class);

		private BufferedReader reader;

		/**
		 * The process command line, supplied in the flow properties.
		 */
		private String command;

		/**
		 * The executed process
		 */
		private Process process;

		public static final String FIELD_EXEC_COMMAND = "command";

		public static final int MAX_BATCH_SIZE = 1; // 50;

		protected ExecFlowSource(ExecIngester ingester, Flow flow) {
			super(ingester, flow);

			this.command = flow.getProperties().getString(FIELD_EXEC_COMMAND);

			if (this.command == null)
				throw new IllegalArgumentException("Missing required property '" + FIELD_EXEC_COMMAND + "'");
		}

		/**
		 * Start this flow.
		 */
		@Override
		public synchronized void start() throws IngestException {

			logger.info("Exec'ing process {}", command);

			// Exec the process and set up the stdout reader.
			try {
				final String[] commandArgs = this.command.split("\\s+");
				this.process = new ProcessBuilder(commandArgs).start();
				this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			} catch (IOException e) {
				throw new IngestException(e.getMessage(), e);
			}

			// Call superclass's start() method LAST, to start the threadpool.
			super.start();
		}

		/**
		 * Stop this flow.
		 */
		@Override
		public synchronized void stop() throws InterruptedException {

			// Stop the threadpool first.
			super.stop();

			// Terminate the process and stop events.
			logger.info("Stopping process {}", command);

			if (process != null) {
				try {
					process.destroy();
					process.waitFor();
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} finally {
					this.process = null;
				}
			}
		}

		/**
		 * Ingest some more events. This method can return zero events, a single
		 * event, or a batch.
		 */
		@Override
		protected List<?> ingestSomeMore() throws IngestException, InterruptedException {

			String line = null;
			List<Object> events = new ArrayList<Object>();

			try {
				while (events.size() < MAX_BATCH_SIZE && (line = reader.readLine()) != null) {
					events.add(line);
				}
			} catch (IOException e) {
				throw new IngestException(e);
			}

			return events;
		}
	}
}
