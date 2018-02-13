import React from 'react';
import "./UserWorkspaceAbsent.css";

const UserWorkspaceAbsent = ({requestWorkspace}) => {
    return (
        <div className="UserWorkspaceAbsent" onClick={requestWorkspace}>
            <div className="UserWorkspaceAbsent-notice">
                <i className="far fa-frown"></i>
                <h3>You don't have a workspace yet!</h3>
                <h4>Let's set one up...</h4>
            </div>
            <i className="fas fa-plus"></i>
        </div>
    );
};

export default UserWorkspaceAbsent;