import React from 'react';
import { Row, Col, Tabs, Table, Modal, Button, } from 'antd';
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

class DBDisplay extends React.Component {
  state = {
    selectedDatabase: false,
    visible: false,
  }

  showModal = (selectedDatabase) => {
    this.setState({
      selectedDatabase,
      visible: true,
    });
  }

  columns = [{
    title: 'Name',
    dataIndex: 'name',
  }, {
    title: 'Location',
    dataIndex: 'location',
  }, {
    title: 'Max Size',
    dataIndex: 'size_in_gb',
    render: size => `${size}gb`
  }, {
    title: 'Help',
    render: (text, record) => <a href="#" onClick={() => this.showModal(record.name)}>Examples</a>
  }];

  render() {
    const { workspace, cluster } = this.props;
    return (
      <div>
        <Table
          bordered
          pagination={false}
          visible={this.state.visible}
          columns={this.columns}
          dataSource={workspace.data} />
        <Modal
          title="Code Examples"
          visible={this.state.visible}
          onCancel={() => this.setState({ visible: false })}
          footer={[<Button onClick={() => this.setState({ visible: false })}>OK</Button>]}>
          <CodeHelp cluster={cluster} databaseName={this.state.selectedDatabase} />
        </Modal>
      </div>
    );
  }
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
