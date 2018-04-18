import React from 'react';
import {push} from "react-router-redux";
import {connect} from "react-redux";
import Panel from "../Common/Panel";
import "./RequestInformationArea.css";

const RequestInformationArea = ({push}) => (
    <Panel className="RequestInformationArea" onClick={() => push("/information-request")}>
        <h3>Request new...</h3>
        <i className="fa fa-plus"/>
    </Panel>
);


export default connect(
    state => state,
    {push}
)(RequestInformationArea);