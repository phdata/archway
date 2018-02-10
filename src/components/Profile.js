import React from 'react';
import "./Profile.css";
import {connect} from "react-redux";

const Profile = ({profile}) =>  {
    const initials = profile ? profile.initials : '';
    return (
        <div className="Profile">
            {initials}
        </div>
    );
};

export default connect(
    state => state.account,
    {}
)(Profile);