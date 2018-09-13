import { Button, Col, Row, Tabs } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { setRequestType, setRequest } from './actions';
import BehaviorPage from './BehaviorPage';
import { Request } from './model';
import OverviewPage from './OverviewPage';

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
    this.requestUpdated = this.requestUpdated.bind(this);
    
    this.behaviorChanged(this.state.behavior);
  }

  requestUpdated(request: Request) {
    this.setState({
      ...this.state,
      request
    });
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
            <OverviewPage setRequest={this.requestUpdated} />
          </Tabs.TabPane>
          <Tabs.TabPane tab="Review" key="3">
            <div />
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

const mapStateToProps = () =>
  createStructuredSelector({
  });

const mapDispatchToProps = (dispatch: any) => ({
  setType: (requestType: string) => dispatch(setRequestType(requestType)),
  setRequest: (request: Request) => dispatch(setRequest(request)),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceRequest);
