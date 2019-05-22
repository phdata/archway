import * as React from 'react';
import { Card, Table, Icon } from 'antd';
import CardHeader from './CardHeader';
import { Colors } from '../../../../components';
import { NamespaceInfoList } from '../../../../models/Workspace';

interface Props {
  info?: NamespaceInfoList;
  onRefreshHiveTables: () => void;
}

const TablesCard = ({ info, onRefreshHiveTables }: Props) => {
  return (
    <Card bordered>
      <CardHeader>
        TABLES
        <Icon
          style={{ fontSize: 16, color: Colors.Green.string(), marginLeft: 8, cursor: 'pointer' }}
          type="sync"
          spin={info && info.loading}
          onClick={() => {
            if ((!info || !info.loading) && onRefreshHiveTables) {
              onRefreshHiveTables();
            }
          }}
        />
      </CardHeader>
      <Table
        dataSource={(info && info.data && info.data.length > 0) ? info.data[0].tables : []}
        pagination={false}
      >
        <Table.Column
          title="Name"
          dataIndex="name"
          key="name"
        />
      </Table>
    </Card>
  );
};

export default TablesCard;
