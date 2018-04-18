import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import {tomorrowNightBlue} from 'react-syntax-highlighter/styles/hljs';
import "./DatabaseDisplay.css"
import Spinner from "./Spinner";
import "./Panel.css";
import {workspace} from "../API";

const syntaxStyle = {
    marginTop: 10,
    marginBottom: 10,
    fontSize: 18,
    padding: 20
};

const DatabaseDisplay = ({name, cluster}) => {
    console.log(cluster);
    console.log(name);
    if (name) {
        const impala = `$ impala-shell -i ${cluster.services.impala.host} -d ${name}`;
        const jdbc = `jdbc:impala://${cluster.services.impala.host}/${name}`;
        const beeline = `$ beeline -u 'jdbc:hive2://${cluster.services.impala.host}/${name};auth=noSasl'`;
        return (
            <div className="DatabaseDisplay">
                <h2><i className="fa fa-database"/>Data ({name})</h2>
                <div className="DatabaseDisplay-Display">
                    <div className="DatabaseDisplay-Display-left">
                        <h4>Connect Via JDBC</h4>
                        <SyntaxHighlighter language="sql" customStyle={syntaxStyle} style={tomorrowNightBlue}>
                            {jdbc}
                        </SyntaxHighlighter>
                        <h4>Connect Via impala-shell</h4>
                        <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={tomorrowNightBlue}>
                            {impala}
                        </SyntaxHighlighter>
                        <h4>Connect Via Beeline</h4>
                        <SyntaxHighlighter language="shell" customStyle={syntaxStyle} style={tomorrowNightBlue}>
                            {beeline}
                        </SyntaxHighlighter>
                    </div>
                    <div className="DatabaseDisplay-Display-right">
                        N/A
                        <h4>GB</h4>
                    </div>
                </div>
            </div>
        );
    }
    else {
        return <Spinner>Setting things up, please wait...</Spinner>
    }
};

export default DatabaseDisplay;