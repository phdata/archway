import React from 'react';
import { connect } from 'react-redux';
import { Icon, Tooltip, Row, Col, Card, Avatar, Dropdown, Menu, List } from 'antd';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { solarizedDark } from 'react-syntax-highlighter/styles/hljs';
import { Line, Pie } from 'react-chartjs-2';

import WorkspaceListItem from '../Common/WorkspaceListItem';
import Color from '../Common/Colors';

const serviceColor = (service) => {
  switch (service && service.status) {
    case 'GOOD_HEALTH':
      return Color.Green.rgb().string();
    case 'CONCERNING_HEALTH':
      return Color.Orange.rgb().string();
    case 'BAD_HEALTH':
      return Color.Red.rgb().string();
    default:
      return '#aaa'
  }
}

const serviceText = (service) => {
  switch (service && service.status) {
    case 'GOOD_HEALTH':
      return '"good"'
    case 'CONCERNING_HEALTH':
      return '"concerning"'
    case 'BAD_HEALTH':
      return '"bad"'
    default:
      return 'unknown'
  }
}

const GlowColor = (status) => {
  switch (status) {
    case 'GOOD_HEALTH':
      return `0 0 5px 2px ${Color.Green.hsl().string()}`;
    case 'CONCERNING_HEALTH':
      return `0 0 5px 2px ${Color.Orange.hsl().string()}`;
    case 'BAD_HEALTH':
      return `0 0 5px 2px ${Color.Red.hsl().string()}`;
    default:
      return false
  }
}

const Service = ({ name, color, status, links, rawStatus, index }) => (
  <Card
    style={{ flex: 1, marginLeft: index == 0 ? 0 : 25 }}
    actions={links}>
    <Card.Meta
      title={name}
      description={`${name}'s status is currently ${status}`}
      avatar={
        <Avatar
          size="small"
          alt={`${name}'s status is currently ${status}`}
          style={{ boxShadow: GlowColor(rawStatus), backgroundColor: color }}
          />
      } />
  </Card>
);

const PersonalWorkspace = ({ databaseName = 'user_benny', poolName = 'user.benny', services }) => {
  const code = `
  from impala.dbapi import connect
  conn = connect(host='${services && services.IMPALA.host}', port=21050, database='${databaseName}')
  cursor = conn.cursor()
  cursor.execute('SELECT * FROM mytable LIMIT 100')
  print cursor.description  # prints the result set's schema
  results = cursor.fetchall()
  `;
  const sparkSubmit = `
  $ spark-submit --class org.apache.spark.examples.SparkPi \\
              --master yarn \\
              --deploy-mode cluster \\
              --queue ${poolName} \\
              examples/jars/spark-examples*.jar \\
              10
  `;
  return (
    <Card bodyStyle={{ display: 'flex' }} title="Your Personal Workspace">
      <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
        <h3>Connect to Your Database With <a target="_blank" rel="noreferrer noopener" href="https://github.com/cloudera/impyla">impyla</a></h3>
        <SyntaxHighlighter language="python" style={solarizedDark}>{code}</SyntaxHighlighter>
      </Card.Grid>
      <Card.Grid style={{ flex: 1, boxShadow: 'none' }}>
        <h3>Submit SparkPi to your personal queue on YARN</h3>
        <SyntaxHighlighter language="shell" style={solarizedDark}>{sparkSubmit}</SyntaxHighlighter>
      </Card.Grid>
    </Card>
  );
}

const workspaces = [
  { name: "Thor", description: "Loan Application Archive", type: "Simple", totalDisk: 3000, totalCores: 5, totalMemory: 64 },
  { name: "Ivar", description: "Customer satisfaction survey", type: "Simple", totalDisk: 1000, totalCores: 2, totalMemory: 16 },
  { name: "Thor", description: "Loan Application Archive", type: "Simple", totalDisk: 3000, totalCores: 5, totalMemory: 64 }
];

const requestsData = {
  labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
  datasets: [{
    fill: true,
    label: false,
    borderColor: Color.Blue.fade(.8).hsl().string(),
    backgroundColor: Color.Blue.fade(.8).hsl().string(),
    data: [1, 3, 5, 3, 8, 4, 2]
  }]
}

const capacityData = {
  labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July'],
  datasets: [{
    label: "2",
    fill: true,
    borderColor: Color.Blue.fade(.8).hsl().string(),
    backgroundColor: Color.Blue.fade(.8).hsl().string(),
    data: [1000, 1000, 1000, 1000, 1000, 2500, 2500]
  }, {
    label: "1",
    fill: true,
    borderColor: Color.Blue.lighten(.5).fade(.8).hsl().string(),
    backgroundColor: Color.Blue.lighten(.5).fade(.8).hsl().string(),
    data: [250, 400, 484, 440, 780, 923, 1024]
  }]
}

const Home = ({ name, displayStatus, color, services }) => {
  const hiveLinks = [
    (<a target="_blank" rel="noreferrer noopener" href={`https://${services && services.HIVESERVER2.host}:10002`}>Hive UI</a>)
  ];
  const hueLinks = [
    (<a target="_blank" rel="noreferrer noopener" href="https://master2.valhalla.phdata.io:8889">Hue UI</a>)
  ]
  const rmLinks = (
    <Menu>
      <Menu.Item>
        <a target="_blank" rel="noreferrer noopener" href="https://worker1.valhalla.phdata.io:8090">master1</a>
      </Menu.Item>
      <Menu.Item>
        <a target="_blank" rel="noreferrer noopener" href="https://worker2.valhalla.phdata.io:8090">master2</a>
      </Menu.Item>
    </Menu>
  )
  const yarnLinks = [
    (<Dropdown overlay={rmLinks}><a href="#" className="ant-dropdown-link">Node Manager UI <Icon type="down" /></a></Dropdown>),
    (<Dropdown overlay={rmLinks}><a href="#" className="ant-dropdown-link">Resource Manager UI <Icon type="down" /></a></Dropdown>),
  ];
  return (
    <div>
      <div style={{ padding: 24, background: '#fff', textAlign: 'center', height: '100%' }}>
        <h1 style={{ fontWeight: 100 }}>
          You are currently connected to {name}!
        </h1>
        <h3 style={{ fontWeight: 100 }}>
          The current status of {name} is <span style={{ fontWeight: 'bold', color }}>{displayStatus}</span>
        </h3>
        <h2>
          <a target="_blank" rel="noreferrer noopener" href="http://master1.jotunn.io:7180/">
            {name}&apos;s Cloudera Manager UI
          </a>
        </h2>
      </div>
      <div style={{ display: 'flex', marginTop: 25 }}>
        <Service target="_blank" rel="noreferrer noopener" name="Hive" index={0} rawStatus={services && services.HIVESERVER2.status} status={serviceText(services && services.HIVESERVER2)} color={serviceColor(services && services.HIVESERVER2)} links={hiveLinks} />
        <Service target="_blank" rel="noreferrer noopener" name="Hue" index={1} rawStatus={services && services.HUE.status} status={serviceText(services && services.HUE)} color={serviceColor(services && services.HUE)} links={hueLinks} />
        <Service target="_blank" rel="noreferrer noopener" name="Yarn" index={2} rawStatus={services && services.YARN.status} status={serviceText(services && services.YARN)} color={serviceColor(services && services.YARN)} links={yarnLinks} />
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