# HTML + LaTeX 公式渲染 Demo

本项目是一个 Android 应用示例，用于展示如何智能解析并渲染富文本中包含的 LaTeX 数学公式和 SVG 图片。适用于从接口获取 HTML 内容并需要渲染数学公式的场景。

## 📦 项目结构

- **app/**: 主模块，包含所有 Android 相关代码。
- **gradle.properties**: Gradle 构建配置。
- **build.gradle**: 项目构建脚本。
- **gradlew.bat**: Windows 下的 Gradle 启动脚本。

## 🔧 使用的技术

- [Markwon](https://noties.io/Markwon/): 轻量级 Markdown 渲染库，支持部分 LaTeX 公式。
- WebView: 对于 Markwon 无法解析的复杂 LaTeX 公式或 SVG 图像，使用 WebView 进行渲染。
- 缓存机制：记录无法解析的 LaTeX 公式，下次直接使用 WebView 显示，避免重复判断。

## 🧪 功能说明

- 接口返回的数据中可能包含以下内容：
    - 简单 LaTeX 公式（如 `$f(x) = \sqrt{x}$`） → 使用 `TextView` + Markwon 渲染。
    - 复杂 LaTeX 公式（如化学方程式、多行公式等） → 自动切换到 [WebView](file://android\webkit\WebView.java#L48-L311) 渲染。
    - SVG 图片 → 使用 [WebView](file://android\webkit\WebView.java#L48-L311) 加载本地或远程 SVG。
- 支持缓存已知无法解析的公式，提高性能。

## 🚀 如何运行

1. 确保你已安装 Android Studio。
2. 打开项目并同步 Gradle。
3. 连接设备或使用模拟器。
4. 点击 Run 按钮运行应用。

## 📄 License

MIT License
