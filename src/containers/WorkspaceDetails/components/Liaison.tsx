import * as React from 'react';
import { Icon } from 'antd';
import { Colors } from '../../../components';
import { Member } from '../../../models/Workspace';

interface Props {
  data?: Member;
}

const Liaison = ({ data }: Props) => data ? (
  <div style={{ display: 'flex', alignItems: 'center' }}>
    <div style={{ textTransform: 'uppercase', textAlign: 'right', marginRight: 8 }}>
      <div style={{ fontSize: 18, fontWeight: 300 }}>LIAISON</div>
      <a href={`mailto:${data.email}`} style={{ color: Colors.Green.string(), textTransform: 'uppercase' }}>
        {data.name}
      </a>
    </div>
    <Icon style={{ fontSize: 40 }} type="crown" theme="twoTone" twoToneColor="#D7C9AA" />
  </div>
) : null;

export default Liaison;
