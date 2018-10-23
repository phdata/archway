import { Card } from 'antd';
import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/styles/hljs';
import Label from './Label';

interface Props {
  host: string;
  port: number;
  namespace: string;
}

const SetupHelp = ({host, port, namespace}: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>step 2. create</Label>
    <SyntaxHighlighter language="python" style={tomorrowNightEighties}>
        {`from impala.dbapi
import connect
conn = connect(
  host = '${host}',
  port = ${port},
  database = '${namespace}'
)
cursor = conn.cursor()
cursor.execute('SELECT * FROM mytable LIMIT 100')
print cursor.description # prints the result set’ s schema
results = cursor.fetchall()`}
    </SyntaxHighlighter>
  </Card>
);

export default SetupHelp;
