import * as React from 'react';
import { Card, Icon } from 'antd';
import Label from './Label';
import { Doughnut } from 'react-chartjs-2';
import Colors from '../../../components/Colors';

interface Props {
  allocated: number;
  consumed?: number;
  location: string;
}

const Allocations = ({ location, allocated, consumed = 0}: Props) => {
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
      <Label style={{ textAlign: 'center' }}>Available Capacity</Label>
      <div
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
            data={data}
            redraw={false}
            options={{ legend: false, title: false, maintainAspectRatio: false }} />
      </div>
      <div style={{ letterSpacing: 1, fontSize: 18, textAlign: 'center' }}>
        {`${(allocated - consumed).toFixed(1)}/${allocated.toFixed(1)} GB`}
      </div>
    </Card>
  );
};

export default Allocations;
