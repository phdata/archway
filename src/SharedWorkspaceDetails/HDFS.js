import React from "react";
import DetailPanel from "./DetailPanel";
import "./DetailPanel.css";
import Spinner from "../Common/Spinner";

const HDFS = ({workspace: {hdfs: {location}}}) => {
    let content;
    if(location)
        content = <div>{location}</div>;
    else
        content = <Spinner width={50}>Provisioning...</Spinner>;

    return (
        <DetailPanel title="HDFS">
            {content}
        </DetailPanel>
    );
}

export default HDFS;