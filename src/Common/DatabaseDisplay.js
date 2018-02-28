import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightBlue } from 'react-syntax-highlighter/styles/hljs';
import "./DatabaseDisplay.css"
import Spinner from "./Spinner";

const syntaxStyle = {
    marginTop: 10,
    marginBottom: 10,
    fontSize: 18,
    padding: 20
};

const DatabaseDisplay = ({database: {name}, cluster}) => {
    console.log(cluster);
    if(name) {
        const cli = `$ impala-shell -i ${cluster.services.impala.host} -d ${name}`
        const jdbc = `jdbc:impala://${cluster.services.impala.host}/${name}`;
        return (
            <div className="DatabaseDisplay">
                <h2>Here are a few ways to connect to your shared workspace...</h2>
                <h3>Via JDBC</h3>
                <SyntaxHighlighter language="sql" customStyle={syntaxStyle} style={tomorrowNightBlue}>
                    {jdbc}
                </SyntaxHighlighter>
                <h3>Via impala-shell</h3>
                <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={tomorrowNightBlue}>
                    {cli}
                </SyntaxHighlighter>
            </div>
        );
    }
    else {
        return <Spinner>Setting things up, please wait...</Spinner>
    }
};

export default DatabaseDisplay;