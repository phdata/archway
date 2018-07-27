import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import {tomorrowNightBlue} from 'react-syntax-highlighter/styles/hljs';
import Spinner from "./Spinner";
import MetricDisplay from "./MetricDisplay";
import "./ProcessingDisplay.css"

const syntaxStyle = {
  marginTop: 10,
  marginBottom: 10,
  fontSize: 18,
  padding: 20
};

const codeHelp = ({ pool_name }) => {
  const sparkJob = `
$ spark-submit --class org.apache.spark.examples.SparkPi \\
                   --master yarn \\
                   --deploy-mode cluster \\
                   --queue ${processing.pool_name} \\
                   examples/jars/spark-examples*.jar \\
                   10
                    `;
  return (
    <div>
      <h4>Submit a Spark Job</h4>
      <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={tomorrowNightBlue}>
        {sparkJob}
      </SyntaxHighlighter>
    </div>
  )
}

const ProcessingDisplay = ({processing, cluster}) => {
  let content;
  if (processing && processing.pool_name && cluster.services) {
    content = (<div className="ProcessingDisplay-Display">
      <div className="ProcessingDisplay-Display-left">
      </div>
      <div className="ProcessingDisplay-Display-right">
        <MetricDisplay metric={processing.max_cores} label="max core(s)"/>
        <MetricDisplay metric={processing.max_memory} label="max memory"/>
      </div>
    </div>);
  } else {
    content = <Spinner>Provisioning...</Spinner>
  }
  return (<div className="ProcessingDisplay">
    <h2><i className="fa fa-gears"/>Processing {processing ? `(${processing.pool_name})` : ""}</h2>
    {content}
  </div>)
};

export default ProcessingDisplay;
