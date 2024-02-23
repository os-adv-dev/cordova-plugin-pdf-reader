//
//  BPIWebViewController.h
//  BPI App Shell
//
//  Created by BPI
//  Copyright © 2016 BPI. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVInvokedUrlCommand.h>

@interface PDFViewerViewController : UIViewController<UIWebViewDelegate, UIScrollViewDelegate, NSURLConnectionDelegate, WKNavigationDelegate>

+ (instancetype) initWithFileString:(NSString*) fileString
                           andTitle:(NSString*) aTitle
                         withPlugin:(CDVPlugin*) aPlugin
                        withButtons:(NSArray*) aButtons
                        withSubject:(NSString*) subject
                    withDescription:(NSString*) description
                      andCallbackId:(NSString *)aCallbackId;
@end
