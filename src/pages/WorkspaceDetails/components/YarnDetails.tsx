import { Card, Row } from 'antd';
import * as React from 'react';
import { YarnService } from '../../../types/Cluster';
import { PoolInfo, YarnApplication } from '../../../types/Workspace';
import { CardHeader } from '.';

interface Props {
  yarn?: YarnService;
  poolName: string;
  pools?: PoolInfo[];
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

const YarnDetails = ({ yarn, poolName, pools }: Props) => {
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
        subheading={poolName} />
      <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
        {pools && pools[0].applications.length > 0 && pools[0].applications.map(renderApplication(rmURL))}
        {(!pools || pools.length <= 0 || pools[0].applications.length <= 0) && (
          <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
            No running YARN applications.
          </div>
        )}
      </Row>
    </Card>
  );
};

export default YarnDetails;
