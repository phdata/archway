import React from "react";
import LoginForm from "../components/LoginForm";
import logo from "../components/logo.png";
import {connect} from "react-redux";
import { login } from "../actions/index";

const Login = ({login}) => {
    return (
        <div>
            <img src={logo} width={200} alt="logo"/>
            <h1>Please log in</h1>
            <LoginForm onSubmit={login}/>
        </div>
    );
};

export default connect(
    state => state,
    { login }
)(Login);