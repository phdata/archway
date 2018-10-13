import { Card } from 'antd';
import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/styles/hljs';
import { Label } from '.';

interface Props {
  queue: string;
}

const RunHelp = ({ queue }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>step 3. run</Label>
    <SyntaxHighlighter language="shell" style={tomorrowNightEighties}>
        {`$ spark-submit --class org.apache.spark.examples.SparkPi \\
  --master yarn \\
  --deploy-mode cluster \\
  --queue ${queue} \\
  examples/jars/spark-examples*.jar \\
  10'`}
    </SyntaxHighlighter>
  </Card>
);

export default RunHelp;
