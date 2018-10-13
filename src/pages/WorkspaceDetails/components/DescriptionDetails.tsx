import * as React from 'react';
import { Card } from 'antd';
import Label from './Label';

interface Props {
  description: string;
}

const DescriptionDetails = ({ description }: Props) => (
  <Card
    style={{ display: 'flex', flex: 1 }}
    bodyStyle={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
    <Label>description</Label>
    <div
      style={{
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
      }}>
      <p style={{ textAlign: 'center', marginBottom: 0 }}>
        {description}
      </p>
    </div>
  </Card>
);

export default DescriptionDetails;
