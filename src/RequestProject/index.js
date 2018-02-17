import React from "react";
import {connect} from "react-redux";
import RequestForm from "./RequestForm";
import {requestSharedWorkspace} from "./actions";
import "./RequestProject.css";

const RequestProject = ({requestSharedWorkspace}) => {
    return (
        <div className="RequestProject">
            <RequestForm className="RequestProject-form" onSubmit={requestSharedWorkspace} />
        </div>
    )
};

export default connect(
    state => state,
    {requestSharedWorkspace}
)(RequestProject);