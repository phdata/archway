import React from "react";
import {connect} from "react-redux";
import RequestForm from "./RequestForm";
import {requestGovernedDataset} from "./actions";
import "./RequestGovernedDataset.css";

const RequestGovernedDataset = ({requestGovernedDataset}) => {
    return (
        <div className="RequestGovernedDataset">
            <RequestForm className="RequestGovernedDataset-form" onSubmit={requestGovernedDataset}/>
        </div>
    )
};

export default connect(
    state => state,
    {requestGovernedDataset}
)(RequestGovernedDataset);