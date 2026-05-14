package com.chiemy.cardview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.example.cardview.R;


public class TestFragment extends Fragment{
	private TextView tv;
	private View root;
	private View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			 ViewGroup container,  Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.frag_layout, container,false);
		initUI(root);
		return root;
	}
	
	private void initUI(final View root) {
		root.setClickable(true);
		root.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {}
		});
		tv = (TextView) root.findViewById(R.id.textView);
		root.findViewById(R.id.button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				root.setClickable(false);
				root.animate()
				.rotationY(-90).setDuration(200)
				.setListener(new AnimatorListenerAdapter(){
					@Override
					public void onAnimationEnd(Animator animation) {
						root.clearAnimation();
						root.setVisibility(View.INVISIBLE);
						view.setEnabled(true);
					}
				});
			}
		});
	}

	public void show(final View view,Bundle bundle){
		view.setEnabled(false);
		this.view = view;
		String text = bundle.getString("text");
		tv.setText(text);
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
				view.setRotationY( 0);
			}
		});
	}
}
