import React from 'react';
import { List, Tabs, Row, Col } from 'antd';
import PropTypes from 'prop-types';

import ValueDisplay from './ValueDisplay';
import TabIcon from './TabIcon';

const ListHeader = ({ name }) => (
  <h3 style={{ textAlign: 'center' }}>
    {name}
  </h3>
);

const DBDisplay = ({
  name,
  size_in_gb,
  managers,
  readonly,
}) => {
  console.log(name);
  return (
    <div>
      <Row className="Data" type="flex" align="center">
        <Col span={18} style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-around' }}>
          <ValueDisplay label="database name">
            {name}
          </ValueDisplay>
          <ValueDisplay label="disk quota">
            {`${size_in_gb}gb`}
          </ValueDisplay>
        </Col>
        <Col span={6}>
          <List
            itemLayout="vertical"
            header={<ListHeader name="Workspace Managers" />}
            dataSource={managers}
            renderItem={item => <List.Item style={{ textAlign: 'center' }}>{item.username}</List.Item>}
          />
          <List
            header={<ListHeader name="Read Only Members" />}
            dataSource={readonly}
            renderItem={item => <List.Item>{item.name}</List.Item>}
          />
        </Col>
      </Row>
    </div>
  );
};

DBDisplay.propTypes = {
  name: PropTypes.string.isRequired,
  size_in_gb: PropTypes.number.isRequired,
  managers: PropTypes.arr,
  readonly: PropTypes.arr,
};

export default DBDisplay;
