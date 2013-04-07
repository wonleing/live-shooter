

#import "PutController.h"

#import "NetworkManager.h"

#include <CFNetwork/CFNetwork.h>

enum {
    kSendBufferSize = 32768
};

@interface PutController () <UITextFieldDelegate, NSStreamDelegate>

// things for IB

//@property (nonatomic, strong, readwrite) IBOutlet UITextField *               urlText;
//@property (nonatomic, strong, readwrite) IBOutlet UITextField *               usernameText;
//@property (nonatomic, strong, readwrite) IBOutlet UITextField *               passwordText;
//@property (nonatomic, strong, readwrite) IBOutlet UILabel *                   statusLabel;
//@property (nonatomic, strong, readwrite) IBOutlet UIActivityIndicatorView *   activityIndicator;
//@property (nonatomic, strong, readwrite) IBOutlet UIBarButtonItem *           cancelButton;
//
//- (IBAction)sendAction:(UIView *)sender;
//- (IBAction)cancelAction:(id)sender;

@property (nonatomic, retain) UITextField * urlText;
@property (nonatomic, retain) UITextField * usernameText;
@property (nonatomic, retain) UITextField * passwordText;
@property (nonatomic, retain) UILabel * statusLabel;

// Properties that don't need to be seen by the outside world.

@property (nonatomic, assign, readonly ) BOOL              isSending;
@property (nonatomic, strong, readwrite) NSOutputStream *  networkStream;
@property (nonatomic, strong, readwrite) NSInputStream *   fileStream;
@property (nonatomic, assign, readonly ) uint8_t *         buffer;
@property (nonatomic, assign, readwrite) size_t            bufferOffset;
@property (nonatomic, assign, readwrite) size_t            bufferLimit;

@end

@implementation PutController
{
    uint8_t                     _buffer[kSendBufferSize];
    UITextField * _urlText;
    UITextField * _usernameText;
    UITextField * _passwordText;
    UILabel *     _statusLabel;
    UIButton *    _sendButton;
}

@synthesize urlText           = _urlText;
@synthesize usernameText      = _usernameText;
@synthesize passwordText      = _passwordText;
@synthesize statusLabel       = _statusLabel;
@synthesize documentFilePath  = _documentPaht;

//@synthesize activityIndicator = _activityIndicator;
//@synthesize cancelButton      = _cancelButton;

@synthesize networkStream = _networkStream;
@synthesize fileStream    = _fileStream;
@synthesize bufferOffset  = _bufferOffset;
@synthesize bufferLimit   = _bufferLimit;

#pragma mark * Status management

// These methods are used by the core transfer code to update the UI.

- (void)sendDidStart
{
    self.statusLabel.text = @"Sending";
//    self.cancelButton.enabled = YES;
//    [self.activityIndicator startAnimating];
    [[NetworkManager sharedInstance] didStartNetworkOperation];
}

- (void)updateStatus:(NSString *)statusString
{
    assert(statusString != nil);
    self.statusLabel.text = statusString;
}

- (void)sendDidStopWithStatus:(NSString *)statusString
{
    if (statusString == nil) {
        statusString = @"Put succeeded";
    }
    self.statusLabel.text = statusString;
//    self.cancelButton.enabled = NO;
//    [self.activityIndicator stopAnimating];
    [[NetworkManager sharedInstance] didStopNetworkOperation];
}

#pragma mark * Core transfer code

// This is the code that actually does the networking.

// Because buffer is declared as an array, you have to use a custom getter.  
// A synthesised getter doesn't compile.

- (uint8_t *)buffer
{
    return self->_buffer;
}

- (BOOL)isSending
{
    return (self.networkStream != nil);
}

