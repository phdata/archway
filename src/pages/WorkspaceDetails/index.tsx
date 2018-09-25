import { Card, Col, Icon, Row, Spin } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';
import { Workspace, Member, YarnApplication, HiveTable } from '../../types/Workspace';
import MemberList from './MemberList';
import HiveDetails from './HiveDetails';
import * as actions from './actions';
import * as selectors from './selectors';
import YarnDetails from './YarnDetails';

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
  members?: Member[];
  applications?: YarnApplication[];
  tables?: HiveTable[];

  getWorkspaceDetails: (id: number) => void;
  getTableList: (id: number) => void;
  getApplicationList: (id: number) => void;
}

const Compliance = ({ checked, children }: {checked: boolean, children: string}) => (
  <div style={{ display: 'flex', flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
    <Icon type={checked ? 'warning' : 'dash'} style={{ marginBottom: 5, fontSize: 28 }} />
    <div style={{ letterSpacing: 1 }}>{children}</div>
  </div>
);

const Label = ({ style, children }: {style?: React.CSSProperties, children: any}) =>
  <div
    style={{
        marginBottom: 10,
        fontSize: 14,
        textTransform: 'uppercase',
        letterSpacing: 1,
        fontWeight: 200,
        display: 'flex',
        alignItems: 'center',
        ...style,
      }}>
    {children}
  </div>;

class WorkspaceDetails extends React.PureComponent<Props> {

  public componentDidMount() {
    const id = this.props.match.params.id;
    this.props.getWorkspaceDetails(id);
  }

  public render() {
    const { workspace, loading, members, applications, tables } = this.props;

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
            <Col span={8}>
              <Card>
                <Label style={{ lineHeight: '18px' }}>
                  <Icon type="database" style={{ paddingRight: 5, fontSize: 18 }} />Hive
                </Label>
                <HiveDetails namespace={workspace.data[0].name} tables={tables} />
              </Card>
            </Col>
            <Col span={8}>
              <Card>
                <Label style={{ lineHeight: '18px' }}>
                  <Icon type="rocket" style={{ paddingRight: 5, fontSize: 18 }} />Yarn
                </Label>
                <YarnDetails poolName={workspace.processing[0].pool_name} applications={applications} />
              </Card>
            </Col>
            <Col span={8}>
              <Card>
                <Label style={{ lineHeight: '18px' }}>
                  <Icon type="sound" style={{ paddingRight: 5, fontSize: 18 }} />Kafka
                </Label>
                <MemberList members={members} />
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
    members: selectors.getMembers(),
    tables: selectors.getHiveTables(),
    applications: selectors.getApplications(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceDetails);
