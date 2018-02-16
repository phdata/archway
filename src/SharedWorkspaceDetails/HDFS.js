import React from "react";
import DetailPanel from "./DetailPanel";
import "./DetailPanel.css";

const HDFS = ({workspace: {hdfs: {location}}}) => (
    <DetailPanel title="HDFS">
        <div className="DetailPanel-content">
            {location}
        </div>
    </DetailPanel>
);

export default HDFS;