import { Card, Spin } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../types/Cluster';
import { Profile } from '../types/Profile';
import { Workspace } from '../types/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';

interface DetailsRouteProps {
  id: number
}

interface Props extends RouteComponentProps<DetailsRouteProps> {
  workspace?: Workspace
  cluster: Cluster
  profile: Profile
  loading: boolean

  getWorkspaceDetails: (id: number) => void
}

class WorkspaceDetails extends React.PureComponent<Props> {

  componentDidMount() {
    this.props.getWorkspaceDetails(this.props.match.params.id);
  }

  render() {
    const { workspace, cluster, profile, loading } = this.props;

    if (loading) return <Spin />;

    console.log(workspace);

    return (
      <div>
        <Card>
          <h1>{workspace!.name}</h1>
        </Card>
      </div>
    );
  }

}

const mapStateToProps = () =>
  createStructuredSelector({
    workspace: selectors.getWorkspace(),
    cluster: selectors.getClusterDetails(),
    profile: selectors.getProfile(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceDetails);
