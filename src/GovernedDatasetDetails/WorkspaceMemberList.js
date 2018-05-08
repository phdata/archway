import React from "react";
import "./Compliance.css";
import Spinner from "../Common/Spinner";
import "./WorkspaceMemberList.css";

const MemberItem = ({member: {name, username}, index}) => (
    <div className={"MemberItem"}>
        {name}
    </div>
);

const MemberList = ({members}) => {
    let children = <Spinner>&nbsp;</Spinner>;
    if (members)
        children = members.map((member, index) => (<MemberItem member={member} index={index}/>));
    return (
        <div className="MemberList">
            <h2><i className="fa fa-user"/>Members<i className=""/> </h2>
            {children}
            </div>
    );
};

export default MemberList;