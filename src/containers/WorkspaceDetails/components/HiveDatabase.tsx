import * as React from 'react';
import { Doughnut } from 'react-chartjs-2';
import { Colors } from '../../../components';
import { HiveAllocation } from '../../../models/Workspace';

interface Props {
  data: HiveAllocation;
  isDefault: boolean;
  isSelected: boolean;
  onSelect: () => void;
}

const HiveDatabase = ({ data, isDefault, isSelected, onSelect }: Props) => {
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
        backgroundColor: [Colors.Green.string(), Colors.Green.lighten(.5).string()],
      },
    ],
  };

  const title = isDefault ? 'Default' : data.name.split('_')[0];
  const subtitle = data.name;

  return (
    <div
      style={{
        padding: 4,
        borderWidth: 2,
        borderColor: Colors.Green.string(),
        borderStyle: 'solid',
        backgroundColor: 'white',
        textAlign: 'center',
        position: 'relative',
        cursor: 'pointer',
      }}
      onClick={onSelect}
    >
      <div style={{ fontSize: 16, textTransform: 'uppercase' }}>{title}</div>
      <div style={{ fontSize: 11, overflow: 'hidden', textOverflow: 'ellipsis' }}>{subtitle}</div>
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
      {isSelected && (
        <div
          style={{
            width: 0,
            height: 0,
            borderLeft: '16px solid transparent',
            borderRight: '16px solid transparent',
            borderTop: '16px solid rgb(67, 170, 139)',
            position: 'absolute',
            left: '50%',
            top: '100%',
            transform: 'translateX(-50%)',
          }}
        >
          <div
            style={{
              width: 0,
              height: 0,
              borderLeft: '14px solid transparent',
              borderRight: '14px solid transparent',
              borderTop: '14px solid white',
              position: 'absolute',
              left: '-14px',
              top: '-16px',
            }}
          />
        </div>
      )}
    </div>
  );
}

export default HiveDatabase;
