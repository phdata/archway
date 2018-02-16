import React from "react";
import Navigation from "./Navigation/index";
import UserWorkspace from "./UserWorkspace/index";
import RequestProject from "./RequestProject/index";
import SharedWorkspaces from "./SharedWorkspaces/index";
import {ConnectedRouter} from "react-router-redux";
import {Route} from 'react-router';
import  SharedWorkspaceDetails from "./SharedWorkspaceDetails";

const Main = ({history}) => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{marginTop: 10, display: "flex", flex: 1}}>
                    <Route exact path="/" component={UserWorkspace}/>
                    <Route path="/shared-request" component={RequestProject}/>
                    <Route path="/workspaces" component={SharedWorkspaces}/>
                    <Route path="/workspace/:id" component={SharedWorkspaceDetails} />
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;