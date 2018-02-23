import React from "react";
import WorkspaceListNav from "./WorkspaceListNav";
import WorkspaceList from "./WorkspaceList";
import FAB from "../Common/FAB";
import {connect} from "react-redux";
import {push} from "react-router-redux";
import "./SharedWorkspaces.css";

const SharedWorkspaces = ({push}) => {
    return (
        <div className="SharedWorkspaces">
            <WorkspaceList />
            <FAB onClick={() => push("/shared-request")} />
        </div>
    )
};

export default connect(
    state => state,
    {push}
)(SharedWorkspaces);