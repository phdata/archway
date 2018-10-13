import { Card } from 'antd';
import * as React from 'react';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/styles/hljs';
import { Label } from '.';

interface Props {
  location: string;
  namespace: string;
}

const SetupHelp = ({ location, namespace }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>step 1: prepare</Label>
    <SyntaxHighlighter language="sql" style={{ overflow: 'auto', ...tomorrowNightEighties }}>
        {`CREATE TABLE ${namespace}.new_data_landing
LOCATION '${location}/new_data/landing'`}
    </SyntaxHighlighter>
  </Card>
);

export default SetupHelp;
