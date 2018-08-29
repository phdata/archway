import React from 'react';
import PropTypes from 'prop-types';
import { Card, List, Avatar } from 'antd';

import Color from './Colors';

const WorkspaceStatus = () => (
  <div
    style={{
      width: '50%',
      backgroundColor: 'green',
      display: 'inline-block',
      color: 'white',
      marginTop: 5
    }}>
    Approved
  </div>
);

const detailStyle = {
  width: '25%',
  textAlign: 'center',
  boxShadow: 'none'
};

const WorkspaceDetail = ({ label, value }) => (
  <Card.Grid style={detailStyle}>
    <h2 style={{ color: Color.Orange.rgb().string(), fontWeight: 100, textAlign: 'center' }}>
      {value}
    </h2>
    <h5 style={{ textAlign: 'center' }}>
      {label}
    </h5>
  </Card.Grid>
)

const WorkspaceListItem = ({ workspace, onSelected }) => {
  const gridStyle = { width: '33%', textAlign: 'center' };
  const {
    id,
    name,
  } = workspace;
  return (
    <List.Item>
      <Card
        style={{ textAlign: 'center' }}
        onClick={() => onSelected(`/workspaces/${id}`)}
        hoverable>
        <Avatar icon="user" />
        <h2 style={{ textAlign: 'center' }}>{name}</h2>
        <h4>partially approved</h4>
        <WorkspaceDetail label="DBs" value={1} />
        <WorkspaceDetail label="Pools" value={1} />
        <WorkspaceDetail label="Topics" value={0} />
        <WorkspaceDetail label="Apps" value={1} />
      </Card>
    </List.Item>
  );
};

WorkspaceListItem.propTypes = {
  item: PropTypes.shape({
    id: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    requester: PropTypes.string.isRequired,
    requested_date: PropTypes.string.isRequired,
    single_user: PropTypes.bool.isRequired,
  }).isRequired,
  onSelected: PropTypes.func.isRequired,
};

export default WorkspaceListItem;