- (void)startSend:(NSURL *)filePath   //filePath里是沙盒路径
{
    BOOL                    success;
    NSURL *                 url;
    
//    assert(filePath != nil);
//    assert([[NSFileManager defaultManager] fileExistsAtPath:filePath]); //返回一个BOOL值,判断文件是否已存在
//    assert( [filePath.pathExtension isEqual:@"png"] || [filePath.pathExtension isEqual:@"jpg"] );
//    
//    assert(self.networkStream == nil);      // don't tap send twice in a row!
//    assert(self.fileStream == nil);         // ditto

    // First get and check the URL.
//    NSLog(@"self.urlText is %@,",self.urlText.text);
    url = [[NetworkManager sharedInstance] smartURLForString:self.urlText.text];
    success = (url != nil);
    
    if (success) {
        // Add the last part of the file name to the end of the URL to form the final 
        // URL that we're going to put to.
        
        url = CFBridgingRelease(  //拼传送路径
            CFURLCreateCopyAppendingPathComponent(NULL, ( CFURLRef) url, ( CFStringRef) [filePath lastPathComponent], false)
        );
        success = (url != nil);
        NSLog(@"putController url is %@",url);
    }
    
    // If the URL is bogus, let the user know.  Otherwise kick off the connection.

    if ( ! success) {
        self.statusLabel.text = @"Invalid URL";
    } else {

        // Open a stream for the file we're going to send.  We do not open this stream; 
        // NSURLConnection will do it for us.
        
        NSLog(@"%@", [filePath path]);
        self.fileStream = [NSInputStream inputStreamWithFileAtPath:[filePath path]];  //这里写入吗。。返回什么流
        NSLog(@"file stream %@",[filePath path]);
        NSLog(@"%d", [[NSFileManager defaultManager] fileExistsAtPath:[filePath path]]);
        NSLog(@"%@", [NSURL fileURLWithPath:[filePath path]]);
        assert(self.fileStream != nil);
        
        [self.fileStream open];
        
        // Open a CFFTPStream for the URL.

        self.networkStream = CFBridgingRelease(
            CFWriteStreamCreateWithFTPURL(NULL, ( CFURLRef) url)
        );
        assert(self.networkStream != nil);

        if ([self.usernameText.text length] != 0) { //
            success = [self.networkStream setProperty:self.usernameText.text forKey:(id)kCFStreamPropertyFTPUserName];
            assert(success);
            success = [self.networkStream setProperty:self.passwordText.text forKey:(id)kCFStreamPropertyFTPPassword];
            assert(success);
        }

        self.networkStream.delegate = self;
        [self.networkStream scheduleInRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        [self.networkStream open];

        // Tell the UI we're sending.
        
        [self sendDidStart];
    }
}

- (void)stopSendWithStatus:(NSString *)statusString
{
    if (self.networkStream != nil) {
        [self.networkStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSDefaultRunLoopMode];
        self.networkStream.delegate = nil;
        [self.networkStream close];
        self.networkStream = nil;
    }
    if (self.fileStream != nil) {
        [self.fileStream close];
        self.fileStream = nil;
    }
    [self sendDidStopWithStatus:statusString];
}

- (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode
    // An NSStream delegate callback that's called when events happen on our 
    // network stream.
{
    #pragma unused(aStream)
    assert(aStream == self.networkStream);

    switch (eventCode) {
        case NSStreamEventOpenCompleted: {
            [self updateStatus:@"Opened connection"];
        } break;
        case NSStreamEventHasBytesAvailable: {
            assert(NO);     // should never happen for the output stream
        } break;
        case NSStreamEventHasSpaceAvailable: {
            [self updateStatus:@"Sending"];
            
            // If we don't have any data buffered, go read the next chunk of data.
            
            if (self.bufferOffset == self.bufferLimit) {
                NSInteger   bytesRead;
//                NSLog(@"%s",self.buffer);
                bytesRead = [self.fileStream read:self.buffer maxLength:kSendBufferSize];
                
                if (bytesRead == -1) {
                    [self stopSendWithStatus:@"File read error"];
                } else if (bytesRead == 0) {
                    [self stopSendWithStatus:nil];
                } else {
                    self.bufferOffset = 0;
                    self.bufferLimit  = bytesRead;
                }
            }
            
            // If we're not out of data completely, send the next chunk.
            
            if (self.bufferOffset != self.bufferLimit) {
                NSInteger   bytesWritten;
                bytesWritten = [self.networkStream write:&self.buffer[self.bufferOffset] maxLength:self.bufferLimit - self.bufferOffset];
                assert(bytesWritten != 0);
                if (bytesWritten == -1) {
                    [self stopSendWithStatus:@"Network write error"];
                } else {
                    self.bufferOffset += bytesWritten;
                }
            }
        } break;
        case NSStreamEventErrorOccurred: {
            [self stopSendWithStatus:@"Stream open error"];
        } break;
        case NSStreamEventEndEncountered: {
            // ignore
        } break;
        default: {
            assert(NO);
        } break;
    }
}

#pragma mark * Actions

- (void)sendAction:(UIView *)sender
{
    assert( [sender isKindOfClass:[UIView class]] );

    if ( ! self.isSending ) {
        NSURL *  filePath;
        
        // User the tag on the UIButton to determine which image to send.
        
        assert(sender.tag >= 0);
        filePath = [[[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject] URLByAppendingPathComponent:@"output.mov"];
//        filePath = [[NetworkManager sharedInstance] pathForTestImage:(NSUInteger) sender.tag]; //
//        NSLog(@"%@",filePath);
        assert(filePath != nil);
        
        [self startSend:filePath];
    }
}

- (void)cancelAction:(id)sender
{
    #pragma unused(sender)
    [self stopSendWithStatus:@"Cancelled"];
}

- (void)textFieldDidEndEditing:(UITextField *)textField
    // A delegate method called by the URL text field when the editing is complete. 
    // We save the current value of the field in our settings.
{
    NSString *  defaultsKey;
    NSString *  newValue;
    NSString *  oldValue;
    
    if (textField == self.urlText) {
        defaultsKey = @"CreateDirURLText";
    } else if (textField == self.usernameText) {
        defaultsKey = @"Username";
    } else if (textField == self.passwordText) {
        defaultsKey = @"Password";
    } else {
        assert(NO);
        defaultsKey = nil;          // quieten warning
    }

    newValue = textField.text;
    oldValue = [[NSUserDefaults standardUserDefaults] stringForKey:defaultsKey];

    // Save the URL text if it's changed.
    
    assert(newValue != nil);        // what is UITextField thinking!?!
    assert(oldValue != nil);        // because we registered a default
    
    if ( ! [newValue isEqual:oldValue] ) {
        [[NSUserDefaults standardUserDefaults] setObject:newValue forKey:defaultsKey];
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
    // A delegate method called by the URL text field when the user taps the Return 
    // key.  We just dismiss the keyboard.
{
    #pragma unused(textField)
    assert( (textField == self.urlText) || (textField == self.usernameText) || (textField == self.passwordText) );
    [textField resignFirstResponder];
    return NO;
}

#pragma mark * View controller boilerplate

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
    
    _urlText = [[UITextField alloc] initWithFrame:CGRectMake(0, 0, 320, 30)];
    _urlText.borderStyle = UITextBorderStyleRoundedRect;
    _urlText.text = @"192.168.0.136";
    [self.view addSubview:_urlText];
    
    _usernameText = [[UITextField alloc] initWithFrame:CGRectMake(20, 50, 120, 30)];
    _usernameText.borderStyle = UITextBorderStyleRoundedRect;
    _usernameText.text = @"same_y";
    [self.view addSubview:_usernameText];
    
    _passwordText = [[UITextField alloc] initWithFrame:CGRectMake(180, 50, 120, 30)];
    _passwordText.borderStyle = UITextBorderStyleRoundedRect;
    _passwordText.text = @"q312444516";
    [self.view addSubview:_passwordText];
    
    _statusLabel = [[UILabel alloc] initWithFrame:CGRectMake(100, 100, 220, 50)];
    [self.view addSubview:_statusLabel];
    
    _sendButton = [UIButton buttonWithType:UIButtonTypeRoundedRect];
    _sendButton.tag = 1;
    _sendButton.frame = CGRectMake(self.view.frame.size.width / 2 - 50, 150, 100, 50);
    [_sendButton addTarget:self action:@selector(sendAction:) forControlEvents:UIControlEventTouchUpInside];
    [_sendButton setTitle:@"上传" forState:UIControlStateNormal];
    [self.view addSubview:_sendButton];
    
    assert(self.urlText != nil);
    assert(self.usernameText != nil);
    assert(self.passwordText != nil);
    assert(self.statusLabel != nil);
//    assert(self.activityIndicator != nil);
//    assert(self.cancelButton != nil);

//    self.urlText.text = [[NSUserDefaults standardUserDefaults] stringForKey:@"PutURLText"];
    // The setup of usernameText and passwordText deferred to -viewWillAppear:
    // because those values are shared by multiple tabs.
    
//    self.activityIndicator.hidden = YES;
    self.statusLabel.text = @"Tap start the put";
//    self.cancelButton.enabled = NO;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
//    self.usernameText.text = [[NSUserDefaults standardUserDefaults] stringForKey:@"Username"];
//    self.passwordText.text = [[NSUserDefaults standardUserDefaults] stringForKey:@"Password"];
}

- (void)viewDidUnload
{
    [super viewDidUnload];

    self.urlText = nil;
    self.usernameText = nil;
    self.passwordText = nil;
    self.statusLabel = nil;
//    self.activityIndicator = nil;
//    self.cancelButton = nil;
}

- (void)dealloc
{
    [self.urlText release];
    [self.usernameText release];
    [self.statusLabel release];
    [self stopSendWithStatus:@"Stopped"];
    [super dealloc];
}

@end
