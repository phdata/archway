import * as React from 'react';
import { HiveTable } from '../../types/Workspace';
import { List } from 'antd';

interface Props {
  namespace: string;
  tables?: HiveTable[];
}

const renderTable = (hiveTable: HiveTable) => (
  <div>{hiveTable.name}</div>
);

const HiveDetails = ({ namespace, tables }: Props) => (
  <List
    dataSource={tables}
    renderItem={renderTable} />
);

export default HiveDetails;
