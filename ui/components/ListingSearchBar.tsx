import * as React from 'react';
import { Card, Row, Col, Input, Checkbox } from 'antd';

import FieldLabel from './FieldLabel';
import Behavior from './Behavior';
import { workspaceStatuses, workspaceBehaviors, behaviorProperties } from '../constants';
import { Filters } from '../models/Listing';

interface Props {
  filters: Filters;
  updateFilter: (filter: string, behavior: string[], statuses: string[]) => void;
}

const ListingSearchBar = ({ filters: { filter, behaviors, statuses }, updateFilter }: Props) => {
  function filterUpdated(event: React.ChangeEvent<HTMLInputElement>) {
    updateFilter(event.target.value, behaviors, statuses);
  }

  function behaviorChanged(behavior: string, checked: boolean) {
    if (checked) {
      behaviors.push(behavior);
    } else {
      behaviors.splice(behaviors.indexOf(behavior), 1);
    }

    updateFilter(filter, behaviors, statuses);
  }

  function statusChanged(status: string, checked: boolean) {
    if (checked) {
      statuses.push(status);
    } else {
      statuses.splice(statuses.indexOf(status), 1);
    }

    updateFilter(filter, behaviors, statuses);
  }

  return (
    <Card>
      <Row gutter={12} type="flex">
        <Col
          span={24}
          lg={12}
          style={{
            display: 'flex',
            flex: '1',
            flexDirection: 'column',
            justifyContent: 'space-around',
          }}
        >
          <div>
            <FieldLabel>FILTER</FieldLabel>
            <Input.Search value={filter} onChange={filterUpdated} placeholder="find a workspace..." />
          </div>
          <div>
            <FieldLabel>STATUS</FieldLabel>
            <div style={{ display: 'flex', justifyContent: 'space-around' }}>
              {workspaceStatuses.map((status: string) => (
                <Checkbox
                  key={status}
                  style={{ fontSize: 12, textTransform: 'uppercase' }}
                  defaultChecked
                  name={status}
                  checked={statuses.includes(status)}
                  onChange={(e: any) => statusChanged(status, e.target.checked)}
                >
                  {status}
                </Checkbox>
              ))}
            </div>
          </div>
        </Col>
        <Col span={24} lg={12}>
          <FieldLabel>BEHAVIOR</FieldLabel>
          <Row type="flex" style={{ flexDirection: 'row', justifyContent: 'center' }} gutter={12}>
            {workspaceBehaviors.map((behavior, index) => (
              <Col span={24 / workspaceBehaviors.length} key={index}>
                <Behavior
                  behaviorKey={behavior}
                  selected={behaviors.includes(behavior)}
                  onChange={behaviorChanged}
                  icon={behaviorProperties[behavior].icon}
                  title={behaviorProperties[behavior].title}
                />
              </Col>
            ))}
          </Row>
        </Col>
      </Row>
    </Card>
  );
};

export default ListingSearchBar;
