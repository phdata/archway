import * as React from 'react';
import { Row, Col } from 'antd';
import HiveDatabase from '../HiveDatabase';
import { InstructionCard, PermissionsCard, TablesCard } from '../cards';
import { Member, HiveAllocation, Workspace, NamespaceInfoList } from '../../../../models/Workspace';
import { Cluster } from '../../../../models/Cluster';

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
      cluster,
      infos,
      members,
      selectedAllocation,
      onAddMember,
      onChangeAllocation,
      onChangeMemberRole,
      requestRefreshHiveTables,
    } = this.props;

    if (!workspace) {
      return null;
    }

    return (
      <div style={{ padding: 16 }}>
        <Row gutter={16} type="flex">
          <Col span={12}>
            <Row gutter={16}>
              {workspace.data.map((allocation: HiveAllocation, index: number) => (
                <Col key={index} span={8} offset={workspace.data.length === 1 ? 8 : 0}>
                  <HiveDatabase
                    data={allocation}
                    isDefault={workspace.data.length === 1}
                    isSelected={!!selectedAllocation && allocation.id === selectedAllocation.id}
                    onSelect={() => onChangeAllocation(allocation)}
                  />
                </Col>
              ))}
            </Row>
            <Row style={{ marginTop: 16 }}>
              <Col span={24}>
                <InstructionCard
                  location={selectedAllocation && selectedAllocation.location}
                  namespace={selectedAllocation && selectedAllocation.name}
                  host={cluster.services.hive.thrift && cluster.services.hive.thrift[0].host}
                  port={cluster.services.hive.thrift && cluster.services.hive.thrift[0].port}
                  queue={workspace && workspace.processing && workspace.processing[0].pool_name}
                />
              </Col>
            </Row>
            <Row style={{ marginTop: 16 }}>
              <Col span={24}>
                <TablesCard
                  info={infos}
                  onRefreshHiveTables={requestRefreshHiveTables}
                />
              </Col>
            </Row>
          </Col>
          <Col span={12}>
            <PermissionsCard
              allocations={workspace.data}
              members={members}
              onAddMember={onAddMember}
              onChangeMemberRole={(member, id, role) => {
                onChangeMemberRole(member.distinguished_name, id, role, 'data');
              }}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

export default DataTab;
