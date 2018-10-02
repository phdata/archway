import { Card, Col, Icon, List, Row } from 'antd';
import * as React from 'react';
import { ResourcePool, Workspace, Application, HiveAllocation } from '../../types/Workspace';
import Label from '../WorkspaceDetails/Components/Label';

interface Props {
    workspace?: Workspace;
    name: string;
}

const ResourceItem = (name: string) => <h4>{name}</h4>;

const SummaryPage = ({ workspace, name }: Props) => {
  const databaseNames = workspace && workspace.data.map((database: HiveAllocation) => database.name);
  const poolNames = workspace && workspace.processing.map((pool: ResourcePool) => pool.pool_name);
  const applicationNames = workspace && workspace.applications.map((application: Application) => application.name);
  return (
    <div>
      <h3>
        Here's a preview of what we'll request. Take a look and submit your request when you're ready!
    </h3>
      <Row type="flex" justify="center" style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={8}>
          <Card>
            <Icon type="crown" style={{ fontSize: 42 }} />
            <Label>You will be the liason for this project</Label>
            <h4>{name}</h4>
          </Card>
        </Col>
      </Row>
      <Row type="flex" justify="center" gutter={25} style={{ marginTop: 25, marginBottom: 25 }}>
        <Col span={6} style={{ display: 'flex', flex: 1 }}>
          <Card style={{ justifyContent: 'center', display: 'flex', flex: 1 }}>
            <Icon type="database" style={{ fontSize: 42 }} />
            <Label>The following hive databases will be created for you</Label>
            <List
              renderItem={ResourceItem}
              dataSource={databaseNames} />
          </Card>
        </Col>
        <Col span={6} style={{ display: 'flex', flex: 1 }}>
          <Card style={{ justifyContent: 'center', display: 'flex', flex: 1 }}>
            <Icon type="rocket" style={{ fontSize: 42 }} />
            <Label>The following resource pools will be created for you</Label>
            <List
              renderItem={ResourceItem}
              dataSource={poolNames} />
          </Card>
        </Col>
        <Col span={6} style={{ display: 'flex', flex: 1 }}>
          <Card style={{ justifyContent: 'center', display: 'flex', flex: 1 }}>
            <Icon type="thunderbolt" style={{ fontSize: 42 }} />
            <Label>The following applications (with their own consumer group) will be created for you</Label>
            <List
              renderItem={ResourceItem}
              dataSource={applicationNames} />
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default SummaryPage;
