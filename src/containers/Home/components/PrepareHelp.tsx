import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/dist/styles/hljs';
import Label from './Label';

interface Props {
  location: string;
  namespace: string;
}

const SetupHelp = ({ location, namespace }: Props) => (
  <div style={{ padding: '4px' }}>
    <Label>step 1: prepare</Label>
    <p style={{
      marginBottom: '0.5em',
      fontSize: '12px',
      textAlign: 'center',
    }}>set up a table for your work in your namespace</p>
    <SyntaxHighlighter language="sql" style={{ overflow: 'auto', ...tomorrowNightEighties }}>
        {`CREATE TABLE ${namespace}.new_data_landing
LOCATION '${location}/new_data/landing'`}
    </SyntaxHighlighter>
  </div>
);

export default SetupHelp;
