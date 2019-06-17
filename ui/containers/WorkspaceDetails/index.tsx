import * as React from 'react';
import { Button, Spin, Tabs, Modal, Tooltip, notification } from 'antd';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { throttle } from 'lodash';
import {
  Status,
  Liaison,
  Info,
  Compliance,
  QuickLinks,
  OverviewTab,
  DataTab,
  ApplicationsTab,
  MessagingTab,
  KafkaTopicRequest,
  SimpleMemberRequest,
  SimpleTopicMemberRequest,
  ApplicationRequest,
} from './components';
import { Cluster } from '../../models/Cluster';
import { Profile } from '../../models/Profile';
import {
  NamespaceInfoList,
  ResourcePoolsInfo,
  Workspace,
  UserSuggestions,
  HiveAllocation,
  Member,
  KafkaTopic,
  Application,
} from '../../models/Workspace';
import * as actions from './actions';
import * as selectors from './selectors';

interface DetailsRouteProps {
  id: any;
}

interface Props extends RouteComponentProps<DetailsRouteProps> {
  workspace?: Workspace;
  cluster: Cluster;
  profile: Profile;
  pools?: ResourcePoolsInfo;
  infos?: NamespaceInfoList;
  approved: boolean;
  activeApplication?: Application;
  activeModal?: string;
  selectedAllocation?: HiveAllocation;
  userSuggestions?: UserSuggestions;
  liasion?: Member;
  members?: Member[];

  clearDetails: () => void;
  getWorkspaceDetails: (id: number) => void;
  selectApplication: (application: Application) => void;
  showTopicDialog: (e: React.MouseEvent) => void;
  showSimpleMemberDialog: (e: React.MouseEvent) => void;
  showSimpleTopicMemberDialog: (e: React.MouseEvent) => void;
  showApplicationDialog: (e: React.MouseEvent) => void;
  clearModal: () => void;
  approveRisk: (e: React.MouseEvent) => void;
  approveOperations: (e: React.MouseEvent) => void;
  requestTopic: () => void;
  simpleMemberRequest: (resource: string) => void;
  changeMemberRoleRequest: (distinguished_name: string, roleId: number, role: string, resource: string) => void;
  requestApplication: () => void;
  updateSelectedAllocation: (allocation: HiveAllocation) => void;
  requestRefreshYarnApps: () => void;
  requestRefreshHiveTables: () => void;
  getUserSuggestions: (filter: string) => void;
  removeMember: (distinguished_name: string, roleId: number, resource: string) => void;
}

class WorkspaceDetails extends React.PureComponent<Props> {
  public delayedFetchUsers = throttle((v: string) => {
    this.props.getUserSuggestions(v);
  }, 2000);

  public componentDidMount() {
    // clear previous details data
    this.props.clearDetails();

    const {
      match: {
        params: { id },
      },
    } = this.props;
    this.props.getWorkspaceDetails(id);
  }

