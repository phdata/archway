import React from 'react';
import './Navigation.css';
import logo from './logo.png';
import Profile from "./Profile";
import ClusterInfo from "./ClusterInfo";

const Navigation = () => {
    return (
        <div className="Navigation">
            <div className="Navigation-left">
                <img src={logo} alt="logo" className="Navigation-logo" />
                Heimdali
            </div>
            <div className="Navigation-right">
                <ClusterInfo />
                <Profile />
            </div>
        </div>
    );
};

export default Navigation;