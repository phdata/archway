import { Card, Row, Menu, Dropdown, Icon } from 'antd';
import * as React from 'react';
import { HueService } from '../../../types/Cluster';
import { HiveTable, HiveAllocation, NamespaceInfoList } from '../../../types/Workspace';
import CardHeader from './CardHeader';

interface Props {
    hue?: HueService;
    info?: NamespaceInfoList;
    allocations: HiveAllocation[];
    selectedAllocation?: HiveAllocation;
    onChangeAllocation: (allocation: HiveAllocation) => void;
    onRefreshHiveTables?: () => void;
}

const renderTable = (hiveTable: HiveTable) => (
  <div style={{ margin: 10, textAlign: 'center' }}>{hiveTable.name}</div>
);

const HiveDetails = ({
  hue,
  allocations,
  info,
  selectedAllocation,
  onChangeAllocation,
  onRefreshHiveTables,
}: Props) => {
  const hueHost = hue && `${hue.load_balancer[0].host}:${hue.load_balancer[0].port}`;
  return (
    <Card
      actions={[
        <a
          target="_blank"
          href={hue && selectedAllocation ? `//${hueHost}/hue/metastore/tables/${selectedAllocation.name}` : undefined}>
          See in Hue
        </a>,
      ]}>
      <CardHeader
        icon="database"
        heading="Hive Tables"
        subheading={(
          <Dropdown
            overlay={(
              <Menu
                onClick={({ key }) => onChangeAllocation(allocations[parseInt(key, 10)])}
              >
                {allocations.map(({ name }, index) => (
                  <Menu.Item key={index}>{name}</Menu.Item>
                ))}
              </Menu>
            )}
            trigger={['click']}
          >
            <a className="ant-dropdown-link" href="#">
              {selectedAllocation ? selectedAllocation.name : 'No Database Found'}
              <Icon type="down" />
            </a>
          </Dropdown>
        )}
        rightAction={(
          <Icon
            style={{ cursor: 'pointer', alignSelf: 'flex-start' }}
            type="sync"
            spin={info && info.loading}
            onClick={() => {
              if ((!info || !info.loading) && onRefreshHiveTables) {
                onRefreshHiveTables();
              }
            }}
          />
        )}
      />
      <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
        {(info && info.data && info.data.length > 0 && info.data[0].tables.length > 0) ?
          info.data[0].tables.map(renderTable) : (
            <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
              No tables yet.
            </div>
          )}
      </Row>
    </Card>
  );
};

export default HiveDetails;
