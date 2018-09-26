import { Col, Row, Spin } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';
import { NamespaceInfo, Workspace, YarnApplication, PoolInfo } from '../../types/Workspace';
import * as actions from './actions';
import ComplianceDetails from './Components/ComplianceDetails';
import DescriptionDetails from './Components/DescriptionDisplay';
import HiveDetails from './Components/HiveDetails';
import KafkaDetails from './Components/KafkaDetails';
import Liason from './Components/Liason';
import MemberList from './Components/MemberList';
import YarnDetails from './Components/YarnDetails';
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
  pools?: PoolInfo[];
  infos?: NamespaceInfo[];

  getWorkspaceDetails: (id: number) => void;
  getTableList: (id: number) => void;
  getApplicationList: (id: number) => void;
}

class WorkspaceDetails extends React.PureComponent<Props> {

  public componentDidMount() {
    const id = this.props.match.params.id;
    this.props.getWorkspaceDetails(id);
  }

  public render() {
    const { workspace, cluster, loading, pools, infos } = this.props;

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
                }}
                ><TimeAgo datetime={workspace.requested_date} />
            </div>
          </div>
          <Row gutter={12} type="flex">
            <Col span={12} lg={6} style={{ marginTop: 10, display: 'flex' }}>
              <DescriptionDetails
                description={workspace.description} />
            </Col>
            <Col span={12} lg={6} style={{ marginTop: 10, display: 'flex' }}>
              <ComplianceDetails
                pii={workspace.compliance.pii_data}
                pci={workspace.compliance.pci_data}
                phi={workspace.compliance.phi_data} />
            </Col>
            <Col span={12} lg={6} style={{ marginTop: 10, display: 'flex' }}>
              <Liason liason={workspace.requester} />
            </Col>
            <Col span={12} lg={6} style={{ marginTop: 10, display: 'flex' }}>
              <Liason liason={workspace.requester} />
            </Col>
          </Row>
          <Row gutter={12} type="flex" style={{ flexDirection: 'row' }}>
            <Col span={24} lg={12} style={{ marginTop: 10 }}>
              <HiveDetails
                hue={cluster.services && cluster.services.hue}
                namespace={workspace.data[0].name}
                info={infos} />
            </Col>
            <Col span={24} lg={12} style={{ marginTop: 10 }}>
              <YarnDetails
                poolName={workspace.processing[0].pool_name}
                pools={pools} />
            </Col>
            <Col span={24} lg={12} style={{ marginTop: 10 }}>
              <KafkaDetails
                consumerGroup={workspace.applications[0] && workspace.applications[0].consumer_group}
                topics={workspace.topics} />
            </Col>
            <Col span={24} lg={12} style={{ marginTop: 10 }}>
              <MemberList />
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
    infos: selectors.getNamespaceInfo(),
    pools: selectors.getPoolInfo(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),
});

export default connect(mapStateToProps, mapDispatchToProps)(WorkspaceDetails);
