import * as React from 'react';
import { Table } from 'antd';
import { Workspace, HiveAllocation } from '../../../models/Workspace';

interface Props {
  workspace?: Workspace;
}

function tableDataForDatabase(database: HiveAllocation) {
  const data = [
    { title: 'Location', value: database.location },
    { title: 'Size In GB', value: database.size_in_gb },
    { title: 'Managing Group Common Name', value: database.managing_group.group.common_name },
    { title: 'Managing Group Distinguished Name', value: database.managing_group.group.distinguished_name },
    { title: 'Managing Group Sentry Role', value: database.managing_group.group.sentry_role },
  ];
  if (database.readonly_group) {
    data.push({ title: 'Readonly Group Common Name', value: database.readonly_group.group.common_name });
    data.push({ title: 'Readonly Group Distinguished Name', value: database.readonly_group.group.distinguished_name });
    data.push({ title: 'Readonly Group Sentry Role', value: database.readonly_group.group.sentry_role });
  }

  return data;
}

const DatabasesSummary = ({ workspace }: Props) => {
  if (!workspace) {
    return null;
  }

  return (
    <div style={{ textAlign: 'left', paddingLeft: 24, paddingRight: 24, paddingTop: 24 }}>
      <h2>Databases</h2>
      {workspace.data.map((database: HiveAllocation) => (
        <div key={database.id}>
          <h3>{database.name}</h3>
          <Table
            pagination={false}
            dataSource={tableDataForDatabase(database)}
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

export default DatabasesSummary;
