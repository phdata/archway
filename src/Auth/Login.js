import React from "react";
import LoginForm from "./LoginForm";
import {login} from "./actions";
import {connect} from "react-redux";
import logo from "../Common/white_logo_transparent.png";
import "./Login.css";

const Login = ({login, error}) => {
    let errorBlock = <br />;
    if (error)
        errorBlock = <div className="Login-error">{error.message}</div>;
    return (
        <div className="Login">
            <div className="Login-brandcontainer">
                <img src={logo} alt="logo" className="Login-logo"/>
            </div>
            <div className="Login-panel">
                <h1 className="Login-title">Please log in</h1>
                <LoginForm onSubmit={login} />
            </div>
            {errorBlock}
        </div>
    );
};

export default connect(
    state => state.account,
    {login}
)(Login);