import React from 'react';
import logo from './logo.png';
import Profile from "./Profile";
import ClusterInfo from "./ClusterInfo";
import {NavLink} from "react-router-dom";
import './Navigation.css';

const Navigation = () => (
    <div className="Navigation">
        <div className="Navigation-left">
            <div className="Navigation-title">
                <img src={logo} alt="logo" className="Navigation-logo"/>
                <h1>Heimdali</h1>
            </div>
            <div className="Navigation-nav">
                <NavLink to="/" exact className="Navigation-nav-link" activeClassName="active">
                    My Workspace
                </NavLink>
                <NavLink to="/workspaces" className="Navigation-nav-link" activeClassName="active">
                    Shared Workspaces
                </NavLink>
            </div>
        </div>
        <div className="Navigation-right">
            <ClusterInfo/>
            <Profile/>
        </div>
    </div>
);

export default Navigation;