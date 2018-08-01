import React from 'react';
import { connect } from 'react-redux';
import { List, Row, Col, Form, Select, Input, Tabs, Avatar, Icon, Button, Popconfirm } from 'antd';
import PropTypes from 'prop-types';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';

import ValueDisplay from '../ValueDisplay';

const CodeHelp = ({ cluster, databaseName }) => {
  const impala = `$ impala-shell -i ${cluster.services.IMPALA.host}:21000 -d ${databaseName}`;
  const jdbc = `jdbc:impala://${cluster.services.IMPALA.host}:21050/${databaseName}`;
  const beeline = `$ beeline -u 'jdbc:hive2://${cluster.services.HIVESERVER2.host}:10000/${databaseName};auth=noSasl'`;
  return (
    <Tabs animated={false}>
      <Tabs.TabPane tab="JDBC" key="jdbc">
        <SyntaxHighlighter language="sql" style={solarizedDark}>
          {jdbc}
        </SyntaxHighlighter>
      </Tabs.TabPane>
      <Tabs.TabPane tab="Impala" key="impala">
        <SyntaxHighlighter language="shell" style={solarizedDark}>
          {impala}
        </SyntaxHighlighter>
      </Tabs.TabPane>
      <Tabs.TabPane tab="Beeline" key="beeline">
        <SyntaxHighlighter language="shell" style={solarizedDark}>
          {beeline}
        </SyntaxHighlighter>
      </Tabs.TabPane>
    </Tabs>
  );
}

const DatabaseItem = ({ database: { name, size_in_gb, location }, cluster }) => (
  <div>
    <Row type="flex" justify="space-around">
      <Col span={8}>
        <ValueDisplay label="hdfs location">
          {location}
        </ValueDisplay>
      </Col>
      <Col span={8}>
        <ValueDisplay label="database name">
          {name}
        </ValueDisplay>
      </Col>
      <Col span={8}>
        <ValueDisplay label="disk quota">
          {`${size_in_gb}gb`}
        </ValueDisplay>
      </Col>
    </Row>
    <h2>Connect to Your Database</h2>
    { (cluster.services) && <CodeHelp cluster={cluster} databaseName={name} /> }
  </div>
)

const DBDisplay = ({ workspace, cluster }) => {
  if (workspace.data.length == 1)
    return <DatabaseItem database={workspace.data[0]} cluster={cluster} />;
  return (
    <Tabs animated={false}>
      {workspace.data.map(db => (
        <Tabs.TabPane tab={db.name} key={db.id}>
          <DatabaseItem database={db} cluster={cluster} />
        </Tabs.TabPane>
      ))}
    </Tabs>
  );
}

DBDisplay.propTypes = {
  database: PropTypes.shape({
    name: PropTypes.string.isRequired,
    size_in_gb: PropTypes.number.isRequired,
  }),
  cluster: PropTypes.shape({
    services: PropTypes.object.isRequired,
  })
};

export default DBDisplay;
