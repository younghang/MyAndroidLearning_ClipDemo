package com.example.yanghang.clipboard;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.DBClipInfos.ListInfoDB;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.Log.CrashHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        replaceHeaderLayoutResId();
    }

    private void replaceHeaderLayoutResId() {
        try {
            ListAdapter adapter = getListAdapter();
            Class headerAdapterClazz = Class.forName("android.preference.PreferenceActivity$HeaderAdapter");
            if (!headerAdapterClazz.isInstance(adapter)) {
                return;
            }

            boolean ok = false;

            try {
                Field field = headerAdapterClazz.getDeclaredField("mLayoutResId");
                field.setAccessible(true);
                field.setInt(adapter, R.layout.item_preference_header);

                ok = true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            if (!ok) {
                try {
                    Field field = headerAdapterClazz.getDeclaredField("mInflater");
                    field.setAccessible(true);
                    field.set(adapter, new FakeLayoutInflater((LayoutInflater) field.get(adapter)));

                    ok = true;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }

            if (ok) {
                getListView().setDivider(getResources().getDrawable(R.drawable.press_up));
                getListView().setDividerHeight(0);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName) ||
                AboutPreferenceFragment.class.getName().equals(fragmentName);

    }

    public static class AboutPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference deleteFilePreference;
        private Preference logFilePreference;
        private Preference specialCataloguePreference;
        AlertDialog loadingDialog;
        File file;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            file = new File(getActivity().getCacheDir().getAbsolutePath() + CrashHandler.FileName);
            deleteFilePreference = findPreference("setting_about_delete_data_file");
            logFilePreference = findPreference("setting_about_log_file");
            specialCataloguePreference = findPreference("setting_about_special_catalogue");

            deleteFilePreference.setOnPreferenceClickListener(this);
            logFilePreference.setOnPreferenceClickListener(this);
            specialCataloguePreference.setOnPreferenceClickListener(this);

            logFilePreference.setSummary(FileUtils.getAutoFileOrFilesSize(file.getAbsolutePath()));
            deleteFilePreference.setSummary(FileUtils.getAutoFileOrFilesSize(getActivity().getDatabasePath(ListInfoDB.DB_NAME).getAbsolutePath())+"  共"+MainFormActivity.TotalDataCount+"条记录");

        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == deleteFilePreference) {
                showFileDeleteDialog();
                return true;
            }
            if (preference == logFilePreference) {
                showLogFileDialog();
                return true;
            }
            if (preference == specialCataloguePreference) {
                showSpecialCatalogueNames();
            }
            return false;
        }

        private void showSpecialCatalogueNames() {
            new AlertDialog.Builder(getActivity()).setTitle("特殊的目录名称")
                    .setMessage("待办事项, collect_calendar_catalogue[diary luser weight] ，番剧").setCancelable(true).show();
        }

        private void showLogFileDialog() {

            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.loading, null);
            final EditText editText = (EditText) view.findViewById(R.id.loadingEditText);
            final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));
