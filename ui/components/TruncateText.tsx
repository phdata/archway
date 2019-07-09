import * as React from 'react';

interface Props {
  text: string;
  maxLine: number;
  lineHeight: number;
}

const TruncateText = ({ text, maxLine, lineHeight }: Props) => (
  <div
    style={{
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      display: '-webkit-box',
      lineHeight: `${lineHeight}px`,
      minHeight: `${lineHeight * maxLine}px`,
      WebkitLineClamp: maxLine,
      WebkitBoxOrient: 'vertical',
      wordBreak: 'break-all',
    }}
  >
    {text}
  </div>
);

export default TruncateText;
