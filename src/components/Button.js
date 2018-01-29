import React from 'react';
import PropTypes from 'prop-types';
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

Button.propTypes = {
  busy: PropTypes.bool,
  onClick: PropTypes.func
};

export default Button;
