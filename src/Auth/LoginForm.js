import React from "react";
import {Field, reduxForm} from 'redux-form';
import Button from "../Common/Button";
import "../Common/Forms.css";
import "./LoginForm.css";

const LoginForm = ({handleSubmit, pristine, submitting}) => (
    <form onSubmit={handleSubmit} className="LoginForm">
        <label>Username</label>
        <Field name="username" component="input" type="text"/>
        <label>Password</label>
        <Field name="password" component="input" type="password"/>
        <Button type="submit" disabled={pristine || submitting}>Log In</Button>
    </form>
);

export default reduxForm({
    form: 'login',
})(LoginForm);