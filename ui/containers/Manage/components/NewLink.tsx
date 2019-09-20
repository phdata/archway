import * as React from 'react';
import { Form, Button, Input, Row, Col, Icon } from 'antd';
import { FormComponentProps } from 'antd/lib/form';
import { Link } from '../../../models/Manage';
import { linkLayout } from '../constants';
import { Colors } from '../../../components';

export interface Props extends FormComponentProps {
  children?: any;
  customLinkGroupId: number | undefined;

  addNewLink: (link: Link) => void;
}

const NewLink = ({ addNewLink, customLinkGroupId, form }: Props) => {
  const [name, setName] = React.useState('');
  const [description, setDescription] = React.useState('');
  const [url, setUrl] = React.useState('');
  const { getFieldDecorator } = form;

  const handleClick = () => {
    form.validateFields(err => {
      if (err) {
        return;
      }
      let newLink: Link = {
        name,
        description,
        url,
      };
      if (!!customLinkGroupId) {
        newLink = { ...newLink, customLinkGroupId };
      }
      addNewLink(newLink);
      form.resetFields();
    });
  };

  return (
    <div style={{ border: `1px solid ${Colors.PrimaryColor.toString()}`, padding: 24, marginBottom: 48 }}>
      <Form {...linkLayout}>
        <Row style={{ textAlign: 'center' }}>
          <Col span={24}>
            <Form.Item label="Name">
              {getFieldDecorator(`name`, {
                rules: [{ required: true, message: 'Name is required' }],
              })(<Input onChange={e => setName(e.target.value)} style={{ marginRight: 10 }} />)}
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="Url">
              {getFieldDecorator(`url`, {
                rules: [
                  { required: true, message: 'Url is required' },
                  { type: 'url', message: 'The input is not valid url' },
                ],
              })(<Input onChange={e => setUrl(e.target.value)} />)}
            </Form.Item>
          </Col>
          <Col span={24}>
            <Form.Item label="Description">
              {getFieldDecorator(`description`, {
                rules: [{ required: true, message: 'Description is required' }],
              })(<Input onChange={e => setDescription(e.target.value)} style={{ flex: 1 }} />)}
            </Form.Item>
          </Col>
          <Button
            type="primary"
            onClick={handleClick}
            style={{ width: '60%', position: 'absolute', left: '50%', transform: 'translateX(-50%)', bottom: '-41px' }}
          >
            <Icon type="plus" /> Add new link
          </Button>
        </Row>
      </Form>
    </div>
  );
};

export default Form.create<Props>()(NewLink);
