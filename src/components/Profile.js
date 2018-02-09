import React from 'react';
import "./Profile.css";
import {connect} from "react-redux";

const Profile = ({profile}) =>  {
    return (
        <div className="Profile">
            {profile.initials}
        </div>
    );
};

export default connect(
    state => state.account,
    {}
)(Profile);