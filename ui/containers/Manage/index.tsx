import * as React from 'react';
import { connect } from 'react-redux';
import { RouteComponentProps, withRouter } from 'react-router-dom';
import { createStructuredSelector } from 'reselect';
import { Tabs } from 'antd';

import * as actions from './actions';
import * as selectors from './selectors';
import { ComplianceContent, Question, LinksGroup, Link } from '../../models/Manage';
import { ComplianceTab, LinksTab } from './components/tabs';
import { ManagePage } from './constants';
import { Profile } from '../../models/Profile';
import { getProfile } from '../../redux/selectors';

const { TabPane } = Tabs;

interface Props extends RouteComponentProps<any> {
  compliances: ComplianceContent[];
  selectedCompliance: ComplianceContent;
  loading: boolean;
  requester: string;

  linksGroups: LinksGroup[];
  selectedLinksGroup: LinksGroup;

  profile: Profile;

  removeQuestion: (index: number) => void;
  clearSelectedCompliance: () => void;
  fetchCompliances: () => void;
  setSelectedCompliance: (compliance: ComplianceContent) => void;
  setQuestion: (index: number, question: Question) => void;
  addNewQuestion: (question: Question) => void;
  addCompliance: () => void;
  deleteCompliance: () => void;
  updateCompliance: () => void;

  fetchLinksGroups: () => void;
  clearSelectedLinksGroup: () => void;
  setSelectedLinksGroup: (linksGroup: LinksGroup) => void;
  addLinksGroup: () => void;
  updateLinksGroup: () => void;
  deleteLinksGroup: () => void;
  setLink: (index: number, link: Link) => void;
  addNewLink: (link: Link) => void;
  removeLink: (index: number) => void;
}

class Manage extends React.Component<Props> {
  public constructor(props: Props) {
    super(props);

    this.handleTabsClick = this.handleTabsClick.bind(this);
  }

  public componentDidMount() {
    const { tab } = this.props.match.params;
    if (![ManagePage.ComplianceTab, ManagePage.LinksTab].includes(tab)) {
      this.props.history.push(`/manage/${ManagePage.ComplianceTab}`);
    }
  }

  public handleTabsClick(key: string) {
    this.props.history.push(`/manage/${key}`);
  }

  public render() {
    const {
      compliances,
      loading,
      clearSelectedCompliance,
      fetchCompliances,
      setSelectedCompliance,
      selectedCompliance,
      setQuestion,
      addNewQuestion,
      removeQuestion,
      addCompliance,
      deleteCompliance,
      updateCompliance,
      requester,
      linksGroups,
      selectedLinksGroup,
      fetchLinksGroups,
      clearSelectedLinksGroup,
      setSelectedLinksGroup,
      addLinksGroup,
      updateLinksGroup,
      deleteLinksGroup,
      setLink,
      addNewLink,
      removeLink,
      profile,
    } = this.props;
    const { tab } = this.props.match.params;

    return (
      <div style={{ textAlign: 'center', color: 'black' }}>
        <h1 style={{ padding: 24, margin: 0 }}>MANAGE</h1>
        <Tabs
          tabBarStyle={{ textAlign: 'center' }}
          defaultActiveKey={ManagePage.ComplianceTab}
          activeKey={tab}
          onChange={this.handleTabsClick}
        >
          {profile && profile.permissions.risk_management && (
            <TabPane tab="Compliances" key={ManagePage.ComplianceTab}>
              <ComplianceTab
                compliances={compliances}
                loading={loading}
                fetchCompliances={fetchCompliances}
                setRequest={setSelectedCompliance}
                selectedCompliance={selectedCompliance}
                clearSelectedCompliance={clearSelectedCompliance}
                setQuestion={setQuestion}
                addNewQuestion={addNewQuestion}
                removeQuestion={removeQuestion}
                addCompliance={addCompliance}
                deleteCompliance={deleteCompliance}
                updateCompliance={updateCompliance}
                requester={requester}
              />
            </TabPane>
          )}
          {profile && profile.permissions.platform_operations && (
            <TabPane tab="Links" key={ManagePage.LinksTab}>
              <LinksTab
                loading={loading}
                linksGroups={linksGroups}
                selectedLinksGroup={selectedLinksGroup}
                fetchLinksGroups={fetchLinksGroups}
                clearSelectedLinksGroup={clearSelectedLinksGroup}
                setSelectedLinksGroup={setSelectedLinksGroup}
                addLinksGroup={addLinksGroup}
                updateLinksGroup={updateLinksGroup}
                deleteLinksGroup={deleteLinksGroup}
                setLink={setLink}
                addNewLink={addNewLink}
                removeLink={removeLink}
              />
            </TabPane>
          )}
        </Tabs>
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    compliances: selectors.getCompliances(),
    selectedCompliance: selectors.getSelectedCompliance(),
    loading: selectors.isLoading(),
    requester: selectors.getRequester(),

    linksGroups: selectors.getLinksGroups(),
    selectedLinksGroup: selectors.getSelectedLinksGroup(),

    profile: getProfile(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  fetchCompliances: () => dispatch(actions.getCompliances()),
  clearSelectedCompliance: () => dispatch(actions.clearSelectedCompliance()),
  setSelectedCompliance: (compliance: ComplianceContent) => dispatch(actions.setSelectedCompliance(compliance)),
  setQuestion: (index: number, question: Question) => dispatch(actions.setQuestion(index, question)),
  addNewQuestion: (question: Question) => dispatch(actions.addNewQuestion(question)),
  removeQuestion: (index: number) => dispatch(actions.removeQuestion(index)),
  addCompliance: () => dispatch(actions.requestNewCompliance()),
  updateCompliance: () => dispatch(actions.requestUpdateCompliance()),
  deleteCompliance: () => dispatch(actions.requestDeleteCompliance()),

  fetchLinksGroups: () => dispatch(actions.getLinksGroups()),
  clearSelectedLinksGroup: () => dispatch(actions.clearSelectedLinksGroup()),
  setSelectedLinksGroup: (linksGroup: LinksGroup) => dispatch(actions.setSelectedLinksGroup(linksGroup)),
  setLink: (index: number, link: Link) => dispatch(actions.setLink(index, link)),
  addNewLink: (link: Link) => dispatch(actions.addNewLink(link)),
  removeLink: (index: number) => dispatch(actions.removeLink(index)),
  addLinksGroup: () => dispatch(actions.requestNewLinksGroup()),
  updateLinksGroup: () => dispatch(actions.requestUpdateLinksGroup()),
  deleteLinksGroup: () => dispatch(actions.requestDeleteLinksGroup()),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(Manage));
