import * as React from 'react';
import { Col, Row } from 'antd';
import ComplianceCard from './ComplianceCard';
import { RequestInput } from '../../../models/RequestInput';

const complianceData = {
  pci: {
    icon: 'bank',
    label: 'Payment Card Industry [Data Security Standard]',
    values: [
      {
        key: 'card',
        label: 'Full or partial credit card numbers?',
      },
      {
        key: 'bank',
        label: 'Full or partial bank account numbers?',
      },
      {
        key: 'other',
        label: 'Any other combination of data that can be used to make purchases?',
      },
    ],
  },
  pii: {
    icon: 'idcard',
    label: 'Personally Identifiable Information',
    values: [
      {
        key: 'name',
        label: 'Full name',
      },
      {
        key: 'address',
        label: 'Home address',
      },
      {
        key: 'email',
        label: 'Email address',
      },
      {
        key: 'ssn',
        label: 'Social security number',
      },
      {
        key: 'passport',
        label: 'Passport number',
      },
      {
        key: 'driver_license',
        // prettier-ignore
        label: 'Driver\'s license number'
      },
      {
        key: 'credit_card',
        label: 'Credit card number',
      },
      {
        key: 'dob',
        label: 'Date of birth',
      },
      {
        key: 'phone',
        label: 'Telephone number',
      },
      {
        key: 'credentials',
        label: 'Software credentials',
      },
    ],
  },
  phi: {
    icon: 'medicine-box',
    label: 'Protected Health Information',
    values: [
      {
        key: 'status',
        label: 'Health status',
      },
      {
        key: 'provision',
        label: 'Provision of health care',
      },
      {
        key: 'payment',
        label: 'Payment for health care',
      },
    ],
  },
};

interface Props {
  request: RequestInput;
  setRequest: (request: RequestInput | boolean) => void;
}

interface State {
  pci: any;
  pii: any;
  phi: any;
}

class CompliancePage extends React.Component<Props, State> {
  public state: State = {
    pci: {},
    pii: {},
    phi: {},
  };

  public getBooleanFromData(data: any) {
    return Object.keys(data).filter(key => !!data[key]).length > 0;
  }

  public onChange(type: 'pci' | 'pii' | 'phi', name: string, value: boolean) {
    const { request, setRequest } = this.props;
    const { [type]: data } = this.state;
    const newData = {
      ...data,
      [name]: value,
    };
    switch (type) {
      case 'pci':
        this.setState({ pci: newData });
        break;
      case 'pii':
        this.setState({ pii: newData });
        break;
      case 'phi':
        this.setState({ phi: newData });
        break;
      default:
    }

    if (Object.keys(newData).length === complianceData[type].values.length) {
      let typeKey: 'pci_data' | 'pii_data' | 'phi_data' = 'pci_data';
      if (type === 'pii') {
        typeKey = 'pii_data';
      } else if (type === 'phi') {
        typeKey = 'phi_data';
      }
      setRequest({
        ...request,
        compliance: {
          ...request.compliance,
          [typeKey]: this.getBooleanFromData(newData),
        },
      });
    }
  }

  public render() {
    const { pci, pii, phi } = this.state;

    return (
      <div>
        <h3>Help us be proactive in addressing risk-related data needs</h3>
        <Row type="flex" gutter={25} style={{ marginTop: 25, marginBottom: 25 }}>
          <Col span={8}>
            <ComplianceCard
              type="pci"
              data={complianceData.pci}
              values={pci}
              onChange={(name: string, value: boolean) => this.onChange('pci', name, value)}
            />
          </Col>
          <Col span={8}>
            <ComplianceCard
              type="pii"
              data={complianceData.pii}
              values={pii}
              onChange={(name: string, value: boolean) => this.onChange('pii', name, value)}
            />
          </Col>
          <Col span={8}>
            <ComplianceCard
              type="phi"
              data={complianceData.phi}
              values={phi}
              onChange={(name: string, value: boolean) => this.onChange('phi', name, value)}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

export default CompliancePage;
