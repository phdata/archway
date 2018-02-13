import React from "react";
import WorkspaceListNav from "../components/WorkspaceListNav";
import WorkspaceList from "./WorkspaceList";
import "./SharedWorkspaces.css";
import FAB from "../components/FAB";
import {connect} from "react-redux";
import {push} from "react-router-redux";

const SharedWorkspaces = ({push}) => {
    return (
        <div className="SharedWorkspaces">
            <WorkspaceListNav />
            <WorkspaceList />
            <FAB onClick={() => push("/shared-request")} />
        </div>
    )
};

export default connect(
    state => state,
    {push}
)(SharedWorkspaces);