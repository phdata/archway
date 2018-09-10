import * as React from 'react';
import { connect } from 'react-redux';
import { Icon, Tooltip, Row, Col, Card, Avatar, Dropdown, Menu, List } from 'antd';
import { Line, Pie } from 'react-chartjs-2';

import WorkspaceListItem from '../Common/WorkspaceListItem';
import PersonalWorkspace from './PersonalWorkspace';
import Service, {ServiceInfo} from './Service';
import Hive from './Hive';
import Hue from './Hue';
import Yarn from './Yarn';

interface Props {
  name: string
  overallStatus: string,
  overallStatusColor: string
  hive: ServiceInfo
  hue: ServiceInfo
  yarn: ServiceInfo
  cmUrl: string
}

const Home = ({ name, overallStatus, overallStatusColor, hive, hue, yarn, cmUrl }: Props) => {
  return (
    <div>
      <div style={{ padding: 24, background: '#fff', textAlign: 'center', height: '100%' }}>
        <h1 style={{ fontWeight: 100 }}>
          You are currently connected to {name}!
        </h1>
        <h3 style={{ fontWeight: 100 }}>
          The current status of {name} is <span style={{ fontWeight: 'bold', overallStatusColor }}>{overallStatus}</span>
        </h3>
        <h2>
          <a target="_blank" rel="noreferrer noopener" href={cmUrl}>
            {name}&apos;s Cloudera Manager UI
          </a>
        </h2>
      </div>
      <div style={{ display: 'flex', marginTop: 25 }}>
        <Hive name="Hive" index={0} />
        <Hue name="Hue" index={1} />
        <Yarn name="Yarn" index={2} />
      </div>
      <div style={{ marginTop: 25, display: 'flex' }}>
        <Card style={{ flex: 1 }} title="Project Requests">
          <Line data={requestsData} height={250} options={{ legend: { display: false }, maintainAspectRatio: false }} />
        </Card>
        <Card style={{ flex: 1, marginLeft: 25 }} title="HDFS Capacity">
          <Line data={capacityData} height={250} options={{ legend: { display: false }, maintainAspectRatio: false }} />
        </Card>
      </div>
      <List
        grid={{ column: 3 }}
        style={{ marginTop: 25 }}
        dataSource={workspaces}
        renderItem={workspace => <WorkspaceListItem workspace={workspace} />}
        />
      <div style={{ marginTop: 25 }}>
        <PersonalWorkspace services={services} />
      </div>
    </div>
  );
}

export default connect(
  s => s.cluster, {}
)(Home);
