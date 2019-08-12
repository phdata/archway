import * as React from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Colors } from '../../../components';
import { HiveAllocation } from '../../../models/Workspace';
import { Card } from 'antd';

interface Props {
  data: HiveAllocation;
  isDefault: boolean;
}

const HiveDatabase = ({ data, isDefault }: Props) => {
  const total_disk_allocated_in_gb = data.size_in_gb;
  const total_disk_consumed_in_gb = data.consumed_in_gb;
  const allocated = total_disk_allocated_in_gb || 1;
  const consumed = total_disk_consumed_in_gb || 0;
  const sizeData = {
    labels: ['Available (GB)', 'Consumed (GB)'],
    datasets: [
      {
        label: false,
        data: [allocated - consumed, consumed],
        backgroundColor: [Colors.PrimaryColor.string(), Colors.PrimaryColor.lighten(0.5).string()],
      },
    ],
  };

  return (
    <Card>
      <div style={{ fontSize: 14, overflow: 'hidden', textOverflow: 'ellipsis', textAlign: 'center' }}>{data.name}</div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginTop: 12 }}>
        <Doughnut
          height={52}
          width={52}
          // @ts-ignore
          data={sizeData}
          redraw={false}
          // @ts-ignore
          options={{ legend: false, title: false, maintainAspectRatio: false }}
        />
      </div>
      <div style={{ letterSpacing: 1, textAlign: 'center', fontSize: 12, padding: '4px 0 8px 0' }}>
        {`${(allocated - consumed).toFixed(1)}GB/${allocated}GB`}
        <br />
        AVAILABLE
      </div>
    </Card>
  );
};

export default HiveDatabase;
