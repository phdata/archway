import * as React from 'react';
import { Card, Button } from 'antd';
import { ModalType } from '../../../../constants';

interface Props {
  children?: any;
  buttonText: string;
  modalType: ModalType;
  disabled: boolean;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const WorkspaceCard = ({ children, modalType, buttonText, disabled, showModal }: Props) => {
  return (
    <Card style={{ height: '100%' }} bordered>
      <div style={{ textAlign: 'center', fontSize: 17 }}>{children}</div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          fontWeight: 300,
          padding: '32px 0 48px 0',
        }}
      >
        <Button type="primary" onClick={e => showModal(e, modalType)} disabled={disabled}>
          {buttonText}
        </Button>
      </div>
    </Card>
  );
};

export default WorkspaceCard;
