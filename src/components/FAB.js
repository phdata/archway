import React from "react";
import "./FAB.css";

const FAB = ({onClick}) => {
    return (
        <div className="FAB" onClick={onClick}>
            <i className="fas fa-plus"></i>
        </div>
    );
};

export default FAB;