import React from "react";
import "./Spinner.css";

const Spinner = ({children = "Loading..."}) => {
    return (
        <div className="lds-css">
            <div className="lds-disk">
                <div>
                    <div></div>
                    <div></div>
                </div>
            </div>
            {children}
        </div>
    );
};

export default Spinner;