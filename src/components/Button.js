import React from 'react';
import "./Button.css";

const Button = ({children, disabled = false, handleClick = () => {}, type = "button"}) => {
    return (
        <button type={type} disabled={disabled} onClick={handleClick} className="Button">
            {children}
        </button>
    )
};

export default Button;
