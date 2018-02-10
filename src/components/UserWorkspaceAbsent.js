import React from 'react';
import "./UserWorkspaceAbsent.css";

const UserWorkspaceAbsent = ({requestWorkspace}) => {
    const doit = () => {
        console.log("here again");
        requestWorkspace();
    };
    return (
        <div className="UserWorkspaceAbsent" onClick={doit}>
            <div className="UserWorkspaceAbsent-notice">
                <i className="far fa-frown"></i>
                <div>You don't have a workspace yet!</div>
                <div>Let's add one...</div>
            </div>
            <i className="fas fa-plus"></i>
        </div>
    );
};

export default UserWorkspaceAbsent;