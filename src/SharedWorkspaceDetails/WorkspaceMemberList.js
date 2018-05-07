import React from "react";
import "./Compliance.css";
import Spinner from "../Common/Spinner";
import "./WorkspaceMemberList.css";
import "./DetailPanel.css";
import {Field, reduxForm} from "redux-form";

const MemberItem = ({member: {name, username}, id, onRemove}) => (
    <div className={"MemberList-item"} key={username}>
        <i className="fa fa-user-times" onClick={() => onRemove(id, username)} />
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

const MemberList = ({members, onAdd, onRemove}) => {
    let children = <Spinner>&nbsp;</Spinner>;
    if (members)
        children = members.map((member, index) => (<MemberItem member={member} index={index} onRemove={onRemove}/>));
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