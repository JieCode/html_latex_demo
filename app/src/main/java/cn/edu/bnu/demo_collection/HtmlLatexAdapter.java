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

import java.util.List;
import java.util.Set;

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
        holder.textView.setVisibility(View.VISIBLE);
        holder.flWebView.setVisibility(View.GONE);

        if (LatexPreferenceUtil.getInstance().needWebView(context, html)) {
            Log.e("HtmlLatexAdapter", "Need WebView: " + html);
            // 直接用WebView展示
            webViewLatex(holder, html);
        } else {
            Log.e("HtmlLatexAdapter", "MarkDownLatexUtil: " + html);
            // 用Markwon渲染内容
            MarkDownLatexUtil util = new MarkDownLatexUtil(context, new MarkDownLatexUtil.MarkwonLatexListener() {
                @Override
                public void onImageClick(String imageUrl) {
                    Toast.makeText(context, "点击图片：" + imageUrl, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLatexError(String errorLatex) {
                    // 保存错误latex
                    webViewLatex(holder, html);
                }
            });
            holder.textView.setText(""); // 先清空，防止复用残影
            util.renderContent(holder.textView, html);
            holder.textView.requestLayout(); // 强制重新测量
            holder.textView.invalidate();
        }
    }

    /**
     * 使用 WebView 渲染 latex 公式
     *
     * @param holder
     * @param html
     */
    private void webViewLatex(@NonNull ViewHolder holder, String html) {
        holder.textView.setVisibility(View.GONE);
        holder.flWebView.setVisibility(View.VISIBLE);
        holder.flWebView.post(() -> {
            holder.flWebView.removeAllViews();
            try {
                WebView webview = new WebView(context);
                WebViewLatexUtil util = new WebViewLatexUtil(context, new WebViewLatexUtil.WebViewListener() {
                    @Override
                    public void onImageClick(String url) {
                        Toast.makeText(context, "点击图片：" + url, Toast.LENGTH_SHORT).show();
                    }
                });
                util.renderContent(webview, html);
                holder.flWebView.addView(webview);
            } catch (Exception e) {
                Log.e("HtmlLatexAdapter", "WebView creation failed", e);
            }
        });
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