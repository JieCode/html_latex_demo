package cn.edu.bnu.demo_collection;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;

import java.util.List;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.latex.JLatexMathPlugin;
import io.noties.markwon.ext.latex.JLatexMathTheme;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.AsyncDrawable;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.glide.GlideImagesPlugin;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import ru.noties.jlatexmath.JLatexMathDrawable;

/**
 * @CreateDate: 2025/6/11
 * @author: jingjie
 * @desc: 用于Markwon加载不带表格的HTML和Latex的工具类
 */
public class MarkDownLatexUtil {
    private static final String TAG = "MarkDownLateexUtil";
    private static volatile MarkDownLatexUtil instance;
    private Markwon markwon;
    private Context mContext; // Hold context for Markwon builder
    private MarkwonLatexListener listener;

    public MarkDownLatexUtil(Context context, MarkwonLatexListener listener) {
        this.mContext = context;
        this.listener = listener;
        markwon = Markwon.builder(mContext)
                .usePlugin(ImagesPlugin.create())
                .usePlugin(HtmlPlugin.create()) // 保留HtmlPlugin处理<sub>, <sup>, <u>等
                .usePlugin(GlideImagesPlugin.create(new GlideImagesPlugin.GlideStore() {
                    @NonNull
                    @Override
                    public RequestBuilder<Drawable> load(@NonNull AsyncDrawable drawable) {
                        return Glide.with(mContext)
                                .load(drawable.getDestination())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        if (resource != null) {
                                            // 图片大小调整，统一在ImageView中处理
                                            int rawWidth = resource.getIntrinsicWidth();
                                            int rawHeight = resource.getIntrinsicHeight();

                                            // 统一放大2倍（非表格图片的默认大小调整）
                                            float scale = 1.5f; // 根据需要调整缩放比例
                                            rawWidth = (int) (rawWidth * scale);
                                            rawHeight = (int) (rawHeight * scale);

                                            int maxWidth = mContext.getResources().getDisplayMetrics().widthPixels;
                                            // int maxHeight = mContext.getResources().getDisplayMetrics().heightPixels; // 不需要限制高度，防止图片被截断

                                            // 如果超出屏幕宽度，按比例缩放至屏幕宽度的80%
                                            if (rawWidth > maxWidth * 0.8f) {
                                                float ratio = (float) (maxWidth * 0.8f) / rawWidth;
                                                rawWidth = (int) (rawWidth * ratio);
                                                rawHeight = (int) (rawHeight * ratio);
                                            }

                                            resource.setBounds(0, 0, rawWidth, rawHeight);
                                        }
                                        return false;
                                    }
                                });
                    }

                    @Override
                    public void cancel(@NonNull Target<?> target) {
                        Glide.with(mContext).clear(target);
                    }
                }))
                .usePlugin(MarkwonInlineParserPlugin.create())
                .usePlugin(JLatexMathPlugin.create(36f, builder -> {
                    builder.inlinesEnabled(true);
                    builder.blocksLegacy(true);
                    builder.blocksEnabled(true);
                    builder.theme().blockPadding(JLatexMathTheme.Padding.symmetric(24, 24));
                    builder.theme().inlinePadding(JLatexMathTheme.Padding.symmetric(8, 8));
                    builder.theme().blockHorizontalAlignment(JLatexMathDrawable.ALIGN_LEFT);
                    builder.theme().textColor(Color.BLACK);
                    builder.errorHandler((latex, error) -> {
                        Log.e(TAG, "Error rendering LaTeX: " + error.getMessage());
                        if (listener != null) {
                            listener.onLatexError(latex);
                        }
                        return null;
                    });
                }))
                .build();
    }

    /**
     * 渲染不带表格的HTML内容，将结果设置到传入的TextView。
     *
     * @param textView 目标TextView
     * @param html     原始HTML字符串
     */
    public void renderContent(TextView textView, String html) {
//        Log.e(TAG, "Original HTML content (Markwon): " + html);

        // 2. 将 LaTeX 公式的 $ ... $ 和 $$ ... $$ 转成 Markwon 支持的 $...$ 和 $$...$$
        String processedHtml = html
                .replaceAll("\\\\\\((.+?)\\\\\\)", "<latex>$1</latex>")
                .replaceAll("\\\\\\[(.+?)\\\\\\]", "<latex>$1</latex>");


//        Log.e(TAG, "Processed HTML content (Markwon): " + processedHtml);

        // 3. 转换成 Markdown
        String markdown = convertHtmlToMarkdown(processedHtml);
//        markdown = markdown
//                .replaceAll("\\\\\\((.+?)\\\\\\)", "\\$$1\\$")
//                .replaceAll("\\\\\\[(.+?)\\\\\\]", "\\$\\$$1\\$\\$");
//        Log.e(TAG, "Markdown content (Markwon): " + markdown);

        // 1. HTML实体解码，将 &lt; &gt; 等转成对应字符
        String decodedHtml = Parser.unescapeEntities(markdown, false);
//        Log.e(TAG, "DecodedHtml HTML content (Markwon): " + decodedHtml);

        // 4. 设置 Markdown 内容
        markwon.setMarkdown(textView, markdown);

        // 5. 为图片添加点击事件
        CharSequence text = textView.getText();
        if (text instanceof android.text.Spannable) {
            android.text.Spannable spannable = (android.text.Spannable) text;
            Object[] spans = spannable.getSpans(0, spannable.length(), Object.class);
            for (Object span : spans) {
                if (span instanceof io.noties.markwon.image.AsyncDrawableSpan) {
                    final io.noties.markwon.image.AsyncDrawableSpan imageSpan = (io.noties.markwon.image.AsyncDrawableSpan) span;
                    final String imageUrl = imageSpan.getDrawable().getDestination();
                    int start = spannable.getSpanStart(span);
                    int end = spannable.getSpanEnd(span);
                    android.text.style.ClickableSpan clickableSpan = new android.text.style.ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (listener != null) {
                                listener.onImageClick(imageUrl);
                            }
                        }
                    };
                    spannable.setSpan(clickableSpan, start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
        // 确保TextView可点击span
        textView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        // 打印分割线
//        Log.e(TAG, "renderContent: ------------------------------------------------------------------------");
    }

    private String convertHtmlToMarkdown(String html) {
        Document doc = Jsoup.parse(html);
        StringBuilder markdown = new StringBuilder();
        List<Node> nodes = doc.body().childNodes();

        for (Node node : nodes) {
            markdown.append(processNode(node, false)); // inTableCell=false
        }
        return markdown.toString();
    }

    // 调整processNode，使<sub>, <sup>, <u>始终输出HTML标签，因为HtmlPlugin会处理
    private String processNode(Node node, boolean inTableCell) {
        StringBuilder sb = new StringBuilder();
        if (node instanceof TextNode) {
//            String text = ((TextNode) node).text().replaceAll("&nbsp;", " ");
            sb.append(((TextNode) node).text());
        } else if (node instanceof Element) {
            Element el = (Element) node;
            switch (el.tagName()) {
                case "img":
                    String src = el.attr("src");
                    String alt = el.attr("alt");
                    if (TextUtils.isEmpty(alt)) {
                        alt = "图片";
                    }
                    // 如果src不是以http://或https://开头，补全为http://
                    if (!src.startsWith("http://") && !src.startsWith("https://")) {
                        if (src.startsWith("//")) {
                            src = "http:" + src;
                        } else {
                            src = "http://" + src;
                        }
                    }
                    sb.append(" ![").append(alt).append("](").append(src).append(") ");
                    break;
                case "i":
                    sb.append("*").append(el.text()).append("*");
                    break;
                case "sup":
                    // 始终输出HTML标签，由HtmlPlugin处理
                    sb.append("<sup>");
                    for (Node child : el.childNodes()) {
                        sb.append(processNode(child, inTableCell));
                    }
                    sb.append("</sup>");
                    break;
                case "sub":
                    // 始终输出HTML标签，由HtmlPlugin处理
                    sb.append("<sub>");
                    for (Node child : el.childNodes()) {
                        sb.append(processNode(child, inTableCell));
                    }
                    sb.append("</sub>");
                    break;
                case "u":
                    // 始终输出HTML标签，由HtmlPlugin处理
                    sb.append("<u>");
                    for (Node child : el.childNodes()) {
                        sb.append(processNode(child, inTableCell));
                    }
                    sb.append("</u>");
                    break;
                case "latex":
                    String latexContent = el.text();
                    // Remove any existing LaTeX delimiters
                    latexContent = latexContent.replaceAll("^\\\\\\(|\\\\\\)$", "")
                            .replaceAll("^\\\\\\[|\\\\\\]$", "");

                    // For inline formulas, use $...$ without extra spaces
                    if (latexContent.length() > 100 || latexContent.contains("\\begin")) {
                        // For block formulas, use $$...$$, 前后多加一个换行
                        sb.append("\n$$").append(latexContent).append("$$\n\n");
                    } else {
                        // For inline formulas, use $...$ without extra spaces
                        sb.append("$$").append(latexContent).append("$$");
                    }
                    break;
                case "p":
                    // 在Markwon方案中，p标签外统一加换行
                    sb.append("\n");
                    for (Node child : el.childNodes()) {
                        sb.append(processNode(child, false)); // p标签内不再传递inTableCell
                    }
                    sb.append("\n\n");
                    break;
                // REMOVE table related cases for MarkDownLatexUtil
                // case "table": ...
                // case "tr": ...
                // case "th": ...
                // case "td": ...
                default:
                    // 处理其他未明确处理的HTML标签，递归其子节点
                    for (Node child : el.childNodes()) {
                        sb.append(processNode(child, inTableCell));
                    }
            }
        }
        return sb.toString();
    }

    public interface MarkwonLatexListener {
        void onImageClick(String url);

        void onLatexError(String errorLatex);
    }
}