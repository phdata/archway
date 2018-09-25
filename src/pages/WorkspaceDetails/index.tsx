import { Card, Col, Icon, Row, Spin } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';
import { Workspace } from '../../types/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';

/* tslint:disable:no-var-requires */
const TimeAgo = require('timeago-react').default;

interface DetailsRouteProps {
  id: number;
}

interface Props extends RouteComponentProps<DetailsRouteProps> {
  workspace?: Workspace;
  cluster: Cluster;
  profile: Profile;
  loading: boolean;

  getWorkspaceDetails: (id: number) => void;
}

const Compliance = ({ checked, children }: {checked: boolean, children: string}) => (
  <div style={{ display: 'flex', flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
    <Icon type={checked ? 'warning' : 'dash'} style={{ marginBottom: 5, fontSize: 28 }} />
    <div style={{ letterSpacing: 1 }}>{children}</div>
  </div>
);

const Label = ({ children }: {children: string}) =>
  <div style={{ marginBottom: 10, fontSize: 14, textTransform: 'uppercase', letterSpacing: 1, fontWeight: 200 }}>
    {children}
  </div>;

class WorkspaceDetails extends React.PureComponent<Props> {

  public componentDidMount() {
    this.props.getWorkspaceDetails(this.props.match.params.id);
  }

  public render() {
    const { workspace, loading } = this.props;

    if (loading || !workspace) { return <Spin />; }

    return (
      <div>
          <div style={{ textAlign: 'center' }}>
            <h1 style={{ marginBottom: 0 }}>{workspace!.name}</h1>
            <div>{workspace!.summary}</div>
            <div
              style={{
                  textTransform: 'uppercase',
                  fontSize: 14,
                  color: '#aaa',
                }}><TimeAgo datetime={workspace.requested_date} /></div>
          </div>
          <Row gutter={12} style={{ display: 'flex', marginTop: 15 }}>
            <Col span={8} style={{ textAlign: 'center', display: 'flex' }}>
              <Card
                style={{ display: 'flex', flex: 1 }}
                bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                <Label>description</Label>
                <p style={{ marginBottom: 0 }}>
                  {workspace.description}
                </p>
              </Card>
            </Col>
            <Col span={8} style={{ textAlign: 'center', display: 'flex' }}>
              <Card
                style={{ display: 'flex', flex: 1 }}
                bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                <Label>compliance</Label>
                <div style={{ display: 'flex', flex: 1, alignItems: 'center' }}>
                  <Compliance checked={workspace.compliance.pci_data}>PCI</Compliance>
                  <Compliance checked={workspace.compliance.phi_data}>PHI</Compliance>
                  <Compliance checked={workspace.compliance.pii_data}>PII</Compliance>
                </div>
              </Card>
            </Col>
            <Col span={8} style={{ textAlign: 'center', display: 'flex' }}>
              <Card
                style={{ display: 'flex', flex: 1 }}
                bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                <Label>liason</Label>
                <div
                  style={{
                      textAlign: 'center',
                      display: 'flex',
                      flexDirection: 'column',
                      flex: 1,
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}>
                  <Icon type="crown" style={{ marginBottom: 5, fontSize: 28 }} />
                  <div style={{ letterSpacing: 1, textTransform: 'uppercase' }}>{workspace.requester}</div>
                </div>
              </Card>
            </Col>
          </Row>
          <Row gutter={12} style={{ display: 'flex', marginTop: 10 }}>
            <Col span={24}>
              <Card>
                <Label>Members</Label>
                <MemberList />
              </Card>
            </Col>
          </Row>
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
