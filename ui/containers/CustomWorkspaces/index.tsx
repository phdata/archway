import * as React from 'react';
import { connect } from 'react-redux';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { createStructuredSelector } from 'reselect';
import { Col, Row, Button, Icon } from 'antd';

import Behavior from '../../components/Behavior';
import * as workspaceRequestActions from '../WorkspaceRequest/actions';
import * as selectors from './selectors';
import * as workspaceRequestSelectors from '../WorkspaceRequest/selectors';
import { CustomDescription } from '../../models/Template';

interface Props extends RouteComponentProps<any> {
  customDescriptions: CustomDescription[];
  currentBehavior: string;
  listCustomDescriptions: () => void;
  setBehavior: (behavior: string) => void;
  gotoNextPage: () => void;
  gotoPrevPage: () => void;
  setCurrentPage: (page: string) => void;
}

class CustomWorkspaces extends React.PureComponent<Props> {
  public renderBehaviors() {
    const { customDescriptions, currentBehavior, setBehavior } = this.props;
    return customDescriptions.map((item, index) => (
      <Col span={6} key={index}>
        <Behavior
          behaviorKey={item.name}
          selected={currentBehavior === item.name}
          onChange={(behavior, checked) => checked && setBehavior(behavior)}
          icon="select"
          title={item.name}
          description={item.description}
          useCases={['brainstorming', 'evaluation', 'prototypes']}
        />
      </Col>
    ));
  }

  public render() {
    const { currentBehavior, gotoNextPage, gotoPrevPage } = this.props;
    return (
      <div style={{ textAlign: 'center', color: 'black', paddingLeft: 15, paddingRight: 15 }}>
        <h1 style={{ padding: 24, margin: 0 }}>Custom Workspaces List</h1>
        <Row gutter={8}>{this.renderBehaviors()}</Row>
        <Row type="flex" justify="center" gutter={16}>
          <Col span={5}>
            <Button size="large" type="primary" block onClick={gotoPrevPage}>
              <Icon type="left" />
              Previous
            </Button>
          </Col>
          <Col span={5}>
            <Button size="large" type="primary" block disabled={!currentBehavior} onClick={gotoNextPage}>
              <Icon type="right" />
              Next
            </Button>
          </Col>
        </Row>
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    customDescriptions: selectors.getCustomDescriptions(),
    currentBehavior: workspaceRequestSelectors.getBehavior(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  setBehavior: (behavior: string) => dispatch(workspaceRequestActions.setBehavior(behavior)),
  gotoNextPage: () => dispatch(workspaceRequestActions.gotoNextPage()),
  gotoPrevPage: () => dispatch(workspaceRequestActions.gotoPrevPage()),
  setCurrentPage: (page: string) => dispatch(workspaceRequestActions.setCurrentPage(page)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(CustomWorkspaces));
