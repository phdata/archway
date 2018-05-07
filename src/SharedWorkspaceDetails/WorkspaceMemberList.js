import React from "react";
import "./Compliance.css";
import Spinner from "../Common/Spinner";
import "./WorkspaceMemberList.css";
import "./DetailPanel.css";
import {Field, reduxForm} from "redux-form";

const MemberItem = ({member: {name, username}}) => (
    <div className={"MemberList-item"} key={username}>
        {name}
    </div>
);

let AddForm = ({handleSubmit}) => (
    <form onSubmit={handleSubmit}>
        <Field name="username" component="input" type="text" placeholder="enter username to add" />
    </form>
);

AddForm = reduxForm({
    form: "workspace_member"
})(AddForm);

const MemberList = ({members, onAdd}) => {
    let children = <Spinner>&nbsp;</Spinner>;
    if (members)
        children = members.map((member, index) => (<MemberItem member={member} index={index}/>));
    return (
        <div className="MemberList">
            <h2><i className="fa fa-user"/>Members<i className=""/></h2>
            {children}
            <div className="MemberList-item-add">
                <i className="fa fa-user-plus" />
                <AddForm onSubmit={onAdd} />
            </div>
        </div>
    );
};

export default MemberList;