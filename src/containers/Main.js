import React from "react";
import RequestProject from "./RequestProject";
import {ConnectedRouter} from "react-router-redux";
import Navigation from "../components/Navigation";
import {Route} from 'react-router';
import createHistory from "history/createBrowserHistory";
import UserWorkspace from "./UserWorkspace";

const history = createHistory();

const Main = () => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{marginTop: 10, display: "flex", flex: 1}}>
                    <Route exact path="/" component={UserWorkspace}/>
                    <Route path="/shared-request" component={RequestProject}/>
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;