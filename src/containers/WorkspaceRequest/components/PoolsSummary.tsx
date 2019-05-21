import * as React from 'react';
import { Table } from 'antd';
import { Workspace, ResourcePool } from '../../../models/Workspace';

interface Props {
  workspace?: Workspace;
}

function tableDataForPool(pool: ResourcePool) {
  return [
    { title: 'Max Cores', value: pool.max_cores },
    { title: 'Max Memory In GB', value: pool.max_memory_in_gb },
  ];
}

const PoolsSummary = ({ workspace }: Props) => {
  if (!workspace) {
    return null;
  }

  return (
    <div style={{ textAlign: 'left', paddingLeft: 24, paddingRight: 24, paddingTop: 24 }}>
      <h2>Resource Pools</h2>
      {workspace.processing.map((pool: ResourcePool) => (
        <div key={pool.id}>
          <h3>{pool.pool_name}</h3>
          <Table
            pagination={false}
            dataSource={tableDataForPool(pool)}
          >
            <Table.Column
              title="Attribute"
              dataIndex="title"
              key="title"
              width="50%"
            />
            <Table.Column
              title="Value"
              dataIndex="value"
              key="value"
              width="50%"
            />
          </Table>
        </div>
      ))}
    </div>
  );
};

export default PoolsSummary;
