import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./SharedWorkspaceDetails.css"
import Overview from "./Overview";
import HDFS from "./HDFS";
import YARN from "./YARN";

const SharedWorkspaceDetails = ({workspaceDetails}) => {
    if (!workspaceDetails || !workspaceDetails.hdfs)
        return <Spinner>Loading Details...</Spinner>;
    else {
        console.log(workspaceDetails);
        const {id, name, purpose} = workspaceDetails;
        return (
            <div className="SharedWorkspaceDetails">
                <h1>{name}</h1>
                <h2>{purpose}</h2>
                <div className="SharedWorkspaceDetails-details">
                    <Overview workspace={workspaceDetails} />
                    <HDFS workspace={workspaceDetails} />
                    <YARN workspace={workspaceDetails} />
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(SharedWorkspaceDetails);