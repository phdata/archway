import React from 'react';
import {render} from 'react-dom';
import {connect, Provider} from "react-redux";
import Login from "./containers/Login";
import Main from "./containers/Main";
import Loading from "./containers/Loading";
import store from "./store/configureStore";

const AppContainer = ({loading, token}) => {
    if (loading) {
        return <Loading/>;
    } else if (token) {
        return <Main/>;
    } else {
        return <Login/>;
    }
};

const App = connect(
    state => state.rootReducer.account,
    {}
)(AppContainer);

render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById('root')
);