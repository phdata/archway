import * as React from 'react';
import { Card } from 'antd';
import Label from './Label';

interface Props {
  description: string;
}

const DescriptionDetails = ({description}: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>description</Label>
    <p style={{ textAlign: 'center', marginBottom: 0 }}>
      {description}
    </p>
  </Card>
);

export default DescriptionDetails;
