import { Card, Row, Icon } from 'antd';
import * as React from 'react';
import { YarnService } from '../../../types/Cluster';
import { ResourcePoolsInfo, YarnApplication } from '../../../types/Workspace';
import CardHeader from './CardHeader';

interface Props {
  yarn?: YarnService;
  poolName: string;
  pools?: ResourcePoolsInfo;
  onRefreshPools?: () => void;
}

const renderApplication = (yarnUrl?: string) => (yarnApplication: YarnApplication) => (
  <div style={{ textAlign: 'center' }}>
    <div>
      {`${yarnApplication.name}`}
    </div>
    <a
      style={{ fontSize: 12 }}
      target="_blank"
      href={`${yarnUrl}/cluster/app/${yarnApplication.id}/`}>
      DETAILS
    </a>
  </div>
);

const YarnDetails = ({ yarn, poolName, pools, onRefreshPools }: Props) => {
  const rmURL = yarn && `${yarn.resource_manager[0].host}:${yarn.resource_manager[0].port}`;
  return (
    <Card
      actions={[
        <a
          target="_blank"
          href={yarn ? `//${rmURL}` : undefined}>
          Open Resource Manager
        </a>,
      ]}>
      <CardHeader
        icon="rocket"
        heading="Yarn Applications"
        subheading={poolName}
        rightAction={(
          <Icon
            style={{ cursor: 'pointer', alignSelf: 'flex-start' }}
            type="sync"
            spin={pools && pools.loading}
            onClick={() => {
              if ((!pools || !pools.loading) && onRefreshPools) {
                onRefreshPools();
              }
            }}
          />
        )}
      />
      <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
        {pools && pools.data && pools.data[0].applications.length > 0
          && pools.data[0].applications.map(renderApplication(rmURL))}
        {(!pools || !pools.data || pools.data.length <= 0
          || pools.data[0].applications.length <= 0) && (
          <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
            No running YARN applications.
          </div>
        )}
      </Row>
    </Card>
  );
};

export default YarnDetails;
