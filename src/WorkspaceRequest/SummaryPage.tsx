import * as React from 'react';
import { Card, Icon, Row, Col, List } from 'antd';
import { Workspace, Database } from '../WorkspaceListing/Workspace';

interface Props {
  workspace: Workspace
  name: string
}

const DatabaseItem = (name: string) => <h4>{name}</h4>;

const SummaryPage = ({ workspace, name }: Props) => {
  const databaseNames = workspace.data.map((database: Database) => database.name)
  return (
    <div>
      <h3>
        Here's a preview of what we'll request. Take a look and submit your request when you're ready!
    </h3>
      <Row type="flex" justify="center" style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={12}>
          <Card>
            <Icon type="crown" style={{ fontSize: 42 }} />
            <h3>You will be the liason for this project</h3>
            <h4>{name}</h4>
          </Card>
        </Col>
      </Row>
      <Row type="flex" justify="center" gutter={25} style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={12}>
          <Card>
            <Icon type="database" style={{ fontSize: 42 }} />
            <h3>The following databases will be created for you</h3>
            <List
              renderItem={DatabaseItem}
              dataSource={databaseNames} />
          </Card>
          <Card>
            <Icon type="thunderbolt" style={{ fontSize: 42 }} />
            <h3>You will be the liason for this project</h3>
            <h4>{name}</h4>
          </Card>
        </Col>
      </Row>
    </div>
  );
}

export default SummaryPage;