import * as React from 'react';
import Service, { ServiceInfo } from './Service';

const Hive = (props: ServiceInfo) => (
  <Service
    links={[]}
    {...props} />
);

export default Hive;