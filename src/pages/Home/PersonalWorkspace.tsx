import { Button, Card, Spin } from 'antd';
import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';
import { Workspace } from '../../types/Workspace';

interface Props {
    workspace: Workspace;
    services: any;
    loading: boolean;
    requestWorkspace: () => void;
}

const PersonalWorkspace = ({ workspace, services, requestWorkspace, loading }: Props) => {
  const host = services.impala && services.impala.hiveServer2[0].host;
  const port = services.impala && services.impala.hiveServer2[0].port;
  const dbName = workspace.data && workspace.data[0].name;
  const code = `
  from impala.dbapi import connect
  conn = connect(host='${host}', port=${port}, database='${dbName}')
  cursor = conn.cursor()
  cursor.execute('SELECT * FROM mytable LIMIT 100')
  print cursor.description  # prints the result set's schema
  results = cursor.fetchall()
  `;
  const sparkSubmit = `
  $ spark-submit --class org.apache.spark.examples.SparkPi \\
              --master yarn \\
              --deploy-mode cluster \\
              --queue ${workspace.processing && workspace.processing[0].pool_name} \\
              examples/jars/spark-examples*.jar \\
              10
  `;
  // hue/metastore/tables/flights
  return (
    <div style={{ position: 'relative' }}>
      <div
        style={{
          position: 'absolute',
          zIndex: 999,
          display: workspace.data ? 'none' : 'flex',
          width: '100%',
          height: '100%',
          justifyContent: 'center',
          alignItems: 'center',
        }}>
        {loading && <Spin />}
        {!loading && (
          <Card bodyStyle={{ textAlign: 'center' }}>
            <h3>You don't have a personal workspace yet!</h3>
            <Button type="primary" onClick={requestWorkspace}>Create One Now</Button>
          </Card>
        )}
      </div>
      <div style={{ filter: workspace.data ? 'none' : 'blur(.4rem)', transition: '1s ease' }}>
        <Card bodyStyle={{ display: 'flex' }} title="Your Personal Workspace">
          <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
            <h3>
              Connect to Your Database With{' '}
              <a target="_blank" rel="noreferrer noopener" href="https://github.com/cloudera/impyla">
                impyla
              </a>
            </h3>
            <SyntaxHighlighter language="python" style={solarizedDark}>{code}</SyntaxHighlighter>
          </Card.Grid>
          <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
            <h3>Submit SparkPi to your personal queue on YARN</h3>
            <SyntaxHighlighter language="shell" style={solarizedDark}>{sparkSubmit}</SyntaxHighlighter>
          </Card.Grid>
        </Card>
      </div>
    </div>
  );
};

export default PersonalWorkspace;
