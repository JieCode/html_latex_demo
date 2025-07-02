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

    private SharedPreferences sharedPreferences;

    public LatexPreferenceUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存一个无法识别的latex公式
     * @param latex 公式字符串
     */
    public void saveErrorLatex(String latex) {
        Set<String> latexSet = sharedPreferences.getStringSet(KEY_ERROR_LATEX_SET, new HashSet<>());
        // 由于SharedPreferences的StringSet是引用类型，先复制一份
        Set<String> newSet = new HashSet<>(latexSet);
        newSet.add(latex);
        sharedPreferences.edit().putStringSet(KEY_ERROR_LATEX_SET, newSet).apply();
    }

    /**
     * 获取所有保存的无法识别的latex公式
     * @return 公式集合
     */
    public Set<String> getErrorLatexSet() {
        return sharedPreferences.getStringSet(KEY_ERROR_LATEX_SET, new HashSet<>());
    }

    /**
     * 清空所有保存的latex公式
     */
    public void clearErrorLatex() {
        sharedPreferences.edit().remove(KEY_ERROR_LATEX_SET).apply();
    }
}

