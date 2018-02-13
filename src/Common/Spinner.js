import React from "react";
import "./Spinner.css";

const Spinner = ({children = "Loading...", width = 100}) => {
    const half = width / 2;
    const scale = width / 200;
    const style = {
        width: width,
        height: width,
        transform: "translate(-" + half + "px, -" + half + "px) scale(" + scale + ") translate(" + half + "px, " + half + "px)"
    };
    return (
        <div className="lds-css">
            <div className="lds-disk" style={style}>
                <div>
                    <div></div>
                    <div></div>
                </div>
            </div>
            <h2>{children}</h2>
        </div>
    );
};

export default Spinner;