package com.example.yanghang.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.yanghang.myapplication.FileUtils.FileUtils;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.myapplication.OthersView.PerformEdit;

import java.util.List;

public class ActivityEditInfo extends AppCompatActivity {
    public static int RESULT_ADD_NEW = 345;
    public static int RESULT_NOTHING_NEW = 678;
    EditText tvShowInfo;
    PerformEdit mPerformEdit;

    EditText editRemark;
    Spinner spinner;
    List<String> mCatalogue;
    private ListData listData;
    private int pos;
    private ArrayAdapter<String> arr_adapter;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        Initial();
    }

    void Initial() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.edit_toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Bundle bundle = getIntent().getExtras();
        listData = (ListData) bundle.get(MainFormActivity.LIST_DATA);
        pos = (int) bundle.get(MainFormActivity.LIST_DATA_POS);
        if (pos == -1)
            isEdit = true;
        else
            isEdit = false;

        tvShowInfo = (EditText) findViewById(R.id.tv_ShowInfo);

        editRemark = (EditText) findViewById(R.id.edit_remark);
        tvShowInfo.setText(listData.getContent());
        editRemark.setText(listData.getRemarks());
        editRemark.setFocusable(isEdit);
        tvShowInfo.setFocusable(isEdit);
        spinner = (Spinner) findViewById(R.id.catalogue_spinner);
        mCatalogue = getCatalogue();

        arr_adapter = new ArrayAdapter<String>(this, R.layout.item_catalogue, R.id.item_catalogue_tv, mCatalogue);
        //设置样式
        arr_adapter.setDropDownViewResource(R.layout.item_catalogue_pop);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        String catalogue = listData.getCatalogue();
        int index = mCatalogue.indexOf(catalogue);
        if (index != -1)
            spinner.setSelection(index, true);
        else spinner.setSelection(0, true);

        spinner.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        mPerformEdit = new PerformEdit(tvShowInfo) {
            @Override
            protected void onTextChanged(Editable s) {
                //文本发生改变,可以是用户输入或者是EditText.setText触发.(setDefaultText的时候不会回调)
                super.onTextChanged(s);
            }
        };
        tvShowInfo.requestFocus();

        tvShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.v(MainFormActivity.MTTAG, "EditInfo Activity EditText click");
                tvShowInfo.requestFocus();
                InputMethodManager imm = (InputMethodManager) ActivityEditInfo.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(tvShowInfo, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_checked:
                listData.setContent(tvShowInfo.getText().toString());
                listData.setRemarks(editRemark.getText().toString());
                listData.setCatalogue(spinner.getSelectedItem().toString());
                Intent intent = new Intent(ActivityEditInfo.this, MainFormActivity.class);
                intent.putExtra(MainFormActivity.LIST_DATA, listData);
                intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
                if (pos == -1) {
                    if (listData.getContent().equals("")) {
                        setResult(RESULT_NOTHING_NEW, intent);
                    } else
                        setResult(RESULT_ADD_NEW, intent);
                } else
                    setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.menu_redo:
                mPerformEdit.redo();
                break;
            case R.id.menu_undo:
                mPerformEdit.undo();
                break;
            case R.id.menu_editable:
                isEdit = true;
                editRemark.setFocusable(true);
                editRemark.requestFocus();
                editRemark.setFocusableInTouchMode(true);
                tvShowInfo.setFocusable(true);
                tvShowInfo.setFocusableInTouchMode(true);
                spinner.setVisibility(View.VISIBLE);
                tvShowInfo.requestFocus();
                invalidateOptionsMenu();
        }
        return true;
    }

    public List<String> getCatalogue() {
        return FileUtils.loadCatalogue(getApplicationContext().getFilesDir().getAbsolutePath());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_redo).setVisible(isEdit);
        menu.findItem(R.id.menu_undo).setVisible(isEdit);
        menu.findItem(R.id.menu_checked).setVisible(isEdit);
        menu.findItem(R.id.menu_editable).setVisible(!isEdit);
        return super.onPrepareOptionsMenu(menu);
    }
}
