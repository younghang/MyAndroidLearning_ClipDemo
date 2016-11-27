package com.example.yanghang.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.yanghang.myapplication.OthersView.PerformEdit;
import com.example.yanghang.myapplication.ListPackage.ListData;

public class ActivityEditInfo extends AppCompatActivity {
    public static int RESULT_ADD_NEW = 345;
    public static int RESULT_NOTHING_NEW = 678;
    EditText tvShowInfo;
    PerformEdit mPerformEdit;
    ImageButton btnRedo;
    ImageButton btnUndo;
    ImageButton btnChecked;
    ImageButton btnCancle;
    EditText editRemark;
    private ListData listData;
    private int pos;

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
        tvShowInfo = (EditText) findViewById(R.id.tv_ShowInfo);

        editRemark = (EditText) findViewById(R.id.edit_remark);
        tvShowInfo.setText(listData.getInformation());
        editRemark.setText(listData.getRemarks());



        mPerformEdit = new PerformEdit(tvShowInfo) {
            @Override
            protected void onTextChanged(Editable s) {
                //文本发生改变,可以是用户输入或者是EditText.setText触发.(setDefaultText的时候不会回调)
                super.onTextChanged(s);
            }
        };
        tvShowInfo.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.checked:
                listData.setInformation(tvShowInfo.getText().toString());
                listData.setRemarks(editRemark.getText().toString());
                Intent intent = new Intent(ActivityEditInfo.this, MainFormActivity.class);
                intent.putExtra(MainFormActivity.LIST_DATA, listData);
                intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
                if (pos == -1) {
                    if (listData.getInformation().equals("")) {
                        setResult(RESULT_NOTHING_NEW, intent);
                    } else
                        setResult(RESULT_ADD_NEW, intent);
                } else
                    setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.redo:
                mPerformEdit.redo();
                break;
            case R.id.undo:
                mPerformEdit.undo();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}
