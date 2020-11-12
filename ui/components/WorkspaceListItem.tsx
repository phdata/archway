import * as React from 'react';
import chartjs from 'chart.js';
import { Card, Icon, List } from 'antd';
import { ChartData, Doughnut } from 'react-chartjs-2';
import Colors from './Colors';
import { Workspace, WorkspaceSearchResult } from '../models/Workspace';
import TruncateText from './TruncateText';

interface Props {
  workspace: Workspace | WorkspaceSearchResult;
  onSelected: (id: number) => void;
}

const WorkspaceListItem = ({ workspace, onSelected }: Props) => {
  const { id, name, status = '', behavior, summary } = workspace;

  let total_disk_allocated_in_gb;
  let total_disk_consumed_in_gb;
  const workspaceSearchResult = workspace as WorkspaceSearchResult;
  const workspaceData = (workspace as Workspace).data;
  if (workspaceSearchResult.total_disk_allocated_in_gb) {
    total_disk_allocated_in_gb = workspaceSearchResult.total_disk_allocated_in_gb;
    total_disk_consumed_in_gb = workspaceSearchResult.total_disk_consumed_in_gb;
  } else if (workspaceData) {
    total_disk_allocated_in_gb = workspaceData.reduce((acc, cur) => acc + cur.size_in_gb, 0);
    total_disk_consumed_in_gb = workspaceData.reduce((acc, cur) => acc + cur.consumed_in_gb, 0);
  }

  const onClick = () => onSelected(id);

  const ApprovalMessage = () => {
    let color = '';
    switch (status.toLowerCase()) {
      case 'pending':
        color = '#CFB2B0';
        break;
      case 'rejected':
        color = '#7B2D26';
        break;
      case 'approved':
        color = '#0B7A75';
        break;
      default:
    }

    return (
      <div
        style={{
          textTransform: 'uppercase',
          fontSize: 10,
          fontWeight: 300,
          color,
        }}
      >
        {status}
      </div>
    );
  };

  const allocated = total_disk_allocated_in_gb || 1;
  const consumed = total_disk_consumed_in_gb || 0;
  const sizeData: ChartData<chartjs.ChartData> = {
    labels: ['Available (GB)', 'Consumed (GB)'],
    datasets: [
      {
        label: '',
        data: [allocated - consumed, consumed],
        backgroundColor: [
          total_disk_consumed_in_gb ? Colors.PrimaryColor.string() : Colors.LightGray.string(),
          total_disk_consumed_in_gb
            ? Colors.PrimaryColor.lighten(0.5).string()
            : Colors.LightGray.lighten(0.5).string(),
        ],
      },
    ],
  };

  return (
    <List.Item>
      <Card bordered={true} onClick={onClick} hoverable={true} bodyStyle={{ padding: '20px' }}>
        <ApprovalMessage />
        <div style={{ display: 'flex', alignItems: 'center', padding: '28px 0 40px' }}>
          <div style={{ fontSize: '12px', color: 'rgba(0, 0, 0, 0.65)', flex: 7 }}>
            <div style={{ fontSize: '22px', textTransform: 'uppercase' }}>
              <TruncateText text={name} lineHeight={32} maxLine={1} />
            </div>
            <div>
              <TruncateText text={summary} lineHeight={16} maxLine={2} />
            </div>
            <div
              style={{
                textTransform: 'uppercase',
                display: 'flex',
                justifyContent: 'flex-start',
                alignItems: 'center',
              }}
            >
              <Icon type={behavior === 'simple' ? 'team' : 'deployment-unit'} />
              &nbsp;
              <TruncateText text={`${behavior} dataset`} lineHeight={12} maxLine={1} />
            </div>
            <div style={{ color: Colors.PrimaryColor.string(), lineHeight: '24px' }}>DETAILS &gt;</div>
          </div>
          <div style={{ flex: 3 }}>
            <div
              style={{
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                flex: 1,
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Doughnut
                height={52}
                width={52}
                data={sizeData}
                redraw={false}
                options={{ legend: undefined, title: undefined, maintainAspectRatio: false }}
              />
            </div>
            <div style={{ letterSpacing: 1, textAlign: 'center' }}>
              {`${(allocated - consumed).toFixed(1)}GB/${allocated}GB`}
            </div>
          </div>
        </div>
      </Card>
    </List.Item>
  );
};

export default WorkspaceListItem;
