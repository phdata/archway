import React from 'react';
import { Tabs, Row, Table, Modal, Button } from 'antd';
import PropTypes from 'prop-types';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';
import { connect } from 'react-redux';

import ValueDisplay from '../ValueDisplay';

const syntaxStyle = {
  padding: 10
};

const CodeHelp = ({ poolName }) => {
  const sparkJob =
    `$ spark-submit --class org.apache.spark.examples.SparkPi \\
                --master yarn \\
                --deploy-mode cluster \\
                --queue ${poolName} \\
                examples/jars/spark-examples*.jar \\
                10`;

  return (
    <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={solarizedDark}>
      {sparkJob}
    </SyntaxHighlighter>
  );
}

class ProcessingDisplay extends React.Component {
  state = {
    selectedPool: false,
    visible: false,
  }

  showModal = (selectedPool) => {
    this.setState({
      selectedPool,
      visible: true,
    });
  }

  columns = [{
    title: 'Name',
    dataIndex: 'pool_name',
  }, {
    title: 'Max Cores',
    dataIndex: 'max_cores',
  }, {
    title: 'Max Memory',
    dataIndex: 'max_memory_in_gb',
    render: size => `${size}gb`
  }, {
    title: 'Help',
    render: (text, record) => <a href="#" onClick={() => this.showModal(record.pool_name)}>Examples</a>
  }];

  render() {
    const { workspace } = this.props;
    return (
      <div>
        <Table
          bordered
          pagination={false}
          visible={this.state.visible}
          columns={this.columns}
          dataSource={workspace.processing} />
        <Modal
          title="Run SparkPi"
          visible={this.state.visible}
          onCancel={() => this.setState({ visible: false })}
          footer={[<Button onClick={() => this.setState({ visible: false })}>OK</Button>]}>
          <CodeHelp poolName={this.state.selectedPool} />
        </Modal>
      </div>
    );
  }
}

ProcessingDisplay.propTypes = {
  pool_name: PropTypes.string.isRequired,
  max_cores: PropTypes.string.isRequired,
  max_memory_in_gb: PropTypes.string.isRequired,
};

export default connect(
  state => state.workspaces.details, {}
)(ProcessingDisplay);
