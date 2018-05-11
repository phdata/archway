import React from "react";
import SyntaxHighlighter from 'react-syntax-highlighter';
import {tomorrowNightBlue} from 'react-syntax-highlighter/styles/hljs';
import Spinner from "./Spinner";
import MetricDisplay from "./MetricDisplay";
import "./DatabaseDisplay.css"

const syntaxStyle = {
  marginTop: 10,
  marginBottom: 10,
  fontSize: 18,
  padding: 20
};

const DatabaseDisplay = ({database, cluster}) => {
  let content;
  if (database && database.name && cluster.services) {
    const impala = `$ impala-shell -i ${cluster.services.IMPALA.host}:21000 -d ${database.name}`;
    const jdbc = `jdbc:impala://${cluster.services.IMPALA.host}:21050/${database.name}`;
    const beeline = `$ beeline -u 'jdbc:hive2://${cluster.services.HIVESERVER2.host}:10000/${database.name};auth=noSasl'`;
    content = (<div className="DatabaseDisplay-Display">
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
        <MetricDisplay metric={database.size_in_gb} label="gb quota"/>
      </div>
    </div>);
  } else {
    content = <Spinner>Provisioning...</Spinner>
  }
  return (<div className="DatabaseDisplay">
    <h2><i className="fa fa-database"/>Data {database ? `(${database.name})` : ""}</h2>
    {content}
  </div>)
};

export default DatabaseDisplay;
