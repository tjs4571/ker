//
//  UILabel+KerViewProtocol.m
//  Ker
//
//  Created by hailong11 on 2018/11/21.
//  Copyright © 2018 kkmofang.cn. All rights reserved.
//

#import "UILabel+KerViewProtocol.h"
#import "UIColor+Ker.h"
#import "UIFont+Ker.h"
#import "KerApp.h"

@implementation UILabel (KerViewProtocol)

-(void) KerViewObtain:(KerViewId) viewId app:(KerApp *)app {
    [super KerViewObtain:viewId app:app];
    self.numberOfLines = 0;
}

-(void) KerView:(KerViewId)viewId setAttribute:(NSString *)key value:(NSString *)value app:(KerApp *)app {
    [super KerView:viewId setAttribute:key value:value app:app];
    
    if(key == nil) {
        return;
    }
    
    if([key isEqualToString:@"color"]) {
        self.textColor = [UIColor colorWithKerCString:[value UTF8String]];
    } else if([key isEqualToString:@"font"]) {
        self.font = [UIFont fontWithKerCString:[value UTF8String]];
    } else if([key isEqualToString:@"text-align"]) {
        kk::ui::TextAlign v = kk::ui::TextAlignFromString([value UTF8String]);
        switch (v) {
            case kk::ui::TextAlignCenter:
                self.textAlignment = NSTextAlignmentCenter;
                break;
            case kk::ui::TextAlignEnd:
            case kk::ui::TextAlignRight:
                self.textAlignment = NSTextAlignmentRight;
                break;
            default:
                self.textAlignment = NSTextAlignmentLeft;
                break;
        }
    } else if([key isEqualToString:@"#text"]) {
        self.text = value;
    }
}


@end
