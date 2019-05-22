import * as React from 'react';
import { Icon } from 'antd';
import { Colors } from '../../../components';

/* tslint:disable:no-var-requires */
const TimeAgo = require('timeago-react').default;

interface Props {
  ready?: boolean;
  createdAt: Date;
}

const Status = ({ ready, createdAt }: Props) => (
  <div style={{ display: 'flex', alignItems: 'center' }}>
    <Icon
      style={{ fontSize: 32 }}
      type={ready ? 'check-circle' : 'close-circle'}
      theme="twoTone"
    />
    <div style={{ textTransform: 'uppercase', marginLeft: 8 }}>
      <div style={{ fontSize: 18 }}>
        {ready ? 'READY' : 'PENDING'}
      </div>
      <div style={{ fontSize: 10, color: Colors.Gray.string(), textTransform: 'uppercase' }}>
        created <TimeAgo datetime={createdAt} />
      </div>
    </div>
  </div>
);

export default Status;
