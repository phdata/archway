import React from 'react';
import { Tabs, Row, Col } from 'antd';
import PropTypes from 'prop-types';

import ValueDisplay from './ValueDisplay';
import TabIcon from './TabIcon';

const ProcessingDisplay = ({ pool_name, max_cores, max_memory_in_gb }) => (
  <Tabs.TabPane tab={<TabIcon icon="dashboard" name={`${pool_name} (yarn)`} />} key={`pool-${pool_name}`}>
    <Row className="Processing" type="flex" align="middle">
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
          {max_memory_in_gb}
        </ValueDisplay>
      </Col>
    </Row>
  </Tabs.TabPane>
);

ProcessingDisplay.propTypes = {
  pool_name: PropTypes.string.isRequired,
  max_cores: PropTypes.string.isRequired,
  max_memory_in_gb: PropTypes.string.isRequired,
};

export default ProcessingDisplay;
