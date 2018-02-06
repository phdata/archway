import React from "react";
import {connect} from "react-redux";
import Login from "./containers/Login";
import Main from "./containers/Main";
import Spinner from "./components/Spinner";

const AppContainer = ({loading, token}) => {
    if (loading) {
        return <Spinner />;
    } else if (token) {
        return <Main/>;
    } else {
        return <Login/>;
    }
};

export default connect(
    state => state.account,
    {}
)(AppContainer);