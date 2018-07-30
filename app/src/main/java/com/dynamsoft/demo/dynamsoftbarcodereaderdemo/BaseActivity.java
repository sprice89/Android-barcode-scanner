package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Method;

/**
 * Created by Elemen on 2018/7/25.
 */
public abstract class BaseActivity extends AppCompatActivity {
	private Toolbar toolbar;
	private FrameLayout viewContent;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_top_bar);
		Logger.addLogAdapter(new AndroidLogAdapter());
		toolbar = findViewById(R.id.toolbar);
		viewContent = findViewById(R.id.fl_view_content);
		setSupportActionBar(toolbar);
		LayoutInflater.from(this).inflate(getLayoutId(), viewContent);
		toolbar.setOverflowIcon(getResources().getDrawable(R.mipmap.clock));
		init(savedInstanceState);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	protected abstract int getLayoutId();

	protected abstract void init(Bundle savedInstanceState);

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_camera, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menu != null) {
			if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
				try {
					Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
					method.setAccessible(true);
					method.invoke(menu, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}
}
