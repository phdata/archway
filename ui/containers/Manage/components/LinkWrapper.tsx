import * as React from 'react';
import { Input, Form, Button, Row, Col, Icon, Popconfirm } from 'antd';

import { Link } from '../../../models/Manage';
import { WrappedFormUtils } from 'antd/lib/form/Form';
import { linkLayout } from '../constants';
import { Colors } from '../../../components';

interface Props {
  link: Link;
  index: number;
  form: WrappedFormUtils<any>;

  removeLink: (index: number) => void;
  setLink: (index: number, link: Link) => void;
}

class LinkWrapper extends React.Component<Props> {
  constructor(props: Props) {
    super(props);

    this.handleChange = this.handleChange.bind(this);
    this.handleRemove = this.handleRemove.bind(this);
  }
  public handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { index, setLink } = this.props;
    const link: Link = {
      ...this.props.link,
      [e.target.name]: e.target.value,
    };
    setLink(index, link);
  }

  public handleRemove(index: number) {
    this.props.form.resetFields([`name${index}`, `description${index}`, `url${index}`]);
    this.props.removeLink(index);
  }

  public render() {
    const { index, link } = this.props;
    const { getFieldDecorator } = this.props.form;

    return (
      <div style={{ border: `1px solid ${Colors.PrimaryColor.toString()}`, padding: 24, marginBottom: 48 }}>
        <Row style={{ textAlign: 'center' }}>
          <Col span={24}>
            <Form.Item label="Name" {...linkLayout}>
              {getFieldDecorator(`name${index}`, {
                rules: [{ required: true, message: 'Name is required' }],
                initialValue: link.name,
              })(<Input name="name" onChange={this.handleChange} style={{ marginRight: 10 }} />)}
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="Url" {...linkLayout}>
              {getFieldDecorator(`url${index}`, {
                rules: [
                  { required: true, message: 'Url is required' },
                  { type: 'url', message: 'The input is not valid url' },
                ],
                initialValue: link.url,
              })(<Input name="url" onChange={this.handleChange} />)}
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="Description" {...linkLayout}>
              {getFieldDecorator(`description${index}`, {
                rules: [{ required: true, message: 'Description is required' }],
                initialValue: link.description,
              })(<Input name="description" onChange={this.handleChange} style={{ flex: 1 }} />)}
            </Form.Item>
          </Col>
          <Popconfirm
            placement="top"
            title="Are you sure?"
            onConfirm={() => this.handleRemove(index)}
            icon={<Icon type="question-circle-o" style={{ color: 'red' }} />}
          >
            <Button type="danger" style={{ width: 300, position: 'absolute', bottom: '-41px', right: 20 }}>
              <Icon type="minus" /> Remove this link
            </Button>
          </Popconfirm>
        </Row>
      </div>
    );
  }
}

export default LinkWrapper;
