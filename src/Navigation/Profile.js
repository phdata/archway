import React from 'react';
import {connect} from "react-redux";
import { Button, Avatar } from 'antd';

import {requestLogout} from "../Auth/actions";

const Profile = ({profile, requestLogout}) =>  {
    let name;
    if (profile && profile.name)
        name = profile.name;

    return (
        <div style={{ width: '100%', padding: 15, color: 'white', textAlign: 'center', borderTop: '1px solid #C6CACF', borderBottom: '1px solid #C6CACF', backgroundColor: '#415161' }}>
            <Avatar shape="circle" size="large" icon="user" style={{ backgroundColor: 'transparent', color: 'white' }} />
            <h3 style={{ color: 'white' }}>
                hey, {name}!
            </h3>
            <h6>
              <a href="#" style={{ textTransform: 'uppercase', fontWeight: 200, color: 'white' }} onClick={requestLogout}>
                  log out
              </a>
            </h6>
        </div>
    );
};

export default connect(
    state => state.auth,
    {requestLogout}
)(Profile);
