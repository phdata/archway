import * as React from 'react';
import { Col, Row, Spin, Modal } from 'antd';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';
import { NamespaceInfo, PoolInfo, Workspace } from '../../types/Workspace';
import * as actions from './actions';
import {
  ApprovalDetails,
  ComplianceDetails,
  DescriptionDisplay as DescriptionDetails,
  HiveDetails,
  KafkaDetails,
  Liaison,
  MemberList,
  YarnDetails,
  SetupHelp,
  Allocations,
  KafkaTopicRequest,
 } from './components';
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
    pools?: PoolInfo[];
    infos?: NamespaceInfo[];
    approved: boolean;
    activeModal?: string;

    getWorkspaceDetails: (id: number) => void;
    showTopicDialog: (e: React.MouseEvent) => void;
    clearModal: () => void;
    approveRisk: (e: React.MouseEvent) => void;
    approveOperations: (e: React.MouseEvent) => void;
    requestTopic: () => void;
}

class WorkspaceDetails extends React.PureComponent<Props> {

    public componentDidMount() {
    const { match: { params: { id } } } = this.props;
    this.props.getWorkspaceDetails(id);
  }

    public render() {
    const {
      workspace,
      cluster,
      pools,
      infos,
      approved,
      activeModal,
      showTopicDialog,
      clearModal,
      approveRisk,
      approveOperations,
      profile,
      requestTopic,
    } = this.props;

    if (!workspace) { return <Spin />; }

    return (
      <div>
          <div style={{ textAlign: 'center' }}>
            <h1 style={{ marginBottom: 0 }}>
              {workspace!.name}
              <span
                style={{
                  verticalAlign: 'super',
                  fontSize: 10,
                  color: approved ? 'green' : 'red',
                  textTransform: 'uppercase',
                }}>
                {approved ? 'approved' : 'pending'}
              </span>
            </h1>
            <div>{workspace!.summary}</div>
            <div
              style={{
                textTransform: 'uppercase',
                fontSize: 12,
                color: '#aaa',
              }}>
              created <TimeAgo datetime={workspace.requested_date} />
            </div>
          </div>
          <Row gutter={12} type="flex">
            <Col span={24} xxl={8} style={{ marginTop: 10, display: 'flex' }}>
              <DescriptionDetails
                description={workspace.description} />
            </Col>
            <Col span={12} xxl={4} style={{ marginTop: 10, display: 'flex' }}>
              <ComplianceDetails
                pii={workspace.compliance.pii_data}
                pci={workspace.compliance.pci_data}
                phi={workspace.compliance.phi_data} />
            </Col>
            <Col span={12} xxl={4} style={{ marginTop: 10, display: 'flex' }}>
              <Liaison liaison={workspace.requester} />
            </Col>
            <Col span={12} xxl={4} style={{ marginTop: 10, display: 'flex' }}>
              <Allocations
                location={workspace.data[0] && workspace.data[0].location}
                allocated={workspace.data[0] && workspace.data[0].size_in_gb}
                consumed={workspace.data[0] && workspace.data[0].consumed_in_gb} />
            </Col>
            <Col span={12} xxl={4} style={{ marginTop: 10, display: 'flex' }}>
              <ApprovalDetails
                risk={workspace.approvals && workspace.approvals.risk}
                infra={workspace.approvals && workspace.approvals.infra}
                approveOperations={profile.permissions && profile.permissions.platform_operations && approveOperations}
                approveRisk={profile.permissions && profile.permissions.risk_management && approveRisk} />
            </Col>
          </Row>
          {approved && (
            <Row gutter={12} type="flex" style={{ alignItems: 'stretch' }}>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <HiveDetails
                  hue={cluster.services && cluster.services.hue}
                  namespace={workspace.data[0].name}
                  info={infos} />
              </Col>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <YarnDetails
                  yarn={cluster.services && cluster.services.yarn}
                  poolName={workspace.processing[0].pool_name}
                  pools={pools} />
              </Col>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <KafkaDetails
                  consumerGroup={workspace.applications[0] && workspace.applications[0].consumer_group}
                  topics={workspace.topics}
                  showModal={showTopicDialog} />
                <Modal
                  visible={activeModal === 'kafka'}
                  title="New Topic"
                  onCancel={clearModal}
                  onOk={requestTopic}>
                  <KafkaTopicRequest />
                </Modal>
              </Col>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <MemberList />
              </Col>
            </Row>
          )}
          <Row>
            <Col span={24} style={{ marginTop: 10 }}>
                <SetupHelp approved={approved} />
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
    approved: selectors.getApproved(),
    activeModal: selectors.getActiveModal(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),
  showTopicDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('kafka'));
  },
  approveRisk: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.requestApproval('risk'));
  },
  approveOperations: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.requestApproval('infra'));
  },
  clearModal: () => dispatch(actions.setActiveModal(false)),
  requestTopic: () => dispatch(actions.requestTopic()),
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(WorkspaceDetails));
