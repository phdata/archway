import React from "react";
import {Field, reduxForm} from 'redux-form';
import "./LoginForm.css";
import Button from "./Button";

const LoginForm = (values) => {
    console.log(values);
    const {handleSubmit, pristine, submitting} = values;
    return (
        <form onSubmit={handleSubmit} className="LoginForm">
            <label>Username</label>
            <Field name="username" component="input" type="text"/>
            <label>Password</label>
            <Field name="password" component="input" type="password"/>
            <Button type="submit" disabled={pristine || submitting}>Log In</Button>
        </form>
    )
};

export default reduxForm({
    form: 'login',
})(LoginForm);