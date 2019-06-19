export const FEATURE_FLAG: string = 'FEATURE_FLAG';

export const setFeatureFlag = (featureFlags: string[]) => {
  return {
    type: FEATURE_FLAG,
    featureFlags,
  };
};
