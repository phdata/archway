import * as React from 'react';
import { Row, Col, Tabs, Card } from 'antd';
import HiveDatabase from '../HiveDatabase';
import { PermissionsCard, TablesCard } from '../cards';
import { Member, HiveAllocation, Workspace, NamespaceInfoList } from '../../../../models/Workspace';
import { Cluster } from '../../../../models/Cluster';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { tomorrowNightEighties } from 'react-syntax-highlighter/dist/styles/hljs';
import CardHeader from '../cards/CardHeader';

interface Props {
  workspace?: Workspace;
  cluster: Cluster;
  infos?: NamespaceInfoList;
  members?: Member[];
  selectedAllocation?: HiveAllocation;
  onAddMember: (e: React.MouseEvent) => void;
  onChangeAllocation: (allocation: HiveAllocation) => void;
  onChangeMemberRole: (distinguished_name: string, roleId: number, role: string, resource: string) => void;
  requestRefreshHiveTables: () => void;
  removeMember: (distinguished_name: string, database_role: string) => void;
}

class DataTab extends React.Component<Props> {
  public render() {
    const {
      workspace,
      infos,
      members,
      onAddMember,
      onChangeMemberRole,
      requestRefreshHiveTables,
    } = this.props;

    if (!workspace) {
      return null;
    }

    return (
      <div style={{ padding: 16 }}>
        <Tabs>
          {workspace.data.map((allocation: HiveAllocation, index: number) => {
            const isDefault = workspace.data.length === 1;
            const name = isDefault ? 'Default' : allocation.name.split('_')[0];
            return (
            <Tabs.TabPane tab={name} key={allocation.id.toString()}>
              <Row gutter={16} type="flex" justify="center" style={{ marginBottom: 16 }}>
                <Col span={6}>
                  <HiveDatabase
                    data={allocation}
                    isDefault
                  />
                </Col>
                <Col span={18}>
                  <Card style={{ height: '100%' }}>
                    <CardHeader>
                      Example
                    </CardHeader>
                    <SyntaxHighlighter language="sql" style={{ overflow: 'auto', ...tomorrowNightEighties }}>
                        {`CREATE TABLE ${allocation.name}.new_data_landing
LOCATION '${location}/new_data/landing'`}
                    </SyntaxHighlighter>
                  </Card>
                </Col>
              </Row>
              <Row gutter={16} type="flex" justify="center">
                <Col span={12}>
                  <TablesCard
                    info={infos}
                    onRefreshHiveTables={requestRefreshHiveTables}
                  />
                </Col>
                <Col span={12}>
                  <PermissionsCard
                    allocation={allocation}
                    members={members}
                    onAddMember={onAddMember}
                    onChangeMemberRole={(member, id, role) => {
                      onChangeMemberRole(member.distinguished_name, id, role, 'data');
                    }}
                  />
                </Col>
              </Row>
            </Tabs.TabPane>
          );
          })}
        </Tabs>
      </div>
    );
  }
}

export default DataTab;
