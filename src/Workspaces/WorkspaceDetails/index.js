import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Spin, Icon, Tabs, Card, Steps } from 'antd';

import DBDisplay from '../DBDisplay';
import ProcessingDisplay from '../ProcessingDisplay';
import Members from '../Members';
import Topics from '../Topics';
import Status from '../Status';
import Applications from '../Applications';
import { getWorkspace, setTab } from './actions';

class WorkspaceDetails extends React.Component {

  componentDidMount() {
    this.props.getWorkspace(this.props.match.params.id);
  }

  render() {
    const { workspaces: { activeWorkspace, memberForm, existingMembers, newMembers }, cluster, profile } = this.props;

    if (!activeWorkspace)
      return <Spin />;

    return (
      <div>
        <Card>
          <h1>{activeWorkspace && activeWorkspace.name}</h1>
        </Card>
        <Card
          style={{ marginTop: 15 }}
          title="Status"
          avatar={<Icon type="info-circle-o" />}>
          <Steps current={Object.keys(activeWorkspace.approvals).length || 0 }>
            <Steps.Step title="Requested" description="Waiting for approvals." />
            <Steps.Step title="In Review" description="Partially approved." />
            <Steps.Step title="Ready" description="Ready for use." />
          </Steps>
        </Card>
        <Status workspace={activeWorkspace} profile={profile} />
        <Card
          style={{ marginTop: 15 }}
          title="Databases">
          <DBDisplay workspace={activeWorkspace} cluster={cluster} />
        </Card>
        <Card
          style={{ marginTop: 15 }}
          title="Resource Pools">
          <ProcessingDisplay workspace={activeWorkspace} />
        </Card>
        <Topics />
        <Applications />
        <Card
          style={{ marginTop: 15 }}
          title="Members">
          <Members memberForm={memberForm} existingMembers={existingMembers} newMembers={newMembers} />
        </Card>
      </div>
    );
  }

}

WorkspaceDetails.propTypes = {
  getWorkspace: PropTypes.func.isRequired,
};

export default connect(
  state => ({
    workspaces: state.workspaces.details,
    cluster: state.cluster,
    profile: state.auth.profile,
  }), {
    getWorkspace,
    setTab,
  }
)(WorkspaceDetails);
