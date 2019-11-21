import * as React from 'react';
import { Doughnut } from 'react-chartjs-2';

import { Colors, Feature } from '../../../components';
import { HiveAllocation } from '../../../models/Workspace';
import { Card, Button } from 'antd';
import { ModalType, FeatureFlagType } from '../../../constants';
import { ProtocolTypes } from '../constants';

interface Props {
  data: HiveAllocation;
  isDefault: boolean;
  isPlatformOperations: boolean;

  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const HiveDatabase = ({ data, showModal, isPlatformOperations, isDefault }: Props) => {
  const protocol = data.protocol || ProtocolTypes.HDFS;
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
    <Card style={{ minHeight: 165 }}>
      <div style={{ fontSize: 14, overflow: 'hidden', textOverflow: 'ellipsis', textAlign: 'center' }}>{data.name}</div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginTop: 12 }}>
        {'Protocol: ' + protocol}
      </div>
      {protocol === ProtocolTypes.HDFS && (
        <React.Fragment>
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
        </React.Fragment>
      )}
      <Feature flag={FeatureFlagType.DiskQuota}>
        {isPlatformOperations && (
          <div style={{ textAlign: 'center' }}>
            <Button type="primary" onClick={e => showModal(e, ModalType.ModifyDiskQuota)}>
              Modify
            </Button>
          </div>
        )}
      </Feature>
    </Card>
  );
};

export default HiveDatabase;
