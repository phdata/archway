import React, {Component} from 'react';
import './Navigation.css';
import logo from './logo.png';
import {Link} from "react-router-dom";

class Navigation extends Component {

    static workspaceItem(workspace) {
        return (
            <h1>{workspace}</h1>
        )
    }

    render() {
        const {username, cluster} = this.props;
        return (
            <div className="Navigation">
                <div className="Navigation-branding">
                    <img src={logo} width={200} alt="logo"/>
                </div>
                <div className="Navigation-context">
                    <h4>{username}</h4>
                    <h3>{cluster}</h3>
                </div>
                <div>
                    <Link to="/shared-request" className="Navigation-link">Create a new Request</Link>
                </div>
            </div>
        );
    }

}

export default Navigation;