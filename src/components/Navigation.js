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
                    Heimdali
                </div>
                <ul className="Navigation-nav">
                    <li>
                        <NavLink to="/" activeStyle={{
                            color: "lightgray"
                        }}>
                            My Workspace
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/shared" activeStyle={{
                            color: "lightgray"
                        }}>
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