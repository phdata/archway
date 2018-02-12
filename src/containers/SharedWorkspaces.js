import React from "react";
import WorkspaceListNav from "../components/WorkspaceListNav";
import WorkspaceList from "./WorkspaceList";
import "./SharedWorkspaces.css";

const SharedWorkspaces = () => {
    return (
        <div className="SharedWorkspaces">
            <WorkspaceListNav />
            <WorkspaceList />
        </div>
    )
};

export default SharedWorkspaces;