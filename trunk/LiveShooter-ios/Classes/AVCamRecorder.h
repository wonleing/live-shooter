
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@protocol AVCamRecorderDelegate;

@interface AVCamRecorder : NSObject {
}

@property (nonatomic,retain) AVCaptureSession *session;
@property (nonatomic,retain) AVCaptureMovieFileOutput *movieFileOutput;
@property (nonatomic,copy) NSURL *outputFileURL;
@property (nonatomic,readonly) BOOL recordsVideo;
@property (nonatomic,readonly) BOOL recordsAudio;
@property (nonatomic,readonly,getter=isRecording) BOOL recording;
@property (nonatomic,assign) id <NSObject,AVCamRecorderDelegate> delegate;

-(id)initWithSession:(AVCaptureSession *)session outputFileURL:(NSURL *)outputFileURL;
-(void)startRecordingWithOrientation:(AVCaptureVideoOrientation)videoOrientation;
-(void)stopRecording;

@end

@protocol AVCamRecorderDelegate
@required
-(void)recorderRecordingDidBegin:(AVCamRecorder *)recorder;
-(void)recorder:(AVCamRecorder *)recorder recordingDidFinishTomovieFileOutputFileURL:(NSURL *)outputFileURL error:(NSError *)error;
@end
