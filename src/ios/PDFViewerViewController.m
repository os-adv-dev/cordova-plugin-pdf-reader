//
//  BPIWebViewController.m
//  BPI App Shell
//
//  Created BPI
//  Copyright © 2016 BPI. All rights reserved.
//

#import "PDFViewerViewController.h"
#import "UIColor+BPIColor.h"


#define CUSTOM_ORANGE_COLOR [UIColor colorWithRed:255.0/255.0 green:102.0/255.0 blue:0.0/255.0 alpha:1]
#define CUSTOM_BLUE_COLOR [UIColor colorWithRed:0/255.0 green:0/255.0 blue:83/255.0 alpha:1]
#define CUSTOM_BLUE_COLOR_DISABLED [UIColor colorWithRed:0.40 green:0.40 blue:0.59 alpha:1.00]

#define ORANGE_BPI      @"#FF6600"
#define ORANGE_DISABLED @"#FFC299"


@interface PDFViewerViewController ()

@property (weak, nonatomic) IBOutlet WKWebView *webView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *loadingIndicator;
@property (weak, nonatomic) IBOutlet UIView *header;

@property (weak, nonatomic) IBOutlet UIButton *backBtn;
@property (weak, nonatomic) IBOutlet UILabel *titleLbl;
@property (weak, nonatomic) IBOutlet UIButton *shareBtn;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterShare;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterBack;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterTitle;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintHeightHeader;

@property(strong, nonatomic) NSString *fileString;
@property(strong, nonatomic) NSString *viewTitle;
@property(strong, nonatomic) NSString *subject;
@property(strong, nonatomic) NSString *textDescription;
@property(strong, nonatomic) NSURL *localFileURL;

@property(weak, nonatomic) CDVPlugin *plugin;
@property(strong, nonatomic) NSString *callbackId;
@property(strong, nonatomic) NSArray *buttons;

@property(nonatomic) BOOL isWebViewLoaded;

// Offline Support
@property (strong, nonatomic) NSString *failedURL;

@property (strong, nonatomic) NSURLConnection* urlConnection;
@property (strong, nonatomic) NSURLRequest* urlRequest;

@property (weak, nonatomic) IBOutlet UIView *footerView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *footerHeight;

@property (weak, nonatomic) IBOutlet UILabel *lblDescription;
@property (weak, nonatomic) IBOutlet UIButton *btn1;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1Width;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1WidthEqualsBtn2Constraint;
@property (weak, nonatomic) IBOutlet UIButton *btn2;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn2Width;
@property (weak, nonatomic) IBOutlet UIButton *btn3;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3Width;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3WidthEqualsBtn1Constraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn3Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn2Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *btn1Leading;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *trailing;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintHeightDescription;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintCenterButtons;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintButtonBottom;

@property (strong, nonatomic) NSArray* arrayButtons;

@end

@implementation PDFViewerViewController

#pragma mark - Convenience methods

+ (instancetype) initWithFileString:(NSString*) fileString
                           andTitle:(NSString*) aTitle
                         withPlugin:(CDVPlugin*) aPlugin
                        withButtons:(NSArray*) aButtons
                        withSubject:(NSString*) subject
                    withDescription:(NSString*) description
                      andCallbackId:(NSString*) aCallbackId
{
    PDFViewerViewController *vc = [[PDFViewerViewController alloc] init];
    if(vc)
    {
        vc.fileString = fileString;
        vc.viewTitle = aTitle;
        vc.plugin = aPlugin;
        vc.buttons = aButtons;
        vc.subject = subject;
        vc.callbackId = aCallbackId;
        vc.textDescription = description;
    }

    return vc;
}

#pragma mark - UIViewController methods
- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.webView.navigationDelegate = self;

    //WKWebview
    // loads everything first into memory before rendering
    //self.webView.suppressesIncrementalRendering = YES;
    //self.webView.scalesPageToFit = YES;
    
    self.webView.configuration.suppressesIncrementalRendering = YES;
    
    //if its a url don't transform
    if([self.fileString hasPrefix:@"http://"] || [self.fileString hasPrefix:@"https://"])
    {

        NSURL* pdfURL = [NSURL URLWithString:self.fileString];
        NSURLRequest* request = [NSURLRequest requestWithURL:pdfURL];
        [self.webView loadRequest:request];
    }
    else
    {
        //if base64 transform into file
        NSData* fileData = [[NSData alloc] initWithBase64EncodedString:self.fileString options:NSDataBase64DecodingIgnoreUnknownCharacters];
        NSString *pdfName = self.viewTitle;

        NSString *guidPDF = [NSString stringWithFormat:@"%@.pdf", pdfName];
        self.localFileURL = [NSURL fileURLWithPath:[NSTemporaryDirectory() stringByAppendingString:guidPDF]];
        [fileData writeToURL:self.localFileURL atomically:NO];
        //WKWebview
        [self.webView loadData:fileData MIMEType:@"application/pdf" characterEncodingName:@"utf-8" baseURL:self.localFileURL];
        //[self.webView loadData:fileData MIMEType:@"application/pdf" textEncodingName:@"utf-8" baseURL:self.localFileURL];
    }
    
