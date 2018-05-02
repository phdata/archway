import React from "react";
import Navigation from "./Navigation/index";
import RequestProject from "./RequestProject/index";
import {ConnectedRouter} from "react-router-redux";
import {Route} from 'react-router';
import UserWorkspace from './UserWorkspace';
import SharedWorkspaceDetails from "./SharedWorkspaceDetails";
import SharedWorkspaces from "./SharedWorkspaces";
import InformationAreas from "./InformationAreas";
import RequestGovernedDataset from "./RequestGovernedDataset";
import GovernedDatasetDetails from "./GovernedDatasetDetails";

const Main = ({history}) => {
    return (
        <ConnectedRouter history={history}>
            <div style={{display: "flex", flex: 1, flexDirection: "column"}}>
                <Navigation/>
                <div style={{display: "flex", flex: 1}}>
                    <Route path="/personal" component={UserWorkspace}/>
                    <Route path="/datasets" component={InformationAreas}/>
                    <Route path="/workspaces" component={SharedWorkspaces}/>
                    <Route path="/shared-request" component={RequestProject}/>
                    <Route path="/workspace/:id" component={SharedWorkspaceDetails} />
                    <Route path="/information-request" component={RequestGovernedDataset} />
                    <Route path="/dataset/:id" component={GovernedDatasetDetails} />
                </div>
            </div>
        </ConnectedRouter>
    );
};

export default Main;