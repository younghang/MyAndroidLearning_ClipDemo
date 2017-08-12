package com.example.yanghang.clipboard.Fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.yanghang.clipboard.ActivityBangumi;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiData;
import com.example.yanghang.clipboard.R;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FragmentBangumi extends Fragment {

    private View root;
    private View view;
    private String TAG = "nihao_bangumi";

    private String name;
    private List<String> classifications = new ArrayList<>();
    private int episodes;
    private Map<String, Boolean> episodesState = new HashMap<>();
    private Map<String, String> comments = new HashMap<>();
    private String remark;
    private int grades;
    private String imageUrl;
    public boolean addNewBangumi = false;
    private List<String> episodeNames = new ArrayList<>();
    private Map<String, Boolean> episodeLikes = new HashMap<>();

    private TextView tvBangumiName;
    private ImageButton btnBangumiNameEdit;
    private TextView tvGrades;
    private TextView tvProgress;
    private TextView tvEpisodes;
    private ImageButton btnAddClassification;
    private ImageButton btnAddEpisode;
    private LinearLayout classificationsLinearLayout;
    private LinearLayout episodesLinearLayout;
    private EditText editEpisodeComment;
    private EditText editRemark;
    private OnClickListener onClassificationAddItemButtonListener;
    private View.OnLongClickListener onClassificationItemClicked;
    private OnClickListener onEpisodesAddItemButtonListener;
    private View.OnLongClickListener onEpisodeItemLongClicked;
    private OnClickListener onEpisodeItemClicked;

    private View tempViewForEpisode = null;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_bangumi, container, false);

        initUI(root);
        return root;
    }

    private void initUI(final View root) {

        root.setClickable(true);


        onClassificationAddItemButtonListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                showClassificationAddItemDialog(view);
            }
        };


        onClassificationItemClicked = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showClassificationAlterDialog(view);
                return true;
            }
        };


        onEpisodesAddItemButtonListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                showEpisodeAddItemDialog(view);
            }
        };
        onEpisodeItemLongClicked = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                showEpisodeAlterDialog(view);
                return true;
            }
        };
        onEpisodeItemClicked = new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (tempViewForEpisode != view) {
                    if (tempViewForEpisode != null) {
                        //防止切换之前已删除
                        if (comments.get(((Button) tempViewForEpisode).getText().toString()) != null)
                            comments.put(((Button) tempViewForEpisode).getText().toString(), editEpisodeComment.getText().toString());
                    }
                } else {
                    if (!editEpisodeComment.getText().toString().equals(""))
                    comments.put(((Button) view).getText().toString(), editEpisodeComment.getText().toString());
                }
                Log.d(TAG, "onClick: "+comments.get(((Button) view).getText()));
                tempViewForEpisode = view;
                editEpisodeComment.setText(comments.get(((Button) view).getText()) == null ? "" : comments.get(((Button) view).getText()));

            }
        };

        tvBangumiName = root.findViewById(R.id.bangumi_name);
        btnBangumiNameEdit = root.findViewById(R.id.bangumi_edit_name);
        btnBangumiNameEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlterNameDialog(tvBangumiName.getText().toString());
            }
        });

        tvGrades = root.findViewById(R.id.bangumi_grades);
        tvGrades.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlterGradesDialog(grades + "");
            }
        });
        tvProgress = root.findViewById(R.id.bangumi_progress);
        tvEpisodes = root.findViewById(R.id.bangumi_episode);
        tvEpisodes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlterEpisodesDialog(episodes + "");
            }
        });

        btnAddClassification = root.findViewById(R.id.bangumi_add_classification);
        btnAddClassification.setOnClickListener(onClassificationAddItemButtonListener);
        setImageColor(btnAddClassification, R.drawable.ic_add_circle_outline_white_24dp);
        btnAddEpisode = root.findViewById(R.id.bangumi_add_episode);
        btnAddEpisode.setOnClickListener(onEpisodesAddItemButtonListener);
        setImageColor(btnAddEpisode, R.drawable.ic_add_circle_outline_white_24dp);
        classificationsLinearLayout = root.findViewById(R.id.bangumi_classifications_linearLayout);
        episodesLinearLayout = root.findViewById(R.id.bangumi_episodes_linearLayout);

        editEpisodeComment = root.findViewById(R.id.bangumi_episode_comment);
        editEpisodeComment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.getId() == R.id.bangumi_episode_comment) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        editRemark = root.findViewById(R.id.bangumi_remarks);
        editRemark.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view.getId() == R.id.bangumi_remarks) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            view.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

    }

    private void setImageColor(ImageButton imageButton, int id) {
        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), id, getActivity().getTheme());
        //你需要改变的颜色
        vectorDrawableCompat.setTint(getResources().getColor(R.color.light_gray));
        imageButton.setImageDrawable(vectorDrawableCompat);
    }

    AlertDialog alertDialog;

    private void showEpisodeAddItemDialog(final View tag) {
        if (tempViewForEpisode!=null)
        comments.put(((Button) tempViewForEpisode).getText().toString(), editEpisodeComment.getText().toString());
        editEpisodeComment.setText("");
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_episode, null);
        final EditText editText = view.findViewById(R.id.bangumi_episode_name);
        final Switch likeSwitch = view.findViewById(R.id.bangumi_episode_switch_like);
        final Switch watchSwitch = view.findViewById(R.id.bangumi_episode_switch_watched);
        final Switch firstSwitch = view.findViewById(R.id.bangumi_episode_switch_first);
        editText.setText("第集");
        alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("添加集数").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTagName = editText.getText().toString();
                        if (episodeNames.contains(newTagName)) {
                            Toast.makeText(getActivity(), newTagName + "已经存在", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            return;
                        }
                        episodesState.put(newTagName, watchSwitch.isChecked());
                        comments.put(newTagName, "");
                        episodeNames.add(newTagName);
                        episodeLikes.put(newTagName, likeSwitch.isChecked());
                        if (firstSwitch.isChecked())
                        {
                            addEpisodeItemView(newTagName, 0);
                        }else
                        {
                            addEpisodeItemView(newTagName, -1);
                        }
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void showEpisodeAlterDialog(final View tag) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_episode, null);
        final EditText editText = view.findViewById(R.id.bangumi_episode_name);
        final Switch watchSwitch = view.findViewById(R.id.bangumi_episode_switch_watched);
        final Switch likeSwitch = view.findViewById(R.id.bangumi_episode_switch_like);
        final String oldTagName = ((Button) tag).getText().toString();
        final Switch firstSwitch = view.findViewById(R.id.bangumi_episode_switch_first);

        editText.setText(oldTagName);
        likeSwitch.setChecked(episodeLikes.get(oldTagName));
        watchSwitch.setChecked(episodesState.get(oldTagName));

