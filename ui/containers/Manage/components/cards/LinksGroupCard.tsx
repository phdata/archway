import * as React from 'react';
import { Link as LinkTo, RouteComponentProps, withRouter } from 'react-router-dom';
import { Card, Tooltip } from 'antd';

import { LinksGroup, Link } from '../../../../models/Manage';
import { Colors } from '../../../../components';
import { LinksGroupCardPage } from '../../../../constants';

interface Props extends RouteComponentProps<any> {
  linksGroup: LinksGroup;
  page: LinksGroupCardPage;
}

const cardHeight = {
  [LinksGroupCardPage.Overview]: 120,
  [LinksGroupCardPage.Manage]: 250,
};

const renderLinks = (links: Link[]) => (
  <ul>
    {links.map((link, index) => (
      <li key={index} style={{ marginBottom: 8 }}>
        <Tooltip placement="right" title={link.description}>
          <a
            target="_blank"
            rel="noopener noreferrer"
            href={link.url}
            style={{ color: Colors.PrimaryColor.toString() }}
          >
            {link.name}
          </a>
        </Tooltip>
      </li>
    ))}
  </ul>
);

const LinksGroupCard: React.FunctionComponent<Props> = ({ linksGroup, match, page }: Props) => {
  const { name, links, id } = linksGroup;

  return (
    <Tooltip title={linksGroup.description}>
      <Card
        title={<span style={{ fontSize: 25, fontWeight: 300 }}>{name}</span>}
        hoverable
        extra={
          page === LinksGroupCardPage.Manage && (
            <LinkTo to={`${match.url}/${id}`} style={{ color: Colors.Green.toString() }}>
              Edit
            </LinkTo>
          )
        }
        style={{ cursor: 'default' }}
        bodyStyle={{
          backgroundColor: '#fafafa',
          borderTop: '1px solid #e8e8e8',
          padding: '15px 24px',
          height: cardHeight[page],
          overflow: 'auto',
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
