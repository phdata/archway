import * as React from 'react';
import { Card, Button } from 'antd';
import { ModalType } from '../../../../constants';

interface Props {
  children?: any;
  title: string;
  modalType: ModalType;
  buttonText: string;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const ChangeOwnerCard = ({ title, modalType, buttonText, showModal, children }: Props) => (
  <Card style={{ height: '100%' }}>
    <div style={{ textAlign: 'center', fontSize: 17 }}>{title}</div>
    <div style={{ padding: '80px 20px 20px 20px' }}>
      <Button type="primary" onClick={e => showModal(e, modalType)}>
        {buttonText}
      </Button>
    </div>
    {children}
  </Card>
);

export default ChangeOwnerCard;
