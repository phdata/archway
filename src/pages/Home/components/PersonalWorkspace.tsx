import * as React from 'react';
import { Button, Card, Spin, Row, Col } from 'antd';
import { PrepareHelp, RunHelp, CreateHelp, Label } from '../../WorkspaceDetails/components';
import { Workspace } from '../../../types/Workspace';
import { Cluster } from '../../../types/Cluster';

interface Props {
    workspace: Workspace;
    services: any;
    loading: boolean;
    cluster?: Cluster;
    requestWorkspace: () => void;
}

const PersonalWorkspace = ({ workspace, services, requestWorkspace, loading }: Props) => {
  // hue/metastore/tables/flights
  return (
    <div style={{ position: 'relative', flex: 1 }}>
      <div
        style={{
          position: 'absolute',
          zIndex: 999,
          display: workspace.data ? 'none' : 'flex',
          width: '100%',
          height: '100%',
          justifyContent: 'center',
          alignItems: 'center',
        }}>
        {loading && <Spin />}
        {!loading && (
          <Card bodyStyle={{ textAlign: 'center' }}>
            <h3>You don't have a personal workspace yet!</h3>
            <Button type="primary" onClick={requestWorkspace}>Create One Now</Button>
          </Card>
        )}
      </div>
      <div style={{ filter: workspace.data ? 'none' : 'blur(.4rem)', transition: '1s ease' }}>
        <Card bodyStyle={{ display: 'flex', flexDirection: 'column' }}>
          <Label>Your Personal Workspace</Label>
          {workspace && (
            <div>
              <div style={{
                display: 'flex',
                justifyContent: 'center',
                textAlign: 'center',
                fontWeight: 'bold',
                fontSize: '12px',
              }}>
                <div style={{ padding: '10px' }}>
                  <div>HDFS LOCATION</div>
                  <div style={{ fontWeight: 200 }}>{workspace.data[0].location}</div>
                </div>
                <div style={{ padding: '10px' }}>
                  <div>HIVE NAMESPACE</div>
                  <div style={{ fontWeight: 200 }}>{workspace.data[0].name}</div>
                </div>
                <div style={{ padding: '10px' }}>
                  <div>RESOURCE POOL</div>
                  <div style={{ fontWeight: 200 }}>{workspace.processing[0].pool_name}</div>
                </div>
              </div>
              <Row gutter={12}>
                <Col span={24} xl={8} style={{ marginTop: 10 }}>
                  <PrepareHelp
                    location={workspace.data[0].location}
                    namespace={workspace.data[0].name} />
                </Col>
                <Col span={24} xl={8} style={{ marginTop: 10 }}>
                  <CreateHelp
                    host={services.hive.thrift[0].host}
                    port={services.hive.thrift[0].port}
                    namespace={workspace.data[0].name} />
                </Col>
                <Col span={24} xl={8} style={{ marginTop: 10 }}>
                  <RunHelp
                    queue={workspace.processing[0].pool_name} />
                </Col>
              </Row>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};

export default PersonalWorkspace;
