import * as React from 'react';
import { withRouter, RouteComponentProps } from 'react-router-dom';
import { Card } from 'antd';

import { ComplianceContent, Question } from '../../../../models/Manage';
import TruncateText from '../../../../components/TruncateText';

interface Props extends RouteComponentProps<any> {
  compliance: ComplianceContent;
  history: any;
  match: any;
}

const biggerThan = (str: Question[], num: number): Question[] => {
  if (str.length > num) {
    return str.slice(0, num);
  } else {
    return str;
  }
};

const ComplianceQuestionCard = ({ compliance, history, match }: Props) => {
  const { name, description, questions, id } = compliance;
  const { url } = match;
  return (
    <Card
      bordered
      hoverable
      style={{ height: 300 }}
      onClick={() => {
        history.push(`${url}/${id}`);
      }}
    >
      <div style={{ height: 252, overflow: 'hidden' }}>
        <TruncateText text={name} maxLine={1} lineHeight={30} style={{ fontSize: 24, textTransform: 'uppercase' }} />
        <TruncateText text={description} maxLine={1} lineHeight={30} style={{ fontSize: 16 }} />
        <ul style={{ textAlign: 'left', marginLeft: -24, marginBottom: 0 }}>
          {biggerThan(questions, 5).map((question, index) => (
            <li key={index}>
              <TruncateText text={question.question} maxLine={1} lineHeight={30} />
            </li>
          ))}
        </ul>
        {questions.length > 5 && <span style={{ fontSize: 30 }}>&hellip;</span>}
      </div>
    </Card>
  );
};

export default withRouter(ComplianceQuestionCard);
