import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import {tomorrowNightBlue} from 'react-syntax-highlighter/styles/hljs';
import "./DatabaseDisplay.css"
import Spinner from "./Spinner";
import "./Panel.css";

const syntaxStyle = {
    marginTop: 10,
    marginBottom: 10,
    fontSize: 18,
    padding: 20
};

const DatabaseDisplay = ({database, cluster}) => {
    if (database && database.name && cluster.services) {
        const impala = `$ impala-shell -i ${cluster.services.IMPALA.host}:10000 -d ${database.name}`;
        const jdbc = `jdbc:impala://${cluster.services.IMPALA.host}:10000/${database.name}`;
        const beeline = `$ beeline -u 'jdbc:hive2://${cluster.services.IMPALA.host}:10000/${database.name};auth=noSasl'`;
        return (
            <div className="DatabaseDisplay">
                <h2><i className="fa fa-database"/>Data ({database.name})</h2>
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
                        {database.size_in_gb}
                        <h4>GB</h4>
                    </div>
                </div>
            </div>
        );
    }
    else {
        return <Spinner>Provisioning your workspace...</Spinner>
    }
};

export default DatabaseDisplay;