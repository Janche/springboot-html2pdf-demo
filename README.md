# Java通过Html(freemarker模板)生成PDF实战, 可支持商用

## 技术架构
***springboot + freemarker + [pdfbox] + flying-saucer-pdf***

### 生成流程：

1. freemarker: 根据数据填充ftl模板文件，得到包含有效数据的html文件（包含页眉页脚页码的处理，和解决中文渲染等问题）。
2. flying-saucer-pdf: 将html转换成PDF文件。
3. pdfbox: 操作PDF文件，完成加解密等操作。

#### 注意事项：
1. 图片需要通过base64的方式加载，直接加载图片路径可能无法渲染
2. 字体名称需要和Java代码中加载的字体名称保持一致，中文无法渲染可能是没有设置别名
3. 替换的变量，如果有null值需要在模板中判断
4. 如果填充的变量中存在特殊字符，通过<![CDATA[${变量名}]]> 方式设置
5. 部分高级的CSS样式或者标签可能不支持
6. 页眉页脚采用running的方式处理

#### 内含字体文件：
1. PingFang-Regular.ttf 中文字体
2. Poppins-Medium.ttf 英文字体

**博客链接：https://blog.csdn.net/qq_34997906/article/details/139214502**