import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/dist/styles/hljs';
import Label from './Label';

interface Props {
  queue: string;
}

const RunHelp = ({ queue }: Props) => (
  <div style={{ padding: '4px' }}>
    <Label>step 3. run</Label>
    <p
      style={{
        marginBottom: '0.5em',
        fontSize: '12px',
        textAlign: 'center',
      }}
    >
      run your application using your own resource pool
    </p>
    <SyntaxHighlighter language="shell" style={tomorrowNightEighties}>
      {`$ spark-submit --class org.apache.spark.examples.SparkPi \\
  --master yarn \\
  --deploy-mode cluster \\
  --queue ${queue} \\
  examples/jars/spark-examples*.jar \\
  10'`}
    </SyntaxHighlighter>
  </div>
);

export default RunHelp;
