import React from "react";
import "./UserWorkspaceDisplay.css";
import DatabaseDisplay from "../Common/DatabaseDisplay";

const UserWorkspaceDisplay = ({workspace, cluster}) => {
    return (
        <div className="UserWorkspaceDisplay">
            <h2 className="UserWorkspaceDisplay-notice">
                Your workspace is set up<br />on "{cluster.name}"!
            </h2>
            <DatabaseDisplay database={workspace.database} cluster={cluster} />
        </div>
    );
};

export default UserWorkspaceDisplay;