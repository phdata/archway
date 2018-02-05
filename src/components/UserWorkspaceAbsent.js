import React from 'react';
import "./UserWorkspaceAbsent.css";

const UserWorkspaceAbsent = ({requestWorkspace}) => {
    return (
        <div className="UserWorkspaceAbsent" onClick={requestWorkspace}>
            <div className="UserWorkspaceAbsent-notice">
                <i className="far fa-frown"></i>
                <div>You don't have a workspace yet!</div>
            </div>
            <i className="fas fa-plus"></i>
        </div>
    );
};

export default UserWorkspaceAbsent;