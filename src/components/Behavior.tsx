import * as React from 'react';
import { Card, Icon, List } from 'antd';

interface Props {
    behaviorKey: string;
    icon: string;
    title: string;
    description?: string;
    useCases?: string[];
    selected?: boolean;
    style?: React.CSSProperties;
    onChange: (behavior: string, selected: boolean) => void;
}

const UseCase = (item: string) => <div>{item}</div>;

class Behavior extends React.PureComponent<Props> {

    constructor(props: Props) {
    super(props);

    this.select = this.select.bind(this);
  }
    public select() {
    this.props.onChange(this.props.behaviorKey, !this.props.selected);
  }

    public render() {
    const { icon, title, description, useCases, selected } = this.props;
    return (
      <Card
        onClick={this.select}
        hoverable={true}
        style={{ borderRadius: 3, ...this.props.style }}
        bodyStyle={{ textAlign: 'center' }}>
        <div
          style={{
            opacity: selected ? 1 : 0,
            position: 'absolute',
            top: 25,
            right: 25,
            transition: 'opacity 300ms',
          }}>
          <Icon type="check" style={{ fontSize: 14 }} />
        </div>
        <Icon type={icon} style={{ fontSize: 42 }} />
        <div style={{ fontSize: 14, textTransform: 'uppercase' }}>
          {title}
        </div>
        {description && <p>{description}</p>}
        {useCases && (
          <div>
            <div style={{ textTransform: 'uppercase', fontSize: 12 }}>example use cases</div>
            <List
              dataSource={useCases}
              renderItem={UseCase} />
          </div>
        )}
      </Card>
    );
  }
}

export default Behavior;
