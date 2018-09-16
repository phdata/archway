import { Card, Col, Icon, List, Row } from 'antd';
import * as React from 'react';
import { Database, ResourcePool, Workspace } from '../../types/Workspace';

interface Props {
  workspace?: Workspace;
  name: string;
}

const ResourceItem = (name: string) => <h4>{name}</h4>;

const SummaryPage = ({ workspace, name }: Props) => {
  const databaseNames = workspace && workspace.data.map((database: Database) => database.name);
  const poolNames = workspace && workspace.processing.map((pool: ResourcePool) => pool.pool_name);
  return (
    <div>
      <h3>
        Here's a preview of what we'll request. Take a look and submit your request when you're ready!
    </h3>
      <Row type="flex" justify="center" style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={8}>
          <Card>
            <Icon type="crown" style={{ fontSize: 42 }} />
            <h3>You will be the liason for this project</h3>
            <h4>{name}</h4>
          </Card>
        </Col>
      </Row>
      <Row type="flex" justify="center" gutter={25} style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={8}>
          <Card>
            <Icon type="database" style={{ fontSize: 42 }} />
            <h3>The following <b>hive databases</b> will be created for you</h3>
            <List
              renderItem={ResourceItem}
              dataSource={databaseNames} />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Icon type="thunderbolt" style={{ fontSize: 42 }} />
            <h3>The following <b>resource pools</b> will be created for you</h3>
            <List
              renderItem={ResourceItem}
              dataSource={poolNames} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default SummaryPage;
