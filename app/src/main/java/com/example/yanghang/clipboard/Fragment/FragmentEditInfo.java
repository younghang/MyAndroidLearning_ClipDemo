package com.example.yanghang.clipboard.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.KeyListener;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;

import com.example.yanghang.clipboard.Fragment.JsonData.RichTextData;
import com.example.yanghang.clipboard.OthersView.PerformEdit;
import com.example.yanghang.clipboard.R;

import java.util.Stack;

public class FragmentEditInfo extends FragmentEditAbstract {
    private static final String ARG_RICH_TEXT = "richText";

    private EditText editInfo;
    private PerformEdit mPerformEdit;
    private View mView;
    private NestedScrollView editInfoScroll;
    private HorizontalScrollView richTextToolbar;
    private KeyListener keyListener = null;
    private int inputType = 0;
    private boolean isRichTextContent = false;
    private boolean richTextEnabled = false;
    private Stack<CharSequence> richHistory = new Stack<CharSequence>();
    private Stack<CharSequence> richHistoryBack = new Stack<CharSequence>();

    public static FragmentEditInfo newInstance(String information, boolean isEdit) {
        return newInstance(information, isEdit, false);
    }

    public static FragmentEditInfo newInstance(String information, boolean isEdit, boolean richTextEnabled) {
        FragmentEditInfo fragment = new FragmentEditInfo();
        newInstance(fragment, information, isEdit);
        fragment.getArguments().putBoolean(ARG_RICH_TEXT, richTextEnabled);
        return fragment;
    }

    public FragmentEditInfo() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onICreate();
        if (getArguments() != null) {
            richTextEnabled = getArguments().getBoolean(ARG_RICH_TEXT, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_edit_info, null);
        initView();
        return mView;
    }

