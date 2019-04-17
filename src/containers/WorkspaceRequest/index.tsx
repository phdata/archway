import * as React from 'react';
import { Row, Col, Button, Icon, Tabs, notification } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { BehaviorPage, OverviewPage, CompliancePage, SummaryPage, WorkspaceSummary } from './components';
import * as actions from './actions';
import * as selectors from './selectors';
import {
  PAGE_BEHAVIOR,
  PAGE_DETAILS,
  PAGE_COMPLIANCE,
  PAGE_REVIEW,
} from './constants';
import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';
import { Profile } from '../../models/Profile';

interface Props {
  profile: Profile;
  behavior: string;
  request: RequestInput;
  workspace?: Workspace;
  currentPage: string;
  loading: boolean;
  error: string;
  nextStepEnabled: boolean;
  advancedVisible: boolean;

  setAdvancedVisible: (visible: boolean) => void;
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

  public componentWillReceiveProps(nextProps: Props) {
    if (!this.props.error && nextProps.error) {
      this.showFailedNotification(nextProps.error);
    }
  }

  public showFailedNotification(error: string) {
    notification.open({
      message: 'Failed to create workspace',
      description: `The request to create a personal workspace failed due to the following error: ${error}`,
    });
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
      advancedVisible,
      setAdvancedVisible,
      setBehavior,
      setRequest,
      gotoNextPage,
      gotoPrevPage,
    } = this.props;

    return (
      <div style={{ textAlign: 'center', color: 'black' }}>
        <h1 style={{ paddingTop: 16 }}>New Workspace Request</h1>
  
        <Tabs tabBarStyle={{ textAlign: 'center' }} activeKey={currentPage}>
          <Tabs.TabPane tab="Behavior" key={PAGE_BEHAVIOR}>
            <BehaviorPage
              selected={behavior}
              onChange={setBehavior}
            />
          </Tabs.TabPane>

          <Tabs.TabPane tab="Details" key={PAGE_DETAILS}>
            <OverviewPage
              request={request}
              setRequest={setRequest}
            />
          </Tabs.TabPane>
  
          <Tabs.TabPane tab="Compliance" key={PAGE_COMPLIANCE}>
            <CompliancePage
              request={request}
              setRequest={setRequest}
            />
          </Tabs.TabPane>

          <Tabs.TabPane tab="Review" key={PAGE_REVIEW}>
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
              disabled={loading || (currentPage === PAGE_BEHAVIOR)}
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
              {(currentPage === PAGE_REVIEW) ? (
                loading ? (
                  <Icon type="loading" spin style={{ color: 'white' }} />
                ) : (
                  <span><Icon type="inbox" />Request</span>
                )
              ) : (
                <span>Next<Icon type="right" /></span>
              )}
            </Button>
          </Col>
        </Row>

        {(currentPage === PAGE_REVIEW && !!workspace) && (
          <WorkspaceSummary
            workspace={workspace}
            expanded={advancedVisible}
            onToggleExpand={() => setAdvancedVisible(!advancedVisible)}
          />
        )}
      </div>
    );
  }
}

const mapStateToProps = () => createStructuredSelector({
  profile: selectors.getProfile(),
  loading: selectors.getLoading(),
  error: selectors.getError(),
  behavior: selectors.getBehavior(),
  request: selectors.getRequest(),
  workspace: selectors.getGeneratedWorkspace(),
  currentPage: selectors.getCurrentPage(),
  nextStepEnabled: selectors.isNextStepEnabled(),
  advancedVisible: selectors.isAdvancedVisible(),
});

const mapDispatchToProps = (dispatch: any) => ({
  setAdvancedVisible: (visible: boolean) => dispatch(actions.setAdvancedVisible(visible)),
  setBehavior: (behavior: string) => dispatch(actions.setBehavior(behavior)),
  setRequest: (request: RequestInput) => dispatch(actions.setRequest(request)),
  gotoNextPage: () => dispatch(actions.gotoNextPage()),
  gotoPrevPage: () => dispatch(actions.gotoPrevPage()),
  clearRequest: () => dispatch(actions.clearRequest()),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceRequest);
