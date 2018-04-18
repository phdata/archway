import React from 'react';
import logo from './logo.png';
import Profile from "./Profile";
import ClusterInfo from "./ClusterInfo";
import {NavLink} from "react-router-dom";
import './Navigation.css';

const Navigation = () => (
    <div className="Navigation">
        <div className="Navigation-left">
            <NavLink to="/personal" className="Navigation-title-link">
                <div className="Navigation-title">
                    <img src={logo} alt="logo" className="Navigation-logo"/>
                    <h1>Heimdali</h1>
                </div>
            </NavLink>
            <div className="Navigation-nav">
                <NavLink to="/personal" className="Navigation-nav-link" activeClassName="active">
                    <i className="fa fa-home"></i>Home
                </NavLink>
                <NavLink to="/workspaces" className="Navigation-nav-link" activeClassName="active">
                    <i className="fa fa-users"></i>Workspaces
                </NavLink>
                <NavLink to="/areas" className="Navigation-nav-link" activeClassName="active">
                    <i className="fa fa-info"></i>Areas
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