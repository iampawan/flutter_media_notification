#import "MediaNotificationPlugin.h"
#import <AVFoundation/AVPlayer.h>
#import <MediaPlayer/MediaPlayer.h>
#import <AVFoundation/AVFoundation.h>

@implementation MediaNotificationPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"media_notification"
            binaryMessenger:[registrar messenger]];
  MediaNotificationPlugin* instance = [[MediaNotificationPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"show" isEqualToString:call.method]) {
      NSString* title = call.arguments[@"title"];
      NSString* author = call.arguments[@"author"];
      BOOL play = call.arguments[@"play"];
    
      NSMutableDictionary *albumInfo = [[NSMutableDictionary alloc] init];
     // UIImage *artWork = [UIImage imageNamed:album.imageUrl];
      [albumInfo setObject:title forKey:MPMediaItemPropertyTitle];
      [albumInfo setObject:author forKey:MPMediaItemPropertyArtist];
     // [albumInfo setObject:album.title forKey:MPMediaItemPropertyAlbumTitle];
     // [albumInfo setObject:artworkP forKey:MPMediaItemPropertyArtwork];
     // [albumInfo setObject:album.playrDur forKey:MPMediaItemPropertyPlaybackDuration]
    //  [albumInfo setObject:album.elapsedTime forKey:MPNowPlayingInfoPropertyElapsedPlaybackTime]
      [[MPNowPlayingInfoCenter defaultCenter] setNowPlayingInfo:albumInfo];
     
      if (play == YES) {
          [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
          [[AVAudioSession sharedInstance]setActive:YES error:nil];
      }
      
      //setup and receive remote control events:

      result(@"success");
  }
}

@end
