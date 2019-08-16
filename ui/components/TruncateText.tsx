import * as React from 'react';

interface Props {
  text: string;
  maxLine: number;
  lineHeight: number;
  style?: React.CSSProperties;
}

// tslint:disable-next-line: no-shadowed-variable
const TruncateText = ({ text, maxLine, lineHeight, style }: Props) => (
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
      ...style,
    }}
  >
    {text}
  </div>
);

export default TruncateText;
