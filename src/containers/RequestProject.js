import React from "react";
import {connect} from "react-redux";
import RequestForm from "../components/RequestForm";
import {requestSharedWorkspace} from "../actions";
import "./RequestProject.css";

const RequestProject = () => {
    return (
        <div className="RequestProject">
            <RequestForm className="RequestProject-form" handleSubmit={requestSharedWorkspace}/>
        </div>
    )
};

export default connect(
    state => state,
    {requestSharedWorkspace}
)(RequestProject);