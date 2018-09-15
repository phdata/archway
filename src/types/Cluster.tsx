import { Dropdown, Icon, Menu } from 'antd';
import * as Color from 'color';
import * as React from 'react';
import Colors from '../components/Colors';

export interface WebLocation {
  host: string;
  port: number;
}

export interface Statusable {
  status: string
}

export class Status<T extends Statusable> {
  statusable: T

  constructor(statusable: T) {
    this.statusable = statusable;
  }

  statusColor = (): Color => {
    switch (this.statusable.status) {
      case 'GOOD_HEALTH':
        return Colors.Green;
      case 'CONCERNING_HEALTH':
        return Colors.Orange;
      case 'BAD_HEALTH':
        return Colors.Red;
      default:
        return Colors.Gray;
    }
  }

  statusText = () => {
    switch (this.statusable.status) {
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

  glowColorText = () =>
    `0 0 5px 2px ${this.statusColor().hsl().string()}`
}

export interface HueService extends Statusable {
  load_balancer: WebLocation[];
}

export interface HiveService extends Statusable {

}

export interface YarnService extends Statusable {
  resource_manager: WebLocation[];
  node_manager: WebLocation[];
}

export abstract class ServiceLinks<T extends Statusable> {
  service: T;

  constructor(service: T) {
    this.service = service;
  }

  links: JSX.Element[]
}

export class HueServiceLinks extends ServiceLinks<HueService> {
  links: JSX.Element[] =
    this.service && this.service.load_balancer.map((location: WebLocation) => (
      <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>Hue UI</a>
    ));
}

export class HiveServiceLinks extends ServiceLinks<HiveService> {
  links: JSX.Element[] = [];
}

export class YarnServiceLinks extends ServiceLinks<YarnService> {
  resourceManagerLinks: JSX.Element[] =
    this.service && this.service.resource_manager.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          {location.host}
        </a>
      </Menu.Item>
    ));

  nodeManagerLinks: JSX.Element[] =
    this.service && this.service.node_manager.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          {location.host}
        </a>
      </Menu.Item>
    ));

  links: JSX.Element[] = [
    (<Dropdown overlay={<Menu>{this.nodeManagerLinks}</Menu>}><a href="#" className="ant-dropdown-link">Node Manager UI <Icon type="down" /></a></Dropdown>),
    (<Dropdown overlay={<Menu>{this.resourceManagerLinks}</Menu>}><a href="#" className="ant-dropdown-link">Resource Manager UI <Icon type="down" /></a></Dropdown>),
  ]
}

export interface Cluster extends Statusable {
  name: String
  cm_url: string
  services: {
    hive: HiveService
    hue: HueService
    yarn: YarnService
  }
}