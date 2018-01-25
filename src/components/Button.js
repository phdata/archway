import React, { PropTypes } from 'react';
import './Button.css';

const Button = ({busy, children, onClick}) => {

  return (
    <div
    className={"button " + (busy ? 'disabled' : '')}
    onClick={onClick}>
      <a
      href="#">
        {children}
      </a>
    </div>
  );

};

export default Button;