    private void initView() {
        editInfo = (EditText) mView.findViewById(R.id.tv_ShowInfo);
        editInfoScroll = (NestedScrollView) mView.findViewById(R.id.edit_info_scroll);
        richTextToolbar = (HorizontalScrollView) getActivity().findViewById(R.id.rich_text_toolbar);

        if (richTextEnabled) {
            RichTextData richTextData = RichTextData.parse(infoEdit);
            isRichTextContent = richTextData.isRichText();
            editInfo.setText(richTextData.toSpanned());
        } else {
            editInfo.setText(infoEdit);
        }
        bindRichTextToolbar();

        mPerformEdit = new PerformEdit(editInfo) {
            @Override
            protected void onTextChanged(Editable s) {
                super.onTextChanged(s);
            }
        };

        if (!isEdit) {
            keyListener = editInfo.getKeyListener();
            inputType = editInfo.getInputType();
            editInfo.setKeyListener(null);
            setRichToolbarVisible(false);
        } else {
            editInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showKeyboard();
                }
            });
            editInfo.requestFocus();
            setRichToolbarVisible(richTextEnabled);
        }
    }

    private void bindRichTextToolbar() {
        bindButton(R.id.rich_bold, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyInlineSpan(new StyleSpan(Typeface.BOLD));
            }
        });
        bindButton(R.id.rich_italic, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyInlineSpan(new StyleSpan(Typeface.ITALIC));
            }
        });
        bindButton(R.id.rich_title, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyParagraphSpan(new StyleSpan(Typeface.BOLD), new RelativeSizeSpan(1.25f));
            }
        });
        bindButton(R.id.rich_quote, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyParagraphSpan(new LeadingMarginSpan.Standard(dp(12), dp(12)),
                        new ForegroundColorSpan(Color.rgb(96, 96, 96)));
            }
        });
        bindButton(R.id.rich_bullet, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyParagraphSpan(new BulletSpan(dp(10)));
            }
        });
        bindButton(R.id.rich_clear, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllFormat();
            }
        });
        bindButton(R.id.rich_clear_line, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCurrentFormat();
            }
        });
    }

    private void bindButton(int id, View.OnClickListener listener) {
        View button = getActivity() == null ? null : getActivity().findViewById(id);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }

    private void applyInlineSpan(Object span) {
        int[] range = getSelectionOrWordRange();
        if (range[0] == range[1]) {
            Toast.makeText(getActivity(), "先选中文字", Toast.LENGTH_SHORT).show();
            return;
        }
        recordRichHistory();
        editInfo.getText().setSpan(span, range[0], range[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        isRichTextContent = true;
    }

    private void applyParagraphSpan(Object... spans) {
        int[] range = getParagraphRange();
        if (range[0] == range[1]) {
            Toast.makeText(getActivity(), "先输入内容", Toast.LENGTH_SHORT).show();
            return;
        }
        recordRichHistory();
        Editable editable = editInfo.getText();
        for (Object span : spans) {
            editable.setSpan(span, range[0], range[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        isRichTextContent = true;
    }

    private void clearCurrentFormat() {
        int[] range = editInfo.getSelectionStart() == editInfo.getSelectionEnd()
                ? getParagraphRange()
                : getSelectionOrWordRange();
        if (range[0] == range[1]) {
            return;
        }
        recordRichHistory();
        clearSpansInRange(range[0], range[1]);
        isRichTextContent = RichTextData.hasRichSpans(editInfo.getText());
    }

    private void clearAllFormat() {
        recordRichHistory();
        int start = editInfo.getSelectionStart();
        int end = editInfo.getSelectionEnd();
        editInfo.setText(editInfo.getText().toString());
        int length = editInfo.getText().length();
        editInfo.setSelection(clamp(start, 0, length), clamp(end, 0, length));
        isRichTextContent = false;
    }

    private void clearSpansInRange(int start, int end) {
        Editable editable = editInfo.getText();
        Object[] spans = editable.getSpans(start, end, Object.class);
        for (Object span : spans) {
            if (span instanceof CharacterStyle || span instanceof ParagraphStyle) {
                editable.removeSpan(span);
            }
        }
    }

    private void recordRichHistory() {
        if (!richTextEnabled) {
            return;
        }
        richHistory.push(new SpannableStringBuilder(editInfo.getText()));
        richHistoryBack.clear();
    }

    private int[] getSelectionOrWordRange() {
        Editable editable = editInfo.getText();
        int start = Math.max(0, editInfo.getSelectionStart());
        int end = Math.max(0, editInfo.getSelectionEnd());
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        if (start != end) {
            return new int[]{start, end};
        }
        while (start > 0 && !Character.isWhitespace(editable.charAt(start - 1))) {
            start--;
        }
        while (end < editable.length() && !Character.isWhitespace(editable.charAt(end))) {
            end++;
        }
        return new int[]{start, end};
    }

    private int[] getParagraphRange() {
        Editable editable = editInfo.getText();
        int start = Math.max(0, editInfo.getSelectionStart());
        int end = Math.max(0, editInfo.getSelectionEnd());
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        while (start > 0 && editable.charAt(start - 1) != '\n') {
            start--;
        }
        while (end < editable.length() && editable.charAt(end) != '\n') {
            end++;
        }
        return new int[]{start, end};
    }

    public void undo() {
        if (richTextEnabled && !richHistory.empty()) {
            richHistoryBack.push(new SpannableStringBuilder(editInfo.getText()));
            restoreRichSnapshot(richHistory.pop());
            return;
        }
        mPerformEdit.undo();
    }

    public void redo() {
        if (richTextEnabled && !richHistoryBack.empty()) {
            richHistory.push(new SpannableStringBuilder(editInfo.getText()));
            restoreRichSnapshot(richHistoryBack.pop());
            return;
        }
        mPerformEdit.redo();
    }

    private void restoreRichSnapshot(CharSequence snapshot) {
        int selection = editInfo.getSelectionStart();
        mPerformEdit.setDefaultText(snapshot);
        int length = editInfo.getText().length();
        editInfo.setSelection(clamp(selection, 0, length));
        isRichTextContent = RichTextData.hasRichSpans(editInfo.getText());
    }

    public String getString() {
        Editable editable = editInfo.getText();
        if (richTextEnabled && (RichTextData.hasRichSpans(editable) || isRichTextContent)) {
            return RichTextData.toStorageString((Spanned) editable);
        }
        return editable.toString();
    }

    public void enableEdit() {
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });
        editInfo.requestFocus();
        editInfo.setKeyListener(keyListener);
        editInfo.setInputType(inputType);
        setRichToolbarVisible(richTextEnabled);
    }

    private void showKeyboard() {
        editInfo.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editInfo, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setRichToolbarVisible(boolean visible) {
        if (richTextToolbar != null) {
            richTextToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                richTextToolbar.bringToFront();
                richTextToolbar.post(new Runnable() {
                    @Override
                    public void run() {
                        updateScrollBottomPadding(true);
                    }
                });
            }
        }
        updateScrollBottomPadding(visible);
    }

    private void updateScrollBottomPadding(boolean visible) {
        if (editInfoScroll != null) {
            int bottomPadding = visible ? Math.max(dp(58), richTextToolbar == null ? 0 : richTextToolbar.getHeight()) : 0;
            editInfoScroll.setClipToPadding(false);
            editInfoScroll.setPadding(
                    editInfoScroll.getPaddingLeft(),
                    editInfoScroll.getPaddingTop(),
                    editInfoScroll.getPaddingRight(),
                    bottomPadding);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null) {
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
        }
    }
}