//    NSData* fileData;
//
//    if([self.fileString hasPrefix:@"http://"] || [self.fileString hasPrefix:@"https://"])
//    {
//        fileData = [[NSData alloc] initWithContentsOfURL:[
//        NSURL URLWithString:self.fileString]];
//    }
//    else{
//
//    //if base64 transform into file
//    fileData = [[NSData alloc] initWithBase64EncodedString:self.fileString options:NSDataBase64DecodingIgnoreUnknownCharacters];
//    }
//    NSString *pdfName = self.viewTitle;
//
//    NSString *guidPDF = [NSString stringWithFormat:@"%@.pdf", pdfName];
//    self.localFileURL = [NSURL fileURLWithPath:[NSTemporaryDirectory() stringByAppendingString:guidPDF]];
//    [fileData writeToURL:self.localFileURL atomically:NO];
//    [self.webView loadData:fileData MIMEType:@"application/pdf" textEncodingName:@"utf-8" baseURL:self.localFileURL];
    

}

- (void)viewDidAppear:(BOOL)animated
{
    [self setupTitleHeader];
    [super viewDidAppear:animated];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.webView.navigationDelegate = self;
    [[self.btn1 layer] setBorderWidth:1.0];
    [[self.btn1 layer] setBorderColor:[[UIColor colorWithHexString:ORANGE_BPI] CGColor]];
    [[self.btn2 layer] setBorderWidth:1.0];
    [[self.btn2 layer] setBorderColor:[[UIColor colorWithHexString:ORANGE_BPI] CGColor]];
    [[self.btn3 layer] setBorderWidth:1.0];
    [[self.btn3 layer] setBorderColor:[[UIColor colorWithHexString:ORANGE_BPI] CGColor]];
    self.arrayButtons = [NSArray arrayWithObjects: self.btn1,self.btn2,self.btn3, nil];
    [self setMyButtons:self.buttons];

    if(self.textDescription)
    {
        self.lblDescription.text = self.textDescription;
        [self.lblDescription sizeToFit];
        NSInteger numberOfLines = floor(ceilf(CGRectGetHeight(self.lblDescription.frame)) / self.lblDescription.font.lineHeight);
        float height = 0;
        if(numberOfLines == 1)
        {
            height = 0;
        }
        else if(numberOfLines == 2)
        {
            height = self.lblDescription.frame.size.height;
        }
        else if(numberOfLines >= 3)
        {
            height = self.lblDescription.frame.size.height;
        }
        self.constraintCenterButtons.active = FALSE;
        self.footerHeight.constant = self.footerHeight.constant + height;
    }
    else
    {
        self.constraintButtonBottom.active = FALSE;
        self.lblDescription.hidden = YES;
        self.footerHeight.constant = self.footerHeight.constant - self.lblDescription.frame.size.height;
        [self.lblDescription removeFromSuperview];
    }
    

    self.constraintHeightHeader.constant  = [UIApplication sharedApplication].statusBarFrame.size.height > 20 ? 84.0 : self.constraintHeightHeader.constant;
    self.constraintCenterBack.constant = [UIApplication sharedApplication].statusBarFrame.size.height / 2;
    self.constraintCenterShare.constant = [UIApplication sharedApplication].statusBarFrame.size.height / 2;
    self.constraintCenterTitle.constant = [UIApplication sharedApplication].statusBarFrame.size.height / 2;
    
    
    self.webView.scrollView.delegate = self;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    /*[[UIApplication sharedApplication] setStatusBarStyle:UIStatusBarStyleDefault];
    UIView *statusBar = [[[UIApplication sharedApplication] valueForKey:@"statusBarWindow"] valueForKey:@"statusBar"];

    if ([statusBar respondsToSelector:@selector(setBackgroundColor:)]) {
        statusBar.backgroundColor = [UIColor clearColor];
    }*/
}


