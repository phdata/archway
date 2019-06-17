import * as React from 'react';
import { Card, Dropdown, Menu, Icon } from 'antd';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/dist/styles/hljs';
import CardHeader from './CardHeader';

interface Props {
  location?: string;
  namespace?: string;
  host?: string;
  port?: number;
  queue?: string;
}

const instructions = {
  create_table: 'CREATE A TABLE',
  create_app: 'CREATE AN APPLICATION',
  run_app: 'RUN APPLICATION',
};

class InstructionCard extends React.Component<Props> {
  public state = {
    instruction: 'create_table',
  };

  public handleChangeInstruction(key: string) {
    this.setState({ instruction: key });
  }

  public render() {
    const { location, namespace, host, port, queue } = this.props;
    const { instruction } = this.state;

    return (
      <Card bordered>
        <CardHeader>
          HOW WOULD I... &nbsp;
          <Dropdown
            overlay={
              <Menu onClick={({ key }) => this.handleChangeInstruction(key)}>
                <Menu.Item key="create_table">CREATE A TABLE</Menu.Item>
                <Menu.Item key="create_app">CREATE AN APPLICATION</Menu.Item>
                <Menu.Item key="run_app">RUN APPLICATION</Menu.Item>
              </Menu>
            }
            trigger={['click']}
          >
            <a href="#">
              {' '}
              {/* eslint-disable-line */}
              {instructions[instruction]}
              <Icon type="caret-down" style={{ fontSize: 14, marginLeft: 4 }} />
            </a>
          </Dropdown>
        </CardHeader>
        {instruction === 'create_table' && !!location && !!namespace && (
          <SyntaxHighlighter language="sql" style={{ overflow: 'auto', ...tomorrowNightEighties }}>
            {`CREATE TABLE ${namespace}.new_data_landing
  LOCATION '${location}/new_data/landing'`}
          </SyntaxHighlighter>
        )}
        {instruction === 'create_app' && !!host && !!port && (
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
        )}
        {instruction === 'run_app' && !!queue && (
          <SyntaxHighlighter language="shell" style={tomorrowNightEighties}>
            {`$ spark-submit --class org.apache.spark.examples.SparkPi \\
    --master yarn \\
    --deploy-mode cluster \\
    --queue ${queue} \\
    examples/jars/spark-examples*.jar \\
    10'`}
          </SyntaxHighlighter>
        )}
      </Card>
    );
  }
}

export default InstructionCard;
