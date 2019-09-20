import * as React from 'react';
import { List, Button } from 'antd';
import { withRouter, RouteComponentProps } from 'react-router-dom';

import { ComplianceQuestionCard } from './cards';
import { ComplianceContent } from '../../../models/Manage';

interface Props extends RouteComponentProps<any> {
  loading: boolean;
  compliances: ComplianceContent[];

  clearSelectedCompliance: () => void;
}

const ComplianceList = ({ loading, compliances, history, match, clearSelectedCompliance }: Props) => (
  <div>
    <Button
      style={{ zIndex: 9999, marginBottom: 24 }}
      type="primary"
      onClick={() => {
        clearSelectedCompliance();
        history.push(`${match.url}/add`);
      }}
    >
      Add a new group
    </Button>
    <List
      grid={{ gutter: 16, lg: 3, md: 2, sm: 1 }}
      dataSource={compliances}
      loading={loading}
      renderItem={(compliance: ComplianceContent) => (
        <List.Item>
          <ComplianceQuestionCard compliance={compliance} />
        </List.Item>
      )}
    />
  </div>
);

export default withRouter(ComplianceList);
