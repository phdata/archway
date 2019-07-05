import * as React from 'react';
import { Card, Icon, List } from 'antd';

interface Props {
  behaviorKey: string;
  icon: string;
  title: string;
  description?: string;
  useCases?: string[];
  selected?: boolean;
  onChange: (behavior: string, selected: boolean) => void;
}

const UseCase = (item: string) => <div>{item}</div>;

const Behavior = ({ behaviorKey, icon, title, description, useCases, selected, onChange }: Props) => (
  <Card
    onClick={() => onChange(behaviorKey, !selected)}
    hoverable={true}
    style={{ flex: 1, borderRadius: 3, marginBottom: 15 }}
    bodyStyle={{ textAlign: 'center' }}
  >
    <div
      style={{
        opacity: selected ? 1 : 0,
        position: 'absolute',
        top: 25,
        right: 25,
        transition: 'opacity 300ms',
      }}
    >
      <Icon type="check" style={{ fontSize: 14 }} />
    </div>
    <Icon type={icon} style={{ fontSize: 42 }} />
    <div style={{ fontSize: 14, textTransform: 'uppercase', display: 'flex', justifyContent: 'center' }}>{title}</div>
    {description && <p>{description}</p>}
    {useCases && (
      <div>
        <div style={{ textTransform: 'uppercase', fontSize: 12 }}>example use cases</div>
        <List dataSource={useCases} renderItem={UseCase} />
      </div>
    )}
  </Card>
);

export default Behavior;
