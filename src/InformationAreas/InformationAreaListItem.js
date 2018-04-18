import React from "react";
import TimeAgo from 'timeago-react';
import "./InformationAreaListItem.css";
import Panel from "../Common/Panel";

const InformationAreaListItem = ({push, area: {id, name, created, created_by}}) => (
    <Panel onClick={() => push("/area/" + id + "/overview")} className="InformationAreaListItem">
        <div className="InformationAreaListItem-details">
            <h3 className="InformationAreaItem-title">{name}</h3>
            <div className="InformationAreaItem-created">
                Created <TimeAgo datetime={created}/><br/>by {created_by}
            </div>
        </div>
        <div className="InformationAreaListItem-action">
            <i className="fa fa-arrow-circle-right"></i>
        </div>
    </Panel>
);

export default InformationAreaListItem;