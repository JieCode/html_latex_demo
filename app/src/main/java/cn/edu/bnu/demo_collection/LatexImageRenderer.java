package cn.edu.bnu.demo_collection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @CreateDate: 2025/7/2
 * @author: jingjie
 * @desc:
 */
public class LatexImageRenderer {

    private static final String LATEX_RENDER_URL = "https://latex.codecogs.com/png.latex?\\dpi{300}";

    public static void renderLatexToWebView(Context context, WebView webView, String latexCode) {
        try {
            // 对LaTeX进行URL编码
            String encodedLatex = URLEncoder.encode(latexCode, "UTF-8")
                    .replace("+", "%20")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%5B", "[")
                    .replace("%5D", "]");

            // 构建图片URL
            String imageUrl = LATEX_RENDER_URL + encodedLatex;

            // 在WebView中显示图片
            String html = "<html><body style='margin:0;padding:0;'>" +
                    "<img src='" + imageUrl + "' style='max-width:100%;'/>" +
                    "</body></html>";

            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        } catch (UnsupportedEncodingException e) {
            Toast.makeText(context, "LaTeX渲染失败", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // 可选：将Bitmap转为Base64内嵌图片（完全离线）
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
