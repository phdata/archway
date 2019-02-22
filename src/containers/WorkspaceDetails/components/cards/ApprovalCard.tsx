import * as React from 'react';
import { Card, Icon } from 'antd';
import { ApprovalItem } from '../../../../models/Workspace';

/* tslint:disable:no-var-requires */
const TimeAgo = require('timeago-react').default;

interface Props {
  data?: ApprovalItem;
  children?: any;
  onApprove?: React.MouseEventHandler<HTMLAnchorElement>;
}

const ApprovalCard = ({ data, children, onApprove }: Props) => {
  const loading = data && data.status && data.status.loading;
  const approvedAt = data && data.approval_time;

  return (
    <Card style={{ height: '100%' }} bordered>
      <div style={{ textAlign: 'center', fontSize: 17 }}>
        {children}
      </div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          fontWeight: 300,
          padding: '32px 0 48px 0',
        }}
      >
        <Icon
          type={approvedAt ? 'safety-certificate' : 'dash'}
          theme={approvedAt ? 'twoTone' : 'outlined'}
          style={{ fontSize: 24 }}
        />
        <div style={{ marginLeft: 8, lineHeight: 1.3 }}>
          {!!approvedAt && <div>APPROVED</div>}
          {!!approvedAt && (
            <div style={{ fontSize: 12 }}>
              <TimeAgo datetime={approvedAt} />
            </div>
          )}
          {!approvedAt && (
            loading ? (
              <Icon
                theme="outlined"
                type="loading"
                style={{ fontSize: 16 }}
              />
            ) : !!onApprove && (
              <a onClick={onApprove}>APPROVE</a>
            )
          )}
        </div>
      </div>
    </Card>
  );
}

export default ApprovalCard;
