# Bean Invoker
Invoke spring bean method conveniently.

## How to Use
- Enable bean invoker in `ToolsMenu`.
- Need give a `public method with no parameter` in spring bean. On the method, you can invoke other methods as you want.
- Shortcut: right click on current method name, and find `Fast Invoke`. Default shortcut key is <kbd>option/alt x</kbd>.
- Set port : not required usually. If you get trouble with windows dynamic port, try [default-dynamic-port-range-tcpip](https://learn.microsoft.com/en-US/troubleshoot/windows-server/networking/default-dynamic-port-range-tcpip-chang)  

=======================

## 使用方式
- 在`ToolsMenu`菜单或设置中启用
- 需指定**无参public方法**，支持自动生成。此方法中，可实现其他有参函数调用
- 入口：当前方法名称上右键，找到`快速调用`，默认快捷键<kbd>option/alt x</kbd>
- 指定端口：通常不需要指定，如果遇到windows动态端口占用，可参考 [TCP/IP默认动态端口范围](https://learn.microsoft.com/zh-CN/troubleshoot/windows-server/networking/default-dynamic-port-range-tcpip-chang)