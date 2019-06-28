#include "AppDelegate.h"
#include "GeneratedPluginRegistrant.h"
#import <AVFoundation/AVFoundation.h>
#import <MediaPlayer/MediaPlayer.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  [GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
    NSError *error = nil;
    if (![[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:&error]) {
        NSLog(@"%@", error);
    }
    [[UIApplication sharedApplication] beginReceivingRemoteControlEvents];
    return [super application:application didFinishLaunchingWithOptions:launchOptions];

}


//- (void)remoteControlReceivedWithEvent:(UIEvent *)receivedEvent {
//    if (receivedEvent.type == UIEventTypeRemoteControl) {
//        switch (receivedEvent.subtype) {
//            case UIEventSubtypeRemoteControlPlay:
//                [[NSNotificationCenter defaultCenter] postNotificationName:kCYCAppDelegatePlayPauseNotificationName object:nil];
//                break;
//            case UIEventSubtypeRemoteControlPause:
//                [[NSNotificationCenter defaultCenter] postNotificationName:kCYCAppDelegatePlayPauseNotificationName object:nil];
//                break;
//            case UIEventSubtypeRemoteControlTogglePlayPause:
//                [[NSNotificationCenter defaultCenter] postNotificationName:kCYCAppDelegatePlayPauseNotificationName object:nil];
//                break;
//            default:
//                break;
//        }
//    }
//}

@end
