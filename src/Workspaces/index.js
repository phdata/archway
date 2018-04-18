import React from "react";
import UserWorkspace from "../UserWorkspace";
import SharedWorkspaces from "../SharedWorkspaces";
import InformationAreas from "../InformationAreas";
import "./Workspaces.css"

const Workspaces = () => (
    <div className="Workspaces">
        <UserWorkspace/>
        <SharedWorkspaces/>
        <InformationAreas/>
    </div>
);

export default Workspaces;