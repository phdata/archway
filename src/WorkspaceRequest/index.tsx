import { Button, Col, Row, Tabs } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import * as actions from './actions';
import * as selectors from './selectors';
import BehaviorPage from './BehaviorPage';
import { RequestInput } from './model';
import OverviewPage from './OverviewPage';
import SummaryPage from './SummaryPage';
import { Workspace } from '../WorkspaceListing/Workspace';

interface Props {
  behavior: string
  request: RequestInput
  selectedPage: number
  workspace: Workspace

  setBehavior: (behavior: string) => void
  setRequest: (request: RequestInput) => void
  setPage: (page: number) => void
}

const WorkspaceRequest = ({ behavior, workspace, setBehavior, setRequest, setPage, selectedPage }: Props) => {
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
          <OverviewPage setRequest={setRequest} />
        </Tabs.TabPane>
        <Tabs.TabPane tab="Review" key="3">
          <SummaryPage workspace={workspace} name='Benny Thompson' />
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
              Previous
              </Button>
          </Col>
        )}
        {selectedPage < 3 && (
          <Col span={5}>
            <Button
              size="large"
              onClick={nextPage}
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

const mapStateToProps = () =>
  createStructuredSelector({
    behavior: selectors.getBehavior(),
    request: selectors.getRequest(),
    selectedPage: selectors.getSelectedPage(),
    workspace: selectors.getGeneratedWorkspace(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  setBehavior: (behavior: string) => dispatch(actions.setBehavior(behavior)),
  setRequest: (request: RequestInput) => dispatch(actions.setRequest(request)),
  setPage: (page: number) => dispatch(actions.setPage(page)),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceRequest);
