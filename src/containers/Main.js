import React from "react";
import Home from "./Home";
import RequestProject from "./RequestProject";
import {ConnectedRouter} from "react-router-redux";
import Navigation from "../components/Navigation";
import {Route} from 'react-router';
import createHistory from "history/createBrowserHistory";

const history = createHistory();

const Main = () => {
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

export default Main;