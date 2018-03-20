import React from 'react';
import {push} from "react-router-redux";
import {connect} from "react-redux";
import "./RequestSharedWorkspace.css";
import Panel from "../Common/Panel";

const RequestSharedWorkspace = ({push}) => (
    <Panel className="RequestSharedWorkspace" onClick={() => push("/shared-request")}>
        <h3>Request new...</h3>
        <i className="fa fa-plus"></i>
    </Panel>
);


export default connect(
    state => state,
    {push}
)(RequestSharedWorkspace);
