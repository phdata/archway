import React from 'react';
import { Tabs, Row, Col } from 'antd';
import PropTypes from 'prop-types';
import SyntaxHighlighter from 'react-syntax-highlighter';
import {solarizedDark} from 'react-syntax-highlighter/styles/hljs';
import { connect } from 'react-redux';

import ValueDisplay from './ValueDisplay';
import TabIcon from './TabIcon';

const syntaxStyle = {
  padding: 10
};

const ProcessingItem = ({ pool: { pool_name, max_cores, max_memory_in_gb }}) => {
  const sparkJob =
`$ spark-submit --class org.apache.spark.examples.SparkPi \\
                --master yarn \\
                --deploy-mode cluster \\
                --queue ${pool_name} \\
                examples/jars/spark-examples*.jar \\
                10`;
  return (
    <div>
      <Row type="flex" justify="space-around">
        <ValueDisplay label="queue name">
          {pool_name}
        </ValueDisplay>
        <ValueDisplay label="max cores">
          {max_cores}
        </ValueDisplay>
        <ValueDisplay label="max memory">
          {max_memory_in_gb}gb
        </ValueDisplay>
      </Row>

      <h2>Run SparkPi In Your New YARN Queue</h2>
      <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={solarizedDark}>
        {sparkJob}
      </SyntaxHighlighter>
    </div>
  );
}

const ProcessingDisplay = ({ activeWorkspace }) => {
  if (activeWorkspace.processing.length == 1)
    return <ProcessingItem pool={activeWorkspace.processing[0]} />;
  return (
    <Tabs>
      {activeWorkspace.processing.map(processing => (
        <Tabs.TabPane tab={<TabIcon name={`${processing.pool_name}`} />} key={`pool-${processing.pool_name}`}>
          <ProcessingItem pool={processing} />
        </Tabs.TabPane>
      ))}
    </Tabs>
  );
}

ProcessingDisplay.propTypes = {
  pool_name: PropTypes.string.isRequired,
  max_cores: PropTypes.string.isRequired,
  max_memory_in_gb: PropTypes.string.isRequired,
};

export default connect(
  state => state.workspaces,
  { }
)(ProcessingDisplay);
