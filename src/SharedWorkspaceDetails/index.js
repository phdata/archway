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
        const {name, purpose, created_by, created} = workspace;
        return (
            <div className="SharedWorkspaceDetails">
                <h1>{name} <sub>{purpose}</sub></h1>
                <h5>Created by {created_by} <TimeAgo datetime={created}/></h5>
                <div className="">
                    <Compliance workspace={workspace}/>
                    <DatabaseDisplay database={workspace.database}
                                     cluster={cluster}/>
                    <WorkspaceMemberList members={members} />
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(SharedWorkspaceDetails);