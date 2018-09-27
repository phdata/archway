import * as React from 'react';
import { Card, Row, Col } from 'antd';
import Label from './Label';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/styles/hljs';

interface Props {
  liaison: string;
}

const SetupHelp = ({}: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>getting started</Label>
    <Row gutter={10}>
      <Col span={24} lg={12}>
        <h3>Write your application...</h3>
        <SyntaxHighlighter language="python" style={tomorrowNightEighties}>
        {`
          from impala.dbapi
          import connect
          conn = connect(
            host = ‘\$ {
              host
            }’, port = \$ {
              port
            }, database = ‘\$ {
              dbName
            }’)
          cursor = conn.cursor()
          cursor.execute(‘SELECT * FROM mytable LIMIT 100’)
          print cursor.description # prints the result set’ s schema
          results = cursor.fetchall()
        `}
        </SyntaxHighlighter>
      </Col>
      <Col span={24} lg={12}>
        <h3>Then just run it!</h3>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <SyntaxHighlighter language="shell" style={tomorrowNightEighties}>
          {`
            $ spark-submit --class org.apache.spark.examples.SparkPi \\
            --master yarn \\
            --deploy-mode cluster \\
            --queue poolName \\
            examples/jars/spark-examples*.jar \\
            10
          `.replace('           ', '')}
          </SyntaxHighlighter>
          <h3>...then refresh the page to see your new application above!</h3>
        </div>
      </Col>
    </Row>
  </Card>
);

export default SetupHelp;
