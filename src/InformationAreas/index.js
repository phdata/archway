import React from "react";
import RequestInformationAreas from "./RequestInformationArea"
import {push} from "react-router-redux";
import {connect} from "react-redux";
import "./InformationAreas.css";
import WorkspaceHeader from "../Common/WorkspaceHeader";
import InformationAreaListItem from "./InformationAreaListItem";

const InformationAreas = ({items, push}) => {
    let areas;
    if (items) {
        areas = items.map(item => (
            <InformationAreaListItem key={item.id}
                               area={item}
                               push={push}/>
        ));
    }
    return (
        <div className="InformationAreas">
            <WorkspaceHeader icon="lightbulb-o" title="Governed Datasets" subtitle="official spaces for real data"/>
            <div className="InformationAreas-list">
                <RequestInformationAreas/>
                {areas}
            </div>
        </div>
    )
};

export default connect(
    state => state.informationAreas,
    {push}
)(InformationAreas);