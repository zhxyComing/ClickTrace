# ClickTrace - AOP

Android针对点击事件的无痕埋点插件。

## 简介
通过引入ClickTrace，可以给指定Activity或全部Activity的所有可点击View设置一个统一回调，而无须修改现有代码。
通常用于日志、无痕埋点上报。

## 配置

#### 配置流程

1.在根目录的build.gradle里如下配置。

```Java
buildscript {
  repositories {
    //配置1
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    //配置2
    classpath "gradle.plugin.com.dixon.clicktrace:clicktrace:1.0.0"
  }
}
```

2.在主module的build.gradle里引用插件。
```Java
apply plugin: "com.dixon.clicktrace"
```

3.根据你的项目需求，配置可点击回调的页面、以及回调的函数。
```Java
clickconfig {
    basepath 'com.app.dixon.testgradleclicktrace.MainActivity'
    tracepath 'com.app.dixon.testgradleclicktrace.ClickTrace', 'log'
}
```
**配置监听页面**
如上，假如你只想监听单个Activity的所有点击事件，请在`basepath`如上配置该Activity的全路径；你可以使用`basepaths`来配置多个页面；如果你想监听app内的所有Activity，请配置你的`basepath`为你的BaseActivity。

**配置回调函数**
你可以任意创建一个类，并手动命名函数作为你的回调函数。
但是该函数要求有如下参数：
```Java
//示例
public class ClickTrace {

    public static void log(View view, int eventType, String path, String content, int position) {
    
    }
}
```
最后，使用clickconfig里的tracepath来配置你的回调函数，第一个参数是你创建的回调类的全路径，第二个参数是回调的方法。

至此配置完毕。

如果配置正确的话，当你点击任意View，只要该View设置了点击事件，均会在点击事件执行完毕后执行你的回调函数。

#### 回调函数参数解释

参数|解释
---|---
view|你点击的组件。你可以通过`view.getClass().getSimpleName()`获取名称，或通过`view.getId()`获取id（动态添加的view id为-1）。
eventType|事件类型。clicktrace不仅仅会监听点击，还包括滑动、长按等AccessibilityDelegate支持的事件，如果你仅对点击事件感兴趣，可以通过判断`eventType==AccessibilityEvent.TYPE_VIEW_CLICKED`或`eventType==1`来进行筛选。（**强烈建议筛选**）
path|该view的组件树路径。如：DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ConstraintLayout/AppCompatTextView
content|如果该组件继承自TextView，content会返回文字内容。
position|如果你点击的是ListView中的某一个item，它将返回该item的position。

#### 已知问题
ListView可能在某些情况存在漏点问题（尽量不要通过setItemOnClickListener给每个item设置点击事件）；
