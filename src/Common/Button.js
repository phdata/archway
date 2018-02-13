import React from 'react';
import "./Button.css";
import Spinner from "./Spinner";

const Button = ({children, disabled = false, loading = false, handleClick = () => {}, type = "button"}) => {
    const content = loading ? <Spinner width={45}>&nbsp;</Spinner> : children;
    return (
        <button type={type} disabled={disabled} onClick={handleClick} className="Button">
            {content}
        </button>
    )
};

export default Button;
