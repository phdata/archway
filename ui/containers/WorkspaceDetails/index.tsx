import * as React from 'react';
import { Button, Spin, Tabs, Modal, Tooltip, notification } from 'antd';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { throttle } from 'lodash';
import {
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
  ManageTab,
  WarningText,
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
import { FeatureFlagType, ProvisioningType, ModalType } from '../../constants';
import { Provisioning } from '../../components';
import { getFeatureFlags } from '../../redux/selectors';

interface DetailsRouteProps {
  id: any;
}

interface ManageLoading {
  provision: boolean;
  deprovision: boolean;
  delete: boolean;
}

interface Props extends RouteComponentProps<DetailsRouteProps> {
  workspace?: Workspace;
  cluster: Cluster;
  profile: Profile;
  pools?: ResourcePoolsInfo;
  infos?: NamespaceInfoList;
  activeApplication?: Application;
  activeModal?: string;
  selectedAllocation?: HiveAllocation;
  userSuggestions?: UserSuggestions;
  liasion?: Member;
  members?: Member[];
  notificationInfo: any;
  memberLoading: boolean;
  provisioning: ProvisioningType;
  manageLoading: ManageLoading;
  featureFlags: string[];

  clearDetails: () => void;
  getWorkspaceDetails: (id: number) => void;
  selectApplication: (application: Application) => void;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
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
  deleteWorkspace: () => void;
  deprovisionWorkspace: () => void;
  provisionWorkspace: () => void;
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
      notificationInfo,
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

    const { type, message } = notificationInfo;

    if (!!message && message !== this.props.notificationInfo.message) {
      notification[type]({ message: type.toUpperCase(), description: message });
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
      members,
      activeApplication,
      activeModal,
      selectApplication,
      showModal,
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
      memberLoading,
      provisioning,
      deleteWorkspace,
      deprovisionWorkspace,
      provisionWorkspace,
      manageLoading,
      featureFlags,
    } = this.props;
    const hasApplicationFlag = featureFlags.includes(FeatureFlagType.Application);
    const hasMessagingFlag = featureFlags.includes(FeatureFlagType.Messaging);

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
            justifyContent: 'flex-end',
            backgroundColor: 'white',
            padding: 16,
          }}
        >
          <Liaison data={liasion} />
        </div>

        <div style={{ backgroundColor: 'white', padding: '16px 0' }}>
          <Info behavior={workspace.behavior} name={workspace.name} summary={workspace.summary} />
          <Compliance
            pii={workspace.compliance.pii_data}
            pci={workspace.compliance.pci_data}
            phi={workspace.compliance.phi_data}
          />
          <Provisioning provisioning={provisioning} />
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
              showModal={showModal}
              removeMember={removeMember}
              selectedAllocation={selectedAllocation}
              onChangeAllocation={updateSelectedAllocation}
              onChangeMemberRole={changeMemberRoleRequest}
              requestRefreshHiveTables={requestRefreshHiveTables}
              memberLoading={memberLoading}
            />
          </Tabs.TabPane>
          {hasApplicationFlag && (
            <Tabs.TabPane tab="APPLICATIONS" key="applications">
              <ApplicationsTab
                workspace={workspace}
                yarn={cluster.services && cluster.services.yarn}
                pools={pools}
                selectedApplication={activeApplication}
                showModal={showModal}
                onRefreshPools={requestRefreshYarnApps}
                onSelectApplication={selectApplication}
              />
            </Tabs.TabPane>
          )}
          {hasMessagingFlag && (
            <Tabs.TabPane tab="MESSAGING" key="messaging">
              <MessagingTab
                workspace={workspace}
                profile={profile}
                members={members}
                showModal={showModal}
                onChangeMemberRole={changeMemberRoleRequest}
                removeMember={removeMember}
              />
            </Tabs.TabPane>
          )}
          {profile && profile.permissions.platform_operations && profile.permissions.risk_management && (
            <Tabs.TabPane tab="MANAGE" key="manage">
              <ManageTab showModal={showModal} provisioning={provisioning} />
            </Tabs.TabPane>
          )}
        </Tabs>
        <Modal
          visible={activeModal === ModalType.SimpleMember}
          title="Add A Member"
          onCancel={clearModal}
          onOk={() => simpleMemberRequest('data')}
          confirmLoading={memberLoading}
        >
          <SimpleMemberRequest
            allocations={workspace.data}
            suggestions={userSuggestions}
            onSearch={this.handleMemberSearch}
          />
        </Modal>
        <Modal
          visible={activeModal === ModalType.SimpleTopicMember}
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
          visible={activeModal === ModalType.Application}
          title="New Application"
          onCancel={clearModal}
          onOk={requestApplication}
        >
          <ApplicationRequest />
        </Modal>
        <Modal
          visible={activeModal === ModalType.DeleteWorkspace}
          title="Delete Workspace"
          onCancel={clearModal}
          onOk={deleteWorkspace}
          okText="Delete"
          okButtonProps={{ type: 'danger' }}
          confirmLoading={manageLoading.delete}
        >
          <WarningText message="Deleting a workspace cannot be undone. Are you sure you want to delete this workspace?" />
        </Modal>
        <Modal
          visible={activeModal === ModalType.DeprovisionWorkspace}
          title="Deprovision Workspace"
          onCancel={clearModal}
          onOk={deprovisionWorkspace}
          okText="Deprovision"
          okButtonProps={{ type: 'danger' }}
          confirmLoading={manageLoading.deprovision}
        >
          <WarningText
            message={
              'Deprovisioning a workspace cannot be undone. Are you sure you want to deprovision this workspace? Deprovisioning can take up to 15 minutes. It is not necessary to keep this page open.'
            }
          />
        </Modal>
        <Modal
          visible={activeModal === ModalType.ProvisionWorkspace}
          title="Provision Workspace"
          onCancel={clearModal}
          onOk={provisionWorkspace}
          okText="Yes"
          okButtonProps={{ type: 'danger' }}
          confirmLoading={manageLoading.provision}
        >
          <WarningText message="Are you sure you want to reprovision this workspace? Provisioning can take up to 15 minutes. It is not necessary to keep this page open." />
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
    activeTopic: selectors.getActiveTopic(),
    activeApplication: selectors.getActiveApplication(),
    activeModal: selectors.getActiveModal(),
    userSuggestions: selectors.getUserSuggestions(),
    liasion: selectors.getLiaison(),
    members: selectors.getMembers(),
    notificationInfo: selectors.getNotification(),
    memberLoading: selectors.isMemberLoading(),
    provisioning: selectors.getProvisioning(),
    manageLoading: selectors.getManageLoading(),
    featureFlags: getFeatureFlags(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  clearDetails: () => dispatch(actions.clearDetails()),
  getWorkspaceDetails: (id: number) => dispatch(actions.getWorkspace(id)),

  updateSelectedAllocation: (allocation: HiveAllocation) => dispatch(actions.updateSelectedAllocation(allocation)),

  selectTopic: (topic: KafkaTopic) => dispatch(actions.setActiveTopic(topic)),
  selectApplication: (application: Application) => dispatch(actions.setActiveApplication(application)),

  showModal: (e: React.MouseEvent, type: ModalType) => {
    e.preventDefault();
    return dispatch(actions.setActiveModal(type));
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
  deleteWorkspace: () => dispatch(actions.requestDeleteWorkspace()),
  deprovisionWorkspace: () => dispatch(actions.requestDeprovisionWorkspace()),
  provisionWorkspace: () => dispatch(actions.requestProvisionWorkspace()),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(WorkspaceDetails));
