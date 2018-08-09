import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import { Spin, Icon, Tabs, } from 'antd';

import DBDisplay from '../DBDisplay';
import ProcessingDisplay from '../ProcessingDisplay';
import Members from '../Members';
import Topics from '../Topics';
import Status from '../Status';
import Applications from '../Applications';
import { getWorkspace } from './actions';

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
        <h1>{activeWorkspace && activeWorkspace.name}</h1>
        <Tabs size="large">
          <Tabs.TabPane key="status" tab={<span><Icon type="info-circle-o" /> Status</span>}>
            <Status workspace={activeWorkspace} profile={profile} />
          </Tabs.TabPane>
          <Tabs.TabPane key="data" tab={<span><Icon type="database" /> Data ({activeWorkspace.data.length})</span>}>
            <DBDisplay workspace={activeWorkspace} cluster={cluster} />
          </Tabs.TabPane>
          {activeWorkspace && activeWorkspace.approved && (
            <Tabs.TabPane key="topics" tab={<span><Icon type="message" /> Topics ({activeWorkspace.topics.length})</span>}>
              <Topics />
            </Tabs.TabPane>
          )}
          <Tabs.TabPane key="processing" tab={<span><Icon type="dashboard" /> Pools ({activeWorkspace.processing.length})</span>}>
            <ProcessingDisplay />
          </Tabs.TabPane>
          {activeWorkspace && activeWorkspace.approved && (
            <Tabs.TabPane key="applications" tab={<span><Icon type="code" /> Applications ({activeWorkspace.applications.length})</span>}>
              <Applications />
            </Tabs.TabPane>
          )}
          {activeWorkspace && activeWorkspace.approved && (
            <Tabs.TabPane key="members" tab={<span><Icon type="team" /> Members</span>}>
              <Members memberForm={memberForm} existingMembers={existingMembers} newMembers={newMembers} />
            </Tabs.TabPane>
          )}
        </Tabs>
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
  }), { getWorkspace }
)(WorkspaceDetails);