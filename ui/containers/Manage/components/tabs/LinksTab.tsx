import * as React from 'react';
import { withRouter, Switch, Route, RouteComponentProps } from 'react-router-dom';
import { Spin } from 'antd';

import { LinksGroupDetails, LinksGroupsList } from '../';
import { LinksGroup, Link } from '../../../../models/Manage';
import { OpenType, ManagePage } from '../../constants';

interface Props extends RouteComponentProps<any> {
  loading: boolean;
  linksGroups: LinksGroup[];
  selectedLinksGroup: LinksGroup;

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

class LinksTab extends React.Component<Props> {
  public componentDidMount() {
    this.props.fetchLinksGroups();
  }

  public render() {
    const {
      loading,
      linksGroups,
      selectedLinksGroup,
      clearSelectedLinksGroup,
      setSelectedLinksGroup,
      updateLinksGroup,
      deleteLinksGroup,
      addLinksGroup,
      addNewLink,
      removeLink,
      setLink,
    } = this.props;
    const url = `/manage/${ManagePage.LinksTab}`;
    const linksGroupDetailsProps = {
      linksGroups,
      selectedLinksGroup,
      setLink,
      addNewLink,
      removeLink,
      setSelectedLinksGroup,
      addLinksGroup,
      deleteLinksGroup,
      updateLinksGroup,
    };

    return (
      <div style={{ padding: 16 }}>
        <Switch>
          <Route
            exact
            path={`${url}`}
            render={props => (
              <LinksGroupsList
                loading={loading}
                linksGroups={linksGroups}
                clearSelectedLinksGroup={clearSelectedLinksGroup}
                {...props}
              />
            )}
          />
          <Route
            exact
            path={`${url}/add`}
            render={props =>
              loading ? <Spin /> : <LinksGroupDetails openFor={OpenType.Add} {...props} {...linksGroupDetailsProps} />
            }
          />
          <Route
            exact
            path={`${url}/:id`}
            render={props =>
              loading ? (
                <Spin />
              ) : (
                <LinksGroupDetails openFor={OpenType.Update} {...props} {...linksGroupDetailsProps} />
              )
            }
          />
        </Switch>
      </div>
    );
  }
}

export default withRouter(LinksTab);
