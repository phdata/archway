import * as React from 'react';
import { Col, Row, Spin, Modal, notification } from 'antd';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Cluster } from '../../types/Cluster';
import { Profile } from '../../types/Profile';
import { NamespaceInfo, ResourcePoolsInfo, Workspace, HiveAllocation } from '../../types/Workspace';
import * as actions from './actions';
import {
  ApprovalDetails,
  ComplianceDetails,
  DescriptionDetails,
  HiveDetails,
  KafkaDetails,
  Liaison,
  MemberList,
  YarnDetails,
  Allocations,
  KafkaTopicRequest,
  PrepareHelp,
  RunHelp,
  CreateHelp,
  SimpleMemberRequest,
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
    pools?: ResourcePoolsInfo;
    infos?: NamespaceInfo[];
    approved: boolean;
    activeModal?: string;
    selectedAllocation?: HiveAllocation;

    getWorkspaceDetails: (id: number) => void;
    showTopicDialog: (e: React.MouseEvent) => void;
    showSimpleMemberDialog: (e: React.MouseEvent) => void;
    clearModal: () => void;
    approveRisk: (e: React.MouseEvent) => void;
    approveOperations: (e: React.MouseEvent) => void;
    requestTopic: () => void;
    simpleMemberRequest: () => void;
    updateSelectedAllocation: (allocation: HiveAllocation) => void;
    requestRefreshYarnApps: () => void;
}

class WorkspaceDetails extends React.PureComponent<Props> {

  public componentDidMount() {
    const { match: { params: { id } } } = this.props;
    this.props.getWorkspaceDetails(id);
  }

  public componentWillReceiveProps(nextProps: Props) {
    const { workspace } = this.props;
    const { workspace: newWorkspace } = nextProps;
    const riskStatus = workspace && workspace.approvals
      && workspace.approvals.risk && workspace.approvals.risk.status;
    const infraStatus = workspace && workspace.approvals
      && workspace.approvals.infra && workspace.approvals.infra.status;
    const newRiskStatus = newWorkspace && newWorkspace.approvals
      && newWorkspace.approvals.risk && newWorkspace.approvals.risk.status;
    const newInfraStatus = newWorkspace && newWorkspace.approvals
      && newWorkspace.approvals.infra && newWorkspace.approvals.infra.status;
    if (riskStatus && riskStatus.loading) {
      if (newRiskStatus && (newRiskStatus.success === true || newRiskStatus.success === false)) {
        this.showApprovalNotification('Risk', newRiskStatus.error);
      }
    }
    if (infraStatus && infraStatus.loading) {
      if (newInfraStatus && (newInfraStatus.success === true || newInfraStatus.success === false)) {
        this.showApprovalNotification('Infra', newInfraStatus.error);
      }
    }
  }

  public showApprovalNotification(type: string, error: string | undefined) {
    if (!error) {
      notification.open({
        message: `${type} Successfully Approved`,
        description: `Your ${type.toLowerCase()} approval was successful`,
      });
    } else {
      notification.open({
        message: `${type} NOT Approved`,
        description: `Your ${type.toLowerCase()} approval failed due to the following error: ${error}`,
      });
    }
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
      showSimpleMemberDialog,
      clearModal,
      approveRisk,
      approveOperations,
      profile,
      requestTopic,
      simpleMemberRequest,
      selectedAllocation,
      updateSelectedAllocation,
      requestRefreshYarnApps,
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
              {selectedAllocation && (
                <Allocations
                  location={selectedAllocation.location}
                  allocated={selectedAllocation.size_in_gb}
                  consumed={selectedAllocation.consumed_in_gb}
                />
              )}
            </Col>
            <Col span={12} xxl={4} style={{ marginTop: 10, display: 'flex' }}>
              <ApprovalDetails
                risk={workspace.approvals && workspace.approvals.risk}
                infra={workspace.approvals && workspace.approvals.infra}
                approveOperations={
                  (profile.permissions && profile.permissions.platform_operations) ? approveOperations : undefined
                }
                approveRisk={
                  (profile.permissions && profile.permissions.risk_management) ? approveRisk : undefined
                }
            />
            </Col>
          </Row>
          {approved && (
            <Row gutter={12} type="flex" style={{ alignItems: 'stretch' }}>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <HiveDetails
                  hue={cluster.services && cluster.services.hue}
                  allocations={workspace.data}
                  info={infos}
                  selectedAllocation={selectedAllocation}
                  onChangeAllocation={updateSelectedAllocation} />
              </Col>
              <Col span={24} lg={12} xxl={6} style={{ marginTop: 10 }}>
                <YarnDetails
                  yarn={cluster.services && cluster.services.yarn}
                  poolName={workspace.processing[0].pool_name}
                  pools={pools}
                  onRefreshPools={requestRefreshYarnApps}
                />
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
                <MemberList
                  showModal={showSimpleMemberDialog} />
                <Modal
                  visible={activeModal === 'simpleMember'}
                  title="Add A Member"
                  onCancel={clearModal}
                  onOk={simpleMemberRequest}>
                  <SimpleMemberRequest />
                </Modal>
              </Col>
            </Row>
          )}
          {selectedAllocation && (
            <Row gutter={12}>
              <Col span={24} xxl={8} style={{ marginTop: 10 }}>
                <PrepareHelp
                  location={selectedAllocation.location}
                  namespace={selectedAllocation.name} />
              </Col>
              <Col span={24} xxl={8} style={{ marginTop: 10 }}>
                <CreateHelp
                  host={cluster.services.hive.thrift[0].host}
                  port={cluster.services.hive.thrift[0].port}
                  namespace={selectedAllocation.name} />
              </Col>
              <Col span={24} xxl={8} style={{ marginTop: 10 }}>
                <RunHelp
                  queue={workspace.processing[0].pool_name} />
              </Col>
            </Row>
          )}
      </div>
    );
  }

}

const mapStateToProps = () =>
  createStructuredSelector({
    workspace: selectors.getWorkspace(),
    cluster: selectors.getClusterDetails(),
    selectedAllocation: selectors.getSelectedAllocation(),
    profile: selectors.getProfile(),
    infos: selectors.getNamespaceInfo(),
    pools: selectors.getPoolInfo(),
    approved: selectors.getApproved(),
    activeModal: selectors.getActiveModal(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),

  updateSelectedAllocation: (allocation: HiveAllocation) => dispatch(actions.updateSelectedAllocation(allocation)),

  showTopicDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('kafka'));
  },
  showSimpleMemberDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('simpleMember'));
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
  simpleMemberRequest: () => dispatch(actions.simpleMemberRequest()),
  requestRefreshYarnApps: () => dispatch(actions.requestRefreshYarnApps()),
});

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(WorkspaceDetails));
