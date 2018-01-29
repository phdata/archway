import React from 'react';
import {render} from 'react-dom';
import {connect, Provider} from "react-redux";
import createSagaMiddleware from 'redux-saga';

import {applyMiddleware, combineReducers, createStore} from 'redux'

import rootReducer from './reducers'
import {ConnectedRouter} from "react-router-redux";
import Home from './containers/Home';
import RequestProject from "./containers/RequestProject";
import createHistory from 'history/createBrowserHistory';
import {Route} from 'react-router';
import Navigation from "./components/Navigation";
import { reducer as reduxFormReducer } from 'redux-form';
import Login from "./containers/Login";

const sagaMiddleware = createSagaMiddleware();

const store = createStore(
    combineReducers({
        form: reduxFormReducer,
        rootReducer
    }),
    applyMiddleware(sagaMiddleware)
);

const history = createHistory();

let App = () => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", minHeight: "100%"}}>
                <Navigation/>
                <div style={{marginLeft: 10}}>
                    <Route exact path="/" component={Home}/>
                    <Route path="/shared-request" component={RequestProject}/>
                </div>
            </div>
        </ConnectedRouter>
    );
};

App = connect(
    state => ({ token: startup() }),
    {}
)(App);

render(
    <Provider store={store}>
        <App />
    </Provider>,
    document.getElementById('root')
);