  public componentWillReceiveProps(nextProps: Props) {
    const {
      match: {
        params: { id: oldId },
      },
    } = this.props;
    const {
      match: {
        params: { id },
      },
    } = nextProps;
    if (oldId !== id) {
      this.props.clearDetails();
      this.props.getWorkspaceDetails(id);
    }

    const { workspace } = this.props;
    const { workspace: newWorkspace } = nextProps;
    const riskStatus = workspace && workspace.approvals && workspace.approvals.risk && workspace.approvals.risk.status;
    const infraStatus =
      workspace && workspace.approvals && workspace.approvals.infra && workspace.approvals.infra.status;
    const newRiskStatus =
      newWorkspace && newWorkspace.approvals && newWorkspace.approvals.risk && newWorkspace.approvals.risk.status;
    const newInfraStatus =
      newWorkspace && newWorkspace.approvals && newWorkspace.approvals.infra && newWorkspace.approvals.infra.status;
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

  public handleMemberSearch = (v: string) => {
    if (v.length >= 3) {
      this.delayedFetchUsers(v);
    }
  };

  public convertJSONtoBase64 = (): string => {
    const { workspace } = this.props;
    const base64Workspace = btoa(JSON.stringify(workspace, null, 4));
    return base64Workspace;
  };

  public render() {
    const {
      workspace,
      cluster,
      pools,
      infos,
      approved,
      members,
      activeApplication,
      activeModal,
      selectApplication,
      showTopicDialog,
      showSimpleMemberDialog,
      showSimpleTopicMemberDialog,
      showApplicationDialog,
      clearModal,
      approveRisk,
      approveOperations,
      profile,
      requestTopic,
      simpleMemberRequest,
      changeMemberRoleRequest,
      requestApplication,
      selectedAllocation,
      updateSelectedAllocation,
      requestRefreshYarnApps,
      requestRefreshHiveTables,
      userSuggestions,
      removeMember,
      liasion,
    } = this.props;

    if (!workspace) {
      return (
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Spin />
        </div>
      );
    }

    return (
      <div style={{ height: '100%' }}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            backgroundColor: 'white',
            padding: 16,
          }}
        >
          <Status ready={approved} createdAt={workspace.requested_date} />
          <Liaison data={liasion} />
        </div>

        <div style={{ backgroundColor: 'white', padding: '16px 0' }}>
          <Info behavior={workspace.behavior} name={workspace.name} summary={workspace.summary} />
          <Compliance
            pii={workspace.compliance.pii_data}
            pci={workspace.compliance.pci_data}
            phi={workspace.compliance.phi_data}
          />
        </div>
        <Tooltip placement="topLeft" title="Download Workspace JSON data" arrowPointAtCenter>
          <Button
            shape="circle"
            icon="cloud-download"
            style={{ marginLeft: '15px', marginTop: '10px', zIndex: 2, border: 'none' }}
            href={`data:application/octet-stream;charset=utf-16le;base64,${this.convertJSONtoBase64()}`}
            download={`${workspace.name}`}
          />
        </Tooltip>
        <Tabs
          tabBarStyle={{
            textAlign: 'center',
            margin: 0,
            padding: '0 16px 0 156px',
            height: 56,
            backgroundColor: 'white',
          }}
          defaultActiveKey="overview"
          tabBarExtraContent={
            <QuickLinks
              hue={cluster.services && cluster.services.hue}
              yarn={cluster.services && cluster.services.yarn}
              selectedAllocation={selectedAllocation}
            />
          }
          style={{ marginTop: '-42px' }}
        >
          <Tabs.TabPane tab="OVERVIEW" key="overview">
            <OverviewTab
              workspace={workspace}
              profile={profile}
              approveRisk={approveRisk}
              approveOperations={approveOperations}
            />
          </Tabs.TabPane>
          <Tabs.TabPane tab="DATA" key="data">
            <DataTab
              workspace={workspace}
              profile={profile}
              cluster={cluster}
              infos={infos}
              members={members}
              onAddMember={showSimpleMemberDialog}
              removeMember={removeMember}
              selectedAllocation={selectedAllocation}
              onChangeAllocation={updateSelectedAllocation}
              onChangeMemberRole={changeMemberRoleRequest}
              requestRefreshHiveTables={requestRefreshHiveTables}
            />
          </Tabs.TabPane>
          <Tabs.TabPane tab="APPLICATIONS" key="applications">
            <ApplicationsTab
              workspace={workspace}
              yarn={cluster.services && cluster.services.yarn}
              pools={pools}
              selectedApplication={activeApplication}
              onAddApplication={showApplicationDialog}
              onRefreshPools={requestRefreshYarnApps}
              onSelectApplication={selectApplication}
            />
          </Tabs.TabPane>
          <Tabs.TabPane tab="MESSAGING" key="messaging">
            <MessagingTab
              workspace={workspace}
              profile={profile}
              members={members}
              onAddTopic={showTopicDialog}
              onAddMember={showSimpleTopicMemberDialog}
              onChangeMemberRole={changeMemberRoleRequest}
              removeMember={removeMember}
            />
          </Tabs.TabPane>
        </Tabs>
        <Modal
          visible={activeModal === 'simpleMember'}
          title="Add A Member"
          onCancel={clearModal}
          onOk={() => simpleMemberRequest('data')}
        >
          <SimpleMemberRequest
            allocations={workspace.data}
            suggestions={userSuggestions}
            onSearch={this.handleMemberSearch}
          />
        </Modal>
        <Modal
          visible={activeModal === 'simpleTopicMember'}
          title="Add A Member"
          onCancel={clearModal}
          onOk={() => simpleMemberRequest('topics')}
        >
          <SimpleTopicMemberRequest suggestions={userSuggestions} onSearch={this.handleMemberSearch} />
        </Modal>
        <Modal visible={activeModal === 'kafka'} title="New Topic" onCancel={clearModal} onOk={requestTopic}>
          <KafkaTopicRequest />
        </Modal>
        <Modal
          visible={activeModal === 'application'}
          title="New Application"
          onCancel={clearModal}
          onOk={requestApplication}
        >
          <ApplicationRequest />
        </Modal>
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
    activeTopic: selectors.getActiveTopic(),
    activeApplication: selectors.getActiveApplication(),
    activeModal: selectors.getActiveModal(),
    userSuggestions: selectors.getUserSuggestions(),
    liasion: selectors.getLiaison(),
    members: selectors.getMembers(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  clearDetails: () => dispatch(actions.clearDetails()),
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),

  updateSelectedAllocation: (allocation: HiveAllocation) => dispatch(actions.updateSelectedAllocation(allocation)),

  selectTopic: (topic: KafkaTopic) => dispatch(actions.setActiveTopic(topic)),
  selectApplication: (application: Application) => dispatch(actions.setActiveApplication(application)),

  showTopicDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('kafka'));
  },
  showSimpleMemberDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('simpleMember'));
  },
  showSimpleTopicMemberDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('simpleTopicMember'));
  },
  showApplicationDialog: (e: React.MouseEvent) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal('application'));
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
  simpleMemberRequest: (resource: string) => dispatch(actions.simpleMemberRequest(resource)),
  changeMemberRoleRequest: (distinguished_name: string, roleId: number, role: string, resource: string) =>
    dispatch(actions.changeMemberRoleRequest(distinguished_name, roleId, role, resource)),
  requestApplication: () => dispatch(actions.requestApplication()),
  requestRefreshYarnApps: () => dispatch(actions.requestRefreshYarnApps()),
  requestRefreshHiveTables: () => dispatch(actions.requestRefreshHiveTables()),
  getUserSuggestions: (filter: string) => dispatch(actions.getUserSuggestions(filter)),

  removeMember: (distinguished_name: string, roleId: number, resource: string) =>
    dispatch(actions.requestRemoveMember(distinguished_name, roleId, resource)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(WorkspaceDetails));
