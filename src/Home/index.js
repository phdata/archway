import React from 'react';
import { push } from 'react-router-redux';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Row, Col, Icon } from 'antd';

const serviceColor = (service) => {
  console.log(service);
  switch (service && service.status) {
    case 'GOOD_HEALTH':
      return '#43AA8B'
      break;
    case 'CONCERNING_HEALTH':
      return '#FF6F59'
      break;
    case 'BAD_HEALTH':
      return '#DB504A'
      break;
    default:
      return '#aaa'
      break;
  }
}

const Service = ({ name, color, status, links }) => (
  <div style={{ backgroundColor: color, borderRadius: 10, margin: 10, width: 250, padding: 10 }}>
    <h3 style={{ color: 'white' }}>{name}</h3>
    <ul style={{ listStyle: 'none', margin: 0, padding: 0 }}>
    {links.map(link => (
      <li>
        <a style={{ color: 'white' }} target="_blank" href={link.url}>
          <Icon type="link" style={{ marginRight: 5 }} />{link.name}
        </a>
      </li>
    ))}
    </ul>
  </div>
);

const Home = ({ name, displayStatus, color, services }) => {
  const hiveLinks = [
    {name: "Hive UI", url: `https://${services && services.HIVESERVER2.host}:10002`}
  ];
  const hueLinks = [
    {name: "Hue UI", url: "https://master2.valhalla.phdata.io:8889"}
  ]
  const yarnLinks = [
    {name: "Resource Manager UI", url: "https://master3.valhalla.phdata.io:8090"},
  ];
  return (
    <div style={{ textAlign: 'center', height: '100%' }}>
      <h1 style={{ fontWeight: 100  }}>
        You are currently connected to {name}!
      </h1>
      <h3 style={{ fontWeight: 100 }}>
        The current status is <span style={{ color: color }}>{displayStatus}</span>
      </h3>
      <div style={{ display: 'flex', justifyContent: 'center', marginTop: 25 }}>
        <Service name="Hive" status="Good" color={serviceColor(services && services.HIVESERVER2)} links={hiveLinks} />
        <Service name="Hue" status="Good" color={serviceColor(services && services.HUE)} links={hueLinks} />
        <Service name="Yarn" status="Good" color={serviceColor(services && services.YARN)} links={yarnLinks} />
      </div>
    </div>
  );
}

export default connect(
  s => s.cluster,
  {}
)(Home);
