import React from 'react';
import './UserWorkspace.css';
import {connect} from "react-redux";
import UserWorkspaceDisplay from './UserWorkspaceDisplay';
import UserWorkspaceAbsent from "./UserWorkspaceAbsent";
import {requestWorkspace} from './actions';
import Spinner from "../Common/Spinner";
import WorkspaceHeader from "../Common/WorkspaceHeader";

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
            <WorkspaceHeader icon="user" title="Personal" subtitle="a private workspace"/>
            {workspaceContent}
        </div>
    );
};


export default connect(
    state => state,
    {requestWorkspace}
)(UserWorkspace);