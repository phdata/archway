import {createSelector} from 'reselect';

import Color from '../Common/Colors';
import {getCluster} from '../selectors';

const serviceColor = (service: any) => {
  switch (service && service.status) {
    case 'GOOD_HEALTH':
      return Color.Green.rgb().string();
    case 'CONCERNING_HEALTH':
      return Color.Orange.rgb().string();
    case 'BAD_HEALTH':
      return Color.Red.rgb().string();
    default:
      return '#aaa'
  }
}

const serviceText = (service: any) => {
  switch (service && service.status) {
    case 'GOOD_HEALTH':
      return '"good"'
    case 'CONCERNING_HEALTH':
      return '"concerning"'
    case 'BAD_HEALTH':
      return '"bad"'
    default:
      return 'unknown'
  }
}

const glowColor = (status: any) => {
  switch (status) {
    case 'GOOD_HEALTH':
      return `0 0 5px 2px ${Color.Green.hsl().string()}`;
    case 'CONCERNING_HEALTH':
      return `0 0 5px 2px ${Color.Orange.hsl().string()}`;
    case 'BAD_HEALTH':
      return `0 0 5px 2px ${Color.Red.hsl().string()}`;
    default:
      return false
  }
}

const getColor