import React from 'react';
import "./Profile.css";
import {connect} from "react-redux";
import {requestLogout} from "../Auth/actions";

const Profile = ({profile, requestLogout}) =>  {
    let name;
    if (profile && profile.name)
        name = profile.name;

    return (
        <div className="Profile">
            <div className="Profile-name">
                {name}
            </div>
            <div className="Profile-logout">
                <a href="#" onClick={requestLogout}>
                    Log Out
                </a>
            </div>
        </div>
    );
};

export default connect(
    state => state.auth,
    {requestLogout}
)(Profile);