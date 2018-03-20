import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./SharedWorkspaceDetails.css"
import Compliance from "./Compliance";
import TimeAgo from "timeago-react";
import DatabaseDisplay from "../Common/DatabaseDisplay";
import WorkspaceMemberList from "./WorkspaceMemberList";

const SharedWorkspaceDetails = ({workspaceDetails: {workspace, members}, cluster}) => {
    if (!workspace || !workspace.hdfs)
        return <Spinner>Loading Details...</Spinner>;
    else {
        const {name, purpose, created_by, created, compliance} = workspace;
        return (
            <div className="SharedWorkspaceDetails">
                <div className="SharedWorkspaceDetails-header">
                <h1>{name}</h1>
                <Compliance workspace={workspace}/>
                </div>
                <h5>{purpose}</h5>
                <div className="SharedWorkspaceDetails-details">
                    <div className="SharedWorkspaceDetails-details-left">
                    <DatabaseDisplay database={workspace.database}
                                     cluster={cluster}/>
                    </div>
                    <div className="SharedWorkspaceDetails-details-right">
                    <WorkspaceMemberList members={members} />
                    </div>
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(SharedWorkspaceDetails);