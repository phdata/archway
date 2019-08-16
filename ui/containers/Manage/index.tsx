import * as React from 'react';
import { connect } from 'react-redux';
import { createStructuredSelector } from 'reselect';
import { Tabs } from 'antd';

import * as actions from './actions';
import * as selectors from './selectors';
import { ComplianceContent, Question } from '../../models/Manage';
import { ComplianceTab } from './components/tabs';
import { ManagePage } from './constants';
import { Feature } from '../../components';
import { FeatureFlagType } from '../../constants';

const { TabPane } = Tabs;

interface Props {
  compliances: ComplianceContent[];
  selectedCompliance: ComplianceContent;
  loading: boolean;

  removeQuestion: (id: number) => void;
  clearCompliances: () => void;
  clearSelectedCompliance: () => void;
  fetchCompliances: () => void;
  requestNewCompliance: () => void;
  setSelectedCompliance: (compliance: ComplianceContent) => void;
  setQuestion: (id: number, date: Date, question: string, requester: string) => void;
  addNewQuestion: (question: Question) => void;
}

class Manage extends React.Component<Props> {
  public componentDidMount() {
    this.props.clearCompliances();
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
    } = this.props;

    return (
      <div style={{ textAlign: 'center', color: 'black' }}>
        <h1 style={{ padding: 24, margin: 0 }}>MANAGE</h1>
        <Feature flag={FeatureFlagType.ManageTab}>
          <Tabs tabBarStyle={{ textAlign: 'center' }} defaultActiveKey={ManagePage.ComplianceTab}>
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
              />
            </TabPane>
          </Tabs>
        </Feature>
      </div>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    compliances: selectors.getCompliances(),
    selectedCompliance: selectors.getSelectedCompliance(),
    loading: selectors.isLoading(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  fetchCompliances: () => dispatch(actions.getCompliances()),
  requestNewCompliance: () => dispatch(actions.requestNewCompliance()),
  clearCompliances: () => dispatch(actions.clearCompliances()),
  clearSelectedCompliance: () => dispatch(actions.clearSelectedCompliance()),
  setSelectedCompliance: (compliance: ComplianceContent) => dispatch(actions.setSelectedCompliance(compliance)),
  setQuestion: (id: number, date: Date, question: string, requester: string) =>
    dispatch(actions.setQuestion(id, date, question, requester)),
  addNewQuestion: (question: Question) => dispatch(actions.addNewQuestion(question)),
  removeQuestion: (id: number) => dispatch(actions.removeQuestion(id)),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Manage);
