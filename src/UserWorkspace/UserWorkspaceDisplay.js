import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightBlue } from 'react-syntax-highlighter/styles/hljs';
import "./UserWorkspaceDisplay.css";
import DatabaseDisplay from "../Common/DatabaseDisplay";

const UserWorkspaceDisplay = ({workspace, cluster}) => {
    const codeString = "SELECT * FROM " + workspace.database;
    return (
        <div className="UserWorkspaceDisplay">
            <h2 className="UserWorkspaceDisplay-notice">
                Your workspace is set up<br />on "{cluster.name}"!
            </h2>
            <DatabaseDisplay database={workspace.database} cluster={cluster} />
            Happy Coding!
        </div>
    );
};

export default UserWorkspaceDisplay;