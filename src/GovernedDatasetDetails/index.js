import React from "react";
import {connect} from "react-redux";
import Spinner from "../Common/Spinner";
import "./GovernedDatasetDetails.css"
import Compliance from "./Compliance";
import DatabaseDisplay from "../Common/DatabaseDisplay";

const GovernedDatasetDetails = ({datasetDetails: {dataset}, cluster}) => {
    if (!dataset || !dataset.raw)
        return <Spinner>Loading Details...</Spinner>;
    else {
        const {name, purpose} = dataset;
        return (
            <div className="GovernedDatasetDetails">
                <div className="GovernedDatasetDetails-header">
                    <h1>{name}</h1>
                    <Compliance workspace={dataset}/>
                </div>
                <h5>{purpose}</h5>
                <div className="GovernedDatasetDetails-details">
                    <DatabaseDisplay database={dataset.raw.data}
                                     cluster={cluster}/>
                    <DatabaseDisplay database={dataset.staging.data}
                                     cluster={cluster}/>
                    <DatabaseDisplay database={dataset.modeled.data}
                                     cluster={cluster}/>
                </div>
            </div>
        );
    }
};

export default connect(
    state => state,
    {}
)(GovernedDatasetDetails);