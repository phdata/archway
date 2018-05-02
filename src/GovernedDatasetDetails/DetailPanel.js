import React from "react";
import "../Common/Panel.css";

const DetailPanel = ({title, children}) => (
    <div className="DetailPanel">
        <h3>{title}</h3>
        <div className="DetailPanel-details">
            {children}
        </div>
    </div>
);

export default DetailPanel;