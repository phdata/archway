import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/dist/styles/hljs';
import Label from './Label';

interface Props {
  host?: string;
  port?: number;
  namespace: string;
}

const SetupHelp = ({ host, port, namespace }: Props) => (
  <div style={{ padding: '4px' }}>
    <Label>step 2. create</Label>
    <p
      style={{
        marginBottom: '0.5em',
        fontSize: '12px',
        textAlign: 'center',
      }}
    >
      create an app that interacts with your workspace
    </p>
    <SyntaxHighlighter language="python" style={tomorrowNightEighties}>
      {`from impala.dbapi
import connect
conn = connect(
  host = '${host || 'worker1.example.com'}',
  port = ${port || '10000'},
  database = '${namespace}'
)
cursor = conn.cursor()
cursor.execute('SELECT * FROM mytable LIMIT 100')
print cursor.description # prints the result set’ s schema
results = cursor.fetchall()`}
    </SyntaxHighlighter>
  </div>
);

export default SetupHelp;
