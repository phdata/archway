import * as React from 'react';
import {Card} from 'antd';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';

interface Props {
  databaseName: string
  poolName: string
  services: any
}

const PersonalWorkspace = ({ databaseName = 'user_benny', poolName = 'user.benny', services }: Props) => {
  const code = `
  from impala.dbapi import connect
  conn = connect(host='${services && services.impala.hiveServer2[0].host}', port=${services && services.impala.hiveServer2[0].port}, database='${databaseName}')
  cursor = conn.cursor()
  cursor.execute('SELECT * FROM mytable LIMIT 100')
  print cursor.description  # prints the result set's schema
  results = cursor.fetchall()
  `;
  const sparkSubmit = `
  $ spark-submit --class org.apache.spark.examples.SparkPi \\
              --master yarn \\
              --deploy-mode cluster \\
              --queue ${poolName} \\
              examples/jars/spark-examples*.jar \\
              10
  `;
  return (
    <Card bodyStyle={{ display: 'flex' }} title="Your Personal Workspace">
      <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
        <h3>Connect to Your Database With <a target="_blank" rel="noreferrer noopener" href="https://github.com/cloudera/impyla">impyla</a></h3>
        <SyntaxHighlighter language="python" style={solarizedDark}>{code}</SyntaxHighlighter>
      </Card.Grid>
      <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
        <h3>Submit SparkPi to your personal queue on YARN</h3>
        <SyntaxHighlighter language="shell" style={solarizedDark}>{sparkSubmit}</SyntaxHighlighter>
      </Card.Grid>
    </Card>
  );
}

export default PersonalWorkspace;