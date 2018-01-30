import React from "react";
import LoginForm from "../components/LoginForm";
import logo from "./logo_black.png";
import {connect} from "react-redux";
import {login} from "../actions/index";
import "./Login.css";

const Login = ({login, error}) => {
    let errorBlock = <br />;
    if (error)
        errorBlock = <div className="Login-error">{error.message}</div>;
    return (
        <div className="Login">
            <img src={logo} width={200} alt="logo" className="Login-logo"/>
            <div className="Login-panel">
                <h1 className="Login-title">Please log in</h1>
                <LoginForm onSubmit={login}/>
            </div>
            {errorBlock}
        </div>
    );
};

export default connect(
    state => state.rootReducer.account,
    {login}
)(Login);