import React from "react";
import "./Compliance.css";
import Spinner from "../Common/Spinner";

const MemberItem = ({member: {name, username}, index}) => (
    <div className={"MemberItem"}>
        <i class="fa fa-user"></i> {name}
    </div>
);

const MemberList = ({members}) => {
    let children = <Spinner>&nbsp;</Spinner>;
    if (members)
        children = members.map((member, index) => (<MemberItem member={member} index={index}/>));
    return (
        <div className="MemberList">
            {children}
        </div>
    );
};

export default MemberList;