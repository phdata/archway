import * as React from 'react';
import { Card, Button } from 'antd';
import { ModalType } from '../../../../constants';

interface Props {
  children?: any;
  title: string;
  buttonText: string;
  modalType: ModalType;
  disabled: boolean;
  showModal: (e: React.MouseEvent, type: ModalType) => void;
}

const WorkspaceCard = ({ title, children, modalType, buttonText, disabled, showModal }: Props) => {
  return (
    <Card style={{ height: '100%' }} bordered>
      <div style={{ textAlign: 'center', fontSize: 17 }}>{title}</div>
      <div style={{ textAlign: 'left', fontSize: 15, padding: '20px 20px 100px 20px' }}>{children}</div>
      <div
        style={{
          fontWeight: 300,
          position: 'absolute',
          bottom: 20,
          left: '50%',
          transform: 'translateX(-50%)',
          padding: 20,
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
