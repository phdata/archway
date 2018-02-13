import React from "react";
import {connect} from "react-redux";
import Spinner from "../components/Spinner";
import WorkspaceListItem from "../components/WorkspaceListItem";
import "./WorkspaceList.css";

const WorkspaceList = ({loading, items}) => {
    let content;
    if (loading) {
        content = <Spinner>Loading workspaces...</Spinner>;
    } else if (!items || (items.length && items.length <= 0)) {
        content = <div className="WorkspaceList-nothing">No projects created yet.</div>;
    } else {
        content = (
            <div className="WorkspaceList">
                {items.map(item => {
                    return <WorkspaceListItem key={item.id} workspace={item} />;
                })}
            </div>
        );
    }
    return content;
};

export default connect(
    state => state.workspaces,
    {}
)(WorkspaceList);