//          去掉即可复制，用于解决textView不能滑动的问题
//            editText.setMovementMethod(new ScrollingMovementMethod());
            loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                    .setTitle("日志详情")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingDialog.setCancelable(false);
                            preventDismissDialog(loadingDialog);
                            editText.setVisibility(View.GONE);
                            progress.setVisibility(View.VISIBLE);
                            file.delete();
                            Toast.makeText(getActivity().getApplicationContext(), "日志已删除", Toast.LENGTH_SHORT).show();
                            dismissDialog(loadingDialog);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(loadingDialog);
                        }
                    }) .create();
            StringBuffer sb = new StringBuffer();
            try {

                BufferedReader br = new BufferedReader(new FileReader(file));
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editText.setText(sb.toString());
            editText.setKeyListener(null);
            editText.setBackground(null);
            editText.setSingleLine(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                editText.setTextColor(getActivity().getColor(R.color.message_text));
            } else {
                editText.setTextColor(getResources().getColor(R.color.message_text));
            }
            editText.setTextSize(15);
            progress.setVisibility(View.INVISIBLE);
            loadingDialog.show();
        }

        private void showFileDeleteDialog() {
            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.loading, null);
            final EditText editText = (EditText) view.findViewById(R.id.loadingEditText);
            final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));

            loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                    .setTitle("文件删除操作")
                    .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadingDialog.setCancelable(false);
                            preventDismissDialog(loadingDialog);
                            editText.setVisibility(View.GONE);
                            progress.setVisibility(View.VISIBLE);
                            new DBListInfoManager(getActivity()).deleteDatabase();
                            Toast.makeText(getActivity().getApplicationContext(), "数据已删除", Toast.LENGTH_SHORT).show();
                            dismissDialog(loadingDialog);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(loadingDialog);
                        }
                    }).create();
            editText.setText("确定删除所有记录项?");
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(false);
            editText.setBackground(null);
            progress.setVisibility(View.INVISIBLE);
            loadingDialog.show();
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        public static final int REQUEST_CODE_FILE = 2;
        public static final int REQUEST_CODE_CATALOGUE = 3;
        public static final int REQUEST_CODE_FILEPATH = 1;
        private static final String MSG_FILE = "saving_loading_file";

        private static final int MSG_SAVING_FILE_FINISH = 4561;
        private static final int MSG_SAVING_CATALOGUE_FINISH = 4562;
        private static final int MSG_SAVING_FILE_FAILED = 459;
        private static final int MSG_LOADING_FILE_FINISH = 51;
        private static final int MSG_LOADING_FILE_FAILED = 99;
        AlertDialog loadingDialog;
        private Preference filePathPreference;
        SwitchPreference encodePreference;

        private Preference fileNamePreference;
        private Preference catalogueNamePreference;
        private Preference catalogueImportPreference;
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                super.handleMessage(msg);
                int msgLoading = msg.getData().getInt(MSG_FILE);
                switch (msgLoading) {
                    case MSG_SAVING_FILE_FINISH:
                        dismissDialog(loadingDialog);
                        Toast.makeText(getActivity(), "文件保存在" + filePathPreference.getSummary() + "/" + fileNamePreference.getSummary() + (encodePreference.isChecked()?".sphykey":".json"), Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_SAVING_CATALOGUE_FINISH:
                        loadingDialog.dismiss();
                        Toast.makeText(getActivity(), "文件保存在" + filePathPreference.getSummary() + "/" + catalogueNamePreference.getSummary() + ".json", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_SAVING_FILE_FAILED:
                        dismissDialog(loadingDialog);
                        Toast.makeText(getActivity(), "文件保存失败，或没有权限访问外置存储卡", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_LOADING_FILE_FINISH:
//                        loadingDialog.dismiss();
                        dismissDialog(loadingDialog);
                        Toast.makeText(getActivity(), "加载完成，请关闭本程序，重新打开.", Toast.LENGTH_SHORT).show();
                        break;
                    case MSG_LOADING_FILE_FAILED:
//                        loadingDialog.dismiss();
                        dismissDialog(loadingDialog);
                        Toast.makeText(getActivity(), "数据加载失败，请检查密码是否正确或访问存储权限", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        private Preference fileImport;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            fileNamePreference = findPreference(getResources().getString(R.string.dataFileName));
            filePathPreference = findPreference(getResources().getString(R.string.dataFilePath));
            catalogueNamePreference = findPreference(getResources().getString(R.string.dataCatalogueExport));
            catalogueImportPreference = findPreference(getResources().getString(R.string.dataCatalogueImport));
            encodePreference = (SwitchPreference) findPreference("encodePreference");
            fileImport = findPreference(getResources().getString(R.string.dataImport));

            fileImport.setOnPreferenceClickListener(this);
            catalogueImportPreference.setOnPreferenceClickListener(this);
            catalogueNamePreference.setOnPreferenceClickListener(this);
            filePathPreference.setOnPreferenceClickListener(this);
            fileNamePreference.setOnPreferenceClickListener(this);
//            fileNamePreference.setOnPreferenceChangeListener(this);
//            filePathPreference.setOnPreferenceChangeListener(this);
            fileNamePreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(fileNamePreference.getKey(), ""));
            filePathPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(filePathPreference.getKey(), ""));
            fileImport.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(fileImport.getKey(), ""));
            catalogueNamePreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(catalogueNamePreference.getKey(), ""));
            catalogueImportPreference.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(catalogueImportPreference.getKey(), ""));


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                onDestroy();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == fileNamePreference) {
                showDialog(fileNamePreference);
                return true;
            }
            if (preference == catalogueNamePreference) {
                showDialog(catalogueNamePreference);
                return true;
            }
            if (preference == filePathPreference) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                try {
                    startActivityForResult(intent, REQUEST_CODE_FILEPATH);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "setting filePath:can't find activity", Toast.LENGTH_SHORT).show();
                }
                return true;

            }
            if (preference == catalogueImportPreference) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_CATALOGUE);
                return true;
            }
            if (preference == fileImport) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, REQUEST_CODE_FILE);
                return true;
            }
            return false;
        }
        private void loadClipsJson(final String filePath, final boolean encoded)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    List<ListData> listsNew = new ArrayList<ListData>();
                    try {
                        List<ListData> lists = FileUtils.loadListDatas(filePath,encoded);
                        List<CatalogueInfos> catalogue = FileUtils.loadCatalogue(getActivity().getFilesDir().getAbsolutePath());
                        DBListInfoManager dbListInfoManager = new DBListInfoManager(getActivity());
                        CatalogueAdapter catalogueAdapter = new CatalogueAdapter(catalogue, getActivity());
                        List<ListData> listsOrigin = dbListInfoManager.getDatas("");
                        for (int i = 0; i < lists.size(); i++) {
                            ListData ld = lists.get(i);
                            boolean IsItemAdd = true;
                            if (!catalogueAdapter.contains(ld.getCatalogue())) {
                                if (!ld.getCatalogue().equals(FragmentCalendar.CALENDAR_CATALOGUE_NAME))
                                catalogueAdapter.addItem(new CatalogueInfos(ld.getCatalogue(), ""));
                            }
                            for (int j = 0; j < listsOrigin.size(); j++) {
                                if (listsOrigin.get(j).getContent().equals(ld.getContent())) {
                                    IsItemAdd = false;
//                                        Log.v(MainFormActivity.TAG,"导入记录SettingActivity ：条目"+ld.getContent()+"已经存在，不再添加" ) ;
                                    break;
                                }
                            }
                            if (IsItemAdd) {
                                listsNew.add(ld);
                            }
                        }
                        FileUtils.saveCatalogue(getActivity().getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), true, "");
                        dbListInfoManager.insertDatas(listsNew);
                        data.putInt(MSG_FILE, MSG_LOADING_FILE_FINISH);
                    } catch (Exception e) {
                        data.putInt(MSG_FILE, MSG_LOADING_FILE_FAILED);
                        e.printStackTrace();
                    }
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }).start();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_FILEPATH) {
                Uri uri = data.getData();
                String file = FileUtils.getFilePathFromContentUri(getActivity(), uri);
                filePathPreference.setSummary(file);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(filePathPreference.getKey(), file).apply();
            } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_FILE) {
                Uri uri = data.getData();
                final String file = FileUtils.getFilePathFromContentUri(getActivity(), uri);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(fileImport.getKey(), file).apply();
                fileImport.setSummary(file);
                File fileImport = new File(file);
                String[] suf = fileImport.getName().split("\\.");
                String suffix="";
                if (suf.length>1)
                {
                    suffix=suf[1];
                }
                View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.loading, null);
                final EditText codeText = (EditText) view.findViewById(R.id.loadingEditText);
                final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));
                final String []seed=new String[1];
                    if (suffix.equals("json")){
                        loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                                .setTitle("Loading")
                                .setCancelable(false).create();

                        codeText.setVisibility(View.GONE);
                        loadingDialog.show();
                        loadClipsJson(file,false);
                    }else if (suffix.equals("sphykey")){
                        loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                                .setTitle("输入解密密码")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadingDialog.setCancelable(false);
                                        preventDismissDialog(loadingDialog);
                                        codeText.setVisibility(View.GONE);
                                        progress.setVisibility(View.VISIBLE);
                                        seed[0] = codeText.getText().toString();
                                        FileUtils.SEED = seed[0];
                                        loadClipsJson(file,true);
                                    }}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dismissDialog(loadingDialog);
                                    }
                                }).create();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            codeText.setTextColor(getActivity().getColor(R.color.message_text));
                        } else {
                            codeText.setTextColor(getResources().getColor(R.color.message_text));
                        }
                        codeText.requestFocus();
                        codeText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                codeText.requestFocus();
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(codeText, InputMethodManager.HIDE_IMPLICIT_ONLY);
                            }
                        });
                        codeText.requestFocus();
                        progress.setVisibility(View.INVISIBLE);
                        loadingDialog.show();

                    }else {
                        Toast.makeText(getActivity(),"文本类型错误，加载失败", Toast.LENGTH_SHORT).show();

                    }



            } else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_CATALOGUE) {
                Uri uri = data.getData();
                final String file = FileUtils.getFilePathFromContentUri(getActivity(), uri);
                //存储设置值和读取设置值，需要手动完成,不具备自动关联
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(catalogueImportPreference.getKey(), file).apply();
                catalogueImportPreference.setSummary(file);
                loadingDialog = new AlertDialog.Builder(getActivity()).setView(LayoutInflater.from(getActivity().getApplicationContext())
                        .inflate(R.layout.loading, null))
                        .setTitle("Loading")
                        .setCancelable(false).show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        Bundle data = new Bundle();

                        try {
                            List<CatalogueInfos> lists = FileUtils.loadCatalogueFromDisk(file);
                            List<CatalogueInfos> catalogue = FileUtils.loadCatalogue(getActivity().getFilesDir().getAbsolutePath());

                            CatalogueAdapter catalogueAdapter = new CatalogueAdapter(catalogue, getActivity());

                            for (int i = lists.size()-1; i>=0 ; i--) {
                                CatalogueInfos ld = lists.get(i);
                                if (!catalogueAdapter.contains(ld.getCatalogue())) {
                                    catalogueAdapter.addItem(ld);
                                }

                            }
                            FileUtils.saveCatalogue(getActivity().getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), true, "");
                            data.putInt(MSG_FILE, MSG_LOADING_FILE_FINISH);
                        } catch (Exception e) {
                            data.putInt(MSG_FILE, MSG_LOADING_FILE_FAILED);
                            e.printStackTrace();
                        }
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }).start();
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

        private void showDialog(final Preference preference) {
            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.dialog_data_save, null);
            final EditText editText = (EditText) view.findViewById(R.id.edit_text);
            editText.setText(preference.getSummary());
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("保存文件名")
                    .setView(view)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String str = editText.getText().toString();
                            final String[] seed = new String[1];
                            if (str != null && !new File(filePathPreference.getSummary().toString()).exists()) {
                                Toast.makeText(getActivity(), "请先选择正确的保存路径", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(preference.getKey(), str).apply();
                            preference.setSummary(str);

                            View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.loading, null);
                            final EditText codeText = (EditText) view.findViewById(R.id.loadingEditText);
                            final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));
                            if (encodePreference.isChecked() && preference == fileNamePreference) {
                                loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                                        .setTitle("输入加密密码")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                loadingDialog.setCancelable(false);
                                                preventDismissDialog(loadingDialog);
                                                codeText.setVisibility(View.GONE);
                                                progress.setVisibility(View.VISIBLE);
                                                seed[0] = codeText.getText().toString();
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Message msg = new Message();
                                                        Bundle data = new Bundle();
                                                        try {

                                                            FileUtils.SEED = seed[0];
                                                            boolean re = FileUtils.saveListData(new DBListInfoManager(getActivity()).getDatas(""), filePathPreference.getSummary().toString(), fileNamePreference.getSummary().toString(), true);
                                                            if (re)
                                                                data.putInt(MSG_FILE, MSG_SAVING_FILE_FINISH);
                                                            else
                                                                data.putInt(MSG_FILE, MSG_SAVING_FILE_FAILED);


                                                        } catch (Exception e) {
                                                            data.putInt(MSG_FILE, MSG_SAVING_FILE_FAILED);
                                                        }
                                                        msg.setData(data);
                                                        handler.sendMessage(msg);
                                                    }
                                                }).start();
                                                return;
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dismissDialog(loadingDialog);
                                            }
                                        }).create();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    codeText.setTextColor(getActivity().getColor(R.color.message_text));
                                } else {
                                    codeText.setTextColor(getResources().getColor(R.color.message_text));
                                }
                                codeText.requestFocus();
                                codeText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        codeText.requestFocus();
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.showSoftInput(codeText, InputMethodManager.HIDE_IMPLICIT_ONLY);
                                    }
                                });
                                codeText.requestFocus();
                                progress.setVisibility(View.INVISIBLE);
                                loadingDialog.show();

                            } else {
                                loadingDialog = new AlertDialog.Builder(getActivity()).setView(view)
                                        .setTitle("Loading")
                                        .setCancelable(false).create();

                                codeText.setVisibility(View.GONE);
                                loadingDialog.show();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = new Message();
                                    Bundle data = new Bundle();
                                    try {
                                        Thread.sleep(500);
                                        if (preference == fileNamePreference) {
                                            boolean re = FileUtils.saveListData(new DBListInfoManager(getActivity()).getDatas(""), filePathPreference.getSummary().toString(), fileNamePreference.getSummary().toString(), false);
                                            if (re) data.putInt(MSG_FILE, MSG_SAVING_FILE_FINISH);
                                            else data.putInt(MSG_FILE, MSG_SAVING_FILE_FAILED);

                                        } else if (preference == catalogueNamePreference) {
                                            boolean re = FileUtils.saveCatalogue(filePathPreference.getSummary().toString(), FileUtils.loadCatalogue(getActivity().getFilesDir().getAbsolutePath()), false, catalogueNamePreference.getSummary().toString());
                                            if (re)
                                                data.putInt(MSG_FILE, MSG_SAVING_CATALOGUE_FINISH);
                                            else data.putInt(MSG_FILE, MSG_SAVING_FILE_FAILED);
                                        }

                                    } catch (Exception e) {
                                        data.putInt(MSG_FILE, MSG_SAVING_FILE_FAILED);
                                    }
                                    msg.setData(data);
                                    handler.sendMessage(msg);
                                }
                            }).start();
                        }
                        }
                    })
                    .setNegativeButton("取消", null)

                    .show();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
//            Log.v(MainFormActivity.TAG,"Preference Changed:"+ stringValue);
            //不会调用的，需要自己手动改
            preference.setSummary(stringValue);
            return true;
        }
    }
    /**
     * 关闭对话框
     */
    private static void dismissDialog(AlertDialog alertDialog) {
        try {
            Field field = alertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            field.set(alertDialog, true);
        } catch (Exception e) {
        }
        alertDialog.dismiss();
    }

    /**
     * 通过反射 阻止关闭对话框
     */
    private  static void preventDismissDialog(AlertDialog alertDialog) {
        try {
            Field field = alertDialog.getClass().getSuperclass().getDeclaredField("mShowing");
            field.setAccessible(true);
            //设置mShowing值，欺骗android系统
            field.set(alertDialog, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FakeLayoutInflater extends LayoutInflater {

        private LayoutInflater mInflater;

        protected FakeLayoutInflater(LayoutInflater inflater) {
            super(null);
            mInflater = inflater;
        }

        @Override
        public LayoutInflater cloneInContext(Context newContext) {
            return null;
        }

        @Override
        public View inflate(int resource, ViewGroup root, boolean attachToRoot) {
            return mInflater.inflate(R.layout.item_preference_header, root, attachToRoot);
        }
    }


}
