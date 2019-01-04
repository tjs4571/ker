//
//  app.h
//  KK
//
//  Created by zhanghailong on 2018/10/29.
//  Copyright © 2018年 kkmofang.cn. All rights reserved.
//

#ifndef ui_app_h
#define ui_app_h

#include <ui/ui.h>
#include <ui/view.h>

namespace kk {
    
    namespace ui {
        
        class Package;
        class Page;
        
        
        class AppBackCommand : public Command {
        public:
            kk::Uint delta;
            kk::Boolean animated;
        };
        
        class App : public Context {
        public:
            
            App(kk::Uint64 appid,kk::CString basePath,kk::CString platform,kk::CString userAgent,kk::CString appkey);
            
            App(kk::Uint64 appid,kk::CString basePath,kk::CString appkey);
            
            virtual ~App();
            
            virtual void open(kk::CString uri,kk::Boolean animated);
            
            virtual void back(kk::Uint delta,kk::Boolean animated);
            
            virtual void run(kk::Object * query);
            
            virtual kk::Strong<View> createView(kk::CString name,ViewConfiguration * configuration);
            
            virtual kk::Strong<View> getView(kk::Uint64 viewId);
            
            virtual kk::Strong<View> createView(kk::Native * native,Rect & frame);
            
            virtual void removeView(kk::Uint64 viewId);
            
            virtual kk::Strong<Canvas> createCanvas(View * view,DispatchQueue * queue);
            
            virtual void removeCanvas(kk::Uint64 canvasId);
            
            virtual kk::Strong<Page> createPage(kk::CString type);
            
            virtual kk::Strong<Page> getPage(kk::Uint64 pageId);
            
            virtual void removePage(kk::Uint64 pageId);
            
            virtual Size getAttributedTextContentSize(AttributedText * text,Float maxWidth);
            
            virtual kk::CString appkey();
            
            virtual kk::Uint64 appid();
            
            virtual void execCommand(Command * command);
            
            virtual void dispatchCommand(Command * command);
            
            virtual void openlib();
            
            static void Openlib();
            
            Ker_CLASS(App,Context,"UIApp")
    
        protected:
            kk::String _appkey;
            std::map<kk::Uint64,kk::Weak<View>> _views;
            std::map<kk::Uint64,kk::Weak<Canvas>> _canvas;
            std::map<kk::Uint64,kk::Weak<Page>> _pages;
            kk::Uint64 _appid;
        };
        
        
        void addAppOpenlib(std::function<void(duk_context *,App *)> && func);
        
    }
}


#endif /* app_h */
