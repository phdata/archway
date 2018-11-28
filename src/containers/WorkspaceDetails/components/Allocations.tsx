import * as React from 'react';
import { Card, Dropdown, Menu, Icon } from 'antd';
import Label from './Label';
import { Doughnut } from 'react-chartjs-2';
import { HiveAllocation } from '../../../types/Workspace';
import { Colors } from '../../../components';
import CardHeader from './CardHeader';

interface Props {
  allocations: HiveAllocation[];
  selectedAllocation?: HiveAllocation;
  onChangeAllocation: (allocation: HiveAllocation) => void;
}

const Allocations = ({
  allocations,
  selectedAllocation,
  onChangeAllocation,
}: Props) => {
  const allocated = selectedAllocation ? selectedAllocation.size_in_gb : 1;
  const consumed = selectedAllocation ? selectedAllocation.consumed_in_gb : 0;

  const data = {
    labels: ['Available (GB)', 'Consumed (GB)'],
    datasets: [
      {
        label: false,
        data: [allocated - consumed, consumed],
        backgroundColor: [Colors.Green.string(), Colors.Green.lighten(.5).string()],
      },
    ],
  };

  return (
    <Card
      style={{ display: 'flex', flex: 1 }}
      bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
      <CardHeader
        heading={<Label style={{ textAlign: 'center' }}>Available Capacity</Label>}
        subheading={selectedAllocation && (
          <div style={{ textAlign: 'center' }}>
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
          </div>
        )}
      />
      {selectedAllocation && <div
        style={{
          textAlign: 'center',
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <Doughnut
            height={100}
            width={100}
            // @ts-ignore
            data={data}
            redraw={false}
            // @ts-ignore
            options={{ legend: false, title: false, maintainAspectRatio: false }} />
      </div>}
      {selectedAllocation && <div style={{ letterSpacing: 1, textAlign: 'center' }}>
        {`${(allocated - consumed).toFixed(1)}GB/${allocated}GB`}
      </div>}
    </Card>
  );
};

export default Allocations;
