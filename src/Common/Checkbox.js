import React from "react";
import {Field} from 'redux-form';
import "./Checkbox.css";

const Checkbox = (props) => {
    const {children} = props;
    const checkbox = ({input: {name, value, onChange}}) => (
        <label className="Checkbox">
            <input type="checkbox"
                   name={name}
                   value={value}
                   onChange={onChange}
                   checked={!!value} />
            <div className="checkmark"></div>
            {children}
        </label>
    );

    return <Field {...props} type="checkbox" component={checkbox} />;
};

export default Checkbox;