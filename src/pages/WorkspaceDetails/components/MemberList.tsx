import { Card, Icon, Row, notification } from 'antd';
import * as React from 'react';
import { connect } from 'react-redux';
import { Dispatch } from 'redux';
import { createStructuredSelector } from 'reselect';
import { Member } from '../../../types/Workspace';
import * as selectors from '../selectors';
import { Colors } from '../../../components';
import { requestRemoveMember } from '../actions';
import CardHeader from './CardHeader';

interface Props {
  members?: Member[];

  removeMember: (distinguished_name: string) => void;
  showModal: (e: React.MouseEvent) => void;
}

class MemberList extends React.Component<Props> {
  public componentWillReceiveProps(nextProps: Props) {
    const oldMembers = this.props.members || [];
    const newMembers = nextProps.members || [];
    if (oldMembers !== newMembers) {
        const oldMembersDict: any = {};
        const newMembersDict: any = {};
        oldMembers.forEach((member: Member) => {
          if (member.removeStatus && member.removeStatus.loading) {
            oldMembersDict[member.distinguished_name] = member;
          }
        });
        newMembers.forEach((member: Member) => {
          if (member.removeStatus) {
            newMembersDict[member.distinguished_name] = member;
          }
        });
        Object.keys(oldMembersDict).forEach((distinguished_name: string) => {
          const newMember: Member = newMembersDict[distinguished_name];
          const { removeStatus = null } = newMember || {};
          if (!removeStatus) {
            notification.open({
              message: 'Member Successfully Removed',
              description: 'Member removed successfully!',
            });
          } else if (removeStatus.error) {
            notification.open({
              message: 'Member NOT Removed',
              description: `The following error occurred when removing the member: ${removeStatus.error}`,
            });
          }
        });
      }
  }

  public renderMember = (member: Member) => {
    const { removeMember } = this.props;
    const { name, distinguished_name, removeStatus = {} } = member;

    return (
      <div
        key={distinguished_name}
        style={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}
      >
        <span
          style={{
            color: 'rgba(0, 0, 0, 0.65)',
            fontSize: '14px',
            fontWeight: 600,
            marginRight: '16px',
          }}
        >{name}</span>
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            width: '50px',
            height: '20px',
          }}
        >
          {removeStatus.loading ? (
            <Icon
              theme="outlined"
              type="loading"
              style={{ fontSize: 16 }}
            />
          ) : (
            <span
              style={{
                color: Colors.Green.string(),
                fontSize: '10px',
                cursor: 'pointer',
              }}
              onClick={() => removeMember(distinguished_name)}
            >REMOVE</span>
          )}
        </div>
      </div>
    );
  }

  public render() {
    const { members, showModal } = this.props;

    return (
      <Card
        actions={[
          <a href="#" onClick={showModal}>Add a member</a>,
        ]}
      >
      <CardHeader
        icon="lock"
        heading="Membership"
        subheading={`${members ? members!.length : 0} members`} />
        <Row gutter={12} type="flex" justify="center" style={{ marginTop: 18 }}>
          {members && members.length > 0 && members.map(this.renderMember)}
          {(!members || members.length <= 0) && (
            <div style={{ color: 'rgba(0, 0, 0, .65)' }}>
              No additional members yet.
            </div>
          )}
        </Row>
      </Card>
    );
  }
}

const mapStateToProps = () =>
  createStructuredSelector({
    members: selectors.getMembers(),
  });

const mapDispatchToProps = (dispatch: Dispatch<any>) => ({
  removeMember: (distinguished_name: string) => dispatch(requestRemoveMember(distinguished_name)),
});

export default connect(mapStateToProps, mapDispatchToProps)(MemberList);
