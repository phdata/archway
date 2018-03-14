import React from "react";
import "./WorkspaceHeader.css";

const WorkspaceHeader = ({icon, title, subtitle}) => (
    <div className="WorkspaceHeader-header">
        <i className={`fa fa-${icon}`}/>
        <h2>{title}</h2>
        <h5>{subtitle}</h5>
    </div>
);

export default WorkspaceHeader;