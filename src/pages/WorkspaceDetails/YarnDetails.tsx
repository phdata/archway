import * as React from 'react';
import { HiveTable, YarnApplication } from '../../types/Workspace';
import { List } from 'antd';

interface Props {
  poolName: string;
  applications?: YarnApplication[];
}

const renderApplication = (yarnApplication: YarnApplication) => (
  <div>{yarnApplication.name}</div>
);

const YarnDetails = ({ poolName, applications }: Props) => (
  <List
    dataSource={applications}
    renderItem={renderApplication} />
);

export default YarnDetails;
