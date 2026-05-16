package com.example.yanghang.clipboard.Fragment.JsonData;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import org.json.JSONObject;

public class RichTextData {
    public static final String DATA_TYPE = "richText";

    private boolean richText;
    private String plainText;
    private String html;

    private RichTextData(boolean richText, String plainText, String html) {
        this.richText = richText;
        this.plainText = plainText == null ? "" : plainText;
        this.html = html == null ? "" : html;
    }

    public static RichTextData parse(String content) {
        if (content == null) {
            return new RichTextData(false, "", "");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("{")) {
            try {
                JSONObject object = new JSONObject(trimmed);
                if (DATA_TYPE.equals(object.optString("dataType")) || object.has("html")) {
                    String html = object.optString("html", "");
                    String plainText = object.optString("plainText", "");
                    if (plainText.equals("") && !html.equals("")) {
                        plainText = fromHtml(html).toString();
                    }
                    return new RichTextData(true, plainText, html);
                }
            } catch (Exception ignored) {
            }
        }
        return new RichTextData(false, content, "");
    }

    public static String toPlainText(String content) {
        return parse(content).getPlainText();
    }

    public static String toStorageString(Spanned text) {
        String plainText = text == null ? "" : text.toString();
        String html = toHtml(text);
        try {
            JSONObject object = new JSONObject();
            object.put("dataType", DATA_TYPE);
            object.put("plainText", plainText);
            object.put("html", html);
            return object.toString();
        } catch (Exception ignored) {
            return plainText;
        }
    }

    public static boolean hasRichSpans(CharSequence text) {
        if (!(text instanceof Spanned)) {
            return false;
        }
        Object[] spans = ((Spanned) text).getSpans(0, text.length(), Object.class);
        for (Object span : spans) {
            if (span instanceof StyleSpan
                    || span instanceof RelativeSizeSpan
                    || span instanceof ForegroundColorSpan
                    || span instanceof LeadingMarginSpan
                    || span instanceof BulletSpan
                    || span instanceof UnderlineSpan
                    || span instanceof StrikethroughSpan) {
                return true;
            }
            if (span instanceof CharacterStyle || span instanceof ParagraphStyle) {
                String name = span.getClass().getName();
                if (!name.startsWith("android.text.")) {
                    return true;
                }
            }
        }
        return false;
    }

    public Spanned toSpanned() {
        if (richText && !html.equals("")) {
            return fromHtml(html);
        }
        return fromHtml(escapeHtml(plainText).replace("\n", "<br>"));
    }

    public boolean isRichText() {
        return richText;
    }

    public String getPlainText() {
        return plainText;
    }

    private static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html == null ? "" : html, Html.FROM_HTML_MODE_LEGACY);
        }
        return Html.fromHtml(html == null ? "" : html);
    }

    private static String toHtml(Spanned text) {
        if (text == null) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.toHtml(text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        }
        return Html.toHtml(text);
    }

    private static String escapeHtml(String text) {
        return text == null ? "" : text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
