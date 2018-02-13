import React from "react";
import {connect} from "react-redux";
import Login from "./Auth/Login";
import Main from "./Main";
import Spinner from "./Common/Spinner";

const AppContainer = ({loading, token, history}) => {
    if (loading) {
        return <Spinner />;
    } else if (token) {
        return <Main history={history}/>;
    } else {
        return <Login/>;
    }
};

export default connect(
    state => state.auth,
    {}
)(AppContainer);