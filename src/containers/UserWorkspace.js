import React from 'react';
import './UserWorkspace.css';
import {connect} from "react-redux";
import UserWorkspaceDisplay from '../components/UserWorkspaceDisplay';
import UserWorkspaceAbsent from "../components/UserWorkspaceAbsent";
import {requestWorkspace} from '../actions';
import Spinner from "../components/Spinner";

const UserWorkspace = ({loading, workspace, requestWorkspace}) => {
    let workspaceContent = <div/>;
    if (workspace)
        workspaceContent = <UserWorkspaceDisplay workspace/>;
    else if (loading)
        workspaceContent = <Spinner />;
    else
        workspaceContent = <UserWorkspaceAbsent requestWorkspace={requestWorkspace}/>;
    return (
        <div className="UserWorkspace">
            {workspaceContent}
        </div>
    );
};

export default connect(
    state => state.account,
    {requestWorkspace}
)(UserWorkspace);