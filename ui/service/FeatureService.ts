import { providerStore } from '../../ui';

export const prepareFeatureString = (feature: string): string => {
  return feature.toLowerCase().trim();
};

export class FeatureService {
  public isEnabled(feature: string): boolean {
    const mapState = providerStore.getState() as any;
    const { config } = mapState.toJS();
    const { featureFlags } = config;
    return featureFlags
      .map((featureFlag: string) => prepareFeatureString(featureFlag))
      .includes(prepareFeatureString(feature));
  }

  public all(): void {
    const mapState = providerStore.getState() as any;
    return mapState.toJS().config.featureFlag;
  }
}
