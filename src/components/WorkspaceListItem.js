import React from "react";
import TimeAgo from 'timeago-react';
import "./WorkspaceListItem.css";

const WorkspaceListItem = ({workspace: {id, name, created_by, created}}) => {
    return (
        <div key={id} className="WorkspaceListItem">
            <i className="fas fa-arrow-circle-right"></i>
            <h1 className="WorkspaceListItem-title">{name}</h1>
            <div className="WorkspaceListItem-created">
                Created <TimeAgo datetime={created} /><br />by {created_by}
            </div>
        </div>
    );
};

export default WorkspaceListItem;