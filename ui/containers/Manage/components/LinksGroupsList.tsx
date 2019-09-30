import * as React from 'react';
import { List, Button } from 'antd';
import { withRouter, RouteComponentProps } from 'react-router-dom';

import { LinksGroupCard } from './cards';
import { LinksGroup } from '../../../models/Manage';
import { LinksGroupCardPage } from '../../../constants';

interface Props extends RouteComponentProps<any> {
  loading: boolean;
  linksGroups: LinksGroup[];

  clearSelectedLinksGroup: () => void;
}

const LinksGroupsList = ({ loading, linksGroups, history, match, clearSelectedLinksGroup }: Props) => (
  <div>
    <Button
      style={{ marginBottom: 24 }}
      type="primary"
      onClick={() => {
        clearSelectedLinksGroup();
        history.push(`${match.url}/add`);
      }}
    >
      Add a new custom links
    </Button>
    <List
      grid={{ gutter: 16, lg: 3, md: 2, sm: 1 }}
      dataSource={linksGroups}
      loading={loading}
      renderItem={(linksGroup: LinksGroup) => (
        <List.Item>
          <LinksGroupCard linksGroup={linksGroup} page={LinksGroupCardPage.Manage} />
        </List.Item>
      )}
    />
  </div>
);

export default withRouter(LinksGroupsList);
