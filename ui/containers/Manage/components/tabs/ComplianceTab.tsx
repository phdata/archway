import * as React from 'react';
import { Spin } from 'antd';
import { Route, Switch, RouteComponentProps, withRouter, Redirect } from 'react-router-dom';

import { ComplianceList } from '../';
import { ComplianceContent, Question } from '../../../../models/Manage';
import ComplianceDetails from '../ComplianceDetails';
import { OpenType } from '../../constants';

interface Props extends RouteComponentProps<any> {
  compliances: ComplianceContent[];
  loading: boolean;
  match: any;
  selectedCompliance: ComplianceContent;

  fetchCompliances: () => void;
  clearSelectedCompliance: () => void;
  setRequest: (compliance: ComplianceContent) => void;
  setQuestion: (id: number, date: Date, question: string, requester: string) => void;
  addNewQuestion: (question: Question) => void;
  removeQuestion: (id: number) => void;
}

class CompliancesTab extends React.Component<Props> {
  public componentDidMount() {
    this.props.fetchCompliances();
  }

  public render() {
    const {
      compliances,
      loading,
      clearSelectedCompliance,
      selectedCompliance,
      setRequest,
      setQuestion,
      addNewQuestion,
      removeQuestion,
    } = this.props;
    const { url } = this.props.match;
    return (
      <div style={{ padding: 16 }}>
        <Switch>
          <Route
            exact
            path={`${url}`}
            render={props => (
              <ComplianceList
                loading={loading}
                compliances={compliances}
                clearSelectedCompliance={clearSelectedCompliance}
                {...props}
              />
            )}
          />
          <Route
            exact
            path={`${url}/compliance/:id`}
            render={props =>
              loading ? (
                <Spin />
              ) : (
                <ComplianceDetails
                  openFor={OpenType.Update}
                  setRequest={setRequest}
                  selectedCompliance={selectedCompliance}
                  compliances={compliances}
                  setQuestion={setQuestion}
                  addNewQuestion={addNewQuestion}
                  removeQuestion={removeQuestion}
                  {...props}
                />
              )
            }
          />
          <Route
            exact
            path={`${url}/addcompliance`}
            render={props =>
              loading ? (
                <Spin />
              ) : (
                <ComplianceDetails
                  openFor={OpenType.Add}
                  setRequest={setRequest}
                  selectedCompliance={selectedCompliance}
                  compliances={compliances}
                  setQuestion={setQuestion}
                  addNewQuestion={addNewQuestion}
                  removeQuestion={removeQuestion}
                  {...props}
                />
              )
            }
          />
          <Redirect to={`${url}`} />
        </Switch>
      </div>
    );
  }
}

export default withRouter(CompliancesTab);
