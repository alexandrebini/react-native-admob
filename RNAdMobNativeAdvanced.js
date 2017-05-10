import React from 'react';
import { requireNativeComponent } from 'react-native';

const RNBanner = requireNativeComponent('RNAdMobNativeAdvanced', AdMobNativeAdvanced);

export default class AdMobNativeAdvanced extends React.Component {
  render() {
    const { adUnitID, testDeviceID, didFailToReceiveAdWithError } = this.props;
    return (
      <RNBanner
        onAdViewDidReceiveAd={this.props.adViewDidReceiveAd}
        onDidFailToReceiveAdWithError={(event) => didFailToReceiveAdWithError(event.nativeEvent.error)}
        onAdViewWillPresentScreen={this.props.adViewWillPresentScreen}
        onAdViewWillDismissScreen={this.props.adViewWillDismissScreen}
        onAdViewDidDismissScreen={this.props.adViewDidDismissScreen}
        onAdViewWillLeaveApplication={this.props.adViewWillLeaveApplication}
        onAppInstallAdLoaded={this.props.appInstallAdLoaded}
        testDeviceID={testDeviceID}
        adUnitID={adUnitID}
      />
    );
  }
}

AdMobNativeAdvanced.propTypes = {
  /**
   * AdMob ad unit ID
   */
  adUnitID: React.PropTypes.string,

  /**
   * Test device ID
   */
  testDeviceID: React.PropTypes.string,

  /**
   * AdMob iOS library events
   */
  adViewDidReceiveAd: React.PropTypes.func,
  didFailToReceiveAdWithError: React.PropTypes.func,
  adViewWillPresentScreen: React.PropTypes.func,
  adViewWillDismissScreen: React.PropTypes.func,
  adViewDidDismissScreen: React.PropTypes.func,
  adViewWillLeaveApplication: React.PropTypes.func,
  appInstallAdLoaded: React.PropTypes.func
};

AdMobNativeAdvanced.defaultProps = { didFailToReceiveAdWithError: () => {} };
