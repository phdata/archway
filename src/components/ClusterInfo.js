import React from "react";
import {connect} from "react-redux";
import "./ClusterInfo.css";

const ClusterInfo = ({name, status = "unknown"}) => {
    return (
        <div className="ClusterInfo">
            <div className={"ClusterInfo-status ClusterInfo-status-" + status} />
            <div>
                {name}
            </div>
        </div>
    );
};

export default connect(
    state => state.cluster,
    {}
)(ClusterInfo);