import React from "react";
import {Link} from "react-router-dom";
import "./WorkspaceListNav.css";

const WorkspaceListNav = () => {
    return (
        <div className="WorkspaceListNav">
            <Link to="/workspace-request" className="WorkspaceListNav-link">
                <i className="fas fa-plus"></i> Request a new project
            </Link>
        </div>
    );
};

export default WorkspaceListNav;