# LZMA

本项目是针对[7z的LZMA JavaSDK](http://www.7-zip.org/sdk.html)做的改进。

在7z官方的LZMA java sdk中，只示例了简单文件的压缩。我基于此，完善了encode和decode，将其输入参数由文件流，变成了Bytes流，提供了api来压缩某个bytes数组（尽管需要在外部将byte[]包装成ByteArrayInputStream），并将结果保存到引用的参数ByteArrayOutputStream里。

另外一个[LZMA example项目](https://git.oschina.net/findspace/LZMAExample)提供了简单的示例。