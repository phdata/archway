import React from "react";
import TimeAgo from 'timeago-react';
import "./WorkspaceListItem.css";
import Panel from "../Common/Panel";

const WorkspaceListItem = ({push, workspace: {id, name, created, created_by}}) => (
    <Panel onClick={() => push("/workspace/" + id + "/overview")} className="WorkspaceListItem">
        <div className="WorkspaceListItem-details">
            <h3 className="WorkspaceListItem-title">{name}</h3>
            <div className="WorkspaceListItem-created">
                Created <TimeAgo datetime={created}/><br/>by {created_by}
            </div>
        </div>
        <div className="WorkspaceListItem-action">
            <i className="fa fa-arrow-circle-right"></i>
        </div>
    </Panel>
);

export default WorkspaceListItem;