import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import Compliance from "./Compliance";
import DatabaseDisplay from "../Common/DatabaseDisplay";
import WorkspaceMemberList from "./WorkspaceMemberList";
import {requestNewMember, removeMember} from "./actions";
import "./SharedWorkspaceDetails.css"

const SharedWorkspaceDetails = ({workspaceDetails: {workspace, members}, cluster, requestNewMember, removeMember}) => {
    if (!workspace || !workspace.ldap)
        return <Spinner>Loading Details...</Spinner>;
    else {
        const {name, purpose} = workspace;
        return (
            <div className="SharedWorkspaceDetails">
                <div className="SharedWorkspaceDetails-header">
                    <h1>{name}</h1>
                    <Compliance workspace={workspace}/>
                </div>
                <h5>{purpose}</h5>
                <div className="SharedWorkspaceDetails-details">
                    <div className="SharedWorkspaceDetails-details-left">
                        <DatabaseDisplay database={workspace.data}
                                         cluster={cluster}/>
                    </div>
                    <div className="SharedWorkspaceDetails-details-right">
                        <WorkspaceMemberList members={members} onAdd={requestNewMember} onRemove={removeMember} />
                    </div>
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {requestNewMember, removeMember}
)(SharedWorkspaceDetails);