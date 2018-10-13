import { Card, Row } from 'antd';
import * as React from 'react';
import { HueService } from '../../../types/Cluster';
import { HiveTable, NamespaceInfo } from '../../../types/Workspace';
import CardHeader from './CardHeader';

interface Props {
    hue?: HueService;
    namespace: string;
    info?: NamespaceInfo[];
}

const renderTable = (hiveTable: HiveTable) => (
  <div style={{ margin: 10, textAlign: 'center' }}>{hiveTable.name}</div>
);

const HiveDetails = ({ hue, namespace, info }: Props) => {
  const hueHost = hue && `${hue.load_balancer[0].host}:${hue.load_balancer[0].port}`;
  return (
    <Card
      actions={[
        <a
          target="_blank"
          href={hue ? `//${hueHost}/hue/metastore/tables/${namespace}` : undefined}>
          See in Hue
        </a>,
      ]}>
      <CardHeader
        icon="database"
        heading="Hive Tables"
        subheading={namespace} />
      <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
        {info && info.length > 0 && info[0].tables.length > 0 && info[0].tables.map(renderTable)}
        {(!info || info.length <= 0 || info[0].tables.length <= 0) && (
          <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
            No tables yet.
          </div>
        )}
      </Row>
    </Card>
  );
};

export default HiveDetails;
