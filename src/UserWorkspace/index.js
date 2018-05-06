import React from 'react';
import './UserWorkspace.css';
import {connect} from "react-redux";
import UserWorkspaceDisplay from './UserWorkspaceDisplay';
import UserWorkspaceAbsent from "./UserWorkspaceAbsent";
import {requestWorkspace} from './actions';
import Spinner from "../Common/Spinner";
import WorkspaceHeader from "../Common/WorkspaceHeader";

const UserWorkspace = ({userWorkspace: {loading, requesting, workspace}, cluster, requestWorkspace}) => {
    let workspaceContent = <div/>;
    if (loading)
        workspaceContent = <Spinner />;
    else if (workspace)
        workspaceContent = <UserWorkspaceDisplay workspace={workspace} cluster={cluster}/>;
    else if (requesting)
        workspaceContent = (
            <Spinner>
                Setting you up with a workspace...
            </Spinner>
        );
    else
        workspaceContent = <UserWorkspaceAbsent requestWorkspace={requestWorkspace}/>;
    return (
        <div className="UserWorkspace">
            <WorkspaceHeader icon="user" title="Your Personal Workspace" subtitle="a place to try things out"/>
            <div className="UserWorkspace-content">
                {workspaceContent}
            </div>
        </div>
    );
};


export default connect(
    state => state,
    {requestWorkspace}
)(UserWorkspace);