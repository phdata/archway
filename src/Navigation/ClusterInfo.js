import React from "react";
import {connect} from "react-redux";
import "./ClusterInfo.css";

const ClusterInfo = ({name, status = "unknown"}) => {
    return (
        <div className="ClusterInfo">
            <div>
                {name}
            </div>
            <div className={"ClusterInfo-status ClusterInfo-status-" + status} />
        </div>
    );
};

export default connect(
    state => state.cluster,
    {}
)(ClusterInfo);