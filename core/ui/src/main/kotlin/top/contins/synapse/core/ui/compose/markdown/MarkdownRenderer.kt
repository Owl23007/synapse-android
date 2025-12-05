package top.contins.synapse.core.ui.compose.markdown

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.Color
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

@Composable
fun MarkdownRenderer(markdown: String) {
    val flavour = CommonMarkFlavourDescriptor()
    val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(markdown)
    val rawHtml = HtmlGenerator(markdown, parsedTree, flavour).generateHtml()

    // 注入自定义 CSS 样式
    val styledHtml = """
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body {
                    font-family: sans-serif;
                    margin: 16px;
                    color: #333333;
                    line-height: 1.6;
                    font-size: 16px;
                }
                h1 {
                    font-size: 18px;       /* 原来太大，现在缩小 */
                    color: #d32f2f;
                    margin: 24px 0 12px 0;
                    font-weight: bold;
                }
                h2 {
                    font-size: 16px;       /* 缩小 */
                    color: #1976d2;
                    margin: 20px 0 10px 0;
                    font-weight: 600;
                }
                h3 {
                    font-size: 14px;       /* 缩小 */
                    color: #388e3c;
                    margin: 16px 0 8px 0;
                }
                p {
                    margin: 8px 0;
                }
                ul, ol {
                    margin: 8px 0 8px 20px;
                }
                code {
                    background-color: #f0f0f0;
                    padding: 2px 4px;
                    border-radius: 4px;
                    font-family: monospace;
                    font-size: 14px;
                }
                pre {
                    background-color: #f5f5f5;
                    padding: 12px;
                    border-radius: 8px;
                    overflow-x: auto;
                    font-family: monospace;
                    font-size: 14px;
                    margin: 12px 0;
                }
                a {
                    color: #1976d2;
                    text-decoration: none;
                }
                a:hover {
                    text-decoration: underline;
                }
            </style>
        </head>
        <body>
            $rawHtml
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.defaultTextEncodingName = "utf-8"
                // 禁用缩放、滚动条等
                isVerticalScrollBarEnabled = false
                webViewClient = WebViewClient() // 避免跳转浏览器
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", null)
        }
    )
}
