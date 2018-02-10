import React from "react";
import "./UserWorkspaceDisplay.css";

const UserWorkspaceDisplay = ({workspace, cluster}) => {
    return (
        <div className="UserWorkspaceDisplay">
            <h2 className="UserWorkspaceDisplay-notice">
                Your workspace is set up!
            </h2>
            <h3 className="UserWorkspaceDisplay-sub">
                here's the info for {cluster.name}
            </h3>
            <dl className="UserWorkspaceDisplay-info">
                <dt>Database</dt>
                <dd>{workspace.database}</dd>
                <dt>Data Directory</dt>
                <dd>{workspace.data_directory}</dd>
            </dl>
        </div>
    );
};

export default UserWorkspaceDisplay;