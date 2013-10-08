package com.acunu.analytics.example;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acunu.analytics.conf.ConfigProperties;
import com.acunu.analytics.Context;
import com.acunu.analytics.alerts.AlertServer;
import com.acunu.analytics.alerts.AlertSink;

/**
 * An alert sink which saves alerts to files based on an incrementing counter.
 * 
 * To use this alert sink:
 * <ol>
 * <li>Compile it, make a jar, and copy the jar to $JBIRD_HOME/plugins</li>
 * <li>Modify the alert-config.yaml sink declaration to include this class under
 * a suitable alias, e.g. "file_example":
 * 
 * <pre>
 * sinks:
 *    file_example:
 *       classname: com.acunu.analytics.example.FileAlertSink
 * </pre>
 * 
 * </li>
 * <li>
 * Make an alert confilguration use the sink by setting the property 'sink' to
 * 'file_example'. Here's an example config which merely requires the table
 * 'tbl' to exist to start firing alerts:
 * 
 * <pre>
 * alerts:
 *    - name: test_alert
 *      sink: file_example
 *      query: "select eventcount from tbl"
 *      frequency_seconds: 10
 *      body: "Hello World"
 * </pre>
 * 
 * </li>
 * <li>
 * Run the alert monitor -- every 10 seconds a file will be created with
 * "body: Hello World" in it, and the number of the alert at the top.</li>
 * </ol>
 * 
 * @author abyde
 */
public class FileAlertSink extends AlertSink {
	private static final Logger logger = LoggerFactory.getLogger(FileAlertSink.class);

	private final String dirStr;
	private String fileRootStr = "alert";
	private AlertServer server;

	private final AtomicLong alertCounter = new AtomicLong(0l);

	private File dir;

	/**
	 * Extract directory from sink parameters.
	 */
	public FileAlertSink(String name, Context context) {
		super(name, context);
		this.server = context.getAlertServer();
		dirStr = context.getConfig().getString("dir", "/tmp/alertstore");
	}

	/**
	 * This method is called once before any alerts are sent.
	 */
	@Override
	public void init() throws IOException {
		try {
			dir = new File(dirStr);
			if (!dir.exists())
				dir.mkdir();
		} catch (Exception exn) {
			throw new IOException(exn);
		}

		if (!dir.isDirectory())
			throw new IOException("Alert directory '" + dirStr + "' is not a directory");
		else if (!dir.canWrite())
			throw new IOException("Cannot write to alert directory '" + dirStr + "'");
	}

	/**
	 * This method is expected to operate asynchronously; the success or
	 * otherwise of sending the alert can be reported to the server using
	 * {@link AlertServer#reportAlert(Map)}.
	 */
	@Override
	public void sendAlert(String destination, String body, Map<String, Object> vars) throws IOException {
		long alertCount = alertCounter.incrementAndGet();
		logger.info("Event [" + alertCount + "]");
		File alertFile = new File(dir, (fileRootStr + alertCount));
		if (alertFile.exists())
			alertFile.delete();

		alertFile.createNewFile();
		PrintWriter writer = new PrintWriter(alertFile);
		writer.println("Alert " + alertCount);
		writer.println("body = " + body);
		writer.println("vars = " + vars);
		writer.flush();

		writer.close();

		// report to the server that we have succeeded.
		server.reportAlert(vars);
	}

	/**
	 * This method is called on exit in order to allow the sink to free
	 * resources.
	 */
	@Override
	public void shutdown() throws IOException {
	}
}
