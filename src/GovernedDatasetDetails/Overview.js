import React from "react";
import TimeAgo from 'timeago-react';
import DetailPanel from "./DetailPanel";
import "./Overview.css";

const Overview = ({workspace: {created_by, created}}) => (
    <DetailPanel title="Overview">
        <div>Created by {created_by}</div>
        <div><TimeAgo datetime={created} /></div>
    </DetailPanel>
);

export default Overview;