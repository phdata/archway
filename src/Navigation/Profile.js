import React from 'react';
import {connect} from "react-redux";
import { Button } from 'antd';

import {requestLogout} from "../Auth/actions";

const Profile = ({profile, requestLogout}) =>  {
    let name;
    if (profile && profile.name)
        name = profile.name;

    return (
        <div style={{ width: '100%', padding: 15, color: 'white', textAlign: 'center', backgroundColor: '#415161' }}>
            <div>
                Hey, {name}
            </div>
            <Button ghost size="small" onClick={requestLogout}>
                Log Out
            </Button>
        </div>
    );
};

export default connect(
    state => state.auth,
    {requestLogout}
)(Profile);
