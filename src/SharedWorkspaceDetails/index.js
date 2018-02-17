import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./SharedWorkspaceDetails.css"
import Compliance from "./Compliance";
import HDFS from "./HDFS";
import YARN from "./YARN";
import TimeAgo from "timeago-react";
import DatabaseDisplay from "../Common/DatabaseDisplay";

const SharedWorkspaceDetails = ({workspaceDetails, cluster}) => {
    if (!workspaceDetails || !workspaceDetails.hdfs)
        return <Spinner>Loading Details...</Spinner>;
    else {
        console.log(workspaceDetails);
        const {name, purpose, created_by, created} = workspaceDetails;
        return (
            <div className="SharedWorkspaceDetails">
                <h1>{name} <sub>{purpose}</sub></h1>
                <h5>Created by {created_by} <TimeAgo datetime={created} /></h5>
                <Compliance workspace={workspaceDetails}/>
                <DatabaseDisplay database={{name: "testing", location: "/data/shared_workspace/testing/db"}} cluster={cluster}/>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(SharedWorkspaceDetails);