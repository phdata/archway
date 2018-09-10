import * as React from 'react';
import {Menu, Dropdown, Icon} from 'antd';

import Service, { ServiceInfo } from './Service';

const Yarn = (props: ServiceInfo) => {
  const rmLinks = (
    <Menu>
      {props.service.resource_manager.map((location: any) => (
        <Menu.Item>
          <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
            {location.host}
          </a>
        </Menu.Item>
      ))}
    </Menu>
  )
  const nmLinks = (
    <Menu>
      {props.service.yarn.node_manager.map((location: any) => (
        <Menu.Item>
          <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
            {location.host}
          </a>
        </Menu.Item>
      ))}
    </Menu>
  )
  const yarnLinks = [
    (<Dropdown overlay={nmLinks}><a href="#" className="ant-dropdown-link">Node Manager UI <Icon type="down" /></a></Dropdown>),
    (<Dropdown overlay={rmLinks}><a href="#" className="ant-dropdown-link">Resource Manager UI <Icon type="down" /></a></Dropdown>),
  ];
  return (<Service
    links={[]}
    {...props} />
  );
}

export default Yarn;