#pragma mark - IBAction methods

- (IBAction)back
{

    CATransition *transition = [CATransition animation];
    transition.duration = 0.3;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type = kCATransitionPush;
    transition.subtype = kCATransitionFromLeft;
    [self.view.window.layer addAnimation:transition forKey:nil];


    [self dismissViewControllerAnimated:YES
                             completion:^{
                                 
                                NSError *errorBlock;
                                if([[NSFileManager defaultManager] removeItemAtURL:self.localFileURL error:&errorBlock] == NO) {
                                    NSLog(@"error deleting file %@", errorBlock.localizedDescription);
                                }
        
                                [self webViewUnload];
                                 
                                 if(!self.plugin) return;
                                 
                                 CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                                                   messageAsString:@"-1"];
                                 [pluginResult setKeepCallbackAsBool:NO];
                                 [self.plugin.commandDelegate sendPluginResult:pluginResult
                                                                    callbackId:self.callbackId];
                             }];
}

- (IBAction)share
{
    if (!self.isWebViewLoaded) return;
        //WKWebview
    NSURL *resourceUrl = self.webView.URL;
    
    if([resourceUrl.absoluteString hasPrefix:@"http://"] || [resourceUrl.absoluteString hasPrefix:@"https://"])
    {
        //save locally for share (applying name as well)
        NSData* fileData = [[NSData alloc] initWithContentsOfURL:resourceUrl];
        NSString *pdfName = self.viewTitle;
        
        NSString *guidPDF = [NSString stringWithFormat:@"%@.pdf", pdfName];
        NSURL *url = [NSURL fileURLWithPath:[NSTemporaryDirectory() stringByAppendingString:guidPDF]];
        [fileData writeToURL:url atomically:NO];
        
        //switch to local for download
        self.localFileURL = url;
        
    }

    UIActivityViewController *activityViewController = [[UIActivityViewController alloc] initWithActivityItems:@[self.localFileURL] applicationActivities:nil];
    if(self.subject && ![self.subject isEqualToString:@""])
    {
        [activityViewController setValue:self.subject forKey:@"subject"];
    }

    [self presentViewController:activityViewController
                       animated:YES
                     completion:nil];

}

- (IBAction)buttonAction : (UIButton*) sender
{
    CATransition *transition = [CATransition animation];
    transition.duration = 0.3;
    transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
    transition.type = kCATransitionPush;
    transition.subtype = kCATransitionFromLeft;
    [self.view.window.layer addAnimation:transition forKey:nil];


    [self dismissViewControllerAnimated:YES
                             completion:^{
                                 NSError *errorBlock;
                                 if([[NSFileManager defaultManager] removeItemAtURL:self.localFileURL error:&errorBlock] == NO) {
                                     NSLog(@"Error deleting file %@. Maybe it was an url.", errorBlock.localizedDescription);
                                 }
                                [self webViewUnload];


                                 if(!self.plugin) return;

                                 CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                     messageAsString:[NSString stringWithFormat:@"%ld", (long)sender.tag]];
                                 
                                 [pluginResult setKeepCallbackAsBool:NO];
                                 [self.plugin.commandDelegate sendPluginResult:pluginResult
                                                                    callbackId:self.callbackId];
                             }];
}
-(void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation
{
    [self.loadingIndicator startAnimating];
    self.isWebViewLoaded = NO;
}
//- (void)webViewDidStartLoad:(UIWebView *)webView
//{
//    [self.loadingIndicator startAnimating];
//    self.isWebViewLoaded = NO;
//}
- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation{
    [self.loadingIndicator stopAnimating];
    self.isWebViewLoaded = YES;
}
//-(void)webViewDidFinishLoad:(UIWebView *)webView
//{
//    if(webView.isLoading) return;
//
//    [self.loadingIndicator stopAnimating];
//    self.isWebViewLoaded = YES;
//}

#pragma mark - UIScrollViewDelegate Protocol
- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
    if(scrollView.contentOffset.y >= (scrollView.contentSize.height - scrollView.frame.size.height))
    {
        //Bottom reached, activate any needsScrolltToEnd Buttons
        int i = 0;
        for (NSDictionary *buttonSettings in self.buttons)
        {
            UIButton* currentButton = self.arrayButtons[i];
            NSString* needsScrolltoEnd = buttonSettings[@"needScrollToEnd"];
            if([needsScrolltoEnd isEqualToString:@"true"])
            {
                currentButton.enabled = YES;
                [currentButton setBackgroundColor:[UIColor colorWithHexString:ORANGE_BPI]];
                [[currentButton layer] setBorderColor:[[UIColor colorWithHexString:ORANGE_BPI] CGColor]];
            }
            
            ++i;
        }
    }
    if(scrollView.contentOffset.y <= 0.0)
    {
        //Top reached
        //Do nothing
    }
}

