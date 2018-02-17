import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./SharedWorkspaceDetails.css"
import Overview from "./Overview";
import Compliance from "./Compliance";
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
                <h1>{name} <sub>{purpose}</sub></h1>
                <div className="SharedWorkspaceDetails-details">
                    <div className="SharedWorkspaceDetails-details-row">
                        <Overview workspace={workspaceDetails} />
                        <Compliance workspace={workspaceDetails} />
                    </div>
                    <div className="SharedWorkspaceDetails-details-row">
                        <HDFS workspace={workspaceDetails} />
                        <YARN workspace={workspaceDetails} />
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