package com.example.yanghang.clipboard;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.FragmentDiary;
import com.example.yanghang.clipboard.Fragment.FragmentEditAbstract;
import com.example.yanghang.clipboard.Fragment.FragmentEditInfo;
import com.example.yanghang.clipboard.Fragment.FragmentToDo;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.util.ArrayList;
import java.util.List;

public class ActivityEditInfo extends AppCompatActivity implements FragmentDiary.OnFragmentInteractionListener{
    public static int RESULT_ADD_NEW = 345;
    public static int RESULT_NOTHING_NEW = 678;


    EditText editRemark;
    Spinner spinner;
    List<CatalogueInfos> mCatalogue;

    private ListData listData;
    private int pos;
    private ArrayAdapter<String> arr_adapter;
    private boolean isEdit = false;
    private boolean notShowSpinner=false;
    FragmentManager fragmentManager=getSupportFragmentManager();

    FragmentEditAbstract fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        Initial();
    }
    void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFragment, fragment);
        //下面这句会导致回退不彻底
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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

        String catalogueName=listData.getCatalogue();
        Log.d(MainFormActivity.TAG, "Initial: catalogueName="+catalogueName);

        editRemark = (EditText) findViewById(R.id.edit_remark);

        editRemark.setText(listData.getRemarks());
        editRemark.setFocusable(isEdit);

        spinner = (Spinner) findViewById(R.id.catalogue_spinner);
        mCatalogue = getCatalogue();
        List<String> catalogueNames = new ArrayList<>();
        for (int i = 0; i < mCatalogue.size(); i++)
        {
            catalogueNames.add(mCatalogue.get(i).getCatalogue());
        }

        arr_adapter = new ArrayAdapter<String>(this, R.layout.item_catalogue_selected, R.id.item_catalogue_tv, catalogueNames);
        //设置样式
        arr_adapter.setDropDownViewResource(R.layout.item_catalogue_pop);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        String catalogue = listData.getCatalogue();
        int index = catalogueNames.indexOf(catalogue);
        if (index != -1)
            spinner.setSelection(index, true);
        else spinner.setSelection(0, true);

        notShowSpinner = true;
        switch(catalogueName)
        {
            case FragmentCalendar.CALENDAR_CATALOGUE_NAME:
                editRemark.setFocusable(false);
                switch (listData.getRemarks())
                {
                    case "diary":
                        fragment = FragmentDiary.newInstance(listData.getContent(), isEdit);
//                notShowSpinner=true;//不仅修改的时候不能出现，而且新建的时候也不能出现
                        break;
                    default:
                        fragment = FragmentEditInfo.newInstance(listData.getContent(), isEdit);
                }
                break;


            case "待办事项":
                fragment = FragmentToDo.newInstance(listData.getContent(), isEdit);
                break;
            case "番剧":
                fragment = FragmentEditInfo.newInstance(listData.getContent(), false);

                break;
            default:
                if (catalogueName.equals(""))
                {
                    catalogueName=spinner.getSelectedItem().toString();
                    listData.setCatalogue(catalogueName);
                }

                fragment = FragmentEditInfo.newInstance(listData.getContent(), isEdit);
                notShowSpinner=false;

        }
        spinner.setVisibility(notShowSpinner||!isEdit ? View.GONE : View.VISIBLE);
        setFragment(fragment);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_checked:
                if (listData.getCatalogue().equals("番剧"))
                {
                    //对于新番，新建的时候只能设置Remake 不能通过ActivityEditInfo来设置content ，有单独的Activity来设置
                    listData.setContent("");
                }
                else{
                    listData.setContent(fragment.getString());
                }
                listData.setRemarks(editRemark.getText().toString());
                //只有calendar 这样在目录里面找不到的，才不需要手动设置
                if (!listData.getCatalogue().equals(FragmentCalendar.CALENDAR_CATALOGUE_NAME))
                listData.setCatalogue(spinner.getSelectedItem().toString());
                Intent intent = new Intent(ActivityEditInfo.this, MainFormActivity.class);
                intent.putExtra(MainFormActivity.LIST_DATA, listData);
                intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
                if (pos == -1) {
                    if (listData.getContent().equals("")&&listData.getRemarks().equals("")) {
                        setResult(RESULT_NOTHING_NEW, intent);
                    } else
                        setResult(RESULT_ADD_NEW, intent);
                } else
                    setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.menu_redo:
                fragment.redo();
                break;
            case R.id.menu_undo:
                fragment.undo();
                break;
            case R.id.menu_editable:
                isEdit = true;
                if (!listData.getCatalogue().equals(FragmentCalendar.CALENDAR_CATALOGUE_NAME))
                {
                    editRemark.setFocusable(true);
                    editRemark.requestFocus();
                    editRemark.setFocusableInTouchMode(true);
                }
                if (!listData.getCatalogue().equals("番剧"))
                {
                    fragment.enableEdit();
                }

                if (!notShowSpinner)
                spinner.setVisibility(View.VISIBLE);

                invalidateOptionsMenu();
        }
        return true;
    }

    public List<CatalogueInfos> getCatalogue() {
        return FileUtils.loadCatalogue(getFilesDir().getAbsolutePath());
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
