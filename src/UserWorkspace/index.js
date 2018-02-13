import React from 'react';
import './UserWorkspace.css';
import {connect} from "react-redux";
import UserWorkspaceDisplay from './UserWorkspaceDisplay';
import UserWorkspaceAbsent from "./UserWorkspaceAbsent";
import {requestWorkspace} from './actions';
import Spinner from "../Common/Spinner";

const UserWorkspace = ({userWorkspace: {requesting, workspace}, cluster, requestWorkspace}) => {
    let workspaceContent = <div/>;
    if (workspace)
        workspaceContent = <UserWorkspaceDisplay workspace={workspace} cluster={cluster}/>;
    else if (requesting)
        workspaceContent = (
            <Spinner>
                Setting up your workspace...
            </Spinner>
        );
    else
        workspaceContent = <UserWorkspaceAbsent requestWorkspace={requestWorkspace}/>;
    return (
        <div className="UserWorkspace">
            {workspaceContent}
        </div>
    );
};

export default connect(
    state => state,
    {requestWorkspace}
)(UserWorkspace);