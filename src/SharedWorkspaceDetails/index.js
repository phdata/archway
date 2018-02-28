import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./SharedWorkspaceDetails.css"
import Compliance from "./Compliance";
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
                <h5>Created by {created_by} <TimeAgo datetime={created}/></h5>
                <div className="">
                    <Compliance workspace={workspaceDetails}/>
                    <DatabaseDisplay database={workspaceDetails.database}
                                     cluster={cluster}/>
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(SharedWorkspaceDetails);