package cn.edu.bnu.demo_collection;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * @CreateDate: 2025/7/2
 * @author: jingjie
 * @desc:
 */
public class LatexPreferenceUtil {
    private static final String PREF_NAME = "latex_prefs";
    private static final String KEY_ERROR_LATEX_SET = "error_latex_set";

    private static LatexPreferenceUtil instance;

    public static LatexPreferenceUtil getInstance() {
        if (instance == null){
            synchronized (LatexPreferenceUtil.class) {
                if (instance == null) {
                    instance = new LatexPreferenceUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 保存一个无法识别的latex公式
     *
     * @param latex 公式字符串
     */
    public void saveErrorLatex(Context context, String latex) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> latexSet = sharedPreferences.getStringSet(KEY_ERROR_LATEX_SET, new HashSet<>());
        // 由于SharedPreferences的StringSet是引用类型，先复制一份
        Set<String> newSet = new HashSet<>(latexSet);
        newSet.add(latex);
        sharedPreferences.edit().putStringSet(KEY_ERROR_LATEX_SET, newSet).apply();
    }

    /**
     * 获取所有保存的无法识别的latex公式
     *
     * @return 公式集合
     */
    public Set<String> getErrorLatexSet(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(KEY_ERROR_LATEX_SET, new HashSet<>());
    }

    /**
     * 清空所有保存的latex公式
     */
    public  void clearErrorLatex(Context  context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(KEY_ERROR_LATEX_SET).apply();
    }

    /**
     * 判断是否需要使用WebView来显示html
     *
     * @param html
     * @return
     */
    public boolean needWebView(Context context, String html) {
        Set<String> errorLatexSet = getErrorLatexSet(context);

        boolean needWebView = false;

        boolean containsLatexTag = html.contains("<latex>");
        boolean containsInlineLatex = html.contains("\\(");
        boolean containsSvg = html.contains("<svg") || html.contains("</svg>") ||
                html.toLowerCase().matches(".*<img[^>]*src=['\"].*.svg['\"].*>.*") || // 匹配 .svg 结尾的图片
                html.toLowerCase().matches(".*<img[^>]*src=['\"].*data:image/svg\\+xml.*>.*"); // 检查是否包含 SVG

        if (containsSvg) {
            needWebView = true;
        } else if (containsLatexTag || containsInlineLatex) {
            // 检查html中是否包含保存的错误latex公式
            for (String errorLatex : errorLatexSet) {
                if (html.contains(errorLatex)) {
                    needWebView = true;
                    break;
                }
            }
        }
        return needWebView;
    }
}

