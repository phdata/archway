import React from 'react';
import { Tabs, Row, Col } from 'antd';
import PropTypes from 'prop-types';
import SyntaxHighlighter from 'react-syntax-highlighter';
import {tomorrowNightBlue} from 'react-syntax-highlighter/styles/hljs';

import ValueDisplay from './ValueDisplay';
import TabIcon from './TabIcon';

const syntaxStyle = {
  padding: 10
};

const ProcessingDisplay = ({ pool_name, max_cores, max_memory_in_gb }) => {
  const sparkJob =
`$ spark-submit --class org.apache.spark.examples.SparkPi \\
                --master yarn \\
                --deploy-mode cluster \\
                --queue $pool_name \\
                examples/jars/spark-examples*.jar \\
                10`;
  return (
    <Tabs.TabPane tab={<TabIcon icon="dashboard" name={`${pool_name} (yarn)`} />} key={`pool-${pool_name}`}>
      <Row className="Processing" type="flex" align="middle">
        <Col span={24}>
        <Row>
          <Col span={8}>
            <ValueDisplay label="queue name">
              {pool_name}
            </ValueDisplay>
          </Col>
          <Col span={8}>
            <ValueDisplay label="max cores">
              {max_cores}
            </ValueDisplay>
          </Col>
          <Col span={8}>
            <ValueDisplay label="max memory">
              {max_memory_in_gb}gb
            </ValueDisplay>
          </Col>
        </Row>
        <Row style={{ paddingTop: 10 }}>
          <h4>Run SparkPi In Your New YARN Queue</h4>
          <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={tomorrowNightBlue}>
            {sparkJob}
          </SyntaxHighlighter>
        </Row>
        </Col>
      </Row>
    </Tabs.TabPane>
  );
}

ProcessingDisplay.propTypes = {
  pool_name: PropTypes.string.isRequired,
  max_cores: PropTypes.string.isRequired,
  max_memory_in_gb: PropTypes.string.isRequired,
};

export default ProcessingDisplay;
