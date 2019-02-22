import * as React from 'react';
import { Card, Table, Icon } from 'antd';

import CardHeader from './CardHeader';
import { Colors } from '../../../../components';
import { ResourcePoolsInfo, YarnApplication } from '../../../../models/Workspace';

interface Props {
  rmURL?: string;
  pools?: ResourcePoolsInfo;
  onRefreshPools?: () => void;
}

const YarnApplicationsCard = ({ pools, rmURL, onRefreshPools }: Props) => {
  return (
    <Card style={{ height: '100%' }} bordered>
      <CardHeader>
        RECENT YARN APPLICATIONS
        <Icon
          style={{ fontSize: 16, color: Colors.Green.string(), marginLeft: 8, cursor: 'pointer' }}
          type="sync"
          spin={pools && pools.loading}
          onClick={() => {
            if ((!pools || !pools.loading) && onRefreshPools) {
              onRefreshPools();
            }
          }}
        />
      </CardHeader>
      <Table
        dataSource={pools && pools.data && pools.data[0].applications.map((yarnApplication: YarnApplication) => ({
          name: yarnApplication.name,
          link: rmURL && `${rmURL}/cluster/app/${yarnApplication.id}/`,
        }))}
        pagination={false}
      >
        <Table.Column
          title="Name"
          dataIndex="name"
          key="name"
        />
        <Table.Column
          title="Logs Link"
          dataIndex="link"
          key="link"
          width={100}
          render={(link: string) => !!link && (
            <a href={link} target="_blank">details</a>
          )}
        />
      </Table>
    </Card>
  );
};

export default YarnApplicationsCard;