#pragma mark - Utilities
- (void)setupTitleHeader
{
    [self preferredStatusBarStyle];
    
    [self setNeedsStatusBarAppearanceUpdate];
    [self.titleLbl setText:self.viewTitle];
    [self.titleLbl setFont:[UIFont fontWithName:@"Barlow Regular" size:17]];
}

-(void) showButtonsFromListSize: (long) size
{
    switch (size) {
        case 0:
            //hide all buttons
            self.footerHeight.constant = 0;
            self.btn1.hidden = YES;
            self.btn2.hidden = YES;
            self.btn3.hidden = YES;
            break;
        case 1:
            //hide button 3
            self.btn3Width.constant = 0;
            self.btn3.hidden = YES;
            self.btn3WidthEqualsBtn1Constraint.active = FALSE;
            self.btn3Leading.constant = 0;
            self.btn1Leading.priority = 1000;
            
            //hide button 2
            self.btn2Width.constant = 0;
            self.btn2.hidden = YES;
            self.btn1WidthEqualsBtn2Constraint.active = FALSE;
            self.btn2Leading.constant = 0;
            self.btn2Leading.priority = 1000;

            break;
        case 2:
            self.btn3Width.constant = 0;
            self.btn3.hidden = YES;
            self.btn3WidthEqualsBtn1Constraint.active = FALSE;
            self.btn3Leading.constant = 0;
            self.btn1Leading.priority = 1000;
            break;
        case 3:
            break;
    }
}

-(void) setMyButtons:(NSArray *)buttons
{
    UIColor *btnBackgroundColor = [UIColor colorWithHexString:ORANGE_BPI];
    
    [self showButtonsFromListSize:[buttons count]];
    int i = 0;
    for (NSDictionary *buttonSettings in buttons)
    {
        UIButton* currentBtn = (UIButton*)(self.arrayButtons[i]);
        
        NSString* name = (NSString*)(buttonSettings[@"name"]);
        NSString *isDefault = (buttonSettings[@"isDefault"]);
        if([isDefault isEqualToString:@"true"]){
            [currentBtn setTitleColor:[UIColor whiteColor ] forState:UIControlStateNormal];
            currentBtn.backgroundColor = btnBackgroundColor;
        }
        long btnID = [buttonSettings[@"id"] longValue];
        currentBtn.tag = btnID;
        [currentBtn setTitle:name forState:UIControlStateNormal];
        
        NSString *needsScrollToEnd = buttonSettings[@"needScrollToEnd"];
        [buttonSettings setValue:needsScrollToEnd forKey:@"needScrollToEnd"];
        if([needsScrollToEnd isEqualToString:@"true"])
        {
            currentBtn.enabled = NO;
            [currentBtn setBackgroundColor:[UIColor colorWithHexString:ORANGE_DISABLED]];
            [[currentBtn layer] setBorderColor:[[UIColor colorWithHexString:ORANGE_DISABLED] CGColor]];
        }
        
        ++i;
    }
}


- (UIStatusBarStyle)preferredStatusBarStyle {
    UIStatusBarStyle style = UIStatusBarStyleDefault;
    return style;
}

#pragma mark - cleanup
//-(void) dealloc
//{
//    self.webView = nil;
//}
-(void)webViewUnload {

    self.localFileURL = nil;
    
    [self.webView stopLoading];
    self.webView.navigationDelegate = nil;
    [self.webView removeFromSuperview];
    
    [self removeFromParentViewController];
    

    self.webView = nil;

    [[NSURLCache sharedURLCache] removeAllCachedResponses];
    [[NSURLCache sharedURLCache] setDiskCapacity:0];
    [[NSURLCache sharedURLCache] setMemoryCapacity:0];
}

//removing this will crash the app on bigger PDFs
-(void) didReceiveMemoryWarning{
    
    [[NSURLCache sharedURLCache] removeAllCachedResponses];
    [[NSURLCache sharedURLCache] setDiskCapacity:0];
    [[NSURLCache sharedURLCache] setMemoryCapacity:0];
}

@end
