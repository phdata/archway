import * as React from 'react';
import Service, { ServiceInfo } from './Service';

const Hue = (props: ServiceInfo) => {
  const hueLinks = props.service.load_balancer.map((location: object) => (
    <a target="_blank" rel="noreferrer noopener" href={`https://${location.host}:${location.port}`}>Hue UI</a>
  ));
  return (
    <Service
      links={[]}
      {...props} />
  );
}

export default Hue;