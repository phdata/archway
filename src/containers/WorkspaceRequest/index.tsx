import * as React from 'react';
import { Button, Col, Icon, Row, Tabs } from 'antd';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { RequestInput } from '../../models/RequestInput';
import { Workspace } from '../../models/Workspace';
import * as actions from './actions';
import { BehaviorPage, OverviewPage, SummaryPage } from './components';
import * as selectors from './selectors';
import { Profile } from '../../models/Profile';

interface Props {
    behavior?: string;
    request: RequestInput;
    selectedPage: number;
    workspace?: Workspace;
    ready: boolean;
    completeEnabled: boolean;
    profile: Profile;

    setBehavior: (behavior: string) => void;
    setRequest: (request: RequestInput) => void;
    setPage: (page: number) => void;
    requestWorkspace: () => void;
}

const WorkspaceRequest =
  ({
    requestWorkspace,
    completeEnabled,
    ready,
    behavior,
    workspace,
    setBehavior,
    setRequest,
    setPage,
    selectedPage,
    profile: { name },
  }: Props) => {
    const nextPage = () => setPage(selectedPage + 1);
    const previousPage = () => setPage(selectedPage - 1);

    return (
    <div style={{ textAlign: 'center', color: 'black' }}>
      <h1>New Workspace Request</h1>

      <Tabs tabBarStyle={{ textAlign: 'center' }} activeKey={`${selectedPage}`}>
        <Tabs.TabPane tab="Behavior" key="1">
          <BehaviorPage
            selected={behavior}
            onChange={setBehavior} />
        </Tabs.TabPane>
        <Tabs.TabPane tab="Details" key="2">
          <OverviewPage
            setRequest={setRequest} />
        </Tabs.TabPane>
        <Tabs.TabPane tab="Review" key="3">
          <SummaryPage
            workspace={workspace}
            name={name} />
        </Tabs.TabPane>
      </Tabs>

      <Row type="flex" justify="center" gutter={16}>
        {selectedPage > 1 && (
          <Col span={5}>
            <Button
              size="large"
              onClick={previousPage}
              type="primary"
              block={true}>
              <Icon type="left" />Previous
              </Button>
          </Col>
        )}
        {completeEnabled && (
          <Col span={5}>
            <Button
              style={{ display: completeEnabled ? 'inline-block' : 'none' }}
              size="large"
              onClick={requestWorkspace}
              type="primary"
              block={true}>
              <Icon type="inbox" />Request
            </Button>
          </Col>
        )}
        {selectedPage < 3 && (
          <Col span={5}>
            <Button
              disabled={!ready}
              size="large"
              onClick={nextPage}
              type="primary"
              block={true}>
              Next<Icon type="right" />
            </Button>
          </Col>
        )}
      </Row>
    </div>
  );
  };

const mapStateToProps = () =>
  createStructuredSelector({
    behavior: selectors.getBehavior(),
    request: selectors.getRequest(),
    selectedPage: selectors.getSelectedPage(),
    workspace: selectors.getGeneratedWorkspace(),
    ready: selectors.isReady(),
    completeEnabled: selectors.isCompleteEnabled(),
    profile: selectors.getProfile(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  setBehavior: (behavior: string) => dispatch(actions.setBehavior(behavior)),
  setRequest: (request: RequestInput) => dispatch(actions.setRequest(request)),
  setPage: (page: number) => dispatch(actions.setPage(page)),
  requestWorkspace: () => dispatch(actions.workspaceRequested()),
});

const mergeProps = (stateProps: any, dispatchProps: any, ownProps: Props) => ({
  ...ownProps,
  ...stateProps,
  ...dispatchProps,
  setRequest: (request: RequestInput) =>
    dispatchProps.setRequest(Object.assign(stateProps.request, request)),
});

export default connect(mapStateToProps, mapDispatchToProps, mergeProps)(WorkspaceRequest);
