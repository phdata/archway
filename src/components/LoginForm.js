import React from "react";
import {Field, reduxForm} from 'redux-form';

const LoginForm = ({handleSubmit, pristine, submitting}) => {
    return (
        <form onSubmit={handleSubmit}>
            <label>Username</label>
            <Field name="username" component="input" type="text"/>
            <label>Password</label>
            <Field name="password" component="input" type="password"/>
            <button type="submit" disabled={pristine || submitting}>Log In</button>
        </form>
    )
};

export default reduxForm({
    form: 'login',
})(LoginForm);