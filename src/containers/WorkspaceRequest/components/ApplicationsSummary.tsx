import * as React from 'react';
import { Table } from 'antd';
import { Workspace, Application } from '../../../models/Workspace';

interface Props {
  workspace?: Workspace;
}

function tableDataForApplication(application: Application) {
  return [
    { title: 'Consumer Group', value: application.consumer_group },
    { title: 'Group Common Name', value: application.group.common_name },
    { title: 'Group Distinguished Name', value: application.group.distinguished_name },
    { title: 'Group Sentry Role', value: application.group.sentry_role },
  ];
}

const ApplicationsSummary = ({ workspace }: Props) => {
  if (!workspace) {
    return null;
  }

  return (
    <div style={{ textAlign: 'left', paddingLeft: 24, paddingRight: 24, paddingTop: 24 }}>
      <h2>Applications</h2>
      {workspace.applications.map((application: Application) => (
        <div key={application.id}>
          <h3>{application.name}</h3>
          <Table
            pagination={false}
            dataSource={tableDataForApplication(application)}
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

export default ApplicationsSummary;
