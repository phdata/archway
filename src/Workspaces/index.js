import React from "react";
import UserWorkspace from "../UserWorkspace";
import SharedWorkspaces from "../SharedWorkspaces";
import "./Workspaces.css"

const Workspaces = () => (
    <div className="Workspaces">
        <UserWorkspace/>
        <SharedWorkspaces/>
    </div>
);

export default Workspaces;