package com.cdol.androidtest.bus;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.io.CachedOutputStream;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidtest.R;

public class BusActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bus, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private TextView text;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_bus, container, false);
			
			Button bt_confirm = (Button)rootView.findViewById(R.id.bt_confirm);
			bt_confirm.setOnClickListener((OnClickListener) this);
			getBusData(rootView);
			
			return rootView;
		}
		
		@Override
		public void OnClick(View rootView){
			
		}
		
		public void getBusData(View rootView){
			String xmlTmp = "";
			
			try {
				xmlTmp = new BusTask().execute().get();
				
				text = (TextView)rootView.findViewById(R.id.busXml);
				text.setText(xmlTmp);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		private class BusTask extends AsyncTask<String, String, String>{ 
			@Override
			protected String doInBackground(String... params) {
				String addr = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByRtid"+"?ServiceKey=";
				String serviceKey = "ev5LosPVIB2lgO8EQWkwklWnglOiJ+H3+dXBjCOEys7cloGLNgQdLTcC7GOJAJ6RCcI3SXa+xr9hEFLUqKgJnA==";
				String parameter = "";
				
				try {
					System.out.println("********************시작*******************");
					//인증키(서비스키) url인코딩
					serviceKey = URLEncoder.encode(serviceKey, "UTF-8");

					parameter = parameter + "&" + "busRouteId=3014700";
					
					addr = addr + serviceKey + parameter;
					
					URL url = new URL(addr);
					InputStream in = url.openStream(); 
					CachedOutputStream bos = new CachedOutputStream();
					IOUtils.copy(in, bos);
					in.close();
					bos.close();
					
					System.out.println("********************결과*******************");
					System.out.println(bos.getOut().toString());
					System.out.println("******************************************");
					
					return bos.getOut().toString();
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return null;
			}
		}
	}

}
