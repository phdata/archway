import * as React from 'react';
import { RouteComponentProps } from 'react-router';
import { Form, Input, Row, Col, Button, Alert } from 'antd';
import { FormComponentProps } from 'antd/lib/form';

import { LinkWrapper, NewLink } from '.';
import { LinksGroup, Link } from '../../../models/Manage';
import { OpenType, ManagePage, formLayout } from '../constants';

interface Props extends FormComponentProps<any>, RouteComponentProps<any> {
  openFor: OpenType;
  linksGroups: LinksGroup[];
  selectedLinksGroup: LinksGroup;

  setSelectedLinksGroup: (linksGroup: LinksGroup) => void;
  addLinksGroup: () => void;
  updateLinksGroup: () => void;
  deleteLinksGroup: () => void;
  setLink: (index: number, link: Link) => void;
  addNewLink: (link: Link) => void;
  removeLink: (index: number) => void;
}

class LinksGroupDetails extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);

    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  public handleSubmit(e: React.SyntheticEvent, buttonType: OpenType) {
    const { updateLinksGroup, deleteLinksGroup, addLinksGroup, form, selectedLinksGroup } = this.props;
    form.validateFields(err => {
      const isLinksGroupEmpty: boolean = selectedLinksGroup.links.length === 0;
      if (err || isLinksGroupEmpty) {
        return;
      }
      switch (buttonType) {
        case OpenType.Update:
          updateLinksGroup();
          break;
        case OpenType.Delete:
          deleteLinksGroup();
          break;
        case OpenType.Add:
          addLinksGroup();
          break;
      }
    });
  }

  public handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { selectedLinksGroup, setSelectedLinksGroup } = this.props;
    setSelectedLinksGroup({ ...selectedLinksGroup, [e.target.name]: e.target.value });
  }
  public componentDidMount() {
    const { openFor, linksGroups, match, setSelectedLinksGroup, history } = this.props;

    if (openFor === OpenType.Update) {
      const id = parseInt(match.params.id, 10);
      const selectedLinksGroup = linksGroups.find(linksGroup => linksGroup.id === id);
      if (!!selectedLinksGroup) {
        setSelectedLinksGroup(selectedLinksGroup);
      } else {
        history.push(`/manage/${ManagePage.LinksTab}`);
      }
    }
  }

  public renderButtons() {
    const { openFor } = this.props;
    return (
      <div>
        <Button
          type="primary"
          style={{ marginRight: 20 }}
          onClick={() => this.props.history.push(`/manage/${ManagePage.LinksTab}`)}
        >
          Cancel
        </Button>
        {openFor === OpenType.Update ? (
          <React.Fragment>
            <Button type="primary" style={{ marginRight: 20 }} onClick={e => this.handleSubmit(e, OpenType.Delete)}>
              Delete
            </Button>
            <Button type="primary" onClick={e => this.handleSubmit(e, OpenType.Update)}>
              Update
            </Button>
          </React.Fragment>
        ) : (
          <Button type="primary" onClick={e => this.handleSubmit(e, OpenType.Add)}>
            Add
          </Button>
        )}
      </div>
    );
  }

  public renderLinks() {
    const { setLink, selectedLinksGroup, removeLink, form } = this.props;
    return (
      !!selectedLinksGroup &&
      !!selectedLinksGroup.links &&
      selectedLinksGroup.links.map((link, index) => (
        <LinkWrapper link={link} setLink={setLink} index={index} key={index} removeLink={removeLink} form={form} />
      ))
    );
  }

  public render() {
    const { selectedLinksGroup, addNewLink, form } = this.props;
    const { getFieldDecorator } = form;
    const isLinksGroupEmpty: boolean = selectedLinksGroup.links.length === 0;

    return (
      <div style={{ textAlign: 'left' }} className="linksgroup-details">
        <Form>
          <Row style={{ marginBottom: 15 }}>
            <Col
              xs={{ span: 24, offset: 0 }}
              sm={{ span: 20, offset: 4 }}
              lg={{ span: 16, offset: 4 }}
              style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}
            >
              <h3 style={{ fontSize: 24, fontWeight: 300, marginBottom: 0 }}>Group Details</h3>
              {this.renderButtons()}
            </Col>
          </Row>
          <Form.Item label="Name" {...formLayout}>
            {getFieldDecorator('name', {
              rules: [{ required: true, message: 'Name is required!' }],
              initialValue: selectedLinksGroup.name,
            })(<Input name="name" onChange={this.handleChange} />)}
          </Form.Item>
          <Form.Item label="Description" {...formLayout}>
            {getFieldDecorator('description', {
              rules: [{ required: true, message: 'Description is required!' }],
              initialValue: selectedLinksGroup.description,
            })(<Input name="description" onChange={this.handleChange} />)}
          </Form.Item>
          {isLinksGroupEmpty && <Alert message="You must add at least one custom-link." type="warning" showIcon />}
          <br />
          {this.renderLinks()}
        </Form>
        <NewLink addNewLink={addNewLink} customLinkGroupId={selectedLinksGroup.id} />
      </div>
    );
  }
}

export default Form.create<Props>()(LinksGroupDetails);
