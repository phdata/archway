import React from "react";
import RequestSharedWorkspace from "./RequestSharedWorkspace"
import WorkspaceHeader from "../Common/WorkspaceHeader";
import WorkspaceListItem from "./WorkspaceListItem";
import {push} from "react-router-redux";
import {connect} from "react-redux";
import "./SharedWorkspaces.css";

const SharedWorkspaces = ({items, push}) => {
    let workspaces;
    if (items) {
        workspaces = items.map(item => (
            <WorkspaceListItem key={item.id}
                               workspace={item}
                               push={push}/>
        ));
    }
    return (
        <div className="SharedWorkspaces">
            <WorkspaceHeader icon="users" title="Shared Workspaces" subtitle="a place to collaborate and learn together"/>
            <div className="SharedWorkspaces-list">
                <RequestSharedWorkspace requestSharedWorkspace={() => push("/shared-request")}/>
                {workspaces}
            </div>
        </div>
    )
};

export default connect(
    state => state.sharedWorkspaces,
    {push}
)(SharedWorkspaces);
