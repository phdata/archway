import React from "react";
import {ConnectedRouter} from "react-router-redux";
import Navigation from "../components/Navigation";
import {Route} from 'react-router';
import UserWorkspace from "./UserWorkspace";
import RequestProject from "./RequestProject";
import SharedWorkspaces from "./SharedWorkspaces";

const Main = ({history}) => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{marginTop: 10, display: "flex", flex: 1}}>
                    <Route exact path="/" component={UserWorkspace}/>
                    <Route path="/shared-request" component={RequestProject}/>
                    <Route path="/workspaces" component={SharedWorkspaces}/>
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;