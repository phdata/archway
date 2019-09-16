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
  requester: string;

  removeQuestion: (index: number) => void;
  clearSelectedCompliance: () => void;
  fetchCompliances: () => void;
  requestNewCompliance: () => void;
  setSelectedCompliance: (compliance: ComplianceContent) => void;
  setQuestion: (index: number, question: Question) => void;
  addNewQuestion: (question: Question) => void;
  addCompliance: () => void;
  deleteCompliance: () => void;
  updateCompliance: () => void;
}

class Manage extends React.Component<Props> {
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
                addCompliance={addCompliance}
                deleteCompliance={deleteCompliance}
                updateCompliance={updateCompliance}
                requester={requester}
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
    requester: selectors.getRequester(),
  });

const mapDispatchToProps = (dispatch: any) => ({
  fetchCompliances: () => dispatch(actions.getCompliances()),
  requestNewCompliance: () => dispatch(actions.requestNewCompliance()),
  clearSelectedCompliance: () => dispatch(actions.clearSelectedCompliance()),
  setSelectedCompliance: (compliance: ComplianceContent) => dispatch(actions.setSelectedCompliance(compliance)),
  setQuestion: (index: number, question: Question) => dispatch(actions.setQuestion(index, question)),
  addNewQuestion: (question: Question) => dispatch(actions.addNewQuestion(question)),
  removeQuestion: (index: number) => dispatch(actions.removeQuestion(index)),
  addCompliance: () => dispatch(actions.requestNewCompliance()),
  updateCompliance: () => dispatch(actions.requestUpdateCompliance()),
  deleteCompliance: () => dispatch(actions.requestDeleteCompliance()),
});

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Manage);
