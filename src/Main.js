import React from "react";
import Navigation from "./Navigation/index";
import RequestProject from "./RequestProject/index";
import {ConnectedRouter} from "react-router-redux";
import {Route} from 'react-router';
import UserWorkspace from './UserWorkspace';
import SharedWorkspaceDetails from "./SharedWorkspaceDetails";
import SharedWorkspaces from "./SharedWorkspaces";
import InformationAreas from "./InformationAreas";

const Main = ({history}) => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{display: "flex", flex: 1}}>
                    <Route path="/personal" component={UserWorkspace}/>
                    <Route exact path="/areas" component={InformationAreas}/>
                    <Route exact path="/workspaces" component={SharedWorkspaces}/>
                    <Route path="/shared-request" component={RequestProject}/>
                    <Route path="/workspace/:id" component={SharedWorkspaceDetails} />
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;