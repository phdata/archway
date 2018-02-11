import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightBlue } from 'react-syntax-highlighter/styles/hljs';
import "./UserWorkspaceDisplay.css";

const UserWorkspaceDisplay = ({workspace, cluster}) => {
    const codeString = "SELECT * FROM " + workspace.database;
    return (
        <div className="UserWorkspaceDisplay">
            <h2 className="UserWorkspaceDisplay-notice">
                Your workspace is set up<br />on "{cluster.name}"!
            </h2>
            <dl className="UserWorkspaceDisplay-info">
                <dt>Database</dt>
                <dd>{workspace.database}</dd>
                <dt>Data Directory</dt>
                <dd>{workspace.data_directory}</dd>
            </dl>
            <SyntaxHighlighter language="sql" customStyle={{
                display: "flex",
                width: "100%",
                height: 50,
                alignItems: "center",
                justifyContent: "center",
                marginTop: 15,
                marginBottom: 15
            }} style={tomorrowNightBlue}>
                {codeString}
            </SyntaxHighlighter>
            Happy Coding!
        </div>
    );
};

export default UserWorkspaceDisplay;