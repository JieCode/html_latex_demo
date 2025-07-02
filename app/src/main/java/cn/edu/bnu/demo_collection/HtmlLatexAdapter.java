package cn.edu.bnu.demo_collection;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zanvent.mathview.MathView;

import java.net.URLEncoder;
import java.util.List;

public class HtmlLatexAdapter extends RecyclerView.Adapter<HtmlLatexAdapter.ViewHolder> {
    private final Context context;
    private final List<String> dataList;

    public HtmlLatexAdapter(Context context, List<String> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_html_latex_text, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String html = dataList.get(position);
        // 用Markwon渲染内容
        MarkDownLatexUtil util = new MarkDownLatexUtil(context, new MarkDownLatexUtil.MarkwonLatexListener() {
            @Override
            public void onImageClick(String imageUrl) {
                Toast.makeText(context, "点击图片：" + imageUrl, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLatexError(String errorLatex) {
                holder.textView.setVisibility(View.GONE);

                Log.e("HtmlLatexAdapter", "Latex error: " + errorLatex);

                holder.flWebView.setVisibility(View.VISIBLE);
                // 延迟创建 WebView
                holder.flWebView.post(() -> {
                    // 检查是否已有 WebView
                    holder.flWebView.removeAllViews();
                    try {
                        WebView webview = new WebView(context);
                        // 判断errorLatex是否包含<latex>标签
                        final String finalErrorLatex;
                        if (!errorLatex.contains("<latex>")) {
                            finalErrorLatex = "<latex>" + errorLatex + "</latex>";
                        } else {
                            finalErrorLatex = errorLatex;
                        }
                        WebViewLatexUtil util = new WebViewLatexUtil(context);
                        util.renderContent(webview, html);
                        holder.flWebView.addView(webview);
                    } catch (Exception e) {
                        Log.e("HtmlLatexAdapter", "WebView creation failed", e);
                    }
                });
            }
        });
        holder.textView.setText(""); // 先清空，防止复用残影
        util.renderContent(holder.textView, html);
        holder.textView.requestLayout(); // 强制重新测量
        holder.textView.invalidate();
        holder.textView.setVisibility(View.VISIBLE);
        holder.flWebView.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // 清理 WebView
        if (holder.flWebView.getChildCount() > 0) {
            View child = holder.flWebView.getChildAt(0);
            if (child instanceof WebView) {
                ((WebView) child).destroy();
            }
            holder.flWebView.removeAllViews();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        FrameLayout flWebView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_content);
            flWebView = itemView.findViewById(R.id.fl_web_view);
        }
    }
}