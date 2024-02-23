//
//  PDFViewer.h
//  iOSPDFPlugin
//
//  Created by Manuel Mouta on 26/12/2016.
//
//

#import <Cordova/CDVPlugin.h>

@interface PDFViewer : CDVPlugin
- (void)openPdf:(CDVInvokedUrlCommand*)command;
@end