//        Log.d(TAG, "showEpisodeAlterDialog: episodeNames="+episodeNames.toString());
        alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("修改集数").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTagName = editText.getText().toString();
                        boolean oldLike = episodeLikes.get(oldTagName);
                        int index = episodeNames.indexOf(oldTagName);
                        if (episodeNames.contains(newTagName) && !newTagName.equals(oldTagName)) {
                            Toast.makeText(getActivity(), newTagName + "已经存在", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                            return;

                        } else if (newTagName.equals(oldTagName)) {


                            if (firstSwitch.isChecked())
                            {
                                episodeNames.remove(index);
                                episodeNames.add(0, newTagName);
                            }
                            else {
                                episodeNames.set(index, newTagName);
                            }
                            comments.remove(oldTagName);
                            comments.put(newTagName, comments.get(oldTagName));


                        }
                        episodeLikes.remove(oldTagName);
//                        Log.d(TAG, "onClick: episodeLikes oldTag="+oldTagName+"  like="+oldLike);
                        episodesState.remove(oldTagName);

                        episodesState.put(newTagName, watchSwitch.isChecked());
                        episodeLikes.put(newTagName, likeSwitch.isChecked());
//                        Log.d(TAG, "onClick: episodeLikes newTag="+newTagName+"  like="+episodeLikes.get(newTagName));
                        int viewIndex;
                        View oldView;
                        if (oldLike) {
                            oldView = (View) tag.getTag();
                        } else {
                            oldView = tag;
                        }
                        viewIndex = episodesLinearLayout.indexOfChild(oldView);
                        if (firstSwitch.isChecked())
                        {
                            addEpisodeItemView(newTagName, 0);
                        }else
                        {
                            addEpisodeItemView(newTagName, viewIndex);
                        }
                        episodesLinearLayout.removeView(oldView);

                    }
                }).setNegativeButton("取消", null)
                .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: like =" + episodeLikes.get(oldTagName));
                        if (episodeLikes.get(oldTagName)) {
                            episodesLinearLayout.removeView((View) tag.getTag());
                        } else {
                            episodesLinearLayout.removeView(tag);
                        }
                        episodesState.remove(oldTagName);
                        episodeNames.remove(oldTagName);
                        episodeLikes.remove(oldTagName);
                        comments.remove(oldTagName);

                    }
                }).show();

    }

    private void showClassificationAlterDialog(final View tag) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        final String oldTagName = ((Button) tag).getText().toString();
        editText.setText(oldTagName);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("修改标签 ").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int index = classifications.indexOf(oldTagName);
                        String newTagName = editText.getText().toString();
                        if (classifications.contains(newTagName)) {
                            Toast.makeText(getActivity(), newTagName + "已经存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ((Button) tag).setText(newTagName);
                        ((ArrayList<String>) classifications).set(index, newTagName);
                    }
                }).setNegativeButton("取消", null)
                .setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        classificationsLinearLayout.removeView(view);

                        classificationsLinearLayout.removeView(tag);
                        ;
                        classifications.remove(((Button) tag).getText().toString());
                    }
                }).show();
    }

    private void showClassificationAddItemDialog(final View tag) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText("");
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("添加标签 ").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newTagName = editText.getText().toString();
                        if (classifications.contains(newTagName)) {
                            Toast.makeText(getActivity(), newTagName + "已经存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        classifications.add(newTagName);
                        addClassificationView(newTagName);
                    }
                }).setNegativeButton("取消", null).show();
    }


    private void showAlterNameDialog(String message) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText(message);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("修改名称").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        name = editText.getText().toString();
                        tvBangumiName.setText(name);
                    }
                }).setNegativeButton("取消", null).show();

    }

    private void showAlterEpisodesDialog(String message) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText(message);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("集数").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String epi = editText.getText().toString();
                        episodes = Integer.parseInt(epi);
                        tvEpisodes.setText("共" + epi);
                    }
                }).setNegativeButton("取消", null).show();
    }

    private void showAlterGradesDialog(String message) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText(message);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle("评价数").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String epi = editText.getText().toString();
                        grades = Integer.parseInt(epi);
                        tvGrades.setText(epi);
                    }
                }).setNegativeButton("取消", null).show();
    }


    public BangumiData hideFragment() {
        root.setClickable(false);

        root.animate()
                .rotationY(-90).setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        root.clearAnimation();
                        root.setVisibility(View.INVISIBLE);
                        view.setEnabled(true);
                    }
                });
        remark = editRemark.getText().toString();
        if (tempViewForEpisode != null) {
            String tagName = ((Button) tempViewForEpisode).getText().toString();
            comments.remove(tagName);
            comments.put(tagName, editEpisodeComment.getText().toString());
        }
        BangumiData bangumiData = new BangumiData(name, classifications, episodes, comments, remark, grades, imageUrl, episodesState, episodeNames, episodeLikes);

        classifications = null;
        episodeNames = null;
        comments = null;
        episodeLikes = null;
        episodesState = null;
        tempViewForEpisode = null;
        editEpisodeComment.setText("");
        editRemark.setText("");
        return bangumiData;
    }

    public void show(final View view, Bundle bundle) {
//        Log.d(TAG, "show: ");
        view.setEnabled(false);
        this.view = view;
        BangumiData bangumiData = (BangumiData) bundle.getSerializable("bangumi");
        addNewBangumi = bundle.getBoolean(ActivityBangumi.NEW_BANGUMI);
//        Log.d(TAG, "FragmentBangumi show: content is="+ JSON.toJSONString(bangumiData));
        initialUIs(bangumiData);


        view.setRotationY(0);
        root.setRotationY(-90);
        root.setVisibility(View.VISIBLE);

        view.animate().rotationY(90)
                .setDuration(300).setListener(null)
                .setInterpolator(new AccelerateInterpolator());


        root.animate()
                .rotationY(0).setDuration(200).setStartDelay(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setRotationY(0);
                    }
                });
    }

    private void initialUIs(BangumiData bangumiData) {
        name = "";
//        if (classifications != null)
//            classifications.clear();
//        if (episodeNames != null)
//            episodeNames.clear();
//        if (comments != null)
//            comments.clear();
//        if (episodeLikes != null)
//            episodeLikes.clear();
//        if (episodesState != null)
//            episodesState.clear();

        episodesLinearLayout.removeAllViews();
        classificationsLinearLayout.removeAllViews();

        name = bangumiData.getName();
        classifications = bangumiData.getClassifications();
        episodes = bangumiData.getEpisodes();
        episodesState = bangumiData.getEpisodesState();
        episodeNames = bangumiData.getEpisodeNames();
        episodeLikes = bangumiData.getEpisodeLikes();
        comments = bangumiData.getComments();
        remark = bangumiData.getRemark();
        grades = bangumiData.getGrades();
        imageUrl = bangumiData.getImageUrl();

        if (classifications == null) {
            classifications = new ArrayList<>();
        }

        tvBangumiName.setText(name == null ? "" : name);
        tvEpisodes.setText(episodes == 0 ? "未知集数" : "共" + episodes + "集");
        int watchedCount = 0;


        for (String key : episodesState.keySet()) {
            if (episodesState.get(key)) {
                watchedCount++;
            }
        }

        tvProgress.setText((episodes == 0 ? 0 : (int) (100 * watchedCount * 1.0f / Math.max(episodes, episodeNames.size()) + 0.5)) + "%");
        tvGrades.setText(grades + "");
        editRemark.setText(remark == null ? "" : remark);
        if (classifications != null) {
            for (int i = 0; i < classifications.size(); i++) {
                addClassificationView(classifications.get(i));
            }
        }

        for (int i = 0; i < episodeNames.size(); i++) {
            addEpisodeItemView(episodeNames.get(i), -1);
        }

        //to avoid the last view to become assigned with empty string once you click any episode item.
        tempViewForEpisode=null;



    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addEpisodeItemView(String key, int index) {
        Log.d(TAG, "addEpisodeItemView: index="+index);
        Button tv = new Button(getActivity());
        tempViewForEpisode=tv;
        tv.setText(key);
        tv.setOnLongClickListener(onEpisodeItemLongClicked);
        tv.setOnClickListener(onEpisodeItemClicked);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
        tv.setTextColor(getActivity().getColor(R.color.colorAccent));
        tv.setTextAppearance(R.style.borderless);
        if (episodeLikes.get(key)) {
            FrameLayout frameLayout = new FrameLayout(getActivity());
            LinearLayout.LayoutParams lf = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lf.setMargins(dpToPx(5), 0, dpToPx(5), 0);
            lf.gravity = Gravity.CENTER;  //必须要加上这句，setMargins才会起作用，而且此句还必须在setMargins下面

            frameLayout.setLayoutParams(lf);

            frameLayout.setBackground(getActivity().getDrawable(R.color.transparent));
            ImageView view = new ImageView(getActivity());
            FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(dpToPx(24), dpToPx(24));
            view.setLayoutParams(fp);
            view.setImageResource(R.drawable.ic_love);
            //  动态添加布局
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, dpToPx(40));
//            lp.setMargins(dpToPx(10), 0, dpToPx(5), 0);
            lp.gravity = Gravity.CENTER;
            tv.setLayoutParams(lp);
            setBackground(tv, episodesState.get(key));
            LinearLayout ll = new LinearLayout(getActivity());
            ll.addView(tv);
            frameLayout.addView(ll);
            frameLayout.addView(view);
            tv.setTag(frameLayout);
            if (index != -1) {
                episodesLinearLayout.addView(frameLayout, index);
            } else {
                episodesLinearLayout.addView(frameLayout);
            }

            return;
        }

        if (index != -1) {
            episodesLinearLayout.addView(initViewParams(tv, episodesState.get(key)), index);
        } else {
            episodesLinearLayout.addView(initViewParams(tv, episodesState.get(key)));
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void addClassificationView(String text) {
        Button tv = new Button(getActivity());
        tv.setText(text);
        tv.setOnLongClickListener(onClassificationItemClicked);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
        tv.setTextColor(getActivity().getColor(R.color.white));
        tv.setTextAppearance(R.style.borderless);
        classificationsLinearLayout.addView(initViewParams(tv, true));

    }


    private View initViewParams(View view, boolean watched) {
        //  动态添加布局
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(40));
        lp.setMargins(5, 5, 5, 5);
        lp.gravity = Gravity.CENTER;
        view.setLayoutParams(lp);
        setBackground(view, watched);

        return view;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackground(View view, boolean watched) {
        if (watched)
            view.setBackground(getActivity().getDrawable(R.drawable.button_background_green));
        else
            view.setBackground(getActivity().getDrawable(R.drawable.button_background_gray));
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f * (dp >= 0 ? 1 : -1));
    }

}
