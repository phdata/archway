import React from "react";
import "./Spinner.css";

const Spinner = () => {
    return (
        <div className="lds-css">
            <div className="lds-disk">
                <div>
                    <div></div>
                    <div></div>
                </div>
            </div>
            Loading...
        </div>
    );
};

export default Spinner;