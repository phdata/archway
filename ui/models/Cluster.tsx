import { Dropdown, Icon, Menu } from 'antd';
import * as Color from 'color';
import * as React from 'react';
import { Colors } from '../components';

export interface WebLocation {
  host: string;
  port: number;
}

export interface Statusable {
  status: string;
}

export class Status<T extends Statusable> {
  public statusable: T;

  constructor(statusable: T) {
    this.statusable = statusable;
  }

  public statusColor = (): Color => {
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
  };

  public statusText = () => {
    switch (this.statusable.status) {
      case 'GOOD_HEALTH':
        return '"good"';
      case 'CONCERNING_HEALTH':
        return '"concerning"';
      case 'BAD_HEALTH':
        return '"bad"';
      default:
        return 'unknown';
    }
  };

  public glowColorText = () =>
    `0 0 5px 2px ${this.statusColor()
      .hsl()
      .string()}`;
}

export interface HueService extends Statusable {
  load_balancer: WebLocation[];
}

/* tslint:disable:no-empty-interface */
export interface HiveService extends Statusable {
  thrift: WebLocation[];
}

export interface YarnService extends Statusable {
  resource_manager: WebLocation[];
  node_manager: WebLocation[];
}

export interface NavigatorService extends Statusable {
  navigator: WebLocation[];
}

export abstract class ServiceLinks<T extends Statusable> {
  public service: T;

  public links!: JSX.Element[];

  constructor(service: T) {
    this.service = service;
  }
}

export class HueServiceLinks extends ServiceLinks<HueService> {
  public hueServiceLinks: JSX.Element[] =
    this.service &&
    this.service.load_balancer.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          Hue UI
        </a>
      </Menu.Item>
    ));

  public links: JSX.Element[] = [
    // tslint:disable-next-line: jsx-key
    <Dropdown overlay={<Menu>{this.hueServiceLinks}</Menu>}>
      <a href="#" className="ant-dropdown-link">
        {/* eslint-disable-line */}
        Quick Links <Icon type="down" />
      </a>
    </Dropdown>,
  ];
}

export class HiveServiceLinks extends ServiceLinks<HiveService> {
  public links: JSX.Element[] = [0].map(index => (
    <h4 key={index} style={{ cursor: 'not-allowed', color: 'rgb(0,0,0,.45)', margin: 0 }}>
      (no links)
    </h4>
  ));
}

export class YarnServiceLinks extends ServiceLinks<YarnService> {
  public resourceManagerLinks: JSX.Element[] =
    this.service &&
    this.service.resource_manager.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          Resource Manager UI ({location.host})
        </a>
      </Menu.Item>
    ));

  public nodeManagerLinks: JSX.Element[] =
    this.service &&
    this.service.node_manager.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          Node Manager UI ({location.host})
        </a>
      </Menu.Item>
    ));

  public links: JSX.Element[] = [
    // tslint:disable-next-line: jsx-key
    <Dropdown
      overlay={
        <Menu>
          {this.resourceManagerLinks}
          {this.nodeManagerLinks}
        </Menu>
      }
    >
      <a href="#" className="ant-dropdown-link">
        {/* eslint-disable-line */}
        Quick Links <Icon type="down" />
      </a>
    </Dropdown>,
  ];
}

export class NavigatorServiceLinks extends ServiceLinks<NavigatorService> {
  public navigatorServiceLinks: JSX.Element[] =
    this.service &&
    this.service.navigator.map(location => (
      <Menu.Item key={location.host}>
        <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>
          Navigator UI
        </a>
      </Menu.Item>
    ));

  public links: JSX.Element[] = [
    // tslint:disable-next-line: jsx-key
    <Dropdown overlay={<Menu>{this.navigatorServiceLinks}</Menu>}>
      <a href="#" className="ant-dropdown-link">
        {/* eslint-disable-line */}
        Quick Links <Icon type="down" />
      </a>
    </Dropdown>,
  ];
}

export interface Cluster extends Statusable {
  name: string;
  cm_url: string;
  services: {
    hive: HiveService;
    hue: HueService;
    yarn: YarnService;
    mgmt: NavigatorService;
  };
}
