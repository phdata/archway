import * as React from 'react';
import { Link as LinkTo, RouteComponentProps, withRouter } from 'react-router-dom';
import { Card, Tooltip } from 'antd';

import { LinksGroup, Link } from '../../../../models/Manage';
import { Colors } from '../../../../components';

interface Props extends RouteComponentProps<any> {
  linksGroup: LinksGroup;
}

const renderLinks = (links: Link[]) =>
  links.map((link, index) => (
    <div key={index} style={{ marginBottom: 8 }}>
      <Tooltip placement="right" title={link.description}>
        <a target="_blank" rel="noopener noreferrer" href={link.url} style={{ color: Colors.PrimaryColor.toString() }}>
          {link.name}
        </a>
      </Tooltip>
    </div>
  ));

const LinksGroupCard: React.FunctionComponent<Props> = ({ linksGroup, match }: Props) => {
  const { name, links, id } = linksGroup;
  return (
    <Tooltip title={linksGroup.description}>
      <Card
        title={<span style={{ fontSize: 25 }}>{name}</span>}
        hoverable
        extra={
          <LinkTo to={`${match.url}/${id}`} style={{ color: Colors.Green.toString() }}>
            Edit
          </LinkTo>
        }
        style={{ cursor: 'default' }}
        bodyStyle={{
          backgroundColor: '#fafafa',
          borderTop: '1px solid #e8e8e8',
          padding: '15px 24px',
          height: 250,
          overflow: 'overlay',
          textAlign: 'left',
          fontSize: 15,
        }}
      >
        {renderLinks(links)}
      </Card>
    </Tooltip>
  );
};

export default withRouter(LinksGroupCard);
