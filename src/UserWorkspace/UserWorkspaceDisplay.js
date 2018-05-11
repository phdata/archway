import React from "react";
import "./UserWorkspaceDisplay.css";
import DatabaseDisplay from "../Common/DatabaseDisplay";
import ProcessingDisplay from "../Common/ProcessingDisplay";

const UserWorkspaceDisplay = ({workspace, cluster}) => {
    return (
        <div className="UserWorkspaceDisplay">
            <h2 className="UserWorkspaceDisplay-notice">
                Your workspace is set up<br />on "{cluster.name}"!
            </h2>
            <DatabaseDisplay database={workspace.database} cluster={cluster} />
            <ProcessingDisplay processing={workspace.processing} cluster={cluster} />
        </div>
    );
};

export default UserWorkspaceDisplay;
