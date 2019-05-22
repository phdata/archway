import * as React from 'react';
import { Row, Col, Button } from 'antd';
import DatabasesSummary from './DatabasesSummary';
import ApplicationsSummary from './ApplicationsSummary';
import PoolsSummary from './PoolsSummary';
import { Workspace } from '../../../models/Workspace';

interface Props {
    workspace?: Workspace;
    expanded?: boolean;
    onToggleExpand?: () => void;
}

const WorkspaceSummary = ({ workspace, expanded, onToggleExpand }: Props) => {
  if (!workspace) {
    return null;
  }

  return (
    <Row type="flex" justify="center" gutter={16}>
      <Col span={5}>
        <Button
          style={{ marginTop: 16, marginBottom: 16 }}
          type="primary"
          ghost
          block
          onClick={onToggleExpand}
        >
          Full IA Details
        </Button>
      </Col>
      {expanded && (
        <Col span={24}>
          <DatabasesSummary workspace={workspace} />
          <PoolsSummary workspace={workspace} />
          <ApplicationsSummary workspace={workspace} />
        </Col>
      )}
    </Row>
  );
};

export default WorkspaceSummary;
