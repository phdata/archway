import * as React from 'react';
import { Dropdown, Menu, Button, Icon } from 'antd';

import { WebLocation, HueService, YarnService } from '../../../models/Cluster';
import { HiveAllocation } from '../../../models/Workspace';

interface Props {
  hue?: HueService;
  yarn?: YarnService;
  selectedAllocation?: HiveAllocation;
}

const Link = ({ name, host }: { name: string; host: WebLocation }) => {
  const location = `${host.host}:${host.port}`;
  const hint = `${name} (${host.host.substring(0, host.host.indexOf('.'))})`;
  return (
    <Menu.Item>
      <a style={{ padding: 10 }} key="action" target="_blank" rel="noopener noreferrer" href={`//${location}/`}>
        {hint}
      </a>
    </Menu.Item>
  );
};

const QuickLinks = ({ hue, yarn, selectedAllocation }: Props) => (
  <Dropdown
    overlay={
      <Menu onClick={() => null}>
        {hue && hue.load_balancer.map((l, i) => <Link key={`hue-$i`} name="Hue" host={l} />)}
        {yarn && yarn.resource_manager.map((l, i) => <Link key={`rn-$i`} name="Resource Manager" host={l} />)}
      </Menu>
    }
    trigger={['click']}
  >
    <Button type="primary">
      QUICK LINKS <Icon type="down" />
    </Button>
  </Dropdown>
);

export default QuickLinks;
