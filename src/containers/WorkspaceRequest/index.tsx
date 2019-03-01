import * as React from 'react';
import { Row, Col, Button, Icon, Tabs } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { BehaviorPage, OverviewPage, SummaryPage } from './components';
import * as actions from './actions';
import * as selectors from './selectors';
import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';
import { Profile } from '../../models/Profile';

interface Props {
  profile: Profile;
  behavior: string;
  request: RequestInput;
  workspace?: Workspace;
  currentPage: number;
  loading: boolean;
  nextStepEnabled: boolean;

  setBehavior: (behavior: string) => void;
  setRequest: (request: RequestInput) => void;
  gotoNextPage: () => void;
  gotoPrevPage: () => void;
  clearRequest: () => void;
}

class WorkspaceRequest extends React.Component<Props> {
  public componentDidMount() {
    this.props.clearRequest();
  }

  public render() {
    const {
      profile,
      behavior,
      request,
      workspace,
      loading,
      nextStepEnabled,
      currentPage,
      setBehavior,
      setRequest,
      gotoNextPage,
      gotoPrevPage,
    } = this.props;

    return (
      <div style={{ textAlign: 'center', color: 'black' }}>
        <h1 style={{ paddingTop: 16 }}>New Workspace Request</h1>
  
        <Tabs tabBarStyle={{ textAlign: 'center' }} activeKey={`${currentPage}`}>
          <Tabs.TabPane tab="Behavior" key="1">
            <BehaviorPage
              selected={behavior}
              onChange={setBehavior}
            />
          </Tabs.TabPane>
  
          <Tabs.TabPane tab="Details" key="2">
            <OverviewPage
              request={request}
              setRequest={setRequest}
            />
          </Tabs.TabPane>
  
          <Tabs.TabPane tab="Review" key="3">
            <SummaryPage
              workspace={workspace}
              profile={profile}
            />
          </Tabs.TabPane>
        </Tabs>
  
        <Row type="flex" justify="center" gutter={16}>
          <Col span={5}>
            <Button
              size="large"
              type="primary"
              block
              disabled={loading || (currentPage === 1)}
              onClick={gotoPrevPage}
            >
              <Icon type="left" />
              Previous
            </Button>
          </Col>
          <Col span={5}>
            <Button
              size="large"
              type="primary"
              block
              disabled={!nextStepEnabled}
              onClick={gotoNextPage}
            >
              {(currentPage === 3) ? (
                <span><Icon type="inbox" />Request</span>
              ): (
                <span>Next<Icon type="right" /></span>
              )}
            </Button>
          </Col>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = () => createStructuredSelector({
  profile: selectors.getProfile(),
  loading: selectors.getLoading(),
  behavior: selectors.getBehavior(),
  request: selectors.getRequest(),
  workspace: selectors.getGeneratedWorkspace(),
  currentPage: selectors.getCurrentPage(),
  nextStepEnabled: selectors.isNextStepEnabled(),
});

const mapDispatchToProps = (dispatch: any) => ({
  setBehavior: (behavior: string) => dispatch(actions.setBehavior(behavior)),
  setRequest: (request: RequestInput) => dispatch(actions.setRequest(request)),
  gotoNextPage: () => dispatch(actions.gotoNextPage()),
  gotoPrevPage: () => dispatch(actions.gotoPrevPage()),
  clearRequest: () => dispatch(actions.clearRequest()),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceRequest);
