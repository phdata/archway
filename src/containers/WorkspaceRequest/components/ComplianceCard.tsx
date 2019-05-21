import * as React from 'react';
import { Card, Icon, Row, Col, Checkbox } from 'antd';

interface Props {
  type: string;
  data: any;
  values: any;
  onChange: (name: string, value: boolean) => void;
}

const CompliancePage = ({ type, data, values: checkValues, onChange } : Props) => {
  const { icon, label: complianceLabel, values } = data;

  return (
    <Card
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        fontSize: 11,
      }}
      bodyStyle={{
        padding: 12,
      }}
    >
      <Icon
        style={{ fontSize: 48 }}
        type={icon}
        theme="twoTone"
        twoToneColor="red"
      />
      <h3 style={{ textTransform: 'uppercase', fontSize: 24, marginBottom: 0 }}>{type}</h3>
      <div style={{ textAlign: 'center', lineHeight: 2, marginBottom: 8 }}>
        {complianceLabel}
        <br />
        DO YOU ANTICIPATE THE POSSIBILITY OF DATA CONTAINING...
      </div>
      <Row type="flex" justify="center" align="middle" gutter={24}>
        <Col span={12} />
        <Col span={2}>YES</Col>
        <Col span={2}>NO</Col>
      </Row>
      {values.map(({ key, label }: any) => {
        const checked = checkValues[key];
        return (
          <Row
            key={key}
            type="flex"
            justify="center"
            align="middle"
            gutter={24}
            style={{ marginTop: 4 }}
          >
            <Col span={12} style={{ textAlign: 'left' }}>
              {label}
            </Col>
            <Col span={2}>
              <Checkbox
                indeterminate={(checked === true)}
                checked={false}
                onChange={() => onChange(key, true)}
              />
            </Col>
            <Col span={2}>
              <Checkbox
                indeterminate={(checked === false)}
                checked={false}
                onChange={() => onChange(key, false)}
              />
            </Col>
          </Row>
        );
      })}
    </Card>
  );
};

export default CompliancePage;
