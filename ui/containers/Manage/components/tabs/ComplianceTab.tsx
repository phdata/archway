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
  requester: string;
  selectedCompliance: ComplianceContent;

  fetchCompliances: () => void;
  clearSelectedCompliance: () => void;
  setRequest: (compliance: ComplianceContent) => void;
  setQuestion: (index: number, question: Question) => void;
  addNewQuestion: (question: Question) => void;
  removeQuestion: (id: number) => void;
  addCompliance: () => void;
  deleteCompliance: () => void;
  updateCompliance: () => void;
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
      addCompliance,
      deleteCompliance,
      updateCompliance,
      requester,
    } = this.props;
    const { url } = this.props.match;
    const complianceDetailsProps = {
      setRequest,
      selectedCompliance,
      compliances,
      setQuestion,
      addNewQuestion,
      removeQuestion,
      addCompliance,
      deleteCompliance,
      updateCompliance,
      requester,
    };

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
                <ComplianceDetails openFor={OpenType.Update} {...props} {...complianceDetailsProps} />
              )
            }
          />
          <Route
            exact
            path={`${url}/addcompliance`}
            render={props =>
              loading ? <Spin /> : <ComplianceDetails openFor={OpenType.Add} {...props} {...complianceDetailsProps} />
            }
          />
          <Redirect to={`${url}`} />
        </Switch>
      </div>
    );
  }
}

export default withRouter(CompliancesTab);
