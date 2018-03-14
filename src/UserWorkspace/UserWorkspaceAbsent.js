import React from 'react';
import Panel from "../Common/Panel";
import "./UserWorkspaceAbsent.css";

const UserWorkspaceAbsent = ({requestWorkspace}) => {
    return (
        <Panel className="UserWorkspaceAbsent" onClick={requestWorkspace}>
            <div className="UserWorkspaceAbsent-notice">
                <h3>Let's get yours set up!</h3>
            </div>
            <i className="fa fa-plus"></i>
        </Panel>
    );
};

export default UserWorkspaceAbsent;