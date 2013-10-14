
**Example Connectors for Acunu Analytics**

Copyright (c) 2013 Acunu. See LICENSE file for terms of use.

Requires Acunu Analytics >=v5.0.

This repository contains some example connectors to make it easy to get started
 with the [Acunu Analytics connectors framework](http://www.acunu.com/documentation.html#%2Fv5.0%2Fdeveloper%2Fplugins.html).

Note that the APIs are 'beta' in Analytics v5.0: they may change as we expand
 the range of connectors available and in response to feedback from users. 

There are several different types of connector. This repo contains the following
 examples, in the matching sub-directories:

* `aggregates`: An exponential moving average aggregate
* `alert-sinks`: An alert sink that records alerts in a file
* `decoders`: XXX
* `ingesters`: XXX
* `preprocessors`: A preprocessor that flattens complex JSON objects

We welcome questions, suggestions, feedback, patches and problems reports. 

Please get in touch at [http://support.acunu.com/](http://support.acunu.com/)


**Building the Examples**

First check `$JBIRD_HOME` points to your Acunu Analytics installation:

    export JBIRD_HOME=/usr/share/acunu/jbird/

You'll need the JARs that are supplied with Analytics, especially the Analytics
Connectors API, `analytics-connectors.jar`.

You can build the connectors together, from the root directory of the
repository, or just one of the connectors by running these commands from 
one of the sub-directories.

To build the connectors, run:

    make
  
And to install the connectors into the JBIRD_HOME/plugins directory:

    sudo make install
  
Or to install them somewhere else:

    PLUGINS_DIR=/my/path/to/plugins/ make install
  
You'll need to restart Analytics for it to pick up the new connectors jars.


** Exponential Moving Average Aggregate **

This example provides a custom aggregate function that represents an exponential 
weighted moving average. It demonstrates how you can develop and use new custom 
functions in Acunu's cubing and query framework.  

The underlying counter is a pair of time and value; the combination of two 
counters is to scale the older one down by a multiplier depending exponentially
on the time difference between the two. The rate of decay is controlled by the
parameter 'lambda' passed in via the constructor.

Using the command line tool `summer`, you can create a cube using AQL like this: 

    create cube select USERDEFINED('com.acunu.analytics.example.ExponentialMovingAverage', 't', 'val', '0.1') from tbl;

Then queries on that cube can be issued like this:

    select USERDEFINED('com.acunu.analytics.example.ExponentialMovingAverage', 't', 'val', '0.1') from tbl;

See the `aggregates/conf/` directory for further AQL snippets that demonstrate 
this.

See the [Acunu connectors API documentation](http://www.acunu.com//documentation.html#%2Fv5.0%2Fdeveloper%2Fplugins.html%2Faggregates) 
for further info.


** File Alert Sink **

In your `alert.yaml` config file, create the sink by creating a section 
like this:

    sinks:
      file_example:
        classname: com.acunu.analytics.alerts.FileAlertSink

Next, make an alert configuration use the sink by setting the property `sink` 
to `file_example`. Here's an example config which merely requires the table 
`tbl` to exist to start firing alerts:
 
    <pre>
    alerts:
      - name: test_alert
        sink: file_example
        query: "select eventcount from tbl"
        frequency_seconds: 10
        body: "Hello World"

To see this in action, run the Acunu Analytics alert monitor, `alertmonitor` -- 
every 10 seconds a file will be created with `body: Hello World` in it, and 
the number of the alert at the top.

See the [Acunu alerts documentation](http://www.acunu.com//documentation.html#%2Fv5.0%2Fdeveloper%2Falerts.html) 
for more details.


** XXX Ingester and Regex Decoder **

This decoder accepts unstructured text and allows you to specify a regular 
expression to extract portions of the text and name them to create a 'map-like' 
event that Acunu Analytics can process.

In version 5.0, decoders cannot be used via the HTTP API. Instead, you need to 
set up an ingester and a flow, and specify the decoder there.

For example:

    CREATE FLOW my_flow INGESTER my_ingester DECODER 'com.acunu.analytics.example.RegexDecoder' 
      RECEIVER my_table PROPERTIES regex = '<regex>', fields = '<comma-separated-group-names>';

** JSON Flattening Preprocessor **

This Java preprocessor will flatten out any JSON event sent to it of the form:

    {"user":{"name":...,"start_date":...},"sale":{"qty":...,"value":...}}

to: 

    {"user_name":...,"user_start_date":...,"sale_qty":...,"sale_value":...}

Create it from `summer` with:

    CREATE PREPROCESSOR flat JAVA 'com.acunu.analytics.example.FlattenPreprocessor' receiver='pptbl';    

You'll first need to create a table `pptbl` to receive the flattened events. See the 
`preprocessors/conf/schema.aql` for you an example.

Finally, POST events to `http://hostname:8080/analytics/api/data/flat`. An example 
is given in ``preprocessors/conf/load_flat`. You should be able to create cubes 
and see the raw events arriving in your destination table flattened. 
