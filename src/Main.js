import React from "react";
import Navigation from "./Navigation/index";
import RequestProject from "./RequestProject/index";
import {ConnectedRouter} from "react-router-redux";
import {Redirect, Route} from 'react-router';
import SharedWorkspaceDetails from "./SharedWorkspaceDetails";
import Workspaces from "./Workspaces";

const Main = ({history}) => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{display: "flex", flex: 1}}>
                    <Redirect from="/" to="/workspaces" />
                    <Route exact path="/workspaces" component={Workspaces}/>
                    <Route path="/shared-request" component={RequestProject}/>
                    <Route path="/workspace/:id" component={SharedWorkspaceDetails} />
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;