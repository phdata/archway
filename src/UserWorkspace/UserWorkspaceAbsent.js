import React from 'react';
import Panel from "../Common/Panel";
import "./UserWorkspaceAbsent.css";

const UserWorkspaceAbsent = ({requestWorkspace}) => {
    return (
        <div className="UserWorkspaceAbsent">
            <h3>Looks like you don't have a workspace yet!</h3>
            <Panel className="UserWorkspaceAbsent-panel" onClick={requestWorkspace}>
                <div className="UserWorkspaceAbsent-notice">
                    <h3>Let's get yours set up.</h3>
                </div>
                <i className="fa fa-plus"></i>
            </Panel>
        </div>
    );
};

export default UserWorkspaceAbsent;