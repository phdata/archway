import * as React from 'react';
import { Button, Form, Input, Col, Row, Tabs, Checkbox, Tooltip, Icon, Card, List } from 'antd';
import { withFormik, InjectedFormikProps, FormikFormProps } from 'formik';
import { connect } from 'react-redux';

import { setRequest, workspaceRequested, setRequestType, requestChanged } from './actions';
import { Request } from './model';

interface FieldLabelProps {
  children: React.ReactNode
}

const FieldLabel = ({ children }: FieldLabelProps) => (
  <div style={{ color: 'rgba(0, 0, 0, .5)', textAlign: 'center', textTransform: 'uppercase', fontSize: 10 }}>
    {children}
  </div>
)

const changeToggle = (stuff: any, more: any) => {
  console.log(more);
}

const OverviewPage = ({ handleChange, handleSubmit, values: { name, summary, description, phi_data, pii_data, pci_data } }: InjectedFormikProps<FormikFormProps, Request>) => (
  <Col span={12} offset={6}>
    <Form
      layout="vertical"
      onSubmit={handleSubmit}>
      <Form.Item
        label={<FieldLabel>Workspace Name</FieldLabel>}>
        <Input
          size="large"
          name="name"
          placeholder="Loan Modification Group (LMG)"
          value={name}
          onChange={handleChange} />
      </Form.Item>
      <Form.Item
        label={<FieldLabel>Summary</FieldLabel>}>
        <Input
          size="large"
          name="summary"
          placeholder="LMG's place to evaluate modification algorithms"
          value={summary}
          onChange={handleChange} />
      </Form.Item>
      <Form.Item
        label={<FieldLabel>Purpose for workspace</FieldLabel>}>
        <Input.TextArea
          name="description"
          placeholder="(Use this area to fully describe to reviewers and future users why this workspace is needed.)"
          rows={5}
          value={description}
          onChange={handleChange} />
      </Form.Item>
      <Form.Item
        label={<FieldLabel>The data in this workspace may contain</FieldLabel>}>
        <Checkbox.Group onChange={handleChange}>
          <Checkbox name="compliance" value="phi_data">PHI</Checkbox>
          <Checkbox name="compliance" value="pci_data">PCI</Checkbox>
          <Checkbox name="compliance" value="pii_data">PII</Checkbox>
        </Checkbox.Group>
      </Form.Item>
    </Form>
  </Col>
);

const OverviewPageForm = withFormik({
  handleSubmit: (payload, { props }: any) => {
    props.setRequest(payload);
  }
})(OverviewPage);

interface BehaviorProps {
  behaviorKey: string
  icon: string
  title: string
  description: string
  useCases: string[]
  selected?: boolean
  onChange: (behavior: string) => void
}

const UseCase = (item: String) => <div>{item}</div>;

class BehaviorChoice extends React.PureComponent<BehaviorProps> {
  select() {
    this.props.onChange(this.props.behaviorKey);
  }

  constructor(props: BehaviorProps) {
    super(props);

    this.select = this.select.bind(this);
  }

  render() {
    const { icon, title, description, useCases, selected, onChange } = this.props;
    return (
      <Card
        onClick={this.select}
        hoverable={true}
        style={{ margin: 25, height: 300, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{ opacity: selected ? 1 : 0, position: 'absolute', top: 25, right: 25, transition: 'opacity 300ms' }}>
          <Icon type="check" style={{ fontSize: 24 }} />
        </div>
        <Icon type={icon} style={{ fontSize: 42 }} />
        <h2>{title}</h2>
        <p>{description}</p>
        <div>
          <div style={{ textTransform: 'uppercase', fontSize: 12 }}>example use cases</div>
          <List
            dataSource={useCases}
            renderItem={UseCase} />
        </div>
      </Card>
    );
  }
}

interface BehaviorPageProps {
  selected: string
  onChange: (behavior: string) => void
}

const BehaviorPage = ({ selected, onChange }: BehaviorPageProps) => (
  <div>
    <h3 style={{ display: 'inline-block', marginRight: 7 }}>
      What kind of behavior should we manage?
    </h3>
    <Tooltip title="Heimdali will set up a structure that enables your team to work on a workspace in a certain way. See descriptions below for more information">
      <Icon theme="twoTone" type="question-circle" />
    </Tooltip>
    <Row type="flex" justify="center">
      <Col span={6}>
        <BehaviorChoice
          behaviorKey="simple"
          selected={selected === "simple"}
          onChange={onChange}
          icon="team"
          title="Simple"
          description="A simple place for multiple users to collaborate on a solution."
          useCases={["brainstorming", "evaluation", "prototypes"]} />
      </Col>
      <Col span={6}>
        <BehaviorChoice
          behaviorKey="structured"
          selected={selected === "structured"}
          onChange={onChange}
          icon="deployment-unit"
          title="Structured"
          description={`Data moves through three steps: raw, staging, modeled. Each step represents a more "structured" version of the data.`}
          useCases={["publishings", "data assets", "external interfacing"]} />
      </Col>
    </Row>
  </div>
)

interface RequestState {
  request: Request
  behavior: string
  selectedPage: number
}

class WorkspaceRequest extends React.Component<{}, RequestState> {
  constructor(props: {}) {
    super(props);

    this.state = {
      request: {
        name: '',
        summary: '',
        description: '',
        phi_data: false,
        pci_data: false,
        pii_data: false
      },
      behavior: 'simple',
      selectedPage: 1
    };

    this.behaviorChanged = this.behaviorChanged.bind(this);
    this.nextPage = this.nextPage.bind(this);
    this.previousPage = this.previousPage.bind(this);
  }

  behaviorChanged(behavior: string) {
    this.setState({
      ...this.state,
      behavior
    });
  }

  nextPage() {
    this.setState({
      ...this.state,
      selectedPage: this.state.selectedPage + 1
    })
  }

  previousPage() {
    this.setState({
      ...this.state,
      selectedPage: this.state.selectedPage - 1
    })
  }

  render() {
    console.log(this.state)
    return (
      <div style={{ textAlign: 'center', color: 'black' }}>
        <h1>New Workspace Request</h1>

        <Tabs tabBarStyle={{ textAlign: 'center' }} activeKey={`${this.state.selectedPage}`}>
          <Tabs.TabPane tab="Behavior" key="1">
            <BehaviorPage
              selected={this.state.behavior}
              onChange={this.behaviorChanged} />
          </Tabs.TabPane>
          <Tabs.TabPane tab="Details" key="2">
            <OverviewPageForm />
          </Tabs.TabPane>
          <Tabs.TabPane tab="Review" key="3">
            <OverviewPageForm />
          </Tabs.TabPane>
        </Tabs>

        <Row type="flex" justify="center" gutter={16}>
          {this.state.selectedPage > 1 && (
            <Col span={5}>
              <Button
                size="large"
                onClick={this.previousPage}
                type="primary"
                block={true}>
                Previous
              </Button>
            </Col>
          )}
          {this.state.selectedPage < 3 && (
            <Col span={5}>
              <Button
                size="large"
                onClick={this.nextPage}
                type="primary"
                block={true}>
                Next
              </Button>
            </Col>
          )}
        </Row>
      </div>
    );
  }
}

export default connect(
  state => state,
  { setRequest }
)(WorkspaceRequest);
