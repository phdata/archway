import React from 'react';
import './Navigation.css';
import logo from './logo.png';
import Profile from "./Profile";
import ClusterInfo from "./ClusterInfo";
import {NavLink} from "react-router-dom";

const Navigation = () => {
    return (
        <div className="Navigation">
            <div className="Navigation-left">
                <div className="Navigation-title">
                    <img src={logo} alt="logo" className="Navigation-logo"/>
                    <h1>Heimdali</h1>
                </div>
                <ul className="Navigation-nav">
                    <li>
                        <NavLink to="/" exact className="Navigation-nav-link" activeClassName="active">
                            My Workspace
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/workspaces" className="Navigation-nav-link" activeClassName="active">
                            Shared Workspaces
                        </NavLink>
                    </li>
                </ul>
            </div>
            <div className="Navigation-right">
                <ClusterInfo/>
                <Profile/>
            </div>
        </div>
    );
};

export default Navigation;