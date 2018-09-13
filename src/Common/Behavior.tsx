import { Card, Icon, List } from 'antd';
import * as React from 'react';

interface Props {
  behaviorKey: string
  icon: string
  title: string
  description?: string
  useCases?: string[]
  selected?: boolean
  style?: React.CSSProperties,
  onChange: (behavior: string, selected: boolean) => void
}

const UseCase = (item: String) => <div>{item}</div>;

class Behavior extends React.PureComponent<Props> {
  select() {
    this.props.onChange(this.props.behaviorKey, !this.props.selected);
  }

  constructor(props: Props) {
    super(props);

    this.select = this.select.bind(this);
  }

  render() {
    const { icon, title, description, useCases, selected } = this.props;
    return (
      <Card
        onClick={this.select}
        hoverable={true}
        bodyStyle={{ textAlign: 'center', ...this.props.style }}>
        <div style={{ opacity: selected ? 1 : 0, position: 'absolute', top: 25, right: 25, transition: 'opacity 300ms' }}>
          <Icon type="check" style={{ fontSize: 24 }} />
        </div>
        <Icon type={icon} style={{ fontSize: 42 }} />
        <h2>{title}</h2>
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

export default Behavior